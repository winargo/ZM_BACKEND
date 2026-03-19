package com.billermanagement.controller;

import com.billermanagement.vo.ResultVO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by yukibuwana on 1/24/17.
 */
public interface RestController<Z, R extends ResultVO> extends RestPageController {

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<R> add(@RequestBody Z vo);

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<R> edit(@PathVariable("id") String secureId, @RequestBody Z vo);

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<R> delete(@PathVariable("id") String secureId);

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<R> findById(@PathVariable("id") String secureId);

    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<R> list(@RequestParam(value = "sortBy", required = false) String sortBy,
                           @RequestParam(value = "direction", required = false) String direction,
                           @RequestParam(value = "searchBy", required = false) String searchBy,
                           @RequestParam(value = "searchVal", required = false) String searchVal);

}
