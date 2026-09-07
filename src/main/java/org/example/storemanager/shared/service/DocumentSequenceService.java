package org.example.storemanager.shared.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentSequenceService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Lấy giá trị kế tiếp từ PostgreSQL sequence (Atomic, Thread-safe, ngoài Transaction context).
     * Tự động khởi tạo sequence nếu chưa tồn tại trong cơ sở dữ liệu.
     */
    public synchronized long getNextSequence(String sequenceName) {
        try {
            Long val = jdbcTemplate.queryForObject("SELECT nextval('" + sequenceName + "')", Long.class);
            if (val != null) {
                return val;
            }
        } catch (Exception e) {
            log.warn("Sequence {} query failed, attempting to create sequence: {}", sequenceName, e.getMessage());
            try {
                jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS " + sequenceName + " START WITH 1001 INCREMENT BY 1");
                Long val = jdbcTemplate.queryForObject("SELECT nextval('" + sequenceName + "')", Long.class);
                if (val != null) {
                    return val;
                }
            } catch (Exception ex) {
                log.error("Failed to create or read sequence {}, falling back to timestamp suffix: {}", sequenceName, ex.getMessage());
            }
        }
        return (System.currentTimeMillis() % 90000) + 10000;
    }

    /**
     * Sinh mã Phiếu thu tiền: PT-yyyyMMdd-XXXXX (Ví dụ: PT-20260908-01001)
     */
    public String generateReceiptCode() {
        long seq = getNextSequence("receipt_voucher_code_seq");
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("PT-%s-%05d", dateStr, seq);
    }

    /**
     * Sinh mã Phiếu chi tiền: PC-yyyyMMdd-XXXXX (Ví dụ: PC-20260908-01001)
     */
    public String generatePaymentCode() {
        long seq = getNextSequence("payment_voucher_code_seq");
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("PC-%s-%05d", dateStr, seq);
    }

    /**
     * Sinh mã Hóa đơn xuất: HD-yyyyMMdd-XXXXX (Ví dụ: HD-20260908-01001)
     */
    public String generateExportInvoiceCode() {
        long seq = getNextSequence("export_invoice_code_seq");
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("HD-%s-%05d", dateStr, seq);
    }
}
