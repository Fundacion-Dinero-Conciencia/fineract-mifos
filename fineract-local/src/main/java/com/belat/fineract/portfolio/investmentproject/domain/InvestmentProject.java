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
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProject;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    @Column(name = "max_amount", nullable = false)
    private BigDecimal maxAmount;

    @Column(name = "min_amount", nullable = false)
    private BigDecimal minAmount;

//    @OneToMany(mappedBy = "investmentProject", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @OrderBy("createdDate DESC")
//    private List<StatusHistoryProject> statusHistory = new ArrayList<>();

    @Transient
    private StatusHistoryProject status;

    @Column(name = "mnemonic", nullable = false)
    private String mnemonic;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "amount_to_be_financed", nullable = false)
    private BigDecimal amountToBeFinanced;

    @Column(name = "amount_to_be_delivered", nullable = false)
    private BigDecimal amountToBeDelivered;

    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {
        if (command.isChangeInBigDecimalParameterNamed(InvestmentProjectConstants.amountParamName, getAmount())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.amountParamName);
            actualChanges.put(InvestmentProjectConstants.amountParamName, newValue);
            this.amount = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(InvestmentProjectConstants.periodParamName, getPeriod(), Locale.ENGLISH)) {
            final Integer newValue = command.integerValueOfParameterNamed(InvestmentProjectConstants.periodParamName, Locale.ENGLISH);
            actualChanges.put(InvestmentProjectConstants.periodParamName, newValue);
            this.period = newValue;
        }

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
        if (command.isChangeInBigDecimalParameterNamed(InvestmentProjectConstants.maxAmountParamName, maxAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.maxAmountParamName);
            if (newValue.compareTo(amount) > 0) {
                throw new GeneralPlatformDomainRuleException("err.msg.max.amount.is.higher.than.project.amount", "Max amount is higher than project amount");
            }
            actualChanges.put(InvestmentProjectConstants.maxAmountParamName, newValue);
            this.maxAmount = newValue;
        }
        if (command.isChangeInBigDecimalParameterNamed(InvestmentProjectConstants.minAmountParamName, minAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.minAmountParamName);
            if (newValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new GeneralPlatformDomainRuleException("err.msg.min.amount.should.be.higher.than.zero", "Min amount should be higher than zero");
            }
            actualChanges.put(InvestmentProjectConstants.minAmountParamName, newValue);
            this.minAmount = newValue;
        }
        if (command.isChangeInIntegerSansLocaleParameterNamed(InvestmentProjectConstants.positionParamName, position)) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(InvestmentProjectConstants.positionParamName);
            actualChanges.put(InvestmentProjectConstants.positionParamName, newValue);
            this.position = newValue;
        }
        if (command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.amountToBeFinancedParamName) != null ||
                command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.amountToBeDeliveredParamName) != null) {
            if (command.isChangeInBigDecimalParameterNamed(InvestmentProjectConstants.amountToBeFinancedParamName, amountToBeFinanced)) {
                final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.amountToBeFinancedParamName);
                if (newValue.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new GeneralPlatformDomainRuleException("err.msg.amount.to.be.financed.should.be.higher.than.zero", "Min amount should be higher than zero");
                }
                actualChanges.put(InvestmentProjectConstants.amountToBeFinancedParamName, newValue);
                this.amountToBeFinanced = newValue;
            }
            if (command.isChangeInBigDecimalParameterNamed(InvestmentProjectConstants.amountToBeDeliveredParamName, amountToBeDelivered)) {
                final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.amountToBeDeliveredParamName);
                if (newValue.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new GeneralPlatformDomainRuleException("err.msg.min.amount.should.be.higher.than.zero", "Min amount should be higher than zero");
                }
                actualChanges.put(InvestmentProjectConstants.amountToBeDeliveredParamName, newValue);
                this.amountToBeDelivered = newValue;
            }
        }

    }

}
