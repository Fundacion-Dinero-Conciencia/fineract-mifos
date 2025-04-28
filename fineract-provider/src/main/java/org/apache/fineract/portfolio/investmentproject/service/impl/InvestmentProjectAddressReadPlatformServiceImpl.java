package org.apache.fineract.portfolio.investmentproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.investmentproject.domain.InvestmentProjectAddress;
import org.apache.fineract.portfolio.investmentproject.domain.InvestmentProjectAddressRepository;
import org.apache.fineract.portfolio.investmentproject.service.InvestmentProjectAddressReadPlatformService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestmentProjectAddressReadPlatformServiceImpl implements InvestmentProjectAddressReadPlatformService {

    private final InvestmentProjectAddressRepository investmentProjectAddressRepository;

    @Override
    public InvestmentProjectAddress getByInvestmentProjectId(Long id) {
        return investmentProjectAddressRepository.getByInvestmentProject_id(id);
    }
}
