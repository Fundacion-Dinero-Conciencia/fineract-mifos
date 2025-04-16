package com.belat.fineract.portfolio.investmentproject.domain.description;

import com.belat.fineract.portfolio.investmentproject.api.InvestmentProjectConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "e_investment_project_description")
public class InvestmentProjectDescription extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "impact_description", nullable = false)
    private String impactDescription;

    @Column(name = "institution_description", nullable = false)
    private String institutionDescription;

    @Column(name = "team_description", nullable = false)
    private String teamDescription;

    @Column(name = "financing_description", nullable = false)
    private String financingDescription;

    public InvestmentProjectDescription(final String impactDescription, final String institutionDescription, final String teamDescription,
            final String financingDescription) {
        this.impactDescription = impactDescription;
        this.institutionDescription = institutionDescription;
        this.teamDescription = teamDescription;
        this.financingDescription = financingDescription;
    }

    public void modifyApplication(final JsonCommand command, final Map<String, Object> actualChanges) {
        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.impactDescriptionParamName, getImpactDescription())) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.impactDescriptionParamName);
            actualChanges.put(InvestmentProjectConstants.impactDescriptionParamName, newValue);
            this.impactDescription = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.institutionDescriptionParamName,
                getInstitutionDescription())) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.institutionDescriptionParamName);
            actualChanges.put(InvestmentProjectConstants.institutionDescriptionParamName, newValue);
            this.institutionDescription = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.teamDescriptionParamName, getTeamDescription())) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.teamDescriptionParamName);
            actualChanges.put(InvestmentProjectConstants.teamDescriptionParamName, newValue);
            this.teamDescription = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(InvestmentProjectConstants.financingDescriptionParamName, getFinancingDescription())) {
            final String newValue = command.stringValueOfParameterNamed(InvestmentProjectConstants.financingDescriptionParamName);
            actualChanges.put(InvestmentProjectConstants.financingDescriptionParamName, newValue);
            this.financingDescription = StringUtils.defaultIfEmpty(newValue, null);
        }
    }

}
