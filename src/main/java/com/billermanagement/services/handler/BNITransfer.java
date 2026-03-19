/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import static com.billermanagement.services.handler.BillerRequest.PENDING;
import com.billermanagement.util.Base64Util;
import com.billermanagement.util.FormatUtil;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.BNI.SignatureVO;
import com.billermanagement.vo.BNI.TokenResVO;
import com.billermanagement.vo.backend.ParamsVO;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * @author ar.muhammad
 */

@Slf4j
@Service
//@Qualifier("bnitransfer")
public class BNITransfer extends BillerRequest{
    @Autowired
    private ApiConfig apiConfig;
    
    @Autowired
    private Base64Util base64util;

    private boolean isPending = false;
    private String[] asyncStatus = null;
    
    @Override
    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        log.info("Entering BNI Transfer Handler");
        InitDB initDB=InitDB.getInstance();
        String currDate="";
        Random rand = new Random();
        int rand_int1 = rand.nextInt(999);
        String acctCode="", bankCode="";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            log.info("account code : "+vo.getAccountCode());
            
            List<ParamsVO>  paramsVOS = new ArrayList<>();

            if(vo.getParams() != null){
                paramsVOS = vo.getParams();
            }

            if(vo.getMethod().equalsIgnoreCase("Payment")){
                paramsVOS.add(createParamsVO("customerReferenceNumber",sdf.format(new Date())+rand_int1));
                paramsVOS.add(createParamsVO("sourceAccount",initDB.get("BNI.OGP.Custodian")));
                if(initDB.get("BNI.OGP.InHouse").toUpperCase().contains(vo.getAccountCode().toUpperCase())){
                    paramsVOS.add(createParamsVO("paymentMethod",initDB.get("BNI.OGP.PayMethod")));
                    paramsVOS.add(createParamsVO("valueDate",sdf.format(new Date())));
                
                }else{
                    paramsVOS.add(createParamsVO("destinationBankName",initDB.get("Bank.Name.OGPBni."+vo.getAccountCode())));
                }
                
                log.info(vo.toString());

            }else{
                if(!initDB.get("BNI.OGP.InHouse").toUpperCase().contains(vo.getAccountCode().toUpperCase())){
                    paramsVOS.add(createParamsVO("customerReferenceNumber",sdf.format(new Date())+rand_int1));
                    paramsVOS.add(createParamsVO("sourceAccount",initDB.get("BNI.OGP.Custodian")));
                }
            }
            bankCode = initDB.get("Bank.Code.OGPBni."+vo.getAccountCode());
            if(bankCode != null){
                acctCode = vo.getAccountCode();
                vo.setAccountCode(bankCode);
            }
            String ogpId = initDB.get("BNI.OGP.id");
            String clienid = initDB.get("BNI.OGP.clientID");
            log.info("clientID : " + clienid);
            log.info("OGP ID : "+ ogpId);
            paramsVOS.add(createParamsVO("clientId",initDB.get("BNI.OGP.id")+base64util.encode(clienid)));
            vo.setParams(paramsVOS);
            String[] request = getRequest(vo, billerResult, Integer.parseInt(initDB.get("BNI.OGP.TransIdLength")),null,null,FormatUtil.getTime("yyyyMMddHHmmss"));
            String req=request[0];
            log.info("Request "+req);
            SignatureVO signatureVO=new SignatureVO();
            signatureVO.setText(req);
            String signature=",\"signature\":\""+getSignaturetf(signatureVO)+"\"}";
            log.info("Signature "+signature);
            req=request[0].substring(0,request[0].length()-1)+signature;
            log.info("Request "+req);
            String msgResponse;
            try {
                Map<String, String> map =getHeadertf();
                String newUrl=generateURLtf(request[1]);
                msgResponse = sendRequest(newUrl, map,req ,getTimeout("BNI.OGP.Connect.Timeout"), getTimeout("BNI.OGP.Read.Timeout"));
            } catch (Exception e) {
                e.printStackTrace();
                msgResponse = e.getMessage();
            }

            vo.setAccountCode(acctCode);
            Object obj = getResponse(vo, billerResult, msgResponse);

            if (initDB.get("BNI.OGP.async.code") != null) {
                asyncStatus = initDB.get("BNI.OGP.async.code").split(";");
            }

            String responseCodeBE=msgResponse;
            if (msgResponse.contains("responseCode")){
                int firstIndex = msgResponse.lastIndexOf("responseCode");
                int lastIndex = msgResponse.indexOf("responseMessage");
                responseCodeBE = msgResponse.substring(firstIndex, lastIndex).replaceAll("[\":,]", "").replace("responseCode", "");
                logger.info("responseCodeBE : " + responseCodeBE);
            }

            for (String async : asyncStatus) {
                logger.info("BNI OGP Async Code :" + async);
                if (responseCodeBE.toUpperCase().contains(async)) {
                    isPending = true;
                }
            }
            logger.info("isPending :" + isPending);

            Object[] result;
            if (isPending)  {
                isPending = false;
                vo.setReffId(vo.getReffId()+";"+currDate);
                log.info("VO "+vo.toString());
                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltResponse);

