package com.belat.fineract.portfolio.investmentproject.data;

import lombok.Data;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

import java.math.BigDecimal;

@Data
public class AdditionalExpensesData {

    private Long id;

    private Integer commissionTypeId;

    private Long projectId;

    private String description;

    private BigDecimal netAmount;

    private BigDecimal vat;

    private BigDecimal total;
}
