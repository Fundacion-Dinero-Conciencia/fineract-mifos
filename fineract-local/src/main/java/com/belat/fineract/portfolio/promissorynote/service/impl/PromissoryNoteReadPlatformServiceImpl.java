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
package com.belat.fineract.portfolio.promissorynote.service.impl;

import com.belat.fineract.portfolio.promissorynote.data.PromissoryNoteData;
import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNote;
import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNoteRepository;
import com.belat.fineract.portfolio.promissorynote.exception.PromissoryNoteNotFoundException;
import com.belat.fineract.portfolio.promissorynote.mapper.PromissoryNoteMapper;
import com.belat.fineract.portfolio.promissorynote.service.PromissoryNoteReadPlatformService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromissoryNoteReadPlatformServiceImpl implements PromissoryNoteReadPlatformService {

    private final PromissoryNoteRepository promissoryNoteRepository;
    private final PromissoryNoteMapper promissoryNoteMapper;
    // private final SavingsAccountReadPlatformServiceImpl savingsAccountReadPlatformService;

    @Override
    public List<PromissoryNoteData> retrieveAll() {
        List<PromissoryNote> promissory = promissoryNoteRepository.findAll();
        return promissoryNoteMapper.map(promissory);
    }

    @Override
    public List<PromissoryNote> retrieveAllPromissoryNote() {
        return promissoryNoteRepository.findAll();
    }

    @Override
    public PromissoryNoteData retrieveOne(Long id) {
        PromissoryNote promissory = promissoryNoteRepository.findById(id).orElseThrow(() -> new PromissoryNoteNotFoundException(id));

        PromissoryNoteData promissoryNoteData = promissoryNoteMapper.map(promissory);
        return promissoryNoteMapper.map(promissory);
    }

    @Override
    public PromissoryNoteData retrieveOneByPromissoryNoteNumber(String promissoryNoteNumber) {
        PromissoryNote promissory = promissoryNoteRepository.retrieveOneByPromissoryNoteNumber(promissoryNoteNumber);
        return promissoryNoteMapper.map(promissory);
    }

    @Override
    public List<PromissoryNote> retrieveByFundAccountId(Long fundAccountId) {
        return promissoryNoteRepository.retrieveByFundAccountId(fundAccountId);
    }

    @Override
    public List<PromissoryNote> retrieveByInvestorAccountId(Long investorAccountId) {
        return promissoryNoteRepository.retrieveByInvestorAccountId(investorAccountId);
    }
}
