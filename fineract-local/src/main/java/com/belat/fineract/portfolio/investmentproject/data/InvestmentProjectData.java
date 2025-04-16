package com.belat.fineract.portfolio.investmentproject.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class InvestmentProjectData {

    private Long id;
    private String name;
    private String subtitle;
    private Long ownerId;
    private BigDecimal amount;
    private String currencyCode;
    private String impactDescription;
    private String institutionDescription;
    private String teamDescription;
    private String financingDescription;
    private Boolean isActive;
    private Integer period;
    private BigDecimal rate;
    private BigDecimal occupancyPercentage;
    private DataCode country;
    private List<ImageDocument> images;
    private List<DataCode> subCategories;
    private Long loanId;
    private BigDecimal maxAmount;
    private DataCode category;
    private DataCode area;

    @Data
    @AllArgsConstructor
    public static class ImageDocument {
        private String name;
        private String url;
    }

    @Data
    @AllArgsConstructor
    public static class DataCode {
        private Long id;
        private String code;
        private String description;
    }

}
