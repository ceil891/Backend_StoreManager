# Hướng Dẫn Kiểm Thử Hiệu Năng Backend (k6 Load & Stress Testing)

Thư mục này chứa các kịch bản kiểm thử hiệu năng tự động bằng công cụ **k6** dành cho Backend Spring Boot (StoreManager).

## 1. Cài đặt k6

- **Windows (Chocolatey hoặc Winget)**:
  ```powershell
  winget install k6 --source winget
  # hoặc
  choco install k6
  ```
- **macOS**:
  ```bash
  brew install k6
  ```
- **Linux (Debian/Ubuntu)**:
  ```bash
  sudo gpg -k
  sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
  echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
  sudo apt-get update && sudo apt-get install k6
  ```

---

## 2. Các kịch bản kiểm thử (Test Scenarios)

### Kịch bản 1: Load Test Danh mục sản phẩm (`load_test_catalog.js`)
- **Mục tiêu**: Kiểm tra khả năng chịu tải của các API đọc thường xuyên nhất: Duyệt danh sách sản phẩm, Tìm kiếm từ khóa, Lọc danh mục.
- **Tải trọng**: Tăng dần từ 0 lên 100 Virtual Users (VUs) đồng thời trong 2.5 phút.
- **Tiêu chuẩn SLA (Thresholds)**:
  - `http_req_duration`: 95% request hoàn thành trong < 300ms (p95 < 300ms).
  - `error_rate`: Tỉ lệ lỗi HTTP < 1%.
- **Chạy kịch bản**:
  ```bash
  k6 run performance-tests/k6/load_test_catalog.js
  ```

### Kịch bản 2: Stress & Spike Test Giỏ hàng (`stress_test_auth_cart.js`)
- **Mục tiêu**: Kiểm tra độ bền bỉ khi hệ thống chịu tải đột biến (Spike) lên 300 VUs liên tục gửi request ghi vào Database (thêm vào giỏ hàng, cập nhật số lượng).
- **Tiêu chuẩn SLA**:
  - `http_req_duration`: p95 < 600ms, p99 < 1500ms.
  - `error_rate`: < 2%.
- **Chạy kịch bản**:
  ```bash
  k6 run performance-tests/k6/stress_test_auth_cart.js
  ```

---

## 3. Các chỉ số hiệu năng quan trọng cần theo dõi

| Chỉ số | Ý nghĩa | Tiêu chuẩn mục tiêu |
| :--- | :--- | :--- |
| **RPS / TPS** | Requests per second (Số lượng request xử lý mỗi giây) | > 500 RPS |
| **p95 Latency** | Thời gian phản hồi của 95% người dùng nhanh nhất | < 300 ms |
| **p99 Latency** | Thời gian phản hồi trong tình huống xấu nhất (99%) | < 800 ms |
| **Error Rate** | Tỉ lệ request thất bại (4xx, 5xx, timeout) | 0% - 1% |
| **Connection Pool** | Số kết nối HikariCP đang bận (Active Connections) | < 80% Max Pool |

---

## 4. Tối ưu hóa hiệu năng Backend đã áp dụng

1. **Redis Caching**: Cache kết quả danh mục sản phẩm và quyền người dùng (Permissions).
2. **Database Indexing**: Đánh chỉ mục (Index) trên các cột thường xuyên query/filter: `product_code`, `sku`, `branch_id`, `created_at`, `status`.
3. **Eager/Batch Fetching**: Sử dụng `@EntityGraph` hoặc `JOIN FETCH` trong Spring Data JPA để loại bỏ vấn đề N+1 Query.
4. **Connection Pool Tuning**: Tinh chỉnh `spring.datasource.hikari.maximum-pool-size=30`, `connection-timeout=20000ms`.
