package com.billermanagement.vo.backend;

import com.billermanagement.vo.BaseVO;
import lombok.Data;

import javax.persistence.Column;

@Data
public class TransTmpVO extends BaseVO {
    private String partnerTid;
    private String partnerCode;
    private String billerTid;
    private String transformId;
    private int status;
}
