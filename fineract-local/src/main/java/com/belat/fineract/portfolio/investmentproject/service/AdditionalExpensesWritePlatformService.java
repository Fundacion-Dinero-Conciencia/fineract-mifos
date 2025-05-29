package com.belat.fineract.portfolio.investmentproject.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface AdditionalExpensesWritePlatformService {

    CommandProcessingResult addAdditionalExpenses(JsonCommand jsonCommand);

    CommandProcessingResult updateAdditionalExpenses(JsonCommand jsonCommand);

    CommandProcessingResult deleteAdditionalExpensesById(JsonCommand jsonCommand);

}
