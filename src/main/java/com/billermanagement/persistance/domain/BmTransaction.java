package com.billermanagement.persistance.domain;

import javax.persistence.Column;

public class BmTransaction extends Base {
    @Column(name = "API_ID", length = 8)
    private String alias;

    @Column(name = "PARTNER_ID", length = 32)
    private String name;

    @Column(name = "PARTNER_TID")
    private String address;

    @Column(name = "DEED_EST_NO", length = 16)
    private String deedEstNo;
}
