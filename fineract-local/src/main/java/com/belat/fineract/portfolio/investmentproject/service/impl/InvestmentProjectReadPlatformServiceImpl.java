package com.belat.fineract.portfolio.investmentproject.service.impl;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProjectRepository;
import com.belat.fineract.portfolio.investmentproject.mapper.InvestmentProjectMapper;
import com.belat.fineract.portfolio.investmentproject.service.InvestmentProjectReadPlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestmentProjectReadPlatformServiceImpl implements InvestmentProjectReadPlatformService {

    private final InvestmentProjectRepository investmentProjectRepository;
    private final InvestmentProjectMapper investmentProjectMapper;

    @Override
    public List<InvestmentProjectData> retrieveAll() {
        List<InvestmentProject> projects = investmentProjectRepository.findAll();
        return investmentProjectMapper.map(projects);
    }

    @Override
    public List<InvestmentProject> retrieveAllInvestmentProject() {
        return investmentProjectRepository.findAll();
    }

    @Override
    public InvestmentProjectData retrieveById(Long id) {
        InvestmentProject project = investmentProjectRepository.retrieveOneByProjectId(id);
        return investmentProjectMapper.map(project);
    }

    @Override
    public List<InvestmentProjectData> retrieveByClientId(Long clientId) {
        List<InvestmentProject> projects = investmentProjectRepository.retrieveByClientId(clientId);
        return investmentProjectMapper.map(projects);

    }

}
