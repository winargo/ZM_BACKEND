package com.billermanagement.vo.EbayVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ResponseStatus {

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("stackTrace")
    private String stackTrace;

    @JsonProperty("error")
    private List<Error> errors;

    @JsonProperty("meta")
    private Meta meta;
}
