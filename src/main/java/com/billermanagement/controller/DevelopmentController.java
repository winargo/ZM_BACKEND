package com.billermanagement.controller;

import com.billermanagement.services.SwitchingService;
import com.billermanagement.services.handler.BRI;
import com.billermanagement.services.handler.HandlerConstant;
import com.billermanagement.services.handler.IRS;
import com.billermanagement.util.FormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DevelopmentController {
    Logger logger = LoggerFactory.getLogger(DevelopmentController.class);

    @Autowired
    private SwitchingService switchingService;

    @Autowired
    private IRS irs;

    @Autowired
    private BRI bri;

    @RequestMapping(value = "/dev", method = RequestMethod.POST)
    public String callback(@RequestBody String req) {
        logger.info("Callback Request received: " + req);

        logger.info("Callback Response process: " + req.replaceAll("\\s", ""));

        StringBuilder sb = new StringBuilder().append("<?xml version=\"1.0\"?><ackResponse><status>");
        try {
            int i = irs.callback(req);
            sb.append(i);
            if (i == HandlerConstant.SUCCESS) sb.append("</status><message>OK</message></ackResponse>");
            else if (i == HandlerConstant.TRANSACTIONID_ERRORS) sb.append("</status><message>TransactionId Errors</message></ackResponse>");
            else if (i == HandlerConstant.BACKEND_ERROR) sb.append("</status><message>Backend errors</message></ackResponse>");
            else if (i == HandlerConstant.MISSING_TRANSACTION) sb.append("</status><message>TransactionId Not Found</message></ackResponse>");
            else sb.append("2000</status><message>Other Error</message></ackResponse>");
        } catch (Exception e) {
            sb.append("2000</status><message>Other Error</message></ackResponse>");
        }

        logger.info("Callback Response sent: " + sb.toString());

        return sb.toString();
    }

    @RequestMapping(method = RequestMethod.GET, value="/test")
    @ResponseBody
    public String test() {
        //System.out.println(bri.test());

        return "OK";
    }
}
