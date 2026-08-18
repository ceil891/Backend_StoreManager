package org.example.storemanager.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * DatabaseMigrationRunner - Runs safe idempotent ALTER TABLE migrations on startup.
 * Uses IF NOT EXISTS so it's safe to run multiple times.
 * Order(1) ensures it runs first among ApplicationRunners.
 */
@Slf4j
@Component
@Lazy(false)
@Order(1)
@RequiredArgsConstructor
public class DatabaseMigrationConfig implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS customer_name VARCHAR(200)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS customer_phone VARCHAR(30)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS shipping_address TEXT"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS order_origin VARCHAR(50) DEFAULT 'ONLINE'"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) DEFAULT 'UNPAID'"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS carrier_id BIGINT"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS carrier VARCHAR(100)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS driver_id BIGINT"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS tracking_code VARCHAR(100)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS tracking_url VARCHAR(500)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS shipper_name VARCHAR(150)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS shipper_phone VARCHAR(30)"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS delivery_status VARCHAR(50) DEFAULT 'UNASSIGNED'"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP"
            );
            jdbcTemplate.execute(
                "ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS assigned_by VARCHAR(100)"
            );

            // Audit Trail table for delivery assignments
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS delivery_assignment_histories (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "order_id BIGINT NOT NULL, " +
                "order_code VARCHAR(50), " +
                "carrier_id BIGINT, " +
                "carrier_name VARCHAR(150), " +
                "shipper_id BIGINT, " +
                "shipper_name VARCHAR(150), " +
                "shipper_phone VARCHAR(30), " +
                "tracking_code VARCHAR(100), " +
                "tracking_url VARCHAR(500), " +
                "delivery_status VARCHAR(50), " +
                "action_type VARCHAR(50), " +
                "assigned_at TIMESTAMP, " +
                "assigned_by VARCHAR(100), " +
                "note VARCHAR(500), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_deleted BOOLEAN DEFAULT FALSE" +
                ")"
            );
            jdbcTemplate.execute(
                "UPDATE sale_orders SET order_origin = 'ONLINE' WHERE order_origin = 'ONLINE_STORE' OR order_origin IS NULL"
            );
            jdbcTemplate.execute(
                "UPDATE sale_orders SET payment_status = 'UNPAID' WHERE payment_status IS NULL"
            );
            jdbcTemplate.execute(
                "UPDATE sale_orders SET is_active = true WHERE is_active IS NULL"
            );
            try {
                jdbcTemplate.execute("ALTER TABLE sale_orders DROP CONSTRAINT IF EXISTS sale_orders_order_origin_check");
                jdbcTemplate.execute("ALTER TABLE sale_orders ADD CONSTRAINT sale_orders_order_origin_check CHECK (order_origin IN ('MANUAL', 'POS', 'ONLINE', 'ONLINE_STORE'))");
            } catch (Exception ex) {
                log.info("[Migration] sale_orders_order_origin_check constraint note: {}", ex.getMessage());
            }
            try {
                jdbcTemplate.execute("ALTER TABLE sale_orders DROP CONSTRAINT IF EXISTS sale_orders_status_check");
                jdbcTemplate.execute("ALTER TABLE sale_orders ADD CONSTRAINT sale_orders_status_check CHECK (status IN ('PENDING','CONFIRMED','PROCESSING','DELIVERING','SHIPPED','COMPLETED','DELIVERED','CANCELLED'))");
                log.info("[Migration] sale_orders_status_check constraint updated OK.");
            } catch (Exception ex) {
                log.info("[Migration] sale_orders_status_check constraint note: {}", ex.getMessage());
            }
            log.info("[Migration] sale_orders columns (customer_name, customer_phone, shipping_address, is_active, order_origin, payment_status) ensured OK.");

            // ---- sale_order_details migrations ----
            try {
                jdbcTemplate.execute("ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS unit_price NUMERIC(18, 2)");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(18, 2) DEFAULT 0");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(18, 2) DEFAULT 0");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS total_amount NUMERIC(18, 2)");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS order_id BIGINT");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS sale_order_id BIGINT");

                jdbcTemplate.execute("UPDATE sale_order_details SET unit_price = unit_price_snapshot WHERE unit_price IS NULL AND unit_price_snapshot IS NOT NULL");
                jdbcTemplate.execute("UPDATE sale_order_details SET total_amount = sub_total WHERE total_amount IS NULL AND sub_total IS NOT NULL");
                jdbcTemplate.execute("UPDATE sale_order_details SET sub_total = total_amount WHERE sub_total IS NULL AND total_amount IS NOT NULL");
                jdbcTemplate.execute("UPDATE sale_order_details SET sale_order_id = order_id WHERE sale_order_id IS NULL AND order_id IS NOT NULL");
                jdbcTemplate.execute("UPDATE sale_order_details SET order_id = sale_order_id WHERE order_id IS NULL AND sale_order_id IS NOT NULL");

                jdbcTemplate.execute("ALTER TABLE sale_order_details ALTER COLUMN unit_price DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ALTER COLUMN total_amount DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ALTER COLUMN sub_total DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ALTER COLUMN sale_order_id DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE sale_order_details ALTER COLUMN order_id DROP NOT NULL");
                log.info("[Migration] sale_order_details columns (unit_price, discount_amount, tax_amount, total_amount, sub_total, sale_order_id, order_id) ensured OK.");
            } catch (Exception ex) {
                log.warn("[Migration] sale_order_details migration warning: {}", ex.getMessage());
            }

            // ---- delivery_assignment_histories: add missing BaseEntity columns ----
            try {
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS created_by VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT false");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS trace_id VARCHAR(255)");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0");
                jdbcTemplate.execute("ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS tenant_id BIGINT");
                jdbcTemplate.execute("UPDATE delivery_assignment_histories SET version = 0 WHERE version IS NULL");
                jdbcTemplate.execute("UPDATE delivery_assignment_histories SET is_deleted = false WHERE is_deleted IS NULL");
                jdbcTemplate.execute("UPDATE delivery_assignment_histories SET is_locked = false WHERE is_locked IS NULL");
                log.info("[Migration] delivery_assignment_histories BaseEntity columns ensured OK.");
            } catch (Exception ex) {
                log.warn("[Migration] delivery_assignment_histories migration warning: {}", ex.getMessage());
            }

            // ---- loyalty_tiers migrations ----
            try {
                jdbcTemplate.execute("ALTER TABLE loyalty_tiers ADD COLUMN IF NOT EXISTS min_spend NUMERIC(15, 2)");
                jdbcTemplate.execute("ALTER TABLE loyalty_tiers ADD COLUMN IF NOT EXISTS max_spend NUMERIC(15, 2)");
                log.info("[Migration] loyalty_tiers columns (min_spend, max_spend) ensured OK.");
            } catch (Exception ex) {
                log.warn("[Migration] loyalty_tiers migration warning: {}", ex.getMessage());
            }

            // ---- product_variants migrations ----
            try {
                jdbcTemplate.execute("ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS attribute_signature VARCHAR(255)");
                log.info("[Migration] product_variants column attribute_signature ensured OK.");
            } catch (Exception ex) {
                log.warn("[Migration] product_variants migration warning: {}", ex.getMessage());
            }

            // ---- warehouse_zones migrations ----
            try {
                jdbcTemplate.execute("ALTER TABLE warehouse_zones DROP CONSTRAINT IF EXISTS uk_hyb8d4xfdqbv7tnwq5d5wny81");
                log.info("[Migration] warehouse_zones constraint uk_hyb8d4xfdqbv7tnwq5d5wny81 dropped OK.");
            } catch (Exception ex) {
                log.warn("[Migration] warehouse_zones migration warning: {}", ex.getMessage());
            }

            // ---- product_attributes & attribute_values default seed ----
            try {
                jdbcTemplate.execute(
                    "INSERT INTO product_attributes (attribute_name, attribute_code, attribute_type, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'Màu sắc (Color)', 'COLOR', 'TEXT', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP " +
                    "WHERE NOT EXISTS (SELECT 1 FROM product_attributes WHERE attribute_code = 'COLOR')"
                );
                jdbcTemplate.execute(
                    "INSERT INTO product_attributes (attribute_name, attribute_code, attribute_type, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'Kích thước (Size)', 'SIZE', 'TEXT', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP " +
                    "WHERE NOT EXISTS (SELECT 1 FROM product_attributes WHERE attribute_code = 'SIZE')"
                );
                jdbcTemplate.execute(
                    "INSERT INTO product_attributes (attribute_name, attribute_code, attribute_type, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'Dung lượng (Storage)', 'STORAGE', 'TEXT', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP " +
                    "WHERE NOT EXISTS (SELECT 1 FROM product_attributes WHERE attribute_code = 'STORAGE')"
                );

                // Seed attribute values for COLOR
                jdbcTemplate.execute(
                    "INSERT INTO attribute_values (attribute_id, value_text, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT id, 'Đỏ (Red)', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM product_attributes WHERE attribute_code = 'COLOR' " +
                    "AND NOT EXISTS (SELECT 1 FROM attribute_values av JOIN product_attributes pa ON av.attribute_id = pa.id WHERE pa.attribute_code = 'COLOR' AND av.value_text = 'Đỏ (Red)')"
                );
                jdbcTemplate.execute(
                    "INSERT INTO attribute_values (attribute_id, value_text, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT id, 'Xanh Đen (Navy)', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM product_attributes WHERE attribute_code = 'COLOR' " +
                    "AND NOT EXISTS (SELECT 1 FROM attribute_values av JOIN product_attributes pa ON av.attribute_id = pa.id WHERE pa.attribute_code = 'COLOR' AND av.value_text = 'Xanh Đen (Navy)')"
                );
                jdbcTemplate.execute(
                    "INSERT INTO attribute_values (attribute_id, value_text, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT id, 'Đen (Black)', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM product_attributes WHERE attribute_code = 'COLOR' " +
                    "AND NOT EXISTS (SELECT 1 FROM attribute_values av JOIN product_attributes pa ON av.attribute_id = pa.id WHERE pa.attribute_code = 'COLOR' AND av.value_text = 'Đen (Black)')"
                );
                jdbcTemplate.execute(
                    "INSERT INTO attribute_values (attribute_id, value_text, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT id, 'Trắng (White)', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM product_attributes WHERE attribute_code = 'COLOR' " +
                    "AND NOT EXISTS (SELECT 1 FROM attribute_values av JOIN product_attributes pa ON av.attribute_id = pa.id WHERE pa.attribute_code = 'COLOR' AND av.value_text = 'Trắng (White)')"
                );

                // Seed attribute values for SIZE
                jdbcTemplate.execute(
                    "INSERT INTO attribute_values (attribute_id, value_text, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT id, 'Size S', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM product_attributes WHERE attribute_code = 'SIZE' " +
                    "AND NOT EXISTS (SELECT 1 FROM attribute_values av JOIN product_attributes pa ON av.attribute_id = pa.id WHERE pa.attribute_code = 'SIZE' AND av.value_text = 'Size S')"
                );
                jdbcTemplate.execute(
                    "INSERT INTO attribute_values (attribute_id, value_text, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT id, 'Size M', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM product_attributes WHERE attribute_code = 'SIZE' " +
                    "AND NOT EXISTS (SELECT 1 FROM attribute_values av JOIN product_attributes pa ON av.attribute_id = pa.id WHERE pa.attribute_code = 'SIZE' AND av.value_text = 'Size M')"
                );
                jdbcTemplate.execute(
                    "INSERT INTO attribute_values (attribute_id, value_text, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT id, 'Size L', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM product_attributes WHERE attribute_code = 'SIZE' " +
                    "AND NOT EXISTS (SELECT 1 FROM attribute_values av JOIN product_attributes pa ON av.attribute_id = pa.id WHERE pa.attribute_code = 'SIZE' AND av.value_text = 'Size L')"
                );
                jdbcTemplate.execute(
                    "INSERT INTO attribute_values (attribute_id, value_text, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT id, 'Size XL', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM product_attributes WHERE attribute_code = 'SIZE' " +
                    "AND NOT EXISTS (SELECT 1 FROM attribute_values av JOIN product_attributes pa ON av.attribute_id = pa.id WHERE pa.attribute_code = 'SIZE' AND av.value_text = 'Size XL')"
                );

                // Seed attribute values for STORAGE
                jdbcTemplate.execute(
                    "INSERT INTO attribute_values (attribute_id, value_text, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT id, '128GB', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM product_attributes WHERE attribute_code = 'STORAGE' " +
                    "AND NOT EXISTS (SELECT 1 FROM attribute_values av JOIN product_attributes pa ON av.attribute_id = pa.id WHERE pa.attribute_code = 'STORAGE' AND av.value_text = '128GB')"
                );
                jdbcTemplate.execute(
                    "INSERT INTO attribute_values (attribute_id, value_text, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT id, '256GB', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM product_attributes WHERE attribute_code = 'STORAGE' " +
                    "AND NOT EXISTS (SELECT 1 FROM attribute_values av JOIN product_attributes pa ON av.attribute_id = pa.id WHERE pa.attribute_code = 'STORAGE' AND av.value_text = '256GB')"
                );
                jdbcTemplate.execute(
                    "INSERT INTO attribute_values (attribute_id, value_text, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT id, '512GB', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM product_attributes WHERE attribute_code = 'STORAGE' " +
                    "AND NOT EXISTS (SELECT 1 FROM attribute_values av JOIN product_attributes pa ON av.attribute_id = pa.id WHERE pa.attribute_code = 'STORAGE' AND av.value_text = '512GB')"
                );

                log.info("[Migration] product_attributes & attribute_values default seed ensured OK.");
            } catch (Exception ex) {
                log.warn("[Migration] product_attributes seed warning: {}", ex.getMessage());
            }

            // ---- colors standard seed ----
            try {
                jdbcTemplate.execute("INSERT INTO colors (color_code, color_name, hex_value, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'CLR-RED', 'Đỏ (Red)', '#EF4444', 'Màu đỏ nổi bật', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM colors WHERE color_code = 'CLR-RED')");
                jdbcTemplate.execute("INSERT INTO colors (color_code, color_name, hex_value, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'CLR-NAVY', 'Xanh Đen (Navy)', '#1E3A8A', 'Màu xanh đen sang trọng', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM colors WHERE color_code = 'CLR-NAVY')");
                jdbcTemplate.execute("INSERT INTO colors (color_code, color_name, hex_value, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'CLR-BLACK', 'Đen (Black)', '#111827', 'Màu đen huyền bí', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM colors WHERE color_code = 'CLR-BLACK')");
                jdbcTemplate.execute("INSERT INTO colors (color_code, color_name, hex_value, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'CLR-WHITE', 'Trắng (White)', '#F9FAFB', 'Màu trắng tinh tế', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM colors WHERE color_code = 'CLR-WHITE')");
                jdbcTemplate.execute("INSERT INTO colors (color_code, color_name, hex_value, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'CLR-BLUE', 'Xanh Dương (Blue)', '#3B82F6', 'Màu xanh năng động', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM colors WHERE color_code = 'CLR-BLUE')");
                jdbcTemplate.execute("INSERT INTO colors (color_code, color_name, hex_value, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'CLR-GOLD', 'Vàng Gold', '#EAB308', 'Màu vàng kim quý phái', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM colors WHERE color_code = 'CLR-GOLD')");
                jdbcTemplate.execute("INSERT INTO colors (color_code, color_name, hex_value, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'CLR-TITAN', 'Titan Tự Nhiên', '#78716C', 'Màu Titan nguyên bản', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM colors WHERE color_code = 'CLR-TITAN')");
                log.info("[Migration] colors default seed ensured OK.");
            } catch (Exception ex) {
                log.warn("[Migration] colors seed warning: {}", ex.getMessage());
            }

            // ---- sizes standard seed ----
            try {
                jdbcTemplate.execute("INSERT INTO sizes (size_code, size_name, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'SZ-128GB', '128GB', 'Dung lượng bộ nhớ 128GB', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sizes WHERE size_code = 'SZ-128GB')");
                jdbcTemplate.execute("INSERT INTO sizes (size_code, size_name, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'SZ-256GB', '256GB', 'Dung lượng bộ nhớ 256GB', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sizes WHERE size_code = 'SZ-256GB')");
                jdbcTemplate.execute("INSERT INTO sizes (size_code, size_name, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'SZ-512GB', '512GB', 'Dung lượng bộ nhớ 512GB', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sizes WHERE size_code = 'SZ-512GB')");
                jdbcTemplate.execute("INSERT INTO sizes (size_code, size_name, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'SZ-1TB', '1TB', 'Dung lượng bộ nhớ 1TB', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sizes WHERE size_code = 'SZ-1TB')");
                jdbcTemplate.execute("INSERT INTO sizes (size_code, size_name, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'SZ-S', 'Size S', 'Kích thước nhỏ (Small)', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sizes WHERE size_code = 'SZ-S')");
                jdbcTemplate.execute("INSERT INTO sizes (size_code, size_name, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'SZ-M', 'Size M', 'Kích thước vừa (Medium)', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sizes WHERE size_code = 'SZ-M')");
                jdbcTemplate.execute("INSERT INTO sizes (size_code, size_name, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'SZ-L', 'Size L', 'Kích thước lớn (Large)', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sizes WHERE size_code = 'SZ-L')");
                jdbcTemplate.execute("INSERT INTO sizes (size_code, size_name, description, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'SZ-XL', 'Size XL', 'Kích thước rất lớn (Extra Large)', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sizes WHERE size_code = 'SZ-XL')");
                log.info("[Migration] sizes default seed ensured OK.");
            } catch (Exception ex) {
                log.warn("[Migration] sizes seed warning: {}", ex.getMessage());
            }

            // ---- n8n_chat_histories_ric_qlbh migrations ----
            try {
                jdbcTemplate.execute("ALTER TABLE IF EXISTS n8n_chat_histories_ric_qlbh ALTER COLUMN \"type\" SET DEFAULT 'message'");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS n8n_chat_histories_ric_qlbh ALTER COLUMN \"type\" DROP NOT NULL");
                log.info("[Migration] n8n_chat_histories_ric_qlbh column type default ensured OK.");
            } catch (Exception ex) {
                log.warn("[Migration] n8n_chat_histories_ric_qlbh migration note: {}", ex.getMessage());
            }

            // ---- units & product base_unit migrations ----
            try {
                jdbcTemplate.execute(
                    "INSERT INTO units (unit_code, unit_name, description, unit_type, conversion_factor, precision_decimals, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'UNIT-CAI', 'Cái', 'Đơn vị tính tiêu chuẩn', 'COUNT', 1, 0, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP " +
                    "WHERE NOT EXISTS (SELECT 1 FROM units WHERE unit_code = 'UNIT-CAI')"
                );
                jdbcTemplate.execute(
                    "INSERT INTO units (unit_code, unit_name, description, unit_type, conversion_factor, precision_decimals, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'UNIT-CHIE', 'Chiếc', 'Đơn vị tính máy tính, màn hình', 'COUNT', 1, 0, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP " +
                    "WHERE NOT EXISTS (SELECT 1 FROM units WHERE unit_code = 'UNIT-CHIE')"
                );
                jdbcTemplate.execute(
                    "INSERT INTO units (unit_code, unit_name, description, unit_type, conversion_factor, precision_decimals, is_active, is_deleted, created_at, updated_at) " +
                    "SELECT 'UNIT-BO', 'Bộ', 'Đơn vị tính theo combo', 'PACKAGE', 1, 0, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP " +
                    "WHERE NOT EXISTS (SELECT 1 FROM units WHERE unit_code = 'UNIT-BO')"
                );
                // Auto link any product without base_unit_id to UNIT-CAI
                jdbcTemplate.execute(
                    "UPDATE products SET base_unit_id = (SELECT id FROM units WHERE unit_code = 'UNIT-CAI' LIMIT 1) " +
                    "WHERE base_unit_id IS NULL AND EXISTS (SELECT 1 FROM units WHERE unit_code = 'UNIT-CAI')"
                );
                log.info("[Migration] units & product base_unit defaults ensured OK.");
            } catch (Exception ex) {
                log.warn("[Migration] units seed migration warning: {}", ex.getMessage());
            }

            // ---- receipt_vouchers & payment_vouchers migrations ----
            try {
                jdbcTemplate.execute("ALTER TABLE IF EXISTS receipt_vouchers ALTER COLUMN reason_id DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS payment_vouchers ALTER COLUMN reason_id DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS receipt_vouchers ALTER COLUMN branch_id DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS payment_vouchers ALTER COLUMN branch_id DROP NOT NULL");
                log.info("[Migration] receipt_vouchers & payment_vouchers reason_id and branch_id constraints updated OK.");
            } catch (Exception ex) {
                log.warn("[Migration] receipt/payment vouchers constraint migration note: {}", ex.getMessage());
            }

            // ---- delivery_notes migrations ----
            try {
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ALTER COLUMN packing_list_id DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ADD COLUMN IF NOT EXISTS waybill_code VARCHAR(100)");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ADD COLUMN IF NOT EXISTS customer_name VARCHAR(200)");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ADD COLUMN IF NOT EXISTS delivery_staff VARCHAR(150)");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ADD COLUMN IF NOT EXISTS total_weight DOUBLE PRECISION");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ADD COLUMN IF NOT EXISTS package_count INTEGER");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ADD COLUMN IF NOT EXISTS product_count INTEGER");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ADD COLUMN IF NOT EXISTS signer_name VARCHAR(150)");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ADD COLUMN IF NOT EXISTS signed_at VARCHAR(100)");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ADD COLUMN IF NOT EXISTS condition_notes TEXT");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ADD COLUMN IF NOT EXISTS attachments TEXT");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ADD COLUMN IF NOT EXISTS rejection_reason_type VARCHAR(100)");
                jdbcTemplate.execute("ALTER TABLE IF EXISTS delivery_notes ADD COLUMN IF NOT EXISTS rejection_reason_detail TEXT");
                log.info("[Migration] delivery_notes columns ensured OK.");
            } catch (Exception ex) {
                log.warn("[Migration] delivery_notes migration note: {}", ex.getMessage());
            }

            // ---- Clean up old mock test orders ----
            try {
                jdbcTemplate.execute("DELETE FROM sale_order_details WHERE sale_order_id IN (SELECT id FROM sale_orders WHERE order_code LIKE 'ORD-POS-2026-100%' OR order_code LIKE 'ORD-ONL-2026-100%')");
                jdbcTemplate.execute("DELETE FROM sale_orders WHERE order_code LIKE 'ORD-POS-2026-100%' OR order_code LIKE 'ORD-ONL-2026-100%'");
                log.info("[Migration] old mock test orders cleaned up OK.");
            } catch (Exception ex) {
                log.warn("[Migration] clean mock orders note: {}", ex.getMessage());
            }
        } catch (Exception e) {
          log.warn("[Migration] database migration warning: {}", e.getMessage());
        }
    }
}

