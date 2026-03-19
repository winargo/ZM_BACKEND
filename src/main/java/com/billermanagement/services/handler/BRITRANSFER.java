package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.TransHistory;
import com.billermanagement.persistance.domain.TransTmp;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.persistance.repository.TransTmpRepository;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.backend.ParamsVO;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;


@Service
@Qualifier("britransfer")
public class BRITRANSFER extends BillerRequest {
    @Autowired
    ApiConfig apiConfig;

    @Autowired
    SLATime slaTimeService;

    private final InitDB initDB = InitDB.getInstance();
    private boolean isPending = false;
    private String[] asyncStatus = null;
    private String sourceAccount = "";

    private static final Map<String,String> briMap = new HashMap<>();

    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        try {
            String productCode = vo.getProductCode();
            String briid = apiConfig.get(productCode).toLowerCase();
            String acctCode = null;
            if(initDB.get("Bank.Code.BRI." + vo.getAccountCode()) != null){
                acctCode = vo.getAccountCode();
                String bankCode = initDB.get("Bank.Code.BRI." + acctCode);
                vo.setAccountCode(bankCode);
            }

            if (vo.getMethod().equals("Payment")){
                vo.setAmount(vo.getAmount()+".00");
            }

            List<ParamsVO> paramsVOList= new ArrayList<>();

            if (vo.getParams() != null){
                paramsVOList = vo.getParams();
            }

            if (initDB.get("BRITRANSFER.Custodian") != null){
                sourceAccount = initDB.get("BRITRANSFER.Custodian");
                paramsVOList.add(createParamsVO("sourceAccount",sourceAccount));
            }

            if (initDB.get("BRITRANSFER.FeeType") != null){
                paramsVOList.add(createParamsVO("FeeType",initDB.get("BRITRANSFER.FeeType")));
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            paramsVOList.add(createParamsVO("TransDateTime",sdf.format(new Date())));

            boolean isRemark = false;
            for (ParamsVO param : paramsVOList) {
                if (param.getName().equals("Remark")) {
                    isRemark = true;
                } else {
                    continue;
                }
            }

            if (isRemark==false && initDB.get("BRITRANSFER.Remark") != null){
                paramsVOList.add(createParamsVO("Remark",initDB.get("BRITRANSFER.Remark")));
            }
            vo.setParams(paramsVOList);
            String[] request = getRequest(vo, billerResult, 20);
            Map<String,String> mapHeader = getHttpHeader(request[1], request[0]);
            logger.info("BRITRANSFER.request: " + request[0]);
            String msgResponse = sendRequest(request[1], mapHeader, request[0]);
            logger.info("BRITRANSFER.response: " + msgResponse);

            vo.setAccountCode(acctCode);
            Object obj = getResponse(vo, billerResult, msgResponse);

            if (initDB.get("BRITRANSFER.async.code") != null) {
                asyncStatus = initDB.get("BRITRANSFER.async.code").split(";");
            }

            String responseCodeBE=msgResponse;
            if (msgResponse.contains("responseCode")){
                int firstIndex = msgResponse.lastIndexOf("responseCode");
                int lastIndex = msgResponse.indexOf("responseDescription");
                responseCodeBE = msgResponse.substring(firstIndex, lastIndex).replaceAll("[\":,]", "").replace("responseCode", "");
                logger.info("responseCodeBE : " + responseCodeBE);
            }

            for (String async : asyncStatus) {
                logger.info("BRI Async Code :" + async);
                if (responseCodeBE.toUpperCase().contains(async)) {
                    isPending = true;
                }
            }
            logger.info("isPending :" + isPending);

            Object[] result;
            String status;
            if (isPending)  {
                System.out.println(">>> BRI PENDING TRANS");
                isPending = false;

                status = PENDING;
                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltResponse);
                obj = cleanUpResponse(result[0],vo, billerResult);
                savePendingTransaction(vo, billerResult, request[1], msgResponse, HandlerConstant.BRI);
                saveTransaction(vo, billerResult, status);
            } else {
                System.out.println(">>> BRI OK TRANS");
                status = transStatus;
                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltCallback);
                obj = cleanUpResponse(result[0],vo, billerResult);
                saveTransaction(vo, billerResult, status);
            }
//            obj = cleanUpResponse(result[0], billerResult);
//            saveTransaction(vo, billerResult, status);
            updateBmLogResponse(vo,new ObjectMapper().writeValueAsString(obj),msgResponse);

