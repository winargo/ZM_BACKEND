package com.billermanagement.vo.InstamoneyVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Address {

    @JsonProperty("country_code")
    private String countryCode;

//    @JsonProperty("province")
//    private String province;
//
//    @JsonProperty("city")
//    private String city;

    @JsonProperty("line_1")
    private String line1;
}
