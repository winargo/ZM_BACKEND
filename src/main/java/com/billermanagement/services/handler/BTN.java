package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.TransHistory;
import com.billermanagement.persistance.domain.TransTmp;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.persistance.repository.TransTmpRepository;
import com.billermanagement.util.FormatUtil;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.BTN.BPJSTKPUCekStatusVO;
import com.billermanagement.vo.BTN.BTNCekStatusVO;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Qualifier("btn")
public class BTN extends BillerRequest {

    InitDB initDB = InitDB.getInstance();

    @Autowired
    private TransTmpRepository transTmpRepository;

    @Autowired
    SLATime slaTimeService;

    @Override
    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        log.info("Entering BTN Handler");
        Map<String, String> mapHeader = getHeader();
        int trxidLength = 15;
        String inqTrxStatusCode = "";
        Object obj = null;
        Object[] result;

        if (initDB.get("BTN.InqTrxStatusCode") != null) {
            inqTrxStatusCode = initDB.get("BTN.InqTrxStatusCode");
        }

        if (initDB.get("BTN.trxidLength") != null) {
            trxidLength = Integer.valueOf(initDB.get("BTN.trxidLength"));
        }
        String[] request = getRequest(vo, billerResult, trxidLength, "btn","KSP");
        if (vo.getProductCode().equalsIgnoreCase(inqTrxStatusCode)) {
            if(vo.getParams().get(0).getName().equals("nik")){
                request[1] = initDB.get("BTN.BPJSTKBPU.CekStatus.URL");
            }else{
                request[1] = initDB.get("BTN.BPJSTKPU.CekStatus.URL");
            }
        }
        String msgResponse = sendRequestBypassSSLCert(request[1], mapHeader, request[0], getTimeout("BTN.connectTimeout"), getTimeout("BTN.readTimeout"));
        obj = getResponse(vo, billerResult, msgResponse);
        try {
            result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltCallback);
            obj = cleanUpResponse(result[0],vo, billerResult);
            if (vo.getProductCode().equalsIgnoreCase(inqTrxStatusCode)) {
//          cek status do not store to transaction history
            } else {
                saveTransaction(vo, billerResult, transStatus);
            }
        } catch (TimeoutException e) {
            if (vo.getProductCode().equalsIgnoreCase(inqTrxStatusCode)) {
//          cek status do not store to transaction history
            } else {
                if (vo.getMethod().equalsIgnoreCase("Payment")) {
                    result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltResponse);
                    obj = cleanUpResponse(result[0],vo, billerResult);
                    savePendingTransaction(vo, billerResult, request[1], msgResponse, HandlerConstant.BTN);
                    saveTransaction(vo, billerResult, PENDING);
                } else {
                    saveTransaction(vo, billerResult, ERROR);
                }
                String errorMsg = "Unexpected Response - "+e.getMessage();
                updateBmLogResponse(vo,errorMsg,errorMsg);
            }
