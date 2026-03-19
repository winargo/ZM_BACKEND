/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.services.bimasakti;

import com.billermanagement.vo.bimasakti.Result;
import com.billermanagement.vo.bimasakti.Status;
import org.springframework.stereotype.Service;

/**
 *
 * @author sulaeman
 */
@Service
public class StatusService {
    
    public Status getStatus(){
        
        Result r = new Result();
        r.setIdtransaksi("2114941856");
        r.setTransaksidatetime("2021-05-03 10:27:31.468067");
        r.setKodeproduk("S5H");
        r.setIdpelanggan1("081218397347");
        r.setIdpelanggan2("");
        r.setNominal("5800");
        r.setNominaladmin("0");
        r.setSn("0061003653316912");
        
        Status s = new Status();
        s.setTanggal("20210503");
        s.setRef1("8121839734720210503102729407");
        s.setRef2("2114941856");
        s.setKodeproduk("S5H");
        s.setIdpel1("081218397347");
        s.setIdpel2("");
        s.setDenom("5800");
        s.setUid("HH124952");
        s.setPin("");
        s.setStatus("00");
        s.setKeterangan("Pembelian voucher pulsa S5H berhasil ke no 081218397347. Kode Voucher: 0061003653316912.");
        s.setResult(r);
        return s;
    }
}
