-- ========================================
-- 0. MEMBERS 초기 데이터 (가장 먼저 실행)
-- ========================================
-- 이메일 변경: admin -> admin1, user -> user1
-- 비밀번호 '1234' 암호화: $2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.hPayFybA6.u.y/0e/j1I9.k.x.W

-- 1. 관리자 (Admin) 생성
INSERT INTO members (
    email, password, name, nickname, phone, role, status,
    email_verified, provider, created_at, updated_at, deleted_at
) VALUES (
             'admin1@test.com',
             '$2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.hPayFybA6.u.y/0e/j1I9.k.x.W',
             '관리자', 'AdminUser', '010-0000-0000', 'ADMIN', 'ACTIVE',
             true, 'LOCAL', NOW(), NOW(), NULL
         );

-- 2. 일반 유저 (User) 생성
INSERT INTO members (
    email, password, name, nickname, phone, role, status,
    email_verified, provider, created_at, updated_at, deleted_at
) VALUES (
             'user1@test.com',
             '$2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.hPayFybA6.u.y/0e/j1I9.k.x.W',
             '일반유저', 'GeneralUser', '010-1111-1111', 'USER', 'ACTIVE',
             true, 'LOCAL', NOW(), NOW(), NULL
         );

-- ========================================
-- 0-1. CARTS 초기 데이터 (Members 생성 후 실행)
-- ========================================
-- Cart가 Member의 ID를 FK(member_id)로 가짐

-- 관리자 장바구니
INSERT INTO carts (member_id, total_count, created_at, updated_at)
VALUES (
           (SELECT id FROM members WHERE email='admin1@test.com'),
           0, NOW(), NOW()
       );

-- 일반 유저 장바구니
INSERT INTO carts (member_id, total_count, created_at, updated_at)
VALUES (
           (SELECT id FROM members WHERE email='user1@test.com'),
           0, NOW(), NOW()
       );
