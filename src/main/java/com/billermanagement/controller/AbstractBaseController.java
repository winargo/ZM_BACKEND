package com.billermanagement.controller;

import com.billermanagement.persistance.domain.Base;
import com.billermanagement.services.BaseService;
import com.billermanagement.util.RestUtil;
import com.billermanagement.vo.BaseVO;
import com.billermanagement.vo.ResultPageVO;
import com.billermanagement.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by yukibuwana on 1/24/17.
 */

@Slf4j
public abstract class AbstractBaseController<T extends Base, V extends BaseVO, Z> implements RestController<Z, ResultVO> {

    protected abstract BaseService<T, V, Z> getDomainService();

    @Override
    public ResponseEntity<ResultVO> add(@RequestBody Z voInput) {
        return RestUtil.getJsonResponse(null, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    public ResponseEntity<ResultVO> edit(@PathVariable("id") String secureId, @RequestBody Z voInput) {
        return RestUtil.getJsonResponse(null, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    public ResponseEntity<ResultVO> delete(@PathVariable("id") String secureId) {
        return RestUtil.getJsonResponse(null, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    public ResponseEntity<ResultVO> findById(@PathVariable("id") String secureId) {
        return RestUtil.getJsonResponse(null, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    public ResponseEntity<ResultPageVO> page(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                             @RequestParam(value = "limit", defaultValue = "10") Integer limit,
                                             @RequestParam(value = "sortBy", required = false) String sortBy,
                                             @RequestParam(value = "direction", required = false) String direction,
                                             @RequestParam(value = "searchBy", required = false) String searchBy,
                                             @RequestParam(value = "searchVal", required = false) String searchVal) {
        return RestUtil.getJsonResponse(null, HttpStatus.METHOD_NOT_ALLOWED);
    }

    protected ResponseEntity<ResultPageVO> constructListResult(Map<String, Object> pageMap) {
        return AbstractRequestHandler.constructListResult(pageMap);
    }

    @Override
    public ResponseEntity<ResultVO> list(@RequestParam(value = "sortBy", required = false) String sortBy,
                                         @RequestParam(value = "direction", required = false) String direction,
                                         @RequestParam(value = "searchBy", required = false) String searchBy,
                                         @RequestParam(value = "searchVal", required = false) String searchVal) {
        return RestUtil.getJsonResponse(null, HttpStatus.METHOD_NOT_ALLOWED);
    }

}
