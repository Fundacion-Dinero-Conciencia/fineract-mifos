package com.belat.fineract.portfolio.investmentproject.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdditionalExpensesData {

    private Long id;

    private Long projectId;

    private String name;

    private BigDecimal netAmount;

    private BigDecimal vat;

    private BigDecimal getTotal() {
        return this.netAmount.multiply(this.vat);
    }
}
