/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author sulaeman
 */
@Data
public class PulsaRqBaselineParamVo {

    @JsonProperty("Name")
    private String name;
    @JsonProperty("Value")
    private String value;
}
