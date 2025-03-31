package com.belat.fineract.portfolio.investmentproject.api;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InvestmentProjectConstants {

    private InvestmentProjectConstants() {}

    public static final String projectNameParamName = "name";
    public static final String projectOwnerIdParamName = "ownerId";
    public static final String amountParamName = "amount";
    public static final String currencyCodeParamName = "currencyCode";
    public static final String descriptionParamName = "description";
    public static final String projectRateParamName = "projectRate";


    /**
     * These parameters will match the class level parameters of {@link InvestmentProjectData}. Where possible, we try to
     * get response parameters to match those of request parameters.
     */
    public static final Set<String> INVESTMENT_PROJECT_PARAMETERS = new HashSet<>(Arrays.asList(projectNameParamName,
            projectOwnerIdParamName, amountParamName, currencyCodeParamName, descriptionParamName, projectRateParamName));

    /**
     * These parameters will match the class level parameters of {@link InvestmentProjectData}. Where possible, we try to
     * get response parameters to match those of request parameters.
     */
    public static final Set<String> INVESTMENT_PROJECT_PARAMETERS_FOR_UPDATE = new HashSet<>(Arrays.asList(projectNameParamName,
            descriptionParamName, projectRateParamName));

}
