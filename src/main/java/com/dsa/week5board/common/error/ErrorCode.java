package com.dsa.week5board.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT("E_400_001", HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    BOARD_NOT_FOUND("E_404_001", HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    DUPLICATE_BOARD("E_409_001", HttpStatus.CONFLICT, "이미 등록된 게시글입니다."),
    INTERNAL_ERROR("E_500_001", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;
}
