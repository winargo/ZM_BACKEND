package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.BmLog;
import com.billermanagement.persistance.domain.TransHistory;
import com.billermanagement.persistance.domain.TransTmp;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.persistance.domain.resultset.PartnerResult;
import com.billermanagement.persistance.repository.TransHistoryRepository;
import com.billermanagement.persistance.repository.TransTmpRepository;
import com.billermanagement.services.BmLogService;
import com.billermanagement.services.HttpProcessingService;
import com.billermanagement.services.TransformService;
import com.billermanagement.util.FormatUtil;
import com.billermanagement.util.InitDB;
import com.billermanagement.util.SHAUtils;
import com.billermanagement.vo.AdditionalInfoVO;
import com.billermanagement.vo.IDN.Xresponse;
import com.billermanagement.vo.backend.ParamsVO;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.text.RandomStringGenerator;

public abstract class BillerRequest {
    //@Autowired
    //private PartnerApiRepository partnerApiRepository;
    @Autowired
    SHAUtils shaUtils;

    @Autowired
    private TransTmpRepository transTmpRepository;

    @Autowired
    private TransHistoryRepository transHistoryRepository;

    @Autowired
    private StatusMapping statusMapping;

    @Autowired
    private HttpProcessingService httpProcessingService;

    @Autowired
    protected TransformService transformService;

    @Autowired
    PartnerInfoConfig partnerInfoConfig;

    @Autowired
    BmLogService bmLogService;

    Logger logger = LoggerFactory.getLogger(BillerRequest.class);

    String transactionId;
    String transactionIdResp;
    //private String partnerTransId;
    private String billerTransId;
    //private String bmTransId;
    private String partnerProductCode;
    private String billerRespPrice;
    private String billerRespFee;
    private String bmStatusCode;
    private String billerStatusCode;
    private String transformType = "JSON_TO_JSON";
    private PartnerResult partnerResult;

    protected String transStatus;
    protected static final String OK = "SUCCESS";
    protected static final String FAILED = "FAILED";
    protected static final String PENDING = "PENDING";
    protected static final String ERROR = "ERROR";

    public abstract Object process(RequestVO vo, BillerResult billerResult) throws Exception;

    private String getTransIdPrefix(RequestVO vo, Integer randomStringLength, String setCustomTrxIdPrefix) {
        String prefix;
        String randomLetters;
        //String prefix = (vo.getAccount() != null) ? getTransIdPrefix(vo.getAccount()) : vo.getRequestId();
        if (vo.getAccount() != null) {
            prefix = vo.getAccount();
            if (prefix.startsWith("0")) {
                prefix = prefix.substring(1);
            } else if (prefix.startsWith("62")) {
                prefix = prefix.substring(2);
            }
        } else {
            prefix = vo.getRequestId();
        }

        if (randomStringLength != null) {
            RandomStringGenerator generator = new RandomStringGenerator.Builder()
                    .withinRange('A', 'Z').build();
            randomLetters = generator.generate(randomStringLength);
            prefix = randomLetters;
        }

        if (setCustomTrxIdPrefix != null) {
            prefix = setCustomTrxIdPrefix;
        }

        return prefix;
    }

    private String setTransactionId(RequestVO vo, Integer randomStringLength, String setCustomTrxIdPrefix) {
        String prefix = getTransIdPrefix(vo, randomStringLength, setCustomTrxIdPrefix);

        return new StringBuilder(prefix).append(FormatUtil.getTime("yyyyMMddHHmmssSSS")).toString();
    }

    private String setTransactionId(RequestVO vo, int i, Integer randomStringLength, String setCustomTrxIdPrefix) {
        String prefix = getTransIdPrefix(vo, randomStringLength, setCustomTrxIdPrefix);

        int len = prefix.length();
        StringBuilder ret = new StringBuilder(prefix);
        if (randomStringLength != null & len >= 3) {
            int lenDate = 17 - (i - len);
            ret.append(FormatUtil.getTime("yyyyMMddHHmmssSSS").substring(lenDate));
        } else if (len >= 3) {
            int lenDate = 17 - (i - len);
            ret.append(FormatUtil.getTime("yyyyMMddHHmmssSSS").substring(lenDate));
        } else {
            ret.append(FormatUtil.getTime("yyyyMMddHHmmssSSS"));
        }

        return ret.toString();
    }

    private void init() {
        transactionId = null;
        transactionIdResp = null;
        //partnerTransId = null;
        billerTransId = null;
        partnerProductCode = null;
        billerRespPrice = null;
        billerRespFee = null;
        bmStatusCode = null;
        billerStatusCode = null;
        partnerResult = null;
        transStatus = null;
    }

    protected String[] getRequest(RequestVO vo, BillerResult billerResult) throws Exception {
        return getRequest(vo, billerResult, null, null, null, null);
    }

    protected String[] getRequest(RequestVO vo, BillerResult billerResult, String billerId) throws Exception {
        return getRequest(vo, billerResult, null, billerId, null, null);
    }

    protected String[] getRequest(RequestVO vo, BillerResult billerResult, Integer transIdLength) throws Exception {
        return getRequest(vo, billerResult, transIdLength, null, null, null);
    }

    protected String[] getRequest(RequestVO vo, BillerResult billerResult, Integer transIdLength, String billerId) throws Exception {
        return getRequest(vo, billerResult, transIdLength, billerId, null, null);
    }

    protected String[] getRequest(RequestVO vo, BillerResult billerResult, Integer transIdLength, Integer randomStringLength) throws Exception {
        return getRequest(vo, billerResult, transIdLength, null, randomStringLength, null);
    }

