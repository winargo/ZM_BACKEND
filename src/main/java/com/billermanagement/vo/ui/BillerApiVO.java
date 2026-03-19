package com.billermanagement.vo.ui;

import com.billermanagement.vo.BaseVO;
import lombok.Data;

@Data
public class BillerApiVO extends BaseVO {
    private int billerId;
    private int apiId;
    private String transformId;
    private String billerCode;
    private int billerPrice;
    private int priority;
    private int denom;
    private int adminFee;
    private String feeType;
    private Boolean status;
}
