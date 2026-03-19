package com.billermanagement.services.EbayService;

import com.billermanagement.util.InitDB;
import com.billermanagement.vo.EbayVO.CheckTransaction.EbayCheckTransactionByRefIDReqVO;
import com.billermanagement.vo.EbayVO.CheckTransaction.EbayCheckTransactionReqVO;
import com.billermanagement.vo.EbayVO.CheckTransaction.EbayCheckTransactionResVO;
import com.billermanagement.vo.EbayVO.GetRecepientName.EbayGetRecepientNameReqVO;
import com.billermanagement.vo.EbayVO.GetRecepientName.EbayGetRecepientNameResVO;
import com.billermanagement.vo.EbayVO.Transfer.EbayTransferReqVO;
import com.billermanagement.vo.EbayVO.Transfer.EbayTransferResVO;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EbayService {

    @Autowired
    EbayHTTPService ebayHTTPService;

    private Gson gson;

    public EbayGetRecepientNameResVO getRecepientName(EbayGetRecepientNameReqVO vo){
        InitDB initDB = InitDB.getInstance();
        String url = initDB.get("ebay.url.GetRecepientName");

        String responseBody = ebayHTTPService.callHttpPost(url, vo);
        EbayGetRecepientNameResVO result =gson.fromJson(responseBody, EbayGetRecepientNameResVO.class);

        return result;
    }

    public EbayTransferResVO transfer(EbayTransferReqVO vo){
        InitDB initDB = InitDB.getInstance();
        String url = initDB.get("ebay.url.Transfer");

        String responseBody = ebayHTTPService.callHttpPost(url, vo);
        EbayTransferResVO result =gson.fromJson(responseBody, EbayTransferResVO.class);

        return result;
    }

    public EbayCheckTransactionResVO checkTransaction(EbayCheckTransactionReqVO vo){
        InitDB initDB = InitDB.getInstance();
        String url = initDB.get("ebay.url.CheckTransaction");

        String responseBody = ebayHTTPService.callHttpPost(url, vo);
        EbayCheckTransactionResVO result =gson.fromJson(responseBody, EbayCheckTransactionResVO.class);

        return result;
    }

    public EbayCheckTransactionResVO checkTransactionByRefId(EbayCheckTransactionByRefIDReqVO vo){
        InitDB initDB = InitDB.getInstance();
        String url = initDB.get("ebay.url.CheckTransactionByRefId");

        String responseBody = ebayHTTPService.callHttpPost(url, vo);
        EbayCheckTransactionResVO result =gson.fromJson(responseBody, EbayCheckTransactionResVO.class);

        return result;
    }
}
