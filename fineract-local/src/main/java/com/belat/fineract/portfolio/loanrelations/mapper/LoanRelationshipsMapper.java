package com.belat.fineract.portfolio.loanrelations.mapper;

import com.belat.fineract.portfolio.loanrelations.data.LoanRelationshipsData;
import com.belat.fineract.portfolio.loanrelations.domain.LoanRelationships;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapstructMapperConfig.class, uses = {})
public interface LoanRelationshipsMapper {

    LoanRelationshipsData map(LoanRelationships loanRelationships);

    List<LoanRelationshipsData> map(List<LoanRelationships> loanRelationships);
}
