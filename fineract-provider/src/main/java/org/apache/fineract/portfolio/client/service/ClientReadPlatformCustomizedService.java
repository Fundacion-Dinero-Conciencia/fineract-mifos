package org.apache.fineract.portfolio.client.service;

import org.apache.fineract.portfolio.client.data.ClientInfoCustomizedDTO;

public interface ClientReadPlatformCustomizedService {

    ClientInfoCustomizedDTO getClientByid(Long id);
}
