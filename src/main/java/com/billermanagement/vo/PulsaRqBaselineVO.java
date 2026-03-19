/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 *
 * @author sulaeman
 */
@Data
public class PulsaRqBaselineVO {

    @JsonProperty("Method")
    private String method;
    @JsonProperty("ProductCode")
    private String productCode;
    @JsonProperty("PartnerId")
    private String partnerId;
    @JsonProperty("PassPin")
    private String passPin;
    @JsonProperty("RequestId")
    private String requestId;
    @JsonProperty("Params")
    private List<PulsaRqBaselineParamVo> params;
}
