package org.apache.fineract.portfolio.client.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ClientInfoCustomizedDTO {

    private String fullName;
    private String fancyName;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String mobileNo;
    private String rut;
    private String occupation;
    private String address;
    private String accountNo;
    private String bankName;
    private String accountType;
    private String contactName;
    private String contactLastName;
    private String contactEmail;
    private String contactMobileNo;
    private String dateOfBirth;
    private String maritalStatus;
    private String nationality;

    /**
     * It´s referenced to Debtor client
     */
    public ClientInfoCustomizedDTO(String fullName, String fancyName, String email, String mobileNo, String rut, String address, String accountNo, String bankName, String accountType, String contactMobileNo, String contactEmail, String contactLastName, String contactName, String occupation, String empty) {
        this.fullName = fullName;
        this.fancyName = fancyName;
        this.email = email;
        this.mobileNo = mobileNo;
        this.rut = rut;
        this.address = address;
        this.accountNo = accountNo;
        this.bankName = bankName;
        this.accountType = accountType;
        this.contactMobileNo = contactMobileNo;
        this.contactEmail = contactEmail;
        this.contactLastName = contactLastName;
        this.contactName = contactName;
        this.occupation = occupation;
    }

    /**
     * It's referenced to investor client
     */
    public ClientInfoCustomizedDTO(String firstName, String middleName, String lastName, String email, String mobileNo, String rut, String occupation, String address, String accountNo, String bankName, String accountType, String dateOfBirth, String maritalStatus, String nationality) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.mobileNo = mobileNo;
        this.rut = rut;
        this.occupation = occupation;
        this.address = address;
        this.accountNo = accountNo;
        this.bankName = bankName;
        this.accountType = accountType;
        this.dateOfBirth = dateOfBirth;
        this.maritalStatus = maritalStatus;
        this.nationality = nationality;
    }
}
