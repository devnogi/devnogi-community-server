DROP TABLE user_summary;
DROP TABLE posts;
DROP TABLE post_history;
DROP TABLE post_likes;
DROP TABLE comments;
DROP TABLE comment_likes;
DROP TABLE post_reports;
DROP TABLE comment_reports;
DROP TABLE attachments;

-- ✅ 사용자 요약 캐시 테이블 (AUTH에서 정기적으로 캐싱 테이블)
CREATE TABLE user_summary (
                              id BIGINT PRIMARY KEY COMMENT 'Auth 서버의 사용자 ID',
                              nickname VARCHAR(50) NOT NULL COMMENT '사용자 닉네임',
                              profile_image_url VARCHAR(255) DEFAULT NULL COMMENT '프로필 이미지 URL',
                              level INT DEFAULT 1 COMMENT '사용자 레벨',
                              grade VARCHAR(30) DEFAULT 'user' COMMENT '등급 (user, manager, admin 등)',
                              UNIQUE KEY uq_user_summnary_nickname (nickname)
) COMMENT='사용자 요약 정보 캐시';

-- ✅ 게시판 (Board)
CREATE TABLE board (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL COMMENT '게시판 이름',
                       description TEXT COMMENT '설명',
                       top_category VARCHAR(50) COMMENT '게시판 상위 카테고리 (예: 일반, 공략, 거래 등)',
                       sub_category VARCHAR(50) COMMENT '게시판 하위 카테고리',
                       is_deleted BOOLEAN DEFAULT FALSE COMMENT '삭제 여부',
                       deleted_at DATETIME DEFAULT NULL COMMENT '삭제 시간',
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       created_by BIGINT COMMENT '생성자 ID',
                       updated_by BIGINT COMMENT '수정자 ID',
                       INDEX idx_board_category (top_category, sub_category),
                       INDEX idx_board_is_deleted (is_deleted)
) COMMENT='게시판';

-- ✅ 게시글 (Post)
CREATE TABLE post (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      board_id BIGINT NOT NULL COMMENT '소속 게시판 ID',
                      user_id BIGINT NOT NULL COMMENT '작성자 ID',
                      title VARCHAR(200) NOT NULL,
                      content TEXT NOT NULL,
                      view_count INT DEFAULT 0,
                      like_count INT DEFAULT 0,
                      comment_count INT DEFAULT 0,
                      is_draft BOOLEAN NOT NULL DEFAULT FALSE COMMENT '임시저장 여부 (true: 임시저장, false: 게시됨)',
                      is_blocked BOOLEAN DEFAULT FALSE,
                      is_deleted BOOLEAN DEFAULT FALSE,
                      deleted_at DATETIME DEFAULT NULL,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      updated_by BIGINT,
                      FOREIGN KEY (board_id) REFERENCES board(id),
                      INDEX idx_post_board_id (board_id),
                      INDEX idx_post_user_id (user_id),
                      INDEX idx_post_is_deleted (is_deleted),
                      FULLTEXT INDEX ft_post_title_content (title, content) COMMENT '제목/내용 검색용'
) COMMENT='게시글';

-- ✅ 공지글 (Announcement)
CREATE TABLE announcement (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              board_id BIGINT NOT NULL COMMENT '소속 게시판 ID',
                              user_id BIGINT NOT NULL COMMENT '작성자 ID',
                              title VARCHAR(200) NOT NULL,
                              content TEXT NOT NULL,
                              view_count INT DEFAULT 0,
                              like_count INT DEFAULT 0,
                              comment_count INT DEFAULT 0,
                              is_draft BOOLEAN NOT NULL DEFAULT FALSE COMMENT '임시저장 여부 (true: 임시저장, false: 게시됨)',
                              is_global BOOLEAN DEFAULT FALSE COMMENT '전체 공지 여부 (true: 전체, false: 게시판)',
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              updated_by BIGINT,
                              FOREIGN KEY (board_id) REFERENCES board(id),
                              INDEX idx_post_board_id (board_id),
                              INDEX idx_post_user_id (user_id),
                              FULLTEXT INDEX ft_post_title_content (title, content) COMMENT '제목/내용 검색용'
) COMMENT='공지글';

-- ✅ 댓글 (Comment)
CREATE TABLE comment (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         post_id BIGINT NOT NULL,
                         user_id BIGINT NOT NULL,
                         parent_comment_id BIGINT DEFAULT NULL COMMENT '대댓글의 경우 상위 댓글 ID',
                         content TEXT NOT NULL,
                         like_count INT DEFAULT 0,
                         is_deleted BOOLEAN DEFAULT FALSE,
                         is_blocked BOOLEAN DEFAULT FALSE,
                         deleted_at DATETIME DEFAULT NULL,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         created_by BIGINT,
                         updated_by BIGINT,
                         FOREIGN KEY (post_id) REFERENCES post(id),
                         INDEX idx_comment_post_id (post_id),
                         INDEX idx_comment_parent (parent_comment_id),
                         INDEX idx_comment_is_deleted (is_deleted)
) COMMENT='댓글 및 대댓글';

-- ✅ 좋아요 (Like)
CREATE TABLE post_like (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          post_id BIGINT NOT NULL,
                           user_id BIGINT NOT NULL,
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (post_id) REFERENCES post(id),
                           UNIQUE KEY uq_post_like_post_id_user_id(post_id, user_id),
                          INDEX idx_post_like_user (user_id)
) COMMENT='게시글 좋아요';

-- ✅ 신고 (Report)
CREATE TABLE report (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        target_type VARCHAR(10) NOT NULL COMMENT '신고 대상 타입(댓글 혹은 게시글 혹은 사용자)',
                        target_id BIGINT NULL COMMENT '신고 대상(댓글 혹은 게시글) ID',
                        target_user_id BIGINT NOT NULL COMMENT '신고 대상 사용자 ID',
                        user_id BIGINT NOT NULL COMMENT '신고자 ID',
                        category_cd VARCHAR(10) NOT NULL,
                        reason VARCHAR(255) NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        status_cd VARCHAR(10) DEFAULT 'REPORTED' NOT NULL COMMENT '신고 처리 상황(REPORTED, REJECT, ACCEPT)',
                        replied_at DATETIME NULL COMMENT '신고 처리일시',
                        replied_by BIGINT NULL COMMENT '신고 처리자 ID',
                        revived_at DATETIME NULL COMMENT '복구 처리일시',
                        revived_by BIGINT NULL COMMENT '복구 처리자 ID',
                        INDEX idx_report_target (target_type, target_id),
                        INDEX idx_report_user (user_id)
) COMMENT='신고';

-- ✅ 태그 (Tag)
CREATE TABLE tag (
                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                     name VARCHAR(50) NOT NULL UNIQUE COMMENT '태그명',
                     created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT='태그';

-- ✅ 게시글-태그 매핑 (PostTag)
CREATE TABLE post_tag (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          post_id BIGINT NOT NULL,
                          tag_id BIGINT NOT NULL,
                          FOREIGN KEY (post_id) REFERENCES post(id),
                          FOREIGN KEY (tag_id) REFERENCES tag(id),
                          INDEX idx_post_tag_tag_id (tag_id),
                          UNIQUE KEY uq_post_tag_post_id_tag_id (post_id, tag_id)
) COMMENT='게시글 - 태그 매핑';

-- ✅ 알림 (Notice)
CREATE TABLE notice (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        title VARCHAR(25) NOT NULL,
                        notice_type VARCHAR(25) NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        URL TEXT NOT NULL
) COMMENT='사용자 알림';