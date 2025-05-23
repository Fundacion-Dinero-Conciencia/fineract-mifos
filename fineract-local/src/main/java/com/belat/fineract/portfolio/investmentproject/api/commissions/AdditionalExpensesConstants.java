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
    public static final String nameParamName = "name";
    public static final String netAmountParamName = "netAmount";
    public static final String vatParamName = "vat";
    public static final String expenseIdParamName = "expenseId";

    /**
     * These parameters will match the class level parameters of {@link AdditionalExpensesData}. Where possible, we try
     * to get response parameters to match those of request parameters.
     */
    public static final Set<String> ADDITIONAL_EXPENSES_DATA_PARAMETERS = new HashSet<>(Arrays.asList(projectIdParamName, nameParamName, netAmountParamName, vatParamName));

    /**
     * These parameters will match the class level parameters of {@link AdditionalExpensesData}. Where possible, we try
     * to get response parameters to match those of request parameters.
     */
    public static final Set<String> ADDITIONAL_EXPENSES_PARAMETERS_FOR_UPDATE = new HashSet<>(Arrays.asList(expenseIdParamName, nameParamName, netAmountParamName, vatParamName));

}