//            throw e;
        } catch (Exception e) {
            if (vo.getProductCode().equalsIgnoreCase(inqTrxStatusCode)) {
//          cek status do not store to transaction history
            } else {
                saveTransaction(vo, billerResult, ERROR);
            }
            String errorMsg = "Unexpected Response - "+e.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
        }

        updateBmLogResponse(vo,new ObjectMapper().writeValueAsString(obj),msgResponse);

        return obj;
    }

    private Map<String, String> getHeader() {
        Map<String, String> map = new HashMap<String, String>();
        String key = initDB.get("BTN.key");
        try {
            map.put("key", key);
            return map;
        } catch (Exception e) {
            throw new NostraException(e.getMessage(), StatusCode.ERROR);
        }
    }

    @Transactional
    public void cekStatus(String transId, int handler) throws Exception {
        long start = System.currentTimeMillis();
        TransTmp record = transTmpRepository.findTrans(transId, handler);
        boolean success = false;
        ObjectMapper mapper = new ObjectMapper();
        String data;
        try {
            if (record.getTransformId().equals("btn.bpjstkbpu")){
                BTNCekStatusVO vo = new BTNCekStatusVO();
                vo.setRefnum(getRefNum());
                vo.setFindreqId(record.getReffId());
                vo.setFindnik(record.getBillerTid());
                vo.setTrxdate(ZonedDateTime.now(ZoneId.of("Asia/Jakarta"))
                        .truncatedTo(ChronoUnit.MINUTES)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                data = mapper.writeValueAsString(vo);

            }else{
                BPJSTKPUCekStatusVO vo = new BPJSTKPUCekStatusVO();
                vo.setRefnum(getRefNum());
                vo.setFindreqId(record.getReffId());
                vo.setFindnoTagihan(record.getBillerTid());
                vo.setTrxdate(ZonedDateTime.now(ZoneId.of("Asia/Jakarta"))
                        .truncatedTo(ChronoUnit.MINUTES)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                data = mapper.writeValueAsString(vo);
            }

            logger.info("Payload Request BTN Cek Status :" + data);

            success = checkTransaction(data, record);

            if (success) {
                transTmpRepository.delete(record);
            }else{
                if (record.getSlaTime() == null || record.getSlaTime().toString().isEmpty()){
                    this.logger.info("Start Put SLA Time to TransTmp fro BTN");
                    int slaInSeconds=0;
                    if (initDB.get("BTN.Pending.SLA") !=null){
                        slaInSeconds = Integer.valueOf(initDB.get("BTN.Pending.SLA"));
                    }
                    Date slaTime = slaTimeService.getTransTmpSLATime(slaInSeconds,record.getId());
                    record.setSlaTime(slaTime);
                    transTmpRepository.save(record);
                }
            }
        } finally {
            logger.info("BTN Check Status:" + transId + "," + handler + "," + success + "," + (System.currentTimeMillis() - start) + "ms");
        }
    }

    @Transactional
    private boolean checkTransaction(String data, TransTmp record) throws Exception {
        String msgResponse = null;
        ObjectMapper mapper = new ObjectMapper();

        Map<String, String> mapHeader = getHeader();

        TransHistory transHistory = new TransHistory();

        if (record.getTransformId().equals("btn.bpjstkbpu")){
            record.setUrl(initDB.get("BTN.BPJSTKBPU.CekStatus.URL"));
        }else{
            record.setUrl(initDB.get("BTN.BPJSTKPU.CekStatus.URL"));
        }

        try {
            msgResponse = sendRequestBypassSSLCert(record.getUrl(), mapHeader, data, getTimeout("BTN.connectTimeout"), getTimeout("BTN.readTimeout"));
        } catch (IOException e) {
            logger.info("BTN Check Status Error : " + e.getMessage());
            return false;
        }

        logger.info(">>>> msgResponse: " + msgResponse);

        ObjectMapper mapperResponse = new ObjectMapper();
        Map<String, Object> mapRes;
        try {
            mapRes = mapperResponse.readValue(msgResponse, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException ex) {
            logger.info("BTN Check Status Error : " + ex.getMessage());
            return false;
        }

        Object obj = getCallbackInfo(mapRes, record);

        logger.info(">>> toTransform:" + record.getTransformId() + "," + record.getMethod() + "," + obj);

        Object[] result = transformService.transformApi(record.getTransformId(), record.getMethod(), obj, Jolt.JoltCallback);

        logger.info(">>> result-0:" + result[0]);
        logger.info(">>> result-1:" + result[1]);
        logger.info(">>> result-2:" + result[2]);
        String partnerMsg;
        transHistory.setBmTid(record.getBmTid());
        transHistory.setPartnerTid(record.getPartnerTid());
        try {
            partnerMsg = mapper.writeValueAsString(processCallbackResponse(result[0], record,transHistory));
        } catch (Exception ex) {
            logger.info("BTN Check Status Error : " + ex.getMessage());
            return false;
        }
        logger.info(">>> partnerMsg:" + partnerMsg);

        //update TransHistory
        updateTransaction(transHistory);

        updateBmLogCallback(record.getPartnerTid(),record.getBmTid(),partnerMsg,msgResponse);

        String partnerUrl = record.getPartnerUrl();
        if (partnerUrl != null && !partnerUrl.equals("")) {
            try {
                sendRequest(partnerUrl, partnerMsg);
            } catch (IOException e) {
                logger.info("Failed to call Partner " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public String getRefNum() {
        String prefix = "KSP";
        int trxidLength = 15;
        if (initDB.get("BTN.trxidLength") != null) {
            trxidLength = Integer.valueOf(initDB.get("BTN.trxidLength"));
        }

        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('A', 'Z').build();
        prefix = generator.generate(3);

        int len = prefix.length();
        StringBuilder ret = new StringBuilder(prefix);
        if (len >= 3) {
            int lenDate = 17 - (trxidLength - len);
            ret.append(FormatUtil.getTime("yyyyMMddHHmmssSSS").substring(lenDate));
        } else {
            ret.append(FormatUtil.getTime("yyyyMMddHHmmssSSS"));
        }

        return ret.toString();
    }

}
