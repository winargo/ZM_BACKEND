package com.billermanagement.vo.EbayVO.GetRecepientName;

import com.billermanagement.vo.EbayVO.Authentication;
import com.billermanagement.vo.EbayVO.Recepient;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EbayGetRecepientNameReqVO {

    @JsonProperty("Authentication")
    private Authentication authentication;

    @JsonProperty("Recepient")
    private Recepient recepient;
}
