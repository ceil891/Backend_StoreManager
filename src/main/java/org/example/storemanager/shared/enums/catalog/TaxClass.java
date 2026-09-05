package org.example.storemanager.shared.enums.catalog;

import java.math.BigDecimal;

public enum TaxClass {
    VAT_0(BigDecimal.ZERO),
    VAT_5(new BigDecimal("0.05")),
    VAT_8(new BigDecimal("0.08")),
    VAT_10(new BigDecimal("0.10")),
    EXEMPT(BigDecimal.ZERO);

    private final BigDecimal rate;

    TaxClass(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getRate() {
        return rate;
    }
}
