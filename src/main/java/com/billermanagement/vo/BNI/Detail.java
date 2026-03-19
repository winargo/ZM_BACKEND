package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Detail {
    @JsonProperty("Fault_element")
    private FaultElement faultElement;

    @JsonProperty("AppFault_element3")
    private FaultElement appFaultElement;
}
