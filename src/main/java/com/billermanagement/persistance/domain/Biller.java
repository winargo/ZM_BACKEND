package com.billermanagement.persistance.domain;

import com.billermanagement.vo.ui.BillerVO;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "BILLER")
@DynamicUpdate
@Data
public class Biller extends Base {
    @Column(name = "BILLER_ALIAS", length = 16, unique = true, nullable = false)
    private String billerAlias;

    @Column(name = "BILLER_NAME", length = 32)
    private String billerName;

    @Column(name = "BILLER_TYPE", length = 8)
    private String billerType;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "DEED_EST_NO", length = 16)
    private String deedEstNo;

    @Column(name = "TIN_NO", length = 16)
    private String tinNo;

    @Column(name = "NIB_SIUP_TDP_NO", length = 16)
    private String nibSiupTdpNo;

    @Column(name = "DEPOSIT_ACCOUNT", length = 16)
    private String depositAccount;

    @Column(name = "DEPOSIT_BANK_NAME", length = 16)
    private String depositBankName;

    @Column(name = "DEPOSIT_BRANCH", length = 32)
    private String depositBranch;

    @Column(name = "DEPOSIT_VA", length = 16)
    private String depositVA;

    @Column(name = "RECON_SFTP_IP", length = 15)
    private String reconSftpIp;

    @Column(name = "RECON_SFTP_PORT", columnDefinition="SMALLINT")
    private int reconSftpPort;

    @Column(name = "RECON_SFTP_FOLDER", length = 128)
    private String reconSftpFolder;

    @Column(name = "RECON_EMAIL", length = 32)
    private String reconEmail;

    @Column(name = "ATTACH_PKS")
    private String pks;

    @Column(name = "ATTACH_API")
    private String api;

    @Column(name = "STATUS", columnDefinition="BOOLEAN", nullable = false)
    private boolean status;

    public Biller() { }

    public Biller(BillerVO vo){
        setValue(vo);
    }

    public void update(BillerVO vo){
        setValue(vo);
    }

    private void setValue(BillerVO vo) {
        billerAlias = vo.getBillerAlias();
        billerName = vo.getBillerName();
        billerType = vo.getBillerType();
        address = vo.getAddress();
        deedEstNo = vo.getDeedEstNo();
        tinNo = vo.getTinNo();
        nibSiupTdpNo = vo.getNibSiupTdpNo();
        depositAccount = vo.getDepositAccount();
        depositBankName = vo.getDepositBankName();
        depositBranch = vo.getDepositBranch();
        depositVA = vo.getDepositVA();
        reconSftpIp = vo.getReconSftpIp();
        reconSftpPort = vo.getReconSftpPort();
        reconSftpFolder = vo.getReconSftpFolder();
        reconEmail = vo.getReconEmail();
        pks = vo.getPks();
        api = vo.getApi();
    }

    @Override
    public void prePersist() {
        this.status = true;
        super.prePersist();
    }
}
