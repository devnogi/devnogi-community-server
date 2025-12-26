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
VALUES (1, '류트', '에린의 중심, 류트 서버 게시판입니다.', '서버', '류트'),
       (2, '만돌린', '음유시인의 노래가 흐르는 만돌린 서버입니다.', '서버', '만돌린'),
       (3, '하프', '낭만과 예술의 하프 서버입니다.', '서버', '하프'),
       (4, '울프', '용맹한 전사들의 울프 서버입니다.', '서버', '울프');

-- user_summary 더미 데이터
INSERT INTO user_summary (id, nickname, profile_image_url, level, grade)
VALUES (1, '나오', 'https://example.com/profiles/nao.jpg', 500, 'ADMIN'),
       (2, '퍼거스', 'https://example.com/profiles/fergus.jpg', 20000, 'USER'),
       (3, '던컨', 'https://example.com/profiles/duncan.jpg', 45000, 'USER');

-- tag 더미 데이터
INSERT INTO tag (id, name)
VALUES (1, '의장'), -- 패션
       (2, '레이드');

-- post 더미 데이터
INSERT INTO post (id, board_id, user_id, title, content, is_draft, is_blocked)
VALUES (1, 1, 1, '류트 서버 뉴비입니다!', '던바튼 광장에 사람이 정말 많네요. 잘 부탁드립니다.', 0, 0),
       (2, 2, 2, '웨폰 브레이커의 사과문', '수리하다 손이 미끄러졌습니다. 보상은 없습니다.', 0, 0),
       (3, 3, 3, '켈틱 드루이드 스태프 S50 팝니다', '마공 옵션 최상급입니다. 류트서버 숲 거래 가능.', 0, 0);

-- postTag 더미 데이터
INSERT INTO post_tag(id, post_id, tag_id)
VALUES (1, 2, 2), -- 퍼거스 글에 레이드 태그 (실수 컨셉)
       (2, 3, 1); -- 아이템 글에 의장 태그

-- comment 더미 데이터
INSERT INTO comment (id, post_id, user_id, parent_comment_id, content, is_deleted, is_blocked)
VALUES (1, 1, 2, NULL, '환영하네! 무기 수리는 나에게 맡기게.', 0, 0),
       (2, 2, 3, NULL, '자네... 내 켈틱 로열 나이트 소드도 부러뜨렸지?', 0, 0),
       (3, 2, 1, 2, '어쩐지 제 류트도 내구도가 0이 되었어요...', 0, 0), -- 대댓글
       (4, 3, 1, NULL, '혹시 의장템 교환도 받으시나요?', 0, 0),
       (5, 3, 2, 4, '수리비 대신 받을 수 있겠나?', 0, 0);

--  게시글 더미 데이터 확장
INSERT INTO post (id, board_id, user_id, title, content, is_draft, is_blocked)
VALUES
    (4, 1, 2, '오늘 이멘 마하 날씨 맑음', '프레시즈 타임이라 산책하기 딱 좋네.', 0, 0),
    (5, 1, 3, '페스티벌 푸드 나눔합니다', '마공/물공 버프 음식 넉넉히 만들었습니다. 광장 1채널로 오세요.', 0, 0),
    (6, 2, 1, '글렌 베르나 어려움 팟 구해요', '세인트 바드 있습니다. 딜러 3분 모십니다.', 0, 0),
    (7, 4, 2, '알반 기사단 훈련소 숨겨진 층', '보석 낙원 띄우는 팁 공유한다.', 0, 0),
    (8, 2, 3, '크롬 바스 100% 클리어 영상', '이루샤 기믹 파훼법 포함입니다.', 0, 0),
    (9, 3, 1, '지향색 앰플 교환 원해요', '리블(리얼 블랙) 앰플이랑 리화(리얼 화이트) 교환하실 분?', 0, 0),
    (10, 4, 2, '안 쓰는 정령석 나눔한다', '체력 정령 키우는 사람 가져가게.', 0, 0),
    (11, 1, 3, '주말 합주 파티 모집', '피아노, 바이올린 가능하신 분? 낭만농장에서 진행합니다.', 0, 0),
    (12, 1, 1, '오늘의 환생', '이번엔 달인작 하려고 영웅 재능 선택했어요.', 0, 0),
    (13, 1, 2, '노동요 추천좀', '블랙스미스 망치질할 때 들을만한 노래 없나?', 0, 0),
    (14, 2, 3, '하시딤 기르가쉬 레이드', '저지먼트 블레이드 수련하실 분 오세요.', 0, 0),
    (15, 2, 1, '블로니의 성장지원 팁', '추억담 퀘스트 밀 때 주의할 점 정리했습니다.', 0, 0),
    (16, 4, 2, '너클 콤보 가이드', '연속기 마스터가 알려주는 스크류 어퍼 활용법.', 0, 0),
    (17, 3, 3, '붕괴된 마력의 정수 팝니다', '경매장 최저가보다 싸게 넘깁니다.', 0, 0),
    (18, 3, 1, '날개 교환 희망', '검은색 수호천사 날개랑 흰색 큐피드 날개 교환 원해요.', 0, 0),
    (19, 1, 2, '주말에 교역하실 분?', '코끼리 타고 윌리엄 파트너랑 같이 교역합시다.', 0, 0),
    (20, 1, 3, '낭만농장 꾸미기 팁', '벚꽃 나무 배치하는 노하우 공유합니다.', 0, 0),
    (21, 2, 1, '위치렉 제보', '펫 탑승했다 내리면 자꾸 위치가 엇갈리네요.', 0, 0),
    (22, 2, 2, '벨테인 미션 배수의 진 팁', '60~100 레벨업은 여기가 최고지.', 0, 0),
    (23, 3, 3, '전용 인챈트 스크롤 경매', '기억 상실(Amnesia) 인챈트 경매 시작합니다.', 0, 0);

