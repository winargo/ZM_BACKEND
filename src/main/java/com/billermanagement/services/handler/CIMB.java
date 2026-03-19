package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.util.FormatUtil;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.backend.ParamsVO;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Qualifier("cimb")
public class CIMB extends BillerRequest {
    private final InitDB initDB = InitDB.getInstance();
    private String sourceAccount = "";
//    private boolean isPending = false;
//    private String[] asyncStatus = null;

    @Override
    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        logger.info("Entering CIMB Handler");

        try {
            String acctCode = vo.getAccountCode();

            if(initDB.get("Bank.Code.CIMB." + vo.getAccountCode()) != null){
                String bankCode = initDB.get("Bank.Code.CIMB." + acctCode);
                vo.setAccountCode(bankCode);
            }

            List<ParamsVO> paramsVOList= new ArrayList<>();

            if (vo.getParams() != null) {
                paramsVOList = vo.getParams();
            }

            if (initDB.get("CIMB.Custodian") != null){
                sourceAccount = initDB.get("CIMB.Custodian");
                paramsVOList.add(createParamsVO("sourceAccount",sourceAccount));
            }

//            if (vo.getMethod().equalsIgnoreCase("Payment")) {
//                vo.setAmount(vo.getAmount());

                SimpleDateFormat trxDate = new SimpleDateFormat("yyyyMMdd");
                trxDate.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                paramsVOList.add(createParamsVO("txnDate",trxDate.format(new Date())));
//            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            paramsVOList.add(createParamsVO("txnRequestDateTime",sdf.format(new Date())));

            if (initDB.get("CIMB.CORPID") != null) {
                paramsVOList.add(createParamsVO("corpID", initDB.get("CIMB.CORPID")));
            }

            String serviceCode = "";

            if (vo.getMethod().equalsIgnoreCase("Payment") && initDB.get("CIMB.AccountCode.Inhouse").contains(acctCode)) {
                serviceCode =  initDB.get("CIMB.ServiceCode.InHouse");

                paramsVOList.add(createParamsVO("benBankName", initDB.get("Bank.Name.CIMB."+acctCode)));
                paramsVOList.add(createParamsVO("benBankAddr1", initDB.get("CIMB.Param.Inhouse.benBankAddr1")));
                paramsVOList.add(createParamsVO("benBankAddr2", initDB.get("CIMB.Param.Inhouse.benBankAddr2")));
                paramsVOList.add(createParamsVO("benBankAddr3", initDB.get("CIMB.Param.Inhouse.benBankAddr3")));
                paramsVOList.add(createParamsVO("benBankBranch", initDB.get("CIMB.Param.Inhouse.benBankBranch")));
                paramsVOList.add(createParamsVO("benBankSWIFT", initDB.get("CIMB.Param.Inhouse.benBankSWIFT")));
                paramsVOList.add(createParamsVO("memo", initDB.get("CIMB.Param.Inhouse.Memo")));

            } else if (vo.getMethod().equalsIgnoreCase("Inquiry") && initDB.get("CIMB.AccountCode.Inhouse").contains(acctCode)) {
                serviceCode = initDB.get("CIMB.ServiceCode.Inquiry");
            } else {
                serviceCode = initDB.get("CIMB.ServiceCode.Otherbank");

                if (vo.getMethod().equalsIgnoreCase("Inquiry")){
                    vo.setAmount("10000");
                    //                    set default beneficiary name
                    paramsVOList.add(createParamsVO("RecepientName","xxx xxx"));
                }

                // instructDate
                SimpleDateFormat instruct = new SimpleDateFormat("yyyyMMdd");
                instruct.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                paramsVOList.add(createParamsVO("instructDate",instruct.format(new Date())));

                SimpleDateFormat remitID = new SimpleDateFormat("yyyyMMddHHmmss");
                remitID.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                paramsVOList.add(createParamsVO("remitID",remitID.format(new Date())));

                paramsVOList.add(createParamsVO("benAddr1", initDB.get("CIMB.Param.Other.benAddr1")));
                paramsVOList.add(createParamsVO("benAddr2", initDB.get("CIMB.Param.Other.benAddr2")));
                paramsVOList.add(createParamsVO("benBankRTGS", initDB.get("CIMB.Param.Other.benBankRTGS")));
                paramsVOList.add(createParamsVO("benBankName", initDB.get("Bank.Name.CIMB."+acctCode)));
                paramsVOList.add(createParamsVO("benBankAddr", initDB.get("CIMB.Param.Other.benBankAddr")));
                paramsVOList.add(createParamsVO("benBankCity", initDB.get("CIMB.Param.Other.benBankCity")));
                paramsVOList.add(createParamsVO("benBankCountry", initDB.get("CIMB.Param.Other.benBankCountry")));
                paramsVOList.add(createParamsVO("memo", initDB.get("CIMB.Param.Other.memo")));
                paramsVOList.add(createParamsVO("remitName", initDB.get("CIMB.Param.Other.remitName")));
                paramsVOList.add(createParamsVO("debitBankCharge", initDB.get("CIMB.Param.Other.debitBankCharge")));
                paramsVOList.add(createParamsVO("debitAgentCharge", initDB.get("CIMB.Param.Other.debitAgentCharge")));
            }

            if (serviceCode != "") {
                paramsVOList.add(createParamsVO("serviceCode", serviceCode));
            }

            vo.setParams(paramsVOList);

            setTransformType("JSON_TO_XML");

            String[] request = getRequest(vo, billerResult, Integer.parseInt(initDB.get("CIMB.TransIdLength")), "CIMB", null, FormatUtil.getTime("yyyyMMddHHmmss"));

            String message = request[0].replaceAll("\"", "");

            String sliceEncode = message.replace("<?xml version=1.0 encoding=UTF-8?>", "");
            String sliceTxnDataFirst = sliceEncode.replace("<java:txnData>", "<java:txnData><![CDATA[");
            String sliceTxnDataLast = sliceTxnDataFirst.replace("</java:txnData>", "]]></java:txnData>");
            String soapTemplate = initDB.get("CIMB.Soap.Template");

            String appendTemplate = soapTemplate.replace("#body", sliceTxnDataLast);

            logger.info("message: " + appendTemplate);
            Map<String, String> map = new HashMap<>();
            map.put("Content-Type", "text/xml;charset=UTF-8");
            map.put("SOAPAction", "https://directchannel.cimbniaga.co.id/HostCustomer/HostCustomerRequest");

            String msgResponse = sendRequest(request[1], map, appendTemplate, getTimeout("CIMB.Connect.Timeout"), getTimeout("CIMB.Read.Timeout"));
            logger.info("CIMB response: " + msgResponse);
            String resBe = msgResponse;

            vo.setAccountCode(acctCode);
            msgResponse = msgResponse.replace("<![CDATA[", "");
            msgResponse = msgResponse.replace("]]>", "");
            msgResponse = msgResponse.replaceAll("&lt;", "<");
            msgResponse = msgResponse.replaceAll("&gt;", ">");
            msgResponse = msgResponse.replaceAll("xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"", "");
            msgResponse = msgResponse.replaceAll("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"", "");
            msgResponse = msgResponse.replaceAll("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "");
            msgResponse = msgResponse.replaceAll("soapenv:", "");
            msgResponse = msgResponse.replaceAll("xmlns=\"https://directchannel.cimbniaga.co.id\"", "");
            msgResponse = msgResponse.replaceAll("xsi:type=\"ns2:Output\"", "");
            msgResponse = msgResponse.replaceAll("xmlns:ns1=\"http://10.25.136.152\"", "");
            msgResponse = msgResponse.replaceAll("xmlns:ns2=\"java:prismagateway.service.HostCustomer\"", "");
            msgResponse = msgResponse.replaceAll("ns1:", "");
            msgResponse = msgResponse.replaceAll("ns2:", "");
            msgResponse = msgResponse.replaceAll("xsi:nil=\"true\"", "");

            if(vo.getMethod().equalsIgnoreCase("Inquiry") && !initDB.get("CIMB.AccountCode.Inhouse").contains(acctCode)){
                if (msgResponse.contains("<statusMsg>")){
                    int firstIndex = msgResponse.lastIndexOf("<statusMsg>");
                    int lastIndex = msgResponse.indexOf("</statusMsg>");
                    String fullStatusMsg = msgResponse.substring(firstIndex,lastIndex).replace("<statusMsg>","");
                    int benNameIndex = 0;
                    String benName = fullStatusMsg.substring(benNameIndex).trim();
                    if (fullStatusMsg.contains("Beneficary name is invalid -")){
                        benNameIndex= fullStatusMsg.lastIndexOf("-");
                        benName = fullStatusMsg.substring(benNameIndex+1).trim();
                    }
                    String realStatusMsg = "<statusMsg>"+fullStatusMsg+"</statusMsg>";
                    String newStatusMsg = "<statusMsg>"+benName+"</statusMsg>";
                    msgResponse = msgResponse.replace(realStatusMsg,newStatusMsg);
                }
                if (msgResponse.contains("<statusCode>")){
                    int firstIndex = msgResponse.lastIndexOf("<statusCode>");
                    int lastIndex = msgResponse.indexOf("</statusCode>");
                    String statusCode = msgResponse.substring(firstIndex,lastIndex).replace("<statusCode>","").trim();
                    String realStatusCode = "<statusCode>"+statusCode+"</statusCode>";
                    String newStatusCode = "<statusCode>"+"99"+statusCode+"</statusCode>";
                    msgResponse = msgResponse.replace(realStatusCode,newStatusCode);
                }
            }

            logger.info("Message Response >>> " + msgResponse);

            Object obj = getResponse(vo, billerResult, msgResponse);
            logger.info("CIMB object to transform: " + obj);

            Object[] result;

            result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltCallback);
            obj = cleanUpResponse(result[0],vo, billerResult);

            saveTransaction(vo, billerResult, transStatus);

            updateBmLogResponse(vo,new ObjectMapper().writeValueAsString(obj),resBe);

            return obj;
        } catch (Exception e) {
            saveTransaction(vo, billerResult, ERROR);
            String errorMsg = "Unexpected Response - "+e.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
            logger.info("CIMB Exception :"+ errorMsg);
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

    protected int getTimeout(String param) {
        String timeout = InitDB.getInstance().get(param);
        if (timeout == null) {
            if (param.contains("Connect")) {
                timeout = InitDB.getInstance().get("CIMB.Connect.Timeout");
            } else if (param.contains("Read")) {
                timeout = InitDB.getInstance().get("CIMB.Read.Timeout");
            }
        }

        return (timeout == null) ? -1 : Integer.parseInt(timeout);
    }
}
