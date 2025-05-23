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
import org.springframework.stereotype.Service;

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
}
