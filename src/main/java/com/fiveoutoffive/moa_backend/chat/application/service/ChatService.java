package com.fiveoutoffive.moa_backend.chat.application.service;

import com.fiveoutoffive.moa_backend.chat.application.command.ChannelCreateCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.ChannelDeleteCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.ChannelMemberAddCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.ChannelUpdateCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.DirectChannelCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.MessageSendCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.MessageUpdateCommand;
import com.fiveoutoffive.moa_backend.chat.application.command.ReadCommand;
import com.fiveoutoffive.moa_backend.chat.application.result.ChannelMemberResponse;
import com.fiveoutoffive.moa_backend.chat.application.result.ChannelResponse;
import com.fiveoutoffive.moa_backend.chat.application.result.MessageResponse;
import com.fiveoutoffive.moa_backend.chat.application.result.MyChannelEvent;
import com.fiveoutoffive.moa_backend.chat.application.usecase.ChatUseCase;
import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelMemberStatus;
import com.fiveoutoffive.moa_backend.chat.domain.model.ChannelType;
import com.fiveoutoffive.moa_backend.chat.domain.model.ChatChannel;
import com.fiveoutoffive.moa_backend.chat.domain.model.ChatChannelMember;
import com.fiveoutoffive.moa_backend.chat.domain.model.ChatMessage;
import com.fiveoutoffive.moa_backend.chat.domain.model.ChatMessageFile;
import com.fiveoutoffive.moa_backend.chat.domain.model.MessageType;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatChannelMemberRepository;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatChannelRepository;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatMessageFileRepository;
import com.fiveoutoffive.moa_backend.chat.domain.repository.ChatMessageRepository;
import com.fiveoutoffive.moa_backend.chat.exception.ChatErrorCode;
import com.fiveoutoffive.moa_backend.file.application.service.FileAttachSupport;
import com.fiveoutoffive.moa_backend.file.domain.model.FileGroup;
import com.fiveoutoffive.moa_backend.global.common.api.response.CursorResponse;
import com.fiveoutoffive.moa_backend.global.common.api.response.MemberRef;
import com.fiveoutoffive.moa_backend.global.exception.BusinessException;
import com.fiveoutoffive.moa_backend.global.exception.GlobalErrorCode;
import com.fiveoutoffive.moa_backend.global.util.KstTime;
import com.fiveoutoffive.moa_backend.global.util.RedisKeys;
import com.fiveoutoffive.moa_backend.global.websocket.PresenceReader;
import com.fiveoutoffive.moa_backend.global.websocket.RealtimePublisher;
import com.fiveoutoffive.moa_backend.notification.application.service.NotificationSender;
import com.fiveoutoffive.moa_backend.notification.domain.model.NotificationType;
import com.fiveoutoffive.moa_backend.room.application.service.RoomAccessService;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomCreatedEvent;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomMemberJoinedEvent;
import com.fiveoutoffive.moa_backend.room.domain.event.RoomMemberLeftEvent;
import com.fiveoutoffive.moa_backend.room.domain.model.RoomMemberStatus;
import com.fiveoutoffive.moa_backend.room.exception.RoomErrorCode;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 채널·1:1 대화·메시지. 전송은 REST, 수신은 STOMP 다. */
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService implements ChatUseCase {

    private static final Duration DEDUP_TTL = Duration.ofMinutes(10);
    private static final int MAX_PAGE = 100;

    private final ChatChannelRepository channelRepository;
    private final ChatChannelMemberRepository memberRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatMessageFileRepository messageFileRepository;
    private final RoomAccessService access;
    private final FileAttachSupport fileAttachSupport;
    private final NotificationSender notificationSender;
    private final RealtimePublisher realtime;
    private final PresenceReader presenceReader;
    private final StringRedisTemplate redisTemplate;

    @Transactional(readOnly = true)
    @Override
    public List<ChannelResponse> getChannels(Long accountId, Long roomId) {
        access.requireMember(roomId, accountId);
        List<ChatChannel> channels = channelRepository.findByRoomIdAndDeletedAtIsNull(roomId);
        Map<Long, ChatChannelMember> mine = memberRepository.findByAccountIdAndChannelIdIn(accountId, channels.stream().map(ChatChannel::getId).toList()).stream()
                .collect(Collectors.toMap(ChatChannelMember::getChannelId, Function.identity()));
        return channels.stream().filter(c -> visible(c, mine.get(c.getId()))).map(c -> toResponse(c, mine.get(c.getId()), accountId)).toList();
    }

    @Override
    public ChannelResponse create(Long accountId, Long roomId, ChannelCreateCommand command) {
        access.requireMember(roomId, accountId);
        if (command.channelType() == ChannelType.DIRECT) {
            throw new BusinessException(GlobalErrorCode.INVALID_REQUEST);
        }
        requireUniqueName(roomId, command.name());
        ChatChannel channel = channelRepository.save(ChatChannel.builder().roomId(roomId).channelType(command.channelType()).name(command.name()).isDefault(false).creatorAccountId(accountId).build());
        ChatChannelMember me = join(channel.getId(), accountId);
        if (command.memberAccountIds() != null) {
            invite(channel, accountId, command.memberAccountIds());
        }
        return toResponse(channel, me, accountId);
    }

    @Transactional(readOnly = true)
    @Override
    public ChannelResponse getChannel(Long accountId, Long roomId, Long channelId) {
        access.requireMember(roomId, accountId);
        ChatChannel channel = find(roomId, channelId);
        ChatChannelMember me = memberRepository.findByChannelIdAndAccountId(channelId, accountId).orElse(null);
        if (!visible(channel, me)) {
            throw new BusinessException(ChatErrorCode.CHANNEL_NOT_FOUND);
        }
        return toResponse(channel, me, accountId);
    }

    @Override
    public ChannelResponse update(Long accountId, Long roomId, Long channelId, ChannelUpdateCommand command) {
        ChatChannel channel = managed(roomId, channelId, accountId);
        requireMutable(channel);
        if (command.name() != null && !command.name().equals(channel.getName())) {
            requireUniqueName(roomId, command.name());
        }
        if (command.channelType() == ChannelType.DIRECT) {
            throw new BusinessException(GlobalErrorCode.INVALID_REQUEST);
        }
        ChatChannel saved = channelRepository.save(channel.toBuilder().name(command.name() != null ? command.name() : channel.getName()).channelType(command.channelType() != null ? command.channelType() : channel.getChannelType()).build());
        ChannelResponse response = toResponse(saved, memberRepository.findByChannelIdAndAccountId(channelId, accountId).orElse(null), accountId);
        realtime.toChannel(channelId, "CHANNEL_UPDATED", response);
        return response;
    }

    @Override
    public void delete(Long accountId, Long roomId, Long channelId, ChannelDeleteCommand command) {
        ChatChannel channel = managed(roomId, channelId, accountId);
        requireMutable(channel);
        if (!command.confirmName().equals(channel.getName())) {
            throw new BusinessException(ChatErrorCode.CHANNEL_DELETE_CONFIRM_MISMATCH);
        }
        // 요구사항 51: 채널 삭제 시 메시지와 첨부도 함께 삭제한다. (방 파일 목록·저장 용량에서도 빠진다)
        var now = KstTime.nowUtc();
        List<Long> fileIds = messageFileRepository.findFileIdsByChannelId(channelId);
        if (!fileIds.isEmpty()) {
            fileAttachSupport.softDelete(fileIds);
        }
        messageRepository.softDeleteByChannelId(channelId, now);
        channelRepository.save(channel.toBuilder().deletedAt(now).build());
        realtime.toChannel(channelId, "CHANNEL_DELETED", Map.of("channelId", channelId));
    }

    @Override
    public ChannelResponse joinPublic(Long accountId, Long roomId, Long channelId) {
        access.requireMember(roomId, accountId);
        ChatChannel channel = find(roomId, channelId);
        if (channel.getChannelType() != ChannelType.PUBLIC) {
            throw new BusinessException(ChatErrorCode.CHANNEL_NOT_FOUND);
        }
        memberRepository.findByChannelIdAndAccountId(channelId, accountId).filter(m -> m.getStatus() == ChannelMemberStatus.JOINED).ifPresent(m -> {
            throw new BusinessException(ChatErrorCode.CHANNEL_MEMBER_ALREADY_JOINED);
        });
        return toResponse(channel, join(channelId, accountId), accountId);
    }

    @Override
    public void leave(Long accountId, Long roomId, Long channelId) {
        access.requireMember(roomId, accountId);
        ChatChannel channel = find(roomId, channelId);
        requireMutable(channel);
        ChatChannelMember me = joined(channelId, accountId);
        memberRepository.save(me.toBuilder().status(ChannelMemberStatus.LEFT).leftAt(KstTime.nowUtc()).build());
    }

    @Override
    public ChannelResponse acceptInvitation(Long accountId, Long roomId, Long channelId) {
        access.requireMember(roomId, accountId);
        ChatChannel channel = find(roomId, channelId);
        ChatChannelMember me = memberRepository.findByChannelIdAndAccountId(channelId, accountId).filter(m -> m.getStatus() == ChannelMemberStatus.INVITED)
                .orElseThrow(() -> new BusinessException(ChatErrorCode.CHANNEL_INVITATION_NOT_FOUND));
        return toResponse(channel, memberRepository.save(me.toBuilder().status(ChannelMemberStatus.JOINED).joinedAt(KstTime.nowUtc()).build()), accountId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChannelMemberResponse> getMembers(Long accountId, Long roomId, Long channelId) {
        access.requireMember(roomId, accountId);
        ChatChannel channel = find(roomId, channelId);
        if (!visible(channel, memberRepository.findByChannelIdAndAccountId(channelId, accountId).orElse(null))) {
            throw new BusinessException(ChatErrorCode.CHANNEL_NOT_FOUND);
        }
        return memberResponses(roomId, channelId);
    }

    @Override
    public List<ChannelMemberResponse> addMembers(Long accountId, Long roomId, Long channelId, ChannelMemberAddCommand command) {
        ChatChannel channel = managed(roomId, channelId, accountId);
        requireMutable(channel);
        invite(channel, accountId, command.accountIds());
        return memberResponses(roomId, channelId);
    }

    @Override
    public void kickMember(Long accountId, Long roomId, Long channelId, Long targetId) {
        ChatChannel channel = managed(roomId, channelId, accountId);
        requireMutable(channel);
        if (targetId.equals(channel.getCreatorAccountId())) {
            throw new BusinessException(ChatErrorCode.CHANNEL_CANNOT_KICK_CREATOR);
        }
        ChatChannelMember target = memberRepository.findByChannelIdAndAccountId(channelId, targetId).orElseThrow(() -> new BusinessException(RoomErrorCode.MEMBER_NOT_FOUND));
        memberRepository.save(target.toBuilder().status(ChannelMemberStatus.KICKED).leftAt(KstTime.nowUtc()).build());
        realtime.toUser(targetId, "channels", new MyChannelEvent("KICKED", channelId, roomId, null));
        // 이미 맺은 구독을 끊는다. 재연결하면 이 채널은 구독 권한 검사에서 거절된다.
        realtime.disconnectUser(String.valueOf(targetId));
    }

    @Override
    public ChannelResponse openDirect(Long accountId, Long roomId, DirectChannelCommand command) {
        access.requireMember(roomId, accountId);
        Long target = command.targetAccountId();
        if (accountId.equals(target) || !access.isActiveMember(roomId, target)) {
            throw new BusinessException(ChatErrorCode.CHANNEL_DM_TARGET_INVALID);
        }
        String dmKey = Math.min(accountId, target) + ":" + Math.max(accountId, target);
        Optional<ChatChannel> existing = channelRepository.findByRoomIdAndDmKey(roomId, dmKey);
        if (existing.isPresent()) {
            ChatChannelMember me = memberRepository.findByChannelIdAndAccountId(existing.get().getId(), accountId).orElseThrow(() -> new BusinessException(ChatErrorCode.CHANNEL_NOT_FOUND));
            return toResponse(existing.get(), memberRepository.save(me.toBuilder().hiddenAt(null).build()), accountId);
        }
        ChatChannel channel = channelRepository.save(ChatChannel.builder().roomId(roomId).channelType(ChannelType.DIRECT).isDefault(false).dmKey(dmKey).creatorAccountId(accountId).build());
        join(channel.getId(), target);
        return toResponse(channel, join(channel.getId(), accountId), accountId);
    }

    @Override
    public void hideDirect(Long accountId, Long roomId, Long channelId) {
        access.requireMember(roomId, accountId);
        ChatChannel channel = find(roomId, channelId);
        if (channel.getChannelType() != ChannelType.DIRECT) {
            throw new BusinessException(ChatErrorCode.CHANNEL_NOT_DIRECT);
        }
        memberRepository.save(joined(channelId, accountId).toBuilder().hiddenAt(KstTime.nowUtc()).build());
    }

    @Transactional(readOnly = true)
    @Override
    public CursorResponse<MessageResponse> getMessages(Long accountId, Long roomId, Long channelId, Long before, Long after, int size) {
        access.requireMember(roomId, accountId);
        find(roomId, channelId);
        joined(channelId, accountId);
        int limit = Math.clamp(size, 1, MAX_PAGE);
        List<ChatMessage> fetched = after != null ? messageRepository.findAfter(channelId, after, limit + 1) : messageRepository.findBefore(channelId, before, limit + 1);
        return CursorResponse.of(toResponses(roomId, fetched), limit, MessageResponse::messageId);
    }

    @Override
    public MessageResponse send(Long accountId, Long roomId, Long channelId, MessageSendCommand command) {
        access.requireMember(roomId, accountId);
        ChatChannel channel = find(roomId, channelId);
        joined(channelId, accountId);
        List<Long> fileIds = command.fileIds() == null ? List.of() : command.fileIds();
        if (!StringUtils.hasText(command.content()) && fileIds.isEmpty()) {
            throw new BusinessException(ChatErrorCode.MESSAGE_EMPTY);
        }
        if (channel.getChannelType() == ChannelType.DIRECT && peerLeft(channel, accountId)) {
            throw new BusinessException(ChatErrorCode.CHANNEL_DM_PEER_LEFT);
        }
        String dedupKey = RedisKeys.MESSAGE_DEDUP_PREFIX + channelId + ":" + command.clientMessageId();
        String duplicated = redisTemplate.opsForValue().get(dedupKey);
        if (duplicated != null) {
            return messageRepository.findById(Long.valueOf(duplicated)).map(m -> toResponses(roomId, List.of(m)).getFirst()).orElseThrow(() -> new BusinessException(ChatErrorCode.MESSAGE_NOT_FOUND));
        }
        fileAttachSupport.requireAttachable(roomId, accountId, fileIds, FileGroup.CHAT_ATTACHMENT, 0);
        ChatMessage saved = messageRepository.save(ChatMessage.builder().channelId(channelId).senderAccountId(accountId)
                .messageType(StringUtils.hasText(command.content()) ? MessageType.TEXT : MessageType.FILE).content(StringUtils.hasText(command.content()) ? command.content() : null).build());
        for (int i = 0; i < fileIds.size(); i++) {
            messageFileRepository.save(ChatMessageFile.builder().messageId(saved.getId()).fileId(fileIds.get(i)).sortOrder(i).build());
        }
        redisTemplate.opsForValue().set(dedupKey, String.valueOf(saved.getId()), DEDUP_TTL);
        MessageResponse base = toResponses(roomId, List.of(saved)).getFirst();
        MessageResponse response = new MessageResponse(base.messageId(), base.channelId(), base.messageType(), base.sender(), base.content(), base.files(), false, false, base.createdAt(), command.clientMessageId());
        realtime.toChannel(channelId, "MESSAGE_CREATED", response);
        memberRepository.findByChannelIdAndStatusIn(channelId, List.of(ChannelMemberStatus.JOINED)).stream().filter(m -> !m.getAccountId().equals(accountId)).forEach(m -> {
            if (m.getHiddenAt() != null) {
                memberRepository.save(m.toBuilder().hiddenAt(null).build());
                realtime.toUser(m.getAccountId(), "channels", new MyChannelEvent("DIRECT_UNHIDDEN", channelId, roomId, null));
            }
            long lastRead = m.getLastReadMessageId() == null ? 0L : m.getLastReadMessageId();
            realtime.toUser(m.getAccountId(), "channels", new MyChannelEvent("UNREAD_CHANGED", channelId, roomId, messageRepository.countUnread(channelId, lastRead, m.getAccountId())));
        });
        return response;
    }

    @Override
    public MessageResponse updateMessage(Long accountId, Long roomId, Long channelId, Long messageId, MessageUpdateCommand command) {
        access.requireMember(roomId, accountId);
        find(roomId, channelId);
        ChatMessage saved = messageRepository.save(ownMessage(channelId, messageId, accountId).toBuilder().content(command.content()).editedAt(KstTime.nowUtc()).build());
        MessageResponse response = toResponses(roomId, List.of(saved)).getFirst();
        realtime.toChannel(channelId, "MESSAGE_UPDATED", response);
        return response;
    }

    @Override
    public void deleteMessage(Long accountId, Long roomId, Long channelId, Long messageId) {
        access.requireMember(roomId, accountId);
        find(roomId, channelId);
        ChatMessage saved = messageRepository.save(ownMessage(channelId, messageId, accountId).toBuilder().deletedAt(KstTime.nowUtc()).build());
        fileAttachSupport.softDelete(messageFileRepository.findByMessageIdInOrderBySortOrderAsc(List.of(messageId)).stream().map(ChatMessageFile::getFileId).toList());
        realtime.toChannel(channelId, "MESSAGE_DELETED", toResponses(roomId, List.of(saved)).getFirst());
    }

    /** 채널이 속한 방 기준의 표시 이름(방별 닉네임, 없으면 기본 이름). 참여 여부는 STOMP SEND 권한 검사가 먼저 확인한다. */
    @Transactional(readOnly = true)
    @Override
    public Optional<MemberRef> typingMember(Long accountId, Long channelId) {
        return channelRepository.findById(channelId)
                .filter(channel -> channel.getDeletedAt() == null)
                .map(channel -> access.memberRef(channel.getRoomId(), accountId));
    }

    @Override
    public void markRead(Long accountId, Long roomId, Long channelId, ReadCommand command) {
        access.requireMember(roomId, accountId);
        find(roomId, channelId);
        ChatChannelMember me = joined(channelId, accountId);
        if (me.getLastReadMessageId() == null || command.lastReadMessageId() > me.getLastReadMessageId()) {
            memberRepository.save(me.toBuilder().lastReadMessageId(command.lastReadMessageId()).build());
            realtime.toUser(accountId, "channels", new MyChannelEvent("UNREAD_CHANGED", channelId, roomId, messageRepository.countUnread(channelId, command.lastReadMessageId(), accountId)));
        }
    }

    @EventListener
    @Override
    public void onRoomCreated(RoomCreatedEvent event) {
        ChatChannel channel = channelRepository.save(ChatChannel.builder().roomId(event.roomId()).channelType(ChannelType.PUBLIC).name("전체").isDefault(true).creatorAccountId(event.ownerAccountId()).build());
        join(channel.getId(), event.ownerAccountId());
    }

    @EventListener
    @Override
    public void onMemberJoined(RoomMemberJoinedEvent event) {
        channelRepository.findFirstByRoomIdAndIsDefaultTrue(event.roomId()).ifPresent(channel -> join(channel.getId(), event.accountId()));
    }

    @EventListener
    @Override
    public void onMemberLeft(RoomMemberLeftEvent event) {
        ChannelMemberStatus status = event.status() == RoomMemberStatus.KICKED ? ChannelMemberStatus.KICKED : ChannelMemberStatus.LEFT;
        List<Long> channelIds = channelRepository.findByRoomIdAndDeletedAtIsNull(event.roomId()).stream().filter(c -> c.getChannelType() != ChannelType.DIRECT).map(ChatChannel::getId).toList();
        memberRepository.findByAccountIdAndChannelIdIn(event.accountId(), channelIds).forEach(m -> memberRepository.save(m.toBuilder().status(status).leftAt(KstTime.nowUtc()).build()));
    }

    private void invite(ChatChannel channel, Long actorId, List<Long> accountIds) {
        for (Long id : accountIds) {
            if (!access.isActiveMember(channel.getRoomId(), id)) {
                throw new BusinessException(RoomErrorCode.MEMBER_NOT_FOUND);
            }
            Optional<ChatChannelMember> existing = memberRepository.findByChannelIdAndAccountId(channel.getId(), id);
            if (existing.filter(m -> m.getStatus() == ChannelMemberStatus.JOINED || m.getStatus() == ChannelMemberStatus.INVITED).isPresent()) {
                throw new BusinessException(ChatErrorCode.CHANNEL_MEMBER_ALREADY_JOINED);
            }
            memberRepository.save(existing.map(m -> m.toBuilder().status(ChannelMemberStatus.INVITED).leftAt(null).build())
                    .orElseGet(() -> ChatChannelMember.builder().channelId(channel.getId()).accountId(id).status(ChannelMemberStatus.INVITED).build()));
            notificationSender.send(channel.getRoomId(), actorId, NotificationType.CHANNEL_INVITED, List.of(id), "채널에 초대되었습니다", channel.getName(), "CHANNEL", channel.getId());
            realtime.toUser(id, "channels", new MyChannelEvent("INVITED", channel.getId(), channel.getRoomId(), null));
        }
    }

    private ChatChannelMember join(Long channelId, Long accountId) {
        return memberRepository.save(memberRepository.findByChannelIdAndAccountId(channelId, accountId)
                .map(m -> m.toBuilder().status(ChannelMemberStatus.JOINED).joinedAt(KstTime.nowUtc()).leftAt(null).build())
                .orElseGet(() -> ChatChannelMember.builder().channelId(channelId).accountId(accountId).status(ChannelMemberStatus.JOINED).joinedAt(KstTime.nowUtc()).build()));
    }

    private boolean visible(ChatChannel channel, ChatChannelMember me) {
        boolean participating = me != null && (me.getStatus() == ChannelMemberStatus.JOINED || me.getStatus() == ChannelMemberStatus.INVITED);
        return switch (channel.getChannelType()) {
            case PUBLIC -> true;
            case PRIVATE -> participating;
            case DIRECT -> participating && me.getHiddenAt() == null;
        };
    }

    private boolean peerLeft(ChatChannel channel, Long accountId) {
        return Arrays.stream(channel.getDmKey().split(":")).map(Long::valueOf).filter(id -> !id.equals(accountId)).noneMatch(id -> access.isActiveMember(channel.getRoomId(), id));
    }

    private ChannelResponse toResponse(ChatChannel c, ChatChannelMember me, Long accountId) {
        long lastRead = me == null || me.getLastReadMessageId() == null ? 0L : me.getLastReadMessageId();
        long unread = me != null && me.getStatus() == ChannelMemberStatus.JOINED ? messageRepository.countUnread(c.getId(), lastRead, accountId) : 0;
        MemberRef peer = c.getChannelType() != ChannelType.DIRECT ? null : Arrays.stream(c.getDmKey().split(":")).map(Long::valueOf).filter(id -> !id.equals(accountId)).findFirst().map(id -> access.memberRef(c.getRoomId(), id)).orElse(null);
        return new ChannelResponse(c.getId(), c.getRoomId(), c.getChannelType(), c.getName(), c.getIsDefault(), c.getCreatorAccountId(), me == null ? "NONE" : me.getStatus().name(), unread, peer);
    }

    private List<ChannelMemberResponse> memberResponses(Long roomId, Long channelId) {
        List<ChatChannelMember> members = memberRepository.findByChannelIdAndStatusIn(channelId, List.of(ChannelMemberStatus.INVITED, ChannelMemberStatus.JOINED));
        List<Long> ids = members.stream().map(ChatChannelMember::getAccountId).toList();
        Map<Long, MemberRef> refs = access.memberRefs(roomId, ids);
        Set<Long> online = presenceReader.onlineOf(ids);
        return members.stream().map(m -> new ChannelMemberResponse(m.getAccountId(), refs.get(m.getAccountId()).displayName(), refs.get(m.getAccountId()).profileImageUrl(), m.getStatus(), online.contains(m.getAccountId()))).toList();
    }

    private List<MessageResponse> toResponses(Long roomId, List<ChatMessage> messages) {
        Map<Long, MemberRef> refs = access.memberRefs(roomId, messages.stream().map(ChatMessage::getSenderAccountId).filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, List<Long>> files = messages.isEmpty() ? Map.of() : messageFileRepository.findByMessageIdInOrderBySortOrderAsc(messages.stream().map(ChatMessage::getId).toList()).stream()
                .collect(Collectors.groupingBy(ChatMessageFile::getMessageId, Collectors.mapping(ChatMessageFile::getFileId, Collectors.toList())));
        return messages.stream().map(m -> {
            boolean deleted = m.getDeletedAt() != null;
            return new MessageResponse(m.getId(), m.getChannelId(), m.getMessageType(), refs.get(m.getSenderAccountId()), deleted ? null : m.getContent(),
                    deleted ? List.of() : fileAttachSupport.responses(files.getOrDefault(m.getId(), List.of())), m.getEditedAt() != null, deleted, m.getCreatedAt(), null);
        }).toList();
    }

    private void requireUniqueName(Long roomId, String name) {
        if (channelRepository.existsByRoomIdAndNameAndDeletedAtIsNull(roomId, name)) {
            throw new BusinessException(ChatErrorCode.CHANNEL_NAME_DUPLICATED);
        }
    }

    private void requireMutable(ChatChannel channel) {
        if (Boolean.TRUE.equals(channel.getIsDefault()) || channel.getChannelType() == ChannelType.DIRECT) {
            throw new BusinessException(ChatErrorCode.CHANNEL_DEFAULT_IMMUTABLE);
        }
    }

    private ChatChannel find(Long roomId, Long channelId) {
        return channelRepository.findById(channelId).filter(c -> c.getRoomId().equals(roomId) && c.getDeletedAt() == null).orElseThrow(() -> new BusinessException(ChatErrorCode.CHANNEL_NOT_FOUND));
    }

    /** 채널 생성자·방장만 통과시킨다. 방장도 보이지 않는 비공개 채널은 존재를 숨긴다. */
    private ChatChannel managed(Long roomId, Long channelId, Long accountId) {
        access.requireMember(roomId, accountId);
        ChatChannel channel = find(roomId, channelId);
        ChatChannelMember me = memberRepository.findByChannelIdAndAccountId(channelId, accountId).orElse(null);
        if (!visible(channel, me)) {
            throw new BusinessException(ChatErrorCode.CHANNEL_NOT_FOUND);
        }
        if (!accountId.equals(channel.getCreatorAccountId()) && !access.isOwner(roomId, accountId)) {
            throw new BusinessException(ChatErrorCode.CHANNEL_NO_PERMISSION);
        }
        return channel;
    }

    private ChatChannelMember joined(Long channelId, Long accountId) {
        return memberRepository.findByChannelIdAndAccountId(channelId, accountId).filter(m -> m.getStatus() == ChannelMemberStatus.JOINED)
                .orElseThrow(() -> new BusinessException(ChatErrorCode.CHANNEL_NOT_JOINED));
    }

    private ChatMessage ownMessage(Long channelId, Long messageId, Long accountId) {
        ChatMessage message = messageRepository.findById(messageId).filter(m -> m.getChannelId().equals(channelId)).orElseThrow(() -> new BusinessException(ChatErrorCode.MESSAGE_NOT_FOUND));
        if (!accountId.equals(message.getSenderAccountId())) {
            throw new BusinessException(ChatErrorCode.MESSAGE_NOT_SENDER);
        }
        if (message.getDeletedAt() != null) {
            throw new BusinessException(ChatErrorCode.MESSAGE_ALREADY_DELETED);
        }
        return message;
    }
}
