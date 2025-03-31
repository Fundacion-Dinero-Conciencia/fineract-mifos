package com.belat.fineract.portfolio.investmentproject.domain;

import com.belat.fineract.portfolio.investmentproject.api.InvestmentProjectConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.client.domain.Client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "e_investment_project")
public class InvestmentProject extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "name", nullable = false)
    private String name;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false, referencedColumnName = "id")
    private Client owner;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "rate", nullable = false)
    private BigDecimal rate;

    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {
        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.projectNameParamName, getName())) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.projectNameParamName);
            actualChanges.put(InvestmentProjectConstants.projectNameParamName, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.descriptionParamName, getName())) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.descriptionParamName);
            actualChanges.put(InvestmentProjectConstants.descriptionParamName, newValue);
            this.description = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInBigDecimalParameterNamed(InvestmentProjectConstants.projectRateParamName, getRate())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(InvestmentProjectConstants.projectRateParamName);
            actualChanges.put(InvestmentProjectConstants.projectRateParamName, newValue);
            this.rate = newValue;
        }
    }

}
