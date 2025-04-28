package org.apache.fineract.portfolio.investmentproject.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface InvestmentProjectAddressWritePlatformService {

    CommandProcessingResult createAddress(JsonCommand command);

}
