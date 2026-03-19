package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.TransHistory;
import com.billermanagement.persistance.domain.TransTmp;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.persistance.repository.TransTmpRepository;
import com.billermanagement.util.Base64Util;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.backend.ParamsVO;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
public class XfersTransfer extends BillerRequest {

    @Autowired
    private TransTmpRepository transTmpRepository;
    private final InitDB initDB = InitDB.getInstance();

    @Autowired
    Base64Util base64Util;

    @Autowired
    SLATime slaTimeService;

    @Override
    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        logger.info("Entering Xfers Transfer Handler");
        Map<String, String> map = new HashMap<>();
        String[] asyncStatus = null;
        boolean isPending = false;
        Object[] result = null;
        Object obj = null;
        int trxidLength = 22;
        try {
            if (initDB.get("xfers.trxidLength") != null) {
                trxidLength = Integer.valueOf(initDB.get("xfers.trxidLength"));
            }

            String partnerAccountCode = vo.getAccountCode();
            if (initDB.get("Bank.Code.Xfers."+vo.getAccountCode()) != null) {
                vo.setAccountCode(initDB.get("Bank.Code.Xfers."+vo.getAccountCode()));
            }

            if (vo.getMethod().equals("Payment")){
                List<ParamsVO> paramsVOList= new ArrayList<>();

                if (vo.getParams() != null){
                    paramsVOList = vo.getParams();
                }

                String description = "";
                if (initDB.get("XfersTransfer.Description") != null){
                    description = initDB.get("XfersTransfer.Description");
                    paramsVOList.add(createParamsVO("Description",description));
                }

                vo.setParams(paramsVOList);
            }

            String[] request = getRequest(vo, billerResult, trxidLength, 5);

            map.put("Authorization", "Basic " + getAuth());
            map.put("Content-Type", "application/vnd.api+json");

            String msgResponse = sendRequest(request[1], map, request[0], getTimeout("xfers.connectTimeout"), getTimeout("xfers.readTimeout"));

            vo.setAccountCode(partnerAccountCode);
            obj = getResponse(vo, billerResult, msgResponse);

            if (initDB.get("xfers.async.code") != null) {
                asyncStatus = initDB.get("xfers.async.code").split(";");
            }

            for (String async : asyncStatus) {
                logger.info("Async Code :" + async);
                if (msgResponse.toUpperCase().contains(async)) {
                    isPending = true;
                }
            }
            logger.info("isPending :" + isPending);

            if (isPending) {

                isPending = false;

                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltResponse);

                obj = cleanUpResponse(result[0],vo, billerResult);

                savePendingTransaction(vo, billerResult, request[1], msgResponse, HandlerConstant.XFT);

                saveTransaction(vo, billerResult, PENDING);

            } else {
                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltCallback);
                obj = cleanUpResponse(result[0],vo, billerResult);

                saveTransaction(vo, billerResult, transStatus);
            }

            updateBmLogResponse(vo,new ObjectMapper().writeValueAsString(obj),msgResponse);

        } catch (Exception ex) {
            saveTransaction(vo, billerResult, ERROR);
            String errorMsg = "Unexpected Response - "+ex.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
        }
        return obj;
    }

