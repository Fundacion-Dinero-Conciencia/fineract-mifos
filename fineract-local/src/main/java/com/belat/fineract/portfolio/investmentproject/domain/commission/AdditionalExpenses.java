package com.belat.fineract.portfolio.investmentproject.domain.commission;

import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "e_additional_expenses")
public class AdditionalExpenses extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id", nullable = false)
    private InvestmentProject project;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "net_amount", nullable = false, scale = 6, precision = 19)
    private BigDecimal netAmount;

    // Value added tax
    @Column(name = "vat", nullable = false, scale = 6, precision = 19)
    private BigDecimal vat;

    protected AdditionalExpenses() {
    }

    public AdditionalExpenses(InvestmentProject project, String name, BigDecimal netAmount, BigDecimal vat) {
        this.project = project;
        this.name = name;
        this.netAmount = netAmount;
        this.vat = vat;
    }

    public static AdditionalExpenses createAdditionalExpenses(final InvestmentProject project, final String name, final BigDecimal netAmount, final BigDecimal vat) {
        return new AdditionalExpenses(project, name, netAmount, vat);
    }
}
