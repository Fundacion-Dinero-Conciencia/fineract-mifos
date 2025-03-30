package com.belat.fineract.portfolio.investmentproject.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.client.domain.Client;

import java.math.BigDecimal;

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

}
