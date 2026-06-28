package com.dsa.week5board.board.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dsa.week5board.board.dto.BoardPopularCacheResponse;
import com.dsa.week5board.board.dto.BoardResponse;
import com.dsa.week5board.board.repository.BoardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCacheService {

    private final BoardRepository boardRepository;

    @Cacheable(cacheNames = "board:popular", key = "'top10'")
    public BoardPopularCacheResponse findPopularTop10() {
        List<BoardResponse> items = boardRepository.findTop10ByOrderByViewsDesc().stream()
                .map(BoardResponse::from)
                .toList();
        return BoardPopularCacheResponse.builder()
                .items(items)
                .build();
    }

    @CacheEvict(cacheNames = "board:popular", key = "'top10'")
    public void evictPopularTop10() {
        // Cache eviction is handled by the annotation.
    }
}