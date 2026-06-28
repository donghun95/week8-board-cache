CREATE DATABASE IF NOT EXISTS week5_board DEFAULT CHARACTER SET utf8mb4;
USE week5_board;

DROP TABLE IF EXISTS board_tags;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS tags;
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

CREATE TABLE comments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id    BIGINT NOT NULL,
    writer      VARCHAR(50) NOT NULL,
    content     TEXT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    INDEX idx_comments_board (board_id)
);

CREATE TABLE tags (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE board_tags (
    board_id BIGINT NOT NULL,
    tag_id   BIGINT NOT NULL,
    PRIMARY KEY (board_id, tag_id),
    CONSTRAINT fk_board_tags_board FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    CONSTRAINT fk_board_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    INDEX idx_board_tags_tag (tag_id)
);

INSERT INTO board (title, content, writer, views, created_at)
VALUES
    ('Spring Boot 동적 쿼리 정리', 'where, if, foreach 실습용 게시글입니다.', 'donghun', 3, NOW() - INTERVAL 5 DAY),
    ('MyBatis foreach 예제', 'IN 절과 빈 리스트 방어를 확인합니다.', 'mentor', 7, NOW() - INTERVAL 4 DAY),
    ('Cursor 페이징 테스트', 'id 기준 cursor 페이징을 실습합니다.', 'donghun', 0, NOW() - INTERVAL 3 DAY),
    ('조회수 증가 동시성', 'views = views + 1 방식으로 증가합니다.', 'mentor', 10, NOW() - INTERVAL 2 DAY),
    ('REST API 설계 예고', '6주차 RESTful API 설계로 이어집니다.', 'donghun', 1, NOW() - INTERVAL 1 DAY);

INSERT INTO comments (board_id, writer, content)
VALUES
    (1, 'donghun', '동적 쿼리 잘 정리됐네요.'),
    (1, 'mentor', '<where> 깔끔합니다.'),
    (1, 'reader', '실무에서도 자주 씁니다.'),
    (2, 'mentor', 'foreach 빈 리스트 방어 중요'),
    (3, 'donghun', 'cursor 좋네요.'),
    (3, 'mentor', 'limit + 1 트릭'),
    (4, 'reader', '동시성 부분 어렵네요.'),
    (5, 'donghun', 'REST 6주차 기대됩니다.');

INSERT INTO tags (name)
VALUES ('mybatis'), ('jpa'), ('paging'), ('concurrency'), ('rest');

INSERT INTO board_tags (board_id, tag_id)
VALUES
    (1, 1), (1, 3),
    (2, 1),
    (3, 1), (3, 3),
    (4, 1), (4, 4),
    (5, 5);
