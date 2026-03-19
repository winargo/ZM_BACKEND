/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.controller;

import com.billermanagement.enums.Jolt;
import com.billermanagement.services.TransformService;
import com.billermanagement.services.bimasakti.StatusService;
import com.billermanagement.vo.PulsaRqBaselineVO;
import com.billermanagement.vo.ResultVO;
import com.billermanagement.vo.bimasakti.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author sulaeman
 */
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/v1")
public class TransformControllerTest {

    @Autowired
    private TransformService service;

    @Autowired
    private StatusService bmsStatus;

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/Request")
    @ResponseBody
    public ResponseEntity<ResultVO> request(@RequestBody Object payload, @RequestParam("transformId") String transformId, @RequestParam("method") String method) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.transformApi(transformId, method, payload, Jolt.JoltRequest);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/Request2")
    @ResponseBody
    public ResponseEntity<ResultVO> request2(@RequestBody PulsaRqBaselineVO payload) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Object data = mapper.convertValue(payload, Object.class);

        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.transformApi(payload.getProductCode(), payload.getMethod(), data, Jolt.JoltRequest);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/Response")
    @ResponseBody
    public ResponseEntity<ResultVO> ack(@RequestBody Object payload, @RequestParam("transformId") String transformId, @RequestParam("method") String method) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.transformApi(transformId, method, payload, Jolt.JoltResponse);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/Response2")
    @ResponseBody
    public ResponseEntity<ResultVO> ack2(@RequestBody String payload, @RequestParam("transformId") String transformId, @RequestParam("method") String method) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object data = mapper.readValue(payload, Object.class);

        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.transformApi(transformId, method, data, Jolt.JoltResponse);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/Callback")
    @ResponseBody
    public ResponseEntity<ResultVO> callback(@RequestBody Object payload, @RequestParam("transformId") String transformId, @RequestParam("method") String method) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.transformApi(transformId, method, payload, Jolt.JoltCallback);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/bimasakti/cekstatus")
    @ResponseBody
    public Status getStatusTest() {
        return bmsStatus.getStatus();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/bimasakti/partner")
    @ResponseBody
    public String partnerURL(@RequestBody String data) {
        System.out.println("Callback Response : " + data);
        return "OK";
    }

}
