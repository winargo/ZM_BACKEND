package com.billermanagement.persistance.domain.resultset;

import java.util.Date;

public interface TransHistoryResult {
    int getId();
    Date getCreationDate();
    String getBmTid();
    String getStatus();

    String getPartnerTid();
    int getPartnerId();
    String getPartnerName();
    String getPartnerCode();
    int getPartnerPrice();
    int getPartnerFee();

    String getBillerTid();
    int getBillerId();
    String getBillerName();
    String getBillerCode();
    int getBillerPrice();
    int getBillerFee();
}
