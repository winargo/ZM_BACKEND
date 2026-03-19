package com.billermanagement.controller;

import com.billermanagement.services.EbayService.EbayService;
import com.billermanagement.services.InstamoneyService.InstamoneyService;
import com.billermanagement.services.handler.Instamoney;
import com.billermanagement.vo.EbayVO.CheckTransaction.EbayCheckTransactionByRefIDReqVO;
import com.billermanagement.vo.EbayVO.CheckTransaction.EbayCheckTransactionReqVO;
import com.billermanagement.vo.EbayVO.GetRecepientName.EbayGetRecepientNameReqVO;
import com.billermanagement.vo.EbayVO.Transfer.EbayTransferReqVO;
import com.billermanagement.vo.InstamoneyVO.Callback.IMCallbackReqVO;
import com.billermanagement.vo.InstamoneyVO.Callback.IMInquiryCallbackReqVO;
import com.billermanagement.vo.InstamoneyVO.CreateCustomer.IMCreateCustomerReqVO;
import com.billermanagement.vo.InstamoneyVO.Transfer.IMTransferReqVO;
import com.billermanagement.vo.InstamoneyVO.ValidateBankName.IMValidateBankNameReqVO;
import com.billermanagement.vo.InstamoneyVO.ValidateEwallet.IMValidateEwalletReqVO;
import com.billermanagement.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/Instamoney")
public class InstamoneyController {

    @Autowired
    private InstamoneyService service;

    @Autowired
    private Instamoney im;

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/ValidateBankName")
    @ResponseBody
    public ResponseEntity<ResultVO> validateBankName(@RequestBody final IMValidateBankNameReqVO vo) {
        log.info(vo.toString());
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.ilumaGetBankName(vo);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/CreateCustomer")
    @ResponseBody
    public ResponseEntity<ResultVO> createCustomer(@RequestBody final IMCreateCustomerReqVO vo) {
        log.info(vo.toString());
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.imCreateCustomer(vo);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/Callback")
    @ResponseBody
    public ResponseEntity<ResultVO> callback(@RequestBody final IMCallbackReqVO vo) {
        log.info(vo.toString());
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return im.paymentCallback(vo);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/InquiryCallback")
    @ResponseBody
    public ResponseEntity<ResultVO> inquiryCallback(@RequestBody final IMInquiryCallbackReqVO vo) {
        log.info(vo.toString());
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return im.inpuiryCallback(vo);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/Transfer")
    @ResponseBody
    public ResponseEntity<ResultVO> transfer(@RequestBody final IMTransferReqVO vo) {
        log.info(vo.toString());
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.imTransfer(vo);
            }
        };
        return handler.getResult();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value="/ValidateEwallet")
    @ResponseBody
    public ResponseEntity<ResultVO> validateEwallet(@RequestBody final IMValidateEwalletReqVO vo) {
        log.info(vo.toString());
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return service.validateEwallet(vo);
            }
        };
        return handler.getResult();
    }

}
