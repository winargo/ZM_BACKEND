package com.billermanagement.vo.ui;

import com.billermanagement.vo.BaseVO;
import lombok.Data;

@Data
public class BillerPicVO extends BaseVO {
    private int billerId;
    private String team;
    private String name;
    private String contact;
    private String email;
    private Boolean status;
}
