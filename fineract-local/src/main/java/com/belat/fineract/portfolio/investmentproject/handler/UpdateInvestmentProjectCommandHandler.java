package com.belat.fineract.portfolio.investmentproject.handler;

import com.belat.fineract.portfolio.investmentproject.service.InvestmentProjectWritePlatformService;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@CommandType(entity = "INVESTMENT_PROJECT", action = "UPDATE")
public class UpdateInvestmentProjectCommandHandler implements NewCommandSourceHandler {

    private final InvestmentProjectWritePlatformService investmentProjectWritePlatformService;

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        return investmentProjectWritePlatformService.updateInvestmentProject(command.entityId(), command);
    }
}
