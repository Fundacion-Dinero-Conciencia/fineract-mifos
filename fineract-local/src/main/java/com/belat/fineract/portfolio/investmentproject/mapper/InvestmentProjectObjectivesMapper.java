package com.belat.fineract.portfolio.investmentproject.mapper;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectObjectiveData;
import com.belat.fineract.portfolio.investmentproject.domain.objective.InvestmentProjectObjective;
import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapstructMapperConfig.class, uses = {CodeValueMapper.class})
public interface InvestmentProjectObjectivesMapper {

    @Mapping(target = "investmentProject", ignore = true)
    InvestmentProjectObjectiveData map(InvestmentProjectObjective source);

    List<InvestmentProjectObjectiveData> map(List<InvestmentProjectObjective> sources);
}
