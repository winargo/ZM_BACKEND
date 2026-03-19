package com.billermanagement.vo.EbayVO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Authentication {

    @JsonProperty("UserName")
    private String userName;

    @JsonProperty("Password")
    private String password;
}
