package com.billermanagement.vo.EbayVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Error {

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("fieldName")
    private String fieldName;

    @JsonProperty("message")
    private String message;

    @JsonProperty("meta")
    private Meta meta;
}
