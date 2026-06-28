package com.dsa.week5board.board.dto;

import java.util.List;

import com.dsa.week5board.board.domain.Board;
import com.dsa.week5board.board.domain.Comment;

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
public class BoardWithCommentsResponse {

    private Long id;
    private String title;
    private String writer;
    private int commentCount;
    private List<CommentItem> comments;

    public static BoardWithCommentsResponse from(Board board) {
        List<CommentItem> comments = board.getComments().stream()
                .map(CommentItem::from)
                .toList();

        return BoardWithCommentsResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .writer(board.getWriter())
                .commentCount(comments.size())
                .comments(comments)
                .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentItem {
        private Long id;
        private String writer;
        private String content;

        public static CommentItem from(Comment comment) {
            return CommentItem.builder()
                    .id(comment.getId())
                    .writer(comment.getWriter())
                    .content(comment.getContent())
                    .build();
        }
    }
}
