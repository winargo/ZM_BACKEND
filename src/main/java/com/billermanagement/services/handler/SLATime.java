package com.billermanagement.services.handler;

import com.billermanagement.persistance.domain.Api;
import com.billermanagement.persistance.repository.ApiRepository;
import com.billermanagement.persistance.repository.TransTmpRepository;
import com.billermanagement.util.InitDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SLATime {

    @Autowired
    TransTmpRepository tmpRepository;

    private final InitDB initDB = InitDB.getInstance();

    Logger logger = LoggerFactory.getLogger(SLATime.class);

    public Date getTransTmpSLATime(int seconds, int transTmpId){
        this.logger.info("getTransTmpSLATime(seconds: "+seconds+",transTmpId:"+transTmpId+")");

        int paramInSeconds=seconds;

        if (paramInSeconds == 0 && initDB.get("BM.Pending.SLA") != null){
            paramInSeconds = Integer.valueOf(initDB.get("BM.Pending.SLA"));
        }

        return tmpRepository.getSLATime(paramInSeconds,transTmpId);
    }

}
