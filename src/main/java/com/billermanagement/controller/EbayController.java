package com.billermanagement.controller;

import com.billermanagement.services.EbayService.EbayService;
import com.billermanagement.vo.EbayVO.CheckTransaction.EbayCheckTransactionByRefIDReqVO;
import com.billermanagement.vo.EbayVO.CheckTransaction.EbayCheckTransactionReqVO;
import com.billermanagement.vo.EbayVO.GetRecepientName.EbayGetRecepientNameReqVO;
import com.billermanagement.vo.EbayVO.Transfer.EbayTransferReqVO;
import com.billermanagement.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/Ebay")
public class EbayController {

    @Autowired
    private EbayService service;

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/GetRecepientName")
    @ResponseBody
    public ResponseEntity<ResultVO> add(@RequestBody final EbayGetRecepientNameReqVO vo) {
        log.info(vo.toString());
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.getRecepientName(vo);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/Transfer")
    @ResponseBody
    public ResponseEntity<ResultVO> transfer(@RequestBody final EbayTransferReqVO vo) {
        log.info(vo.toString());
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.transfer(vo);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/CheckTransaction")
    @ResponseBody
    public ResponseEntity<ResultVO> checkTransaction(@RequestBody final EbayCheckTransactionReqVO vo) {
        log.info(vo.toString());
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.checkTransaction(vo);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/CheckTransactionByRefId")
    @ResponseBody
    public ResponseEntity<ResultVO> checkTransactionByRefId(@RequestBody final EbayCheckTransactionByRefIDReqVO vo) {
        log.info(vo.toString());
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.checkTransactionByRefId(vo);
            }
        };
        return handler.getResult();
    }

}
