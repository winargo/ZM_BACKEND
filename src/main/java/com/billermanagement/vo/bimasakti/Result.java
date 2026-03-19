/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.vo.bimasakti;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author sulaeman
 */
@Data
public class Result{
    @JsonProperty("IDTRANSAKSI")
    private String idtransaksi;
    @JsonProperty("TRANSAKSIDATETIME")
    private String transaksidatetime;
    @JsonProperty("KODEPRODUK")
    private String kodeproduk;
    @JsonProperty("IDPELANGGAN1")
    private String idpelanggan1;
    @JsonProperty("IDPELANGGAN2")
    private String idpelanggan2;
    @JsonProperty("NOMINAL")
    private String nominal;
    @JsonProperty("NOMINALADMIN")
    private String nominaladmin;
    @JsonProperty("SN")
    private String sn;
}
