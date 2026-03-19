package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.persistance.domain.TransHistory;
import com.billermanagement.persistance.domain.TransTmp;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.persistance.repository.TransTmpRepository;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.MobilePulsa.MobilePulsaStatusVO;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import javax.xml.bind.DatatypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Qualifier("mobilepulsa")
public class MobilePulsa extends BillerRequest {

    @Autowired
    private TransTmpRepository transTmpRepository;
    @Autowired
    SLATime slaTimeService;
    private final InitDB initDB = InitDB.getInstance();
    private String[] asyncStatus = null;
    private Object obj = null;
    private Object[] result = null;
    private String[] request = null;
    private String msgResponse = null;
    private boolean isPending = false;
    private final ObjectMapper mapper = new ObjectMapper();
    private String responseCode = "";

    @Override
    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        logger.info("Entering MobilePulsa Handler");
        try {
            request = getRequest(vo, billerResult, "mobilepulsa");

            //String msgResponse = sendRequest(request[1], request[0]);
            msgResponse = sendRequest(request[1], request[0], getTimeout("mobilepulsa.connectTimeout"), getTimeout("mobilepulsa.readTimeout"));

            obj = getResponse(vo, billerResult, msgResponse);

            if (vo.getMethod().equalsIgnoreCase("Payment")) {
                if (initDB.get("mobilepulsa.async.code") != null) {
                    asyncStatus = initDB.get("mobilepulsa.async.code").split(";");
                }

                if (msgResponse.contains("response_code")) {
                    int firstIndex = msgResponse.lastIndexOf("response_code");
                    int lastIndex = msgResponse.indexOf("message");
                    responseCode = msgResponse.substring(firstIndex, lastIndex).replaceAll("[\":,]", "").replace("response_code", "");
                }

                logger.info("ResponseCode :" + responseCode);
                for (String async : asyncStatus) {
                    logger.info("Async Code :" + async);
                    if (async.equals(responseCode)) {
                        isPending = true;
                    }
                }
                logger.info("isPending :" + isPending);

                if (isPending) {

                    isPending = false;

                    result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltResponse);

                    obj = cleanUpResponse(result[0],vo, billerResult);

                    savePendingTransaction(vo, billerResult, request[1], msgResponse, HandlerConstant.MP);

                    saveTransaction(vo, billerResult, PENDING);

                } else {
                    result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltCallback);
                    obj = cleanUpResponse(result[0],vo, billerResult);

                    saveTransaction(vo, billerResult, transStatus);

                    TransTmp tmpInq = transTmpRepository.findTransByBillerTid(vo.getReffId());
                    transTmpRepository.delete(tmpInq);
                }
            } else {

                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltCallback);
                obj = cleanUpResponse(result[0],vo, billerResult);

                savePendingTransaction(vo, billerResult, request[1], msgResponse, 0);

                saveTransaction(vo, billerResult, transStatus);
            }

            updateBmLogResponse(vo,new ObjectMapper().writeValueAsString(obj),msgResponse);

        } catch (TimeoutException te) {
            result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltResponse);

            obj = cleanUpResponse(result[0],vo, billerResult);

            savePendingTransaction(vo, billerResult, request[1], msgResponse, HandlerConstant.MP);

            saveTransaction(vo, billerResult, PENDING);

            String errorMsg = "Unexpected Response - "+te.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
        } catch (Exception ex) {
            saveTransaction(vo, billerResult, ERROR);
            String errorMsg = "Unexpected Response - "+ex.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
        }
        return obj;
    }

    @Transactional
    public void cekStatus(String transId, int handler) throws Exception {
        long start = System.currentTimeMillis();
        TransHistory transHistory = new TransHistory();

        TransTmp record = transTmpRepository.findTrans(transId, handler);
        TransTmp tmpInq = transTmpRepository.findTransByBillerTid(record.getReffId());
        boolean success = false;
        String paymentTrId = "";
        try {

            String username = InitDB.getInstance().get("mobilepulsa.uid");
            String apiKey = InitDB.getInstance().get("mobilepulsa.pin");
            String sign = username + apiKey + "cs";

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sign.getBytes());
            byte[] digest = md.digest();
            String myHash = DatatypeConverter.printHexBinary(digest).toLowerCase();
            logger.info("Sign Hash :" + myHash);

//            if (record.getResponse().contains("tr_id")) {
//                int firstIndex = record.getResponse().lastIndexOf("tr_id");
//                int lastIndex = record.getResponse().indexOf("code");
//                paymentTrId = record.getResponse().substring(firstIndex, lastIndex).replaceAll("[\":,]", "").replace("tr_id", "");
//                logger.info("paymentTrxId : " + paymentTrId);
//            }
//            logger.info("BillerTID : " + record.getBillerTid());
            MobilePulsaStatusVO vo = new MobilePulsaStatusVO();
            vo.setCommands("checkstatus");
            vo.setUsername(username);
            vo.setRef_id(tmpInq.getBmTid());
            vo.setSign(myHash);

            String data = mapper.writeValueAsString(vo);
            logger.info("Payload Request Cek Status :" + data);

            msgResponse = sendRequest(record.getUrl(), data);

            logger.info(">>>> msgResponse: " + msgResponse);

            if (initDB.get("mobilepulsa.async.code") != null) {
                asyncStatus = initDB.get("mobilepulsa.async.code").split(";");
            }

            if (msgResponse.contains("response_code")) {
                int firstIndex = msgResponse.lastIndexOf("response_code");
                int lastIndex = msgResponse.indexOf("message");
                responseCode = msgResponse.substring(firstIndex, lastIndex).replaceAll("[\":,]", "").replace("response_code", "");
            }

            logger.info("ResponseCode :" + responseCode);
            for (String async : asyncStatus) {
                logger.info("Async Code :" + async);
                if (async.equals(responseCode)) {
                    isPending = true;
                }
            }
            logger.info("isPending :" + isPending);

            if (isPending) {
                logger.info("Status still Pending");
                isPending = false;
            } else {
                ObjectMapper mapperResponse = new ObjectMapper();
                Map<String, Object> mapRes = mapperResponse.readValue(msgResponse, new TypeReference<Map<String, Object>>() {
                });

                Object obj = getCallbackInfo(mapRes, record);

                logger.info(">>> toTransform:" + record.getTransformId() + "," + record.getMethod() + "," + obj);

                Object[] result = transformService.transformApi(record.getTransformId(), record.getMethod(), obj, Jolt.JoltCallback);

                logger.info(">>> result-0:" + result[0]);
                logger.info(">>> result-1:" + result[1]);
                logger.info(">>> result-2:" + result[2]);
                transHistory.setBmTid(record.getBmTid());
                transHistory.setPartnerTid(record.getPartnerTid());
                String partnerMsg = mapper.writeValueAsString(processCallbackResponse(result[0], record,transHistory));
                logger.info(">>> partnerMsg:" + partnerMsg);

                //update TransHistory
                updateTransaction(transHistory);

                updateBmLogCallback(record.getPartnerTid(),record.getBmTid(),partnerMsg,msgResponse);

                String partnerUrl = record.getPartnerUrl();
                if (partnerUrl != null && !partnerUrl.equals("")) {
                    try {
                        sendRequest(partnerUrl, partnerMsg);
                    } catch (Exception e) {
                        logger.info("Failed to call Partner " + e.getMessage());
                            if (record.getSlaTime() == null || record.getSlaTime().toString().isEmpty()){
                                this.logger.info("Start Put SLA Time to TransTmp");
                                int slaInSeconds=0;
                                if (initDB.get("mobilepulsa.Pending.SLA") !=null){
                                    slaInSeconds = Integer.valueOf(initDB.get("mobilepulsa.Pending.SLA"));
                                }
                                Date slaTime = slaTimeService.getTransTmpSLATime(slaInSeconds,record.getId());
                                record.setSlaTime(slaTime);
                                transTmpRepository.save(record);
                            }
                    }
                }

                transTmpRepository.delete(record);
                transTmpRepository.delete(tmpInq);
                success = true;
            }
        }catch (Exception ex){
            logger.info("Mobilepulsa checkstatus error: " + ex.getMessage());
            if (record.getSlaTime() == null || record.getSlaTime().toString().isEmpty()){
                this.logger.info("Start Put SLA Time to TransTmp");
                int slaInSeconds=0;
                if (initDB.get("mobilepulsa.Pending.SLA") !=null){
                    slaInSeconds = Integer.valueOf(initDB.get("mobilepulsa.Pending.SLA"));
                }
                Date slaTime = slaTimeService.getTransTmpSLATime(slaInSeconds,record.getId());
                record.setSlaTime(slaTime);
                transTmpRepository.save(record);
            }
        }
        finally {
            logger.info("Mobile Pulsa checkStatus:" + transId + "," + handler + "," + success + "," + (System.currentTimeMillis() - start) + "ms");
        }
    }
}
