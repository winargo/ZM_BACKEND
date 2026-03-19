package com.billermanagement.persistance.domain.resultset;

public interface PartnerProduct {
    //select a.API_CATEGORY, a.API_ID, a.API_NAME, a.API_DESCRIPTION, a.NOMINAL, b.PARTNER_PRICE, b.PARTNER_FEE
    String getCategory();
    String getProductCode();
    String getProductName();
    String getProductDesc();
    int getNominal();
    int getPartnerPrice();
    int getPartnerFee();
}
