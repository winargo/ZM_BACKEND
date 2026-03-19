package com.billermanagement.controller;

import com.billermanagement.config.RabbitMQConfig;
import com.billermanagement.services.SwitchingService;
import com.billermanagement.services.handler.HandlerConstant;
import com.billermanagement.services.handler.IRS;
import com.billermanagement.services.handler.Xfers;
import com.billermanagement.util.FormatUtil;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class RequestController {
    
    @Autowired
    private SwitchingService switchingService;
    
//    @Autowired
//    private IRS irs;
    
//    @Autowired
//    private Xfers xfers;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    Logger logger = LoggerFactory.getLogger(RequestController.class);
    
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/request")
    @ResponseBody
    public Object getRequest(@RequestBody final RequestVO requestVO) {
        long start = System.currentTimeMillis();
        logger.info("getRequest: " + requestVO);
        
        String productCode = requestVO.getProductCode();
        Object responseVO;
        try {
            responseVO = switchingService.processRequest(requestVO);
        } catch (Exception e) {
            String status = "1000";
            String desc = e.getMessage();
            if ((desc.startsWith("PRC"))) {
                status = "1002";
                desc = desc.substring(4);
            }
            
            Map<String, String> map = new LinkedHashMap<>();
            map.put("Method", requestVO.getMethod());
            map.put("ProductCode", productCode);
            map.put("RequestId", requestVO.getRequestId());
            map.put("TransactionId", getTransactionId(requestVO));
            map.put("Time", FormatUtil.getTime("yyyyMMddHHmmssSSS"));
            map.put("Status", status);
            map.put("Desc", desc);
            map.put("FlowType", "Sync");
            
            responseVO = map;
        }
        logger.info("getRequestResult: " + responseVO + "," + (System.currentTimeMillis() - start) + "ms");
        
        return responseVO;
    }
    
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE,
            value = "/irsCallback")
    @ResponseBody
    public Object getCallback(@RequestBody final String callback) {
        logger.info("Callback Request received: " + callback);

        Map<Integer, String> mapPayload = new HashMap<>();
        StringBuilder sb = new StringBuilder().append("<?xml version=\"1.0\"?><ackResponse><status>");
        try {
            mapPayload.put(HandlerConstant.IRS,callback);
            this.rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, mapPayload);
//            int i = irs.callback(callback);
            int i = 0;
            sb.append(i);
            if (i == HandlerConstant.SUCCESS) {
                sb.append("</status><message>OK</message></ackResponse>");
            } else if (i == HandlerConstant.TRANSACTIONID_ERRORS) {
                sb.append("</status><message>TransactionId Errors</message></ackResponse>");
            } else if (i == HandlerConstant.BACKEND_ERROR) {
                sb.append("</status><message>Backend errors</message></ackResponse>");
            } else if (i == HandlerConstant.MISSING_TRANSACTION) {
                sb.append("</status><message>TransactionId Not Found</message></ackResponse>");
            } else {
                sb.append("2000</status><message>Other Error</message></ackResponse>");
            }
        } catch (Exception e) {
            sb.append("2000</status><message>Other Error</message></ackResponse>");
        }
        
        logger.info("Callback Response sent: " + sb.toString());
        
        return sb.toString();
    }
    
    @RequestMapping(method = RequestMethod.POST,
            consumes = "application/vnd.api+json",
            produces = "application/vnd.api+json",
            value = "/xfersCallback")
    @ResponseBody
    public Object getXfersCallback(@RequestBody final String callback) {
        long start = System.currentTimeMillis();
        logger.info("xfersCallback Request received: " + callback);

        Object responseVO = null;
        Map<String, String> map = new LinkedHashMap<>();
        Map<Integer, String> mapPayload = new HashMap<>();
        String[] respCode;
        String status;
        String desc;
        
        try {
            map.put("Status", "0");
            map.put("Desc", "Success");
            // Producer sends message to exchange There is no binding after queue Return message when
//            rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
//                logger.info("ackMQSender Send message returned " + exchange +" " + routingKey);
//            });
            // Producer sends message confirm testing
//            this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
//                if (!ack) {
//                    logger.info("ackMQSender Message sending failed " + cause +" "+ correlationData.toString());
//                    map.put("Status", String.valueOf(HandlerConstant.BACKEND_ERROR));
//                    map.put("Desc", "ackMQSender Message sending failed");
//                } else {
//                    logger.info("ackMQSender Message sent successfully ");
//                }
//            });
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readValue(callback,JsonNode.class);
            logger.info("Minify Callback : "+jsonNode.toString());
            mapPayload.put(HandlerConstant.XF,jsonNode.toString());
            this.rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, mapPayload);
//            xfers.processCallback(callback);
            responseVO = map;
        } catch (Exception e) {
            status = "2000";
            desc = e.getMessage();
            if ((desc.contains("#"))) {
                respCode = desc.split("#");
                
                status = respCode[0];
                desc = respCode[1];
            }
            map.put("Status", status);
            map.put("Desc", desc);
            responseVO = map;
        }
        
        logger.info("Callback Response sent: " + responseVO + "," + (System.currentTimeMillis() - start) + "ms");
        
        return responseVO;
    }
    
    private String getTransactionId(RequestVO vo) {
        String prefix;
        if (vo.getAccount() != null) {
            prefix = vo.getAccount();
            if (prefix.startsWith("0")) {
                prefix = prefix.substring(1);
            } else if (prefix.startsWith("62")) {
                prefix = prefix.substring(2);
            }
        } else {
            prefix = vo.getRequestId();
        }
        
        return new StringBuilder(prefix).append(FormatUtil.getTime("yyyyMMddHHmmssSSS")).toString();
    }
    
}
