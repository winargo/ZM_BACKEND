package com.billermanagement.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by yukibuwana on 1/24/17.
 */

@Data
@AllArgsConstructor
public class ResultVO {

    private String message;
    private Object result;

    public ResultVO() { }
}
