package com.billermanagement.vo.InstamoneyVO.ValidateBankName;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IMValidateBankNameResVO {

    @JsonProperty("status")
    private String status;

    @JsonProperty("bank_account_number")
    private String bankAccNumber;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("updated")
    private String updated;

    @JsonProperty("name_matching_result")
    private String nameMatchingResult;

    @JsonProperty("is_normal_account")
    private Boolean isNormalAccount;

    @JsonProperty("id")
    private String id;
}
