package com.billermanagement.vo.EbayVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Recepient {
    @JsonProperty("BankId")
    private String bankID;

    @JsonProperty("AccountNumber")
    private String accountNumber;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Msisdn")
    private String msisdn;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("CountryID")
    private String countryId;
}
