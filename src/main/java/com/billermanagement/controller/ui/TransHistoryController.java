package com.billermanagement.controller.ui;

import com.billermanagement.controller.AbstractRequestHandler;
import com.billermanagement.services.ui.TransHistoryService;
import com.billermanagement.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gui/history")
public class TransHistoryController {
    @Autowired
    private TransHistoryService service;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value="/getAll")
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

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value="/getAll2")
    @ResponseBody
    public ResponseEntity<ResultVO> findAll2(
            @RequestParam(value = "page",required = false,defaultValue = "1") Integer page,
            @RequestParam(value = "size",required = false,defaultValue = "10") Integer size,
            @RequestParam(value = "sort-by", required = false,defaultValue ="id" ) String sortBy,
            @RequestParam(value = "sort-order", required = false,defaultValue = "desc") String sortOrder) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.findAll2(page,size,sortBy,sortOrder);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value="/getHistory")
    @ResponseBody
    public ResponseEntity<ResultVO> findRecord(
            @RequestParam(value = "partnerId", required = false) Integer partnerId,
            @RequestParam(value = "billerId", required = false) Integer billerId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate

    ) {
        System.out.println("Incoming Request");
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.findRecord(partnerId, billerId, category, startDate, endDate);
            }
        };

        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value="/getHistory2")
    @ResponseBody
    public ResponseEntity<ResultVO> findRecord2(
            @RequestParam(value = "partnerId", required = false) Integer partnerId,
            @RequestParam(value = "billerId", required = false) Integer billerId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "page",required = false,defaultValue = "1") Integer page,
            @RequestParam(value = "size",required = false,defaultValue = "10") Integer size,
            @RequestParam(value = "sort-by", required = false,defaultValue ="DATE_CREATED" ) String sortBy,
            @RequestParam(value = "sort-order", required = false,defaultValue = "desc") String sortOrder,
            @RequestParam(value = "search", required = false) String search
    ) {
        System.out.println("Incoming Request");
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.findRecord2(partnerId, billerId, category, startDate, endDate,page,size,sortBy,sortOrder,search);
            }
        };

        return handler.getResult();
    }
}
