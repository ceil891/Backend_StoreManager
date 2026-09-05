-- ====================================================================
-- SCHEMA MIGRATIONS (Safe for clean and existing databases)
-- ====================================================================

-- 1. Sale Orders
ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS customer_name VARCHAR(200);
ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS customer_phone VARCHAR(30);
ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS shipping_address TEXT;
ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS order_origin VARCHAR(50) DEFAULT 'ONLINE';
ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) DEFAULT 'UNPAID';

-- 2. Sale Order Details
ALTER TABLE IF EXISTS sale_order_details ADD COLUMN IF NOT EXISTS unit_price NUMERIC(18, 2);
ALTER TABLE IF EXISTS sale_order_details ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS sale_order_details ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS sale_order_details ADD COLUMN IF NOT EXISTS total_amount NUMERIC(18, 2);
ALTER TABLE IF EXISTS sale_order_details ADD COLUMN IF NOT EXISTS order_id BIGINT;
ALTER TABLE IF EXISTS sale_order_details ADD COLUMN IF NOT EXISTS sale_order_id BIGINT;

-- 3. Delivery Assignment Histories
ALTER TABLE IF EXISTS delivery_assignment_histories ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE IF EXISTS delivery_assignment_histories ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE IF EXISTS delivery_assignment_histories ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE IF EXISTS delivery_assignment_histories ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);
ALTER TABLE IF EXISTS delivery_assignment_histories ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT false;
ALTER TABLE IF EXISTS delivery_assignment_histories ADD COLUMN IF NOT EXISTS trace_id VARCHAR(255);
ALTER TABLE IF EXISTS delivery_assignment_histories ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;
ALTER TABLE IF EXISTS delivery_assignment_histories ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- 4. Product Variants & Stock Ledgers
ALTER TABLE IF EXISTS product_variants ADD COLUMN IF NOT EXISTS attribute_signature VARCHAR(500);
ALTER TABLE IF EXISTS stock_ledgers ADD COLUMN IF NOT EXISTS product_variant_id BIGINT;

-- 5. Purchase Orders
ALTER TABLE IF EXISTS purchase_orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) DEFAULT 'UNPAID';
ALTER TABLE IF EXISTS purchase_orders ADD COLUMN IF NOT EXISTS advance_amount NUMERIC(18, 2) DEFAULT 0;

-- 6. Customers
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS dob DATE;
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS gender VARCHAR(10);
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS tax_code VARCHAR(50);
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS note TEXT;
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS group_id BIGINT;
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS area_id BIGINT;
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS debt_limit NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS password VARCHAR(255);
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN DEFAULT false;
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS avatar_url TEXT;
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS membership_rank VARCHAR(20);
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS points NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS total_spend NUMERIC(18, 2) DEFAULT 0;

-- 7. Debt Ledgers
ALTER TABLE IF EXISTS debt_ledgers ADD COLUMN IF NOT EXISTS entity_name VARCHAR(200);
ALTER TABLE IF EXISTS debt_ledgers ADD COLUMN IF NOT EXISTS entity_type VARCHAR(30);
ALTER TABLE IF EXISTS debt_ledgers ADD COLUMN IF NOT EXISTS due_date TIMESTAMP;
ALTER TABLE IF EXISTS debt_ledgers ADD COLUMN IF NOT EXISTS status VARCHAR(30);
ALTER TABLE IF EXISTS debt_ledgers ADD COLUMN IF NOT EXISTS last_payment_date TIMESTAMP;
ALTER TABLE IF EXISTS debt_ledgers ADD COLUMN IF NOT EXISTS account_manager VARCHAR(100);

-- 8. Shipping Carriers
ALTER TABLE IF EXISTS shipping_carriers ADD COLUMN IF NOT EXISTS email VARCHAR(150);
ALTER TABLE IF EXISTS shipping_carriers ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
ALTER TABLE IF EXISTS shipping_carriers ADD COLUMN IF NOT EXISTS website VARCHAR(255);
ALTER TABLE IF EXISTS shipping_carriers ADD COLUMN IF NOT EXISTS address VARCHAR(255);
ALTER TABLE IF EXISTS shipping_carriers ADD COLUMN IF NOT EXISTS contact_person VARCHAR(150);
ALTER TABLE IF EXISTS shipping_carriers ADD COLUMN IF NOT EXISTS notes TEXT;

