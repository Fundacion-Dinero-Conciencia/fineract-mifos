package org.apache.fineract.portfolio.investmentproject.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestmentProjectAddressRepository extends JpaRepository<InvestmentProjectAddress, Long> {

    InvestmentProjectAddress getByInvestmentProject_id(Long investmentProjectId);
}