    protected String[] getRequest(RequestVO vo, BillerResult billerResult, Integer transIdLength, String billerId, String setCustomTrxIdPrefix) throws Exception {
        return getRequest(vo, billerResult, transIdLength, billerId, null, setCustomTrxIdPrefix);
    }

    protected String[] getRequest(RequestVO vo, BillerResult billerResult, Integer transIdLength, String billerId, Integer randomStringLength, String setCustomTrxIdPrefix) throws Exception {
        init();
        //System.out.println("getRequest: " + vo.getPartnerId() + "," + vo.getProductCode());
        long start = System.currentTimeMillis();
        /*partnerResult = partnerApiRepository.findPartnerInfo(vo.getPartnerId(), vo.getProductCode());
        logger.info("1.partnerPrice: " + partnerResult.getPartnerPrice() + ", partnerFee: " + partnerResult.getPartnerFee() + "," + (System.currentTimeMillis()-start));

        start = System.currentTimeMillis();
        PartnerResult partnerResult = partnerInfoConfig.get(vo.getPartnerId(), vo.getProductCode());
        logger.info("2.partnerPrice: " + partnerResult.getPartnerPrice() + ", partnerFee: " + partnerResult.getPartnerFee() + "," + (System.currentTimeMillis()-start));*/

        partnerResult = partnerInfoConfig.get(vo.getPartnerId(), vo.getProductCode());
        logger.info("partnerPrice: " + partnerResult.getPartnerPrice() + ", partnerFee: " + partnerResult.getPartnerFee() + "," + (System.currentTimeMillis() - start) + "ms");

        transactionId = (transIdLength == null) ? setTransactionId(vo, randomStringLength, setCustomTrxIdPrefix) : setTransactionId(vo, transIdLength, randomStringLength, setCustomTrxIdPrefix);

        vo.setTransactionId(transactionId);

        //partnerTransId = vo.getRequestId();
        partnerProductCode = vo.getProductCode();

        vo.setProductCode(billerResult.getBillerCode());

        ObjectMapper mapper = new ObjectMapper();
        Object o = mapper.convertValue(vo, Object.class);

        //** gak jdi, request tetep vo
        String request = mapper.writeValueAsString(o);
        //o = constructMessage(request, vo, billerResult, 0);
        //**

        Map<String, Object> map = mapper.readValue(request, new TypeReference<Map<String, Object>>() {
        });

        AdditionalInfoVO addInfoVO = new AdditionalInfoVO();
        addInfoVO.setTransactionId(vo.getTransactionId());
        addInfoVO.setPartnerPrice(partnerResult.getPartnerPrice());
        addInfoVO.setBillerPrice(billerResult.getBillerPrice());
        addInfoVO.setAdminFee(partnerResult.getPartnerFee());
        //addInfoVO.setAdminFee(billerResult.getAdminFee());
        addInfoVO.setTime(FormatUtil.getTime("yyyyMMddHHmmssSSS"));
        if (billerId != null) {
            logger.info("Biller : " + billerId);
            String billerUid = InitDB.getInstance().get(billerId + ".uid");
            if (billerUid != null) {
                addInfoVO.setBillerUid(billerUid);
            } else {
                addInfoVO.setBillerUid("");
            }

            String billerPass = InitDB.getInstance().get(billerId + ".pin");
            if (billerPass != null) {
                addInfoVO.setBillerPass(billerPass);
            } else {
                addInfoVO.setBillerPass("");
            }

//            set sign for mobilepulsa
            if (billerId.equals("mobilepulsa")) {
//              if ReffId is not empty then map reffId to tr_id
//              else map TransactionId to tr_id
                logger.info("Start Mobile Pulsa Generate Sign Hash");
                String trId;
                String sign;
                logger.info("ReffId : " + vo.getReffId());
                logger.info("TransactionId : " + addInfoVO.getTransactionId());
                if (vo.getMethod().toUpperCase().equals("PAYMENT")) {
                    trId = vo.getReffId();
                    logger.info("trId equals ReffId");
                } else {
                    trId = addInfoVO.getTransactionId();
                    logger.info("trId equals TransactionId");
                }
                sign = addInfoVO.getBillerUid() + addInfoVO.getBillerPass() + trId;
                logger.info("BillerUID : " + addInfoVO.getBillerUid());
                logger.info("BillerPass : " + addInfoVO.getBillerPass());
                logger.info("Trid : " + trId);
                logger.info("Sign : " + sign);

                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(sign.getBytes());
                byte[] digest = md.digest();
                String myHash = DatatypeConverter.printHexBinary(digest).toLowerCase();
                logger.info("Sign Hash :" + myHash);
                addInfoVO.setSign(myHash);
            } else {
                addInfoVO.setSign("");
            }

            if (billerId.equals("CIMB")) {
                String token = "";
                String securityWord = shaUtils.getSHA256(InitDB.getInstance().get("CIMB.SecurityWord"));
                String serviceCode = "";
                String txnRequestDatetime = "";

                for (ParamsVO vo2 : vo.getParams()) {
                    if (vo2.getName().equals("serviceCode")) {
                        serviceCode = vo2.getValue();
                    }

                    if (vo2.getName().equals("txnRequestDateTime")) {
                        txnRequestDatetime = vo2.getValue();
                    }
                }

                // SHA256(corpID:SHA256(SecurityWord):txnRequestDateTime:txnRequestID:serviceCode)
                token = shaUtils.getSHA256(InitDB.getInstance().get("CIMB.CORPID") + ":" + securityWord + ":" + txnRequestDatetime + ":" + addInfoVO.getTransactionId() + ":" + serviceCode);

                if (token != "") {
                    addInfoVO.setSign(token);
                }
            } else {
                addInfoVO.setSign("");
            }
        } else {
            addInfoVO.setBillerUid("");
            addInfoVO.setBillerPass("");
            addInfoVO.setSign("");
        }

        map.put("additional_info", addInfoVO);

        o = mapper.convertValue(map, Object.class);
        System.out.println(">>> Req:" + o);
        //logger.info("BillerRequest.toTransform: " + transformService + "," + billerResult.getTransformId() + "," + vo.getMethod() + "," + o.toString());
        Object[] res = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), o, Jolt.JoltRequest);
        //logger.info("transformedOutput: "+res[0]);
        //logger.info("url: "+res[1]);
        //logger.info("flow: "+res[2]);

        String msgRequest = mapper.writeValueAsString(res[0]);
        //logger.info("BillerRequest.msgRequest:" + msgRequest);
        if (transformType.equals("JSON_TO_XML")) {
            msgRequest = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(msgRequest).toString();
        }
        //logger.info("BillerRequest.msgRequest:" + msgRequest);

        String[] result = {msgRequest, res[1].toString(), res[2].toString()};

        BmLog bmLog = new BmLog();
        bmLog.setMethod(vo.getMethod());
        bmLog.setPartnerTid(vo.getRequestId());
        bmLog.setBmTid(vo.getTransactionId());
        bmLog.setReqFe(request);
        bmLog.setReqBe(msgRequest);
        bmLogService.save(bmLog);

        return result;
    }

    private Object constructMessage(String message, RequestVO vo, BillerResult billerResult) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Object obj;
        Map<String, Object> map = mapper.readValue(message, new TypeReference<Map<String, Object>>() {
        });
        //vo.setRequestId(partnerTransId); //gak jadi
        if (transformType.equals("JSON_TO_XML")) {
            JSONObject partnerRequest = new JSONObject();
            partnerRequest.put("Method", vo.getMethod());
            partnerRequest.put("ProductCode", vo.getProductCode());
            partnerRequest.put("PartnerId", vo.getPartnerId());
            //partnerRequest.put("PassPin", vo.getPassPin());
            partnerRequest.put("RequestId", vo.getRequestId() != null ? vo.getRequestId() : 0);
            partnerRequest.put("Account", vo.getAccount());
            partnerRequest.put("Amount", vo.getAmount());
            partnerRequest.put("ReffId", vo.getReffId());
            partnerRequest.put("TransactionId", vo.getTransactionId());

            List<ParamsVO> params = vo.getParams();
            if (params != null && params.size() > 0) {
                JSONArray ja = new JSONArray();
                for (ParamsVO param : params) {
                    JSONObject jo = new JSONObject();
                    jo.put("Name", param.getName());
                    jo.put("Value", param.getValue());

                    ja.put(jo);
                }
                partnerRequest.put("Params", ja);
            }
            map.put("partner_request", partnerRequest);

            JSONObject addInfo = new JSONObject();
            addInfo.put("TransactionId", vo.getTransactionId());
            addInfo.put("PartnerPrice", partnerResult.getPartnerPrice());
            addInfo.put("BillerPrice", billerResult.getBillerPrice());
            addInfo.put("AdminFee", partnerResult.getPartnerFee());
            addInfo.put("Time", FormatUtil.getTime("yyyyMMddHHmmssSSS"));

            map.put("additional_info", addInfo);
            JSONObject jsonObject = new JSONObject(map);
            message = new StringBuilder("<root>").append(XML.toString(jsonObject)).append("</root>").toString();

            obj = mapper.convertValue(message, Object.class);
        } else {
            map.put("partner_request", vo);

            AdditionalInfoVO addInfoVO = new AdditionalInfoVO();
            addInfoVO.setTransactionId(vo.getTransactionId());
            addInfoVO.setPartnerPrice(partnerResult.getPartnerPrice());
            addInfoVO.setBillerPrice(billerResult.getBillerPrice());
            addInfoVO.setAdminFee(partnerResult.getPartnerFee());
            //addInfoVO.setAdminFee(billerResult.getAdminFee());
            addInfoVO.setTime(FormatUtil.getTime("yyyyMMddHHmmssSSS"));

            map.put("additional_info", addInfoVO);

            obj = mapper.convertValue(map, Object.class);
            System.out.println(">>> Resp:" + obj);
        }
        return obj;
    }

    protected Object getResponse(RequestVO vo, BillerResult billerResult, String msgResponse) throws Exception {
        //PartnerResult partnerResult = partnerApiRepository.findPartnerInfo(vo.getPartnerId(), vo.getProductCode());
        //logger.info("partnerPrice: " + partnerResult.getPartnerPrice() + ", partnerFee: " + partnerResult.getPartnerFee());

        //ObjectMapper mapper = new ObjectMapper();
        if (transformType.equals("JSON_TO_XML")) {
            JSONObject jsonObject = XML.toJSONObject(msgResponse);
            msgResponse = jsonObject.toString();
        }

        //String request = mapper.writeValueAsString(o);
        //Object obj = constructMessage(msgResponse, vo, billerResult);
        return constructMessage(msgResponse, vo, billerResult);
    }

    protected ParamsVO createParamsVO(String name, String value){
        try{
            ParamsVO result=new ParamsVO();
            result.setName(name);
            result.setValue(value);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw new NostraException(e.getMessage(), StatusCode.ERROR);
        }
    }

    protected Object cleanUpResponse(Object obj, RequestVO vo, BillerResult billerResult) throws Exception {
        List<ParamsVO> paramsVOList= new ArrayList<>();

        if (vo.getParams() != null){
            paramsVOList = vo.getParams();
        }

        ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(obj);
        Map<String, Object> map = mapper.readValue(data, new TypeReference<Map<String, Object>>() {
        });

//        if (map.containsKey("TransactionId")) {
//            transactionId = map.get("TransactionId").toString();
//            transactionIdResp = map.get("TransactionId").toString();
//        }

        if (map.containsKey("AggregatorTID")) {
//            billerTransId = map.get("AggregatorTID").toString();
            paramsVOList.add(createParamsVO("billerTransId",map.get("AggregatorTID").toString()));
            map.remove("AggregatorTID");
        }
        //System.out.println(">>>> AggregatorTID.1: " + billerTransId);

        if (map.containsKey("AggregatorPrice")) {
            billerRespPrice = map.get("AggregatorPrice").toString();
            map.remove("AggregatorPrice");
        }
        if (billerRespPrice == null || billerRespPrice.equals("")) {
            billerRespPrice = Integer.toString(billerResult.getBillerPrice());
        }
        paramsVOList.add(createParamsVO("billerRespPrice",billerRespPrice));
        //System.out.println(">>>> AggregatorPrice.2: " + billerRespPrice);

        if (map.containsKey("AggregatorFee")) {
            billerRespFee = map.get("AggregatorFee").toString();
            map.remove("AggregatorFee");
        }
        if (billerRespFee == null || billerRespFee.equals("")) {
            billerRespFee = Integer.toString(billerResult.getAdminFee());
        }
        //System.out.println(">>>> AggregatorFee.3: " + billerRespFee);
        paramsVOList.add(createParamsVO("billerRespFee",billerRespFee));

        if (map.containsKey("ProductCode")) {
            map.put("ProductCode", partnerProductCode);
            paramsVOList.add(createParamsVO("partnerProductCode",partnerProductCode));
        }

        //int x = billerResult.getId();
        //System.out.println("--> " + x);
        int billerId = billerResult.getBillerId();
        //System.out.println("--> " + billerId);
        String transStatus;

        if (map.containsKey("Status")) {
            String billerStatusCode = map.get("Status").toString();
            paramsVOList.add(createParamsVO("billerStatusCode",billerStatusCode));
            BMCode bmResultCode = statusMapping.get(billerId, billerStatusCode);
            if (bmResultCode != null) {
                String bmStatusCode = bmResultCode.getCode();

                paramsVOList.add(createParamsVO("bmStatusCode",bmStatusCode));

                if (bmStatusCode.equals("0")) {
                    transStatus = OK;
                } else if (bmStatusCode.equals("99")) {
                    transStatus = PENDING;
                } else {
                    transStatus = FAILED;
                }

                map.put("Status", bmStatusCode);
                map.put("Desc", bmResultCode.getDescription());
            } else {
                if (billerStatusCode.equals("0")) {
                    transStatus = OK;
                } else if (billerStatusCode.equals("99")) {
                    transStatus = PENDING;
                } else {
                    transStatus = FAILED;
                }
            }
            paramsVOList.add(createParamsVO("transStatus",transStatus));
        }

        vo.setParams(paramsVOList);

        //saveTransaction(vo, billerResult, status);
        return mapper.convertValue(map, Object.class);
    }

