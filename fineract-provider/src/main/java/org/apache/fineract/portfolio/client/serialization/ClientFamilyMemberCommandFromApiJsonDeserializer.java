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

package org.apache.fineract.portfolio.client.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class ClientFamilyMemberCommandFromApiJsonDeserializer {

    public static final String ID = "id";
    public static final String CLIENT_ID = "clientId";
    public static final String FIRST_NAME = "firstName";
    public static final String MIDDLE_NAME = "middleName";
    public static final String LAST_NAME = "lastName";
    public static final String QUALIFICATION = "qualification";
    public static final String MOBILE_NUMBER = "mobileNumber";
    public static final String AGE = "age";
    public static final String IS_MARITAL_PARTNERSHIP = "isMaritalPartnership";
    public static final String RELATIONSHIP_ID = "relationshipId";
    public static final String MARITAL_STATUS_ID = "maritalStatusId";
    public static final String GENDER_ID = "genderId";
    public static final String DATE_OF_BIRTH = "dateOfBirth";
    public static final String PROFESSION_ID = "professionId";
    public static final String EMAIL = "email";
    public static final String LOCALE = "locale";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String FAMILY_MEMBERS = "familyMembers";
    public static final String DOCUMENT_TYPE_ID = "documentTypeId";
    public static final String DOCUMENT_NUMBER = "documentNumber";
    public static final String ADDRESS = "address";
    public static final String EXPIRATION_DATE = "expirationDate";
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList(ID, CLIENT_ID, FIRST_NAME, MIDDLE_NAME, LAST_NAME, QUALIFICATION, MOBILE_NUMBER, EMAIL, IS_MARITAL_PARTNERSHIP,
                    RELATIONSHIP_ID, MARITAL_STATUS_ID, GENDER_ID, PROFESSION_ID, LOCALE, DATE_FORMAT, FAMILY_MEMBERS,
                    DOCUMENT_TYPE_ID, DOCUMENT_NUMBER, ADDRESS, EXPIRATION_DATE));
    public static final String FAMILY_MEMBERS1 = "FamilyMembers";
    public static final String RELATION_SHIP_ID = "relationShipId";
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ClientFamilyMemberCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(FAMILY_MEMBERS1);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.extractArrayNamed(FAMILY_MEMBERS, element) != null) {
            final JsonArray familyMembers = this.fromApiJsonHelper.extractJsonArrayNamed(FAMILY_MEMBERS, element);
            baseDataValidator.reset().value(familyMembers).arrayNotEmpty();
        } else {
            baseDataValidator.reset().value(this.fromApiJsonHelper.extractJsonArrayNamed(FAMILY_MEMBERS, element)).arrayNotEmpty();
        }

        validateForCreate(1, json);

    }

    public void validateForCreate(final long clientId, String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(FAMILY_MEMBERS1);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        baseDataValidator.reset().value(clientId).notBlank().integerGreaterThanZero();

        if (this.fromApiJsonHelper.extractStringNamed(FIRST_NAME, element) != null) {
            final String firstName = this.fromApiJsonHelper.extractStringNamed(FIRST_NAME, element);
            baseDataValidator.reset().parameter(FIRST_NAME).value(firstName).notNull().notBlank().notExceedingLengthOf(100);
        } else {
            baseDataValidator.reset().parameter(FIRST_NAME).value(this.fromApiJsonHelper.extractStringNamed(FIRST_NAME, element)).notNull()
                    .notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed(LAST_NAME, element) != null) {
            final String lastName = this.fromApiJsonHelper.extractStringNamed(LAST_NAME, element);
            baseDataValidator.reset().parameter(LAST_NAME).value(lastName).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed(MIDDLE_NAME, element) != null) {
            final String middleName = this.fromApiJsonHelper.extractStringNamed(MIDDLE_NAME, element);
            baseDataValidator.reset().parameter(MIDDLE_NAME).value(middleName).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed(QUALIFICATION, element) != null) {
            final String qualification = this.fromApiJsonHelper.extractStringNamed(QUALIFICATION, element);
            baseDataValidator.reset().parameter(QUALIFICATION).value(qualification).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed(MOBILE_NUMBER, element) != null) {
            final String mobileNumber = this.fromApiJsonHelper.extractStringNamed(MOBILE_NUMBER, element);
            baseDataValidator.reset().parameter(MOBILE_NUMBER).value(mobileNumber).notNull().notBlank().notExceedingLengthOf(100);
        }

        final String email = this.fromApiJsonHelper.extractStringNamed(EMAIL, element);
        baseDataValidator.reset().parameter(EMAIL).value(email).notNull().notBlank();

        if (this.fromApiJsonHelper.extractBooleanNamed(IS_MARITAL_PARTNERSHIP, element) != null) {
            final Boolean isMaritalPartnership = this.fromApiJsonHelper.extractBooleanNamed(IS_MARITAL_PARTNERSHIP, element);
            baseDataValidator.reset().parameter(IS_MARITAL_PARTNERSHIP).value(isMaritalPartnership).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractLongNamed(RELATION_SHIP_ID, element) != null) {
            final long relationShipId = this.fromApiJsonHelper.extractLongNamed(RELATION_SHIP_ID, element);
            baseDataValidator.reset().parameter(RELATION_SHIP_ID).value(relationShipId).notBlank().longGreaterThanZero();

        } else {
            baseDataValidator.reset().parameter(RELATION_SHIP_ID).value(this.fromApiJsonHelper.extractLongNamed(RELATION_SHIP_ID, element))
                    .notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed(MARITAL_STATUS_ID, element) != null) {
            final long maritalStatusId = this.fromApiJsonHelper.extractLongNamed(MARITAL_STATUS_ID, element);
            baseDataValidator.reset().parameter(MARITAL_STATUS_ID).value(maritalStatusId).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed(GENDER_ID, element) != null) {
            final long genderId = this.fromApiJsonHelper.extractLongNamed(GENDER_ID, element);
            baseDataValidator.reset().parameter(GENDER_ID).value(genderId).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed(AGE, element) != null) {
            final long age = this.fromApiJsonHelper.extractLongNamed(AGE, element);
            baseDataValidator.reset().parameter(AGE).value(age).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed(PROFESSION_ID, element) != null) {
            final long professionId = this.fromApiJsonHelper.extractLongNamed(PROFESSION_ID, element);
            baseDataValidator.reset().parameter(PROFESSION_ID).value(professionId).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLocalDateNamed(DATE_OF_BIRTH, element) != null) {
            final LocalDate dateOfBirth = this.fromApiJsonHelper.extractLocalDateNamed(DATE_OF_BIRTH, element);
            baseDataValidator.reset().parameter(DATE_OF_BIRTH).value(dateOfBirth).value(dateOfBirth).notNull()
                    .validateDateBefore(DateUtils.getBusinessLocalDate());
        }

        if (this.fromApiJsonHelper.extractLocalDateNamed(EXPIRATION_DATE, element) != null) {
            final LocalDate expirationDate = this.fromApiJsonHelper.extractLocalDateNamed(EXPIRATION_DATE, element);
            baseDataValidator.reset().parameter(EXPIRATION_DATE).value(expirationDate).value(expirationDate).notNull()
                    .validateDateBefore(DateUtils.getBusinessLocalDate());
        }

        if (this.fromApiJsonHelper.extractStringNamed(ADDRESS, element) != null) {
            final String address = this.fromApiJsonHelper.extractStringNamed(ADDRESS, element);
            baseDataValidator.reset().parameter(ADDRESS).value(address).notNull().notBlank().notExceedingLengthOf(100);
        }

        final long documentTypeId = this.fromApiJsonHelper.extractLongNamed(DOCUMENT_TYPE_ID, element);
        baseDataValidator.reset().parameter(DOCUMENT_TYPE_ID).value(documentTypeId).notBlank().longGreaterThanZero();

        final String documentNumber = this.fromApiJsonHelper.extractStringNamed(DOCUMENT_NUMBER, element);
        baseDataValidator.reset().parameter(DOCUMENT_NUMBER).value(documentNumber).notNull().notBlank();

    }

    public void validateForUpdate(final long familyMemberId, String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(FAMILY_MEMBERS1);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        baseDataValidator.reset().value(familyMemberId).notBlank().integerGreaterThanZero();

        if (this.fromApiJsonHelper.extractStringNamed(FIRST_NAME, element) != null) {
            final String firstName = this.fromApiJsonHelper.extractStringNamed(FIRST_NAME, element);
            baseDataValidator.reset().parameter(FIRST_NAME).value(firstName).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed(LAST_NAME, element) != null) {
            final String lastName = this.fromApiJsonHelper.extractStringNamed(LAST_NAME, element);
            baseDataValidator.reset().parameter(LAST_NAME).value(lastName).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed(MIDDLE_NAME, element) != null) {
            final String middleName = this.fromApiJsonHelper.extractStringNamed(MIDDLE_NAME, element);
            baseDataValidator.reset().parameter(MIDDLE_NAME).value(middleName).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed(QUALIFICATION, element) != null) {
            final String qualification = this.fromApiJsonHelper.extractStringNamed(QUALIFICATION, element);
            baseDataValidator.reset().parameter(QUALIFICATION).value(qualification).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractLongNamed(RELATION_SHIP_ID, element) != null) {
            final long relationShipId = this.fromApiJsonHelper.extractLongNamed(RELATION_SHIP_ID, element);
            baseDataValidator.reset().parameter(RELATION_SHIP_ID).value(relationShipId).notBlank().longGreaterThanZero();

        }

        if (this.fromApiJsonHelper.extractLongNamed(MARITAL_STATUS_ID, element) != null) {
            final long maritalStatusId = this.fromApiJsonHelper.extractLongNamed(MARITAL_STATUS_ID, element);
            baseDataValidator.reset().parameter(MARITAL_STATUS_ID).value(maritalStatusId).notBlank().longGreaterThanZero();

        }

        if (this.fromApiJsonHelper.extractLongNamed(GENDER_ID, element) != null) {
            final long genderId = this.fromApiJsonHelper.extractLongNamed(GENDER_ID, element);
            baseDataValidator.reset().parameter(GENDER_ID).value(genderId).longGreaterThanZero();

        }

        if (this.fromApiJsonHelper.extractLongNamed(PROFESSION_ID, element) != null) {
            final long professionId = this.fromApiJsonHelper.extractLongNamed(PROFESSION_ID, element);
            baseDataValidator.reset().parameter(PROFESSION_ID).value(professionId).longGreaterThanZero();

        }

        if (this.fromApiJsonHelper.extractLocalDateNamed(DATE_OF_BIRTH, element) != null) {
            final LocalDate dateOfBirth = this.fromApiJsonHelper.extractLocalDateNamed(DATE_OF_BIRTH, element);
            baseDataValidator.reset().parameter(DATE_OF_BIRTH).value(dateOfBirth).validateDateBefore(DateUtils.getBusinessLocalDate());

        }

        if (this.fromApiJsonHelper.extractStringNamed(MOBILE_NUMBER, element) != null) {
            final String mobileNumber = this.fromApiJsonHelper.extractStringNamed(MOBILE_NUMBER, element);
            baseDataValidator.reset().parameter(MOBILE_NUMBER).value(mobileNumber).notNull().notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.extractStringNamed(EMAIL, element) != null) {
            final String email = this.fromApiJsonHelper.extractStringNamed(EMAIL, element);
            baseDataValidator.reset().parameter(EMAIL).value(email).notNull().notBlank();
        }

        if (this.fromApiJsonHelper.extractLongNamed(DOCUMENT_TYPE_ID, element) != null) {
            final long documentTypeId = this.fromApiJsonHelper.extractLongNamed(DOCUMENT_TYPE_ID, element);
            baseDataValidator.reset().parameter(DOCUMENT_TYPE_ID).value(documentTypeId).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractStringNamed(DOCUMENT_NUMBER, element) != null) {
            final String documentNumber = this.fromApiJsonHelper.extractStringNamed(DOCUMENT_NUMBER, element);
            baseDataValidator.reset().parameter(DOCUMENT_NUMBER).value(documentNumber).notNull().notBlank();
        }

        if (this.fromApiJsonHelper.extractStringNamed(ADDRESS, element) != null) {
            final String address = this.fromApiJsonHelper.extractStringNamed(ADDRESS, element);
            baseDataValidator.reset().parameter(ADDRESS).value(address).notNull().notBlank();
        }

        if (this.fromApiJsonHelper.extractBooleanNamed(IS_MARITAL_PARTNERSHIP, element) != null) {
            final Boolean isMaritalPartnership = this.fromApiJsonHelper.extractBooleanNamed(IS_MARITAL_PARTNERSHIP, element);
            baseDataValidator.reset().parameter(IS_MARITAL_PARTNERSHIP).value(isMaritalPartnership).notNull().notBlank().notExceedingLengthOf(100);
        }

    }

    public void validateForDelete(final long familyMemberId) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(FAMILY_MEMBERS1);

        // final JsonElement element = this.fromApiJsonHelper.parse(json);

        baseDataValidator.reset().value(familyMemberId).notBlank().integerGreaterThanZero();
    }

}
