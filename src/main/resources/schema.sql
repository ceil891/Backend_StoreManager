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

-- Migration: Add payment_status and advance_amount to purchase_orders
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) DEFAULT 'UNPAID';
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS advance_amount NUMERIC(18, 2) DEFAULT 0;

-- Migration: Add customer profile fields to customers table
ALTER TABLE customers ADD COLUMN IF NOT EXISTS dob DATE;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS gender VARCHAR(10);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS tax_code VARCHAR(50);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS note TEXT;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS group_id BIGINT;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS area_id BIGINT;

ALTER TABLE customers DROP CONSTRAINT IF EXISTS fk_customer_group;
ALTER TABLE customers ADD CONSTRAINT fk_customer_group FOREIGN KEY (group_id) REFERENCES partner_groups(id);

ALTER TABLE customers DROP CONSTRAINT IF EXISTS fk_customer_area;
ALTER TABLE customers ADD CONSTRAINT fk_customer_area FOREIGN KEY (area_id) REFERENCES areas(id);

-- TC21: Mở rộng bảng debt_ledgers cho phân hệ Sổ nợ & Công nợ
ALTER TABLE debt_ledgers ADD COLUMN IF NOT EXISTS entity_name VARCHAR(200);
ALTER TABLE debt_ledgers ADD COLUMN IF NOT EXISTS entity_type VARCHAR(30);
ALTER TABLE debt_ledgers ADD COLUMN IF NOT EXISTS due_date TIMESTAMP;
ALTER TABLE debt_ledgers ADD COLUMN IF NOT EXISTS status VARCHAR(30);
ALTER TABLE debt_ledgers ADD COLUMN IF NOT EXISTS last_payment_date TIMESTAMP;
ALTER TABLE debt_ledgers ADD COLUMN IF NOT EXISTS account_manager VARCHAR(100);

-- Migration: Add extra fields to shipping_carriers
ALTER TABLE shipping_carriers ADD COLUMN IF NOT EXISTS email VARCHAR(150);
ALTER TABLE shipping_carriers ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
ALTER TABLE shipping_carriers ADD COLUMN IF NOT EXISTS website VARCHAR(255);
ALTER TABLE shipping_carriers ADD COLUMN IF NOT EXISTS address VARCHAR(255);
ALTER TABLE shipping_carriers ADD COLUMN IF NOT EXISTS contact_person VARCHAR(150);
ALTER TABLE shipping_carriers ADD COLUMN IF NOT EXISTS notes TEXT;

-- Migration: Add avatar column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar TEXT;

-- Migration: Finance module columns
ALTER TABLE bank_accounts ADD COLUMN IF NOT EXISTS swift_bic VARCHAR(50);
ALTER TABLE bank_accounts ADD COLUMN IF NOT EXISTS currency VARCHAR(10) DEFAULT 'VND';
ALTER TABLE bank_accounts ADD COLUMN IF NOT EXISTS current_balance NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE bank_accounts ADD COLUMN IF NOT EXISTS available_working_capital NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE bank_accounts ADD COLUMN IF NOT EXISTS account_type VARCHAR(50) DEFAULT 'PRIMARY_OPERATING';
ALTER TABLE bank_accounts ADD COLUMN IF NOT EXISTS opened_date VARCHAR(20);
ALTER TABLE bank_accounts ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'ACTIVE';
ALTER TABLE bank_accounts ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

ALTER TABLE fund_balances ADD COLUMN IF NOT EXISTS branch_name VARCHAR(150);
ALTER TABLE fund_balances ADD COLUMN IF NOT EXISTS manager_name VARCHAR(100);
ALTER TABLE fund_balances ALTER COLUMN branch_id DROP NOT NULL;

ALTER TABLE tax_duties ALTER COLUMN branch_id DROP NOT NULL;

ALTER TABLE transaction_reasons ADD COLUMN IF NOT EXISTS accounting_code VARCHAR(30);
ALTER TABLE transaction_reasons ADD COLUMN IF NOT EXISTS description VARCHAR(255);

ALTER TABLE fixed_assets ADD COLUMN IF NOT EXISTS category VARCHAR(100);
ALTER TABLE fixed_assets ADD COLUMN IF NOT EXISTS accumulated_depreciation NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE fixed_assets ADD COLUMN IF NOT EXISTS useful_life_months INTEGER DEFAULT 36;
ALTER TABLE fixed_assets ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'ACTIVE';

-- Migration: Banners table column alignment
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS sort_order INTEGER DEFAULT 0;
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS image_url TEXT;
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS link_url VARCHAR(255);
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS valid_from TIMESTAMP;
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS valid_until TIMESTAMP;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'banners' AND column_name = 'createdat') THEN
        UPDATE banners SET created_at = createdat WHERE created_at IS NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'banners' AND column_name = 'updatedat') THEN
        UPDATE banners SET updated_at = updatedat WHERE updated_at IS NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'banners' AND column_name = 'isactive') THEN
        UPDATE banners SET is_active = isactive WHERE is_active IS NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'banners' AND column_name = 'sortorder') THEN
        UPDATE banners SET sort_order = sortorder WHERE sort_order IS NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'banners' AND column_name = 'imageurl') THEN
        UPDATE banners SET image_url = imageurl WHERE image_url IS NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'banners' AND column_name = 'linkurl') THEN
        UPDATE banners SET link_url = linkurl WHERE link_url IS NULL;
    END IF;
END $$;

