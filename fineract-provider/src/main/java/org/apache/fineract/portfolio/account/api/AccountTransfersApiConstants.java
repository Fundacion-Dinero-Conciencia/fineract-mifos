/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.account.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.portfolio.account.AccountDetailConstants;
import org.apache.fineract.portfolio.account.data.AccountTransferData;

public final class AccountTransfersApiConstants {

    private AccountTransfersApiConstants() {

    }

    public static final String ACCOUNT_TRANSFER_RESOURCE_NAME = "accounttransfer";
    public static final String SAVINGS_TRANSACTION_FREEZE_REASON_CODE_NAME = "SavingsTransactionFreezeReasons" ;
    public static final String DISBURSEMENT_COMMISSION = "DISBURSEMENT_COMMISSION";
    public static final String INVESTMENT_THROUGH_KIPHU = "Investment through kiphu";

    // transaction parameters
    public static final String transferDateParamName = "transferDate";
    public static final String transferAmountParamName = "transferAmount";
    public static final String transferDescriptionParamName = "transferDescription";
    public static final String currencyParamName = "currency";
    public static final String transferIsInvestmentParamName = "isInvestment";
    public static final String investmentAgentIdParamName = "investmentAgentId";
    public static final String percentageInvestmentAgentParamName = "percentageInvestmentAgent";
    public static final String amountProjectParamName = "amount";
    public static final String projectIdParamName = "projectId";

    /**
     * These parameters will match the class level parameters of {@link AccountTransferData}. Where possible, we try to
     * get response parameters to match those of request parameters.
     */
    static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(AccountDetailConstants.idParamName, transferDescriptionParamName, currencyParamName));
}
