package com.belat.fineract.portfolio.investmentproject.mapper;

import com.belat.fineract.portfolio.investmentproject.data.InvestmentProjectData;
import com.belat.fineract.portfolio.investmentproject.data.StatusHistoryProjectData;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProject;
import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Mapper(config = MapstructMapperConfig.class, uses = {CodeValueMapper.class})
public interface StatusHistoryProjectMapper {

    @Mapping(source = "investmentProject.id", target = "investmentProjectId")
    @Mapping(source = "statusValue", target = "statusValue")
    @Mapping(source = "createdBy", target = "createdBy", qualifiedByName = "unwrapOptionalLong")
    @Mapping(source = "createdDate", target = "createdDate", qualifiedByName = "unwrapOffsetDateTime")
    @Mapping(target = "personInCharge", ignore = true)
    StatusHistoryProjectData map(StatusHistoryProject entity);

    @Mapping(source = "investmentProjectId", target = "investmentProject", qualifiedByName = "mapIdToInvestmentProject")
    @Mapping(source = "statusValue", target = "statusValue")
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    StatusHistoryProject map(StatusHistoryProjectData data);

    List<StatusHistoryProjectData> map(List<StatusHistoryProject> entities);

    @Named("mapIdToInvestmentProject")
    default InvestmentProject mapIdToInvestmentProject(Long id) {
        if (id == null) return null;
        InvestmentProject project = new InvestmentProject();
        project.setId(id);
        return project;
    }

    @Named("unwrapOptionalLong")
    default Long unwrapOptionalLong(Optional<Long> optional) {
        return optional.orElse(null);
    }

    @Named("unwrapOffsetDateTime")
    default OffsetDateTime unwrapOffsetDateTime(Optional<OffsetDateTime> dateTime) {
        return dateTime.orElse(null);
    }

}

