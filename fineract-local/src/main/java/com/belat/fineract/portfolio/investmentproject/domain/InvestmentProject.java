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
package com.belat.fineract.portfolio.investmentproject.domain;

import com.belat.fineract.portfolio.investmentproject.api.InvestmentProjectConstants;
import com.belat.fineract.portfolio.investmentproject.domain.category.InvestmentProjectCategory;
import com.belat.fineract.portfolio.investmentproject.domain.description.InvestmentProjectDescription;
import com.belat.fineract.portfolio.investmentproject.domain.objective.InvestmentProjectObjective;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "e_investment_project")
public class InvestmentProject extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "subtitle", nullable = false)
    private String subtitle;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false, referencedColumnName = "id")
    private Client owner;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "rate", nullable = false)
    private BigDecimal rate;

    @Column(name = "period", nullable = false)
    private Integer period;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "country_id", nullable = false, referencedColumnName = "id")
    private CodeValue country;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "description_id", nullable = false, referencedColumnName = "id")
    private InvestmentProjectDescription description;

    @Setter
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "investmentProject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvestmentProjectCategory> subCategories;

    @OneToMany(mappedBy = "investmentProject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvestmentProjectObjective> objectives;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "category_id", nullable = false, referencedColumnName = "id")
    private CodeValue category;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "area_id", nullable = false, referencedColumnName = "id")
    private CodeValue area;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "loan_id", nullable = false, referencedColumnName = "id")
    private Loan loan;

    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {
        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.projectNameParamName, getName())) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.projectNameParamName);
            actualChanges.put(InvestmentProjectConstants.projectNameParamName, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.subtitleParamName, getName())) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.subtitleParamName);
            actualChanges.put(InvestmentProjectConstants.subtitleParamName, newValue);
            this.subtitle = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInBigDecimalParameterNamed(InvestmentProjectConstants.projectRateParamName, getRate())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.projectRateParamName);
            actualChanges.put(InvestmentProjectConstants.projectRateParamName, newValue);
            this.rate = newValue;
        }

        this.description.modifyApplication(command, actualChanges);

        if (command.isChangeInBooleanParameterNamed(InvestmentProjectConstants.isActiveParamName, isActive)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(InvestmentProjectConstants.isActiveParamName);
            actualChanges.put(InvestmentProjectConstants.isActiveParamName, newValue);
            this.isActive = newValue;
        }
    }

}
