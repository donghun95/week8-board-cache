package com.dsa.week5board.board.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dsa.week5board.board.dto.BoardCreateRequest;
import com.dsa.week5board.board.dto.BoardResponse;
import com.dsa.week5board.board.dto.BoardTitleUpdateRequest;
import com.dsa.week5board.board.dto.BoardWithCommentsResponse;
import com.dsa.week5board.board.service.BoardCacheService;
import com.dsa.week5board.board.service.BoardJpaService;
import com.dsa.week5board.board.service.BoardViewCountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Boards", description = "JPA 게시글 API")
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardJpaService boardJpaService;
    private final BoardCacheService boardCacheService;
    private final BoardViewCountService boardViewCountService;

    @Operation(summary = "JPA 게시글 등록", description = "Spring Data JPA save()로 게시글을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BoardResponse> create(@Valid @RequestBody BoardCreateRequest request) {
        BoardResponse response = boardJpaService.create(request);
        URI location = URI.create("/api/boards/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "인기 게시글 캐시 조회", description = "조회수 기준 Top 10 게시글을 Redis Cache로 캐싱합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기 게시글 조회 성공")
    })
    @GetMapping("/popular")
    public ResponseEntity<List<BoardResponse>> popular() {
        return ResponseEntity.ok(boardCacheService.findPopularTop10().getItems());
    }

    @Operation(summary = "JPA 게시글 단건 조회", description = "Spring Data JPA findById()로 게시글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> get(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id
    ) {
        return ResponseEntity.ok(boardJpaService.findById(id));
    }

    @Operation(summary = "Redis 조회수 증가", description = "DB UPDATE 없이 Redis INCR로 조회수를 증가시킵니다.")
    @PostMapping("/{id}/views/redis")
    public ResponseEntity<Map<String, Long>> increaseViewsInRedis(@PathVariable Long id) {
        Long count = boardViewCountService.increase(id);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "Redis 조회수 누적값 조회", description = "아직 DB에 반영되지 않은 Redis 조회수 누적값을 확인합니다.")
    @GetMapping("/{id}/views/redis")
    public ResponseEntity<Map<String, Long>> getRedisViews(@PathVariable Long id) {
        Long count = boardViewCountService.getCurrent(id);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "JPA 게시글 삭제", description = "Spring Data JPA deleteById()로 게시글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id
    ) {
        boardJpaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "JPA 게시글 제목 수정", description = "Dirty Checking으로 제목 변경 UPDATE를 발생시킵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JPA 게시글 제목 수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "게시글 없음", content = @Content)
    })
    @PatchMapping("/{id}/title")
    public ResponseEntity<BoardResponse> renameTitle(
            @PathVariable Long id,
            @Valid @RequestBody BoardTitleUpdateRequest request
    ) {
        return ResponseEntity.ok(boardJpaService.renameTitle(id, request.getTitle()));
    }

    @Operation(summary = "JPA 게시글 + 댓글 단건 조회", description = "Fetch Join으로 게시글과 댓글을 한 번에 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JPA 게시글 + 댓글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음", content = @Content)
    })
    @GetMapping("/{id}/with-comments")
    public ResponseEntity<BoardWithCommentsResponse> getWithComments(@PathVariable Long id) {
        return ResponseEntity.ok(boardJpaService.findWithComments(id));
    }

    @Operation(summary = "JPA 목록 조회 N+1 비교", description = "lazy=true이면 N+1을 의도적으로 발생시키고, fetch=join이면 Fetch Join으로 해결합니다.")
    @GetMapping
    public ResponseEntity<List<BoardWithCommentsResponse>> list(
            @RequestParam(defaultValue = "true") boolean lazy,
            @RequestParam(required = false) String fetch
    ) {
        if ("join".equalsIgnoreCase(fetch) || !lazy) {
            return ResponseEntity.ok(boardJpaService.listFetchJoin());
        }
        return ResponseEntity.ok(boardJpaService.listLazy());
    }
}
