package com.billermanagement.services;

import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.persistance.repository.*;
import com.billermanagement.services.handler.BNI;
import com.billermanagement.services.handler.*;
import com.billermanagement.vo.backend.RequestVO;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SwitchingService {
    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PartnerApiRepository partnerApiRepository;

    @Autowired
    BillerRepository billerRepository; //to be changed to memory

    @Autowired
    private BillerApiRepository billerApiRepository;

    @Autowired
    @Qualifier("indogold")
    private BillerRequest indogold;

    @Autowired
    @Qualifier("bimasakti")
    private BillerRequest bimasakti;

    @Autowired
    @Qualifier("irs")
    private BillerRequest irs;

    @Autowired
    @Qualifier("bri")
    private BillerRequest bri;

    @Autowired
    @Qualifier("otherBiller")
    private BillerRequest otherBiller;

    @Autowired
    @Qualifier("instamoney")
    private Instamoney instamoney;

    @Autowired
    @Qualifier("cimb")
    private BillerRequest cimb;

    @Autowired
    @Qualifier("bni")
    private BNI bni;
    
    @Autowired
    @Qualifier("idn")
    private IDN idn;
    
    @Autowired
    @Qualifier("btn")
    private BTN btn;
    
    @Autowired
    @Qualifier("mobilepulsa")
    private MobilePulsa mobilepulsa;
    
    @Autowired
    @Qualifier("xfers")
    private Xfers xfers;

    //@Autowired
    //@Qualifier("abc")
    //private ABC abc;

    @Autowired
    @Qualifier("dsp")
    private BillerRequest dsp;

    @Autowired
    @Qualifier("britransfer")
    private BillerRequest britransfer;

    @Autowired
    InquiryRequest inquiryRequest;

    @Autowired
    PartnerInfoConfig partnerInfo;

    @Autowired
    PartnerConfig partnerConfig;

    @Autowired
    BillerConfig billerConfig;

    @Autowired
    BillerApiConfig billerApiConfig;

    Logger logger = LoggerFactory.getLogger(SwitchingService.class);

    public Object processRequest(RequestVO vo) throws Exception {
        logger.info("processRequest: " + vo);

        String productCode = vo.getProductCode();
        if (productCode.equalsIgnoreCase("PC")) return inquiryRequest.inquiryProductCategory(vo);
        if (productCode.equalsIgnoreCase("PL")) return inquiryRequest.inquiryProductList(vo);
        if (productCode.equalsIgnoreCase("Stat")) return inquiryRequest.inquiryStatus(vo);

        long start = System.currentTimeMillis();
        String alias = vo.getPartnerId();

        /*int partnerId = partnerRepository.findIdByAlias(alias);
        System.out.println(">>> partnerId:" + partnerId + "," + (System.currentTimeMillis()-start));
        start = System.currentTimeMillis();
        System.out.println(">>> partnerId:" + partnerConfig.get(alias) + "," + (System.currentTimeMillis()-start));*/

        int partnerId = partnerConfig.get(alias);
        if (partnerId == -1) throw new Exception("PRC:Unknown Partner");

        /*start = System.currentTimeMillis();
        PartnerSelectionResult partnerSelection = partnerApiRepository.findPartnerIdAndSelection(partnerId, productCode);
        String selection = partnerSelection.getApiSelection();
        System.out.println(">>> selection:" + partnerId + "," + selection + "," + (System.currentTimeMillis()-start));
        start = System.currentTimeMillis();
        System.out.println(">>> selection:" + partnerId + "," + billerApiConfig.get(partnerId, productCode) + "," + (System.currentTimeMillis()-start));*/

        String selection = billerApiConfig.get(partnerId, productCode);

        //logger.info(">>> findPartnerIdAndSelection:" + apiId + "," + selection);
        System.out.println(">>> " + alias + "," + productCode + "," + partnerId + "," + selection + "," + (System.currentTimeMillis()-start) + "ms");
        if (selection == null) throw new Exception("PRC:Product not registered");

        List<BillerResult> billerResults = null;
        /*start = System.currentTimeMillis();
        if (selection.toUpperCase().equals("PRICE")) {
            billerResults = billerApiRepository.findByPrice(partnerId, productCode);
        } else if (selection.toUpperCase().equals("PRIORITY")) {
            billerResults = billerApiRepository.findByPriority(partnerId, productCode);
        }
        System.out.println(">>> billerResults:" + billerResults + "," + (System.currentTimeMillis()-start));
        for (BillerResult billerResult : billerResults) {
            System.out.println(">>> billerResults:" + billerResult.getBillerId() + "," + billerResult.getBillerCode());
        }*/

        start = System.currentTimeMillis();
        if (selection.toUpperCase().equals("PRICE")) {
            billerResults = billerApiConfig.getByPrice(partnerId, productCode);
        } else if (selection.toUpperCase().equals("PRIORITY")) {
            billerResults = billerApiConfig.getByPriority(partnerId, productCode);
        }
        System.out.println(">>> billerResults:" + billerResults + "," + (System.currentTimeMillis()-start) + "ms");
        /*for (BillerResult billerResult : billerResults) {
            System.out.println(">>> billerResults:" + billerResult.getBillerId() + "," + billerResult.getBillerCode());
        }*/
        if (billerResults==null || billerResults.size() == 0) throw new Exception("PRC:Biller Inactive");

        //logger.info("billerApis:" + billerResults);
        int i = 0;
        boolean success = false;
        Object objRes = null;
        String errDesc = null;
        //logger.info("success:" + success + ",!success:" + !success);
        while(!success && i<billerResults.size()) {
            start = System.currentTimeMillis();
            //logger.info("Looping:" + i + ", size:" + billerResults.size());
            BillerResult billerApi = billerResults.get(i);
            try {
                objRes = sending(vo, billerApi);
                if (objRes != null) success = true;
            } catch (java.net.SocketTimeoutException e) {
                System.err.println(e.getMessage());
                if (e.getMessage().equals("Read timed out")) {
                    errDesc = "RTO";
                    success = true;
                }
            } catch (Exception e) {
                System.err.println(e);
                // do nothing, go to next biller
            }
            logger.info("processRequest.sending:" + vo + "," + success + "," + (System.currentTimeMillis()-start) + "ms");
            i++;
        }

        if (errDesc != null) throw new Exception("Read timed out");
        if (objRes == null) throw new Exception("Internal Server Error");

        return objRes;
    }

    private Object sending(RequestVO vo, BillerResult billerResult) throws Exception {
        /*String billerAlias = billerRepository.findBillerAlias(billerResult.getBillerId());
        System.out.println("billerAlias: " + billerAlias + "," + (System.currentTimeMillis()-start));
        start = System.currentTimeMillis();
        System.out.println("billerAlias: " + billerConfig.get(billerResult.getBillerId()) + "," + (System.currentTimeMillis()-start));*/

        long start = System.currentTimeMillis();
        String billerAlias = billerConfig.get(billerResult.getBillerId()).toUpperCase();
        System.out.println(">>> billerAlias: " + billerAlias + "," + (System.currentTimeMillis()-start) + "ms");

        Object ret;
        switch (billerAlias) {
            case "INDOGOLD":
                ret = indogold.process(vo, billerResult);
                break;
            case "BIMASAKTI":
                ret = bimasakti.process(vo, billerResult);
                break;
            case "IRS":
                ret = irs.process(vo, billerResult);
                break;
            case "INSTAMONEY":
                ret = instamoney.process(vo, billerResult);
                break;
            case "BNI":
                ret = bni.process(vo, billerResult);
                break;
            case "IDN":
                ret = idn.process(vo, billerResult);
                break;    
            case "BTN":
                ret = btn.process(vo, billerResult);
                break;
            case "MOBILEPULSA":
                ret = mobilepulsa.process(vo, billerResult);
                break;    
            case "XFERS":
                ret = xfers.process(vo, billerResult);
                break;
            case "BRI":
                ret = bri.process(vo, billerResult);
                break;
            case "CIMB":
                ret = cimb.process(vo, billerResult);
                break;
            //case "ABC":
            //    ret = abc.process(vo, billerResult);
            //    break;
            case "DSP":
                ret = dsp.process(vo, billerResult);
                break;
            case "BRITRANSFER":
                ret = britransfer.process(vo, billerResult);
                break;
            default:
                ret = otherBiller.process(vo, billerResult);
                break;
        }

        return ret;
    }
}
