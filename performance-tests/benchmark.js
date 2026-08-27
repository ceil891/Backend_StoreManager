/**
 * Script Kiểm Thử Hiệu Năng Node.js (Không cần cài đặt thêm công cụ bên ngoài)
 * Chạy trực tiếp: node performance-tests/benchmark.js [URL] [CONCURRENCY] [DURATION_SEC]
 * Ví dụ: node performance-tests/benchmark.js http://localhost:8080/api/v1/products 50 15
 */

const targetUrl = process.argv[2] || 'http://localhost:8080/api/v1/products?page=0&size=20';
const concurrency = parseInt(process.argv[3] || '30', 10);
const durationSec = parseInt(process.argv[4] || '10', 10);

console.log('='.repeat(65));
console.log(`🚀 BẮT ĐẦU KIỂM THỬ HIỆU NĂNG BACKEND (LOAD TEST RUNNER)`);
console.log(`📍 Endpoint:    ${targetUrl}`);
console.log(`👥 Đồng thời:   ${concurrency} Virtual Users (Connections)`);
console.log(`⏱️  Thời gian:   ${durationSec} giây`);
console.log('='.repeat(65));

let totalRequests = 0;
let successRequests = 0;
let failedRequests = 0;
const latencies = [];

const startTime = Date.now();
const endTime = startTime + durationSec * 1000;
let isRunning = true;

async function worker() {
  while (isRunning && Date.now() < endTime) {
    const reqStart = performance.now();
    try {
      const res = await fetch(targetUrl, {
        headers: { 'Accept': 'application/json' },
      });
      const reqDuration = performance.now() - reqStart;
      latencies.push(reqDuration);
      totalRequests++;
      if (res.ok) {
        successRequests++;
      } else {
        failedRequests++;
      }
    } catch (err) {
      const reqDuration = performance.now() - reqStart;
      latencies.push(reqDuration);
      totalRequests++;
      failedRequests++;
    }
  }
}

async function runBenchmark() {
  const workers = [];
  for (let i = 0; i < concurrency; i++) {
    workers.push(worker());
  }

  await Promise.all(workers);
  isRunning = false;

  const totalTimeSec = (Date.now() - startTime) / 1000;
  latencies.sort((a, b) => a - b);

  const avg = latencies.reduce((a, b) => a + b, 0) / (latencies.length || 1);
  const min = latencies[0] || 0;
  const max = latencies[latencies.length - 1] || 0;
  const p50 = latencies[Math.floor(latencies.length * 0.50)] || 0;
  const p90 = latencies[Math.floor(latencies.length * 0.90)] || 0;
  const p95 = latencies[Math.floor(latencies.length * 0.95)] || 0;
  const p99 = latencies[Math.floor(latencies.length * 0.99)] || 0;
  const rps = totalRequests / totalTimeSec;
  const errorRate = (failedRequests / (totalRequests || 1)) * 100;

  console.log('\n📊 KẾT QUẢ KIỂM THỬ HIỆU NĂNG:');
  console.log('-'.repeat(65));
  console.log(`✅ Tổng request hoàn thành:  ${totalRequests} requests`);
  console.log(`⚡ Throughput (RPS):          ${rps.toFixed(2)} req/sec`);
  console.log(`❌ Thất bại / Lỗi:            ${failedRequests} (${errorRate.toFixed(2)}%)`);
  console.log('-'.repeat(65));
  console.log(`🕒 Thời gian phản hồi (Latency):`);
  console.log(`   • Trung bình (Avg):        ${avg.toFixed(2)} ms`);
  console.log(`   • Nhanh nhất (Min):        ${min.toFixed(2)} ms`);
  console.log(`   • Chậm nhất (Max):         ${max.toFixed(2)} ms`);
  console.log(`   • 50th percentile (p50):   ${p50.toFixed(2)} ms`);
  console.log(`   • 90th percentile (p90):   ${p90.toFixed(2)} ms`);
  console.log(`   • 95th percentile (p95):   ${p95.toFixed(2)} ms  <-- ${p95 < 300 ? '🟢 ĐẠT SLA (<300ms)' : '🔴 VƯỢT SLA'}`);
  console.log(`   • 99th percentile (p99):   ${p99.toFixed(2)} ms`);
  console.log('='.repeat(65));
}

runBenchmark();
