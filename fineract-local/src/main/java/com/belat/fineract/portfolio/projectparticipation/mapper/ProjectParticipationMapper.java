package com.belat.fineract.portfolio.projectparticipation.mapper;

import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationData;
import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipation;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapstructMapperConfig.class)
public interface ProjectParticipationMapper {

    @Mapping(target = "participantId", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "status", ignore = true)
    ProjectParticipationData map(ProjectParticipation source);

    List<ProjectParticipationData> map(List<ProjectParticipation> sources);

}
