import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom Metrics
const errorRate = new Rate('error_rate');
const getProductsLatency = new Trend('get_products_duration');
const searchLatency = new Trend('search_duration');

export const options = {
  stages: [
    { duration: '30s', target: 20 },  // Ramp-up lên 20 VUs
    { duration: '1m', target: 50 },   // Giữ tải ở 50 VUs (bình thường)
    { duration: '30s', target: 100 }, // Đỉnh tải 100 VUs
    { duration: '30s', target: 0 },   // Ramp-down về 0
  ],
  thresholds: {
    'http_req_duration': ['p(95)<300', 'p(99)<800'], // 95% request dưới 300ms
    'error_rate': ['rate<0.01'],                     // Tỉ lệ lỗi < 1%
    'get_products_duration': ['p(95)<250'],
  },
};

const BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080/api/v1';

export default function () {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    },
  };

  group('Catalog - Product Listing & Search', () => {
    // 1. GET Danh sách sản phẩm phân trang
    const listRes = http.get(`${BASE_URL}/products?page=0&size=20&sort=createdAt,desc`, params);
    const listOk = check(listRes, {
      'List status is 200': (r) => r.status === 200,
      'List has data': (r) => r.json('data') !== undefined,
    });
    errorRate.add(!listOk);
    getProductsLatency.add(listRes.timings.duration);

    sleep(1);

    // 2. Tìm kiếm sản phẩm theo từ khóa
    const searchRes = http.get(`${BASE_URL}/products?search=ao&page=0&size=10`, params);
    const searchOk = check(searchRes, {
      'Search status is 200': (r) => r.status === 200,
    });
    errorRate.add(!searchOk);
    searchLatency.add(searchRes.timings.duration);

    sleep(1);

    // 3. Lọc sản phẩm theo danh mục
    const catRes = http.get(`${BASE_URL}/products?categoryId=1&page=0&size=10`, params);
    check(catRes, {
      'Category filter status is 200': (r) => r.status === 200,
    });

    sleep(1);
  });
}
