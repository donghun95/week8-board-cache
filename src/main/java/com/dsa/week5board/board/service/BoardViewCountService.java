package com.dsa.week5board.board.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dsa.week5board.board.repository.BoardRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardViewCountService {

    private static final String KEY_PREFIX = "week8:board:views:";
    private static final String KEY_PATTERN = KEY_PREFIX + "*";

    private final StringRedisTemplate redis;
    private final BoardRepository boardRepository;
    private final BoardCacheService boardCacheService;

    public Long increase(Long boardId) {
        return redis.opsForValue().increment(KEY_PREFIX + boardId);
    }

    public Long getCurrent(Long boardId) {
        String value = redis.opsForValue().get(KEY_PREFIX + boardId);
        return value == null ? 0L : Long.parseLong(value);
    }

    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public Map<Long, Long> flushToDatabase() {
        Map<Long, Long> flushed = new LinkedHashMap<>();

        ScanOptions options = ScanOptions.scanOptions()
                .match(KEY_PATTERN)
                .count(100)
                .build();

        try (Cursor<String> cursor = redis.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                String value = redis.opsForValue().getAndDelete(key);
                if (value == null) {
                    continue;
                }

                long delta = Long.parseLong(value);
                if (delta <= 0) {
                    continue;
                }

                Long boardId = parseBoardId(key);
                int updated = boardRepository.addViews(boardId, delta);
                if (updated > 0) {
                    flushed.put(boardId, delta);
                    log.info("Flushed views: boardId={}, delta={}", boardId, delta);
                }
            }
        }

        if (!flushed.isEmpty()) {
            boardCacheService.evictPopularTop10();
        }

        return flushed;
    }

    private Long parseBoardId(String key) {
        return Long.parseLong(key.substring(KEY_PREFIX.length()));
    }
}
