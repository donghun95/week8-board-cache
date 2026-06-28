package com.dsa.week5board.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dsa.week5board.board.domain.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Optional<Board> findByTitle(String title);

    List<Board> findAllByOrderByIdDesc();

    List<Board> findTop10ByOrderByViewsDesc();

    List<Board> findByWriterOrderByIdDesc(String writer);

    @Query("SELECT b FROM Board b WHERE b.views >= :threshold ORDER BY b.views DESC")
    List<Board> findPopular(@Param("threshold") int threshold);

    @Query("SELECT b FROM Board b LEFT JOIN FETCH b.comments WHERE b.id = :id")
    Optional<Board> findWithCommentsById(@Param("id") Long id);

    @Query("SELECT DISTINCT b FROM Board b LEFT JOIN FETCH b.comments ORDER BY b.id DESC")
    List<Board> findAllWithComments();

    @EntityGraph(attributePaths = "comments")
    @Query("SELECT b FROM Board b ORDER BY b.id DESC")
    List<Board> findAllWithCommentsByEntityGraph();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Board b SET b.views = b.views + :delta WHERE b.id = :id")
    int addViews(@Param("id") Long id, @Param("delta") long delta);
}
