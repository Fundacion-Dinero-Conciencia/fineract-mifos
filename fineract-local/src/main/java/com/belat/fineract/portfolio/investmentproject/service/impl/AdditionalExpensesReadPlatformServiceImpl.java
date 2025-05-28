package com.belat.fineract.portfolio.investmentproject.service.impl;

import com.belat.fineract.portfolio.investmentproject.data.AdditionalExpensesData;
import com.belat.fineract.portfolio.investmentproject.domain.commission.AdditionalExpenses;
import com.belat.fineract.portfolio.investmentproject.domain.commission.AdditionalExpensesRepository;
import com.belat.fineract.portfolio.investmentproject.exception.AdditionalExpensesNotFoundException;
import com.belat.fineract.portfolio.investmentproject.mapper.AdditionalExpensesMapper;
import com.belat.fineract.portfolio.investmentproject.service.AdditionalExpensesReadPlatformService;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdditionalExpensesReadPlatformServiceImpl implements AdditionalExpensesReadPlatformService {

    private final AdditionalExpensesRepository additionalExpensesRepository;
    private final AdditionalExpensesMapper additionalExpensesMapper;


    @Override
    public List<AdditionalExpensesData> getAdditionalExpenses(Long projectId) {
        return additionalExpensesMapper.map(additionalExpensesRepository.findByInvestmentProjectId(projectId));
    }

    @Override
    public AdditionalExpensesData getAdditionalExpensesDataById(Long id) {
        return additionalExpensesMapper.map(additionalExpensesRepository.findById(id).orElse(null));
    }

    @Override
    public AdditionalExpenses getAdditionalExpensesById(Long id) {
        return additionalExpensesRepository.findById(id).orElseThrow( () -> new AdditionalExpensesNotFoundException(id));
    }

    @Override
    public BigDecimal getTir(List<BigDecimal> periods) {
        return MathUtil.calculateAnnualIRR(periods.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .toArray());
    }
}
