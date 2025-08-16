package com.belat.fineract.portfolio.investmentproject.mapper;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectCategoryData;
import com.belat.fineract.portfolio.investmentproject.domain.category.InvestmentProjectCategory;
import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapstructMapperConfig.class, uses = {CodeValueMapper.class})
public interface InvestmentProjectCategoryMapper {

    @Mapping(target = "investmentProject", ignore = true)
    InvestmentProjectCategoryData map(InvestmentProjectCategory source);

    List<InvestmentProjectCategoryData> map(List<InvestmentProjectCategory> sources);
}
