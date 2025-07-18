package com.belat.fineract.portfolio.investmentproject.api;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InvestmentProjectConstants {

    private InvestmentProjectConstants() {}

    public static final String SHORT_NAME_FACTORING = "FACT";
    public static final String projectNameParamName = "name";
    public static final String subtitleParamName = "subtitle";
    public static final String projectOwnerIdParamName = "ownerId";
    public static final String amountParamName = "amount";
    public static final String currencyCodeParamName = "currencyCode";
    public static final String projectRateParamName = "projectRate";
    public static final String periodParamName = "period";
    public static final String loanTermFrequencyType = "loanTermFrequencyType";
    public static final String countryIdParamName = "countryId";
    public static final String isActiveParamName = "isActive";
    public static final String categoryParamName = "categoryId";
    public static final String subCategoriesParamName = "subCategories";
    public static final String objectivesParamName = "objectives";
    public static final String areaParamName = "areaId";
    public static final String loanIdParamName = "loanId";
    public static final String statusIdParamName = "statusId";
    public static final String maxAmountParamName = "maxAmount";
    public static final String minAmountParamName = "minAmount";
    public static final String mnemonicParamName = "mnemonic";
    public static final String positionParamName = "position";
    public static final String basedInLoanProductIdParamName = "basedInLoanProductId";
    public static final String amountToBeDeliveredParamName = "amountToBeDelivered";
    public static final String amountToBeFinancedParamName = "amountToBeFinanced";

    // Descriptions
    public static final String impactDescriptionParamName = "impactDescription";
    public static final String institutionDescriptionParamName = "institutionDescription";
    public static final String teamDescriptionParamName = "teamDescription";
    public static final String financingDescriptionParamName = "financingDescription";
    public static final String littleSocioEnvironmentalDescriptionParamName = "littleSocioEnvironmentalDescription";
    public static final String detailedSocioEnvironmentalDescriptionParamName = "detailedSocioEnvironmentalDescription";
    public static final String creditTypeIdParamName = "creditTypeId";

    /**
     * These parameters will match the class level parameters of {@link InvestmentProjectData}. Where possible, we try
     * to get response parameters to match those of request parameters.
     */
    public static final Set<String> INVESTMENT_PROJECT_PARAMETERS = new HashSet<>(Arrays.asList(projectNameParamName, subtitleParamName,
            projectOwnerIdParamName, amountParamName, currencyCodeParamName, projectRateParamName, periodParamName, countryIdParamName,
            impactDescriptionParamName, institutionDescriptionParamName, teamDescriptionParamName, financingDescriptionParamName,
            isActiveParamName, categoryParamName, subCategoriesParamName, areaParamName, objectivesParamName, maxAmountParamName,
            minAmountParamName, statusIdParamName, mnemonicParamName, littleSocioEnvironmentalDescriptionParamName, detailedSocioEnvironmentalDescriptionParamName,
            positionParamName, basedInLoanProductIdParamName, amountToBeDeliveredParamName, amountToBeFinancedParamName, loanTermFrequencyType, creditTypeIdParamName));

    /**
     * These parameters will match the class level parameters of {@link InvestmentProjectData}. Where possible, we try
     * to get response parameters to match those of request parameters.
     */
    public static final Set<String> INVESTMENT_PROJECT_PARAMETERS_FOR_UPDATE = new HashSet<>(Arrays.asList(projectNameParamName,
            subtitleParamName, projectRateParamName, impactDescriptionParamName, institutionDescriptionParamName, teamDescriptionParamName,
            financingDescriptionParamName, isActiveParamName, categoryParamName, subCategoriesParamName, areaParamName, objectivesParamName,
            maxAmountParamName, minAmountParamName, statusIdParamName, mnemonicParamName, littleSocioEnvironmentalDescriptionParamName,
            detailedSocioEnvironmentalDescriptionParamName, positionParamName, amountToBeDeliveredParamName, amountToBeFinancedParamName,
            loanTermFrequencyType, amountParamName, periodParamName, projectRateParamName, creditTypeIdParamName));

}
