package com.billermanagement.vo.EbayVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Meta {

    @JsonProperty("String")
    private String string;
}
