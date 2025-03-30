package com.belat.fineract.portfolio.investmentproject.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class InvestmentProjectNotFoundException extends AbstractPlatformResourceNotFoundException {

    public InvestmentProjectNotFoundException(final Long id, final boolean isWithClientId) {
        super("error.msg.investment.project.not.found", "Investment project with " + (isWithClientId ? "client id " : "identifier ")  + id + " does not exist", id);
    }

}
