package com.billermanagement.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by yukibuwana on 1/24/17.
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseVO implements Serializable {

    /**
     * Secure ID / UUID
     */
    private String id;
    private Integer version;
}
