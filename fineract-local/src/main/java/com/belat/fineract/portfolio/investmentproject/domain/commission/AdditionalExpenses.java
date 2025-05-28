package com.belat.fineract.portfolio.investmentproject.domain.commission;

import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
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

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "commission_type_id", nullable = false, referencedColumnName = "id")
    private CodeValue commissionType;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "net_amount", nullable = false, scale = 6, precision = 19)
    private BigDecimal netAmount;

    // Value added tax
    @Column(name = "vat", nullable = false, scale = 6, precision = 19)
    private BigDecimal vat;

    @Column(name = "total_amount", nullable = false, scale = 6, precision = 19)
    private BigDecimal total;

    protected AdditionalExpenses() {
    }

    public AdditionalExpenses(InvestmentProject project, CodeValue commissionType, String description, BigDecimal netAmount, BigDecimal vat, BigDecimal total) {
        this.project = project;
        this.commissionType = commissionType;
        this.description = description;
        this.netAmount = netAmount;
        this.vat = vat;
        this.total = total;
    }

    public static AdditionalExpenses createAdditionalExpenses(final InvestmentProject project, final CodeValue commissionType, final String description, final BigDecimal netAmount, final BigDecimal vat, final BigDecimal total) {
        return new AdditionalExpenses(project, commissionType, description, netAmount, vat, total);
    }

    public void updateAdditionalExpenses(String description, BigDecimal netAmount, BigDecimal vat, BigDecimal total) {
        this.description = description;
        this.netAmount = netAmount;
        this.vat = vat;
        this.total = total;
    }

}
