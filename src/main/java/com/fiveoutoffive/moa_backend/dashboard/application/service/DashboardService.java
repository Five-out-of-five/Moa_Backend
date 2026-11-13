package com.fiveoutoffive.moa_backend.dashboard.application.service;

import com.fiveoutoffive.moa_backend.ai.application.service.AiGateway;
import com.fiveoutoffive.moa_backend.ai.domain.model.AiFeature;
import com.fiveoutoffive.moa_backend.dashboard.application.command.WidgetPlacement;
import com.fiveoutoffive.moa_backend.dashboard.application.result.DashboardResponse;
import com.fiveoutoffive.moa_backend.dashboard.application.result.LayoutRecommendResponse;
import com.fiveoutoffive.moa_backend.dashboard.application.usecase.DashboardUseCase;
import com.fiveoutoffive.moa_backend.dashboard.domain.event.MediationWidgetAddedEvent;
import com.fiveoutoffive.moa_backend.dashboard.domain.model.RoomLayoutSnapshot;
import com.fiveoutoffive.moa_backend.dashboard.domain.model.RoomWidget;
import com.fiveoutoffive.moa_backend.dashboard.domain.model.WidgetType;
import com.fiveoutoffive.moa_backend.dashboard.domain.repository.RoomLayoutSnapshotRepository;
import com.fiveoutoffive.moa_backend.dashboard.domain.repository.RoomWidgetRepository;
import com.fiveoutoffive.moa_backend.dashboard.exception.DashboardErrorCode;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.exception.GlobalErrorCode;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomCreatedEvent;
import com.fiveoutoffive.moa_backend.room.domain.model.Room;
import com.fiveoutoffive.moa_backend.room.domain.repository.RoomRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 대시보드 배치. 위젯은 배치만 저장하고 카드·업무 데이터와 연결하지 않는다. */
@Service
@Transactional
@RequiredArgsConstructor
public class DashboardService implements DashboardUseCase {

    private static final int GRID_COLUMNS = 12;
    private static final int MAX_HEIGHT = 8;
    private static final String RECOMMEND_PATH = "/v1/layouts/recommend";
    private static final List<WidgetPlacement> DEFAULT_LAYOUT = List.of(
            new WidgetPlacement(null, WidgetType.MOOD, 0, 0, 3, 4),
            new WidgetPlacement(null, WidgetType.IDEA_BOARD, 3, 0, 6, 7),
            new WidgetPlacement(null, WidgetType.CATCHUP_BRIEFING, 9, 0, 3, 7),
            new WidgetPlacement(null, WidgetType.TASK_BOARD, 3, 7, 6, 4));

    private final RoomWidgetRepository widgetRepository;
    private final RoomLayoutSnapshotRepository snapshotRepository;
    private final RoomRepository roomRepository;
    private final RoomAccessService access;
    private final RealtimePublisher realtime;
    private final AiGateway aiGateway;
    private final tools.jackson.databind.json.JsonMapper jsonMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Override
    public DashboardResponse get(Long accountId, Long roomId) {
        access.requireMember(roomId, accountId);
        return view(roomId, access.requireRoom(roomId).getLayoutVersion());
    }

    @Override
    public DashboardResponse addWidget(Long accountId, Long roomId, WidgetPlacement placement) {
        access.requireMember(roomId, accountId);
        if (placement.widgetType() == null) {
            throw new BusinessException(GlobalErrorCode.INVALID_REQUEST);
        }
        if (widgetRepository.existsByRoomIdAndWidgetType(roomId, placement.widgetType())) {
            throw new BusinessException(DashboardErrorCode.DASHBOARD_WIDGET_ALREADY_EXISTS);
        }
        List<WidgetPlacement> all = new ArrayList<>(currentPlacements(roomId));
        all.add(placement);
        validate(all);
        save(roomId, accountId, placement);
        // 위 중복 검사를 통과했으므로 이 방에는 같은 종류가 없었다.
        notifyIfMediationAdded(roomId, false, List.of(placement));
        return changed(roomId, accountId);
    }

