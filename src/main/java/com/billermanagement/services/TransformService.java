/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.services;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.billermanagement.enums.Jolt;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.enums.TransformType;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.Transform;
import com.billermanagement.persistance.repository.TransformRepository;
import com.billermanagement.util.DateDifferent;
import com.billermanagement.util.GlobalHashmap;
import com.billermanagement.util.XmlFormatter;
import com.billermanagement.vo.TransformDistinctVO;
import com.billermanagement.vo.TransformReqVO;
import com.billermanagement.vo.TransformResVO;
import com.billermanagement.vo.TransformUpdateVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author sulaeman
 */
@Service
public class TransformService {
    
    @Autowired
    private TransformRepository repo;
    
    @Autowired
    private GlobalHashmap globalHashMap;
    
    Logger log = LoggerFactory.getLogger(TransformService.class);
    
    public List<Transform> getAll() {
        return repo.findAll();
    }
    
    public List<TransformDistinctVO> getDistinct() {
        return repo.getDistinct();
    }
    
    public Transform addNew(TransformReqVO reqVO) {
        Transform t = new Transform();
        
        t.setTransformId(reqVO.getTransformId().trim());
        t.setMethod(reqVO.getMethod().trim());
        t.setName(reqVO.getName().trim());
        t.setType(reqVO.getType().trim());
        t.setUrl(reqVO.getUrl().trim());
        t.setFlowType(reqVO.getFlowType().trim());
        t.setFileRequest(reqVO.getFileRequest());
        t.setFileResponse(reqVO.getFileResponse());
        t.setFileCallback(reqVO.getFileCallback());
        t = repo.save(t);
        
        String id = t.getTransformId() + "." + t.getMethod();
        globalHashMap.setTransformHashMap(id, t);
        
        return t;
    }
    
    public Transform update(TransformUpdateVO updateVO) {
        Transform t = repo.findById(updateVO.getId()).get();
        
        t.setTransformId(updateVO.getTransformId().trim());
        t.setMethod(updateVO.getMethod().trim());
        t.setName(updateVO.getName().trim());
        t.setType(updateVO.getType().trim());
        t.setUrl(updateVO.getUrl().trim());
        t.setFlowType(updateVO.getFlowType().trim());
        t.setFileRequest(updateVO.getFileRequest());
        t.setFileResponse(updateVO.getFileResponse());
        t.setFileCallback(updateVO.getFileCallback());
        t = repo.save(t);
        
        String id = t.getTransformId() + "." + t.getMethod();
        globalHashMap.updateTransformHashMap(id, t);
        return t;
    }
    
