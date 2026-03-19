package com.billermanagement.services.ui;

import com.billermanagement.controller.InitConfigController;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.DataNotFoundException;
import com.billermanagement.persistance.domain.Partner;
import com.billermanagement.persistance.repository.PartnerRepository;
import com.billermanagement.services.AuditTrailService;
import com.billermanagement.services.handler.ConfigUpdater;
import com.billermanagement.vo.ui.PartnerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PartnerService {
    @Autowired
    PartnerRepository repository;

    @Autowired
    AuditTrailService trail;

    @Autowired
    ConfigUpdater configUpdater;

    @Autowired
    InitConfigController configController;

    @Transactional
    public Partner create(PartnerVO vo) {
        Partner partner = new Partner(vo);
        Partner res = repository.save(partner);

        refreshConfig();

        return res;
    }

    @Transactional
    public Partner update(PartnerVO vo){
        String id = vo.getId();
        Optional<Partner> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            Partner partner = optional.get();
            trail.partner(Integer.valueOf(id), partner, vo, vo.getUsername());
            partner.update(vo);
            Partner res = repository.save(partner);

            refreshConfig();

            return res;
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    @Transactional
    public String delete(PartnerVO vo){
        String id = vo.getId();
        Optional<Partner> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            repository.delete(optional.get());

            refreshConfig();

            return ResultMessage.SUCCESS_DEL;
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public List<Partner> findAll(){
        return repository.findAll();
    }

    public Partner findById(int id) {
        Optional<Partner> optionalPartner = repository.findById(id);
        if (optionalPartner.isPresent()) {
            return optionalPartner.get();
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public Partner toggleStatus(int id) {
        Optional<Partner> optionalPartner = repository.findById(id);
        if (optionalPartner.isPresent()) {
            Partner partner = optionalPartner.get();
            partner.setStatus(!partner.isStatus());
            repository.save(partner);

            refreshConfig();

            return partner;
        }
        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    private void refreshConfig() {
        configUpdater.updateAllInstance();
        configController.setInstanceConfig();
    }
}