    @Override
    public DashboardResponse removeWidget(Long accountId, Long roomId, Long widgetId) {
        access.requireMember(roomId, accountId);
        List<RoomWidget> widgets = widgetRepository.findByRoomId(roomId);
        if (widgets.stream().noneMatch(w -> w.getId().equals(widgetId))) {
            throw new BusinessException(DashboardErrorCode.DASHBOARD_WIDGET_NOT_FOUND);
        }
        if (widgets.size() == 1) {
            throw new BusinessException(DashboardErrorCode.DASHBOARD_LAST_WIDGET);
        }
        widgetRepository.deleteById(widgetId);
        return changed(roomId, accountId);
    }

    @Override
    public DashboardResponse updateLayout(Long accountId, Long roomId, List<WidgetPlacement> placements) {
        access.requireMember(roomId, accountId);
        Map<Long, RoomWidget> widgets = widgetRepository.findByRoomId(roomId).stream().collect(Collectors.toMap(RoomWidget::getId, Function.identity()));
        Map<Long, WidgetPlacement> incoming = new HashMap<>();
        for (WidgetPlacement placement : placements) {
            RoomWidget widget = widgets.get(placement.widgetId());
            if (widget == null) {
                throw new BusinessException(DashboardErrorCode.DASHBOARD_WIDGET_NOT_FOUND);
            }
            incoming.put(widget.getId(), new WidgetPlacement(widget.getId(), widget.getWidgetType(), placement.x(), placement.y(), placement.w(), placement.h()));
        }
        // 요청에 없는 위젯은 제자리에 있다고 보고 함께 검사한다.
        List<WidgetPlacement> all = widgets.values().stream().map(w -> incoming.getOrDefault(w.getId(), toPlacement(w))).toList();
        validate(all);
        incoming.values().forEach(p -> widgetRepository.save(widgets.get(p.widgetId()).toBuilder().posX(p.x()).posY(p.y()).width(p.w()).height(p.h()).updatedByAccountId(accountId).build()));
        return changed(roomId, accountId);
    }

    @Override
    public DashboardResponse reset(Long accountId, Long roomId) {
        access.requireMember(roomId, accountId);
        replaceAll(roomId, accountId, DEFAULT_LAYOUT);
        return changed(roomId, accountId);
    }

    @Override
    public LayoutRecommendResponse recommend(Long accountId, Long roomId, String purpose) {
        access.requireMember(roomId, accountId);
        LayoutRecommendResponse response = aiGateway.call(roomId, accountId, AiFeature.LAYOUT, RECOMMEND_PATH,
                Map.of("purpose", purpose, "widgetTypes", WidgetType.values(), "gridColumns", GRID_COLUMNS), LayoutRecommendResponse.class);
        validate(response.widgets());
        return response;
    }

    @Override
    public DashboardResponse applyRecommendation(Long accountId, Long roomId, List<WidgetPlacement> placements) {
        access.requireMember(roomId, accountId);
        validate(placements);
        boolean hadMediation = hasMediation(roomId);
        String snapshot = jsonMapper.writeValueAsString(currentPlacements(roomId));
        snapshotRepository.deleteByRoomId(roomId);
        snapshotRepository.flush();
        replaceAll(roomId, accountId, placements);
        notifyIfMediationAdded(roomId, hadMediation, placements);
        DashboardResponse response = changed(roomId, accountId);
        snapshotRepository.save(RoomLayoutSnapshot.builder().roomId(roomId).layoutJson(snapshot).createdByAccountId(accountId).build());
        return response;
    }

    @Override
    public DashboardResponse undoRecommendation(Long accountId, Long roomId) {
        access.requireMember(roomId, accountId);
        RoomLayoutSnapshot snapshot = snapshotRepository.findByRoomId(roomId).orElseThrow(() -> new BusinessException(DashboardErrorCode.DASHBOARD_UNDO_NOT_AVAILABLE));
        WidgetPlacement[] placements = jsonMapper.readValue(snapshot.getLayoutJson(), WidgetPlacement[].class);
        boolean hadMediation = hasMediation(roomId);
        replaceAll(roomId, accountId, List.of(placements));
        notifyIfMediationAdded(roomId, hadMediation, List.of(placements));
        return changed(roomId, accountId);
    }

    private boolean hasMediation(Long roomId) {
        return widgetRepository.existsByRoomIdAndWidgetType(roomId, WidgetType.MEDIATION);
    }