    public TransformResVO deleteById(Integer id) {
        TransformResVO resVO = null;
        try {
            Transform t = repo.findById(id).get();
            repo.deleteById(t.getId());
            resVO = new TransformResVO();
            resVO.setId(t.getId());
            resVO.setTransformId(t.getTransformId());
            resVO.setMethod(t.getMethod());
            resVO.setName(t.getName());
            resVO.setType(t.getType());
            resVO.setUrl(t.getUrl());
            resVO.setFlowType(t.getFlowType());
            resVO.setFileRequest(t.getFileRequest());
            resVO.setFileResponse(t.getFileResponse());
            resVO.setFileCallback(t.getFileCallback());
            String key = t.getTransformId() + "." + t.getMethod();
            globalHashMap.removeHashMap(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return resVO;
    }
    
    public TransformResVO getById(Integer id) {
        Transform t = repo.findById(id).get();
        if (t == null) {
            throw new NostraException("Transform is not found", StatusCode.DATA_NOT_FOUND);
        }
        TransformResVO resVO = new TransformResVO();
        resVO.setId(t.getId());
        resVO.setTransformId(t.getTransformId());
        resVO.setMethod(t.getMethod());
        resVO.setName(t.getName());
        resVO.setType(t.getType());
        resVO.setUrl(t.getUrl());
        resVO.setFlowType(t.getFlowType());
        resVO.setFileRequest(t.getFileRequest());
        resVO.setFileResponse(t.getFileResponse());
        resVO.setFileCallback(t.getFileCallback());
        return resVO;
    }

    /*
    params : 1. transformId
             2. method [Inquiry,Payment ]
             3. data [object to scan]
             4. enum Jolt [Jolt.Request,Jolt.Response, Jolt.Callback]
    
    return : 1. transformed json
             2. url
             3. flowType [Sync,Async]
     */
    @SuppressWarnings("null")
    public Object[] transformApi(String transformId, String method, Object data, Jolt j) {
        Instant timeStart = Instant.now();
        Object[] returnObj = new Object[3];
        Object transformedOutput = data;
        String url;
        String flowType;
        Transform t = getTransform(transformId, method);
        if (t != null) {
            try {
                String key = transformId + "." + method;
                log.info("Object Data : " + data.toString());
                if (t.getType().equalsIgnoreCase(TransformType.JSON_TO_JSON.toString())) {
                    try {
                        if (j.equals(Jolt.JoltRequest)) {
//                        Blob blob = new SerialBlob(t.getFileRequest());
//                        InputStream in = blob.getBinaryStream();
//                        Object chainrSpecJSON = JsonUtils.jsonToObject(in);
                            Object chainrSpecJSON = JsonUtils.jsonToObject(t.getFileRequest());
                            Chainr chainr = Chainr.fromSpec(chainrSpecJSON);
                            transformedOutput = chainr.transform(data);
                            log.info(JsonUtils.toPrettyJsonString(transformedOutput));
                            String jsonString = JsonUtils.toPrettyJsonString(transformedOutput);
                            while (jsonString.contains("RESCANX")) {
                                log.info("Processing RESCANX here....");
                                String scanSeq = jsonString.substring(jsonString.indexOf("RESCANX_"), jsonString.indexOf("_SEQ"));
                                String seq = scanSeq.substring(scanSeq.indexOf("_")).replaceAll("_", "");
                                String targetMethod = method + "." + seq;
                                log.info("Target Method : " + targetMethod);
                                t = getTransform(transformId, targetMethod);
                                chainrSpecJSON = JsonUtils.jsonToObject(t.getFileRequest());
                                chainr = Chainr.fromSpec(chainrSpecJSON);
                                transformedOutput = chainr.transform(transformedOutput);
                                jsonString = JsonUtils.toPrettyJsonString(transformedOutput);
                                log.info(jsonString);
                            }
                        } else if (j.equals(Jolt.JoltResponse)) {
//                        Blob blob = new SerialBlob(t.getFileResponse());
//                        InputStream in = blob.getBinaryStream();
//                        Object chainrSpecJSON = JsonUtils.jsonToObject(in);
                            Object chainrSpecJSON = JsonUtils.jsonToObject(t.getFileResponse());
                            Chainr chainr = Chainr.fromSpec(chainrSpecJSON);
                            transformedOutput = chainr.transform(data);
                            log.info(JsonUtils.toPrettyJsonString(transformedOutput));
                            
                            String jsonString = JsonUtils.toPrettyJsonString(transformedOutput);
                            while (jsonString.contains("RESCANX")) {
                                log.info("Processing RESCANX here....");
                                String scanSeq = jsonString.substring(jsonString.indexOf("RESCANX_"), jsonString.indexOf("_SEQ"));
                                String seq = scanSeq.substring(scanSeq.indexOf("_")).replaceAll("_", "");
                                String targetMethod = method + "." + seq;
                                log.info("Target Method : " + targetMethod);
                                t = getTransform(transformId, targetMethod);
                                chainrSpecJSON = JsonUtils.jsonToObject(t.getFileResponse());
                                chainr = Chainr.fromSpec(chainrSpecJSON);
                                transformedOutput = chainr.transform(transformedOutput);
                                jsonString = JsonUtils.toPrettyJsonString(transformedOutput);
                                log.info(jsonString);
                            }
                        } else {
//                        Blob blob = new SerialBlob(t.getFileCallback());
//                        InputStream in = blob.getBinaryStream();
//                        Object chainrSpecJSON = JsonUtils.jsonToObject(in);
                            Object chainrSpecJSON = JsonUtils.jsonToObject(t.getFileCallback());
                            Chainr chainr = Chainr.fromSpec(chainrSpecJSON);
                            transformedOutput = chainr.transform(data);
                            log.info(JsonUtils.toPrettyJsonString(transformedOutput));
                            
                            String jsonString = JsonUtils.toPrettyJsonString(transformedOutput);
                            while (jsonString.contains("RESCANX")) {
                                log.info("Processing RESCANX here....");
                                String scanSeq = jsonString.substring(jsonString.indexOf("RESCANX_"), jsonString.indexOf("_SEQ"));
                                String seq = scanSeq.substring(scanSeq.indexOf("_")).replaceAll("_", "");
                                String targetMethod = method + "." + seq;
                                log.info("Target Method : " + targetMethod);
                                t = getTransform(transformId, targetMethod);
                                chainrSpecJSON = JsonUtils.jsonToObject(t.getFileCallback());
                                chainr = Chainr.fromSpec(chainrSpecJSON);
                                transformedOutput = chainr.transform(transformedOutput);
                                jsonString = JsonUtils.toPrettyJsonString(transformedOutput);
                                log.info(jsonString);
                            }
                        }
                        
                    } catch (JSONException e) {
                        log.error(e.getMessage());
                        transformedOutput = data;
                    }
                } else if (t.getType().equalsIgnoreCase(TransformType.JSON_TO_XML.toString())) {
                    try {
                        if (j.equals(Jolt.JoltRequest)) {
//                        Blob blob = new SerialBlob(t.getFileRequest());
//                        InputStream in = blob.getBinaryStream();
//                        Object chainrSpecJSON = JsonUtils.jsonToObject(in);
                            Object chainrSpecJSON = JsonUtils.jsonToObject(t.getFileRequest());
                            Chainr chainr = Chainr.fromSpec(chainrSpecJSON);
                            transformedOutput = chainr.transform(data);
                            log.info(JsonUtils.toPrettyJsonString(transformedOutput));
                            JSONObject jsonObject = new JSONObject(JsonUtils.toPrettyJsonString(transformedOutput));
                            String xml = XML.toString(jsonObject);
                            log.info(Jolt.JoltRequest + " : " + new XmlFormatter().format(xml));
                            transformedOutput = xml;
                            String xmlString = xml;
                            while (xmlString.contains("RESCANX")) {
                                log.info("Processing RESCANX here....");
                                String scanSeq = xmlString.substring(xmlString.indexOf("RESCANX_"), xmlString.indexOf("_SEQ"));
                                String seq = scanSeq.substring(scanSeq.indexOf("_")).replaceAll("_", "");
                                String targetMethod = method + "." + seq;
                                log.info("Target Method : " + targetMethod);
                                t = getTransform(transformId, targetMethod);
                                chainrSpecJSON = JsonUtils.jsonToObject(t.getFileRequest());
                                chainr = Chainr.fromSpec(chainrSpecJSON);
                                transformedOutput = chainr.transform(transformedOutput);
                                jsonObject = new JSONObject(JsonUtils.toPrettyJsonString(transformedOutput));
                                xml = XML.toString(jsonObject);
                                log.info(Jolt.JoltRequest + " : " + new XmlFormatter().format(xml));
                                transformedOutput = xml;
                                xmlString = xml;
                                log.info(xmlString);
                            }
                        } else if (j.equals(Jolt.JoltResponse)) {
                            String inputXML = data.toString();
                            log.info(Jolt.JoltResponse + " : " + new XmlFormatter().format(inputXML));
                            JSONObject jsonObject = XML.toJSONObject(inputXML);
                            ObjectMapper mapper = new ObjectMapper();
                            Object obj = mapper.readValue(jsonObject.toString(), Object.class);
                            log.info("XML_TO_JSON : " + obj);
//                        Blob blob = new SerialBlob(t.getFileResponse());
//                        InputStream in = blob.getBinaryStream();
//                        Object chainrSpecJSON = JsonUtils.jsonToObject(in);
                            Object chainrSpecJSON = JsonUtils.jsonToObject(t.getFileResponse());
                            Chainr chainr = Chainr.fromSpec(chainrSpecJSON);
                            transformedOutput = chainr.transform(obj);
                            log.info(JsonUtils.toPrettyJsonString(transformedOutput));
                            
                            String jsonString = JsonUtils.toPrettyJsonString(transformedOutput);
                            while (jsonString.contains("RESCANX")) {
                                log.info("Processing RESCANX here....");
                                String scanSeq = jsonString.substring(jsonString.indexOf("RESCANX_"), jsonString.indexOf("_SEQ"));
                                String seq = scanSeq.substring(scanSeq.indexOf("_")).replaceAll("_", "");
                                String targetMethod = method + "." + seq;
                                log.info("Target Method : " + targetMethod);
                                t = getTransform(transformId, targetMethod);
                                chainrSpecJSON = JsonUtils.jsonToObject(t.getFileResponse());
                                chainr = Chainr.fromSpec(chainrSpecJSON);
                                transformedOutput = chainr.transform(transformedOutput);
                                jsonString = JsonUtils.toPrettyJsonString(transformedOutput);
                                log.info(jsonString);
                            }
                        } else {
                            String inputXML = data.toString();
                            log.info(Jolt.JoltCallback + " : " + new XmlFormatter().format(inputXML));
                            JSONObject jsonObject = XML.toJSONObject(inputXML);
                            ObjectMapper mapper = new ObjectMapper();
                            Object obj = mapper.readValue(jsonObject.toString(), Object.class);
                            log.info("XML_TO_JSON : " + obj);
//                        Blob blob = new SerialBlob(t.getFileCallback());
//                        InputStream in = blob.getBinaryStream();
//                        Object chainrSpecJSON = JsonUtils.jsonToObject(in);
                            Object chainrSpecJSON = JsonUtils.jsonToObject(t.getFileCallback());
                            Chainr chainr = Chainr.fromSpec(chainrSpecJSON);
                            transformedOutput = chainr.transform(obj);
                            log.info(JsonUtils.toPrettyJsonString(transformedOutput));
                            
                            String jsonString = JsonUtils.toPrettyJsonString(transformedOutput);
                            while (jsonString.contains("RESCANX")) {
                                log.info("Processing RESCANX here....");
                                String scanSeq = jsonString.substring(jsonString.indexOf("RESCANX_"), jsonString.indexOf("_SEQ"));
                                String seq = scanSeq.substring(scanSeq.indexOf("_")).replaceAll("_", "");
                                String targetMethod = method + "." + seq;
                                log.info("Target Method : " + targetMethod);
                                t = getTransform(transformId, targetMethod);
                                chainrSpecJSON = JsonUtils.jsonToObject(t.getFileCallback());
                                chainr = Chainr.fromSpec(chainrSpecJSON);
                                transformedOutput = chainr.transform(transformedOutput);
                                jsonString = JsonUtils.toPrettyJsonString(transformedOutput);
                                log.info(jsonString);
                            }
                        }
                        
                    } catch (JSONException | IOException e) {
                        log.error(e.getMessage());
                        transformedOutput = data;
                    }
                } else {
//                throw new NostraException("Sorry! Transform Type not match ", StatusCode.ERROR);
                    log.info("Sorry! Transform Type not match ");
                    transformedOutput = data;
                }
            } catch (NostraException e) {
                log.error("transformApi error -> " + e.getMessage());
                transformedOutput = data;
            }
            
            url = t.getUrl();
            flowType = t.getFlowType();
        } else {
            transformedOutput = data;
            url = "";
            flowType = "";
        }
        returnObj[0] = transformedOutput;
        returnObj[1] = url;
        returnObj[2] = flowType;
        Instant timeStop = Instant.now();
        log.info("Duration time : " + new DateDifferent().getInMillis(timeStart, timeStop));
        return returnObj;
    }
    
    private Transform getTransform(String transformId, String method) {
        String key = transformId + "." + method;
        Transform t = null;
        if (globalHashMap.containsKey(key)) {
            Object[] objMap = globalHashMap.getHashMap(key);
            if (objMap[0] != null || !objMap[0].toString().isEmpty()) {
                t = new Transform();
                t.setId((Integer) objMap[0]);
                t.setType(objMap[1].toString());
                t.setFileRequest(objMap[2].toString());
                t.setFileResponse(objMap[3].toString());
                t.setFileCallback(objMap[4].toString());
                t.setUrl((String) objMap[5]);
                t.setFlowType((String) objMap[6]);
                long expiredTime = (long) objMap[7];
                long timeNow = globalHashMap.getTimeNow();
                if (expiredTime < timeNow) {
                    t = repo.findByTransformIdAndMethod(transformId, method).orElse(null);
                    if (t != null) {
                        globalHashMap.updateTransformHashMap(key, t);
                    }
                }
            } else {
                t = repo.findByTransformIdAndMethod(transformId, method).orElse(null);
                if (t != null) {
                    globalHashMap.updateTransformHashMap(key, t);
                }
            }
        } else {
            t = repo.findByTransformIdAndMethod(transformId, method).orElse(null);
            if (t != null) {
                globalHashMap.setTransformHashMap(key, t);
            }
            
        }
        return t;
    }
    
    public void refresh() {
        getAll().forEach((t) -> {
            globalHashMap.updateTransformHashMap(t.getTransformId() + "." + t.getMethod(), t);
        });
        log.info("The Transform in cach memory is refreshed");
    }
    
}
