package com.billermanagement.services;

import com.billermanagement.services.handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {
    @Autowired
    BimasaktiStatus bimasaktiStatus;

    @Autowired
    private MobilePulsa mobilePulsa;

    @Autowired
    private BTN btnHandler;

    @Autowired
    private BRI bri;

    @Autowired
    private Xfers xfers;

    @Autowired
    private XfersTransfer xfersTransfer;

    Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    @Async
    public void processRequest(String transId, int handler) {
        long start = System.currentTimeMillis();
        try {
            if (handler == HandlerConstant.BS) bimasaktiStatus.checkStatus(transId, handler);
            else if (handler == HandlerConstant.BNI) bimasaktiStatus.checkStatus(transId, handler);
            else if (handler == HandlerConstant.MP) mobilePulsa.cekStatus(transId, handler);
            else if (handler == HandlerConstant.BTN) btnHandler.cekStatus(transId, handler);
            else if (handler == HandlerConstant.BRI) bri.checkStatus(transId, handler);
            else if (handler == HandlerConstant.XF) xfers.checkStatus(transId, handler);
            else if (handler == HandlerConstant.XFT) xfersTransfer.checkStatus(transId, handler);

            logger.info("processRequest:" + transId + "," + handler + ",OK," + (System.currentTimeMillis()-start) + "ms");
        } catch (Exception e) {
            logger.error("processRequest:" + transId + "," + handler + "," + e.getMessage() + "," + (System.currentTimeMillis()-start) + "ms");
        }
    }

    /*@Async
    public CompletableFuture<String> saveBms(String transId, int handler) {
        try {
            System.out.println("1");
            Thread.sleep(10000);
            System.out.println("2");
            if (handler == HandlerConstant.BS) bimasaktiStatus.checkStatus(transId, handler);
            System.out.println("3");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return CompletableFuture.completedFuture("OK123");
    }*/
}
