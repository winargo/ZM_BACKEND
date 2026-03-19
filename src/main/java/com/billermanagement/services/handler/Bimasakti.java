package com.billermanagement.services.handler;

import com.billermanagement.enums.Jolt;
import com.billermanagement.persistance.domain.TransHistory;
import com.billermanagement.persistance.domain.TransTmp;
import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.vo.backend.RequestVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Qualifier("bimasakti")
public class Bimasakti extends BillerRequest {

    public Object process(RequestVO vo, BillerResult billerResult) throws Exception {
        try {
            String[] request = getRequest(vo, billerResult, "bimasakti");

            //String msgResponse = sendRequest(request[1], request[0]);
            String msgResponse = sendRequest(request[1], request[0], getTimeout("bimasakti.http.connect.timeout"), getTimeout("bimasakti.http.read.timeout"));

            Object obj = getResponse(vo, billerResult, msgResponse);

            Object[] result;
            if (msgResponse.toUpperCase().contains("SEDANG DIPROSES"))  {
                result = transformService.transformApi(billerResult.getTransformId(), vo.getMethod(), obj, Jolt.JoltResponse);

                obj = cleanUpResponse(result[0],vo, billerResult);

                savePendingTransaction(vo, billerResult, request[1], msgResponse, HandlerConstant.BS);

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
    public boolean checkTrans(String message, TransTmp record) throws Exception {
        System.out.println(">>>" + record.getRequest());

        TransHistory transHistory = new TransHistory();

        String msgResponse;
        try {
            msgResponse = sendRequest(record.getUrl(), message);
        } catch (Exception e) {
            return false;
        }

        System.out.println(">>>> msgResponse: " + msgResponse);

        if (msgResponse.toUpperCase().contains("SEDANG DIPROSES"))  {
            return false;
        } else {
            //{"tanggal":"20121214","ref1":"20101003416520201003195137261","ref2":"49921760","kodeproduk":"FNWOM","idpel1":"201010034165","idpel2":"","denom":"1005500","uid":"HH124952","pin":"------","status":"99","keterangan":"Transaksi dengan kriteria yang dimaksud tidak ditemukan"
            //,"result":{"idtransaksi":"","transaksidatetime":"","kodeproduk":"","idpelanggan1":"","idpelanggan2":"","nominal":"","nominaladmin":"","sn":""}

            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> map = mapper.readValue(msgResponse, new TypeReference<Map<String,Object>>() {});

            CheckStatusVO ck = mapper.convertValue(map.remove("result"), CheckStatusVO.class);

            map.put("idtransaksi", ck.getIdtransaksi());
            map.put("transaksidatetime", ck.getTransaksidatetime());
            map.put("kodeproduk", ck.getKodeproduk());
            map.put("idpelanggan1", ck.getIdpelanggan1());
            map.put("idpelanggan2", ck.getIdpelanggan2());
            map.put("nominal", ck.getNominal());
            map.put("nominaladmin", ck.getNominaladmin());
            map.put("sn", ck.getSn());

            System.out.println(">>" + mapper.writeValueAsString(map));

            /*RequestVO vo = mapper.readValue(record.getRequest(), RequestVO.class);
            map.put("partner_request", vo);

            AdditionalInfoVO addInfoVO = new AdditionalInfoVO();
            addInfoVO.setTransactionId(record.getBmTid());
            addInfoVO.setPartnerPrice(record.getPartnerPrice());
            addInfoVO.setBillerPrice(record.getBillerPrice());
            addInfoVO.setAdminFee(record.getPartnerFee());
            addInfoVO.setTime(FormatUtil.getTime("yyyyMMddHHmmssSSS"));

            map.put("additional_info", addInfoVO);

            Object obj = mapper.convertValue(map, Object.class);*/

            Object obj = getCallbackInfo(map, record);

            System.out.println(">>> toTransform:" + record.getTransformId() + "," + record.getMethod() + "," + obj);

            Object[] result = transformService.transformApi(record.getTransformId(), record.getMethod(), obj, Jolt.JoltCallback);

            System.out.println(">>> result-1:" + result[0]);
            System.out.println(">>> result-1:" + result[1]);
            System.out.println(">>> result-1:" + result[2]);

            /*
            mapper = new ObjectMapper();
            String data = mapper.writeValueAsString(result[0]);
            map = mapper.readValue(data, new TypeReference<Map<String,Object>>() {});

            String bmCode = null;
            String billerCode = null;
            String status = null;
            int billerId = record.getBillerId();
            if (map.containsKey("Status")) {
                billerCode = map.get("Status").toString();
                BMCode bmResultCode = statusMapping.get(billerId, billerCode);
                if (bmResultCode != null) {
                    bmCode = bmResultCode.getCode();

                    if (bmCode.equals("0")) status = OK;
                    //else if (bmCode.equals("99")) return false;
                    else status = FAILED;

                    map.put("Status", bmCode);
                    map.put("Desc", bmResultCode.getDescription());
                } else {
                    if (billerCode.equals("0")) status = OK;
                    //else if (billerCode.equals("99")) return false;
                    else status = FAILED;
                }
            }

            map.remove("AggregatorTID");

            String billerRespPrice = null;
            String billerRespFee = null;
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
            */

            transHistory.setBmTid(record.getBmTid());
            transHistory.setPartnerTid(record.getPartnerTid());
            String partnerMsg = mapper.writeValueAsString(processCallbackResponse(result[0], record,transHistory));
            System.out.println(">>> partnerMsg:" + partnerMsg);

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

            //update TransHistory
            //updateTransaction(Integer.parseInt(billerRespPrice), Integer.parseInt(billerRespFee), billerCode, bmCode,
            //        status, record.getBmTid());

            return true;
        }
    }

    @Data
    private static class CheckStatusVO {
        @JsonProperty("IDTRANSAKSI")
        private String idtransaksi;
        @JsonProperty("TRANSAKSIDATETIME")
        private String transaksidatetime;
        @JsonProperty("KODEPRODUK")
        private String kodeproduk;
        @JsonProperty("IDPELANGGAN1")
        private String idpelanggan1;
        @JsonProperty("IDPELANGGAN2")
        private String idpelanggan2;
        @JsonProperty("NOMINAL")
        private String nominal;
        @JsonProperty("NOMINALADMIN")
        private String nominaladmin;
        @JsonProperty("SN")
        private String sn;
    }
}
