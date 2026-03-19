package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.services.HttpProcessingService;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Qualifier("indogold")
public class Indogold extends BillerRequest {
    private static final Map<String,String> indoGoldMap = new HashMap<>();
    //private static final int TIMEOUT = 60000; //1-minute
    //private static final String URL = "https://stagingapiv3.indogold.com/api/token/get";

    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        try {
            String[] request = getRequest(vo, billerResult, 20);

            Map<String,String> mapHeader = getHttpHeader(0);
            String msgResponse = sendRequest(request[1], mapHeader, request[0]);

            if (msgResponse.toUpperCase().contains("PLEASE PROVIDE CORRECT TOKEN")) {
                mapHeader = getHttpHeader(1);
                msgResponse = sendRequest(request[1], mapHeader, request[0]);
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

    private Map<String,String> getHttpHeader(int type) throws Exception {
        String token = (type == 0) ? getToken() : requestToken();
        Map<String,String> map = new HashMap<>();
        map.put("indogold-token", token);

        return map;
    }

    private String getToken() throws Exception {
        String tokenExpired = indoGoldMap.get("Token-Expired");
        if (tokenExpired == null) {
            requestToken();
            tokenExpired = indoGoldMap.get("Token-Expired");
        }

        //long nowMillis = ZonedDateTime.now().toInstant().toEpochMilli();
        long i = Long.parseLong(tokenExpired) - System.currentTimeMillis();

        //if (i < TIMEOUT) requestToken();
        if (i < (Integer.parseInt(InitDB.getInstance().get("indogold.token.refresh")) * 1000)) requestToken();

        //System.out.println("token:" + indoGoldMap.get("Token"));

        return indoGoldMap.get("Token");
    }

    private String requestToken() throws Exception {
        /*{
            "name": "kaspro",
            "secret_key": "63a19cca40f51684adbde84ff68a48b7",
            "device_id": "lenovo_e460",
            "device_type": "postman"
        }*/
        TokenRequestVO reqVO = new TokenRequestVO();
        //reqVO.setName("kaspro");
        //reqVO.setSecret_key("63a19cca40f51684adbde84ff68a48b7");
        //reqVO.setDevice_id("lenovo_e460");
        //reqVO.setDevice_type("postman");

        reqVO.setName(InitDB.getInstance().get("indogold.name"));
        reqVO.setSecret_key(InitDB.getInstance().get("indogold.secret.key"));
        reqVO.setDevice_id(InitDB.getInstance().get("indogold.device.id"));
        reqVO.setDevice_type(InitDB.getInstance().get("indogold.device.type"));

        ObjectMapper mapper = new ObjectMapper();
        String httpReq = mapper.writeValueAsString(reqVO);

        HttpProcessingService http = new HttpProcessingService();
        //String httpRes = http.postUser(URL, httpReq);
        String httpRes = http.postUser(InitDB.getInstance().get("indogold.token.url"), httpReq);

        /*{
            "api_version": "0.0.1",
            "memory_usage": "353.66 KB",
            "elapse_time": "0.05",
            "lang": "en",
            "error": {},
            "data": {
                "success": true,
                "token": {
                    "id": 26,
                    "apps_id": 7,
                    "device_id": "lenovo_e460",
                    "device_type": "postman",
                    "ip": "::1",
                    "token_code": "ZUEKkdSU0lnpbUM74ie2zhQ1wWq6qggv",
                    "refresh_token": "efNCViJtFXdEGk6ZAgXp46UuKBzKDXFm",
                    "created_date": "2019-02-12T04:15:59.000Z",
                    "expired_date": "2019-02-15T07:03:48.000Z",
                    "token_profile": {
                        "user_id": null,
                        "admin_id": null,
                        "last_activity": "2019-02-14T07:03:48.000Z"
                    }
                }
            }
        }*/
        TokenResponseVO resVO = mapper.readValue(httpRes, TokenResponseVO.class);

        String token = resVO.getData().getToken().getToken_code();
        String expiredDate = resVO.getData().getToken().getExpired_date();

        System.out.println(">>>>> token:" + token + ",expiredDate:" + expiredDate);
        long tokenExpired = Instant.parse(expiredDate).toEpochMilli();

        indoGoldMap.put("Token", token);
        indoGoldMap.put("Token-Expired", Long.toString(tokenExpired));

        return token;
    }

    @Data
    private static class TokenRequestVO {
        private String name;
        private String secret_key;
        private String device_id;
        private String device_type;
    }
}

@Data
class TokenResponseVO {
    private String api_version;
    private String memory_usage;
    private String elapse_time;
    private String lang;
    private Error error;
    private ResponseData data;
}

@Data
class Error {

}

@Data
class ResponseData {
    private String success;
    private ResponseToken token;
}

@Data
class ResponseToken {
    private String id;
    private String apps_id;
    private String device_id;
    private String device_type;
    private String ip;
    private String token_code;
    private String refresh_token;
    private String created_date;
    private String expired_date;
    private TokenProfile token_profile;
}

@Data
class TokenProfile {
    private String user_id;
    private String admin_id;
    private String last_activity;
}

