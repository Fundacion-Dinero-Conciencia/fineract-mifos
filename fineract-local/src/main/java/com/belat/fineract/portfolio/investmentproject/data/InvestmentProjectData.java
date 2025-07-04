package com.belat.fineract.portfolio.investmentproject.data;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

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
    private String logo;
    private List<DataCode> subCategories;
    private List<DataCode> objectives;
    private Long loanId;
    private BigDecimal availableTotalAmount;
    private DataCode category;
    private DataCode area;
    private String ownerName;
    private BigDecimal maxAmount;
    private BigDecimal minAmount;
    private StatusHistoryProjectData status;
    private String mnemonic;
    private Integer position;
    private String littleSocioEnvironmentalDescription;
    private String detailedSocioEnvironmentalDescription;

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
