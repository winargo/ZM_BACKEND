package com.billermanagement.vo.BNI;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OKMessage {

    @JsonProperty("message")
    private String message;
}
