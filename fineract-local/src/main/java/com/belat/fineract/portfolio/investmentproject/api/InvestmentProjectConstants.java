package com.belat.fineract.portfolio.investmentproject.api;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InvestmentProjectConstants {

    private InvestmentProjectConstants() {}

    public static final String projectNameParamName = "name";
    public static final String subtitleParamName = "subtitle";
    public static final String projectOwnerIdParamName = "ownerId";
    public static final String amountParamName = "amount";
    public static final String currencyCodeParamName = "currencyCode";
    public static final String projectRateParamName = "projectRate";
    public static final String periodParamName = "period";
    public static final String countryIdParamName = "countryId";
    public static final String isActiveParamName = "isActive";
    public static final String categoriesParamName = "categories";
    public static final String loanIdParamName = "loanId";

    //Descriptions
    public static final String impactDescriptionParamName = "impactDescription";
    public static final String institutionDescriptionParamName = "institutionDescription";
    public static final String teamDescriptionParamName = "teamDescription";
    public static final String financingDescriptionParamName = "financingDescription";


    /**
     * These parameters will match the class level parameters of {@link InvestmentProjectData}. Where possible, we try to
     * get response parameters to match those of request parameters.
     */
    public static final Set<String> INVESTMENT_PROJECT_PARAMETERS = new HashSet<>(Arrays.asList(projectNameParamName,
            subtitleParamName, projectOwnerIdParamName, amountParamName, currencyCodeParamName, projectRateParamName,
            periodParamName, countryIdParamName, impactDescriptionParamName, institutionDescriptionParamName,
            teamDescriptionParamName, financingDescriptionParamName, isActiveParamName, categoriesParamName,
            loanIdParamName));

    /**
     * These parameters will match the class level parameters of {@link InvestmentProjectData}. Where possible, we try to
     * get response parameters to match those of request parameters.
     */
    public static final Set<String> INVESTMENT_PROJECT_PARAMETERS_FOR_UPDATE = new HashSet<>(Arrays.asList(projectNameParamName,
            subtitleParamName, projectRateParamName, impactDescriptionParamName, institutionDescriptionParamName,
            teamDescriptionParamName, financingDescriptionParamName, isActiveParamName, categoriesParamName));

}
