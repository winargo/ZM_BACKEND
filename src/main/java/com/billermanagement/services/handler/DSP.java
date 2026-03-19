package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.services.HttpProcessingService;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.security.NoSuchAlgorithmException;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Qualifier("dsp")
public class DSP extends BillerRequest {
    private static final Map<String,String> dspMap = new HashMap<>();

    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        try {
            String code = InitDB.getInstance().get("Bank.Code."+vo.getAccountCode());
            if(code!=null){
                vo.setAccountCode(code);
            }
            String[] request = getRequest(vo, billerResult, 20);

            Map<String,String> mapHeader = getHttpHeader(0, request[0]);
            String msgResponse = sendRequestBypassSSLCert(request[1], mapHeader, request[0], 1000, 9000);

            if (msgResponse.toUpperCase().contains("PLEASE PROVIDE CORRECT TOKEN")) {
                mapHeader = getHttpHeader(1, request[0]);
                msgResponse = sendRequestBypassSSLCert(request[1], mapHeader, request[0], 1000, 9000);
            }

            Object obj = getResponse(vo, billerResult, msgResponse);

            Object[] result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltResponse);

            obj = cleanUpResponse(result[0],vo, billerResult);

            saveTransaction(vo, billerResult, transStatus);

            updateBmLogResponse(vo,new ObjectMapper().writeValueAsString(obj),msgResponse);

            return obj;
        } catch (Exception e) {
            saveTransaction(vo, billerResult, ERROR);
            String errorMsg = "Unexpected Response - "+e.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
            throw e;
        }
    }

    private Map<String,String> getHttpHeader(int type, String payload) throws Exception {
        String secretKey = InitDB.getInstance().get("dsp.secret.key");
        String merchantId = InitDB.getInstance().get("dsp.merchant.id");
        String token = (type == 0) ? getToken() : requestToken();

        String signature = getSignature(merchantId, payload, secretKey);
        signature = new StringBuilder().append("Bearer ").append(signature).toString();
        String authorization = new StringBuilder().append("Bearer ").append(token).toString();

        Map<String,String> map = new HashMap<>();
        map.put("Authorization", authorization);
        map.put("DSP-SIGN", signature);

        return map;
    }

    private String getSignature(String merchantId, String payload, String secretKey) throws Exception {
        JSONObject jsonObject=new JSONObject("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String header=jsonObject.toString();
        String encHeader=Base64.getEncoder().encodeToString(header.getBytes(Charset.forName("UTF-8"))).replace("+","-").replace("/","_").replace("=","");
        
        String body=new JSONObject("{\"clientId\":"+merchantId+", \"request\":"+payload+"}").toString();
        String encRequest=Base64.getEncoder().encodeToString(body.getBytes(Charset.forName("UTF-8"))).replace("+","-").replace("/","_").replace("=","");
        String message=encHeader+"."+encRequest;

        //Base64 SHA256-HMAC
        Mac hmacSha256;
        try {
            hmacSha256 = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException nsae) {
            hmacSha256 = Mac.getInstance("HMAC-SHA-256");
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        hmacSha256.init(secretKeySpec);
        String signature = Base64.getEncoder().encodeToString(hmacSha256.doFinal(message.getBytes(Charset.forName("UTF-8")))).replace("+","-").replace("/","_").replace("=","");
        message = message+"."+signature;
        return message;

    }

    private String getToken() throws Exception {
        String tokenExpired = dspMap.get("Token_Expired");
        if (tokenExpired == null) {
            requestToken();
        } else {
            long elapsed = System.currentTimeMillis() - Long.parseLong(dspMap.get("Token_Start"));
            System.out.println("elapsed:" + elapsed);
            if (elapsed >= Integer.parseInt(tokenExpired)) requestToken();
        }

        return dspMap.get("Token");
    }

    private String requestToken() throws Exception {
        /*{
            "client_secret": "931aafdd-cda5-4c19-bd45-00afe493aecf",
            "client_id": "c086e663-b619-4db5-aee5-b942e201f5d3",
            "grant_type": "client_credentials"
        }*/

        String urlParameters = new StringBuilder().append("client_secret=").append(InitDB.getInstance().get("dsp.client.secret"))
                .append("&client_id=").append(InitDB.getInstance().get("dsp.client.id"))
                .append("&grant_type=").append(InitDB.getInstance().get("dsp.grant.type")).toString();
        Map<String,String> map = new HashMap<>();
        map.put("Content-Type", "application/x-www-form-urlencoded");
        map.put("Content-Type", "application/x-www-form-urlencoded");

        String httpRes = sendRequestBypassSSLCert(InitDB.getInstance().get("dsp.token.url"), map, urlParameters, 1000, 9000);

        /*{{
            "access_token": "JSJVBPyLISwCQNvVRdpd57ol566S7Lug756gdeR0TMei6uzRqsV9SO",
            "token_type": "Bearer",
            "expires_in": 3600,
            "scope": "resource.WRITE resource.READ"
        }*/
        ObjectMapper mapper = new ObjectMapper();
        TokenResponseVO resVO = mapper.readValue(httpRes, TokenResponseVO.class);

        String token = resVO.getAccess_token();
        String expiresIn = Integer.toString(resVO.getExpires_in());

        System.out.println(">>>>> token:" + token + ",expires_in:" + expiresIn);
        int tokenExpired = Integer.parseInt(expiresIn) * 1000;

        dspMap.put("Token", token);
        dspMap.put("Token_Start", Long.toString(System.currentTimeMillis()));
        dspMap.put("Token_Expired", Integer.toString(tokenExpired));

        return token;
    }

    @Data
    private static class TokenResponseVO {
        private String access_token;
        private String token_type;
        private int expires_in;
        private String scope;
    }


    @Data
    private static class DspRequestVO {
        @JsonProperty("beneName")
        private String beneName;
        @JsonProperty("msgId")
        private String msgId;
    }

}


