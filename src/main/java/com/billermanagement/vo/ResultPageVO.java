package com.billermanagement.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by yukibuwana on 1/24/17.
 */

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultPageVO extends ResultVO {

    private Integer pages;
    private Integer elements;

    public ResultPageVO() { }
}
