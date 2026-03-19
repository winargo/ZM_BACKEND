package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.persistance.domain.TransHistory;
import com.billermanagement.persistance.domain.TransTmp;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.XML;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;
import java.util.*;

@Service
@Qualifier("irs")
public class IRS extends BillerRequest {

    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        //String requestType = getRequestType(billerResult.getTransformId(), vo.getMethod());
        //logger.info("requestType:" + requestType);
        //setTransformType(requestType);
        try {
            setTransformType("JSON_TO_XML");

            String[] request = getRequest(vo, billerResult, 20, "irs");
            logger.info("request: " + request[0]);

            String message = request[0].replaceAll("\"", "");
            logger.info("message: " + message);
            Map<String, String> map = new HashMap<>();
            map.put("Accept", "application/xml");
            map.put("Content-Type", "application/xml");

            //String msgResponse = sendRequest(request[1], map, message);
            //String url =  "http://localhost:8080/api/v1/request2";
            String msgResponse = sendRequest(request[1], map, message, getTimeout("irs.http.connect.timeout"), getTimeout("irs.http.read.timeout"));
            logger.info("IRS response: " + msgResponse);
            Object obj = getResponse(vo, billerResult, msgResponse);
            logger.info("IRS object to transform: " + obj);

            JAXBContext jaxbContext = JAXBContext.newInstance(MethodResponse.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            MethodResponse irs = (MethodResponse) jaxbUnmarshaller.unmarshal(new StringReader(msgResponse));

            boolean callback = false;
            Member[] members = irs.getParams().getParam().getValues().getStruct().getMember();
            for (Member member : members) {
                if (member.getName().equalsIgnoreCase("RESPONSECODE")) {
                    String respCode = member.getValue().getString();
                    int i=1;
                    while (!callback) {
                        String dbCode = InitDB.getInstance().get("irs.async.code." + i++);
                        if (dbCode == null) break;
                        if (respCode.equals(dbCode)) callback = true;
                    }
                    break;
                }
            }
            //System.out.println("callback:" + callback);

            Object[] result;
            //if (msgResponse.contains("<name>RESPONSECODE</name><value><string>68</string></value>")) {
            if (callback) {
                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltResponse);

                obj = cleanUpResponse(result[0],vo, billerResult);

                savePendingTransaction(vo, billerResult, null, null, HandlerConstant.IRS);

                saveTransaction(vo, billerResult, PENDING);
            } else {
                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltCallback);
                obj = cleanUpResponse(result[0],vo, billerResult);

                saveTransaction(vo, billerResult, transStatus);
            }

            updateBmLogResponse(vo,new ObjectMapper().writeValueAsString(obj),msgResponse);

            return obj;
        } catch (Exception e) {
            saveTransaction(vo, billerResult, ERROR);
            String errorMsg = "Unexpected Response - "+e.getMessage();
            updateBmLogResponse(vo,errorMsg,errorMsg);
            throw e;
        }
    }

    @Transactional
    public int callback(String message) {
        String transId;
        TransTmp transTmp = null;
        TransHistory transHistory = new TransHistory();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MethodCall.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            MethodCall irs = (MethodCall) jaxbUnmarshaller.unmarshal(new StringReader(message));

            System.out.println(">>>>> " + irs);

            ArrayList<Member> alMembers = new ArrayList<>();

            Member[] members = irs.getParams().getParam().getValues().getStruct().getMember();
            for (Member member : members) {
                if (member.getName().equalsIgnoreCase("REQUESTID")) {
                    transId = member.getValue().getString();
                    System.out.println("---> TransId: " + transId);
                    transTmp = getTransTmp(transId, HandlerConstant.IRS);
                    if (transTmp == null) return HandlerConstant.MISSING_TRANSACTION;
                    alMembers.add(member);
                } else if (member.getName().equalsIgnoreCase("SN")) {
                    String value = member.getValue().getString();
                    if (value.indexOf("/") != -1) {
                        String[] items = value.split("/");
                        int i = 1;
                        for (String item : items) {
                            Member m = new Member("SN_" + i++, new Value(item));
                            alMembers.add(m);
                            //System.out.println("1*** " + m);
                            //members.add(new Member(k, new Value(v)));
                            //map.put(items.substring(0, items.indexOf("=")), items.substring(items.indexOf("=")+1));
                        }
                    } else
                        alMembers.add(member);
                } else if (member.getName().equalsIgnoreCase("MESSAGE")) {
                    String value = member.getValue().getString();
                    //System.out.println(value);
                    if (value.indexOf(";") == -1) {
                        Member m = new Member("MESSAGE", new Value(value));
                        alMembers.add(m);
                        //System.out.println("3*** " + m);
                    } else {
                        String[] items = value.split(";");
                        for (String item : items) {
                            System.out.println(">> " + item);
                            String[] s = item.split("=");
                            String k = s[0].replaceAll("\\s", "");
                            String v = (s.length == 1) ? "NA" : s[1].trim();

                            if (v.indexOf('.') != -1 && !k.equalsIgnoreCase("Trx")) {
                                v = v.replace(".", "");
                                //System.out.println("---->v:" + v);
                            }

                            Member m = new Member(k, new Value(v));
                            alMembers.add(m);
                            System.out.println("*** " + m);
                            //System.out.println(">>>> " + k + "," + v);

                            //members.add(new Member(k, new Value(v)));
                            //map.put(items.substring(0, items.indexOf("=")), items.substring(items.indexOf("=")+1));
                        }
                    }
                } else
                    alMembers.add(member);
            }

            for (Member s : alMembers) System.out.println(s);

            Member[] newMembers = new Member[alMembers.size()];
            newMembers = alMembers.toArray(newMembers);

            MethodCall methodCall = new MethodCall();
            methodCall.setMethodName(irs.getMethodName());
            methodCall.setParams(new Params(new Param(new Values(new Struct(newMembers)))));

            String newMessage = methodCall.toString();
            System.out.println(">>" + newMessage);
            //String addInfo = getInfo(transTmp);
            String addInfo = XML.toString(getCallbackInfo(new HashMap<>(), transTmp));
            System.out.println(">>> " + addInfo);
            newMessage = new StringBuilder("<root>").append(newMessage).append(addInfo).append("</root>").toString();
            System.out.println(">>>>> newMessage: " + newMessage);

            Object[] result = transformService.transformApi(transTmp.getTransformId(), transTmp.getMethod(), newMessage, Jolt.JoltCallback);
            System.out.println(">>>>> transform Result:" + transTmp.getTransformId() + "," + transTmp.getMethod() + "," + result[0]);

            transHistory.setBmTid(transTmp.getBmTid());
            transHistory.setPartnerTid(transTmp.getPartnerTid());
            String partnerMsg = new ObjectMapper().writeValueAsString(processCallbackResponse(result[0], transTmp,transHistory));
            System.out.println(">>> partnerMsg:" + partnerMsg);
            updateTransaction(transHistory);

            updateBmLogCallback(transTmp.getPartnerTid(),transTmp.getBmTid(),partnerMsg,message);

            String partnerUrl = transTmp.getPartnerUrl();
            if (partnerUrl != null && !partnerUrl.equals("")) {
                try {
                    sendRequest(partnerUrl, partnerMsg);
                } catch (Exception e) { }
            }

            deleteTransTmp(transTmp);

            return HandlerConstant.SUCCESS;
        } catch(Exception e) {
            e.printStackTrace();

            return HandlerConstant.BACKEND_ERROR;
        }
    }

    //private String getInfo(TransTmp transTmp) throws Exception {
        /*Map<String,Object> map = new HashMap<>();

        System.out.println(">>>" + transTmp.getRequest());
        RequestVO partnerRequest = new ObjectMapper().readValue(transTmp.getRequest(), RequestVO.class);
        System.out.println(">>>" + partnerRequest);

        JSONObject json = new JSONObject(transTmp.getRequest());
        System.out.println(">>>" + json);

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

        //return XML.toString(jsonObject);*/
        /*return XML.toString(getCallbackInfo(new HashMap<>(), transTmp));
    }*/
}

