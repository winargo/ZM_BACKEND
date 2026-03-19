package com.billermanagement.controller.ui;

import com.billermanagement.controller.AbstractRequestHandler;
import com.billermanagement.services.ui.ApiService;
import com.billermanagement.vo.ResultVO;
import com.billermanagement.vo.ui.ApiVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gui/api")
public class ApiController {
    @Autowired
    private ApiService service;

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/create")
    @ResponseBody
    public ResponseEntity<ResultVO> create(@RequestBody final ApiVO vo) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.create(vo);
            }
        };

        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/update")
    @ResponseBody
    public ResponseEntity<ResultVO> update(@RequestBody final ApiVO vo) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.update(vo);
            }
        };

        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/delete")
    @ResponseBody
    public ResponseEntity<ResultVO> delete(@RequestBody final ApiVO vo) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.delete(vo);
            }
        };

        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/getAll")
    @ResponseBody
    public ResponseEntity<ResultVO> findAll() {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.findAll();
            }
        };

        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/getById")
    @ResponseBody
    public ResponseEntity<ResultVO> findById(@RequestParam(value="id") int id) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.findById(id);
            }
        };

        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/getByApiId")
    @ResponseBody
    public ResponseEntity<ResultVO> findByApiId(@RequestParam(value="id") String id) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.findByApiId(id);
            }
        };

        return handler.getResult();
    }

    @GetMapping(value = "/getByCategory/{category}", produces = "application/json")
    public ResponseEntity<ResultVO> findByBillerIdAndCategory(@PathVariable(value = "category") String category) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.findByApiCategory(category);
            }
        };
        return handler.getResult();
    }

    @GetMapping(value = "/getCategory", produces = "application/json")
    public ResponseEntity<ResultVO> findByBillerIdAndCategory() {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.findDistinctCategory();
            }
        };
        return handler.getResult();
    }
}
