package com.recruitment.task;

import com.recruitment.service.OfferExpirationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OfferExpirationTask {

    @Autowired
    private OfferExpirationService offerExpirationService;

    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void expirePendingOffers() {
        offerExpirationService.expirePendingOffers();
    }
}
