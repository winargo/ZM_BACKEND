package com.billermanagement.exception;

import com.billermanagement.enums.StatusCode;
import lombok.Data;

/**
 * Created by yukibuwana on 1/24/17.
 */

@Data
public class NostraException extends RuntimeException {

    private StatusCode code = StatusCode.ERROR;

    public NostraException(String message) {
        super(message);
    }

    public NostraException(String message, StatusCode code) {
        super(message);
        this.code = code;
    }
}
