package com.recruitment.service;

import com.recruitment.entity.Application;
import com.recruitment.entity.Offer;
import com.recruitment.mapper.ApplicationMapper;
import com.recruitment.mapper.ApplicationStatusHistoryMapper;
import com.recruitment.mapper.OfferMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfferExpirationService {

    @Autowired
    private OfferMapper offerMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private ApplicationStatusHistoryMapper historyMapper;

    @Transactional
    public int expirePendingOffers() {
        int historyCount = historyMapper.insertExpiredOfferHistories(
                Application.STATUS_OFFER_PENDING,
                Application.STATUS_OFFER_EXPIRED,
                Offer.STATUS_PENDING);
        int applicationCount = applicationMapper.expireApplicationsForExpiredOffers(
                Application.STATUS_OFFER_PENDING,
                Application.STATUS_OFFER_EXPIRED,
                Offer.STATUS_PENDING);
        int offerCount = offerMapper.expirePendingOffers(Offer.STATUS_PENDING, Offer.STATUS_EXPIRED);
        return Math.max(Math.max(historyCount, applicationCount), offerCount);
    }
}
