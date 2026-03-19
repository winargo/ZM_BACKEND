package com.billermanagement.vo.ui;

import com.billermanagement.vo.BaseVO;
import lombok.Data;

import java.util.List;

@Data
public class PartnerApiVO extends BaseVO {
    private int partnerId;
    private int apiId;
    private int partnerPrice;
    private int partnerFee;
    private String feeType;
    private String url;
    private Boolean status;
    private List<Integer> billerApiId;
}
