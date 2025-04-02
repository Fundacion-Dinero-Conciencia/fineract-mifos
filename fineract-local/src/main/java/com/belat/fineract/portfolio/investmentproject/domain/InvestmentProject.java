package com.belat.fineract.portfolio.investmentproject.domain;

import com.belat.fineract.portfolio.investmentproject.api.InvestmentProjectConstants;
import com.belat.fineract.portfolio.investmentproject.domain.category.InvestmentProjectCategory;
import com.belat.fineract.portfolio.investmentproject.domain.description.InvestmentProjectDescription;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.client.domain.Client;

import java.math.BigDecimal;
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
    private List<InvestmentProjectCategory> categories;

    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {
        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.projectNameParamName, getName())) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.projectNameParamName);
            actualChanges.put(InvestmentProjectConstants.projectNameParamName, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
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
