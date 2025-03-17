package com.belat.fineract.portfolio.saving.service;

import java.util.Map;

public interface DistributeFundWritePlatformService {

    Map<String, Object> distributeFunds(Long accountId);
}
