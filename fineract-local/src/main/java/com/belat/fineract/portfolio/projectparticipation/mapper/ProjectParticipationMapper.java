/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.belat.fineract.portfolio.projectparticipation.mapper;

import com.belat.fineract.portfolio.projectparticipation.data.ProjectParticipationData;
import com.belat.fineract.portfolio.projectparticipation.domain.ProjectParticipation;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.util.List;

@Mapper(config = MapstructMapperConfig.class)
public interface ProjectParticipationMapper {

    @Mapping(target = "participantId", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "participantName", source = "client.displayName")
    @Mapping(target = "projectName", source = "investmentProject.name")
    @Mapping(target = "currencyCode", source = "investmentProject.currencyCode")
    @Mapping(target = "createdOnDate", source = "investmentProject", qualifiedByName = "getCreatedOnDate")
    ProjectParticipationData map(ProjectParticipation source);

    List<ProjectParticipationData> map(List<ProjectParticipation> sources);

    @Named("getCreatedOnDate")
    default LocalDate getCreatedOnDate(final InvestmentProject investmentProject) {
        return investmentProject.getCreatedDate().get().toLocalDate();
    }

}
