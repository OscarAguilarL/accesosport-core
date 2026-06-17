package com.accesosport.event.application.policy;

import com.accesosport.event.domain.exception.OrganizerNotVerifiedException;
import com.accesosport.event.domain.exception.OrganizerStripeNotReadyException;
import com.accesosport.user.domain.model.UserOrganizerProfile;

public class PaidEventEligibilityPolicy {

    public void assertCanOfferPaidModalities(UserOrganizerProfile organizer) {
        if (!organizer.isVerified()) {
            throw new OrganizerNotVerifiedException();
        }
        if (!organizer.isStripeLinked()) {
            throw new OrganizerStripeNotReadyException();
        }
    }
}
