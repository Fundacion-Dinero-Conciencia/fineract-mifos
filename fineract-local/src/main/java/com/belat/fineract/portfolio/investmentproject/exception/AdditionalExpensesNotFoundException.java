package com.belat.fineract.portfolio.investmentproject.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class AdditionalExpensesNotFoundException extends AbstractPlatformResourceNotFoundException {

    public AdditionalExpensesNotFoundException(final Long id) {
        super("error.msg.additional.expenses.not.found",
                "Additional expense does not exist", id);
    }

}
