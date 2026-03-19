/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.vo.bimasakti;

import lombok.Data;

/**
 *
 * @author sulaeman
 */
@Data
public class Status {
    private String tanggal;
    private String ref1;
    private String ref2;
    private String kodeproduk;
    private String idpel1;
    private String idpel2;
    private String denom;
    private String uid;
    private String pin;
    private String status;
    private String keterangan;
    private Result result;
}
