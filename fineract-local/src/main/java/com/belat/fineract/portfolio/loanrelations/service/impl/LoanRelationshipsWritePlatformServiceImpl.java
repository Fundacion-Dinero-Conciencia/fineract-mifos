package com.belat.fineract.portfolio.loanrelations.service.impl;

import com.belat.fineract.portfolio.loanrelations.data.LoanRelationshipsData;
import com.belat.fineract.portfolio.loanrelations.domain.LoanRelationships;
import com.belat.fineract.portfolio.loanrelations.domain.LoanRelationshipsRepository;
import com.belat.fineract.portfolio.loanrelations.mapper.LoanRelationshipsMapper;
import com.belat.fineract.portfolio.loanrelations.service.LoanRelationshipsReadPlatformService;
import com.belat.fineract.portfolio.loanrelations.service.LoanRelationshipsWritePlatformService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class LoanRelationshipsWritePlatformServiceImpl implements LoanRelationshipsWritePlatformService {

    private final LoanRelationshipsRepository loanRelationshipsRepository;
    private final LoanRelationshipsMapper loanRelationshipsMapper;
    private final LoanRelationshipsReadPlatformService loanRelationshipsReadPlatformService;

    @Override
    public LoanRelationshipsData createLoanRelationships(Long simulationId, Long subLoanId) {
        boolean containsCommission = loanRelationshipsReadPlatformService.getLoanRelationshipsDataByLoanSimulationId(simulationId) == null;

        LoanRelationships loanRelationships = new LoanRelationships();
        loanRelationships.setLoanSimulationId(simulationId);
        loanRelationships.setSubLoanId(subLoanId);
        loanRelationships.setContainsCommission(containsCommission);

        loanRelationships = loanRelationshipsRepository.save(loanRelationships);
        return loanRelationshipsMapper.map(loanRelationships);
    }
}
