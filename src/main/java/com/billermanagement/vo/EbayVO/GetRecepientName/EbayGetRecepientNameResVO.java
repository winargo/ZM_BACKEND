package com.billermanagement.vo.EbayVO.GetRecepientName;

import com.billermanagement.vo.EbayVO.Recepient;
import com.billermanagement.vo.EbayVO.ResponseStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EbayGetRecepientNameResVO {
    @JsonProperty("Recepient")
    private Recepient recepient;

    @JsonProperty("responseStatus")
    private ResponseStatus responseStatus;
}
