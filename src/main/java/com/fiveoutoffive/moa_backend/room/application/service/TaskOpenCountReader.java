package com.fiveoutoffive.moa_backend.room.application.service;

/** 방 나가기 확인 화면의 담당 미완료 업무 수. 구현은 업무 도메인이 담당한다. */
public interface TaskOpenCountReader {

    long countAssignedOpen(Long roomId, Long accountId);
}
