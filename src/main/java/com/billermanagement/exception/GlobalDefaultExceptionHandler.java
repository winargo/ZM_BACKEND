package com.billermanagement.exception;

import com.billermanagement.config.Messages;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yukibuwana on 1/25/17.
 */

@ControllerAdvice
@Slf4j
public class GlobalDefaultExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    Messages messages;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> defaultErrorHandler(HttpServletRequest req, Exception e) {

        log.error("Caused by: " + e.getClass().getName());
        log.error("ERROR", e);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ResultVO restResponseVO = new ResultVO();
        restResponseVO.setResult(e.getMessage());
        restResponseVO.setMessage(StatusCode.ERROR.name());

        return new ResponseEntity<>(restResponseVO, status);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<Object> handleDataNotFoundException(HttpServletRequest req, Exception e) {

        log.error("ERROR", e);

        HttpStatus status = HttpStatus.NOT_FOUND;

        ResultVO restResponseVO = new ResultVO();
        restResponseVO.setResult(e.getMessage());
        restResponseVO.setMessage(StatusCode.DATA_NOT_FOUND.name());

        return new ResponseEntity<>(restResponseVO, status);
    }

    @ExceptionHandler(BadArgumentException.class)
    public ResponseEntity<Object> handleBadArgumentException(HttpServletRequest req, Exception e) {

        log.error("ERROR", e);

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResultVO restResponseVO = new ResultVO();
        restResponseVO.setResult(e.getMessage());
        restResponseVO.setMessage(StatusCode.DATA_INTEGRITY.name());

        return new ResponseEntity<>(restResponseVO, status);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityException(HttpServletRequest req, Exception e) {

        log.error("ERROR", e);

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResultVO restResponseVO = new ResultVO();
        restResponseVO.setResult(messages.get("error.duplicate"));
        restResponseVO.setMessage(StatusCode.DATA_INTEGRITY.name());

        return new ResponseEntity<>(restResponseVO, status);
    }

    //TODO: handleUnAuthorizedException

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {

        log.error("ERROR", ex);

        status = HttpStatus.BAD_REQUEST;

        List<String> details = new ArrayList<>();
        for(ObjectError error : ex.getBindingResult().getAllErrors()) {
            details.add(error.getDefaultMessage());
        }

        ResultVO restResponseVO = new ResultVO();
        restResponseVO.setResult(details.toString());
        restResponseVO.setMessage(StatusCode.BAD_REQUEST.name());

        return new ResponseEntity<>(restResponseVO, status);
    }
}