            return obj;
        } catch (Exception e) {
            saveTransaction(vo, billerResult, ERROR);
            String errorMsg = "Unexpected Response - "+e.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
            logger.info("BRI Exception "+ e.getMessage());
            throw e;
        }
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

    private Map<String,String> getHttpHeader(String url, String data) throws Exception {
        String clientSecretKey = new StringBuilder("BRITRANSFER.client.secret").toString();
        String token = getToken("transfer");

        String secretKey = InitDB.getInstance().get(clientSecretKey);
        String timestamp = Instant.now().toString();
        String signature = getSignature(url, token, timestamp, data, secretKey);

        Map<String,String> map = new HashMap<>();
        map.put("Content-Type", "application/json");
        map.put("Authorization", "Bearer " + token);
        map.put("BRI-Signature", signature);
        map.put("BRI-Timestamp", timestamp);

        return map;
    }

    private String getSignature(String url, String token, String timestamp, String data, String secretKey) throws Exception {
        //path=/v1.0/brifast-incoming-remittance&verb=POST&token=Bearer R04XSUbnm1GXNmDiXx9ysWMpFWBr&timestamp=2019-01-02T13:14:15.678Z&body=
        String path = url.substring(url.indexOf('/', 9));

        String message = new StringBuilder().append("path=").append(path).append("&verb=POST&token=Bearer ").append(token)
                .append("&timestamp=").append(timestamp).append("&body=").append(data).toString();

        //Base64 SHA256-HMAC
        Mac hmacSha256;
        try {
            hmacSha256 = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException nsae) {
            hmacSha256 = Mac.getInstance("HMAC-SHA-256");
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        hmacSha256.init(secretKeySpec);

        return Base64.getEncoder().encodeToString(hmacSha256.doFinal(message.getBytes("UTF-8")));

    }

    private String getToken(String briid) throws Exception {
        String tokenExpiredMapKey = new StringBuilder("Token_Expired.").append(briid).toString();
        String tokenMapKey = new StringBuilder("Token.").append(briid).toString();

        String tokenExpired = briMap.get(tokenExpiredMapKey);
        //System.out.println(">>> BRI Token_Expired:" + tokenExpired);
        if (tokenExpired == null) {
            requestToken();
        } else {
            String tokenStartMapKey = new StringBuilder("Token_Start.").append(briid).toString();
            long elapsed = System.currentTimeMillis() - Long.parseLong(briMap.get(tokenStartMapKey));
            //System.out.println(">>> BRI elapsed:" + elapsed);
            if (elapsed >= Integer.parseInt(tokenExpired)) requestToken();
        }

        return briMap.get(tokenMapKey);
    }

    private String requestToken() throws Exception {
        //String url = "https://partner.api.bri.co.id/oauth/client_credential/accesstoken?grant_type=client_credentials";
        String clientIdKey = "BRITRANSFER.client.id";
        String clientSecretKey = "BRITRANSFER.client.secret";

        String urlParameters = new StringBuilder().append("client_id=").append(InitDB.getInstance().get(clientIdKey))
                .append("&client_secret=").append(InitDB.getInstance().get(clientSecretKey)).toString();

        Map<String,String> map = new HashMap<>();
        map.put("Content-Type", "application/x-www-form-urlencoded");

        System.out.println(">>> BRITRANSFER.url:" + InitDB.getInstance().get("BRITRANSFER.token.url"));
        System.out.println(">>> BRITRANSFER.param.url:" + urlParameters);

        String httpRes = sendRequest(InitDB.getInstance().get("BRITRANSFER.token.url"), map, urlParameters);

        httpRes = httpRes.replace("developer.email", "developer_email");
        ObjectMapper mapper = new ObjectMapper();
        TokenResponseVO resVO = mapper.readValue(httpRes, TokenResponseVO.class);

        String token = resVO.getAccess_token();
        String expiredDate = resVO.getExpires_in();
        int tokenExpired = Integer.parseInt(expiredDate) * 1000;

        briMap.put("Token.transfer", token);
        briMap.put("Token_Start.transfer", Long.toString(System.currentTimeMillis()));
        briMap.put("Token_Expired.transfer", Integer.toString(tokenExpired));

        return token;
    }

    @Autowired
    private TransTmpRepository transTmpRepository;

    @Transactional
    public void checkStatus(String transId, int handler) throws Exception {
        System.out.println(">>> BRITRANSFER.checkStatus.start");
        long start = System.currentTimeMillis();

        TransTmp record = transTmpRepository.findTrans(transId, handler);
        boolean success = false;
        try {
            String request = record.getRequest();

            ObjectMapper mapper = new ObjectMapper();
            RequestVO requestVO = mapper.readValue(request, RequestVO.class);

            List<ParamsVO> params = requestVO.getParams();
            String transDateTime = "";
            for (ParamsVO param : params) {
                if (param.getName().equals("TransDateTime")) {
                    transDateTime = param.getValue();
                    break;
                }
            }
            if (transDateTime.length() > 10) transDateTime = transDateTime.substring(0, 10);

            CheckStatusVO vo = new CheckStatusVO();
//            vo.setNoReferral(requestVO.getReffId());
            vo.setNoReferral(record.getBmTid());
            vo.setTransactionDate(transDateTime);

            String data = mapper.writeValueAsString(vo);
            success = checkTrans(data, record);
            if (success) {
                transTmpRepository.delete(record);
            }else{
                if (record.getSlaTime() == null || record.getSlaTime().toString().isEmpty()){
                    this.logger.info("Start Put SLA Time to TransTmp");
                    int slaInSeconds=0;
                    if (initDB.get("BRITRANSFER.Pending.SLA") !=null){
                        slaInSeconds = Integer.valueOf(initDB.get("BRITRANSFER.Pending.SLA"));
                    }
                    Date slaTime = slaTimeService.getTransTmpSLATime(slaInSeconds,record.getId());
                    record.setSlaTime(slaTime);
                    transTmpRepository.save(record);
                }
            }
        } finally {
            logger.info("checkStatus:" + transId + "," + handler + "," + success + "," + (System.currentTimeMillis() - start) + "ms");
            System.out.println(">>> BRITRANSFER.checkStatus.end");
        }
    }

    @Transactional
    boolean checkTrans(String message, TransTmp record) throws Exception {
        System.out.println(">>> BRITRANSFER.checkTrans.start");
        String msgResponse;
        TransHistory transHistory = new TransHistory();
        try {
            Map<String,String> mapHeader = getHttpHeader(InitDB.getInstance().get("BRITRANSFER.status.url"), message);
            logger.info("BRITRANSFER.checkStatus.request: " + message);
            msgResponse = sendRequest(InitDB.getInstance().get("BRITRANSFER.status.url"), mapHeader, message);
            logger.info("BRITRANSFER.checkStatus.response: " + msgResponse);
        } catch (Exception e) {
            return false;
        }

        if (initDB.get("BRITRANSFER.async.code") != null) {
            asyncStatus = initDB.get("BRITRANSFER.async.code").split(";");
        }

        String responseCodeBE=msgResponse;
        if (msgResponse.contains("internalTransferStatus")){
            int firstIndex = msgResponse.lastIndexOf("internalTransferStatus");
            int lastIndex = msgResponse.indexOf("internalTransferDescription");
            responseCodeBE = msgResponse.substring(firstIndex, lastIndex).replaceAll("[\":,]", "").replace("internalTransferStatus", "");
            logger.info("BRITransferCodeStatus : " + responseCodeBE);

            int firstIndex2 = msgResponse.lastIndexOf("responseCode");
            int lastIndex2 = msgResponse.indexOf("responseDescription");
            String respCode="";
            respCode = msgResponse.substring(firstIndex2, lastIndex2).replaceAll("[\":,]", "").replace("responseCode", "");
            msgResponse = msgResponse.replaceAll(respCode,responseCodeBE);
        }

        for (String async : asyncStatus) {
            logger.info("BRITRANSFER Async Code :" + async);
            if (responseCodeBE.toUpperCase().contains(async)) {
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
                logger.info("BRITRANSFER.checkTrans.error : " + ex.getMessage());
                return false;
            }

            Object obj = getCallbackInfo(map, record);

            //System.out.println(">>> BRITRANSFER.toTransform:" + record.getTransformId() + "," + record.getMethod() + "," + obj);
            Object[] result = transformService.transformApi(record.getTransformId(), record.getMethod(), obj, Jolt.JoltCallback);

            //System.out.println(">>> result-0:" + result[0]);
            //System.out.println(">>> result-1:" + result[1]);
            //System.out.println(">>> result-2:" + result[2]);

            transHistory.setBmTid(record.getBmTid());
            transHistory.setPartnerTid(record.getPartnerTid());
            String partnerMsg = mapper.writeValueAsString(processCallbackResponse(result[0], record,transHistory));
            //System.out.println(">>> BRITRANSFER.partnerMsg:" + partnerMsg);

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

            System.out.println(">>> BRITRANSFER.checkTrans.end");
            return true;
        }
    }

    public BRITRANSFER(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    /*public String test() {
        try {
            //getToken();

            String url = "https://sandbox.partner.api.bri.co.id/v1.0/brifast-incoming-remittance/inquiryaccount";
            String data = "{\"bankCode\":\"002\", \"accountNumber\":\"020601000110568\"}";
            String msgResponse = sendRequest(url, getHttpHeader(url, data, "remit"), data);
            System.out.println(msgResponse);

            //Vostro
            url = "https://sandbox.partner.api.bri.co.id/v1.0/brifast-incoming-remittance/inquiryvostro";
            data = "{\"currency\":\"IDR\"}";
            //msgResponse = sendRequest(url, getHttpHeader(url, data), data);
            //System.out.println(msgResponse);

            //Payment Account
            url = "https://sandbox.partner.api.bri.co.id/v1.0/brifast-incoming-remittance/paymentaccount";
            data = "{\"referenceNumber\":\"TPA000001\", \"creditAccount\":\"020601000110568\", \"benefName\":\"TEGAR\", \"benefAddress\":\"Jakarta\", \"benefPhone\":\"\", \"benefEmail\":\"\", \"benefId\":\"12345678\", \"benefIdType\":\"4\", \"countryIdBenef\":\"ID\", \"senderName\":\"Bambang Sutatik\", \"senderAddress\":\"Sabah\", \"senderPhone\":\"\", \"senderEmail\":\"\", \"senderId\":\"5234234\", \"senderIdType\":\"4\", \"countryIdSender\":\"MY\", \"debetCurrency\":\"IDR\", \"creditCurrency\":\"IDR\", \"bankCode\":\"002\", \"amount\":\"100000\", \"remark\":\"Test\", \"countrySourceTrx\":\"MY\"}";
            //msgResponse = sendRequest(url, getHttpHeader(url, data), data);
            //System.out.println(msgResponse);

            //Payment Cash
            url = "https://sandbox.partner.api.bri.co.id/v1.0/brifast-incoming-remittance/paymentcash";
            data = "{\"referenceNumber\":\"TPC000001\",\"benefName\":\"Ferguso\",\"benefAddress\":\"Jakarta\",\"benefPhone\":\"\",\"benefEmail\":\"\",\"benefId\":\"42132234\",\"benefIdType\":\"4\",\"countryIdBenef\":\"ID\",\"senderName\":\"Bambank\",\"senderAddress\":\"Sabah\",\"senderPhone\":\"\",\"senderEmail\":\"\",\"senderId\":\"7896759\",\"senderIdType\":\"4\",\"countryIdSender\":\"MY\",\"debetCurrency\":\"IDR\",\"creditCurrency\":\"IDR\",\"bankCode\":\"002\",\"amount\":\"120000\",\"remark\":\"Test Cash Pickup\",\"countrySourceTrx\":\"MY\"}";
            //msgResponse = sendRequest(url, getHttpHeader(url, data), data);
            //System.out.println(msgResponse);

            //Cancel Transaction
            url = "https://sandbox.partner.api.bri.co.id/v1.0/brifast-incoming-remittance/canceltransaction";
            data = "{\"referenceNumber\": \"TPA000002\",\"remark\":\"Test Cancel\"}";
            msgResponse = sendRequest(url, getHttpHeader(url, data, "remit"), data);
            System.out.println(msgResponse);

            //Inquiry Transaction
            url = "https://sandbox.partner.api.bri.co.id/v1.0/brifast-incoming-remittance/canceltransaction";
            data = "{\"referenceNumber\": \"TPA000001\"}";
            msgResponse = sendRequest(url, getHttpHeader(url, data, "remit"), data);
            System.out.println(msgResponse);
            data = "{\"referenceNumber\": \"TPC000001\"}";
            msgResponse = sendRequest(url, getHttpHeader(url, data, "remit"), data);
            System.out.println(msgResponse);
            data = "{\"referenceNumber\": \"TPC000002\"}";
            msgResponse = sendRequest(url, getHttpHeader(url, data, "remit"), data);
            System.out.println(msgResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "OK";
    }*/

    @Data
    private static class TokenResponseVO {
        private String refresh_token_expires_in;
        private String api_product_list;
        private String[] api_product_list_json;
        private String organization_name;
        private String developer_email;
        private String token_type;
        private String issued_at;
        private String client_id;
        private String access_token;
        private String application_name;
        private String scope;
        private String expires_in;
        private String refresh_count;
        private String status;
    }

    @Data
    private class CheckStatusVO {
        private String noReferral;
        private String transactionDate;
    }
}
