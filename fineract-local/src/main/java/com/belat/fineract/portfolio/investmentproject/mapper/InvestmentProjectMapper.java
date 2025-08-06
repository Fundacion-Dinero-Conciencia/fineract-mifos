package com.belat.fineract.portfolio.investmentproject.mapper;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import java.util.List;

import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class, uses = {StatusHistoryProjectMapper.class, CodeValueMapper.class, InvestmentProjectObjectivesMapper.class, InvestmentProjectCategoryMapper.class})
@DecoratedWith(InvestmentProjectMapperDecorator.class)
public interface InvestmentProjectMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.fullname")
    @Mapping(target = "loanId", source = "loan.id")
    @Mapping(target = "loanApprovedPrincipalAmount", source = "loan.approvedPrincipal")
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "impactDescription", source = "description.impactDescription")
    @Mapping(target = "institutionDescription", source = "description.institutionDescription")
    @Mapping(target = "teamDescription", source = "description.teamDescription")
    @Mapping(target = "financingDescription", source = "description.financingDescription")
    @Mapping(target = "littleSocioEnvironmentalDescription", source = "description.socioEnvironmentalDescription.littleDescription")
    @Mapping(target = "detailedSocioEnvironmentalDescription", source = "description.socioEnvironmentalDescription.detailedDescription")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "cover", ignore = true)
    @Mapping(target = "logo", ignore = true)
    @Mapping(target = "availableTotalAmount", ignore = true)
    @Mapping(target = "occupancyPercentage", ignore = true)
    @Mapping(target = "status", ignore = true)
    InvestmentProjectData map(InvestmentProject source);

    List<InvestmentProjectData> map(List<InvestmentProject> sources);

}
