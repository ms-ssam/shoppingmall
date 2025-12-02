-- CATEGORY 초기 데이터
INSERT INTO category (
    id, parent_id, name, slug, path, depth,
    display_order, is_visible,
    created_at, updated_at
) VALUES
-- 대분류
(1, NULL, '상의', 'tops', '/', 0, 1, true, NOW(), NOW()),
(2, NULL, '하의', 'bottoms', '/', 0, 2, true, NOW(), NOW()),
(3, NULL, '아우터', 'outer', '/', 0, 3, true, NOW(), NOW()),
(4, NULL, '기타', 'etc', '/', 0, 4, true, NOW(), NOW()),

-- 소분류 - 상의
(5, 1, '민소매', 'sleeveless', '/', 1, 1, true, NOW(), NOW()),
(6, 1, '반팔', 'short-sleeve', '/', 1, 2, true, NOW(), NOW()),
(7, 1, '긴팔', 'long-sleeve', '/', 1, 3, true, NOW(), NOW()),
(8, 1, '맨투맨', 'mtm', '/', 1, 4, true, NOW(), NOW()),
(9, 1, '후드티', 'hoodie', '/', 1, 5, true, NOW(), NOW()),
(10, 1, '셔츠', 'shirt', '/', 1, 6, true, NOW(), NOW()),
(11, 1, '니트', 'knit', '/', 1, 7, true, NOW(), NOW()),

-- 소분류 - 하의
(12, 2, '청바지', 'jeans', '/', 1, 1, true, NOW(), NOW()),
(13, 2, '면바지', 'cotton-pants', '/', 1, 2, true, NOW(), NOW()),
(14, 2, '반바지', 'shorts', '/', 1, 3, true, NOW(), NOW()),
(15, 2, '조거/트레이닝', 'jogger', '/', 1, 4, true, NOW(), NOW()),

-- 소분류 - 아우터
(16, 3, '자켓', 'jacket', '/', 1, 1, true, NOW(), NOW()),
(17, 3, '코트', 'coat', '/', 1, 2, true, NOW(), NOW()),
(18, 3, '패딩', 'padded', '/', 1, 3, true, NOW(), NOW()),
(19, 3, '후드집업', 'hood-zipup', '/', 1, 4, true, NOW(), NOW()),
(20, 3, '가디건', 'cardigan', '/', 1, 5, true, NOW(), NOW()),

-- 소분류 - 기타
(21, 4, '모자', 'hat', '/', 1, 1, true, NOW(), NOW()),
(22, 4, '벨트', 'belt', '/', 1, 2, true, NOW(), NOW()),
(23, 4, '아이웨어', 'eyewear', '/', 1, 3, true, NOW(), NOW()),
(24, 4, '양말', 'socks', '/', 1, 4, true, NOW(), NOW()),
(25, 4, '장갑', 'gloves', '/', 1, 5, true, NOW(), NOW());

-- product (✅ 더 많은 상품 추가)
INSERT INTO product (
    id, category_id, name, price, discount_rate, status,
    description, average_rating, review_count, wish_list_count,
    version, deleted_at, created_at, updated_at
) VALUES
-- 상의 카테고리 (반팔=6, 긴팔=7, 후드티=9)
(1, 6, '베이직 반팔티', 19000, 0, 'SELLING',
 '남녀공용 데일리 반팔티', 4.5, 10, 5,
 0, NULL, NOW(), NOW()),
(2, 7, '베이직 긴팔티', 24000, 10, 'SELLING',
 '가장 편한 긴팔 티셔츠', 4.2, 8, 3,
 0, NULL, NOW(), NOW()),
(6, 9, '오버핏 후드티', 45000, 15, 'SELLING',
 '편안한 오버핏 후드', 4.6, 25, 15,
 0, NULL, NOW(), NOW()),