-- 9. Users
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS avatar TEXT;

-- 10. Finance & Bank Accounts
ALTER TABLE IF EXISTS bank_accounts ADD COLUMN IF NOT EXISTS swift_bic VARCHAR(50);
ALTER TABLE IF EXISTS bank_accounts ADD COLUMN IF NOT EXISTS currency VARCHAR(10) DEFAULT 'VND';
ALTER TABLE IF EXISTS bank_accounts ADD COLUMN IF NOT EXISTS current_balance NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS bank_accounts ADD COLUMN IF NOT EXISTS available_working_capital NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS bank_accounts ADD COLUMN IF NOT EXISTS account_type VARCHAR(50) DEFAULT 'PRIMARY_OPERATING';
ALTER TABLE IF EXISTS bank_accounts ADD COLUMN IF NOT EXISTS opened_date VARCHAR(20);
ALTER TABLE IF EXISTS bank_accounts ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'ACTIVE';
ALTER TABLE IF EXISTS bank_accounts ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

ALTER TABLE IF EXISTS fund_balances ADD COLUMN IF NOT EXISTS branch_name VARCHAR(150);
ALTER TABLE IF EXISTS fund_balances ADD COLUMN IF NOT EXISTS manager_name VARCHAR(100);

ALTER TABLE IF EXISTS transaction_reasons ADD COLUMN IF NOT EXISTS accounting_code VARCHAR(30);
ALTER TABLE IF EXISTS transaction_reasons ADD COLUMN IF NOT EXISTS description VARCHAR(255);

ALTER TABLE IF EXISTS fixed_assets ADD COLUMN IF NOT EXISTS category VARCHAR(100);
ALTER TABLE IF EXISTS fixed_assets ADD COLUMN IF NOT EXISTS accumulated_depreciation NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS fixed_assets ADD COLUMN IF NOT EXISTS useful_life_months INTEGER DEFAULT 36;
ALTER TABLE IF EXISTS fixed_assets ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'ACTIVE';

-- 11. Banners Column Alignment
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS sort_order INTEGER DEFAULT 0;
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS image_url TEXT;
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS link_url VARCHAR(255);
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS valid_from TIMESTAMP;
ALTER TABLE IF EXISTS banners ADD COLUMN IF NOT EXISTS valid_until TIMESTAMP;

-- 12. Products
ALTER TABLE IF EXISTS products ADD COLUMN IF NOT EXISTS is_serial_tracked BOOLEAN DEFAULT false;
ALTER TABLE IF EXISTS products ADD COLUMN IF NOT EXISTS warranty_period_months INTEGER;
ALTER TABLE IF EXISTS products ADD COLUMN IF NOT EXISTS origin_country VARCHAR(100);

-- 13. POS Sessions
CREATE TABLE IF NOT EXISTS pos_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_code VARCHAR(50),
    terminal_code VARCHAR(50),
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    opening_cash NUMERIC(18, 2) DEFAULT 0,
    expected_closing_cash NUMERIC(18, 2) DEFAULT 0,
    actual_closing_cash NUMERIC(18, 2) DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    user_id BIGINT,
    branch_id BIGINT,
    note TEXT,
    is_deleted BOOLEAN DEFAULT false,
    is_locked BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    tenant_id BIGINT,
    trace_id VARCHAR(255),
    version INTEGER DEFAULT 0
);
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS session_code VARCHAR(50);
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS terminal_code VARCHAR(50);
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS end_time TIMESTAMP;
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS opening_cash NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS expected_closing_cash NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS actual_closing_cash NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'OPEN';
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS branch_id BIGINT;
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS note TEXT;
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT false;
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT false;
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS trace_id VARCHAR(255);
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;
ALTER TABLE IF EXISTS pos_sessions ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- ====================================================================
-- 10. Product Batches — Data Consistency Cleanup
-- ====================================================================
-- Auto-mark expired batches: if expiry_date < NOW and quality_status is still 'PASSED_QA', flag as EXPIRED
UPDATE product_batches
  SET quality_status = 'EXPIRED',
      status = 'EXPIRED'
  WHERE expiry_date IS NOT NULL
    AND expiry_date < CURRENT_DATE
    AND (quality_status IS NULL OR quality_status = 'PASSED_QA')
    AND (is_deleted IS NULL OR is_deleted = false);

