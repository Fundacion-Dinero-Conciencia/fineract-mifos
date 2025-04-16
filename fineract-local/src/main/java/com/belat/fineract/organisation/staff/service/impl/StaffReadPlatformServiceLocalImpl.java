package com.belat.fineract.organisation.staff.service.impl;

import com.belat.fineract.organisation.staff.domain.StaffRepositoryLocal;
import com.belat.fineract.organisation.staff.service.StaffReadPlatformServiceLocal;
import java.util.Optional;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StaffReadPlatformServiceLocalImpl implements StaffReadPlatformServiceLocal {

    @Autowired
    private StaffRepositoryLocal repository;

    @Override
    public Staff getById(Long staffId) {
        final Optional<Staff> staff = this.repository.findById(staffId);
        if (staff.isEmpty()) {
            throw new StaffNotFoundException(staffId);
        }
        return staff.get();
    }

}
