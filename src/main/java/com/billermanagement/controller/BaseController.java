package com.billermanagement.controller;

import com.billermanagement.persistance.domain.Base;
import com.billermanagement.vo.BaseVO;
import com.billermanagement.vo.ResultPageVO;
import com.billermanagement.vo.ResultVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Map;

/**
 * Created by yukibuwana on 1/24/17.
 */

public abstract class BaseController<T extends Base, V extends BaseVO, Z> extends AbstractBaseController<T, V, Z> {

    @Override
    public ResponseEntity<ResultVO> add(@Valid @RequestBody final Z voInput) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {

                V vo = getDomainService().add(voInput);
                return vo == null ? null : vo.getId();
            }
        };
        return handler.getResult();
    }

    @Override
    public ResponseEntity<ResultVO> edit(@PathVariable("id") final String secureId, @RequestBody final Z voInput) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return getDomainService().update(secureId, voInput);
            }
        };
        return handler.getResult();
    }

    @Override
    public ResponseEntity<ResultVO> delete(@PathVariable("id") final String secureId) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return getDomainService().delete(secureId);
            }
        };
        return handler.getResult();
    }

    @Override
    public ResponseEntity<ResultVO> findById(@PathVariable("id") final String secureId) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return getDomainService().findBySecureId(secureId);
            }
        };
        return handler.getResult();
    }

    @Override
    public ResponseEntity<ResultPageVO> page(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                             @RequestParam(value = "limit", defaultValue = "10") Integer limit,
                                             @RequestParam(value = "sortBy", required = false) String sortBy,
                                             @RequestParam(value = "direction", required = false) String direction,
                                             @RequestParam(value = "searchBy", required = false) String searchBy,
                                             @RequestParam(value = "searchVal", required = false) String searchVal) {

        Map<String, Object> pageMap = getDomainService().search(page, limit, sortBy, direction, searchBy, searchVal);
        return constructListResult(pageMap);
    }

    @Override
    public ResponseEntity<ResultVO> list(@RequestParam(value = "sortBy", required = false) String sortBy,
                                         @RequestParam(value = "direction", required = false) String direction,
                                         @RequestParam(value = "searchBy", required = false) String searchBy,
                                         @RequestParam(value = "searchVal", required = false) String searchVal) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return getDomainService().search(sortBy, direction, searchBy, searchVal);
            }
        };
        return handler.getResult();
    }

}
