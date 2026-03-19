/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.controller;

import com.billermanagement.services.TransformService;
import com.billermanagement.vo.ResultVO;
import com.billermanagement.vo.TransformReqVO;
import com.billermanagement.vo.TransformUpdateVO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author sulaeman
 */
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/v1/transform")
public class TransformController {

    @Autowired
    private TransformService service;

    @RequestMapping(method = RequestMethod.POST, value = "/Add")
    public ResponseEntity<ResultVO> add(@RequestParam(value = "transformId", required = true) String transformId, @RequestParam(value = "method", required = true) String method, @RequestParam("name") String name, @RequestParam("type") String type, @RequestParam("fileRq") MultipartFile fileRq, @RequestParam("fileRs") MultipartFile fileRs, @RequestParam("fileCb") MultipartFile fileCb, @RequestParam(value = "url", required = true) String url, @RequestParam(value = "flowtype", required = true) String flowType) throws IOException {
        System.out.println(">>>> ada pesan");
        String request = new String(fileRq.getBytes(), StandardCharsets.UTF_8);
        String response = new String(fileRs.getBytes(), StandardCharsets.UTF_8);
        String callback = new String(fileCb.getBytes(), StandardCharsets.UTF_8);
        TransformReqVO reqVO = new TransformReqVO();
        reqVO.setTransformId(transformId);
        reqVO.setMethod(method);
        reqVO.setName(name);
        reqVO.setType(type);
        reqVO.setFileRequest(request);
        reqVO.setFileResponse(response);
        reqVO.setFileCallback(callback);
        reqVO.setUrl(url);
        reqVO.setFlowType(flowType);
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.addNew(reqVO);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/Update")
    public ResponseEntity<ResultVO> update(@RequestParam(value = "transformId", required = true) String transformId, @RequestParam(value = "id", required = true) Integer id, @RequestParam(value = "method", required = true) String method, @RequestParam("name") String name, @RequestParam("type") String type, @RequestParam("fileRq") MultipartFile fileRq, @RequestParam("fileRs") MultipartFile fileRs, @RequestParam("fileCb") MultipartFile fileCb, @RequestParam(value = "url", required = true) String url, @RequestParam(value = "flowtype", required = true) String flowType) throws IOException {
        String request = new String(fileRq.getBytes(), StandardCharsets.UTF_8);
        String response = new String(fileRs.getBytes(), StandardCharsets.UTF_8);
        String callback = new String(fileCb.getBytes(), StandardCharsets.UTF_8);
        TransformUpdateVO updateVO = new TransformUpdateVO();
        updateVO.setId(id);
        updateVO.setTransformId(transformId);
        updateVO.setMethod(method);
        updateVO.setName(name);
        updateVO.setType(type);
        updateVO.setFileRequest(request);
        updateVO.setFileResponse(response);
        updateVO.setFileCallback(callback);
        updateVO.setUrl(url);
        updateVO.setFlowType(flowType);
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.update(updateVO);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{id}")
    public ResponseEntity<ResultVO> getById(@PathVariable(value = "id", required = true) Integer id) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.getById(id);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/All")
    public ResponseEntity<ResultVO> getByAll() {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.getAll();
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/Distinct")
    public ResponseEntity<ResultVO> getDistinct() {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.getDistinct();
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{id}")
    public ResponseEntity<ResultVO> deleteById(@PathVariable(value = "id", required = true) Integer id) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.deleteById(id);
            }
        };
        return handler.getResult();
    }

}
