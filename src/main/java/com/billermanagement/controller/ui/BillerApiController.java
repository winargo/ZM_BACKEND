package com.billermanagement.controller.ui;

import com.billermanagement.controller.AbstractRequestHandler;
import com.billermanagement.services.ui.BillerApiService;
import com.billermanagement.vo.ResultVO;
import com.billermanagement.vo.ui.BillerApiVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gui/billerApi")
public class BillerApiController {
    @Autowired
    BillerApiService service;

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/create")
    @ResponseBody
    public ResponseEntity<ResultVO> create(@RequestBody final BillerApiVO vo) {
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
    public ResponseEntity<ResultVO> update(@RequestBody final BillerApiVO vo) {
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
    public ResponseEntity<ResultVO> delete(@RequestBody final BillerApiVO vo) {
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
            value="/getByBillerId")
    @ResponseBody
    public ResponseEntity<ResultVO> findByBillerId(@RequestParam(value="id") int id) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.findByBillerId(id);
            }
        };

        return handler.getResult();
    }

    @GetMapping(value = "/category/{id}", produces = "application/json")
    public ResponseEntity<ResultVO> findDistinctCategoryByBillerId(@PathVariable(value = "id") Integer id) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.findDistinctCategoryByBillerId(id);
            }
        };
        return handler.getResult();
    }

    @GetMapping(value = "/getByBillerIdAndCategory/{id}/{category}", produces = "application/json")
    public ResponseEntity<ResultVO> findByBillerIdAndCategory(@PathVariable(value = "id") Integer id,
                                                              @PathVariable(value = "category") String category) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.findByBillerIdAndCategory(id, category);
            }
        };
        return handler.getResult();
    }

    @GetMapping(value = "/getByCategory/{category}", produces = "application/json")
    public ResponseEntity<ResultVO> findByCategory(@PathVariable(value = "category") String category) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.findByCategory(category);
            }
        };
        return handler.getResult();
    }

    @GetMapping(value = "/status/toggle/{id}", produces = "application/json")
    public ResponseEntity<ResultVO> toggleStatus(@PathVariable(value = "id") int id) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.toggleStatus(id);
            }
        };
        return handler.getResult();
    }

    @GetMapping(value = "/category/status/toggle/{id}/{category}/{status}", produces = "application/json")
    public ResponseEntity<ResultVO> toggleCategoryStatus(@PathVariable(value = "id") Integer id,
                                                         @PathVariable(value = "category") String category,
                                                         @PathVariable(value = "status") String status) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.toggleCategoryStatus(id, category, status);
            }
        };
        return handler.getResult();
    }
}
