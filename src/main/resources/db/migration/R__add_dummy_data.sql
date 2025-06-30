-- board 더미 데이터
INSERT INTO board (name, description, top_category, sub_category)
VALUES ('자유게시판', '자유로운 이야기를 나누는 게시판입니다.', '일반', '자유'),
       ('공략 게시판', '게임 공략을 공유하는 게시판입니다.', '공략', '게임'),
       ('거래 게시판', '아이템 거래를 위한 게시판입니다.', '거래', '아이템');

-- user_summary 더미 데이터
INSERT INTO user_summary (id, nickname, profile_image_url, level, grade)
VALUES (1, 'Alice', 'https://example.com/profiles/alice.jpg', 5, 'user'),
       (2, 'Bob', 'https://example.com/profiles/bob.jpg', 10, 'manager'),
       (3, 'Charlie', 'https://example.com/profiles/charlie.jpg', 15, 'admin');

-- post 더미 데이터
INSERT INTO post (board_id, user_id, title, content, view_count, like_count, comment_count, is_draft, is_blocked)
VALUES (1, 1, '안녕하세요!', '처음으로 글을 써봅니다!', 10, 2, 1, 0, 0),
       (2, 2, '보스 공략법', '이 보스를 쉽게 이기는 방법을 공유합니다.', 200, 15, 5, 0, 0),
       (3, 3, '레어 아이템 판매합니다.', '관심 있으시면 댓글 남겨주세요!', 50, 4, 2, 0, 0);

-- comment 더미 데이터
INSERT INTO comment (post_id, user_id, parent_comment_id, content, like_count, is_deleted, is_blocked)
VALUES (1, 2, NULL, '첫 글 축하드려요!', 1, 0, 0),
       (2, 3, NULL, '좋은 공략 감사합니다.', 3, 0, 0),
       (2, 1, 2, '저도 이 방법으로 성공했습니다!', 2, 0, 0), -- 대댓글
       (3, 1, NULL, '구매 희망합니다!', 0, 0, 0),
       (3, 2, 4, '저도 관심 있어요.', 1, 0, 0);