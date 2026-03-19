package com.billermanagement.services.ui;

import com.billermanagement.controller.InitConfigController;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.DataNotFoundException;
import com.billermanagement.persistance.domain.Biller;
import com.billermanagement.persistance.repository.BillerRepository;
import com.billermanagement.services.AuditTrailService;
import com.billermanagement.services.handler.ConfigUpdater;
import com.billermanagement.vo.ui.BillerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BillerService {
    @Autowired
    BillerRepository repository;

    @Autowired
    AuditTrailService trail;

    @Autowired
    ConfigUpdater configUpdater;

    @Autowired
    InitConfigController configController;

    @Transactional
    public Biller create(BillerVO vo) {
        Biller biller = new Biller(vo);
        Biller res = repository.save(biller);

        refreshConfig();

        return res;
    }

    @Transactional
    public Biller update(BillerVO vo){
        String id = vo.getId();
        Optional<Biller> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            Biller biller = optional.get();
            trail.biller(Integer.parseInt(id),biller,vo,vo.getUsername());
            biller.update(vo);
            Biller res = repository.save(biller);

            refreshConfig();

            return res;
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    @Transactional
    public String delete(BillerVO vo){
        String id = vo.getId();
        Optional<Biller> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            repository.delete(optional.get());

            refreshConfig();

            return ResultMessage.SUCCESS_DEL;
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public List<Biller> findAll(){
        return repository.findAll();
    }

    public Biller findById(int id) {
        Optional<Biller> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    private void refreshConfig() {
        configUpdater.updateAllInstance();
        configController.setInstanceConfig();
    }
}