(7, 10, '옥스포드 셔츠', 39000, 0, 'SELLING',
 '데일리 셔츠', 4.3, 12, 8,
 0, NULL, NOW(), NOW()),

-- 하의 카테고리 (청바지=12, 면바지=13)
(3, 12, '슬림핏 청바지', 39000, 0, 'SELLING',
 '데일리 청바지', 4.8, 20, 12,
 0, NULL, NOW(), NOW()),
(8, 13, '와이드 면바지', 35000, 10, 'SELLING',
 '편안한 면바지', 4.4, 15, 6,
 0, NULL, NOW(), NOW()),

-- 아우터 카테고리 (자켓=16, 패딩=18)
(4, 16, '오버핏 데님 자켓', 69000, 5, 'SELLING',
 '남녀공용 데님 자켓', 4.7, 18, 7,
 0, NULL, NOW(), NOW()),
(9, 18, '숏패딩', 89000, 20, 'SELLING',
 '가벼운 숏패딩', 4.9, 30, 20,
 0, NULL, NOW(), NOW()),

-- 기타 카테고리 (모자=21)
(5, 21, '로고 볼캡', 29000, 0, 'SELLING',
 '심플 볼캡', 4.4, 5, 2,
 0, NULL, NOW(), NOW());

-- ProductOptionGroup
INSERT INTO product_option_group (
    id, product_id, name, display_order,
    created_at, updated_at
) VALUES
      (1, 1, '색상', 1, NOW(), NOW()),
      (2, 1, '사이즈', 2, NOW(), NOW()),
      (3, 2, '사이즈', 1, NOW(), NOW()),
      (4, 3, '사이즈', 1, NOW(), NOW()),
      (5, 4, '색상', 1, NOW(), NOW());

-- OptionDetail
INSERT INTO option_detail (
    id, product_option_group_id, name, sku,
    add_price, stock_quantity, display_order,
    version, deleted_at, created_at, updated_at
) VALUES
-- 반팔티 색상
(1, 1, 'RED', 'TSHIRT-RED', 0, 50, 1, NULL, NULL, NOW(), NOW()),
(2, 1, 'WHITE', 'TSHIRT-WHITE', 0, 50, 2, NULL, NULL, NOW(), NOW()),
(3, 1, 'BLACK', 'TSHIRT-BLACK', 0, 50, 3, NULL, NULL, NOW(), NOW()),

-- 반팔티 사이즈
(4, 2, 'M', 'TSHIRT-M', 0, 100, 1, NULL, NULL, NOW(), NOW()),
(5, 2, 'L', 'TSHIRT-L', 0, 100, 2, NULL, NULL, NOW(), NOW()),
(6, 2, 'XL', 'TSHIRT-XL', 0, 100, 3, NULL, NULL, NOW(), NOW()),

-- 긴팔티 사이즈
(7, 3, 'M', 'LONGTEE-M', 0, 80, 1, NULL, NULL, NOW(), NOW()),
(8, 3, 'L', 'LONGTEE-L', 0, 80, 2, NULL, NULL, NOW(), NOW()),
(9, 3, 'XL', 'LONGTEE-XL', 0, 80, 3, NULL, NULL, NOW(), NOW()),

-- 청바지 사이즈
(10, 4, '28', 'JEANS-28', 0, 60, 1, NULL, NULL, NOW(), NOW()),
(11, 4, '30', 'JEANS-30', 0, 60, 2, NULL, NULL, NOW(), NOW()),
(12, 4, '32', 'JEANS-32', 0, 60, 3, NULL, NULL, NOW(), NOW()),
(13, 4, '34', 'JEANS-34', 0, 60, 4, NULL, NULL, NOW(), NOW()),

-- 데님자켓 색상
(14, 5, 'BLUE', 'JACKET-BLUE', 0, 40, 1, NULL, NULL, NOW(), NOW()),
(15, 5, 'BLACK', 'JACKET-BLACK', 0, 40, 2, NULL, NULL, NOW(), NOW());