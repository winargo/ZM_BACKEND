package com.billermanagement.persistance.domain;

import com.billermanagement.vo.ui.PartnerVO;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "PARTNER")
@DynamicUpdate
@Data
public class Partner extends Base {
    @Column(name = "PARTNER_ALIAS", length = 16, unique = true, nullable = false)
    private String alias;

    @Column(name = "PARTNER_NAME", length = 32)
    private String name;

    @Column(name = "PARTNER_ADDRESS")
    private String address;

    @Column(name = "DEED_EST_NO", length = 16)
    private String deedEstNo;

    @Column(name = "TIN_NO", length = 16)
    private String tinNo;

    @Column(name = "NIB_SIUP_TDP_NO", length = 16)
    private String nibSiupTdpNo;

    @Column(name = "PIC_NAME", length = 32)
    private String picName;

    @Column(name = "PIC_ADDRESS")
    private String picAddress;

    @Column(name = "PIC_ID_NUMBER", length = 16)
    private String picIdNumber;

    @Column(name = "PIC_TIN_NUMBER", length = 16)
    private String picTinNumber;

    @Column(name = "PIC_PHONE_NUMBER", length = 16)
    private String picPhoneNumber;

    @Column(name = "PIC_EMAIL", length = 32)
    private String picEmail;

    @Column(name = "COOP_BANK_NAME", length = 16)
    private String coopBankName;

    @Column(name = "COOP_ACCOUNT_NUMBER", length = 16)
    private String coopAccountNumber;

    @Column(name = "COOP_ACCOUNT_NAME", length = 32)
    private String coopAccountName;

    @Column(name = "COOP_SETTLEMENT_PERIOD", length = 8)
    private String coopSettlementPeriod;

    @Column(name = "COOP_PERIOD", length = 16)
    private String coopPeriod;

    @Column(name = "COOP_TYPE", length = 10)
    private String coopType;

    @Column(name = "COOP_NOMINAL", columnDefinition="INT")
    private int coopNominal;

    @Column(name = "ATTACH_INCORPORATION_DEED")
    private String attachIncorporationDeed;

    @Column(name = "ATTACH_SK_KEMENKUMHAM")
    private String attachSkKemenkumham;

    @Column(name = "ATTACH_AMENDMENT_DEED")
    private String attachAmendmentDeed;

    @Column(name = "ATTACH_TIN")
    private String attachTin;

    @Column(name = "ATTACH_NIB")
    private String attachNib;

    @Column(name = "ATTACH_PIC")
    private String attachPic;

    @Column(name = "ATTACH_STATEMENT_LETTER")
    private String attachStatementLetter;

    @Column(name = "ATTACH_BUSINESS_PHOTO")
    private String attachBusinessPhoto;

    @Column(name = "STATUS", columnDefinition="BOOLEAN", nullable = false)
    private boolean status;

    public Partner() { }

    public Partner(PartnerVO vo) {
        setValue(vo);
    }

    public void update(PartnerVO vo){
        setValue(vo);
    }

    private void setValue(PartnerVO vo) {
        alias = vo.getAlias();
        name = vo.getName();
        address = vo.getAddress();
        deedEstNo = vo.getDeedEstNo();
        tinNo = vo.getTinNo();
        nibSiupTdpNo = vo.getNibSiupTdpNo();
        picName = vo.getPicName();
        picAddress = vo.getPicAddress();
        picIdNumber = vo.getPicIdNumber();
        picTinNumber = vo.getPicTinNumber();
        picPhoneNumber = vo.getPicPhoneNumber();
        picEmail = vo.getPicEmail();
        coopBankName = vo.getCoopBankName();
        coopAccountNumber = vo.getCoopAccountNumber();
        coopAccountName = vo.getCoopAccountName();
        coopSettlementPeriod = vo.getCoopSettlementPeriod();
        coopPeriod = vo.getCoopPeriod();
        coopType = vo.getCoopType();
        coopNominal = vo.getCoopNominal();
        attachIncorporationDeed = vo.getAttachIncorporationDeed();
        attachSkKemenkumham = vo.getAttachSkKemenkumham();
        attachAmendmentDeed = vo.getAttachAmendmentDeed();
        attachTin = vo.getAttachTin();
        attachNib = vo.getAttachNib();
        attachPic = vo.getAttachPic();
        attachStatementLetter = vo.getAttachStatementLetter();
        attachBusinessPhoto = vo.getAttachBusinessPhoto();
    }

    @Override
    public void prePersist() {
        this.status = true;
        super.prePersist();
    }
}
