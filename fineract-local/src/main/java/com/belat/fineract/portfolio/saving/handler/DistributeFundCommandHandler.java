package com.belat.fineract.portfolio.saving.handler;

import com.belat.fineract.portfolio.saving.service.impl.DistributeFundServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CommandType(entity = "SAVINGACCOUNT", action = "DISTRIBUTE_FUND")
public class DistributeFundCommandHandler implements NewCommandSourceHandler {

    private final DistributeFundServiceImpl distributeFunds;

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        return distributeFunds.distributeFund(command);
    }
}
