package com.billermanagement.services.handler;

import com.billermanagement.persistance.domain.resultset.PartnerProduct;
import com.billermanagement.persistance.domain.resultset.TransStatus;
import com.billermanagement.persistance.repository.ApiRepository;
import com.billermanagement.persistance.repository.TransHistoryRepository;
import com.billermanagement.util.FormatUtil;
import com.billermanagement.vo.backend.ParamsVO;
import com.billermanagement.vo.backend.RequestVO;
import com.billermanagement.vo.backend.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InquiryRequest {
    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private TransHistoryRepository transHistoryRepository;

    public Object inquiryProductCategory(RequestVO vo) {
        String partner = vo.getPartnerId();
        List<String> res = apiRepository.findPartnerCategory(partner);

        List<ParamsVO> paramsVO = new ArrayList<>();
        int i = 0;
        for (String s : res) {
            ParamsVO param = new ParamsVO();

            String count = (++i < 10) ? "0" + i : Integer.toString(i);
            String name = new StringBuilder("cat.").append(count).append(".name").toString();

            param.setName(name);
            param.setValue(s);

            paramsVO.add(param);
        }

        String datetime = FormatUtil.getTime("yyyyMMddHHmmssSSS");
        String transId = new StringBuilder(vo.getRequestId()).append(datetime).toString();

        ResponseVO resVO = new ResponseVO();
        resVO.setMethod(vo.getMethod());
        resVO.setProductCode(vo.getProductCode());
        resVO.setRequestId(vo.getRequestId());
        resVO.setTransactionId(transId);
        resVO.setTime(datetime);
        resVO.setStatus("0");
        resVO.setDesc("Success");
        resVO.setFlowType("Sync");
        resVO.setParams(paramsVO);

        return resVO;
    }

    public Object inquiryProductList(RequestVO vo) {
        String partner = vo.getPartnerId();
        String category = vo.getReffId();

        List<PartnerProduct> res = apiRepository.findPartnerProduct(partner, category);

        List<ParamsVO> paramsVO = new ArrayList<>();
        int i = 0;
        for (PartnerProduct s : res) {
            ParamsVO param = new ParamsVO();

            if (category == null) param.setCategory(s.getCategory());
            param.setCode(s.getProductCode());
            param.setName(s.getProductName());
            param.setDesc(s.getProductDesc());
            param.setNominal(s.getNominal());
            param.setPrice(s.getPartnerPrice());
            param.setFee(s.getPartnerFee());

            paramsVO.add(param);
        }

        String datetime = FormatUtil.getTime("yyyyMMddHHmmssSSS");
        String transId = new StringBuilder(vo.getRequestId()).append(datetime).toString();

        ResponseVO resVO = new ResponseVO();
        resVO.setMethod(vo.getMethod());
        resVO.setProductCode(vo.getProductCode());
        resVO.setRequestId(vo.getRequestId());
        resVO.setTransactionId(transId);
        resVO.setTime(datetime);
        resVO.setStatus("0");
        resVO.setDesc("Success");
        resVO.setFlowType("Sync");
        resVO.setReffId(vo.getReffId());

        resVO.setParams(paramsVO);

        return resVO;
    }

    public Object inquiryStatus(RequestVO vo) {
        String requestId = null;
        String reqTransId = null;
        String name = vo.getParams().get(0).getName();
        String value = vo.getParams().get(0).getValue();

        if (name.equalsIgnoreCase("RequestId")) requestId = value;
        else reqTransId = value;

        Optional<TransStatus> optional = transHistoryRepository.findTransaction(requestId, reqTransId);

        List<ParamsVO> paramsVO = null;
        if (optional.isPresent()) {
            paramsVO = new ArrayList<>();
            TransStatus status = optional.get();

            ParamsVO param = new ParamsVO();
            param.setName("RequestId");
            param.setValue(status.getRequestId());
            paramsVO.add(param);

            param = new ParamsVO();
            param.setName("TransactionId");
            param.setValue(status.getTransId());
            paramsVO.add(param);

            param = new ParamsVO();
            param.setName("Status");
            param.setValue(status.getStatus());
            paramsVO.add(param);

            /*param = new ParamsVO();
            param.setName("StatusDesc");
            param.setValue(status.getDesc());
            paramsVO.add(param);*/

            param = new ParamsVO();
            param.setName("StatusTime");
            param.setValue(status.getDatetime());
            paramsVO.add(param);
        }

        String datetime = FormatUtil.getTime("yyyyMMddHHmmssSSS");
        String transId = new StringBuilder(vo.getRequestId()).append(datetime).toString();

        ResponseVO resVO = new ResponseVO();
        resVO.setMethod(vo.getMethod());
        resVO.setProductCode(vo.getProductCode());
        resVO.setRequestId(vo.getRequestId());
        resVO.setTransactionId(transId);
        resVO.setTime(datetime);
        resVO.setStatus("0");
        resVO.setDesc("Success");
        resVO.setFlowType("Sync");
        resVO.setParams(paramsVO);

        return resVO;
    }
}
