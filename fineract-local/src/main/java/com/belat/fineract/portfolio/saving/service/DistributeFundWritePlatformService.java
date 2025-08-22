package com.belat.fineract.portfolio.saving.service;

import java.time.LocalDate;
import java.util.Map;

public interface DistributeFundWritePlatformService {

    Map<String, Object> distributeFunds(Long accountId, LocalDate transferDate);
}
