package com.belat.fineract.portfolio.investmentproject.mapper;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapstructMapperConfig.class)
public interface InvestmentProjectMapper {

    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "impactDescription", ignore = true)
    @Mapping(target = "institutionDescription", ignore = true)
    @Mapping(target = "teamDescription", ignore = true)
    @Mapping(target = "financingDescription", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    @Mapping(target = "occupancyPercentage", ignore = true)
    @Mapping(target = "loanId", ignore = true)
    @Mapping(target = "maxAmount", ignore = true)
    @Mapping(target = "area", ignore = true)
    @Mapping(target = "category", ignore = true)
    InvestmentProjectData map(InvestmentProject source);

    List<InvestmentProjectData> map(List<InvestmentProject> sources);

}
