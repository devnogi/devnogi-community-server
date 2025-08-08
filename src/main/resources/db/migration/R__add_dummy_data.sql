DELETE from comment;
DELETE from post;
DELETE from board;
DELETE from user_summary;

-- board 더미 데이터
INSERT INTO board (id, name, description, top_category, sub_category)
VALUES (1, '자유게시판', '자유로운 이야기를 나누는 게시판입니다.', '일반', '자유'),
       (2, '공략 게시판', '게임 공략을 공유하는 게시판입니다.', '공략', '게임'),
       (3, '거래 게시판', '아이템 거래를 위한 게시판입니다.', '거래', '아이템');

-- user_summary 더미 데이터
INSERT INTO user_summary (id, nickname, profile_image_url, level, grade)
VALUES (1, 'Alice', 'https://example.com/profiles/alice.jpg', 5, 'user'),
       (2, 'Bob', 'https://example.com/profiles/bob.jpg', 10, 'manager'),
       (3, 'Charlie', 'https://example.com/profiles/charlie.jpg', 15, 'admin');

-- post 더미 데이터
INSERT INTO post (id, board_id, user_id, title, content, is_draft, is_blocked)
VALUES (1, 1, 1, '안녕하세요!', '처음으로 글을 써봅니다!', 0, 0),
       (2, 2, 2, '보스 공략법', '이 보스를 쉽게 이기는 방법을 공유합니다.', 0, 0),
       (3, 3, 3, '레어 아이템 판매합니다.', '관심 있으시면 댓글 남겨주세요!', 0, 0);

-- comment 더미 데이터
INSERT INTO comment (id, post_id, user_id, parent_comment_id, content, is_deleted, is_blocked)
VALUES (1, 1, 2, NULL, '첫 글 축하드려요!', 0, 0),
       (2, 2, 3, NULL, '좋은 공략 감사합니다.', 0, 0),
       (3, 2, 1, 2, '저도 이 방법으로 성공했습니다!', 0, 0), -- 대댓글
       (4, 3, 1, NULL, '구매 희망합니다!', 0, 0),
       (5, 3, 2, 4, '저도 관심 있어요.', 0, 0);

-- tag 더미 데이터
INSERT INTO tag (id, name)
VALUES (1, '보스'),
       (2, '공략');

-- postTag 더미 데이터
INSERT INTO post_tag(id, post_id, tag_id)
VALUES (1,2,1),
       (2,2,2);