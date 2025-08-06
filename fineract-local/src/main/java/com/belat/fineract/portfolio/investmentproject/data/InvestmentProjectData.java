package com.belat.fineract.portfolio.investmentproject.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

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
    private CodeValueData country;
    private ImageDocument cover;
    private List<ImageDocument> images;
    private String logo;
    private List<InvestmentProjectCategoryData> subCategories;
    private List<InvestmentProjectObjectiveData> objectives;
    private Long loanId;
    private BigDecimal loanApprovedPrincipalAmount;
    private BigDecimal availableTotalAmount;
    private CodeValueData category;
    private CodeValueData area;
    private String ownerName;
    private BigDecimal maxAmount;
    private BigDecimal minAmount;
    private StatusHistoryProjectData status;
    private String mnemonic;
    private Integer position;
    private String littleSocioEnvironmentalDescription;
    private String detailedSocioEnvironmentalDescription;
    private CodeValueData creditType;

    @Data
    @AllArgsConstructor
    public static class ImageDocument {

        private String name;
        private String url;
        private String order;
    }
}