-- ========================================
-- 1. CATEGORY 초기 데이터
-- ========================================
INSERT INTO category (
    id, parent_id, name, slug, path, depth,
    display_order, is_visible, created_at, updated_at
) VALUES
-- 대분류
(1, NULL, '상의', 'tops', '/', 0, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, NULL, '하의', 'bottoms', '/', 0, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, NULL, '아우터', 'outer', '/', 0, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, NULL, '기타', 'etc', '/', 0, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 소분류 - 상의
(5, 1, '민소매', 'sleeveless', '/1/', 1, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 1, '반팔', 'short-sleeve', '/1/', 1, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 1, '긴팔', 'long-sleeve', '/1/', 1, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 1, '맨투맨', 'mtm', '/1/', 1, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 1, '후드티', 'hoodie', '/1/', 1, 5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 1, '셔츠', 'shirt', '/1/', 1, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 1, '니트', 'knit', '/1/', 1, 7, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 소분류 - 하의
(12, 2, '청바지', 'jeans', '/2/', 1, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 2, '면바지', 'cotton-pants', '/2/', 1, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 2, '반바지', 'shorts', '/2/', 1, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 2, '조거/트레이닝', 'jogger', '/2/', 1, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 소분류 - 아우터
(16, 3, '자켓', 'jacket', '/3/', 1, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 3, '코트', 'coat', '/3/', 1, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 3, '패딩', 'padded', '/3/', 1, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 3, '후드집업', 'hood-zipup', '/3/', 1, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 3, '가디건', 'cardigan', '/3/', 1, 5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 소분류 - 기타
(21, 4, '모자', 'hat', '/4/', 1, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 4, '벨트', 'belt', '/4/', 1, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 4, '아이웨어', 'eyewear', '/4/', 1, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 4, '양말', 'socks', '/4/', 1, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 4, '장갑', 'gloves', '/4/', 1, 5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- ========================================
-- 2. PRODUCT 초기 데이터 (total_stock 추가)
-- ========================================
INSERT INTO product (
    id, category_id, name, price, discount_rate, status,
    description, average_rating, review_count, wish_list_count,
    total_stock, version, deleted_at, created_at, updated_at
) VALUES
      (1, 6, '베이직 반팔티', 19000, 0, 'SELLING', '남녀공용 데일리 반팔티', 4.5, 10, 5, 450, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (2, 7, '베이직 긴팔티', 24000, 10, 'SELLING', '가장 편한 긴팔 티셔츠', 4.2, 8, 3, 240, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (3, 12, '슬림핏 청바지', 39000, 0, 'SELLING', '데일리 청바지', 4.8, 20, 12, 240, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (4, 16, '오버핏 데님 자켓', 69000, 5, 'SELLING', '남녀공용 데님 자켓', 4.7, 18, 7, 80, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (5, 21, '로고 볼캡', 29000, 0, 'SELLING', '심플 볼캡', 4.4, 5, 2, 150, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (6, 9, '오버핏 후드티', 45000, 15, 'SELLING', '편안한 오버핏 후드', 4.6, 25, 15, 300, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (7, 10, '옥스포드 셔츠', 39000, 0, 'SELLING', '데일리 셔츠', 4.3, 12, 8, 200, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (8, 13, '와이드 면바지', 35000, 10, 'SELLING', '편안한 면바지', 4.4, 15, 6, 180, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (9, 18, '숏패딩', 89000, 20, 'SELLING', '가벼운 숏패딩', 4.9, 30, 20, 250, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- ========================================
-- 3. PRODUCT OPTION GROUP 초기 데이터
-- ========================================
INSERT INTO product_option_group (
    id, product_id, name, display_order, created_at, updated_at
) VALUES
      (1, 1, '화이트', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (2, 1, '블랙', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (3, 1, '그레이', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (4, 2, '네이비', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (5, 2, '베이지', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (6, 3, '라이트블루', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (7, 3, '다크블루', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (8, 4, '라이트블루', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (9, 4, '블랙', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (10, 5, '블랙', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (11, 5, '베이지', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (12, 6, '블랙', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (13, 6, '그레이', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (14, 6, '네이비', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (15, 7, '화이트', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (16, 7, '블루', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (17, 8, '블랙', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (18, 8, '베이지', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (19, 9, '블랙', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (20, 9, '네이비', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- ========================================
-- 4. OPTION DETAIL 초기 데이터
-- ========================================
INSERT INTO option_detail (
    id, product_option_group_id, name, sku, add_price,
    stock_quantity, display_order, version, deleted_at, created_at, updated_at
) VALUES
-- 상품 1: 베이직 반팔티 (화이트/블랙/그레이)
(1, 1, 'S', 'TSHIRT-WHITE-S', 0, 50, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'M', 'TSHIRT-WHITE-M', 0, 50, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 'L', 'TSHIRT-WHITE-L', 0, 50, 3, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, 'S', 'TSHIRT-BLACK-S', 0, 50, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, 'M', 'TSHIRT-BLACK-M', 0, 50, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 2, 'L', 'TSHIRT-BLACK-L', 0, 50, 3, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 3, 'S', 'TSHIRT-GRAY-S', 0, 50, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 3, 'M', 'TSHIRT-GRAY-M', 0, 50, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 3, 'L', 'TSHIRT-GRAY-L', 0, 50, 3, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 2: 베이직 긴팔티 (네이비/베이지)
(10, 4, 'M', 'LONGTEE-NAVY-M', 0, 80, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 4, 'L', 'LONGTEE-NAVY-L', 0, 80, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 5, 'M', 'LONGTEE-BEIGE-M', 0, 40, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 5, 'L', 'LONGTEE-BEIGE-L', 0, 40, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 3: 슬림핏 청바지 (라이트/다크)
(14, 6, '28', 'JEANS-LIGHT-28', 0, 30, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 6, '30', 'JEANS-LIGHT-30', 0, 30, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 6, '32', 'JEANS-LIGHT-32', 0, 30, 3, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 6, '34', 'JEANS-LIGHT-34', 0, 30, 4, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 7, '28', 'JEANS-DARK-28', 0, 30, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 7, '30', 'JEANS-DARK-30', 0, 30, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 7, '32', 'JEANS-DARK-32', 0, 30, 3, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 7, '34', 'JEANS-DARK-34', 0, 30, 4, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 4: 오버핏 데님 자켓 (라이트블루/블랙)
(22, 8, 'M', 'JACKET-LIGHT-M', 0, 20, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 8, 'L', 'JACKET-LIGHT-L', 0, 20, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 9, 'M', 'JACKET-BLACK-M', 0, 20, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 9, 'L', 'JACKET-BLACK-L', 0, 20, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 5: 로고 볼캡 (블랙/베이지)
(26, 10, 'FREE', 'CAP-BLACK-FREE', 0, 80, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 11, 'FREE', 'CAP-BEIGE-FREE', 0, 70, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 6: 오버핏 후드티 (블랙/그레이/네이비)
(28, 12, 'M', 'HOODIE-BLACK-M', 0, 50, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(29, 12, 'L', 'HOODIE-BLACK-L', 0, 50, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(30, 13, 'M', 'HOODIE-GRAY-M', 0, 50, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(31, 13, 'L', 'HOODIE-GRAY-L', 0, 50, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(32, 14, 'M', 'HOODIE-NAVY-M', 0, 50, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(33, 14, 'L', 'HOODIE-NAVY-L', 0, 50, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 7: 옥스포드 셔츠 (화이트/블루)
(34, 15, 'M', 'SHIRT-WHITE-M', 0, 50, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(35, 15, 'L', 'SHIRT-WHITE-L', 0, 50, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(36, 16, 'M', 'SHIRT-BLUE-M', 0, 50, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(37, 16, 'L', 'SHIRT-BLUE-L', 0, 50, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 8: 와이드 면바지 (블랙/베이지)
(38, 17, 'M', 'PANTS-BLACK-M', 0, 45, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(39, 17, 'L', 'PANTS-BLACK-L', 0, 45, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(40, 18, 'M', 'PANTS-BEIGE-M', 0, 45, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(41, 18, 'L', 'PANTS-BEIGE-L', 0, 45, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 9: 숏패딩 (블랙/네이비)
(42, 19, 'M', 'PADDING-BLACK-M', 0, 60, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(43, 19, 'L', 'PADDING-BLACK-L', 0, 60, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(44, 19, 'XL', 'PADDING-BLACK-XL', 0, 60, 3, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(45, 20, 'M', 'PADDING-NAVY-M', 0, 30, 1, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(46, 20, 'L', 'PADDING-NAVY-L', 0, 30, 2, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(47, 20, 'XL', 'PADDING-NAVY-XL', 0, 10, 3, 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- ========================================
-- 5. PRODUCT IMAGE 초기 데이터
-- ========================================
INSERT INTO product_image (
    id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at
) VALUES
-- 상품 1: 베이직 반팔티
(1, 1, '/images/products/t-shirt.jpg', 'MAIN', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, '/uploads/products/1/slider/thumbnail/tshirt-slide1.jpg', 'SLIDER', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, '/uploads/products/1/slider/thumbnail/tshirt-slide2.jpg', 'SLIDER', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 1, '/uploads/products/1/slider/thumbnail/tshirt-slide3.jpg', 'SLIDER', 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, '/uploads/products/1/description/resized/tshirt-desc1.jpg', 'DESCRIPTION', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 1, '/uploads/products/1/description/resized/tshirt-desc2.jpg', 'DESCRIPTION', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 2: 베이직 긴팔티
(7, 2, '/images/products/long-sleeve.jpg', 'MAIN', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 2, '/uploads/products/2/slider/thumbnail/longtee-slide1.jpg', 'SLIDER', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 2, '/uploads/products/2/slider/thumbnail/longtee-slide2.jpg', 'SLIDER', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 2, '/uploads/products/2/description/resized/longtee-desc1.jpg', 'DESCRIPTION', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 3: 슬림핏 청바지
(11, 3, '/images/products/jean.jpg', 'MAIN', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 3, '/uploads/products/3/slider/thumbnail/jeans-slide1.jpg', 'SLIDER', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 3, '/uploads/products/3/slider/thumbnail/jeans-slide2.jpg', 'SLIDER', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 3, '/uploads/products/3/slider/thumbnail/jeans-slide3.jpg', 'SLIDER', 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 3, '/uploads/products/3/description/resized/jeans-desc1.jpg', 'DESCRIPTION', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 3, '/uploads/products/3/description/resized/jeans-desc2.jpg', 'DESCRIPTION', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 4: 오버핏 데님 자켓
(17, 4, '/images/products/jacket.jpg', 'MAIN', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 4, '/uploads/products/4/slider/thumbnail/jacket-slide1.jpg', 'SLIDER', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 4, '/uploads/products/4/slider/thumbnail/jacket-slide2.jpg', 'SLIDER', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 4, '/uploads/products/4/description/resized/jacket-desc1.jpg', 'DESCRIPTION', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 5: 로고 볼캡
(21, 5, '/images/products/cap.jpg', 'MAIN', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 5, '/uploads/products/5/slider/thumbnail/cap-slide1.jpg', 'SLIDER', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 5, '/uploads/products/5/description/resized/cap-desc1.jpg', 'DESCRIPTION', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 6: 오버핏 후드티
(24, 6, '/images/products/hoodie.jpg', 'MAIN', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 6, '/uploads/products/6/slider/thumbnail/hoodie-slide1.jpg', 'SLIDER', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 6, '/uploads/products/6/slider/thumbnail/hoodie-slide2.jpg', 'SLIDER', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 6, '/uploads/products/6/slider/thumbnail/hoodie-slide3.jpg', 'SLIDER', 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(28, 6, '/uploads/products/6/description/resized/hoodie-desc1.jpg', 'DESCRIPTION', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(29, 6, '/uploads/products/6/description/resized/hoodie-desc2.jpg', 'DESCRIPTION', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 7: 옥스포드 셔츠
(30, 7, '/images/products/shirt.jpg', 'MAIN', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(31, 7, '/uploads/products/7/slider/thumbnail/shirt-slide1.jpg', 'SLIDER', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(32, 7, '/uploads/products/7/description/resized/shirt-desc1.jpg', 'DESCRIPTION', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 8: 와이드 면바지
(33, 8, '/images/products/pants.jpg', 'MAIN', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(34, 8, '/uploads/products/8/slider/thumbnail/pants-slide1.jpg', 'SLIDER', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(35, 8, '/uploads/products/8/slider/thumbnail/pants-slide2.jpg', 'SLIDER', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(36, 8, '/uploads/products/8/description/resized/pants-desc1.jpg', 'DESCRIPTION', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 상품 9: 숏패딩
(37, 9, '/images/products/padding.jpg', 'MAIN', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(38, 9, '/uploads/products/9/slider/thumbnail/padding-slide1.jpg', 'SLIDER', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(39, 9, '/uploads/products/9/slider/thumbnail/padding-slide2.jpg', 'SLIDER', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(40, 9, '/uploads/products/9/slider/thumbnail/padding-slide3.jpg', 'SLIDER', 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(41, 9, '/uploads/products/9/description/resized/padding-desc1.jpg', 'DESCRIPTION', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(42, 9, '/uploads/products/9/description/resized/padding-desc2.jpg', 'DESCRIPTION', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);





-- ==========================================
-- 6. QUESTION (문의) 더미 데이터
-- ==========================================

-- 1. 관리자가 작성한 일반 문의 (상품 ID: 1)
INSERT INTO question (member_id, product_id, title, content, is_secret, answered, created_at, updated_at, deleted_at)
VALUES (
           (SELECT id FROM members WHERE email='admin1@test.com'), -- 변경된 이메일로 ID 조회
           1,
           '관리자 테스트 문의입니다.',
           '배송 시스템 점검을 위한 테스트 질문입니다.',
           false,
           false,
           NOW(), NOW(), NULL
       );

-- 2. 관리자가 작성한 비밀 문의 (상품 ID: 2)
INSERT INTO question (member_id, product_id, title, content, is_secret, answered, created_at, updated_at, deleted_at)
VALUES (
           (SELECT id FROM members WHERE email='admin1@test.com'),
           2,
           '비밀글 테스트',
           '관리자 비밀글 기능 확인 중입니다.',
           true,
           false,
           NOW(), NOW(), NULL
       );

-- 3. 관리자가 작성하고 답변 완료된 문의 (상품 ID: 3)
INSERT INTO question (member_id, product_id, title, content, is_secret, answered, created_at, updated_at, deleted_at)
VALUES (
           (SELECT id FROM members WHERE email='admin1@test.com'),
           3,
           '재입고 날짜 확인용',
           '데이터베이스 정합성 테스트 질문입니다.',
           false,
           true,
           NOW(), NOW(), NULL
       );


-- ==========================================
-- 7. ANSWER (답변) 더미 데이터
-- ==========================================

-- 위 3번 문의(question_id = 3)에 대한 답변
INSERT INTO answer (question_id, admin_id, content, created_at, updated_at)
VALUES (
           3,
           (SELECT id FROM members WHERE email='admin1@test.com'), -- 답변자도 admin1
           '테스트 확인 완료되었습니다. 정상 작동합니다.',
           NOW(), NOW()
       );

-- ==========================================
-- 8. REVIEW (리뷰) 더미 데이터
-- ==========================================

INSERT INTO review (
    product_id, member_id, rating, content, image_url,
    created_at, updated_at, deleted_at
) VALUES
-- 상품 1: 베이직 반팔티
(
    1,
    (SELECT id FROM members WHERE email='admin1@test.com'),
    5,
    '핏이 깔끔하고 재질도 좋아요. 데일리로 입기 딱입니다.',
    '/images/reviews/review1.png',
    NOW(), NOW(), NULL
),
-- 상품 3: 슬림핏 청바지
(
    3,
    (SELECT id FROM members WHERE email='admin1@test.com'),
    4,
    '색감 좋고 라인도 예쁜데 허리가 살짝 타이트해요.',
    NULL,
    NOW(), NOW(), NULL
),
-- 상품 9: 숏패딩
(
    9,
    (SELECT id FROM members WHERE email='admin1@test.com'),
    5,
    '가볍고 따뜻해서 만족! 배송도 빨랐어요.',
    '/images/reviews/review2.png',
    NOW(), NOW(), NULL
);

INSERT INTO review (
    product_id, member_id, rating, content, image_url,
    created_at, updated_at, deleted_at
) VALUES
    (
        2,
        (SELECT id FROM members WHERE email='admin1@test.com'),
        4,
        '테스트 리뷰입니다. 긴팔 기본템으로 무난합니다.',
        NULL,
        NOW(), NOW(), NULL
    );