package com.belat.fineract.portfolio.investmentproject.domain.objective;

import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "e_investment_project_objective")
public class InvestmentProjectObjective extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "objective_id", nullable = false, referencedColumnName = "id")
    private CodeValue objective;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "investment_project_id", nullable = false, referencedColumnName = "id")
    private InvestmentProject investmentProject;

    public InvestmentProjectObjective(CodeValue objective, InvestmentProject investmentProject) {
        this.objective = objective;
        this.investmentProject = investmentProject;
    }
}