-- Remove duplicate batch entries: keep only the first row per (batch_number, product_id)
DELETE FROM product_batches
  WHERE id NOT IN (
    SELECT MIN(id)
    FROM product_batches
    WHERE (is_deleted IS NULL OR is_deleted = false)
    GROUP BY batch_number, product_id
  )
  AND (is_deleted IS NULL OR is_deleted = false);

-- ====================================================================
-- 11. Payment Methods & Branch Assignment (N-N with Branches)
-- ====================================================================
CREATE TABLE IF NOT EXISTS payment_methods (
    id BIGSERIAL PRIMARY KEY,
    method_code VARCHAR(50) NOT NULL UNIQUE,
    method_name VARCHAR(100) NOT NULL,
    type VARCHAR(30),
    provider_type VARCHAR(50),
    fee_type VARCHAR(20) DEFAULT 'PERCENT',
    fee_value NUMERIC(18, 2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    sort_order INTEGER DEFAULT 0,
    processing_fee_pct NUMERIC(5, 2) DEFAULT 0,
    fixed_fee_usd NUMERIC(18, 2) DEFAULT 0,
    settlement_time VARCHAR(50) DEFAULT 'INSTANT',
    total_volume_usd NUMERIC(18, 2) DEFAULT 0,
    configured_gateways VARCHAR(250),
    logo_url VARCHAR(500),
    bank_name VARCHAR(150),
    bank_account VARCHAR(50),
    bank_account_name VARCHAR(150),
    transfer_syntax VARCHAR(150) DEFAULT 'POS {order_code}',
    merchant_id VARCHAR(100),
    api_key VARCHAR(250),
    secret_key VARCHAR(250),
    checksum_key VARCHAR(250),
    allow_pos BOOLEAN DEFAULT TRUE,
    allow_online BOOLEAN DEFAULT FALSE,
    apply_to_all_branches BOOLEAN DEFAULT TRUE,
    currency VARCHAR(10) DEFAULT 'VND',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    trace_id VARCHAR(255),
    version INTEGER DEFAULT 0,
    tenant_id BIGINT
);

ALTER TABLE IF EXISTS payment_methods ADD COLUMN IF NOT EXISTS apply_to_all_branches BOOLEAN DEFAULT TRUE;
ALTER TABLE IF EXISTS payment_methods ADD COLUMN IF NOT EXISTS provider_type VARCHAR(50);
ALTER TABLE IF EXISTS payment_methods ADD COLUMN IF NOT EXISTS allow_pos BOOLEAN DEFAULT TRUE;
ALTER TABLE IF EXISTS payment_methods ADD COLUMN IF NOT EXISTS allow_online BOOLEAN DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS payment_method_branches (
    id BIGSERIAL PRIMARY KEY,
    payment_method_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    trace_id VARCHAR(255),
    version INTEGER DEFAULT 0,
    tenant_id BIGINT,
    CONSTRAINT uq_payment_method_branch UNIQUE (payment_method_id, branch_id)
);

-- Fix data mismatch: Set MoMo, ZaloPay, VNPay to E_WALLET / QR_EWALLET instead of CASH
UPDATE payment_methods
  SET type = 'E_WALLET',
      provider_type = 'E_WALLET'
  WHERE UPPER(method_code) IN ('MOMO', 'VNPAY', 'ZALOPAY', 'SHOPEEPAY')
    OR UPPER(method_name) LIKE '%MOMO%'
    OR UPPER(method_name) LIKE '%VÍ ĐIỆN TỬ%'
    OR UPPER(method_name) LIKE '%VI DIEN TU%';

-- ====================================================================
-- 12. Add Missing Entity Columns
-- ====================================================================
ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS voucher_code VARCHAR(50);
ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS voucher_discount_amount NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS loyalty_points_used INTEGER DEFAULT 0;
ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS loyalty_points_earned INTEGER DEFAULT 0;

ALTER TABLE IF EXISTS purchase_orders ADD COLUMN IF NOT EXISTS vat_amount NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS purchase_orders ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS purchase_orders ADD COLUMN IF NOT EXISTS vat_rate NUMERIC(5, 2) DEFAULT 0;
ALTER TABLE IF EXISTS purchase_orders ADD COLUMN IF NOT EXISTS shipping_fee NUMERIC(18, 2) DEFAULT 0;

ALTER TABLE IF EXISTS export_invoices ADD COLUMN IF NOT EXISTS due_date TIMESTAMP;
ALTER TABLE IF EXISTS export_invoices ADD COLUMN IF NOT EXISTS einvoice_ref VARCHAR(100);
ALTER TABLE IF EXISTS export_invoices ADD COLUMN IF NOT EXISTS company_name VARCHAR(255);
ALTER TABLE IF EXISTS export_invoices ADD COLUMN IF NOT EXISTS tax_id VARCHAR(50);
ALTER TABLE IF EXISTS export_invoices ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(50);

-- 13. Missing entity columns synchronization
ALTER TABLE IF EXISTS customers ADD COLUMN IF NOT EXISTS is_credit_blocked BOOLEAN DEFAULT false;

ALTER TABLE IF EXISTS products ADD COLUMN IF NOT EXISTS allow_negative_stock BOOLEAN DEFAULT false;
ALTER TABLE IF EXISTS products ADD COLUMN IF NOT EXISTS dimensions VARCHAR(100);

ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS pos_session_id BIGINT;

ALTER TABLE IF EXISTS purchase_orders ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(50);

ALTER TABLE IF EXISTS journal_entries ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'POSTED';

ALTER TABLE IF EXISTS debt_ledgers ADD COLUMN IF NOT EXISTS notes TEXT;

ALTER TABLE IF EXISTS operating_costs ADD COLUMN IF NOT EXISTS cost_code VARCHAR(50);
ALTER TABLE IF EXISTS operating_costs ADD COLUMN IF NOT EXISTS category VARCHAR(100);

ALTER TABLE IF EXISTS payrolls ADD COLUMN IF NOT EXISTS payroll_code VARCHAR(50);
ALTER TABLE IF EXISTS payrolls ADD COLUMN IF NOT EXISTS kpi_bonus NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS payrolls ADD COLUMN IF NOT EXISTS payment_date TIMESTAMP;

ALTER TABLE IF EXISTS receipt_vouchers ADD COLUMN IF NOT EXISTS category VARCHAR(100);

ALTER TABLE IF EXISTS employee_contracts ADD COLUMN IF NOT EXISTS base_salary NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS employee_contracts ADD COLUMN IF NOT EXISTS notes TEXT;

ALTER TABLE IF EXISTS kpi_records ADD COLUMN IF NOT EXISTS department_name VARCHAR(100);
ALTER TABLE IF EXISTS kpi_records ADD COLUMN IF NOT EXISTS rating_grade VARCHAR(50);
ALTER TABLE IF EXISTS kpi_records ADD COLUMN IF NOT EXISTS bonus_amount NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS kpi_records ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'PENDING';

ALTER TABLE IF EXISTS leave_requests ADD COLUMN IF NOT EXISTS request_code VARCHAR(50);
ALTER TABLE IF EXISTS leave_requests ADD COLUMN IF NOT EXISTS approver_name VARCHAR(100);
ALTER TABLE IF EXISTS leave_requests ADD COLUMN IF NOT EXISTS approved_by_user_id BIGINT;

ALTER TABLE IF EXISTS channel_product_mappings ADD COLUMN IF NOT EXISTS internal_sku VARCHAR(100);
ALTER TABLE IF EXISTS channel_product_mappings ADD COLUMN IF NOT EXISTS product_name VARCHAR(255);
ALTER TABLE IF EXISTS channel_product_mappings ADD COLUMN IF NOT EXISTS channel_name VARCHAR(100);
ALTER TABLE IF EXISTS channel_product_mappings ADD COLUMN IF NOT EXISTS channel_price NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS channel_product_mappings ADD COLUMN IF NOT EXISTS last_synced_at TIMESTAMP;

ALTER TABLE IF EXISTS sales_channels ADD COLUMN IF NOT EXISTS shop_id VARCHAR(100);
ALTER TABLE IF EXISTS sales_channels ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'ACTIVE';
ALTER TABLE IF EXISTS sales_channels ADD COLUMN IF NOT EXISTS last_synced_at TIMESTAMP;
ALTER TABLE IF EXISTS sales_channels ADD COLUMN IF NOT EXISTS product_count INTEGER DEFAULT 0;

ALTER TABLE IF EXISTS system_configs ADD COLUMN IF NOT EXISTS category VARCHAR(50);
ALTER TABLE IF EXISTS system_configs ADD COLUMN IF NOT EXISTS data_type VARCHAR(30);
ALTER TABLE IF EXISTS system_configs ADD COLUMN IF NOT EXISTS is_encrypted BOOLEAN DEFAULT false;
ALTER TABLE IF EXISTS system_configs ADD COLUMN IF NOT EXISTS requires_reboot BOOLEAN DEFAULT false;
ALTER TABLE IF EXISTS system_configs ADD COLUMN IF NOT EXISTS updated_by_role VARCHAR(50);

-- ====================================================================
-- 14. Missing Tables (SaleReturnRequest, ShiftSwapRequest, SupplierStorage, etc.)
-- ====================================================================
CREATE TABLE IF NOT EXISTS sale_return_requests (
    id BIGSERIAL PRIMARY KEY,
    request_code VARCHAR(50) NOT NULL,
    order_code VARCHAR(50),
    customer_id BIGINT,
    customer_name VARCHAR(150),
    customer_phone VARCHAR(30),
    requested_qty INTEGER,
    returned_qty INTEGER,
    remaining_qty INTEGER,
    refund_amount NUMERIC(18, 2),
    refund_method VARCHAR(50),
    reason TEXT,
    request_date TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING',
    items_json TEXT,
    note TEXT,
    is_deleted BOOLEAN DEFAULT false,
    is_locked BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    tenant_id BIGINT,
    trace_id VARCHAR(255),
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS shift_swap_requests (
    id BIGSERIAL PRIMARY KEY,
    request_code VARCHAR(50) NOT NULL,
    requester_name VARCHAR(100) NOT NULL,
    requester_shift VARCHAR(100) NOT NULL,
    target_user_name VARCHAR(100) NOT NULL,
    target_user_shift VARCHAR(100) NOT NULL,
    swap_date DATE,
    reason TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    approved_by VARCHAR(100),
    notes TEXT,
    note TEXT,
    is_deleted BOOLEAN DEFAULT false,
    is_locked BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    tenant_id BIGINT,
    trace_id VARCHAR(255),
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS supplier_storages (
    id BIGSERIAL PRIMARY KEY,
    storage_code VARCHAR(50) NOT NULL,
    storage_name VARCHAR(200) NOT NULL,
    warehouse_name VARCHAR(200),
    storage_type VARCHAR(50),
    area_type VARCHAR(50),
    zone_type VARCHAR(50),
    putaway_rule VARCHAR(50),
    capacity NUMERIC(14, 2),
    capacity_pallets INTEGER,
    used_pallets INTEGER,
    current_usage NUMERIC(14, 2),
    capacity_unit VARCHAR(30),
    operating_hours VARCHAR(100),
    allow_import BOOLEAN DEFAULT true,
    allow_export BOOLEAN DEFAULT true,
    allow_transfer BOOLEAN DEFAULT true,
    status VARCHAR(30) DEFAULT 'TRONG',
    notes TEXT,
    note TEXT,
    is_deleted BOOLEAN DEFAULT false,
    is_locked BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    tenant_id BIGINT,
    trace_id VARCHAR(255),
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS supplier_warehouses (
    id BIGSERIAL PRIMARY KEY,
    warehouse_code VARCHAR(50) NOT NULL,
    warehouse_name VARCHAR(200) NOT NULL,
    supplier_name VARCHAR(200),
    address TEXT,
    warehouse_type VARCHAR(50),
    capacity NUMERIC(14, 2),
    capacity_unit VARCHAR(30),
    manager_name VARCHAR(150),
    manager_phone VARCHAR(50),
    manager_email VARCHAR(150),
    contact_person VARCHAR(150),
    phone VARCHAR(50),
    loading_contact_phone VARCHAR(50),
    operating_hours VARCHAR(100),
    operating_days VARCHAR(100),
    storage_conditions VARCHAR(255),
    status VARCHAR(30) DEFAULT 'HOAT_DONG',
    notes TEXT,
    internal_notes TEXT,
    note TEXT,
    is_deleted BOOLEAN DEFAULT false,
    is_locked BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    tenant_id BIGINT,
    trace_id VARCHAR(255),
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS purchase_invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_code VARCHAR(50) NOT NULL UNIQUE,
    po_code VARCHAR(50),
    po_id BIGINT,
    supplier_id BIGINT NOT NULL,
    branch_id BIGINT,
    invoice_date TIMESTAMP NOT NULL,
    due_date TIMESTAMP,
    sub_total NUMERIC(18, 2),
    vat_amount NUMERIC(18, 2),
    discount_amount NUMERIC(18, 2),
    total_amount NUMERIC(18, 2),
    status VARCHAR(50),
    payment_terms VARCHAR(50),
    note TEXT,
    is_deleted BOOLEAN DEFAULT false,
    is_locked BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    tenant_id BIGINT,
    trace_id VARCHAR(255),
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS supplier_requests (
    id BIGSERIAL PRIMARY KEY,
    rfq_code VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(500),
    selected_suppliers TEXT,
    destination_branch VARCHAR(200),
    branch_id BIGINT,
    sent_date DATE,
    expiry_date DATE,
    handler VARCHAR(150),
    status VARCHAR(50) DEFAULT 'CHO_BAO_GIA',
    notes TEXT,
    note TEXT,
    is_deleted BOOLEAN DEFAULT false,
    is_locked BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    tenant_id BIGINT,
    trace_id VARCHAR(255),
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS supplier_request_details (
    id BIGSERIAL PRIMARY KEY,
    supplier_request_id BIGINT NOT NULL,
    sku VARCHAR(100),
    product_name VARCHAR(200) NOT NULL,
    quantity NUMERIC(14, 2) NOT NULL,
    unit VARCHAR(50),
    specifications TEXT,
    note TEXT,
    is_deleted BOOLEAN DEFAULT false,
    is_locked BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    tenant_id BIGINT,
    trace_id VARCHAR(255),
    version INTEGER DEFAULT 0
);

-- 55. Product & Tax Alignment
ALTER TABLE IF EXISTS products ADD COLUMN IF NOT EXISTS tax_class VARCHAR(20);
ALTER TABLE IF EXISTS export_invoice_details ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS export_invoice_details ADD COLUMN IF NOT EXISTS total_amount NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(18, 2) DEFAULT 0;
ALTER TABLE IF EXISTS sale_orders ADD COLUMN IF NOT EXISTS sub_total NUMERIC(18, 2) DEFAULT 0;
