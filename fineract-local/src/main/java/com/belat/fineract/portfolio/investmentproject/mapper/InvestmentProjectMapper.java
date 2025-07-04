package com.belat.fineract.portfolio.investmentproject.mapper;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import java.util.List;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class, uses = {StatusHistoryProjectMapper.class})
@DecoratedWith(InvestmentProjectMapperDecorator.class)
public interface InvestmentProjectMapper {

    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "impactDescription", ignore = true)
    @Mapping(target = "institutionDescription", ignore = true)
    @Mapping(target = "teamDescription", ignore = true)
    @Mapping(target = "financingDescription", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "logo", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    @Mapping(target = "occupancyPercentage", ignore = true)
    @Mapping(target = "loanId", ignore = true)
    @Mapping(target = "availableTotalAmount", ignore = true)
    @Mapping(target = "area", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "ownerName", ignore = true)
    @Mapping(target = "objectives", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "littleSocioEnvironmentalDescription", ignore = true)
    @Mapping(target = "detailedSocioEnvironmentalDescription", ignore = true)
    InvestmentProjectData map(InvestmentProject source);

    List<InvestmentProjectData> map(List<InvestmentProject> sources);

}
