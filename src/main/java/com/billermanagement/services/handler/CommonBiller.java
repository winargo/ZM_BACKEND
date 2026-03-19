package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.persistance.repository.TransTmpRepository;
import com.billermanagement.services.handler.common.RequestHeader;
import com.billermanagement.util.FormatUtil;
import com.billermanagement.util.GlobalHashmap;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.backend.ParamsVO;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Qualifier("otherBiller")
public class CommonBiller extends BillerRequest {
    @Autowired
    private TransTmpRepository transTmpRepository;

    @Autowired
    private GlobalHashmap globalHashMap;

    @Autowired
    private BillerConfig billerConfig;

    private final InitDB initDB = InitDB.getInstance();

    @Autowired
    SLATime slaTimeService;

    @Autowired
    private RequestHeader requestHeader;

    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        try {
            String billerAlias = billerConfig.get(billerResult.getBillerId()).toUpperCase();
            logger.info("Entering Handler : " + billerAlias);
            int trxidLength = 0;
            int randomStringLength = 0;
            String customTrxIdPrefix = null;
            String inqTrxStatusCode = "";
            if (initDB.get(billerAlias + ".TrxIdLength") != null) {
                trxidLength = Integer.valueOf(initDB.get(billerAlias + ".TrxIdLength"));
                logger.info("TrxIdLength : " + trxidLength);
            }

            if (initDB.get(billerAlias + ".TrxIdRandomLength") != null) {
                randomStringLength = Integer.valueOf(initDB.get(billerAlias + ".TrxIdRandomLength"));
                logger.info("RandomStringLength : " + randomStringLength);
            }

            if (initDB.get(billerAlias + ".TrxIdCustomPrefix") != null) {
                String trxIdCustomPrefixType = initDB.get(billerAlias + ".TrxIdCustomPrefix");
                int beginIndex = 0;
                if (trxIdCustomPrefixType.contains("static")) {
                    //values = static;TLR
                    beginIndex = trxIdCustomPrefixType.indexOf(";");
                    customTrxIdPrefix = trxIdCustomPrefixType.substring(beginIndex + 1);
                } else if (trxIdCustomPrefixType.contains("timestamp")) {
                    //values = timestamp;yyyyMMddHHmmss
                    beginIndex = trxIdCustomPrefixType.indexOf(";");
                    String format = trxIdCustomPrefixType.substring(beginIndex + 1);
                    customTrxIdPrefix = FormatUtil.getTime(format);
                }
                logger.info("TrxIdCustomPrefix : " + customTrxIdPrefix);
            }

            List<ParamsVO> paramsVOList = new ArrayList<>();

            if (vo.getParams() != null) {
                paramsVOList = vo.getParams();
            }

            paramsVOList.add(createParamsVO("billerAlias", billerAlias));

            if (initDB.get(billerAlias + ".Static.Additional.Params") != null) {
                // put additional params here
                //Additional Params
                logger.info("Additional Params : true");

                String additionalParams = initDB.get(billerAlias + ".Static.Additional.Params");
                if (additionalParams != null) {
                    //example value:
                    //Key1=Value1;Key2=Value2;Key3=Value3
                    String[] keysParams = additionalParams.split(";");

                    for (String k : keysParams) {
                        int firstIndex = 0;
                        int delimiterIndex = k.indexOf("=");
                        String keyName = k.substring(firstIndex, delimiterIndex);
                        String keyValue = k.substring(delimiterIndex + 1);
                        paramsVOList.add(createParamsVO(keyName, keyValue));
                    }
                }

            }

            //Timestamp
            String timeStampParams = initDB.get(billerAlias + ".Timestamp.Additional.Params");
            if (timeStampParams != null) {
                //example value: [KeyName];[dateFormat]
                //BRI-Timestamp;yyyyMMddHHmmss
                int indexOf = timeStampParams.indexOf(";");
                String keyName = timeStampParams.substring(0, indexOf);
                String dateFormat = timeStampParams.substring(indexOf + 1);
                String keyValue = FormatUtil.getTime(dateFormat);
                paramsVOList.add(createParamsVO(keyName, keyValue));
            }

            vo.setParams(paramsVOList);
            logger.info("Params :" + vo.getParams().toString());

            String bankCode = initDB.get("Bank.Code." + billerAlias + "." + vo.getAccountCode());
            String partnerAccountCode = vo.getAccountCode();
            logger.info("partnerAccountCode : " + partnerAccountCode);
            if (bankCode != null) {
                vo.setAccountCode(bankCode);
            }
            logger.info("Aggregator Bank Code : " + vo.getAccountCode());

            String requestType = getRequestType(billerResult.getTransformId(), vo.getMethod());
            logger.info("requestType:" + requestType);
            setTransformType(requestType);
            String[] request = getRequest(vo, billerResult, trxidLength != 0 ? trxidLength : null, billerAlias, randomStringLength != 0 ? randomStringLength : null, customTrxIdPrefix);
            logger.info("request: " + request[0]);

            String message = request[0];
            if (requestType.equals("JSON_TO_XML")) {
                message = message.replaceAll("\"", "");
                logger.info("Request XML : " + message);
            }

            String modifyReqPayload = initDB.get(billerAlias + ".ModifyRequest.Payload");
            if (modifyReqPayload != null && modifyReqPayload.toLowerCase().equals("true")) {
                // please put here with specific condition
                // if jolt result is required to modify before send to BE,
                // it's like add soap envelope etc
                //message =

                logger.info("Modify Request : " + message);
            }

            Map<String, String> mapHeader = requestHeader.getHeader(vo, billerResult, billerAlias, request[1], message);
            String reqBE = message;
            String msgResponse = sendRequest(request[1], mapHeader, message, getTimeout(billerAlias + ".connect.timeout"), getTimeout(billerAlias + ".read.timeout"));
//            if (msgResponse == null){
//                throw new Exception("Unexpected Response - Response body is null");
//            }
            String resBE = msgResponse;
            logger.info("responseBE " + billerAlias + " : " + resBE);

            String modifyResPayload = initDB.get(billerAlias + ".ModifyResponse.Payload");
            if (modifyResPayload != null && modifyResPayload.toLowerCase().equals("true")) {
                // please put here with specific condition
                // if BE response is required to modify before send to jolt transform,
                // it's like replace xml namespace etc
                //msgResponse =


                logger.info("Modify Response : " + msgResponse);
            }

            vo.setAccountCode(partnerAccountCode);
            Object obj = getResponse(vo, billerResult, msgResponse);
            logger.info("object to transform: " + obj);

            Object[] result;
//            String status;
            boolean isPending = false;
            String[] asyncStatus = null;

            if (initDB.get(billerAlias+".async.code") != null) {
                asyncStatus = initDB.get(billerAlias+".async.code").split(";");
            }

            String containsPendingParam = initDB.get(billerAlias+".Pending.ParamName");
            //value : firstParam|lastParam
            //json : responseCode|responseDescription
            //xml : <responseCode>|</responseCode>
            if (containsPendingParam != null){
                int indexOf = containsPendingParam.indexOf("|");
                String firstParam = containsPendingParam.substring(0,indexOf);
                String lastParam = containsPendingParam.substring(indexOf+1);
                String responseCodeBE=null;
                if (msgResponse.contains(firstParam)){
                    int firstIndex = msgResponse.lastIndexOf(firstParam);
                    int lastIndex = msgResponse.indexOf(lastParam);
                    responseCodeBE = msgResponse.substring(firstIndex, lastIndex).replaceAll("[\":,]", "").replace(firstParam, "");
                    logger.info("ResponseCode "+billerAlias+" : " + responseCodeBE);
                }

                for (String async : asyncStatus) {
                    logger.info(billerAlias+" Async Code :" + async);
                    if (responseCodeBE.toUpperCase().contains(async)) {
                        isPending = true;
                    }
                }
            }
            logger.info("isPending :" + isPending);

            if (isPending) {
                isPending = false;
                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltResponse);
                obj = cleanUpResponse(result[0],vo, billerResult);
                int pendingStatusFlag = billerResult.getBillerId();
                savePendingTransaction(vo, billerResult, request[1], msgResponse, pendingStatusFlag);
                saveTransaction(vo, billerResult, PENDING);
            } else {
                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltCallback);
                obj = cleanUpResponse(result[0],vo, billerResult);
                saveTransaction(vo, billerResult, transStatus);
            }
            updateBmLogResponse(vo,new ObjectMapper().writeValueAsString(obj),resBE);

            return obj;
        }catch (Exception e){
            saveTransaction(vo, billerResult, ERROR);
            String errorMsg = "Unexpected Response - "+e.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
            logger.info("Common Biller Exception :"+ errorMsg);
            throw e;
        }
    }

    private String getRequestType(String transformId, String method) {
        String key = new StringBuilder().append(transformId).append('.').append(method).toString();
        Object[] obj = globalHashMap.getHashMap(key);

        return obj[1].toString();
    }
}
