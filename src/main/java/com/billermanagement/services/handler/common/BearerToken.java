package com.billermanagement.services.handler.common;

import com.billermanagement.services.HttpProcessingService;
import com.billermanagement.services.handler.token.BriToken;
import com.billermanagement.util.InitDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class BearerToken {

    private Logger logger = LoggerFactory.getLogger(BearerToken.class);
    private static final Map<String,String> bearerMap = new HashMap<>();

    @Autowired
    private HttpProcessingService httpProcessingService;

    @Autowired
    private RequestHeader requestHeader;

    @Autowired
    private BriToken briToken;

    public String getToken(String billerAlias) throws Exception{
        String tokenExpiredMapKey = new StringBuilder("Token_Expired.").append(billerAlias).toString();
        String tokenMapKey = new StringBuilder("Token.").append(billerAlias).toString();

        String tokenExpired = bearerMap.get(tokenExpiredMapKey);
        if (tokenExpired == null) {
            requestToken(billerAlias);
        } else {
            String tokenStartMapKey = new StringBuilder("Token_Start.").append(billerAlias).toString();
            long elapsed = System.currentTimeMillis() - Long.parseLong(bearerMap.get(tokenStartMapKey));
            if (elapsed >= Integer.parseInt(tokenExpired)) requestToken(billerAlias);
        }

        return bearerMap.get(tokenMapKey);
    }

    private int getTimeout(String param) {
        String timeout = InitDB.getInstance().get(param);
        if (timeout == null) {
            if (param.contains("connect")) {
                timeout = InitDB.getInstance().get("http.connect.timeout");
            } else if (param.contains("read")) {
                timeout = InitDB.getInstance().get("http.read.timeout");
            }
        }
        return (timeout == null) ? -1 : Integer.parseInt(timeout);
    }

    private String requestToken(String billerAlias)throws Exception {
        String clientIdKey = billerAlias+".ClientId";
        String clientSecretKey = billerAlias+".ClientSecret";

        Map<String,String> mapHeader = requestHeader.getHeaderToken(billerAlias);

        String payload = tokenBody(billerAlias,clientIdKey,clientSecretKey);

        logger.info("Request Bearer Token : " + billerAlias);
        logger.info("Request Token URL : " + InitDB.getInstance().get(billerAlias+".TokenURL"));
        logger.info("Request Token Body : " + payload);

        String httpRes = httpProcessingService.postUser(InitDB.getInstance().get(billerAlias+".TokenURL"), mapHeader, payload,getTimeout(billerAlias+".connect.timeout"),getTimeout(billerAlias+".read.timeout"));

        TokenResponseVO resVO = tokenResponse(billerAlias,httpRes);

        String token = resVO.getAccess_token();
        String expiredDate = resVO.getExpires_in();
        int tokenExpired = Integer.parseInt(expiredDate) * 1000;

        bearerMap.put("Token." + billerAlias, token);
        bearerMap.put("Token_Start." + billerAlias, Long.toString(System.currentTimeMillis()));
        bearerMap.put("Token_Expired." + billerAlias, Integer.toString(tokenExpired));

        return token;
    }

    private String tokenBody(String billerAlias,String clientId,String clientSecret){
        String body=null;
        if (billerAlias.equals("BRI")){
            body = briToken.getBody(clientId,clientSecret);
        }
        return body;
    }

    private TokenResponseVO tokenResponse(String billerAlias,String responseBody) throws IOException {
        TokenResponseVO responseVO = new TokenResponseVO();
        if (billerAlias.equals("BRI")){
            responseVO = briToken.getResponse(responseBody);
        }
        return responseVO;
    }

}
