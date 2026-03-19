package com.billermanagement.controller;

import com.billermanagement.vo.ResultPageVO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by yukibuwana on 1/24/17.
 */

public interface RestPageController {

    /**
     *
     * @param page
     *            : page of
     * @param limit
     *            : limit query
     * @param sortBy
     *            : sort by
     * @param direction
     *            : direction {asc:desc}
     * @param searchBy
     * @param searchVal
     * @return Collection of VO, Total-Count & Total Pages on response header
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<ResultPageVO> page(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                      @RequestParam(value = "limit", defaultValue = "10") Integer limit,
                                      @RequestParam(value = "sortBy", required = false) String sortBy,
                                      @RequestParam(value = "direction", required = false) String direction,
                                      @RequestParam(value = "searchBy", required = false) String searchBy,
                                      @RequestParam(value = "searchVal", required = false) String searchVal);

}
