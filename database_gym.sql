-- ==========================================
-- GYM MANAGEMENT SYSTEM DATABASE SCRIPT
-- ==========================================

CREATE DATABASE IF NOT EXISTS `gym_management`;
USE `gym_management`;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Table structure for `users`
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` enum('ADMIN','STAFF') NOT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `users` (`id`, `username`, `password`, `full_name`, `email`, `phone`, `role`, `is_active`) VALUES 
(1,'admin','123456','Quản trị viên','admin@gym.com','0901234567','ADMIN',1),
(2,'staff1','123456','Nhân viên 1','staff1@gym.com','0901234568','STAFF',1),
(3,'staff2','123456','Nhân viên 2','staff2@gym.com','0901234569','STAFF',1);

-- 2. Table structure for `members`
DROP TABLE IF EXISTS `members`;
CREATE TABLE `members` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_code` varchar(20) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `gender` enum('MALE','FEMALE','OTHER') NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `phone` varchar(20) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `address` text,
  `status` enum('ACTIVE','EXPIRED','SUSPENDED') NOT NULL,
  `join_date` date NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `member_code` (`member_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `members` (`id`, `member_code`, `full_name`, `gender`, `date_of_birth`, `phone`, `email`, `address`, `status`, `join_date`) VALUES 
(1,'M00001','Nguyễn Văn B','FEMALE','1990-05-17','0912345679','nguyenvanB@gmail.com','123 Lê Lợi, Q1, TP.DN','EXPIRED','2024-01-15'),
(2,'M00002','Trần Thị Bình','FEMALE','1995-08-20','0923456789','tranthbinh@gmail.com','456 Nguyễn Huệ, Q1, TP.HCM','EXPIRED','2024-02-10'),
(3,'M00003','Lê Minh Cường','MALE','1988-12-10','0934567890','leminhcuong@gmail.com','789 Trần Hưng Đạo, Q5, TP.HCM','ACTIVE','2024-03-05'),
(8,'M00008','Bùi Thị Hà','FEMALE','1991-09-22','0989012345','buithiha@gmail.com','753 Lý Thường Kiệt, Tân Bình','EXPIRED','2024-08-05'),
(10,'M00009','Dang Van B','FEMALE','2014-12-11','0795662033','','','ACTIVE','2025-12-18');

-- 3. Table structure for `promotions`
DROP TABLE IF EXISTS `promotions`;
CREATE TABLE `promotions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `promo_code` varchar(50) NOT NULL,
  `promo_name` varchar(200) NOT NULL DEFAULT 'Khuyến mãi',
  `discount_percent` double DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `promo_code` (`promo_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `promotions` VALUES 
(1,'SUMMER2024','Khuyến mãi hè 2024',15,'2024-06-01','2024-08-31',1),
(2,'NEWYEAR2025','Chào năm mới 2025',20,'2025-01-01','2025-01-31',1),
(3,'STUDENT10','Ưu đãi sinh viên',10,'2024-09-01','2025-06-30',1);

-- 4. Table structure for `packages`
DROP TABLE IF EXISTS `packages`;
CREATE TABLE `packages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `package_code` varchar(20) NOT NULL,
  `package_name` varchar(100) NOT NULL,
  `duration_months` int NOT NULL,
  `price` decimal(12,2) NOT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `promotion_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `package_code` (`package_code`),
  CONSTRAINT `FK_packages_promotion` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `packages` (`id`, `package_code`, `package_name`, `duration_months`, `price`, `is_active`) VALUES 
(1,'PKG001','Gói 1 Tháng',1,500000.00,1),
(2,'PKG002','Gói 3 Tháng',3,1350000.00,1),
(3,'PKG003','Gói 6 Tháng',6,2400000.00,1),
(4,'PKG004','Gói 12 Tháng VIP',12,4200000.00,1);

-- 5. Table structure for `registrations`
DROP TABLE IF EXISTS `registrations`;
CREATE TABLE `registrations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `registration_code` varchar(20) NOT NULL,
  `member_id` bigint NOT NULL,
  `package_id` bigint NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `final_amount` decimal(12,2) NOT NULL,
  `status` enum('ACTIVE','EXPIRED','FROZEN','CANCELLED') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `registration_code` (`registration_code`),
  CONSTRAINT `FK_reg_member` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`),
  CONSTRAINT `FK_reg_package` FOREIGN KEY (`package_id`) REFERENCES `packages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `registrations` (`id`, `registration_code`, `member_id`, `package_id`, `start_date`, `end_date`, `final_amount`, `status`) VALUES 
(1,'REG000001',1,3,'2024-01-15','2024-07-13',1920000.00,'ACTIVE'),
(2,'REG000002',2,2,'2024-02-10','2024-05-10',1215000.00,'EXPIRED');

-- 6. Table structure for `attendance`
DROP TABLE IF EXISTS `attendance`;
CREATE TABLE `attendance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `check_in_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_attendance_member` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `attendance` (`id`, `member_id`, `check_in_time`) VALUES 
(1,1,'2025-12-15 08:30:00'),
(2,3,'2025-12-15 09:15:00');

-- 7. Views
DROP VIEW IF EXISTS `vw_active_members`;
CREATE VIEW `vw_active_members` AS 
SELECT m.*, r.registration_code, r.end_date, p.package_name 
FROM members m 
LEFT JOIN registrations r ON m.id = r.member_id AND r.status = 'ACTIVE'
LEFT JOIN packages p ON r.package_id = p.id 
WHERE m.status = 'ACTIVE';

SET FOREIGN_KEY_CHECKS = 1;
