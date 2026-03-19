package com.billermanagement.vo.ui;

import com.billermanagement.vo.BaseVO;
import lombok.Data;

@Data
public class ApiVO extends BaseVO {
    private String apiId;
    private String apiName;
    private String apiDescription;
    private String apiCategory;
    private String apiSelection;
    private int nominal;
}
