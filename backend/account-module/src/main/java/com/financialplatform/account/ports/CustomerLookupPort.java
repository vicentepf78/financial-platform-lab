package com.financialplatform.account.ports;

import com.financialplatform.sharedkernel.domain.Identifier;

public interface CustomerLookupPort {

    boolean exists(Identifier customerId);
}
