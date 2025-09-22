CREATE TABLE board_archive
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL COMMENT '게시판 ID',
    name VARCHAR(100) NOT NULL COMMENT '게시판 이름',
    description TEXT COMMENT '설명',
    top_category VARCHAR(50) COMMENT '게시판 상위 카테고리 (예: 일반, 공략, 거래 등)',
    sub_category VARCHAR(50) COMMENT '게시판 하위 카테고리',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT '생성자 ID',
    updated_by BIGINT COMMENT '수정자 ID',
    archived_at DATETIME COMMENT '이관 시간'
);

CREATE TABLE post_archive
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    board_id BIGINT NOT NULL COMMENT '소속 게시판 ID',
    user_id BIGINT NOT NULL COMMENT '작성자 ID',
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_draft BOOLEAN NOT NULL DEFAULT FALSE COMMENT '임시저장 여부 (true: 임시저장, false: 게시됨)',
    is_blocked BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    archived_at DATETIME COMMENT '이관 시간'
);

CREATE TABLE comment_archive
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL COMMENT '댓글 ID',
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_comment_id BIGINT DEFAULT NULL COMMENT '대댓글의 경우 상위 댓글 ID',
    content TEXT NOT NULL,
    is_blocked BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    archived_at DATETIME COMMENT '이관 시간'
);