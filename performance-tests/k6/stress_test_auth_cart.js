import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const loginErrorRate = new Rate('login_error_rate');
const cartErrorRate = new Rate('cart_error_rate');
const loginLatency = new Trend('login_duration');
const cartAddLatency = new Trend('cart_add_duration');

export const options = {
  stages: [
    { duration: '20s', target: 30 },   // Tăng nhanh lên 30 VUs
    { duration: '1m', target: 150 },   // Stress test ở 150 VUs
    { duration: '30s', target: 300 },  // Spike test đỉnh 300 VUs
    { duration: '30s', target: 0 },    // Hạ tải
  ],
  thresholds: {
    'http_req_duration': ['p(95)<600', 'p(99)<1500'],
    'login_error_rate': ['rate<0.02'],
    'cart_error_rate': ['rate<0.02'],
  },
};

const BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080/api/v1';

export default function () {
  let guestToken = null;

  group('Cart - Guest Shopping Flow', () => {
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };

    // 1. Thêm sản phẩm vào giỏ hàng
    const payload = JSON.stringify({
      productVariantId: 1,
      quantity: 1,
    });

    const addRes = http.post(`${BASE_URL}/cart/items`, payload, { headers });
    const addSuccess = check(addRes, {
      'Add to cart status 200': (r) => r.status === 200,
    });
    cartErrorRate.add(!addSuccess);
    cartAddLatency.add(addRes.timings.duration);

    // Lấy guest token nếu có trong response header
    const receivedToken = addRes.headers['Guest-Token'] || addRes.headers['guest-token'];
    if (receivedToken) {
      headers['Guest-Token'] = receivedToken;
    }

    sleep(1);

    // 2. Lấy thông tin giỏ hàng
    const getCartRes = http.get(`${BASE_URL}/cart`, { headers });
    check(getCartRes, {
      'Get cart status 200': (r) => r.status === 200,
    });

    sleep(1);
  });
}
