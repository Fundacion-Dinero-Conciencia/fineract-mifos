package com.belat.fineract.portfolio.saving.service.impl;

import static com.belat.fineract.portfolio.saving.api.DistributeFundConstants.fundSavingsAccountIdParamName;
import static com.belat.fineract.portfolio.saving.api.DistributeFundConstants.transferDateParamName;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DistributeFundServiceImpl {

    private final FromJsonHelper fromApiJsonHelper;
    private final PlatformSecurityContext context;
    private final DistributeFundWritePlatformServiceImpl distributeFundWritePlatformService;

    public CommandProcessingResult distributeFund(JsonCommand command) {

        context.authenticatedUser();
        validateParams(command.json());
        Long accountId = command.longValueOfParameterNamed(fundSavingsAccountIdParamName);
        final LocalDate transferDate = command.dateValueOfParameterNamed(transferDateParamName);
        Map<String, Object> changes = null;
        changes = distributeFundWritePlatformService.distributeFunds(accountId, transferDate);

        return new CommandProcessingResultBuilder() //
                .with(changes) //
                .build();
    }

    public void validateParams(final String json) {
        final Set<String> DISTRIBUTE_FUNDS_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(fundSavingsAccountIdParamName, transferDateParamName));
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, DISTRIBUTE_FUNDS_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(fundSavingsAccountIdParamName);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long savingAccountIdFund = this.fromApiJsonHelper.extractLongNamed(fundSavingsAccountIdParamName, element);
        baseDataValidator.reset().parameter(fundSavingsAccountIdParamName).value(savingAccountIdFund).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
