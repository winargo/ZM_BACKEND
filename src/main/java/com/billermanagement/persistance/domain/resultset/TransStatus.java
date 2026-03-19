package com.billermanagement.persistance.domain.resultset;

public interface TransStatus {
    String getTransId();
    String getRequestId();
    String getStatus();
    String getDatetime();
}
