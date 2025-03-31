package com.belat.fineract.portfolio.investmentproject.service.impl;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProjectRepository;
import com.belat.fineract.portfolio.investmentproject.mapper.InvestmentProjectMapper;
import com.belat.fineract.portfolio.investmentproject.service.InvestmentProjectReadPlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        List<InvestmentProjectData> projectsData = new ArrayList<>();
        for (InvestmentProject project : projects) {
            InvestmentProjectData projectData = investmentProjectMapper.map(project);
            projectData.setOwnerId(project.getOwner().getId());
            projectsData.add(projectData);
        }
        return projectsData;
    }

    @Override
    public List<InvestmentProject> retrieveAllInvestmentProject() {
        return investmentProjectRepository.findAll();
    }

    @Override
    public InvestmentProjectData retrieveById(Long id) {
        InvestmentProject project = investmentProjectRepository.retrieveOneByProjectId(id);
        InvestmentProjectData projectData = investmentProjectMapper.map(project);
        if (project != null) {
            projectData.setOwnerId(project.getOwner().getId());
        }
        return projectData;
    }

    @Override
    public List<InvestmentProjectData> retrieveByClientId(Long clientId) {
        List<InvestmentProject> projects = investmentProjectRepository.retrieveByClientId(clientId);
        List<InvestmentProjectData> projectsData = new ArrayList<>();
        for (InvestmentProject project : projects) {
            if (project != null) {
                InvestmentProjectData projectData = investmentProjectMapper.map(project);
                projectData.setOwnerId(project.getOwner().getId());
                projectsData.add(projectData);
            }
        }
        return projectsData;
    }

}
