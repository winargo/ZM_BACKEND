package com.billermanagement.persistance.domain.resultset;

public interface BillerResult {
    int getId();
    int getBillerId();
    String getBillerCode();
    String getTransformId();
    int getPriority();
    int getBillerPrice();
    int getPartnerId();
    int getApiId();
    int getAdminFee();

    void setPartnerId(int id);
}
