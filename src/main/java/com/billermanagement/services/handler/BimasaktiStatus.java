package com.billermanagement.services.handler;

import com.billermanagement.persistance.domain.TransTmp;
import com.billermanagement.persistance.repository.BillerApiRepository;
import com.billermanagement.persistance.repository.PartnerApiRepository;
import com.billermanagement.persistance.repository.TransTmpRepository;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.backend.BimasaktiStatusVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class BimasaktiStatus {
    @Autowired
    TransTmpRepository transTmpRepository;

    @Autowired
    BillerApiRepository billerApiRepository;

    @Autowired
    PartnerApiRepository partnerApiRepository;

    @Autowired
    private Bimasakti bimasakti;

    @Autowired
    SLATime slaTimeService;

    Logger logger = LoggerFactory.getLogger(BimasaktiStatus.class);

    public void checkStatus() {
        List<TransTmp> bsRecords = transTmpRepository.findTrans(HandlerConstant.BS);
        for (TransTmp record : bsRecords) {
            long start = System.currentTimeMillis();
            String response = record.getResponse();

            ObjectMapper mapper = new ObjectMapper();
            try {
                Map<String,Object> map = mapper.readValue(response, new TypeReference<Map<String,Object>>() {});
                BimasaktiStatusVO vo = new BimasaktiStatusVO();
                vo.setMethod("fastpay.cekstatus");
                vo.setUid(InitDB.getInstance().get("bimasakti.uid"));
                vo.setPin(InitDB.getInstance().get("bimasakti.pin"));
                vo.setKode_produk(map.get("kodeproduk").toString());

                Object idpel1 = (map.containsKey("nohp")) ? map.get("nohp") : map.get("idpelanggan1");
                vo.setIdpel1(idpel1.toString());

                if (map.containsKey("idpelanggan2"))
                    vo.setIdpel2(map.get("idpelanggan2").toString());

                String waktu = map.get("waktu").toString();
                if (waktu.length() > 8) waktu = waktu.substring(0, 8);
                vo.setTgl(waktu);

                vo.setRef1(map.get("ref1").toString());
                vo.setRef2(map.get("ref2").toString());
                vo.setDenom(map.get("nominal").toString());

                String data = mapper.writeValueAsString(vo);
                System.out.println(data);

                boolean success = bimasakti.checkTrans(data, record);
                if (success) {
                    transTmpRepository.delete(record);
                }else{
                    if (record.getSlaTime() == null || record.getSlaTime().toString().isEmpty()){
                        this.logger.info("Start Put SLA Time to TransTmp fro Bimasakti");
                        int slaInSeconds=0;
                        if (InitDB.getInstance().get("bimasakti.Pending.SLA") !=null){
                            slaInSeconds = Integer.valueOf(InitDB.getInstance().get("bimasakti.Pending.SLA"));
                        }
                        Date slaTime = slaTimeService.getTransTmpSLATime(slaInSeconds,record.getId());
                        record.setSlaTime(slaTime);
                        transTmpRepository.save(record);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("checkStatus.transactionID:" + record.getBmTid() + "," + (System.currentTimeMillis()-start) + "ms");
        }
    }

    public void checkStatus(String transId, int handler) throws Exception {
        long start = System.currentTimeMillis();

        TransTmp record = transTmpRepository.findTrans(transId, handler);
        boolean success = false;
        try {
            String response = record.getResponse();

            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> map = mapper.readValue(response, new TypeReference<Map<String,Object>>() {});
            BimasaktiStatusVO vo = new BimasaktiStatusVO();
            vo.setMethod("fastpay.cekstatus");
            vo.setUid(InitDB.getInstance().get("bimasakti.uid"));
            vo.setPin(InitDB.getInstance().get("bimasakti.pin"));
            vo.setKode_produk(map.get("kodeproduk").toString());

            Object idpel1 = (map.containsKey("nohp")) ? map.get("nohp") : map.get("idpelanggan1");
            vo.setIdpel1(idpel1.toString());

            if (map.containsKey("idpelanggan2"))
                vo.setIdpel2(map.get("idpelanggan2").toString());

            String waktu = map.get("waktu").toString();
            if (waktu.length() > 8) waktu = waktu.substring(0, 8);
            vo.setTgl(waktu);

            vo.setRef1(map.get("ref1").toString());
            vo.setRef2(map.get("ref2").toString());
            vo.setDenom(map.get("nominal").toString());

            String data = mapper.writeValueAsString(vo);
            System.out.println(data);

            success = bimasakti.checkTrans(data, record);
            if (success) {
                transTmpRepository.delete(record);
            }
        } finally {
            logger.info("checkStatus:" + transId + "," + handler + "," + success + "," + (System.currentTimeMillis()-start) + "ms");
        }
    }
}
