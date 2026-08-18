-- Migration: Add online order fields to sale_orders table
-- Safe to run multiple times (IF NOT EXISTS)
ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS customer_name VARCHAR(200);
ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS customer_phone VARCHAR(30);
ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS shipping_address TEXT;
ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS order_origin VARCHAR(50) DEFAULT 'ONLINE';
ALTER TABLE sale_orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) DEFAULT 'UNPAID';
UPDATE sale_orders SET order_origin = 'ONLINE' WHERE order_origin = 'ONLINE_STORE' OR order_origin IS NULL;
UPDATE sale_orders SET payment_status = 'UNPAID' WHERE payment_status IS NULL;
ALTER TABLE sale_orders DROP CONSTRAINT IF EXISTS sale_orders_order_origin_check;
ALTER TABLE sale_orders ADD CONSTRAINT sale_orders_order_origin_check CHECK (order_origin IN ('MANUAL', 'POS', 'ONLINE', 'ONLINE_STORE'));

-- Migration: Add fields to sale_order_details table
ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS unit_price NUMERIC(18, 2);
ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS total_amount NUMERIC(18, 2);
ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS order_id BIGINT;
ALTER TABLE sale_order_details ADD COLUMN IF NOT EXISTS sale_order_id BIGINT;
UPDATE sale_order_details SET unit_price = unit_price_snapshot WHERE unit_price IS NULL AND unit_price_snapshot IS NOT NULL;
UPDATE sale_order_details SET total_amount = sub_total WHERE total_amount IS NULL AND sub_total IS NOT NULL;
UPDATE sale_order_details SET sub_total = total_amount WHERE sub_total IS NULL AND total_amount IS NOT NULL;
UPDATE sale_order_details SET sale_order_id = order_id WHERE sale_order_id IS NULL AND order_id IS NOT NULL;
UPDATE sale_order_details SET order_id = sale_order_id WHERE order_id IS NULL AND sale_order_id IS NOT NULL;
ALTER TABLE sale_order_details ALTER COLUMN unit_price DROP NOT NULL;
ALTER TABLE sale_order_details ALTER COLUMN total_amount DROP NOT NULL;
ALTER TABLE sale_order_details ALTER COLUMN sub_total DROP NOT NULL;
ALTER TABLE sale_order_details ALTER COLUMN sale_order_id DROP NOT NULL;
ALTER TABLE sale_order_details ALTER COLUMN order_id DROP NOT NULL;

-- Migration: Fix delivery_assignment_histories missing BaseEntity columns
ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);
ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT false;
ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS trace_id VARCHAR(255);
ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;
ALTER TABLE delivery_assignment_histories ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
UPDATE delivery_assignment_histories SET version = 0 WHERE version IS NULL;
UPDATE delivery_assignment_histories SET is_deleted = false WHERE is_deleted IS NULL;
UPDATE delivery_assignment_histories SET is_locked = false WHERE is_locked IS NULL;

-- Migration: Add Indexes for foreign keys to optimize query performance
CREATE INDEX IF NOT EXISTS idx_sale_od_order ON sale_order_details(order_id);
CREATE INDEX IF NOT EXISTS idx_sale_od_sale_order ON sale_order_details(sale_order_id);
CREATE INDEX IF NOT EXISTS idx_sale_od_variant ON sale_order_details(product_variant_id);
CREATE INDEX IF NOT EXISTS idx_suppliers_supplier_code ON suppliers(supplier_code);
CREATE INDEX IF NOT EXISTS idx_sale_orders_customer_id ON sale_orders(customer_id);

-- Migration: Add attribute_signature to product_variants
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS attribute_signature VARCHAR(500);

-- Migration: Add product_variant_id to stock_ledgers
ALTER TABLE stock_ledgers ADD COLUMN IF NOT EXISTS product_variant_id BIGINT;

ALTER TABLE stock_ledgers DROP CONSTRAINT IF EXISTS fk_stock_ledger_variant;
ALTER TABLE stock_ledgers ADD CONSTRAINT fk_stock_ledger_variant
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id);

-- Migration: Partial unique index for active variant barcodes
CREATE UNIQUE INDEX IF NOT EXISTS ux_variant_barcode_active
ON product_variants(barcode)
WHERE is_deleted = false AND barcode IS NOT NULL AND barcode != '';

-- Migration: Partial unique index for active variant attribute signature per product
CREATE UNIQUE INDEX IF NOT EXISTS ux_variant_attr_sig_active
ON product_variants(product_id, attribute_signature)
WHERE is_deleted = false AND attribute_signature IS NOT NULL AND attribute_signature != '';

-- Migration: Index on stock_ledgers.product_variant_id
CREATE INDEX IF NOT EXISTS idx_stock_ledger_variant ON stock_ledgers(product_variant_id);

-- Migration: Add column type if not exists with default 'message' in n8n_chat_histories_ric_qlbh table
ALTER TABLE IF EXISTS n8n_chat_histories_ric_qlbh ADD COLUMN IF NOT EXISTS "type" VARCHAR(50) DEFAULT 'message';



