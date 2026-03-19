package com.billermanagement.services.InstamoneyService;

import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.EbayVO.GetRecepientName.EbayGetRecepientNameReqVO;
import com.billermanagement.vo.EbayVO.GetRecepientName.EbayGetRecepientNameResVO;
import com.billermanagement.vo.InstamoneyVO.CreateCustomer.IMCreateCustomerReqVO;
import com.billermanagement.vo.InstamoneyVO.CreateCustomer.IMCreateCustomerResVO;
import com.billermanagement.vo.InstamoneyVO.Transfer.IMTransferReqVO;
import com.billermanagement.vo.InstamoneyVO.Transfer.IMTransferResVO;
import com.billermanagement.vo.InstamoneyVO.ValidateBankName.IMValidateBankNameReqVO;
import com.billermanagement.vo.InstamoneyVO.ValidateBankName.IMValidateBankNameResVO;
import com.billermanagement.vo.InstamoneyVO.ValidateEwallet.IMValidateEwalletReqVO;
import com.billermanagement.vo.InstamoneyVO.ValidateEwallet.IMValidateEwalletResVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class InstamoneyService {

    @Autowired
    InstamoneyHTTPService instamoneyHTTPService;

    private Gson gson;

    public IMValidateBankNameResVO validateBankName(IMValidateBankNameReqVO vo){
        InitDB initDB = InitDB.getInstance();
        String url = initDB.get("instamoney.url.ValidateBankName");

        HashMap<String, String> params = new HashMap<>();
        params.put("bank_account_number",vo.getBankAccNumber());
        params.put("bank_code",vo.getBankCode());
        params.put("given_name",vo.getGivenName());

        String input = "";

        try {
            input=getDataString(params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new NostraException(e.getMessage(), StatusCode.ERROR);
        }

        String responseBody = instamoneyHTTPService.callHttpPost(url, input);
        log.info("Result "+responseBody);
        IMValidateBankNameResVO result =gson.fromJson(responseBody, IMValidateBankNameResVO.class);

        return result;
    }

    public IMValidateEwalletResVO validateEwallet(IMValidateEwalletReqVO vo){
        InitDB initDB = InitDB.getInstance();
        String url = initDB.get("instamoney.url.ValidateEwallet");

        HashMap<String, String> params = new HashMap<>();
        params.put("ewallet_account_number",vo.getAccNumber());
        params.put("ewallet_type",vo.getAccType());

        String input = "";

        try {
            input=getDataString(params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new NostraException(e.getMessage(), StatusCode.ERROR);
        }

        String responseBody = instamoneyHTTPService.callHttpPost(url, input);
        IMValidateEwalletResVO result =gson.fromJson(responseBody, IMValidateEwalletResVO.class);

        return result;
    }

//    public IMCreateCustomerResVO createCustomer(IMCreateCustomerReqVO vo){
//        InitDB initDB = InitDB.getInstance();
//        String url = initDB.get("instamoney.url.CreateCustomer");
//
//        if(vo.getCustomerType().equals("INDIVIDUAL")){
//            if ((vo.getGivenName()==null || vo.getGivenName().equals(""))||(vo.getIdentification().getPassportCountry()==null||vo.getIdentification().getPassportCountry().equals(""))){
//                throw new NostraException("Given Name and Passport Country are mandatory if Customer Type is INDIVIDUAL", StatusCode.ERROR);
//            }
//        } else if(vo.getCustomerType().equals("BUSINESS")){
//            if ((vo.getBusinessName()==null || vo.getBusinessName().equals(""))||(vo.getIdentification().getBusinessCountry()==null||vo.getIdentification().getBusinessCountry().equals(""))){
//                throw new NostraException("Business Name and Business Tax ID are mandatory if Customer Type is BUSINESS", StatusCode.ERROR);
//            }
//        } else {
//            throw new NostraException("Invalid Customer Type", StatusCode.ERROR);
//        }
//
//        String responseBody = instamoneyHTTPService.callHttpPost(url, vo);
//        log.info("Result "+responseBody);
//        IMCreateCustomerResVO result =gson.fromJson(responseBody, IMCreateCustomerResVO.class);
//
//        return result;
//    }

    public IMTransferResVO createCustomer(IMTransferReqVO vo){
        InitDB initDB = InitDB.getInstance();
        String url = initDB.get("instamoney.url.Transfer");

        String responseBody = instamoneyHTTPService.callHttpPost(url, vo);
        log.info("Result "+responseBody);
        IMTransferResVO result =gson.fromJson(responseBody, IMTransferResVO.class);

        return result;
    }

    private String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    protected String sendRequest(String url, Map<String,String> map, String httpRequest) throws IOException {
        return instamoneyHTTPService.postUser(url, map, httpRequest);
    }

    public String ilumaGetBankName(IMValidateBankNameReqVO vo){
        String result="";
        ObjectMapper mapper = new ObjectMapper();
        Object o = mapper.convertValue(vo, Object.class);

        //** gak jdi, request tetep vo
        String request = "";
        try {
            request = mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
        log.info("Request : "+request);
        //o = constructMessage(request, vo, billerResult, 0);
        //**
        InitDB initDB = InitDB.getInstance();
        String url = initDB.get("iluma.url.getBankName");
        String username=initDB.get("iluma.username");
        log.info("Username "+username);
        String password=initDB.get("iluma.password");

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")) );
        String authHeader = "Basic " + new String( encodedAuth );

        Map <String, String> map = new HashMap<String, String>();
        map.put("Authorization", authHeader);

        log.info("Request sent to "+url+" with header "+authHeader);

        try {
            result = instamoneyHTTPService.postUser(url, map, request);
        } catch (IOException e) {
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }

        return result;
    }

    public String imCreateCustomer(IMCreateCustomerReqVO vo){
        String result="";
        ObjectMapper mapper = new ObjectMapper();
        Object o = mapper.convertValue(vo, Object.class);

        //** gak jdi, request tetep vo
        String request = "";
        try {
            request = mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
        log.info("Request : "+request);
        //o = constructMessage(request, vo, billerResult, 0);
        //**
        InitDB initDB = InitDB.getInstance();
        String url = initDB.get("instamoney.url.createCustomer");
        String username=initDB.get("instamoney.username");
        log.info("Username "+username);
        String password=initDB.get("instamoney.password");

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")) );
        String authHeader = "Basic " + new String( encodedAuth );

        Map <String, String> map = new HashMap<String, String>();
        map.put("Authorization", authHeader);

        log.info("Request sent to "+url+" with header "+authHeader);

        try {
            result = instamoneyHTTPService.postUser(url, map, request);
        } catch (IOException e) {
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }

        return result;
    }

    public String imTransfer(IMTransferReqVO vo){
        String result="";
        ObjectMapper mapper = new ObjectMapper();
        Object o = mapper.convertValue(vo, Object.class);

        //** gak jdi, request tetep vo
        String request = "";
        try {
            request = mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }
        log.info("Request : "+request);
        //o = constructMessage(request, vo, billerResult, 0);
        //**
        InitDB initDB = InitDB.getInstance();
        String url = initDB.get("instamoney.url.transfer");
        String username=initDB.get("instamoney.username");
        log.info("Username "+username);
        String password=initDB.get("instamoney.password");

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")) );
        String authHeader = "Basic " + new String( encodedAuth );

        Map <String, String> map = new HashMap<String, String>();
        map.put("Authorization", authHeader);

        log.info("Request sent to "+url+" with header "+authHeader);

        try {
            result = instamoneyHTTPService.postUser(url, map, request);
        } catch (IOException e) {
            e.printStackTrace();
            throw new NostraException(e.getMessage(),StatusCode.ERROR);
        }

        return result;
    }

}
