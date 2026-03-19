/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.vo.MobilePulsa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author sulaeman
 */
@Data
public class MobilePulsaStatusVO {

    @JsonProperty("commands")
    private String commands;
    @JsonProperty("username")
    private String username;
    @JsonProperty("ref_id")
    private String ref_id;
    @JsonProperty("sign")
    private String sign;
}
