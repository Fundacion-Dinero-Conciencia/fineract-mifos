package com.belat.fineract.portfolio.projectparticipation.domain;

import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.projectparticipation.api.ProjectParticipationConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.client.domain.Client;

import java.math.BigDecimal;
import java.util.Map;

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

    @Column(name = "status_enum", nullable = false)
    private Integer statusEnum;

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
