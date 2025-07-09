package com.belat.fineract.portfolio.investmentproject.api.commissions;

import com.belat.fineract.portfolio.investmentproject.data.AdditionalExpensesData;
import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AdditionalExpensesConstants {

    private AdditionalExpensesConstants() {}

    public static final String projectIdParamName = "projectId";
    public static final String descriptionParamName = "description";
    public static final String netAmountParamName = "netAmount";
    public static final String vatParamName = "vat";
    public static final String expenseIdParamName = "id";
    public static final String commissionTypeParamName = "commissionTypeId";
    public static final String totalAmountParamName = "total";
    public static final String flagAllDeleteParamName = "flagAllDelete";

    public static final String AEF_COMMISSION = "AEF";
    public static final String IVA_AEF_COMMISSION = "IVA-AEF";
    public static final String CONFIG_COMMISSION_TAXES_CODE_NAME = "CONFIG_COMMISSION_TAXES";
    public static final String UPDATE_ADDITIONAL_EXPENSES = "ADDITIONAL_EXPENSES";

    /**
     * These parameters will match the class level parameters of {@link AdditionalExpensesData}. Where possible, we try
     * to get response parameters to match those of request parameters.
     */
    public static final Set<String> ADDITIONAL_EXPENSES_DATA_PARAMETERS = new HashSet<>(Arrays.asList(projectIdParamName, commissionTypeParamName, descriptionParamName, netAmountParamName, vatParamName, totalAmountParamName, expenseIdParamName));

    /**
     * These parameters will match the class level parameters of {@link AdditionalExpensesData}. Where possible, we try
     * to get response parameters to match those of request parameters.
     */
    public static final Set<String> ADDITIONAL_EXPENSES_PARAMETERS_FOR_UPDATE = new HashSet<>(Arrays.asList(expenseIdParamName, commissionTypeParamName, descriptionParamName, netAmountParamName, vatParamName));

}