//    private ParamsVO createParamsVO(String name, String value){
//        try{
//            ParamsVO result=new ParamsVO();
//            result.setName(name);
//            result.setValue(value);
//            return result;
//        }catch (Exception e){
//            e.printStackTrace();
//            throw new NostraException(e.getMessage(), StatusCode.ERROR);
//        }
//    }

    private String getAuth() {
        String username = initDB.get("xfers.transfer.username");
        String password = initDB.get("xfers.transfer.password");
        return base64Util.encode(username + ":" + password);
    }

    @Transactional
    public void processCallback(String message) throws Exception {
        Object obj = null;
        String referenceId = null;
        TransTmp transTmp = null;
        TransHistory transHistory = new TransHistory();

        if (message.isEmpty()) {
            throw new Exception("1000#Invalid Request");
        }

        if (message.contains("referenceId")) {
            int firstIndex = message.lastIndexOf("referenceId");
            int lastIndex = message.indexOf("description");
            referenceId = message.substring(firstIndex, lastIndex).replaceAll("[\":,]", "").replace("referenceId", "");
            logger.info("referenceId : " + referenceId);
        }

        transTmp = getTransTmp(referenceId, HandlerConstant.XF);

        if (transTmp == null) {
            throw new Exception("1100#Missing TransactionId");
        }

        ObjectMapper mapperResponse = new ObjectMapper();
        Map<String, Object> mapRes = mapperResponse.readValue(message, new TypeReference<Map<String, Object>>() {
        });

        obj = getCallbackInfo(mapRes, transTmp);

        Object[] result = transformService.transformApi(transTmp.getTransformId(), transTmp.getMethod(), obj, Jolt.JoltCallback);
        logger.info(">>>>> transform Result:" + transTmp.getTransformId() + "," + transTmp.getMethod() + "," + result[0]);

        transHistory.setBmTid(transTmp.getBmTid());
        transHistory.setPartnerTid(transTmp.getPartnerTid());
        String partnerMsg = new ObjectMapper().writeValueAsString(processCallbackResponse(result[0], transTmp,transHistory));
        logger.info(">>> partnerMsg:" + partnerMsg);

        updateTransaction(transHistory);

        updateBmLogCallback(transTmp.getPartnerTid(),transTmp.getBmTid(),partnerMsg,message);

        String partnerUrl = transTmp.getPartnerUrl();
        if (partnerUrl != null && !partnerUrl.equals("")) {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Content-Type", "application/json");
            headerMap.put("Accept", "application/json");
            sendRequestRedirectPost(partnerUrl, headerMap, partnerMsg);
        }

        deleteTransTmp(transTmp);
    }

    @Transactional
    public void checkStatus(String transId, int handler) throws Exception {
        System.out.println(">>>XfersTransfer.checkStatus.start");
        long start = System.currentTimeMillis();

        TransTmp record = transTmpRepository.findTrans(transId, handler);
        boolean success = false;
        try {
            String data = null;
            success = checkTrans(data, record);
            if (success) {
                transTmpRepository.delete(record);
            }else{
                if (record.getSlaTime() == null || record.getSlaTime().toString().isEmpty()){
                    this.logger.info("Start Put SLA Time to TransTmp fro XfersTransfer");
                    int slaInSeconds=0;
                    if (initDB.get("xfers.Pending.SLA") !=null){
                        slaInSeconds = Integer.valueOf(initDB.get("xfers.Pending.SLA"));
                    }
                    Date slaTime = slaTimeService.getTransTmpSLATime(slaInSeconds,record.getId());
                    record.setSlaTime(slaTime);
                    transTmpRepository.save(record);
                }
            }
        } finally {
            logger.info("checkStatus:" + transId + "," + handler + "," + success + "," + (System.currentTimeMillis() - start) + "ms");
            this.logger.info(">>> XfersTransfer.checkStatus.end");
        }
    }

    @Transactional
    private boolean checkTrans(String message, TransTmp record) throws Exception {
        System.out.println(">>> XfersTransfer.checkTrans.start");
        String[] asyncStatus = null;
        boolean isPending = false;
        String msgResponse;
        TransHistory transHistory = new TransHistory();
        try {
            Map<String,String> mapHeader = new HashMap<>();
            logger.info("XfersTransfer.checkStatus.request: " + message);
            mapHeader.put("Authorization", "Basic " + getAuth());
            mapHeader.put("Content-Type", "application/vnd.api+json");

            String url="";
            if (initDB.get("xfers.cekstatus.url") != null){
                url=initDB.get("xfers.cekstatus.url").toString() +"/"+record.getBillerTid();
            }

            msgResponse = sendRequest(url, mapHeader, getTimeout("xfers.connectTimeout"), getTimeout("xfers.readTimeout"));
            logger.info("XfersTransfer.checkStatus.response: " + msgResponse);
        } catch (Exception e) {
            return false;
        }

        if (initDB.get("xfers.async.code") != null) {
            asyncStatus = initDB.get("xfers.async.code").split(";");
        }

        for (String async : asyncStatus) {
            logger.info("Async Code :" + async);
            if (msgResponse.toUpperCase().contains(async)) {
                isPending = true;
            }
        }
        logger.info("isPending :" + isPending);

        if (isPending)  {
            isPending=false;
            return false;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map;
            try {
                map = mapper.readValue(msgResponse, new TypeReference<Map<String, Object>>() { });
            } catch (IOException ex) {
                logger.info("XfersTransfer.checkTrans.error : " + ex.getMessage());
                return false;
            }

            Object obj = getCallbackInfo(map, record);

            //System.out.println(">>> BRI.toTransform:" + record.getTransformId() + "," + record.getMethod() + "," + obj);
            Object[] result = transformService.transformApi(record.getTransformId(), record.getMethod(), obj, Jolt.JoltCallback);

            //System.out.println(">>> result-0:" + result[0]);
            //System.out.println(">>> result-1:" + result[1]);
            //System.out.println(">>> result-2:" + result[2]);

            transHistory.setBmTid(record.getBmTid());
            transHistory.setPartnerTid(record.getPartnerTid());
            String partnerMsg = mapper.writeValueAsString(processCallbackResponse(result[0], record,transHistory));
            //System.out.println(">>> BRI.partnerMsg:" + partnerMsg);
            //update TransHistory
            updateTransaction(transHistory);

            updateBmLogCallback(record.getPartnerTid(),record.getBmTid(),partnerMsg,msgResponse);

            String partnerUrl = record.getPartnerUrl();
            if (partnerUrl != null && !partnerUrl.equals("")) {
                try {
                    sendRequest(partnerUrl, partnerMsg);
                } catch (Exception e) {
                    return false;
                }
            }

            System.out.println(">>> XfersTransfer.checkTrans.end");
            return true;
        }
    }
}
