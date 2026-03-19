package com.billermanagement.vo.ui;

import com.billermanagement.vo.BaseVO;
import lombok.Data;

@Data
public class PartnerVO extends BaseVO {
    private String alias;
    private String name;
    private String address;
    private String deedEstNo;
    private String tinNo;
    private String nibSiupTdpNo;
    private String picName;
    private String picAddress;
    private String picIdNumber;
    private String picTinNumber;
    private String picPhoneNumber;
    private String picEmail;
    private String coopBankName;
    private String coopAccountNumber;
    private String coopAccountName;
    private String coopSettlementPeriod;
    private String coopPeriod;
    private String coopType;
    private int coopNominal;
    private String attachIncorporationDeed;
    private String attachSkKemenkumham;
    private String attachAmendmentDeed;
    private String attachTin;
    private String attachNib;
    private String attachPic;
    private String attachStatementLetter;
    private String attachBusinessPhoto;
    private Boolean status;
    private String username;
}
