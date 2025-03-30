package com.belat.fineract.organisation.staff.service;

import org.apache.fineract.organisation.staff.domain.Staff;

public interface StaffReadPlatformServiceLocal {

    Staff getById(Long staffId);
}
