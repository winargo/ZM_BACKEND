package com.billermanagement.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AdditionalInfoVO {
    @JsonProperty("TransactionId")
    private String transactionId;

    @JsonProperty("PartnerPrice")
    private int partnerPrice;

    @JsonProperty("BillerPrice")
    private int billerPrice;

    @JsonProperty("AdminFee")
    private int adminFee;

    @JsonProperty("Time")
    private String time;

    @JsonProperty("BillerUid")
    private String billerUid;

    @JsonProperty("BillerPass")
    private String billerPass;
    
    @JsonProperty("Sign")
    private String sign;
}
