package com.belat.fineract.portfolio.investmentproject.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.fineract.portfolio.client.data.ClientData;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class InvestmentProjectData {

    private String name;
    private ClientData owner;
    private BigDecimal amount;
    private String currencyCode;
    private String description;
    private BigDecimal rate;

}
