package com.billermanagement.vo.ui;

import com.billermanagement.vo.BaseVO;
import lombok.Data;

@Data
public class BillerListVO extends BaseVO {
    private int partnerApiId;
    private int billerId;
    private int priority;
    private int billerApiId;
}
