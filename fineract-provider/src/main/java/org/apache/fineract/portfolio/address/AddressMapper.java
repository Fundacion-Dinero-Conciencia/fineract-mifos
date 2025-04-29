package org.apache.fineract.portfolio.address;

import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.portfolio.address.data.AddressDTO;
import org.apache.fineract.portfolio.address.domain.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class, uses = {CodeValueMapper.class, })
public interface AddressMapper {

    AddressDTO toDto(Address address);


    @Mapping(target = "clientaddress", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "id", ignore = true)
    Address toEntity(AddressDTO addressDTO);
}
