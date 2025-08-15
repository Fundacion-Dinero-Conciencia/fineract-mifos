package org.apache.fineract.portfolio.client.domain;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientReadPlatformRepository extends JpaRepository<Client, Long> {

}
