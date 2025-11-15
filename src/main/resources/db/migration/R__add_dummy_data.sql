DELETE from comment;
DELETE from post_tag;
DELETE from post_like;
DELETE from comment_like;
DELETE from tag;
DELETE from announcement;
DELETE from post;
DELETE from board;
DELETE from user_summary;
DELETE from post_meta;

-- board 더미 데이터
INSERT INTO board (id, name, description, top_category, sub_category)
VALUES (1, '자유게시판', '자유로운 이야기를 나누는 게시판입니다.', '일반', '자유'),
       (2, '공략 게시판', '게임 공략을 공유하는 게시판입니다.', '공략', '게임'),
       (3, '거래 게시판', '아이템 거래를 위한 게시판입니다.', '거래', '아이템');

-- user_summary 더미 데이터
INSERT INTO user_summary (id, nickname, profile_image_url, level, grade)
VALUES (1, 'Alice', 'https://example.com/profiles/alice.jpg', 5, 'USER'),
       (2, 'Bob', 'https://example.com/profiles/bob.jpg', 10, 'USER'),
       (3, 'Charlie', 'https://example.com/profiles/charlie.jpg', 15, 'ADMIN');

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

--  게시글 더미 데이터 (id 4 ~ 23)
INSERT INTO post (id, board_id, user_id, title, content, is_draft, is_blocked)
VALUES
    (4, 1, 2, '오늘 날씨 좋네요', '산책하기 딱 좋은 날씨입니다.', 0, 0),
    (5, 1, 3, '점심 추천 부탁드려요', '오늘 점심 뭐 먹을까요?', 0, 0),
    (6, 2, 1, '보스 공략 추가 팁', '지난번 공략에 이어 추가 팁을 알려드립니다.', 0, 0),
    (7, 2, 2, '히든 던전 공략', '숨겨진 던전을 찾는 법 공유합니다.', 0, 0),
    (8, 2, 3, '최종 보스 원킬 영상', '제가 직접 찍은 영상입니다.', 0, 0),
    (9, 3, 1, '아이템 교환 원합니다', '포션과 방패 교환하실 분?', 0, 0),
    (10, 3, 2, '무료 나눔합니다', '쓰지 않는 무기를 나눔합니다.', 0, 0),
    (11, 1, 3, '주말 모임 모집', '같이 게임할 사람 구합니다!', 0, 0),
    (12, 1, 1, '오늘의 잡담', '그냥 심심해서 써봅니다.', 0, 0),
    (13, 1, 2, '음악 추천', '게임할 때 들을만한 음악 추천해주세요.', 0, 0),
    (14, 2, 3, '레이드 파티 모집', '이번 주말 레이드 같이 하실 분?', 0, 0),
    (15, 2, 1, '초보자 가이드', '처음 시작하는 분들을 위한 가이드', 0, 0),
    (16, 2, 2, '스킬트리 공유', '제가 사용하는 스킬트리입니다.', 0, 0),
    (17, 3, 3, '아이템 판매', '희귀 아이템 팝니다.', 0, 0),
    (18, 3, 1, '교환 희망', '무기와 방어구 교환 원합니다.', 0, 0),
    (19, 1, 2, '주말에 뭐하세요?', '다들 주말에 뭐 하시나요?', 0, 0),
    (20, 1, 3, '취미 공유', '게임 외에 취미 있으신가요?', 0, 0),
    (21, 2, 1, '버그 제보', '어제 발견한 버그 제보합니다.', 0, 0),
    (22, 2, 2, '빠른 레벨업 방법', '제가 쓴 레벨업 방법 공유합니다.', 0, 0),
    (23, 3, 3, '아이템 경매합니다', '아이템 경매 시작합니다.', 0, 0);

--  댓글 더미 데이터 (id 6 ~ 25)
INSERT INTO comment (id, post_id, user_id, parent_comment_id, content, is_deleted, is_blocked)
VALUES
    (6, 4, 1, NULL, '저도 산책 나가야겠네요!', 0, 0),
    (7, 5, 2, NULL, '치킨 드시는 게 어떨까요?', 0, 0),
    (8, 5, 1, 7, '치킨 좋습니다!', 0, 0),
    (9, 6, 3, NULL, '좋은 팁 감사합니다.', 0, 0),
    (10, 7, 1, NULL, '숨겨진 던전 위치 공유해주실 수 있나요?', 0, 0),
    (11, 8, 2, NULL, '영상 잘 봤습니다!', 0, 0),
    (12, 9, 3, NULL, '저 교환 가능해요!', 0, 0),
    (13, 10, 1, NULL, '저 주시면 감사하겠습니다.', 0, 0),
    (14, 11, 2, NULL, '참여하고 싶습니다.', 0, 0),
    (15, 12, 3, NULL, '심심할 땐 역시 글쓰기!', 0, 0),
    (16, 13, 1, NULL, '저는 록 음악 추천드립니다.', 0, 0),
    (17, 14, 2, NULL, '레이드 참가합니다!', 0, 0),
    (18, 15, 3, NULL, '초보자인데 도움 많이 됩니다.', 0, 0),
    (19, 16, 1, NULL, '스킬트리 괜찮네요.', 0, 0),
    (20, 17, 2, NULL, '가격 얼마인가요?', 0, 0),
    (21, 18, 3, NULL, '저 교환 원합니다.', 0, 0),
    (22, 19, 1, NULL, '저는 집에서 게임합니다.', 0, 0),
    (23, 20, 2, NULL, '저는 독서합니다.', 0, 0),
    (24, 21, 3, NULL, '버그 제보 감사합니다.', 0, 0),
    (25, 22, 1, NULL, '좋은 정보네요!', 0, 0);

-- 좋아요 더미 데이터
INSERT INTO post_like (id, post_id, user_id, created_at)
VALUES
    (1, 4, 3, NOW()),
    (2, 5, 1, NOW()),
    (3, 6, 2, NOW()),
    (4, 7, 3, NOW()),
    (5, 8, 1, NOW()),
    (6, 9, 1, NOW());

INSERT INTO comment_like (id, comment_id, user_id, created_at)
VALUES
    (1, 6, 2, NOW()),
    (2, 7, 3, NOW()),
    (3, 9, 1, NOW()),
    (4, 14, 3, NOW()),
    (5, 18, 2, NOW());

INSERT INTO post_meta (post_id, view_count, like_count, comment_count)
SELECT
    p.id,
    0,
    COUNT(DISTINCT pl.id),
    COUNT(DISTINCT c.id)
FROM post p
         LEFT JOIN post_like pl ON p.id = pl.post_id
         LEFT JOIN comment c ON p.id = c.post_id
GROUP BY p.id;