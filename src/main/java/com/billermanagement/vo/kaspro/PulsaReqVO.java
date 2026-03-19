package com.billermanagement.vo.kaspro;

import lombok.Data;

@Data
public class PulsaReqVO extends KasproVO {
    private String method;
    private String uid;
    private String pin;
    private String kode_produk;
    private String no_hp;
    private String ref1;
    private String partnerId;

    /*public Integer getPartnerId() {
        return partnerId;
    }*/
/*
    public String getMethod() {
        return method;
    }

    public String getKodeProduk() {
        return kode_produk;
    }

    public void setKodeProduk(String kode_produk) {
        this.kode_produk = kode_produk;
    }*/
}
