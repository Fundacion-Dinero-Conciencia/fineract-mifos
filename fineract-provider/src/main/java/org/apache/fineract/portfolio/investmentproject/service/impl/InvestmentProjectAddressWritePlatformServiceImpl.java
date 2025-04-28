package org.apache.fineract.portfolio.investmentproject.service.impl;

import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.service.InvestmentProjectReadPlatformService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.analysis.function.Add;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.address.domain.Address;
import org.apache.fineract.portfolio.address.domain.AddressRepository;
import org.apache.fineract.portfolio.address.service.AddressWritePlatformService;
import org.apache.fineract.portfolio.investmentproject.domain.InvestmentProjectAddress;
import org.apache.fineract.portfolio.investmentproject.domain.InvestmentProjectAddressRepository;
import org.apache.fineract.portfolio.investmentproject.service.InvestmentProjectAddressReadPlatformService;
import org.apache.fineract.portfolio.investmentproject.service.InvestmentProjectAddressWritePlatformService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestmentProjectAddressWritePlatformServiceImpl implements InvestmentProjectAddressWritePlatformService {

    private final AddressWritePlatformService addressWritePlatformService;
    private final AddressRepository addressRepository;
    private final InvestmentProjectReadPlatformService investmentProjectReadPlatformService;
    private final InvestmentProjectAddressRepository investmentProjectAddressRepository;
    private final InvestmentProjectAddressReadPlatformService investmentProjectAddressReadPlatformService;
    private final CodeValueRepository codeValueRepository;


    @Override
    public CommandProcessingResult createAddress(JsonCommand command) {


        final Long investmentProjectId = command.subentityId();

        InvestmentProject investmentProject = investmentProjectReadPlatformService.retrieveInvestmentById(investmentProjectId);
        InvestmentProjectAddress investmentProjectAddress = investmentProjectAddressRepository.getByInvestmentProject_id(investmentProjectId);
        investmentProjectAddress.setInvestmentProject(investmentProject);

        JsonObject jsonObject = command.parsedJson().getAsJsonObject();

        boolean isUpdate = validateUpdate(investmentProjectId);
        Address address = null;
        if (isUpdate) {
            address = updateAddress(jsonObject, investmentProjectId);
            investmentProjectAddress.setAddress(address);
        } else {
            address = addressWritePlatformService.createAddress(jsonObject);
            investmentProjectAddress.setAddress(address);
        }
        addressRepository.save(address);
        investmentProjectAddress = investmentProjectAddressRepository.saveAndFlush(investmentProjectAddress);

        return new CommandProcessingResultBuilder()
                .withCommandId(command.commandId())
                .withResourceIdAsString(String.valueOf(investmentProjectAddress.getId()))
                .withEntityId(investmentProjectAddress.getId())
                .build();

    }

    private boolean validateUpdate(Long investmentProjectId) {
        InvestmentProjectAddress investmentProjectAddress = investmentProjectAddressReadPlatformService.getByInvestmentProjectId(investmentProjectId);
        return investmentProjectAddress != null;
    }

    private Address updateAddress(JsonObject jsonObject, Long id) {
        CodeValue stateIdCodeValue = null;
        if (jsonObject.get("stateProvinceId") != null) {
            long stateId = jsonObject.get("stateProvinceId").getAsLong();
            stateIdCodeValue = codeValueRepository.getReferenceById(stateId);
        }

        CodeValue countryIdCodeValue = null;
        if (jsonObject.get("countryId") != null) {
            long countryId = jsonObject.get("countryId").getAsLong();
            countryIdCodeValue = codeValueRepository.getReferenceById(countryId);
        }
        InvestmentProjectAddress investmentProjectAddress = investmentProjectAddressReadPlatformService.getByInvestmentProjectId(id);

        Address address = investmentProjectAddress.getAddress();
        Address addressUpdate = Address.fromJsonObject(jsonObject,stateIdCodeValue,countryIdCodeValue);

        address.setAddressLine1(addressUpdate.getAddressLine1());
        address.setAddressLine2(addressUpdate.getAddressLine2());
        address.setAddressLine3(addressUpdate.getAddressLine3());
        address.setCity(addressUpdate.getCity());
        address.setUpdatedOn(DateUtils.getBusinessLocalDate());
        address.setCountry(addressUpdate.getCountry());
        address.setTownVillage(addressUpdate.getTownVillage());
        address.setLatitude(addressUpdate.getLatitude());
        address.setLongitude(addressUpdate.getLongitude());
        return address;
    }

}
