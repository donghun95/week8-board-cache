package com.dsa.week5board.board.exception;

import com.dsa.week5board.common.error.BusinessException;
import com.dsa.week5board.common.error.ErrorCode;

public class BoardNotFoundException extends BusinessException {

    public BoardNotFoundException(Long id) {
        super(ErrorCode.BOARD_NOT_FOUND, "게시글을 찾을 수 없습니다: " + id);
    }
}
