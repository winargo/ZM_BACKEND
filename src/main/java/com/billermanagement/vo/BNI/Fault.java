package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Fault {
    @JsonProperty("faultcode")
    private String faultcode;

    @JsonProperty("faultstring")
    private String faultstring;

    @JsonProperty("detail")
    private Detail detail;
}
