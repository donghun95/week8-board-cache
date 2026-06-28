package com.dsa.week5board.board.dto;

import java.time.LocalDateTime;

import com.dsa.week5board.board.domain.Board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponse {

    private Long id;
    private String title;
    private String content;
    private String writer;
    private Integer views;
    private LocalDateTime createdAt;

    public static BoardResponse from(Board board) {
        return BoardResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getWriter())
                .views(board.getViews())
                .createdAt(board.getCreatedAt())
                .build();
    }
}
