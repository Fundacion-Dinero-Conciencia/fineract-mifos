package com.belat.fineract.portfolio.loanrelations.service.impl;

import com.belat.fineract.portfolio.loanrelations.data.LoanRelationshipsData;
import com.belat.fineract.portfolio.loanrelations.domain.LoanRelationshipsRepository;
import com.belat.fineract.portfolio.loanrelations.mapper.LoanRelationshipsMapper;
import com.belat.fineract.portfolio.loanrelations.service.LoanRelationshipsReadPlatformService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class LoanRelationshipsReadPlatformServiceImpl implements LoanRelationshipsReadPlatformService {

    private final LoanRelationshipsRepository loanRelationshipsRepository;
    private final LoanRelationshipsMapper loanRelationshipsMapper;

    @Override
    public List<LoanRelationshipsData> getLoanRelationshipsData() {
        return loanRelationshipsMapper.map(loanRelationshipsRepository.findAll());
    }

    @Override
    public LoanRelationshipsData getLoanRelationshipsDataBySubLoanId(Long loanId) {
        return loanRelationshipsMapper.map(loanRelationshipsRepository.getLoanRelationshipsBySubLoanId(loanId));
    }

    @Override
    public LoanRelationshipsData getLoanRelationshipsDataByLoanSimulationId(Long loanId) {
        return loanRelationshipsMapper.map(loanRelationshipsRepository.getLoanRelationshipsByLoanSimulationId(loanId));
    }
}
