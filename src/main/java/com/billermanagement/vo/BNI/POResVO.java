package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class POResVO {
    @JsonProperty("processPOResponse")
    private POResponse token;

    @JsonProperty("Fault")
    private Fault fault;
}