    /** 중재 위젯이 없던 방에 생기면 알린다. 위젯을 넣기 전에 올라온 진행 중 회차 카드도 충돌 분석하기 위함이다. */
    private void notifyIfMediationAdded(Long roomId, boolean hadMediation, List<WidgetPlacement> placements) {
        if (!hadMediation && placements.stream().anyMatch(p -> p.widgetType() == WidgetType.MEDIATION)) {
            eventPublisher.publishEvent(new MediationWidgetAddedEvent(roomId));
        }
    }

    @EventListener
    @Override
    public void onRoomCreated(RoomCreatedEvent event) {
        DEFAULT_LAYOUT.forEach(p -> save(event.roomId(), event.ownerAccountId(), p));
    }

    private void replaceAll(Long roomId, Long accountId, List<WidgetPlacement> placements) {
        widgetRepository.deleteByRoomId(roomId);
        widgetRepository.flush();
        placements.forEach(p -> save(roomId, accountId, p));
    }

    private void save(Long roomId, Long accountId, WidgetPlacement p) {
        widgetRepository.save(RoomWidget.builder().roomId(roomId).widgetType(p.widgetType()).posX(p.x()).posY(p.y()).width(p.w()).height(p.h()).updatedByAccountId(accountId).build());
    }

    /** 배치가 바뀌면 버전을 올리고, AI 되돌리기는 적용 직후에만 가능하므로 스냅샷을 지운 뒤 방 전체에 보낸다. */
    private DashboardResponse changed(Long roomId, Long accountId) {
        Room room = access.requireRoom(roomId);
        Long version = room.getLayoutVersion() + 1;
        roomRepository.save(room.toBuilder().layoutVersion(version).build());
        snapshotRepository.deleteByRoomId(roomId);
        snapshotRepository.flush();
        widgetRepository.flush();
        DashboardResponse response = view(roomId, version);
        realtime.toRoom(roomId, "LAYOUT_CHANGED", response, accountId);
        return response;
    }

    private DashboardResponse view(Long roomId, Long version) {
        return new DashboardResponse(version, widgetRepository.findByRoomId(roomId).stream().map(w -> new DashboardResponse.Widget(w.getId(), w.getWidgetType(), w.getPosX(), w.getPosY(), w.getWidth(), w.getHeight())).toList());
    }

    private List<WidgetPlacement> currentPlacements(Long roomId) {
        return widgetRepository.findByRoomId(roomId).stream().map(this::toPlacement).toList();
    }

    private WidgetPlacement toPlacement(RoomWidget w) {
        return new WidgetPlacement(w.getId(), w.getWidgetType(), w.getPosX(), w.getPosY(), w.getWidth(), w.getHeight());
    }

    private void validate(List<WidgetPlacement> placements) {
        // 종류 없음·같은 종류 중복은 DB(uk_room_widget_type)에서 500 으로 터지기 전에 막는다. (AI 구성 적용·추천 응답)
        if (placements.stream().anyMatch(p -> p.widgetType() == null)
                || placements.stream().map(WidgetPlacement::widgetType).distinct().count() != placements.size()) {
            throw new BusinessException(GlobalErrorCode.INVALID_REQUEST);
        }
        for (int i = 0; i < placements.size(); i++) {
            WidgetPlacement a = placements.get(i);
            int[] min = minSize(a.widgetType());
            if (a.w() < min[0] || a.h() < min[1]) {
                throw new BusinessException(DashboardErrorCode.DASHBOARD_WIDGET_TOO_SMALL);
            }
            if (a.x() < 0 || a.y() < 0 || a.x() + a.w() > GRID_COLUMNS || a.h() > MAX_HEIGHT) {
                throw new BusinessException(DashboardErrorCode.DASHBOARD_WIDGET_OUT_OF_GRID);
            }
            for (int j = i + 1; j < placements.size(); j++) {
                WidgetPlacement b = placements.get(j);
                if (a.x() < b.x() + b.w() && b.x() < a.x() + a.w() && a.y() < b.y() + b.h() && b.y() < a.y() + a.h()) {
                    throw new BusinessException(DashboardErrorCode.DASHBOARD_WIDGET_OVERLAP);
                }
            }
        }
    }

    private int[] minSize(WidgetType type) {
        return switch (type) {
            case IDEA_BOARD -> new int[]{6, 5};
            case TASK_BOARD -> new int[]{4, 4};
            case PROGRESS -> new int[]{2, 2};
            default -> new int[]{3, 3};
        };
    }
}
