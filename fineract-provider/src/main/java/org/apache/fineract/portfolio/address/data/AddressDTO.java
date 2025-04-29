package org.apache.fineract.portfolio.address.data;

import lombok.Data;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

import java.math.BigDecimal;

@Data
public class AddressDTO {
    private String street;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String townVillage;
    private String city;
    private String countyDistrict;
    private CodeValueData stateProvince;
    private CodeValueData country;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
