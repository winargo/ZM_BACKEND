package com.billermanagement.vo.EbayVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Sender {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("BornDate")
    private String bornDate;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("City")
    private String city;

    @JsonProperty("CountryID")
    private String countryID;

    @JsonProperty("IdentificationType")
    private String idType;

    @JsonProperty("IdentificationNumber")
    private String idNumber;
}