                obj = cleanUpResponse(result[0],vo, billerResult);

                savePendingTransaction(vo, billerResult, request[1], msgResponse, HandlerConstant.BNI);

                saveTransaction(vo, billerResult, PENDING);
            } else {
                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltCallback);
                obj = cleanUpResponse(result[0],vo, billerResult);

                saveTransaction(vo, billerResult, transStatus);
            }

            updateBmLogResponse(vo,new ObjectMapper().writeValueAsString(obj),msgResponse);

            return obj;
        } catch (Exception e) {
            saveTransaction(vo, billerResult, ERROR);
            String errorMsg = "Unexpected Response - "+e.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
            throw e;
        }
    }
    
    public String generateURLtf(String url){
        String newURL="";
        InitDB initDB=InitDB.getInstance();
        try{
            TokenResVO tokenResVO=getBNITokentf();
            String token=tokenResVO.getToken();
            newURL=url+"?access_token="+token;
            return newURL;
        }catch (Exception e){
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
    }
    
    
    public Map<String, String> getHeadertf(){
        Map<String, String> map=new HashMap<String, String>();
        InitDB initDB=InitDB.getInstance();
        try{
//            String apiKey="ea52e4bd-6b95-4b73-acf3-5e9f974e8528";
            String apiKey=initDB.get("BNI.OGP.apiKey");
            map.put("x-api-key", apiKey);
            return map;
        }catch (Exception e){
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
    }
    
    public TokenResVO getBNITokentf(){
        TokenResVO token = new TokenResVO();
        Map<String, String> map = new HashMap<String, String>();
        InitDB initDB=InitDB.getInstance();
        ObjectMapper mapper = new ObjectMapper();

        try {
//            String url="https://apidev.bni.co.id:8066/api/oauth/token";
            String url = initDB.get("BNI.OGP.url.host")+initDB.get("BNI.OGP.url.token");
//            String username="e0e98788-e2bf-4a71-86a5-e43d0f28d418";
            String username=initDB.get("BNI.OGP.username");
//            String password="288edcc1-d577-4dfa-aafa-64c16c40c119";
            String password=initDB.get("BNI.OGP.password");
            String auth=username+":"+password;

            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("UTF-8")));
            String authHeader = "Basic " + new String( encodedAuth );
            map.put("Authorization", authHeader);
            map.put("Content-Type", "application/x-www-form-urlencoded");
//            String request="{\"grant_type\":\"client_credentials\"}";

            String request="grant_type=client_credentials";
            log.info("Authorization = "+map.get("Authorization"));
            String msgResponse = sendRequest(url, map, request, 45000, 45000);
            log.info("Token = "+msgResponse);
            token=mapper.readValue(msgResponse, TokenResVO.class);
            return token;
        }catch (Exception e){
            e.printStackTrace();
            throw new NostraException(e.getMessage(), StatusCode.ERROR);
        }
    }

    public String getSignaturetf(SignatureVO vo){
        String signature="";
        InitDB initDB=InitDB.getInstance();
        try{
//            String header="{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String header=initDB.get("BNI.OGP.JWT.header");
            JSONObject jsonObject=new JSONObject(header);
            header=jsonObject.toString();
//            String key="3409601e-c064-47c1-86d9-de6ea7cebb6e";
            String key=initDB.get("BNI.OGP.JWT.key");
            String encHeader=new String(Base64.encodeBase64(header.getBytes(Charset.forName("UTF-8")))).replace("+","-").replace("/","_").replace("=","");
            log.info("Header "+encHeader);
            String encRequest=new String(Base64.encodeBase64(vo.getText().getBytes(Charset.forName("UTF-8")))).replace("+","-").replace("/","_").replace("=","");
            log.info("Request "+encRequest);
            String data=encHeader+"."+encRequest;
            log.info("Data "+data);

            signature=getHMAC(key, data).replace("+","-").replace("/","_").replace("=","");
            log.info("Signature "+signature);
            signature=encHeader+"."+encRequest+"."+signature;

            return signature;
        }catch (Exception e){
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
    }
    
    public String getHMAC(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return Base64.encodeBase64String(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
    }
    
    protected int getTimeout(String param) {
        String timeout = InitDB.getInstance().get(param);
        if (timeout == null) {
            if (param.contains("Connect")) {
                timeout = InitDB.getInstance().get("BNI.OGP.Connect.Timeout");
            } else if (param.contains("Read")) {
                timeout = InitDB.getInstance().get("BNI.OGP.Read.Timeout");
            }
        }

        return (timeout == null) ? -1 : Integer.parseInt(timeout);
    }
    
    public ParamsVO createParamsVO(String name, String value){
        try{
            ParamsVO result=new ParamsVO();
            result.setName(name);
            result.setValue(value);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
    }
    
    public String getCurrentDate(){
        String result="";
        try{
            LocalDateTime myDateObj = LocalDateTime.now();
            result=myDateObj.toString();
//            result=result.substring(0,result.indexOf('.'));
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
    }
}