//    protected Object cleanUpResponse(Object obj, BillerResult billerResult) throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//        String data = mapper.writeValueAsString(obj);
//        Map<String, Object> map = mapper.readValue(data, new TypeReference<Map<String, Object>>() {
//        });
//
////        if (map.containsKey("TransactionId")) {
////            transactionId = map.get("TransactionId").toString();
////            transactionIdResp = map.get("TransactionId").toString();
////        }
//
//        if (map.containsKey("AggregatorTID")) {
//            billerTransId = map.get("AggregatorTID").toString();
//            map.remove("AggregatorTID");
//        }
//        //System.out.println(">>>> AggregatorTID.1: " + billerTransId);
//
//        if (map.containsKey("AggregatorPrice")) {
//            billerRespPrice = map.get("AggregatorPrice").toString();
//            map.remove("AggregatorPrice");
//        }
//        if (billerRespPrice == null || billerRespPrice.equals("")) {
//            billerRespPrice = Integer.toString(billerResult.getBillerPrice());
//        }
//        //System.out.println(">>>> AggregatorPrice.2: " + billerRespPrice);
//
//        if (map.containsKey("AggregatorFee")) {
//            billerRespFee = map.get("AggregatorFee").toString();
//            map.remove("AggregatorFee");
//        }
//        if (billerRespFee == null || billerRespFee.equals("")) {
//            billerRespFee = Integer.toString(billerResult.getAdminFee());
//        }
//        //System.out.println(">>>> AggregatorFee.3: " + billerRespFee);
//
//        if (map.containsKey("ProductCode")) {
//            map.put("ProductCode", partnerProductCode);
//        }
//
//        //int x = billerResult.getId();
//        //System.out.println("--> " + x);
//        int billerId = billerResult.getBillerId();
//        //System.out.println("--> " + billerId);
//        if (map.containsKey("Status")) {
//            billerStatusCode = map.get("Status").toString();
//            BMCode bmResultCode = statusMapping.get(billerId, billerStatusCode);
//            if (bmResultCode != null) {
//                bmStatusCode = bmResultCode.getCode();
//
//                if (bmStatusCode.equals("0")) {
//                    transStatus = OK;
//                } else if (bmStatusCode.equals("99")) {
//                    transStatus = PENDING;
//                } else {
//                    transStatus = FAILED;
//                }
//
//                map.put("Status", bmStatusCode);
//                map.put("Desc", bmResultCode.getDescription());
//            } else {
//                if (billerStatusCode.equals("0")) {
//                    transStatus = OK;
//                } else if (billerStatusCode.equals("99")) {
//                    transStatus = PENDING;
//                } else {
//                    transStatus = FAILED;
//                }
//            }
//        }
//
//        //saveTransaction(vo, billerResult, status);
//        return mapper.convertValue(map, Object.class);
//    }

    protected Object getCallbackInfo(Map<String, Object> map, TransTmp transTmp) {
        //System.out.println(">>>" + transTmp.getRequest());
        //RequestVO partnerRequest = new ObjectMapper().readValue(transTmp.getRequest(), RequestVO.class);
        //System.out.println(">>>" + partnerRequest);

        JSONObject json = new JSONObject(transTmp.getRequest());
        //System.out.println(">>>" + json);

        //map.put("partner_request", partnerRequest);
        map.put("partner_request", json);

        JSONObject addInfo = new JSONObject();
        addInfo.put("TransactionId", transTmp.getBmTid());
        addInfo.put("PartnerPrice", transTmp.getPartnerPrice());
        addInfo.put("BillerPrice", transTmp.getBillerPrice());
        addInfo.put("AdminFee", transTmp.getPartnerFee());
        addInfo.put("Time", FormatUtil.getTime("yyyyMMddHHmmssSSS"));
        map.put("additional_info", addInfo);

        JSONObject jsonObject = new JSONObject(map);
        Object obj = null;
        try {
            obj = new ObjectMapper().readValue(jsonObject.toString(), Object.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //return new JSONObject(map);
        return obj;
    }

    protected Object processCallbackResponse(Object obj, TransTmp record) throws Exception {
        //String[] result = null;

        ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(obj);
        Map<String, Object> map = mapper.readValue(data, new TypeReference<Map<String, Object>>() {
        });

        int billerId = record.getBillerId();
        if (map.containsKey("Status")) {
            billerStatusCode = map.get("Status").toString();
            BMCode bmResultCode = statusMapping.get(billerId, billerStatusCode);
            if (bmResultCode != null) {
                bmStatusCode = bmResultCode.getCode();

                if (bmStatusCode.equals("0")) {
                    transStatus = OK;
                } //else if (bmStatusCode.equals("99")) return false;
                else {
                    transStatus = FAILED;
                }

                map.put("Status", bmStatusCode);
                map.put("Desc", bmResultCode.getDescription());
            } else {
                if (billerStatusCode.equals("0")) {
                    transStatus = OK;
                } //else if (billerStatusCode.equals("99")) return false;
                else {
                    transStatus = FAILED;
                }
            }
        }

//        if (map.containsKey("TransactionId")) {
//            transactionId = map.get("TransactionId").toString();
//        }

        map.remove("AggregatorTID");

        if (map.containsKey("AggregatorPrice")) {
            billerRespPrice = map.get("AggregatorPrice").toString();
            map.remove("AggregatorPrice");
        }
        if (billerRespPrice == null || billerRespPrice.equals("")) {
            billerRespPrice = Integer.toString(record.getBillerPrice());
        }

        if (map.containsKey("AggregatorFee")) {
            billerRespFee = map.get("AggregatorFee").toString();
            map.remove("AggregatorFee");
        }
        if (billerRespFee == null || billerRespFee.equals("")) {
            billerRespFee = Integer.toString(record.getAdminFee());
        }

        if (map.containsKey("ProductCode")) {
            map.put("ProductCode", record.getPartnerCode());
        }

        //return mapper.convertValue(map, Object.class).toString();
        return mapper.convertValue(map, Object.class);
    }

    protected Object processCallbackResponse(Object obj, TransTmp record,TransHistory transHistory) throws Exception {
        //String[] result = null;

        ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(obj);
        Map<String, Object> map = mapper.readValue(data, new TypeReference<Map<String, Object>>() {
        });

        int billerId = record.getBillerId();
        String transStatus;
        if (map.containsKey("Status")) {
            String billerStatusCode = map.get("Status").toString();
            transHistory.setBillerStatus(billerStatusCode);
            BMCode bmResultCode = statusMapping.get(billerId, billerStatusCode);
            if (bmResultCode != null) {
                String bmStatusCode = bmResultCode.getCode();
                transHistory.setBmStatus(bmStatusCode);

                if (bmStatusCode.equals("0")) {
                    transStatus = OK;
                } //else if (bmStatusCode.equals("99")) return false;
                else {
                    transStatus = FAILED;
                }

                map.put("Status", bmStatusCode);
                map.put("Desc", bmResultCode.getDescription());
            } else {
                if (billerStatusCode.equals("0")) {
                    transStatus = OK;
                } //else if (billerStatusCode.equals("99")) return false;
                else {
                    transStatus = FAILED;
                }
            }
            transHistory.setStatus(transStatus);
        }

//        if (map.containsKey("TransactionId")) {
//            transactionId = map.get("TransactionId").toString();
//        }

        map.remove("AggregatorTID");

        if (map.containsKey("AggregatorPrice")) {
            billerRespPrice = map.get("AggregatorPrice").toString();
            map.remove("AggregatorPrice");
        }
        if (billerRespPrice == null || billerRespPrice.equals("")) {
            billerRespPrice = Integer.toString(record.getBillerPrice());
        }

        transHistory.setBillerPrice(Integer.valueOf(billerRespPrice));

        if (map.containsKey("AggregatorFee")) {
            billerRespFee = map.get("AggregatorFee").toString();
            map.remove("AggregatorFee");
        }
        if (billerRespFee == null || billerRespFee.equals("")) {
            billerRespFee = Integer.toString(record.getAdminFee());
        }

        transHistory.setAdminFee(Integer.valueOf(billerRespFee));

        if (map.containsKey("ProductCode")) {
            map.put("ProductCode", record.getPartnerCode());
        }

        //return mapper.convertValue(map, Object.class).toString();
        return mapper.convertValue(map, Object.class);
    }

//    protected void saveTransaction(RequestVO vo, BillerResult billerResult, String status) {
//        try {
//            TransHistory transHistory = new TransHistory();
//            transHistory.setPartnerId(billerResult.getPartnerId());
//            transHistory.setBillerId(billerResult.getBillerId());
//            transHistory.setPartnerTid(vo.getRequestId());
//            transHistory.setBillerTid(billerTransId);
//            transHistory.setBmTid(vo.getTransactionId());
//            transHistory.setPartnerCode(partnerProductCode);
//            transHistory.setBillerCode(billerResult.getBillerCode());
//            transHistory.setPartnerPrice(partnerResult.getPartnerPrice());
//            transHistory.setPartnerFee(partnerResult.getPartnerFee());
//            if (billerRespPrice != null) {
//                transHistory.setBillerPrice(Integer.parseInt(billerRespPrice));
//            }
//            //transHistory.setDenom();
//            if (billerRespFee != null) {
//                transHistory.setAdminFee(Integer.parseInt(billerRespFee));
//            }
//            transHistory.setStatus(status);
//            transHistory.setBmStatus(bmStatusCode);
//            transHistory.setBillerStatus(billerStatusCode);
//
//            transHistoryRepository.save(transHistory);
//
//            vo.setProductCode(partnerProductCode);
//
//            logger.info("Trans History:"
//                    + "PartnerId:" + billerResult.getPartnerId()
//                    + ",BillerId:" + billerResult.getBillerId()
//                    + ",PartnerTID:" + vo.getRequestId()
//                    + ",BillerTID:" + billerTransId
//                    + ",BmTID:" + vo.getTransactionId()
//                    + ",PartnerCode:" + partnerProductCode
//                    + ",BillerCode:" + billerResult.getBillerCode()
//                    + ",PartnerPrice:" + partnerResult.getPartnerPrice()
//                    + ",PartnerFee:" + partnerResult.getPartnerFee()
//                    + ",BillerPrice:" + billerRespPrice
//                    + //transHistory.setDenom();
//                    ",AdminFee:" + billerRespFee
//                    + ",Status:" + status
//                    + ",BMStatus:" + bmStatusCode
//                    + ",BillerStatus:" + billerStatusCode);
//
//        } catch (Exception e) {
//            logger.info("Cannot log DB for transaction: " + e.getMessage());
//        }
//    }

    protected void saveTransaction(RequestVO vo, BillerResult billerResult, String status) {
        try {
            TransHistory transHistory = new TransHistory();
            transHistory.setPartnerId(billerResult.getPartnerId());
            transHistory.setBillerId(billerResult.getBillerId());
            transHistory.setPartnerTid(vo.getRequestId());
            transHistory.setBillerTid(billerTransId);
            transHistory.setBmTid(vo.getTransactionId());
            transHistory.setPartnerCode(partnerProductCode);
            transHistory.setBillerCode(billerResult.getBillerCode());
            transHistory.setPartnerPrice(partnerResult.getPartnerPrice());
            transHistory.setPartnerFee(partnerResult.getPartnerFee());
            if (billerRespPrice != null) {
                transHistory.setBillerPrice(Integer.parseInt(billerRespPrice));
            }
            //transHistory.setDenom();
            if (billerRespFee != null) {
                transHistory.setAdminFee(Integer.parseInt(billerRespFee));
            }
            transHistory.setStatus(status);
            transHistory.setBmStatus(bmStatusCode);
            transHistory.setBillerStatus(billerStatusCode);
            if (vo.getParams() != null){
                for (ParamsVO p: vo.getParams()){
                    if (p.getName().equals("transStatus")){
                        transHistory.setStatus(p.getValue());
                    }
                    if (p.getName().equals("bmStatusCode")){
                        transHistory.setBmStatus(p.getValue());
                    }
                    if (p.getName().equals("billerStatusCode")){
                        transHistory.setBillerStatus(p.getValue());
                    }
                    if (p.getName().equals("billerTransId")){
                        transHistory.setBillerTid(p.getValue());
                    }
                    if (p.getName().equals("partnerProductCode")){
                        transHistory.setPartnerCode(p.getValue());
                    }
                    if (p.getName().equals("billerRespPrice")){
                        transHistory.setBillerPrice(Integer.valueOf(p.getValue()));
                    }
                    if (p.getName().equals("billerRespFee")){
                        transHistory.setAdminFee(Integer.valueOf(p.getValue()));
                    }
                }
            }

            transHistoryRepository.save(transHistory);

            vo.setProductCode(partnerProductCode);

            logger.info("Trans History:"
                    + "PartnerId:" + billerResult.getPartnerId()
                    + ",BillerId:" + billerResult.getBillerId()
                    + ",PartnerTID:" + vo.getRequestId()
                    + ",BillerTID:" + billerTransId
                    + ",BmTID:" + vo.getTransactionId()
                    + ",PartnerCode:" + partnerProductCode
                    + ",BillerCode:" + billerResult.getBillerCode()
                    + ",PartnerPrice:" + partnerResult.getPartnerPrice()
                    + ",PartnerFee:" + partnerResult.getPartnerFee()
                    + ",BillerPrice:" + billerRespPrice
                    + //transHistory.setDenom();
                    ",AdminFee:" + billerRespFee
                    + ",Status:" + status
                    + ",BMStatus:" + bmStatusCode
                    + ",BillerStatus:" + billerStatusCode);

        } catch (Exception e) {
            logger.info("Cannot log DB for transaction: " + e.getMessage());
        }
    }

    protected void updateBmLogResponse(RequestVO vo,String resFe,String resBe){
        BmLog bmLog= bmLogService.findByPartnerTidAndBMTid(vo.getRequestId(),vo.getTransactionId());
        bmLog.setBillerTid(billerTransId);
        bmLog.setResBe(resBe);
        bmLog.setResFe(resFe);
        if (vo.getParams() != null){
            for (ParamsVO p: vo.getParams()){
                if (p.getName().equals("billerTransId")){
                    bmLog.setBillerTid(p.getValue());
                }

            }
        }
        bmLogService.save(bmLog);
    }

    protected void updateBmLogCallback(String partnerTid,String bmTid,String cbFe,String cbBe){
        BmLog bmLog= bmLogService.findByPartnerTidAndBMTid(partnerTid,bmTid);
        bmLog.setCbBe(cbBe);
        bmLog.setCbFe(cbFe);
        bmLogService.save(bmLog);
    }

    protected TransTmp getTransTmp(String bmTid, int biller) {
        //System.out.println(">>> transTmpRepository::" + transTmpRepository);
        return transTmpRepository.findTrans(bmTid, biller);
    }

    protected void deleteTransTmp(TransTmp transTmp) {
        //System.out.println(">>> transTmpRepository::" + transTmpRepository);
        try {
            transTmpRepository.delete(transTmp);
            logger.info("Delete pending transaction with BMTid: " + transTmp.getBmTid());
        }catch (Exception ex){
            logger.error("Failed Delete pending transaction with BMTid : "+transTmp.getBmTid() +"Reason : "+ex.getMessage());
        }
    }

    protected void updateTransaction(int billerRespPrice, int billerRespFee, String billerCode, String bmCode, String status, String bmTid) {
        try {
            transHistoryRepository.updateTransaction(billerRespPrice, billerRespFee, billerCode, bmCode, status, bmTid);

            logger.info("BmTID:" + bmTid
                    + ",BillerPrice:" + billerRespPrice
                    + ",AdminFee:" + billerRespFee
                    + ",BillerStatus:" + billerCode
                    + ",BMStatus:" + bmCode
                    + ",Status:" + status);

        } catch (Exception e) {
            logger.info("Cannot log DB for transaction: " + e.getMessage());
        }
    }

    protected void updateTransaction(String bmTid) {
        try {
            logger.info("Update Transaction:"
                    +"BmTID:" + bmTid
                    + ",BillerPrice:" + billerRespPrice
                    + ",AdminFee:" + billerRespFee
                    + ",BillerStatus:" + billerStatusCode
                    + ",BMStatus:" + bmStatusCode
                    + ",Status:" + transStatus);
            transHistoryRepository.updateTransaction(Integer.parseInt(billerRespPrice), Integer.parseInt(billerRespFee), billerStatusCode, bmStatusCode, transStatus, bmTid);
        } catch (Exception e) {
            logger.info("Failed Update Transaction: " + e.getMessage());
        }
    }

    protected void updateTransaction(TransHistory transHistory) {
        try {
            logger.info("Update Transaction:"
                    +"BmTID:" + transHistory.getBmTid()
                    +"PartnerTID:" + transHistory.getPartnerTid()
                    + ",BillerPrice:" + transHistory.getBillerPrice()
                    + ",AdminFee:" + transHistory.getAdminFee()
                    + ",BillerStatus:" + transHistory.getBillerStatus()
                    + ",BMStatus:" + transHistory.getBmStatus()
                    + ",Status:" + transHistory.getStatus());
            transHistoryRepository.updateTransaction2(transHistory.getBillerPrice(), transHistory.getAdminFee(), transHistory.getBillerStatus(), transHistory.getBmStatus(), transHistory.getStatus(), transHistory.getBmTid(),transHistory.getPartnerTid());
        } catch (Exception e) {
            logger.info("Failed Update Transaction: " + e.getMessage());
        }
    }

    protected void updateTransaction(String bmTid,String billerRespPrice,String billerRespFee,String billerStatusCode,String bmStatusCode,String transStatus) {
        try {
            logger.info("Update Transaction:"
                    +"BmTID:" + bmTid
                    + ",BillerPrice:" + billerRespPrice
                    + ",AdminFee:" + billerRespFee
                    + ",BillerStatus:" + billerStatusCode
                    + ",BMStatus:" + bmStatusCode
                    + ",Status:" + transStatus);
            transHistoryRepository.updateTransaction(Integer.valueOf(billerRespPrice), Integer.valueOf(billerRespFee), billerStatusCode, bmStatusCode, transStatus, bmTid);
        } catch (Exception e) {
            logger.info("Failed Update Transaction: " + e.getMessage());
        }
    }


    protected void savePendingTransaction(RequestVO vo, BillerResult billerResult, String url, String msgResponse, int type) throws Exception {
        try {
            vo.setProductCode(partnerProductCode);
            String data = new ObjectMapper().writeValueAsString(vo);

            TransTmp transTmp = new TransTmp();
            transTmp.setRequest(data);
            transTmp.setResponse(msgResponse);

            transTmp.setPartnerId(billerResult.getPartnerId());
            transTmp.setMethod(vo.getMethod());
            transTmp.setReffId(vo.getReffId());
            transTmp.setPartnerName(vo.getPartnerId());
            transTmp.setPartnerTid(vo.getRequestId());
            transTmp.setPartnerCode(partnerProductCode);
            transTmp.setPartnerPrice(partnerResult.getPartnerPrice());
            transTmp.setPartnerFee(partnerResult.getPartnerFee());
            transTmp.setPartnerUrl(partnerResult.getPartnerUrl());

            transTmp.setBillerId(billerResult.getBillerId());
            transTmp.setBillerTid(billerTransId);
            transTmp.setBillerApiId(billerResult.getApiId());
            transTmp.setBillerPrice(billerResult.getBillerPrice());
            transTmp.setAdminFee(billerResult.getAdminFee());
            transTmp.setTransformId(billerResult.getTransformId());
            transTmp.setUrl(url);

            transTmp.setBmTid(vo.getTransactionId());
            transTmp.setStatus(type);

            if (vo.getParams() != null){
                for (ParamsVO p: vo.getParams()){

                    if (p.getName().equals("billerTransId")){
                        transTmp.setBillerTid(p.getValue());
                    }
                    if (p.getName().equals("partnerProductCode")){
                        transTmp.setPartnerCode(p.getValue());
                    }

                }
            }

            transTmpRepository.save(transTmp);

            logger.info("Pending Trans:"
                    + "setPartnerId:" + transTmp.getPartnerId()
                    + ",setMethod:" + transTmp.getMethod()
                    + ",setReffId:" + transTmp.getReffId()
                    + ",setPartnerName:" + transTmp.getPartnerName()
                    + ",setPartnerTid:" + transTmp.getPartnerTid()
                    + ",setPartnerCode:" + transTmp.getPartnerCode()
                    + ",setPartnerPrice:" + transTmp.getPartnerPrice()
                    + ",setPartnerFee:" + transTmp.getPartnerFee()
                    + ",setPartnerUrl:" + transTmp.getPartnerUrl()
                    + ",setBillerId:" + transTmp.getBillerId()
                    +",setBillerTid:" + transTmp.getBillerTid()
                    + ",setBillerApiId:" + transTmp.getBillerApiId()
                    + ",setBillerPrice:" + transTmp.getBillerPrice()
                            + ",setAdminFee:" + transTmp.getAdminFee()
                            + ",setTransformId:" + transTmp.getTransformId()
                            + ",setUrl:" + transTmp.getUrl()
                            + ",setBmTid:" + transTmp.getBmTid()
                            + ",setStatus:" + transTmp.getStatus()
            );
        }catch (Exception ex){
            logger.error("Failed Save Pending Trans: " + ex.getMessage());
        }
    }

    protected int getTimeout(String param) {
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

    protected String sendRequest(String url, String httpRequest) throws IOException {
        return sendRequest(url, null, httpRequest);
    }

    protected String sendRequest(String url, String httpRequest, int connectTimeout, int readTimeout) throws IOException {
        return sendRequest(url, null, httpRequest, connectTimeout, readTimeout);
    }

    protected String sendRequest(String url, Map<String, String> map, String httpRequest) throws IOException {
        return sendRequest(url, map, httpRequest, getTimeout("http.connect.timeout"), getTimeout("http.read.timeout"));
    }

    protected String sendRequestRedirectPost(String url, Map<String, String> map, String httpRequest) throws IOException {
        return sendRequestRedirectPost(url, map, httpRequest, getTimeout("http.connect.timeout"), getTimeout("http.read.timeout"));
    }

    protected String sendRequest(String url, Map<String, String> map, String httpRequest, int connectTimeout, int readTimeout) throws IOException {
        logger.info(httpProcessingService + "," + url + "," + map + "," + httpRequest + "," + connectTimeout + "," + readTimeout);
        return httpProcessingService.postUser(url, map, httpRequest, connectTimeout, readTimeout);
    }

    protected String sendRequestRedirectPost(String url, Map<String, String> map, String httpRequest, int connectTimeout, int readTimeout) throws IOException {
        logger.info(httpProcessingService.toString() + "," + url + "," + map + "," + httpRequest + "," + connectTimeout + "," + readTimeout);
        return httpProcessingService.redirectPostUser(url, map, httpRequest, connectTimeout, readTimeout);
    }

    protected String sendRequestBypassSSLCert(String url, Map<String, String> map, String httpRequest, int connectTimeout, int readTimeout) throws IOException {
        return httpProcessingService.postUserBypasSSLCert(url, map, httpRequest, connectTimeout, readTimeout);
    }

    protected Xresponse sendRequestV2(String url, Map<String, String> map, String httpRequest, int connectTimeout, int readTimeout) {
        return httpProcessingService.postUserV2(url, map, httpRequest, connectTimeout, readTimeout);
    }

    protected String sendRequest(String url, Map<String, String> map, int connectTimeout, int readTimeout) throws IOException {
        return httpProcessingService.get(url, map, connectTimeout, readTimeout);
    }

    protected Xresponse sendRequestV2(String url, Map<String, String> map, int connectTimeout, int readTimeout) {
        return httpProcessingService.getV2(url, map, connectTimeout, readTimeout);
    }

    protected String sendRequest(String url) throws IOException {
        return httpProcessingService.get(url);
    }

    protected void setTransformType(String type) {
        this.transformType = type;
    }
}
