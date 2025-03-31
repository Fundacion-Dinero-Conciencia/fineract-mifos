package com.belat.fineract.portfolio.investmentproject.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface InvestmentProjectWritePlatformService {

    CommandProcessingResult createInvestmentProject(JsonCommand command);

    CommandProcessingResult updateInvestmentProject(Long projectId, JsonCommand command);

}