@XmlRootElement(name = "methodResponse")
@XmlAccessorType(XmlAccessType.FIELD)
class MethodResponse
{
    private Params params;

    public MethodResponse() {}

    public MethodResponse(Params params) {
        this.params = params;
    }

    public Params getParams () {
        return params;
    }

    public void setParams (Params params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "<methodResponse><params>" + params + "</params></methodResponse>";
    }
}

@XmlRootElement(name = "methodCall")
@XmlAccessorType(XmlAccessType.FIELD)
class MethodCall
{
    private String methodName;
    private Params params;

    public MethodCall() {}

    public MethodCall(String methodName, Params params) {
        this.methodName = methodName;
        this.params = params;
    }

    public String getMethodName () {
        return methodName;
    }

    public Params getParams () {
        return params;
    }

    public void setMethodName (String methodName) {
        this.methodName = methodName;
    }

    public void setParams (Params params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "<methodCall><methodName>" + methodName + "</methodName><params>" + params + "</params></methodCall>";
    }
}

@XmlRootElement(name = "params")
@XmlAccessorType(XmlAccessType.FIELD)
class Params
{
    private Param param;

    public Params() {}

    public Params(Param param) {
        this.param = param;
    }

    public Param getParam () {
        return param;
    }

    public void setParam (Param param) {
        this.param = param;
    }

    @Override
    public String toString()
    {
        return "<param>" + param + "</param>";
    }
}

@XmlRootElement(name = "param")
@XmlAccessorType(XmlAccessType.FIELD)
class Param {
    private Values value;

    public Param() {}

    public Param(Values value) {
        this.value = value;
    }

    public Values getValues () {
        return value;
    }

    public void setValues (Values value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "<value>" + value + "</value>";
    }
}

@XmlRootElement(name = "value")
@XmlAccessorType(XmlAccessType.FIELD)
class Values {
    private Struct struct;

    public Values() {}

    public Values(Struct struct) {
        this.struct = struct;
    }

    public Struct getStruct () {
        return struct;
    }

    public void setStruct (Struct struct) {
        this.struct = struct;
    }

    @Override
    public String toString() {
        return "<struct>" + struct + "</struct>";
    }
}

@XmlRootElement(name = "struct")
@XmlAccessorType(XmlAccessType.FIELD)
class Struct {
    private Member[] member;

    public Struct() {}

    public Struct(Member[] member) {
        this.member = member;
    }

    public Member[] getMember () {
        return member;
    }

    public void setMember (Member[] member) {
        this.member = member;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Member value : member)
            sb.append("<member>").append(value).append("</member>");

        return sb.toString();
    }
}

@XmlRootElement(name = "member")
@XmlAccessorType(XmlAccessType.FIELD)
class Member {
    private String name;
    private Value value;

    public Member() {}

    public Member(String name, Value value) {
        this.name = name;
        this.value = value;
    }

    public String getName () {
        return name;
    }

    public Value getValue () {
        return value;
    }

    public void setName (String name) {
        this.name = name;
    }

    public void setValue (Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "<name>" + name + "</name><value>" + value + "</value>";
    }
}

@XmlRootElement(name = "value")
@XmlAccessorType(XmlAccessType.FIELD)
class Value {
    private String string;

    public Value() {}

    public Value(String string) {
        this.string = string;
    }

    public String getString () {
        return string;
    }

    public void setString (String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return "<string>" + string + "</string>";
    }
}
