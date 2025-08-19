package org.apache.fineract.portfolio.client.service;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.data.ClientInfoCustomizedDTO;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientAddress;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepository;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembers;
import org.apache.fineract.portfolio.client.domain.ClientFamilyMembersRepository;
import org.apache.fineract.portfolio.client.domain.ClientIdentifier;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientReadPlatformCustomizedServiceImpl implements ClientReadPlatformCustomizedService {

    private final ClientRepository clientRepository;
    private final ClientFamilyMembersRepository clientFamilyMembersRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final ClientReadPlatformService clientReadPlatformService;
    private final CodeValueRepository codeValueRepository;
    private final PlatformSecurityContext context;
    private final String CODE_ADDRESS_TYPE = "ADDRESS_TYPE";
    private final String CODE_VALUE_LEGAL_ADDRESS = "Domicilio Legal";

    @Override
    public ClientInfoCustomizedDTO getClientByid(Long id) {

        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        final Client client = clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
        List<ClientFamilyMembers> listFamilyMembers = clientFamilyMembersRepository.getClientFamilyMembersByClientId(id);
        final ClientFamilyMembers clientFamilyMember = listFamilyMembers.stream()
                .filter(item -> "Contacto".equals(item.getRelationship().getLabel()))
                .findFirst().orElse(null);

        final Optional<ClientIdentifier> identifier = client.getIdentifiers()
                .stream()
                .filter(item -> "RUT".equalsIgnoreCase(item.documentTypeLabel().trim()))
                .findFirst();
        final String rut = identifier.map(clientIdentifier -> clientIdentifier.documentKey().trim()).orElse("");
        final CodeValue codeValue = codeValueRepository.findByCodeNameAndLabel(CODE_ADDRESS_TYPE, CODE_VALUE_LEGAL_ADDRESS);
        final ClientAddress clientAddress = clientAddressRepository.findByClientIdAndAddressTypeAndIsActive(id, codeValue, true);
        Optional<Map<String, Object>> profiling = clientReadPlatformService.retrieveDataForDatatableProfilingByClientId(id).stream().findFirst();
        Optional<Map<String, Object>> bank;
        String occupation = null;
        String nationality = null;
        String maritalStatus = null;
        if (profiling.isPresent()) {
            if (profiling.get().containsKey("ocupation")) {
                occupation = profiling.get().get("ocupation") == null ? null : String.valueOf(profiling.get().get("ocupation"));
            }
            if (profiling.get().containsKey("nationality")) {
                nationality = profiling.get().get("nationality") == null ? null : String.valueOf(profiling.get().get("nationality"));
            }
            if (profiling.get().containsKey("maritalstatus")) {
                maritalStatus = profiling.get().get("maritalstatus") == null ? null : String.valueOf(profiling.get().get("maritalstatus"));
            }


        }

        if (Objects.equals(2, client.getLegalForm())) {
            bank = clientReadPlatformService.retrieveDataForDatatableBankByClientId(id, "COMP_BANK_DETAILS").stream().findFirst();
            String accountNo = null;
            String accountType = null;
            String bankName = null;


            if (bank.isPresent()) {
                if (bank.get().containsKey("noaccount")) {
                    accountNo = bank.get().get("noaccount") == null ? null : String.valueOf(bank.get().get("noaccount"));
                }
                if (bank.get().containsKey("tipocuenta")) {
                    accountType = bank.get().get("tipocuenta") == null ? null : String.valueOf(bank.get().get("tipocuenta"));
                }
                if (bank.get().containsKey("banco")) {
                    bankName = bank.get().get("banco") == null ? null : String.valueOf(bank.get().get("banco"));
                }

            }
            return new ClientInfoCustomizedDTO(client.getFullname(), client.getFancyName(), client.getEmailAddress(), client.getMobileNo(),
                    rut, clientAddress != null ? clientAddress.getAddress().getAddressLine1() : null, accountNo, bankName, accountType,
                    clientFamilyMember.getMobileNumber(), clientFamilyMember.getEmail(), clientFamilyMember.getLastName(),
                    clientFamilyMember.getFirstName(), occupation, null);
        } else {
            bank = clientReadPlatformService.retrieveDataForDatatableBankByClientId(id, "BANK_DETAILS").stream().findFirst();
            String accountNo = null;
            String accountType = null;
            String bankName = null;
            if (bank.isPresent()) {
                if (bank.get().containsKey("noaccount")) {
                    accountNo = bank.get().get("noaccount") == null ? null : String.valueOf(bank.get().get("noaccount"));
                }
                if (bank.get().containsKey("tipocuenta")) {
                    accountType = bank.get().get("tipocuenta") == null ? null : String.valueOf(bank.get().get("tipocuenta"));
                }
                if (bank.get().containsKey("banco")) {
                    bankName = bank.get().get("banco") == null ? null : String.valueOf(bank.get().get("banco"));
                }
            }
            return new ClientInfoCustomizedDTO(client.getFirstname(), client.getMiddlename(), client.getLastname(), client.getEmailAddress(), client.getMobileNo(),
                    rut, occupation, clientAddress != null ? clientAddress.getAddress().getAddressLine1(): null, accountNo, bankName, accountType,
                    client.getDateOfBirth().format(fmt), maritalStatus, nationality);
        }
    }
}
