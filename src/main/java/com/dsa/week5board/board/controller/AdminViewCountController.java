package com.dsa.week5board.board.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dsa.week5board.board.service.BoardViewCountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin Views", description = "조회수 Redis 누적분 수동 반영 API")
@RestController
@RequestMapping("/api/admin/views")
@RequiredArgsConstructor
public class AdminViewCountController {

    private final BoardViewCountService boardViewCountService;

    @Operation(summary = "Redis 조회수 누적분 DB 반영", description = "스케줄러를 기다리지 않고 Redis 조회수 누적분을 즉시 DB에 더합니다.")
    @PostMapping("/flush")
    public ResponseEntity<Map<Long, Long>> flush() {
        return ResponseEntity.ok(boardViewCountService.flushToDatabase());
    }
}
