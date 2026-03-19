package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.persistance.domain.resultset.PartnerResult;
import com.billermanagement.util.Base64Util;
import com.billermanagement.util.InitDB;
import com.billermanagement.util.SHAUtils;
import com.billermanagement.vo.IDN.Xresponse;
import com.billermanagement.vo.backend.ParamsVO;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@Qualifier("idn")
public class IDN extends BillerRequest {

    @Autowired
    SHAUtils shaUtil;

    @Autowired
    Base64Util base64Util;

    @Autowired
    private StatusMapping statusMapping;

    InitDB initDB = InitDB.getInstance();

    @Override
    public Object process(RequestVO vo, BillerResult billerResult) {
        log.info("Entering IDN Handler");
//        InitDB initDB = InitDB.getInstance();
        String asyncStatus = "503";
        Object obj = null;
        try {
            String[] request = getRequest(vo, billerResult);
            String req = request[0];
            log.info("Request " + req);
            String msgResponse = null;
            Xresponse xres = null;

            if (initDB.get("IDN.async.code") != null) {
                asyncStatus = initDB.get("IDN.async.code");
            }

            try {
                Map<String, String> map = getHeader();
                String newUrl = "";
                int connectTimeout = Optional.ofNullable(Integer.valueOf(initDB.get("IDN.connectTimeout") != null ? initDB.get("IDN.connectTimeout") : "0")).orElse(-1);
                int readTimeout = Optional.ofNullable(Integer.valueOf(initDB.get("IDN.readTimeout") != null ? initDB.get("IDN.readTimeout") : "0")).orElse(-1);
                if (vo.getMethod().equalsIgnoreCase("Payment")) {
                    newUrl = generateURL(request[1], null, null);
                    xres = sendRequestV2(newUrl, map, req, connectTimeout, readTimeout);

                    if (asyncStatus.contains(String.valueOf(xres.getStatusCode()))) {
//                        msgResponse = initDB.get("IDN.async.body");
                        xres = getPaymentStatusV2(vo);
                        if (asyncStatus.contains(String.valueOf(xres.getStatusCode()))) {
                            log.info("Status Code 503 after Cek Payment Status Processed!");
                            String faultBody = "";
                            if (initDB.get("IDN.fault.body") != null) {
                                faultBody = initDB.get("IDN.fault.body");
                            }
                            String replace1 = faultBody.replace("#status", "Backend errors");
                            String replace2 = replace1.replace("#code", "1299");

                            msgResponse = replace2;
                        } else {
                            msgResponse = xres.getResponseBody();
                        }
                    } else {
                        msgResponse = xres.getResponseBody();
                    }
                } else {
                    String schoolCode = "";
                    List<ParamsVO> params = vo.getParams();
                    for (ParamsVO param : params) {
                        if (param.getName().equalsIgnoreCase("school_code")) {
                            schoolCode = param.getValue();
                        }
                    }
                    newUrl = generateURL(request[1] + "?", vo.getAccount(), schoolCode);
                    xres = sendRequestV2(newUrl, map, connectTimeout, readTimeout);

                    if (xres.getStatusCode() == 1999) {
                        log.info("Status Code 1999 Processed!");
                        String faultBody = "";
                        if (initDB.get("IDN.fault.body") != null) {
                            faultBody = initDB.get("IDN.fault.body");
                        }
                        String replace1 = faultBody.replace("#status", "Backend errors");
                        String replace2 = replace1.replace("#code", "1299");

                        msgResponse = replace2;
                    } else {
                        msgResponse = xres.getResponseBody();
                    }
                }
            } catch (Exception e) {
                log.error("Exception Body Exception Processed!");
                xres = new Xresponse();
                xres.setStatusCode(1999);
                if (vo.getMethod().equalsIgnoreCase("Payment")) {
                    if (asyncStatus.contains(String.valueOf(xres.getStatusCode()))) {
                        Xresponse xres2 = getPaymentStatusV2(vo);
                        msgResponse = xres2.getResponseBody();
                    }
                } else {
                    String faultBody = "";
                    if (initDB.get("IDN.fault.body") != null) {
                        faultBody = initDB.get("IDN.fault.body");
                    }
                    String replace1 = faultBody.replace("#status", "Backend errors");
                    String replace2 = replace1.replace("#code", "1299");
                    msgResponse = replace2;
                }

                log.error("Exception Body : " + msgResponse);
            }

            obj = getResponse(vo, billerResult, msgResponse);

            log.info("Object : " + obj);

            Object[] result;
//            if (asyncStatus.contains(String.valueOf(xres.getStatusCode()))) {
//
//                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltResponse);
//
////                savePendingTransaction(vo, billerResult, request[1], msgResponse, HandlerConstant.IDN);
//                obj = cleanUpResponse(result[0], billerResult);
//
//                saveTransaction(vo, billerResult, PENDING);
//
//                checkPendingStatus(vo, billerResult);
//            } else {
            result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltCallback);

            obj = cleanUpResponse(result[0],vo, billerResult);

            saveTransaction(vo, billerResult, transStatus);

            updateBmLogResponse(vo,new ObjectMapper().writeValueAsString(obj),msgResponse);
//            }
            log.info("Transform Result : " + result[0]);
            log.info("Final Object :" + obj);
        } catch (Exception e) {
            saveTransaction(vo, billerResult, ERROR);
            String errorMsg = "Unexpected Response - "+e.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
        }
        return obj;
    }

    private void checkPendingStatus(RequestVO vo, BillerResult billerResult) {
        log.info("IDN Start check Pending Status");
//        InitDB initDB = InitDB.getInstance();
        ObjectMapper mapper = new ObjectMapper();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                log.info("Run in background");
                try {
                    long wkt = 500;
                    if (initDB.get("IDN.async.sleep") != null) {
                        wkt = Long.valueOf(initDB.get("IDN.async.sleep"));
                    }
                    TimeUnit.MILLISECONDS.sleep(wkt);
                    //add code here
                    Object obj = getPaymentStatus(vo, billerResult);

                    String jsonString = mapper.writeValueAsString(obj);
                    log.info("JSON Callback : " + jsonString);

                    PartnerResult partnerResult = partnerInfoConfig.get(vo.getPartnerId(), vo.getProductCode());

                    sendRequest(partnerResult.getPartnerUrl(), jsonString);

                } catch (Exception ex) {
                    log.error("error : " + ex.getMessage());
                }
            }
        };
        new Thread(r).start();
        log.info("IDN Finish check Pending Status");
    }

    private Xresponse getPaymentStatusV2(RequestVO vo) {
        Xresponse xres = null;
        String msgResponse;
        Map<String, String> map = getHeader();
        String host = initDB.get("IDN.host");
        String path = initDB.get("IDN.paymentstatus.path");
        String newUrl = host + path + vo.getReffId();
        int connectTimeout = Optional.ofNullable(Integer.valueOf(initDB.get("IDN.connectTimeout") != null ? initDB.get("IDN.connectTimeout") : "0")).orElse(-1);
        int readTimeout = Optional.ofNullable(Integer.valueOf(initDB.get("IDN.readTimeout") != null ? initDB.get("IDN.readTimeout") : "0")).orElse(-1);
        try {
            long wkt = 500;
            if (initDB.get("IDN.async.sleep") != null) {
                wkt = Long.valueOf(initDB.get("IDN.async.sleep"));
            }
            TimeUnit.MILLISECONDS.sleep(wkt);
            xres = sendRequestV2(newUrl, map, connectTimeout, readTimeout);
        } catch (Exception e) {
            xres = new Xresponse();
            xres.setStatusCode(1999);
            log.info("getPaymentStatusV2 xres: " + xres.toString());
            log.info("getPaymentStatusV2 Status Code 1999 Processed!");
            String faultBody = "";
            if (initDB.get("IDN.fault.body") != null) {
                faultBody = initDB.get("IDN.fault.body");
            }
            String replace1 = faultBody.replace("#status", "Backend errors");
            String replace2 = replace1.replace("#code", "1299");

            xres.setResponseBody(replace2);
        }

        return xres;
    }

    private Object getPaymentStatus(RequestVO vo, BillerResult billerResult) {
//        InitDB initDB = InitDB.getInstance();
        Object obj = null;
        String msgResponse;
        Xresponse xres = null;
        Map<String, String> map = getHeader();
        String host = initDB.get("IDN.host");
        String path = initDB.get("IDN.paymentstatus.path");
        String newUrl = host + path + vo.getReffId();
//        String asyncStatus = "503";
        int connectTimeout = Optional.ofNullable(Integer.valueOf(initDB.get("IDN.connectTimeout") != null ? initDB.get("IDN.connectTimeout") : "0")).orElse(-1);
        int readTimeout = Optional.ofNullable(Integer.valueOf(initDB.get("IDN.readTimeout") != null ? initDB.get("IDN.readTimeout") : "0")).orElse(-1);
//        if (initDB.get("IDN.async.code") != null) {
//            asyncStatus = initDB.get("IDN.async.code");
//        }
        xres = sendRequestV2(newUrl, map, connectTimeout, readTimeout);

//        if (asyncStatus.contains(String.valueOf(xres.getStatusCode()))) {
//            msgResponse = initDB.get("IDN.async.body");
//        } else
        log.info("xres : " + xres.toString());
        if (xres == null || xres.getStatusCode() == 1999) {
            log.info("Status Code 1999 Processed!");
            String faultBody = "";
            if (initDB.get("IDN.fault.body") != null) {
                faultBody = initDB.get("IDN.fault.body");
            }
            String replace1 = faultBody.replace("#status", "Backend errors");
            String replace2 = replace1.replace("#code", "1299");

            msgResponse = replace2;
        } else {
            msgResponse = xres.getResponseBody();
        }
        try {
            obj = getResponse(vo, billerResult, msgResponse);
            Object[] result;
            result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltCallback);
            obj = cleanUpResponse(result[0],vo, billerResult);

            ObjectMapper mapper = new ObjectMapper();
            String data = mapper.writeValueAsString(result[0]);
            Map<String, Object> mapres = mapper.readValue(data, new TypeReference<Map<String, Object>>() {
            });
            String billerRespPrice=null;
            String billerRespFee=null;
            String transStatus=null;
            String billerStatusCode=null;
            String bmStatusCode=null;

            if (mapres.containsKey("AggregatorPrice")) {
                billerRespPrice = mapres.get("AggregatorPrice").toString();
            }
            if (billerRespPrice == null || billerRespPrice.equals("")) {
                billerRespPrice = String.valueOf(billerResult.getBillerPrice());
            }
            //System.out.println(">>>> AggregatorPrice.2: " + billerRespPrice);

            if (mapres.containsKey("AggregatorFee")) {
                billerRespFee = mapres.get("AggregatorFee").toString();
            }
            if (billerRespFee == null || billerRespFee.equals("")) {
                billerRespFee = Integer.toString(billerResult.getAdminFee());
            }

            //int x = billerResult.getId();
            //System.out.println("--> " + x);
            int billerId = billerResult.getBillerId();
            //System.out.println("--> " + billerId);
            if (mapres.containsKey("Status")) {
                billerStatusCode = mapres.get("Status").toString();
                BMCode bmResultCode = statusMapping.get(billerId, billerStatusCode);
                if (bmResultCode != null) {
                    bmStatusCode = bmResultCode.getCode();

                    if (bmStatusCode.equals("0")) {
                        transStatus = OK;
                    } else if (bmStatusCode.equals("99")) {
                        transStatus = PENDING;
                    } else {
                        transStatus = FAILED;
                    }
                } else {
                    if (billerStatusCode.equals("0")) {
                        transStatus = OK;
                    } else if (billerStatusCode.equals("99")) {
                        transStatus = PENDING;
                    } else {
                        transStatus = FAILED;
                    }
                }
            }
            log.info("bmTid : " + vo.getTransactionId());
            updateTransaction(vo.getTransactionId(),billerRespPrice,billerRespFee,billerStatusCode,bmStatusCode,transStatus);
        } catch (Exception e) {
            log.info("getPaymentStatus error " + e.getMessage());
        }

        return obj;
    }

    private String getAuth() {
//        InitDB initDB = InitDB.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String username = initDB.get("IDN.username");
        String password = initDB.get("IDN.password");
        String yyyymmdd = sdf.format(new Date());
        password = shaUtil.getSHA256(shaUtil.getSHA256(password) + yyyymmdd);
        log.info("yyyymmdd : " + yyyymmdd);
        log.info("SHA256 Password :" + password);
        return base64Util.encode(username + ":" + password);
    }

    private Map<String, String> getHeader() {
        Map<String, String> map = new HashMap<String, String>();
        try {
            map.put("Authorization", "Basic " + getAuth());
            return map;
        } catch (Exception e) {
            throw new NostraException(e.getMessage(), StatusCode.ERROR);
        }
    }

    private String generateURL(String path, String billKey, String billerCode) {
        String newURL = "";
//        InitDB initDB = InitDB.getInstance();
        try {
            String host = "";
            if (!path.contains("http")) {
                host = initDB.get("IDN.host");
            }
            if (path.contains("?")) {
                newURL = host + path + "bill_key=" + billKey + "&biller_code=" + billerCode;
            } else {
                newURL = host + path;
            }
            return newURL;
        } catch (Exception e) {
            throw new NostraException(e.getMessage(), StatusCode.ERROR);
        }
    }
}
