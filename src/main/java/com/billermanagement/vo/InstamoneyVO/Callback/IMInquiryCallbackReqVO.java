package com.billermanagement.vo.InstamoneyVO.Callback;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IMInquiryCallbackReqVO {
    @JsonProperty("status")
    private String status;

    @JsonProperty("bank_account_number")
    private String bank_account_number;

    @JsonProperty("bank_code")
    private String bank_code;

    @JsonProperty("updated")
    private String updated;

    @JsonProperty("bank_account_holder_name")
    private String bank_account_holder_name;

    @JsonProperty("is_normal_account")
    private Boolean is_normal_account;

    @JsonProperty("id")
    private String id;
}