-- 댓글 더미 데이터 확장
INSERT INTO comment (id, post_id, user_id, parent_comment_id, content, is_deleted, is_blocked)
VALUES
    (6, 4, 1, NULL, '저도 빗자루 펫 타고 날아가야겠네요!', 0, 0),
    (7, 5, 2, NULL, '스테미나 회복 요리는 없나?', 0, 0),
    (8, 5, 1, 7, '새우 볶음밥 추천드려요!', 0, 0),
    (9, 6, 3, NULL, '엘레멘탈 나이트 1명 신청합니다.', 0, 0),
    (10, 7, 1, NULL, '룬 상급 하드 모드에서도 통하나요?', 0, 0),
    (11, 8, 2, NULL, '내 망치 실력보다 훌륭하군!', 0, 0),
    (12, 9, 3, NULL, '저 진한 파랑(코즈믹) 앰플은 있는데...', 0, 0),
    (13, 10, 1, NULL, '정령 밥으로 잘 쓰겠습니다!', 0, 0),
    (14, 11, 2, NULL, '북(Drum) 치는 건 자신 있다네.', 0, 0),
    (15, 12, 3, NULL, '누렙 2만 찍을 때까지 화이팅입니다.', 0, 0),
    (16, 13, 1, NULL, '어릴 적 할머니가 들려주신 옛 전설(OST) 추천요.', 0, 0),
    (17, 14, 2, NULL, '실드 오브 트러스트 타이밍 알려주나?', 0, 0),
    (18, 15, 3, NULL, '뉴비 필독서네요. 개추 드립니다.', 0, 0),
    (19, 16, 1, NULL, '대쉬 펀치 쿨타임은 어떻게 줄이나요?', 0, 0),
    (20, 17, 2, NULL, '수표 거래 되나?', 0, 0),
    (21, 18, 3, NULL, '날개 가격 차이가 좀 날 텐데요...', 0, 0),
    (22, 19, 1, NULL, '약탈단 만나면 지켜주세요.', 0, 0),
    (23, 20, 2, NULL, '모루를 배치하는 게 최고야.', 0, 0),
    (24, 21, 3, NULL, '그거 재접속하면 해결됩니다.', 0, 0),
    (25, 22, 1, NULL, '엘리트 통행증이 없어서 아쉽네요.', 0, 0);

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

-- postMeta 테이블 데이터
INSERT INTO post_meta (post_id, view_count, like_count, comment_count)
SELECT
    p.id,
    GREATEST(
            COALESCE(l.like_count, 0),
            COALESCE(c.comment_count, 0)
    ) * 10 + CAST(RAND() * 50 AS UNSIGNED) AS view_count, -- 조회수를 조금 더 리얼하게 랜덤 가중치 부여
    COALESCE(l.like_count, 0) AS like_count,
    COALESCE(c.comment_count, 0) AS comment_count
FROM post p
         LEFT JOIN (
    SELECT post_id, COUNT(*) AS like_count
    FROM post_like
    GROUP BY post_id
) l ON p.id = l.post_id
         LEFT JOIN (
    SELECT post_id, COUNT(*) AS comment_count
    FROM comment
    GROUP BY post_id
) c ON p.id = c.post_id;