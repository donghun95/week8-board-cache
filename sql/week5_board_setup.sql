CREATE DATABASE IF NOT EXISTS week5_board DEFAULT CHARACTER SET utf8mb4;
USE week5_board;

DROP TABLE IF EXISTS board;

CREATE TABLE board (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    content     TEXT NOT NULL,
    writer      VARCHAR(50) NOT NULL,
    views       INT NOT NULL DEFAULT 0,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_board_writer (writer),
    INDEX idx_board_created_id (created_at DESC, id DESC)
);

INSERT INTO board (title, content, writer, views, created_at)
VALUES
    ('Spring Boot 동적 쿼리 정리', 'where, if, foreach 실습용 게시글입니다.', 'donghun', 3, NOW() - INTERVAL 5 DAY),
    ('MyBatis foreach 예제', 'IN 절과 빈 리스트 방어를 확인합니다.', 'mentor', 7, NOW() - INTERVAL 4 DAY),
    ('Cursor 페이징 테스트', 'id 기준 cursor 페이징을 실습합니다.', 'donghun', 0, NOW() - INTERVAL 3 DAY),
    ('조회수 증가 동시성', 'views = views + 1 방식으로 증가합니다.', 'mentor', 10, NOW() - INTERVAL 2 DAY),
    ('REST API 설계 예고', '6주차 RESTful API 설계로 이어집니다.', 'donghun', 1, NOW() - INTERVAL 1 DAY);
