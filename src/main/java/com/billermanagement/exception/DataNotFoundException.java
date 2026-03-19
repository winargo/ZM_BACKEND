package com.billermanagement.exception;

import com.billermanagement.enums.StatusCode;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Data
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DataNotFoundException extends RuntimeException {

	private StatusCode code = StatusCode.DATA_NOT_FOUND;

	public DataNotFoundException(String message) {
		super(message);
	}

	public DataNotFoundException(String message, StatusCode code) {
		super(message);
		this.code = code;
	}
}
