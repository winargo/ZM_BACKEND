package com.billermanagement.controller;

import com.billermanagement.exception.NostraException;
import com.billermanagement.util.RestUtil;
import com.billermanagement.vo.ResultPageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

/**
 * Created by yukibuwana on 1/24/17.
 */

@Slf4j
public abstract class AbstractRequestPageHandler {

    public ResponseEntity<ResultPageVO> getResult() {
        ResultPageVO result = new ResultPageVO();
        try {
            return processRequest();
        } catch (NostraException e) {
            result.setMessage(e.getCode().name());
            result.setResult(e.getMessage());

            log.error("ERROR", e);
        }
        return RestUtil.getJsonResponse(result);
    }

    public abstract ResponseEntity<ResultPageVO> processRequest();
}
