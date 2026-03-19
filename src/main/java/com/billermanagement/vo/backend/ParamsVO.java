package com.billermanagement.vo.backend;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParamsVO {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Value")
    private String value;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("Desc")
    private String desc;

    @JsonProperty("Nominal")
    private Integer nominal;

    @JsonProperty("Price")
    private Integer price;

    @JsonProperty("Fee")
    private Integer fee;

    @JsonProperty("Category")
    private String category;
}
