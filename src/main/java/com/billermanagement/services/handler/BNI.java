package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.services.ui.ApiService;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.BNI.*;
import com.billermanagement.vo.backend.ParamsVO;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@Qualifier("bni")
public class BNI extends BillerRequest{
    
    @Autowired
    private ApiConfig apiConfig;
    
    @Autowired
    private BNITransfer bniTransfer;
    
    @Override
    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        log.info("Entering BNI Handler");
        InitDB initDB=InitDB.getInstance();
        
        String currDate="";
        String code="";
        Object obj = null;
        try {

            if(initDB.get("Transfer.Category").toUpperCase().contains(apiConfig.get(vo.getProductCode()).toUpperCase())){
                obj = bniTransfer.process(vo, billerResult);
            }else{
                if(vo.getMethod().equalsIgnoreCase("Payment")){
                    List<ParamsVO>  paramsVOS = vo.getParams();
                    currDate=getCurrentDate();
                    paramsVOS.add(createParamsVO("trxDate",currDate));
                    if(vo.getAccountCode().equalsIgnoreCase("BNI")){
                        code=initDB.get("RTGS.Code."+vo.getAccountCode());
                        if(code!=null){
                            vo.setAccountCode(code);
                        }

                        paramsVOS.add(createParamsVO("ServiceType","BNI"));
                    }else{
                        Long amount=new Long(vo.getAmount());
                        Long interbankMax=new Long(50000000);
                        Long rtgsMin=new Long(100000000);
                        code="";
                        if(amount<=interbankMax){
                            code=initDB.get("Bank.Code."+vo.getAccountCode());
                            if(code!=null){
                                vo.setAccountCode(code);
                            }
                            paramsVOS.add(createParamsVO("ServiceType","INTERBANK"));
                        }else if(amount>=rtgsMin){
                            code=initDB.get("RTGS.Code.")+vo.getAccountCode();
                            if(code!=null){
                                vo.setAccountCode(code);
                            }
                            paramsVOS.add(createParamsVO("ServiceType","RTGS"));
                        }else {
                            throw new NostraException("Amount must not more than "+interbankMax+" but less than "+rtgsMin);
                        }

                    }
                    vo.setParams(paramsVOS);
                    log.info(vo.toString());

                }else {
                    code=initDB.get("Bank.Code."+vo.getAccountCode());
                    if(code!=null){
                        vo.setAccountCode(code);
                    }
                }

                String[] request = getRequest(vo, billerResult, 16);
                String req=request[0];
                log.info("Request "+req);
                SignatureVO signatureVO=new SignatureVO();
                signatureVO.setText(req);
                String signature=",\"signature\":\""+getSignature(signatureVO)+"\"}";
                log.info("Signature "+signature);
                req=request[0].substring(0,request[0].length()-1)+signature;
                log.info("Request "+req);
                String msgResponse;
                try {
                    Map<String, String> map =getHeader();
                    String newUrl=generateURL(request[1]);
                    msgResponse = sendRequest(newUrl, map, req, 45000, 45000);
                } catch (Exception e) {
                    e.printStackTrace();
                    msgResponse = e.getMessage();
                }

                obj = getResponse(vo, billerResult, msgResponse);

                Object[] result;
                if (msgResponse.toUpperCase().contains("OK"))  {
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
            }
 
            return obj;
        } catch (Exception e) {
            saveTransaction(vo, billerResult, ERROR);
            String errorMsg = "Unexpected Response - "+e.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
            throw e;
        }
    }

