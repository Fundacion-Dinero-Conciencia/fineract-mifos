package org.apache.fineract.portfolio.investmentproject.service;

import org.apache.fineract.portfolio.investmentproject.domain.InvestmentProjectAddress;

public interface InvestmentProjectAddressReadPlatformService {

    InvestmentProjectAddress getByInvestmentProjectId(Long id);
}
