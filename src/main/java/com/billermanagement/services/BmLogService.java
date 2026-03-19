/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.services;

import com.billermanagement.persistance.domain.BmLog;
import com.billermanagement.persistance.repository.BmLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 *
 * @author sulaeman
 */
@Service
public class BmLogService {
    
    @Autowired
    private BmLogRepository repo;

    Logger log = LoggerFactory.getLogger(BmLogService.class);
    
    public List<BmLog> getAll() {
        return repo.findAll();
    }

    public void save(BmLog bmLog) {
        repo.save(bmLog);
    }

    public BmLog findByPartnerTid(String partnerTid){
        return repo.findLogByPartnerTid(partnerTid);
    }

    public BmLog findByBmTid(String bmTid){
        return repo.findLogByBmTid(bmTid);
    }

    public BmLog findByPartnerTidAndBMTid(String partnerTid,String bmTid){
        return repo.findLogByPartnerTidAndBmTid(partnerTid,bmTid);
    }
}
