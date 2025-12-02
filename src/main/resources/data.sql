-- ========================================
-- CATEGORY 초기 데이터
-- ========================================
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (1, NULL, '상의', 'tops', '/', 0, 1, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (2, NULL, '하의', 'bottoms', '/', 0, 2, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (3, NULL, '아우터', 'outer', '/', 0, 3, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (4, NULL, '기타', 'etc', '/', 0, 4, true, NOW(), NOW());

-- 소분류 - 상의
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (5, 1, '민소매', 'sleeveless', '/1/', 1, 1, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (6, 1, '반팔', 'short-sleeve', '/1/', 1, 2, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (7, 1, '긴팔', 'long-sleeve', '/1/', 1, 3, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (8, 1, '맨투맨', 'mtm', '/1/', 1, 4, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (9, 1, '후드티', 'hoodie', '/1/', 1, 5, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (10, 1, '셔츠', 'shirt', '/1/', 1, 6, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (11, 1, '니트', 'knit', '/1/', 1, 7, true, NOW(), NOW());

-- 소분류 - 하의
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (12, 2, '청바지', 'jeans', '/2/', 1, 1, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (13, 2, '면바지', 'cotton-pants', '/2/', 1, 2, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (14, 2, '반바지', 'shorts', '/2/', 1, 3, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (15, 2, '조거/트레이닝', 'jogger', '/2/', 1, 4, true, NOW(), NOW());

-- 소분류 - 아우터
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (16, 3, '자켓', 'jacket', '/3/', 1, 1, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (17, 3, '코트', 'coat', '/3/', 1, 2, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (18, 3, '패딩', 'padded', '/3/', 1, 3, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (19, 3, '후드집업', 'hood-zipup', '/3/', 1, 4, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (20, 3, '가디건', 'cardigan', '/3/', 1, 5, true, NOW(), NOW());

-- 소분류 - 기타
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (21, 4, '모자', 'hat', '/4/', 1, 1, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (22, 4, '벨트', 'belt', '/4/', 1, 2, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (23, 4, '아이웨어', 'eyewear', '/4/', 1, 3, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (24, 4, '양말', 'socks', '/4/', 1, 4, true, NOW(), NOW());
INSERT INTO category (id, parent_id, name, slug, path, depth, display_order, is_visible, created_at, updated_at) VALUES (25, 4, '장갑', 'gloves', '/4/', 1, 5, true, NOW(), NOW());


-- ========================================
-- PRODUCT 초기 데이터
-- ========================================
INSERT INTO product (id, category_id, name, price, discount_rate, status, description, average_rating, review_count, wish_list_count, deleted_at, created_at, updated_at) VALUES (1, 6, '베이직 반팔티', 19000, 0, 'SELLING', '남녀공용 데일리 반팔티', 4.5, 10, 5, NULL, NOW(), NOW());
INSERT INTO product (id, category_id, name, price, discount_rate, status, description, average_rating, review_count, wish_list_count, deleted_at, created_at, updated_at) VALUES (2, 7, '베이직 긴팔티', 24000, 10, 'SELLING', '가장 편한 긴팔 티셔츠', 4.2, 8, 3, NULL, NOW(), NOW());
INSERT INTO product (id, category_id, name, price, discount_rate, status, description, average_rating, review_count, wish_list_count, deleted_at, created_at, updated_at) VALUES (3, 12, '슬림핏 청바지', 39000, 0, 'SELLING', '데일리 청바지', 4.8, 20, 12, NULL, NOW(), NOW());
INSERT INTO product (id, category_id, name, price, discount_rate, status, description, average_rating, review_count, wish_list_count, deleted_at, created_at, updated_at) VALUES (4, 16, '오버핏 데님 자켓', 69000, 5, 'SELLING', '남녀공용 데님 자켓', 4.7, 18, 7, NULL, NOW(), NOW());
INSERT INTO product (id, category_id, name, price, discount_rate, status, description, average_rating, review_count, wish_list_count, deleted_at, created_at, updated_at) VALUES (5, 21, '로고 볼캡', 29000, 0, 'SELLING', '심플 볼캡', 4.4, 5, 2, NULL, NOW(), NOW());
INSERT INTO product (id, category_id, name, price, discount_rate, status, description, average_rating, review_count, wish_list_count, deleted_at, created_at, updated_at) VALUES (6, 9, '오버핏 후드티', 45000, 15, 'SELLING', '편안한 오버핏 후드', 4.6, 25, 15, NULL, NOW(), NOW());
INSERT INTO product (id, category_id, name, price, discount_rate, status, description, average_rating, review_count, wish_list_count, deleted_at, created_at, updated_at) VALUES (7, 10, '옥스포드 셔츠', 39000, 0, 'SELLING', '데일리 셔츠', 4.3, 12, 8, NULL, NOW(), NOW());
INSERT INTO product (id, category_id, name, price, discount_rate, status, description, average_rating, review_count, wish_list_count, deleted_at, created_at, updated_at) VALUES (8, 13, '와이드 면바지', 35000, 10, 'SELLING', '편안한 면바지', 4.4, 15, 6, NULL, NOW(), NOW());
INSERT INTO product (id, category_id, name, price, discount_rate, status, description, average_rating, review_count, wish_list_count, deleted_at, created_at, updated_at) VALUES (9, 18, '숏패딩', 89000, 20, 'SELLING', '가벼운 숏패딩', 4.9, 30, 20, NULL, NOW(), NOW());


-- ========================================
-- PRODUCT OPTION GROUP 초기 데이터
-- ========================================
INSERT INTO product_option_group (id, product_id, name, display_order, created_at, updated_at) VALUES (1, 1, '색상', 1, NOW(), NOW());
INSERT INTO product_option_group (id, product_id, name, display_order, created_at, updated_at) VALUES (2, 1, '사이즈', 2, NOW(), NOW());
INSERT INTO product_option_group (id, product_id, name, display_order, created_at, updated_at) VALUES (3, 2, '사이즈', 1, NOW(), NOW());
INSERT INTO product_option_group (id, product_id, name, display_order, created_at, updated_at) VALUES (4, 3, '사이즈', 1, NOW(), NOW());
INSERT INTO product_option_group (id, product_id, name, display_order, created_at, updated_at) VALUES (5, 4, '색상', 1, NOW(), NOW());


-- ========================================
-- OPTION DETAIL 초기 데이터
-- ========================================
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (1, 1, 'RED', 'TSHIRT-RED', 0, 50, 1, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (2, 1, 'WHITE', 'TSHIRT-WHITE', 0, 50, 2, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (3, 1, 'BLACK', 'TSHIRT-BLACK', 0, 50, 3, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (4, 2, 'M', 'TSHIRT-M', 0, 100, 1, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (5, 2, 'L', 'TSHIRT-L', 0, 100, 2, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (6, 2, 'XL', 'TSHIRT-XL', 0, 100, 3, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (7, 3, 'M', 'LONGTEE-M', 0, 80, 1, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (8, 3, 'L', 'LONGTEE-L', 0, 80, 2, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (9, 3, 'XL', 'LONGTEE-XL', 0, 80, 3, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (10, 4, '28', 'JEANS-28', 0, 60, 1, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (11, 4, '30', 'JEANS-30', 0, 60, 2, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (12, 4, '32', 'JEANS-32', 0, 60, 3, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (13, 4, '34', 'JEANS-34', 0, 60, 4, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (14, 5, 'BLUE', 'JACKET-BLUE', 0, 40, 1, NULL, NOW(), NOW());
INSERT INTO option_detail (id, product_option_group_id, name, sku, add_price, stock_quantity, display_order, deleted_at, created_at, updated_at) VALUES (15, 5, 'BLACK', 'JACKET-BLACK', 0, 40, 2, NULL, NOW(), NOW());


-- ========================================
-- PRODUCT IMAGE 초기 데이터
-- ========================================
-- 상품 1: 베이직 반팔티 (product_option_group_id 제거)
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (1, 1, '/uploads/products/1/main/thumbnail/tshirt-main.jpg', 'MAIN', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (2, 1, '/uploads/products/1/slider/thumbnail/tshirt-slide1.jpg', 'SLIDER', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (3, 1, '/uploads/products/1/slider/thumbnail/tshirt-slide2.jpg', 'SLIDER', 1, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (4, 1, '/uploads/products/1/slider/thumbnail/tshirt-slide3.jpg', 'SLIDER', 2, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (5, 1, '/uploads/products/1/description/resized/tshirt-desc1.jpg', 'DESCRIPTION', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (6, 1, '/uploads/products/1/description/resized/tshirt-desc2.jpg', 'DESCRIPTION', 1, NULL, NOW(), NOW());

-- 상품 2: 베이직 긴팔티
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (7, 2, '/uploads/products/2/main/thumbnail/longtee-main.jpg', 'MAIN', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (8, 2, '/uploads/products/2/slider/thumbnail/longtee-slide1.jpg', 'SLIDER', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (9, 2, '/uploads/products/2/slider/thumbnail/longtee-slide2.jpg', 'SLIDER', 1, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (10, 2, '/uploads/products/2/description/resized/longtee-desc1.jpg', 'DESCRIPTION', 0, NULL, NOW(), NOW());

-- 상품 3: 슬림핏 청바지
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (11, 3, '/uploads/products/3/main/thumbnail/jeans-main.jpg', 'MAIN', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (12, 3, '/uploads/products/3/slider/thumbnail/jeans-slide1.jpg', 'SLIDER', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (13, 3, '/uploads/products/3/slider/thumbnail/jeans-slide2.jpg', 'SLIDER', 1, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (14, 3, '/uploads/products/3/slider/thumbnail/jeans-slide3.jpg', 'SLIDER', 2, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (15, 3, '/uploads/products/3/description/resized/jeans-desc1.jpg', 'DESCRIPTION', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (16, 3, '/uploads/products/3/description/resized/jeans-desc2.jpg', 'DESCRIPTION', 1, NULL, NOW(), NOW());

-- 상품 4: 오버핏 데님 자켓
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (17, 4, '/uploads/products/4/main/thumbnail/jacket-main.jpg', 'MAIN', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (18, 4, '/uploads/products/4/slider/thumbnail/jacket-slide1.jpg', 'SLIDER', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (19, 4, '/uploads/products/4/slider/thumbnail/jacket-slide2.jpg', 'SLIDER', 1, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (20, 4, '/uploads/products/4/description/resized/jacket-desc1.jpg', 'DESCRIPTION', 0, NULL, NOW(), NOW());

-- 상품 5: 로고 볼캡
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (21, 5, '/uploads/products/5/main/thumbnail/cap-main.jpg', 'MAIN', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (22, 5, '/uploads/products/5/slider/thumbnail/cap-slide1.jpg', 'SLIDER', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (23, 5, '/uploads/products/5/description/resized/cap-desc1.jpg', 'DESCRIPTION', 0, NULL, NOW(), NOW());

-- 상품 6: 오버핏 후드티
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (24, 6, '/uploads/products/6/main/thumbnail/hoodie-main.jpg', 'MAIN', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (25, 6, '/uploads/products/6/slider/thumbnail/hoodie-slide1.jpg', 'SLIDER', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (26, 6, '/uploads/products/6/slider/thumbnail/hoodie-slide2.jpg', 'SLIDER', 1, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (27, 6, '/uploads/products/6/slider/thumbnail/hoodie-slide3.jpg', 'SLIDER', 2, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (28, 6, '/uploads/products/6/description/resized/hoodie-desc1.jpg', 'DESCRIPTION', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (29, 6, '/uploads/products/6/description/resized/hoodie-desc2.jpg', 'DESCRIPTION', 1, NULL, NOW(), NOW());

-- 상품 7: 옥스포드 셔츠
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (30, 7, '/uploads/products/7/main/thumbnail/shirt-main.jpg', 'MAIN', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (31, 7, '/uploads/products/7/slider/thumbnail/shirt-slide1.jpg', 'SLIDER', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (32, 7, '/uploads/products/7/description/resized/shirt-desc1.jpg', 'DESCRIPTION', 0, NULL, NOW(), NOW());

-- 상품 8: 와이드 면바지
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (33, 8, '/uploads/products/8/main/thumbnail/pants-main.jpg', 'MAIN', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (34, 8, '/uploads/products/8/slider/thumbnail/pants-slide1.jpg', 'SLIDER', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (35, 8, '/uploads/products/8/slider/thumbnail/pants-slide2.jpg', 'SLIDER', 1, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (36, 8, '/uploads/products/8/description/resized/pants-desc1.jpg', 'DESCRIPTION', 0, NULL, NOW(), NOW());

-- 상품 9: 숏패딩
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (37, 9, '/uploads/products/9/main/thumbnail/padding-main.jpg', 'MAIN', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (38, 9, '/uploads/products/9/slider/thumbnail/padding-slide1.jpg', 'SLIDER', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (39, 9, '/uploads/products/9/slider/thumbnail/padding-slide2.jpg', 'SLIDER', 1, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (40, 9, '/uploads/products/9/slider/thumbnail/padding-slide3.jpg', 'SLIDER', 2, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (41, 9, '/uploads/products/9/description/resized/padding-desc1.jpg', 'DESCRIPTION', 0, NULL, NOW(), NOW());
INSERT INTO product_image (id, product_id, image_url, image_type, display_order, deleted_at, created_at, updated_at) VALUES (42, 9, '/uploads/products/9/description/resized/padding-desc2.jpg', 'DESCRIPTION', 1, NULL, NOW(), NOW());
