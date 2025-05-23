package com.belat.fineract.portfolio.investmentproject.service;

import com.belat.fineract.portfolio.investmentproject.data.AdditionalExpensesData;
import com.belat.fineract.portfolio.investmentproject.domain.commission.AdditionalExpenses;

import java.util.List;

public interface AdditionalExpensesReadPlatformService {

    List<AdditionalExpensesData> getAdditionalExpenses(Long projectId);

    AdditionalExpensesData getAdditionalExpensesDataById(Long id);

    AdditionalExpenses getAdditionalExpensesById(Long id);

}
