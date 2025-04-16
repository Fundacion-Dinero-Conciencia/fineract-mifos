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
package com.belat.fineract.portfolio.projectparticipation.domain;

import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.projectparticipation.api.ProjectParticipationConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.client.domain.Client;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "e_project_participation")
public class ProjectParticipation extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "client_id", nullable = false, referencedColumnName = "id")
    private Client client;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "investment_project_id", nullable = false, referencedColumnName = "id")
    private InvestmentProject investmentProject;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "commission", nullable = false)
    private BigDecimal commission;

    @Column(name = "status_enum", nullable = false)
    private Integer statusEnum;

    @Column(name = "type", nullable = false)
    private String type;

    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {
        if (command.isChangeInIntegerSansLocaleParameterNamed(ProjectParticipationConstants.statusParamName, getStatusEnum())) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(ProjectParticipationConstants.statusParamName);
            actualChanges.put(ProjectParticipationConstants.statusParamName, newValue);
            this.statusEnum = newValue;
        }
        if (command.isChangeInBigDecimalParameterNamed(ProjectParticipationConstants.amountParamName, getAmount())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(ProjectParticipationConstants.amountParamName);
            actualChanges.put(ProjectParticipationConstants.amountParamName, newValue);
            this.amount = newValue;
        }
    }

}
