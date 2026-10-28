-- 사용자 요약 정보 테이블 (캐시)
CREATE TABLE user_summary (
    user_id BIGINT PRIMARY KEY COMMENT '사용자 ID (Auth 서버 연동)',
    nickname VARCHAR(50) NOT NULL COMMENT '닉네임',
    role_name VARCHAR(50) COMMENT '사용자 권한 (예: ROLE_USER)',
    updated_at DATETIME COMMENT '정보 동기화 일시'
) COMMENT='Auth 서버에서 동기화된 사용자 요약 정보';

-- 게시글
CREATE TABLE posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시글 ID',
    user_id BIGINT NOT NULL COMMENT '작성자 ID',
    title VARCHAR(200) NOT NULL COMMENT '게시글 제목',
    content TEXT NOT NULL COMMENT '게시글 내용',
    view_count INT DEFAULT 0 COMMENT '조회수',
    like_count INT DEFAULT 0 COMMENT '좋아요 수',
    comment_count INT DEFAULT 0 COMMENT '댓글 수',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '삭제 여부',
    deleted_at DATETIME NULL COMMENT '삭제일시',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) COMMENT='게시글';

-- 게시글 이력 (수정 이력 관리용)
CREATE TABLE post_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이력 ID',
    post_id BIGINT NOT NULL COMMENT '원본 게시글 ID',
    title VARCHAR(200) NOT NULL COMMENT '수정 당시 제목',
    content TEXT NOT NULL COMMENT '수정 당시 내용',
    updated_at DATETIME NOT NULL COMMENT '수정 일시',
    editor_id BIGINT COMMENT '수정한 사용자 ID'
) COMMENT='게시글 수정 이력';

-- 게시글 좋아요
CREATE TABLE post_likes (
    like_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '좋아요 ID',
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    liked_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '좋아요 일시',
    UNIQUE KEY uniq_post_user (post_id, user_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) COMMENT='게시글 좋아요';

-- 댓글
CREATE TABLE comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '댓글 ID',
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    user_id BIGINT NOT NULL COMMENT '작성자 ID',
    parent_comment_id BIGINT DEFAULT NULL COMMENT '부모 댓글 ID (NULL이면 일반 댓글)',
    content TEXT NOT NULL COMMENT '댓글 내용',
    like_count INT DEFAULT 0 COMMENT '좋아요 수',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '삭제 여부',
    deleted_at DATETIME NULL COMMENT '삭제일시',
    INDEX idx_post_id (post_id),
    INDEX idx_parent_id (parent_comment_id),
    INDEX idx_user_id (user_id)
) COMMENT='댓글 및 대댓글';

-- 댓글 좋아요
CREATE TABLE comment_likes (
    like_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '좋아요 ID',
    comment_id BIGINT NOT NULL COMMENT '댓글 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    liked_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '좋아요 일시',
    UNIQUE KEY uniq_comment_user (comment_id, user_id),
    INDEX idx_comment_id (comment_id),
    INDEX idx_user_id (user_id)
) COMMENT='댓글 좋아요';

-- 게시글 신고
CREATE TABLE post_reports (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '신고 ID',
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    user_id BIGINT NOT NULL COMMENT '신고자 ID',
    reason VARCHAR(255) COMMENT '신고 사유',
    reported_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '신고 일시',
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) COMMENT='게시글 신고';

-- 댓글 신고
CREATE TABLE comment_reports (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '신고 ID',
    comment_id BIGINT NOT NULL COMMENT '댓글 ID',
    user_id BIGINT NOT NULL COMMENT '신고자 ID',
    reason VARCHAR(255) COMMENT '신고 사유',
    reported_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '신고 일시',
    INDEX idx_comment_id (comment_id),
    INDEX idx_user_id (user_id)
) COMMENT='댓글 신고';

-- 첨부파일
CREATE TABLE attachments (
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '첨부파일 ID',
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    original_filename VARCHAR(255) NOT NULL COMMENT '원본 파일명',
    stored_filename VARCHAR(255) NOT NULL COMMENT '저장된 파일명',
    file_size BIGINT COMMENT '파일 크기',
    content_type VARCHAR(100) COMMENT 'MIME 타입',
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '업로드 일시',
    INDEX idx_post_id (post_id)
) COMMENT='게시글 첨부파일';
