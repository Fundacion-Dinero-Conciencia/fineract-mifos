package org.apache.fineract.portfolio.investmentproject.mapper;

import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.portfolio.address.AddressMapper;
import org.apache.fineract.portfolio.investmentproject.data.InvestmentProjectAddressDTO;
import org.apache.fineract.portfolio.investmentproject.domain.InvestmentProjectAddress;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapstructMapperConfig.class, uses = {AddressMapper.class, CodeValueMapper.class})
public interface InvestmentProjectAddressMapper {

    InvestmentProjectAddressDTO map(InvestmentProjectAddress source);

    List<InvestmentProjectAddressDTO> map(List<InvestmentProjectAddress> sources);

}

