package com.belat.fineract.portfolio.investmentproject.domain.category;

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
@Table(name = "e_investment_project_category")
public class InvestmentProjectCategory extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "category_id", nullable = false, referencedColumnName = "id")
    private CodeValue category;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "investment_project_id", nullable = false, referencedColumnName = "id")
    private InvestmentProject investmentProject;

    public InvestmentProjectCategory(CodeValue category, InvestmentProject investmentProject) {
        this.category = category;
        this.investmentProject = investmentProject;
    }
}
