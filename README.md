# Gym Management System 🏋️‍♂️

Dự án quản lý phòng Gym được xây dựng bằng Java (JavaFX/Swing) sử dụng Maven và cơ sở dữ liệu MySQL.

## 🚀 Tính năng chính
- Quản lý hội viên (Thêm, sửa, xóa, tìm kiếm).
- Quản lý gói tập và đăng ký.
- Điểm danh hội viên.
- Thống kê và báo cáo doanh thu.
- Hệ thống phân quyền (Admin/Staff).

## 🛠 Công nghệ sử dụng
- **Ngôn ngữ**: Java (JDK 17+)
- **Framework/Library**: Maven, JavaFX
- **Cơ sở dữ liệu**: MySQL
- **Công cụ**: Eclipse IDE

## 📋 Hướng dẫn cài đặt

### 1. Cơ sở dữ liệu
- Cài đặt MySQL Server.
- Import file `database_gym.sql` (nếu có) vào MySQL Workbench hoặc chạy các lệnh SQL trong thư mục dự án.
- Cấu hình lại thông tin kết nối (User/Password) trong code Java (file `src/main/java/com/gym/util/HibernateUtil.java` hoặc tương đương).

### 2. Chạy ứng dụng
- Clone dự án: `git clone https://github.com/Kaivin22/Gym-Management-System.git`
- Mở dự án bằng Eclipse (Import as Existing Maven Project).
- Chạy file `Main.java` (hoặc App.java).

## 👤 Tác giả
- **Kaivin22**