    public TokenResVO getBNIToken(){
        TokenResVO token = new TokenResVO();
        Map<String, String> map = new HashMap<String, String>();
        InitDB initDB=InitDB.getInstance();
        ObjectMapper mapper = new ObjectMapper();

        try {
//            String url="https://apidev.bni.co.id:8066/api/oauth/token";
            String url = initDB.get("BNI.url.host")+initDB.get("BNI.url.token");
//            String username="e0e98788-e2bf-4a71-86a5-e43d0f28d418";
            String username=initDB.get("BNI.username");
//            String password="288edcc1-d577-4dfa-aafa-64c16c40c119";
            String password=initDB.get("BNI.password");
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

    public String getHMAC(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return Base64.encodeBase64String(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
    }

    public String getSignature(SignatureVO vo){
        String signature="";
        InitDB initDB=InitDB.getInstance();
        try{
//            String header="{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String header=initDB.get("BNI.JWT.header");
            JSONObject jsonObject=new JSONObject(header);
            header=jsonObject.toString();
//            String key="3409601e-c064-47c1-86d9-de6ea7cebb6e";
            String key=initDB.get("BNI.JWT.key");
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

    public Map<String, String> getHeader(){
        Map<String, String> map=new HashMap<String, String>();
        InitDB initDB=InitDB.getInstance();
        try{
//            String apiKey="ea52e4bd-6b95-4b73-acf3-5e9f974e8528";
            String apiKey=initDB.get("BNI.apiKey");
            map.put("x-api-key", apiKey);
            return map;
        }catch (Exception e){
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
    }

    public String generateURL(String url){
        String newURL="";
        InitDB initDB=InitDB.getInstance();
        try{
//            String clientID="REPMI";
            String clientID=initDB.get("BNI.clientID");
            TokenResVO tokenResVO=getBNIToken();
            String token=tokenResVO.getToken();
            newURL=url+clientID+"?access_token="+token;
            return newURL;
        }catch (Exception e){
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
    }

    public InquiryResVO inquiryBNI(InquiryReqVO vo){
        InquiryResVO response = new InquiryResVO();
        ObjectMapper obj = new ObjectMapper();
        InitDB initDB=InitDB.getInstance();
//        String url="https://apidev.bni.co.id:8066/remittance/accountInfoInquiry/";
        String url=initDB.get("BNI.url.host")+initDB.get("BNI.url.inquiry");
        log.info("URL = "+url);
        try{
            String request= obj.writeValueAsString(vo);
            JSONObject jsonObject = new JSONObject(request);
            jsonObject.remove("signature");
            request=jsonObject.toString();
            log.info("Request "+request);

            SignatureVO signatureVO = new SignatureVO();
            signatureVO.setText(request);
            String signature=getSignature(signatureVO);
            vo.setSignature(signature);

            request=obj.writeValueAsString(vo);
            log.info("Request "+request);

            Map<String, String> map =getHeader();
            String newUrl=generateURL(url);
            String msgResponse = sendRequest(newUrl, map, request, 45000, 45000);
            response=obj.readValue(msgResponse, InquiryResVO.class);
            return response;
        }catch (Exception e){
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
    }

    public InquiryPOResVO inquiryPO(InquiryPOReqVO vo){
        InquiryPOResVO response = new InquiryPOResVO();
        ObjectMapper obj = new ObjectMapper();
        InitDB initDB = InitDB.getInstance();
//        String url="https://apidev.bni.co.id:8066/remittance/poInfoInquiry/";
        String url = initDB.get("BNI.url.host")+initDB.get("BNI.url.inquiryPO");
        log.info("URL = "+url);
        try{
            String request= obj.writeValueAsString(vo);
            JSONObject jsonObject = new JSONObject(request);
            jsonObject.remove("signature");
            request=jsonObject.toString();
            log.info("Request "+request);

            SignatureVO signatureVO = new SignatureVO();
            signatureVO.setText(request);
            String signature=getSignature(signatureVO);
            vo.setSignature(signature);

            request=obj.writeValueAsString(vo);
            log.info("Request "+request);

            Map<String, String> map =getHeader();
            String newUrl=generateURL(url);
            String msgResponse = sendRequest(newUrl, map, request, 45000, 45000);
            response=obj.readValue(msgResponse, InquiryPOResVO.class);
            return response;
        }catch (Exception e){
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
    }

    public POResVO createPO(POReqVO vo){
        POResVO response = new POResVO();
        ObjectMapper obj = new ObjectMapper();
        InitDB initDB=InitDB.getInstance();
//        String url="https://apidev.bni.co.id:8066/remittance/processPO/";
        String url=initDB.get("BNI.url.host")+initDB.get("BNI.url.processPO");
        log.info("URL = "+url);
        try{
            String request= obj.writeValueAsString(vo);
            JSONObject jsonObject = new JSONObject(request);
            jsonObject.remove("signature");
            request=jsonObject.toString();
            log.info("Request "+request);

            SignatureVO signatureVO = new SignatureVO();
            signatureVO.setText(request);
            String signature=",\"signature\":\""+getSignature(signatureVO)+"\"}";
            vo.setSignature(signature);

            request=request.substring(0,request.length()-1)+signature;
            log.info("Request with signature "+request);

            Map<String, String> map =getHeader();
            String newUrl=generateURL(url);
            String msgResponse = sendRequest(newUrl, map, request, 45000, 45000);
            response=obj.readValue(msgResponse, POResVO.class);
            return response;
        }catch (Exception e){
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
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
