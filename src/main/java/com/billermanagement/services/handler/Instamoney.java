package com.billermanagement.services.handler;

import com.billermanagement.config.RabbitMQConfig;
import com.billermanagement.enums.Jolt;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.TransHistory;
import com.billermanagement.persistance.domain.TransTmp;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.persistance.repository.TransHistoryRepository;
import com.billermanagement.persistance.repository.TransTmpRepository;
import com.billermanagement.util.FormatUtil;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.AdditionalInfoVO;
import com.billermanagement.vo.InstamoneyVO.Callback.IMCallbackReqVO;
import com.billermanagement.vo.InstamoneyVO.Callback.IMInquiryCallbackReqVO;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Qualifier("instamoney")
public class Instamoney extends BillerRequest{

    @Autowired
    TransTmpRepository ttRepo;

    @Autowired
    TransHistoryRepository thRepo;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    Gson gson;

    @Override
    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        log.info("Entering Instamoney Handler");
        String[] asyncStatus = null;
        boolean isPending = false;
        InitDB initDB=InitDB.getInstance();
        try {
            String[] request = getRequest(vo, billerResult);

            String msgResponse;
            try {
                Map<String, String> map =getToken(vo.getMethod());
                log.info("Authorization = "+map.get("Authorization"));
                msgResponse = sendRequest(request[1], map, request[0]);
            } catch (Exception e) {
                e.printStackTrace();
                msgResponse = e.getMessage();
            }

            Object obj = getResponse(vo, billerResult, msgResponse);

            throw new Exception("Sengaja Error buat debug");
//
//            Object[] result;
//            String pendingResp=initDB.get("IM.Pending.Response");
//            if (pendingResp != null) {
//                asyncStatus = pendingResp.split(";");
//            }
//
//            for (String async : asyncStatus) {
//                logger.info("Async Code :" + async);
//                if (msgResponse.toUpperCase().contains(async)) {
//                    isPending = true;
//                }
//            }
//            logger.info("isPending :" + isPending);
//            if (isPending)  {
//                isPending = false;
//                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltResponse);
//
//                obj = cleanUpResponse(result[0],vo, billerResult);
//
//                savePendingTransaction(vo, billerResult, null, msgResponse, HandlerConstant.IM);
//
//                saveTransaction(vo, billerResult, PENDING);
//            } else {
//                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltCallback);
//                obj = cleanUpResponse(result[0],vo, billerResult);
//
//                saveTransaction(vo, billerResult, transStatus);
//            }
//
//            updateBmLogResponse(vo,new ObjectMapper().writeValueAsString(obj),msgResponse);
//
//
//            return obj;
        } catch (Exception e) {

//            saveTransaction(vo, billerResult, ERROR);
//            String errorMsg = "Unexpected Response - "+e.getMessage();
//            updateBmLogResponse(vo,errorMsg,errorMsg);


            saveTransaction(vo, billerResult, PENDING);
            String errorMsg = "Unexpected Response - "+e.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
            throw e;
        }
    }

    private Map<String, String> getToken(String method){
        log.info("Start generating Authorization");
        Map<String, String> map = new HashMap<String, String>();
        InitDB initDB=InitDB.getInstance();

        if (method.equalsIgnoreCase("Inquiry")){
            log.info("Comes to Iluma");
            String username=initDB.get("iluma.username");
            String password=initDB.get("iluma.password");

            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            map.put("Authorization", authHeader);

            log.info("Finished generating Authorization");
            return map;
        }else {
            log.info("Comes to Instamoney");
            String username=initDB.get("instamoney.username");
            String password=initDB.get("instamoney.password");

            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            map.put("Authorization", authHeader);

            log.info("Finished generating Authorization");
            return map;
        }
    }

    public String paymentCallbackProducer(IMCallbackReqVO vo){
        Map<Integer, String> mapPayload = new HashMap<>();
        try {
            mapPayload.put(HandlerConstant.IM,vo.toString());
            this.rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, mapPayload);
        }catch (Exception e){
            throw new NostraException(e.getMessage(), StatusCode.ERROR);
        }
        return "200";
    }

    @Transactional
    public String paymentCallback(IMCallbackReqVO vo){
        String response="";
        TransHistory transHistory = new TransHistory();

        TransTmp transTmp=ttRepo.findTrans(vo.getExternalId(), HandlerConstant.IM);
        if(transTmp==null){
            throw new NostraException("Transaction does not exist", StatusCode.ERROR);
        }else {

            try {

                ObjectMapper mapper = new ObjectMapper();
                String msgResponse = mapper.writeValueAsString(vo);
                Map<String,Object> map = mapper.readValue(msgResponse, new TypeReference<Map<String,Object>>() {});

                /*AdditionalInfoVO addInfoVO = new AdditionalInfoVO();
                addInfoVO.setTransactionId(transTmp.getBmTid());
                addInfoVO.setPartnerPrice(transTmp.getPartnerPrice());
                addInfoVO.setBillerPrice(transTmp.getBillerPrice());
                addInfoVO.setAdminFee(transTmp.getPartnerFee());
                addInfoVO.setTime(FormatUtil.getTime("yyyyMMddHHmmssSSS"));
                map.put("additional_info", addInfoVO);

                RequestVO requestVO = mapper.readValue(transTmp.getRequest(), RequestVO.class);
                map.put("partner_request", requestVO);

                Object obj = mapper.convertValue(map, Object.class);*/
                Object obj = getCallbackInfo(map, transTmp);

                System.out.println(">>> toTransform:" + transTmp.getTransformId() + "," + transTmp.getMethod() + "," + obj);

                Object[] result = transformService.transformApi(transTmp.getTransformId(), transTmp.getMethod(), obj, Jolt.JoltCallback);

                System.out.println(">>> result-1:" + result[0]);
                System.out.println(">>> result-1:" + result[1]);
                System.out.println(">>> result-1:" + result[2]);

                Gson gson = new Gson();

                transHistory.setBmTid(transTmp.getBmTid());
                transHistory.setPartnerTid(transTmp.getPartnerTid());
                Object object = processCallbackResponse(result[0], transTmp,transHistory);
                log.info("Object "+object.toString());



                String jsonString = mapper.writeValueAsString(object);
                log.info("JSON : "+jsonString);

//                String partnerMsg = mapper.writeValueAsString(processCallbackResponse(result[0], transTmp));
//                System.out.println(">>> partnerMsg:" + partnerMsg);

                updateTransaction(transHistory);

                updateBmLogCallback(transTmp.getPartnerTid(),transTmp.getBmTid(),jsonString,msgResponse);

                sendRequest(transTmp.getPartnerUrl(), jsonString);


                ttRepo.delete(transTmp);

                response="200";

            } catch (Exception e) {
                e.printStackTrace();
                throw new NostraException(e.getMessage(), StatusCode.ERROR);
            }

        }

        return response;
    }

    public String inpuiryCallbackProducer(IMInquiryCallbackReqVO vo){
        Map<Integer, String> mapPayload = new HashMap<>();
        try {
            mapPayload.put(HandlerConstant.IMINQ,vo.toString());
            this.rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, mapPayload);
        }catch (Exception e){
            throw new NostraException(e.getMessage(), StatusCode.ERROR);
        }
        return "200";
    }

    @Transactional
    public String inpuiryCallback(IMInquiryCallbackReqVO vo){
        String response="";
        TransHistory transHistory = new TransHistory();

        TransTmp transTmp=ttRepo.findTransByAccount(vo.getBank_account_number(), "Inquiry", HandlerConstant.IM);
        if(transTmp==null){
            response="200";
        }else {

            try {

                ObjectMapper mapper = new ObjectMapper();
                String msgResponse = mapper.writeValueAsString(vo);
                Map<String,Object> map = mapper.readValue(msgResponse, new TypeReference<Map<String,Object>>() {});

                /*AdditionalInfoVO addInfoVO = new AdditionalInfoVO();
                addInfoVO.setTransactionId(transTmp.getBmTid());
                addInfoVO.setPartnerPrice(transTmp.getPartnerPrice());
                addInfoVO.setBillerPrice(transTmp.getBillerPrice());
                addInfoVO.setAdminFee(transTmp.getPartnerFee());
                addInfoVO.setTime(FormatUtil.getTime("yyyyMMddHHmmssSSS"));
                map.put("additional_info", addInfoVO);

                RequestVO requestVO = mapper.readValue(transTmp.getRequest(), RequestVO.class);
                map.put("partner_request", requestVO);

                Object obj = mapper.convertValue(map, Object.class);*/
                Object obj = getCallbackInfo(map, transTmp);

                System.out.println(">>> toTransform:" + transTmp.getTransformId() + "," + transTmp.getMethod() + "," + obj);

                Object[] result = transformService.transformApi(transTmp.getTransformId(), transTmp.getMethod(), obj, Jolt.JoltCallback);

                System.out.println(">>> result-1:" + result[0]);
                System.out.println(">>> result-1:" + result[1]);
                System.out.println(">>> result-1:" + result[2]);

                Gson gson = new Gson();

                transHistory.setBmTid(transTmp.getBmTid());
                transHistory.setPartnerTid(transTmp.getPartnerTid());
                Object object = processCallbackResponse(result[0], transTmp,transHistory);
                log.info("Object "+object.toString());



                String jsonString = mapper.writeValueAsString(object);
                log.info("JSON : "+jsonString);

//                String partnerMsg = mapper.writeValueAsString(processCallbackResponse(result[0], transTmp));
//                System.out.println(">>> partnerMsg:" + partnerMsg);

                updateTransaction(transHistory);

                updateBmLogCallback(transTmp.getPartnerTid(),transTmp.getBmTid(),jsonString,msgResponse);

                sendRequest(transTmp.getPartnerUrl(), jsonString);

                ttRepo.delete(transTmp);

                response="200";

            } catch (Exception e) {
                e.printStackTrace();
                throw new NostraException(e.getMessage(), StatusCode.ERROR);
            }

        }

        return response;
    }

}
