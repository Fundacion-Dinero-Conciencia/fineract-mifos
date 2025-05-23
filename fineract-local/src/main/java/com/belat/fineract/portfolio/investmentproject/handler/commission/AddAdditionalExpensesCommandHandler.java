package com.belat.fineract.portfolio.investmentproject.handler.commission;

import com.belat.fineract.portfolio.investmentproject.service.AdditionalExpensesWritePlatformService;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@CommandType(entity = "ADDITIONAL_EXPENSES", action = "CREATE")
public class AddAdditionalExpensesCommandHandler implements NewCommandSourceHandler {

    private final AdditionalExpensesWritePlatformService additionalExpensesWritePlatformService;

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        return additionalExpensesWritePlatformService.addAdditionalExpenses(command);
    }
}
