package com.billermanagement.exception;

import com.billermanagement.enums.StatusCode;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Data
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadArgumentException extends RuntimeException {

	private StatusCode code = StatusCode.CODE_NOT_FOUND;

	public BadArgumentException(String message) {
		super(message);
	}

	public BadArgumentException(String message, StatusCode code) {
		super(message);
		this.code = code;
	}
}
