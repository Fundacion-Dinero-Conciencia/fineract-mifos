package com.belat.fineract.portfolio.investmentproject.mapper;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public abstract class InvestmentProjectMapperDecorator implements InvestmentProjectMapper {

    @Autowired
    private InvestmentProjectMapper delegate;

    @Autowired
    private StatusHistoryProjectMapper statusMapper;

    @Autowired
    private StatusHistoryProjectRepository statusRepository;

    @Override
    public InvestmentProjectData map(InvestmentProject source) {
        InvestmentProjectData data = delegate.map(source);
        data.setStatus(statusMapper.map(statusRepository.getLastStatusByInvestmentProjectId(source.getId())));
        return data;
    }

    @Override
    public List<InvestmentProjectData> map(List<InvestmentProject> sources) {
        return sources.stream().map(this::map).collect(Collectors.toList());
    }
}
