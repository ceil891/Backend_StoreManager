/**
 * Script Đọc kết quả Surefire và Xuất Bảng Tổng Kết Kiểm Thử Backend
 * Chạy: node test-summary.js
 */

const fs = require('fs');
const path = require('path');

const surefireDir = path.join(__dirname, 'target', 'surefire-reports');

if (!fs.existsSync(surefireDir)) {
  console.log('❌ Chưa có kết quả test. Vui lòng chạy: .\\mvnw test');
  process.exit(0);
}

const files = fs.readdirSync(surefireDir).filter(f => f.startsWith('TEST-') && f.endsWith('.xml'));

let totalTests = 0;
let totalFailures = 0;
let totalErrors = 0;
let totalSkipped = 0;
let totalTime = 0;

const testSuites = [];

files.forEach(file => {
  const content = fs.readFileSync(path.join(surefireDir, file), 'utf8');
  
  const nameMatch = content.match(/<testsuite [^>]*name="([^"]+)"/);
  const testsMatch = content.match(/<testsuite [^>]*tests="([^"]+)"/);
  const failuresMatch = content.match(/<testsuite [^>]*failures="([^"]+)"/);
  const errorsMatch = content.match(/<testsuite [^>]*errors="([^"]+)"/);
  const skippedMatch = content.match(/<testsuite [^>]*skipped="([^"]+)"/);
  const timeMatch = content.match(/<testsuite [^>]*time="([^"]+)"/);

  if (nameMatch) {
    const rawName = nameMatch[1];
    const simpleName = rawName.split('.').pop();
    const tests = parseInt(testsMatch ? testsMatch[1] : '0', 10);
    const failures = parseInt(failuresMatch ? failuresMatch[1] : '0', 10);
    const errors = parseInt(errorsMatch ? errorsMatch[1] : '0', 10);
    const skipped = parseInt(skippedMatch ? skippedMatch[1] : '0', 10);
    const time = parseFloat(timeMatch ? timeMatch[1] : '0');

    if (tests > 0) {
      totalTests += tests;
      totalFailures += failures;
      totalErrors += errors;
      totalSkipped += skipped;
      totalTime += time;

      testSuites.push({
        suite: simpleName,
        fullName: rawName,
        tests,
        passed: tests - failures - errors - skipped,
        failures,
        errors,
        time: time.toFixed(3) + 's',
        status: (failures === 0 && errors === 0) ? '🟢 PASSED' : '🔴 FAILED'
      });
    }
  }
});

console.log('\n' + '='.repeat(85));
console.log('📊 BẢNG TỔNG KẾT KẾT QUẢ KIỂM THỬ BACKEND (JUNIT 5 & MOCKITO REPORT)');
console.log('='.repeat(85));
console.log(
  '| ' + 'STT'.padEnd(4) +
  '| ' + 'Tên Bộ Kiểm Thử (Test Suite)'.padEnd(32) +
  '| ' + 'Tổng'.padEnd(6) +
  '| ' + 'Pass'.padEnd(6) +
  '| ' + 'Fail'.padEnd(6) +
  '| ' + 'Thời gian'.padEnd(11) +
  '| ' + 'Trạng thái'.padEnd(10) + ' |'
);
console.log('-'.repeat(85));

testSuites.forEach((s, idx) => {
  console.log(
    '| ' + String(idx + 1).padEnd(4) +
    '| ' + s.suite.padEnd(32) +
    '| ' + String(s.tests).padEnd(6) +
    '| ' + String(s.passed).padEnd(6) +
    '| ' + String(s.failures + s.errors).padEnd(6) +
    '| ' + s.time.padEnd(11) +
    '| ' + s.status.padEnd(10) + ' |'
  );
});

console.log('-'.repeat(85));
const passRate = totalTests > 0 ? (((totalTests - totalFailures - totalErrors) / totalTests) * 100).toFixed(1) : 0;
console.log(`📌 TỔNG KẾT TOÀN BỘ BACKEND:`);
console.log(`   • Tổng số bài test:   ${totalTests} tests`);
console.log(`   • Đạt chuẩn (Passed): ${totalTests - totalFailures - totalErrors} tests (${passRate}%)`);
console.log(`   • Thất bại (Failed):  ${totalFailures + totalErrors} tests`);
console.log(`   • Tổng thời gian:     ${totalTime.toFixed(2)}s`);
console.log(`   • Đánh giá chất lượng: ${totalFailures + totalErrors === 0 ? '🏆 100% BUILD SUCCESS - SẴN SÀNG TRIỂN KHAI' : '⚠️ CẦN SỬA LỖI'}`);
console.log('='.repeat(85) + '\n');
