/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.vo.BTN;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author sulaeman
 */
@Data
public class BPJSTKPUCekStatusVO {

    @JsonProperty("refnum")
    private String refnum;
    @JsonProperty("findreqId")
    private String findreqId;
    @JsonProperty("findnoTagihan")
    private String findnoTagihan;
    @JsonProperty("trxdate")
    private String trxdate;
}
