package com.billermanagement.services.handler.common;


import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.util.FormatUtil;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.backend.RequestVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class RequestHeader {

    @Autowired
    private BasicAuth basicAuth;

    @Autowired
    private BearerToken bearerToken;

    InitDB initDB = InitDB.getInstance();

    Logger logger = LoggerFactory.getLogger(RequestHeader.class);

    public Map<String, String> getHeader(RequestVO vo, BillerResult billerResult,String billerAlias,String url,String reqPayload,boolean isTokenReq) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        String token = null;
        String timeStamp = null;
        if (isTokenReq==false){
            //for api request
            logger.info("Headers Api Request");

            //Authorization
            String authType = initDB.get(billerAlias+".Authorization.Type");
            if (authType.toLowerCase().contains("basic")){
                if(initDB.get(billerAlias+".Authorization.Key") != null){
                    map.put(initDB.get(billerAlias+".Authorization.Key"), "Basic "+ basicAuth.getBasicAuth(billerAlias));
                }else{
                    map.put("Authorization", "Basic "+ basicAuth.getBasicAuth(billerAlias));
                }
            }else if (authType.toLowerCase().contains("bearer")){
                token = bearerToken.getToken(billerAlias);
                if(initDB.get(billerAlias+".BearerAuth.Key") != null){
                    map.put(initDB.get(billerAlias+".BearerAuth.Key"), "Bearer "+ token);
                }else{
                    map.put("Authorization", "Bearer "+ token);
                }
            }else if (authType.toLowerCase().contains("key")){
                //example value:
                //X-Username=username;X-Password=password
                String keyAuthHeader = initDB.get(billerAlias+".KeyAuthHeader.Key");
                String[] keysAuth = keyAuthHeader.split(";");

                for (String k : keysAuth){
                    int firstIndex =0;
                    int delimiterIndex = k.indexOf("=");
                    String keyName = k.substring(firstIndex,delimiterIndex);
                    String keyValue = k.substring(delimiterIndex+1);
                    map.put(keyName,keyValue);
                }
            }

            //Content-Type
            String contentType = initDB.get(billerAlias+".ContentType");
            if (contentType != null){
                map.put("Content-Type",contentType);
            }

            //Additional Headers
            String additionalHeaders = initDB.get(billerAlias+".Additional.Headers");
            if (additionalHeaders != null){
                //example value:
                //Key1=Value1;Key2=Value2;Key3=Value3
                String[] keysAuth = additionalHeaders.split(";");

                for (String k : keysAuth){
                    int firstIndex =0;
                    int delimiterIndex = k.indexOf("=");
                    String keyName = k.substring(firstIndex,delimiterIndex);
                    String keyValue = k.substring(delimiterIndex+1);
                    map.put(keyName,keyValue);
                }
            }

            //Timestamp
            String timeStampHeader = initDB.get(billerAlias+".Timestamp.Header");
            if (timeStampHeader != null){
                //example value: [KeyName];[dateFormat]
                //BRI-Timestamp;yyyyMMddHHmmss
                int indexOf = timeStampHeader.indexOf(";");
                String keyName = timeStampHeader.substring(0,indexOf);
                String dateFormat = timeStampHeader.substring(indexOf+1);
                String keyValue = FormatUtil.getTime(dateFormat);
                map.put(keyName,keyValue);
                timeStamp = keyValue;
            }

            //signature
            String signatureHeader = initDB.get(billerAlias+".Signature.Header");
            if (signatureHeader != null){
                //example value: [KeyName];[encryptMethod]
                //BRI-Signature;base64 hmac-sha256
                int indexOf = signatureHeader.indexOf(";");
                String keyName = signatureHeader.substring(0,indexOf);
                String encryptMethod = signatureHeader.substring(indexOf+1);
                String keyValue = null;
                String clientSecretKey = billerAlias+".ClientSecret";
                String secretKey = InitDB.getInstance().get(clientSecretKey);
                String message = reqPayload;

                if (billerAlias.equals("BRI")){
                    String path = url.substring(url.indexOf('/', 9));
                    message = new StringBuilder()
                            .append("path=").append(path)
                            .append("&verb=POST&token=Bearer ").append(token)
                            .append("&timestamp=").append(timeStamp)
                            .append("&body=").append(reqPayload).toString();
                }

                if (encryptMethod.toLowerCase().equals("base64 hmac-sha256")){
                    //Base64 SHA256-HMAC
                    Mac hmacSha256;
                    try {
                        hmacSha256 = Mac.getInstance("HmacSHA256");
                    } catch (NoSuchAlgorithmException nsae) {
                        hmacSha256 = Mac.getInstance("HMAC-SHA-256");
                    }
                    SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
                    hmacSha256.init(secretKeySpec);

                    keyValue = Base64.getEncoder().encodeToString(hmacSha256.doFinal(message.getBytes("UTF-8")));
                }
                map.put(keyName,keyValue);
            }

            //BMtid to header
            String bmTidToHeader = initDB.get(billerAlias+".BmTidHeader.Key");
            if (bmTidToHeader != null){
                //example value: [KeyName]
                //X-Req-Id
                String keyName = bmTidToHeader.trim();
                String keyValue = vo.getTransactionId();
                map.put(keyName,keyValue);
            }

        }else{
            //for token request
            logger.info("Headers Token Request");
            String authType = initDB.get(billerAlias+".Token.Authorization.Type");
            if (authType.toLowerCase().contains("basic")){
                if(initDB.get(billerAlias+".Authorization.Key") != null){
                    map.put(initDB.get(billerAlias+".Authorization.Key"), "Basic "+ basicAuth.getBasicAuth(billerAlias));
                }else{
                    map.put("Authorization", "Basic "+ basicAuth.getBasicAuth(billerAlias));
                }
            }else if (authType.toLowerCase().contains("key")){
                //example value:
                //X-Username=username;X-Password=password
                String keyAuthHeader = initDB.get(billerAlias+".KeyAuthHeader.Key");
                String[] keysAuth = keyAuthHeader.split(";");

                for (String k : keysAuth){
                    int firstIndex =0;
                    int delimiterIndex = k.indexOf("=");
                    String keyName = k.substring(firstIndex,delimiterIndex);
                    String keyValue = k.substring(delimiterIndex+1);
                    map.put(keyName,keyValue);
                }
            }

            String tokenContentType = initDB.get(billerAlias+".Token.ContentType");
            if (tokenContentType != null){
                map.put("Content-Type",tokenContentType);
            }
            //Additional Headers
            String additionalHeaders = initDB.get(billerAlias+".Token.Additional.Headers");
            if (additionalHeaders != null){
                //example value:
                //Key1=Value1;Key2=Value2;Key3=Value3
                String[] keysAuth = additionalHeaders.split(";");

                for (String k : keysAuth){
                    int firstIndex =0;
                    int delimiterIndex = k.indexOf("=");
                    String keyName = k.substring(firstIndex,delimiterIndex);
                    String keyValue = k.substring(delimiterIndex+1);
                    map.put(keyName,keyValue);
                }
            }

            //Timestamp
            String timeStampHeader = initDB.get(billerAlias+"Token.Timestamp.Header");
            if (timeStampHeader != null){
                //example value: [KeyName];[dateFormat]
                //BRI-Timestamp;yyyyMMddHHmmss
                int indexOf = timeStampHeader.indexOf(";");
                String keyName = timeStampHeader.substring(0,indexOf);
                String dateFormat = timeStampHeader.substring(indexOf+1);
                String keyValue = FormatUtil.getTime(dateFormat);
                map.put(keyName,keyValue);
            }

        }

        for (Map.Entry<String,String> entry : map.entrySet()){
            logger.info("Header ==>"+ entry.getKey() +":"+ entry.getValue());
        }

        return map;
    }

    public Map<String, String> getHeaderToken(String billerAlias) throws Exception {
        return getHeader(null,null,billerAlias,null,null,true);
    }

    public Map<String, String> getHeader(RequestVO vo, BillerResult billerResult,String billerAlias,String url,String reqPayload) throws Exception {
        return getHeader(vo,billerResult,billerAlias,url,reqPayload,false);
    }
}
