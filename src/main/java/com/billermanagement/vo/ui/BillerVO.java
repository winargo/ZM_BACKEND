package com.billermanagement.vo.ui;

import com.billermanagement.vo.BaseVO;
import lombok.Data;

@Data
public class BillerVO extends BaseVO {
    private String billerAlias;
    private String billerName;
    private String billerType;
    private String address;
    private String deedEstNo;
    private String tinNo;
    private String nibSiupTdpNo;
    private String depositAccount;
    private String depositBankName;
    private String depositBranch;
    private String depositVA;
    private String reconSftpIp;
    private int reconSftpPort;
    private String reconSftpFolder;
    private String reconEmail;
    private String pks;
    private String api;
    private Boolean status;
    private String username;
}
