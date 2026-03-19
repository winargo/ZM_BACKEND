package com.billermanagement.services.ui;

import com.billermanagement.controller.InitConfigController;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.DataNotFoundException;
import com.billermanagement.persistance.domain.Api;
import com.billermanagement.persistance.domain.Biller;
import com.billermanagement.persistance.domain.BillerList;
import com.billermanagement.persistance.domain.Partner;
import com.billermanagement.persistance.repository.ApiRepository;
import com.billermanagement.persistance.repository.BillerListRepository;
import com.billermanagement.persistance.repository.BillerRepository;
import com.billermanagement.persistance.repository.PartnerRepository;
import com.billermanagement.services.handler.ConfigUpdater;
import com.billermanagement.vo.ui.BillerListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BillerListService {
    @Autowired
    BillerListRepository repository;

    @Autowired
    PartnerRepository partnerRepository;

    @Autowired
    BillerRepository billerRepository;

    @Autowired
    ApiRepository apiRepository;

    @Autowired
    ConfigUpdater configUpdater;

    @Autowired
    InitConfigController configController;

    @Transactional
    public BillerList create(BillerListVO vo) {
        return null;

        /*Optional<Partner> optionalPartner = partnerRepository.findById(vo.getPartnerId());
        if (optionalPartner.isPresent()) {
            Optional<Api> optionalApi = apiRepository.findById(vo.getApiId());
            if (optionalApi.isPresent()) {
                Optional<Biller> optionalBiller = billerRepository.findById(vo.getBillerId());
                if (optionalBiller.isPresent()) {
                    BillerList billerList = new BillerList(vo);
                    //billerList.setPartner(optionalPartner.get());
                    //billerList.setApi(optionalApi.get());
                    billerList.setBillerId(optionalBiller.get());

                    return repository.save(billerList);
                }
                throw new DataNotFoundException(ResultMessage.BILLER_ID, StatusCode.DATA_NOT_FOUND);
            }
            throw new DataNotFoundException(ResultMessage.API_ID, StatusCode.DATA_NOT_FOUND);
        }
        throw new DataNotFoundException(ResultMessage.PARTNER_ID, StatusCode.DATA_NOT_FOUND);*/
    }

    @Transactional
    public BillerList update(BillerListVO vo){
        return null;

        /*String id = vo.getId();
        Optional<BillerList> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            Optional<Partner> optionalPartner = partnerRepository.findById(vo.getPartnerId());
            if (optionalPartner.isPresent()) {
                Optional<Api> optionalApi = apiRepository.findById(vo.getApiId());
                if (optionalApi.isPresent()) {
                    Optional<Biller> optionalBiller = billerRepository.findById(vo.getBillerId());
                    if (optionalBiller.isPresent()) {
                        BillerList billerList = optional.get();
                        //billerList.setPartner(optionalPartner.get());
                        //billerList.setApi(optionalApi.get());
                        billerList.setBillerId(optionalBiller.get());

                        return repository.save(billerList);
                    }
                    throw new DataNotFoundException(ResultMessage.BILLER_ID, StatusCode.DATA_NOT_FOUND);
                }
                throw new DataNotFoundException(ResultMessage.API_ID, StatusCode.DATA_NOT_FOUND);
            }
            throw new DataNotFoundException(ResultMessage.PARTNER_ID, StatusCode.DATA_NOT_FOUND);
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);*/
    }

    @Transactional
    public String delete(BillerListVO vo){
        String id = vo.getId();
        Optional<BillerList> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            repository.delete(optional.get());

            refreshConfig();

            return ResultMessage.SUCCESS_DEL;
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public List<BillerList> findAll(){
        return repository.findAll();
    }

    public BillerList findById(int id) {
        Optional<BillerList> optional = repository.findById(id);
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
