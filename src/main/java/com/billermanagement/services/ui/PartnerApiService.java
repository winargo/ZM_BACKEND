package com.billermanagement.services.ui;

import com.billermanagement.controller.InitConfigController;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.DataNotFoundException;
import com.billermanagement.persistance.domain.*;
import com.billermanagement.persistance.repository.*;
import com.billermanagement.services.handler.ConfigUpdater;
import com.billermanagement.vo.ui.PartnerApiVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PartnerApiService {
    @Autowired
    PartnerApiRepository repository;

    @Autowired
    PartnerRepository partnerRepository;

    @Autowired
    BillerRepository billerRepository;

    @Autowired
    BillerListRepository billerListRepository;

    @Autowired
    ApiRepository apiRepository;

    @Autowired
    BillerApiRepository billerApiRepository;

    @Autowired
    ConfigUpdater configUpdater;

    @Autowired
    InitConfigController configController;

    @Transactional
    public PartnerApi createPartnerApi(PartnerApiVO vo) {
        int partnerId = vo.getPartnerId();
        Optional<Partner> optional = partnerRepository.findById(partnerId);
        if (optional.isPresent()) {
            Optional<Api> optionalApi = apiRepository.findById(vo.getApiId());
            if (optionalApi.isPresent()) {
                PartnerApi partnerApi = new PartnerApi(vo);
                partnerApi.setPartner(optional.get());
                partnerApi.setApi(optionalApi.get());

                PartnerApi res = repository.save(partnerApi);

                refreshConfig();

                return res;
            }
            throw new DataNotFoundException(ResultMessage.API_ID, StatusCode.DATA_NOT_FOUND);
        }

        throw new DataNotFoundException(ResultMessage.PARTNER_ID, StatusCode.DATA_NOT_FOUND);
    }

    @Transactional
    public PartnerApi createPartnerApi2(PartnerApiVO vo) {
        Optional<Partner> optional = partnerRepository.findById(vo.getPartnerId());
        if (optional.isPresent()) {
            Optional<Api> optionalApi = apiRepository.findById(vo.getApiId());
            if (optionalApi.isPresent()) {
                PartnerApi partnerApi = new PartnerApi(vo);
                partnerApi.setPartner(optional.get());
                partnerApi.setApi(optionalApi.get());
                partnerApi = repository.save(partnerApi);

                List<Integer> billerIds = vo.getBillerApiId();
                List<BillerList> billerList = new ArrayList<>();
                for (int billerId : billerIds) {
                    Optional<Biller> optionalBiller = billerRepository.findById(billerId);
                    if (optionalBiller.isPresent()) {
                        BillerList biller = new BillerList();
                        biller.setPartnerApiId(partnerApi.getId());
                        //biller.setBillerId(optionalBiller.get());

                        billerList.add(billerListRepository.save(biller));
                    }
                }
                partnerApi.setBillerApiId(billerList);
                refreshConfig();
                //return repository.save(partnerApi);
                return partnerApi;
            }
            throw new DataNotFoundException(ResultMessage.API_ID, StatusCode.DATA_NOT_FOUND);
        }

        throw new DataNotFoundException(ResultMessage.PARTNER_ID, StatusCode.DATA_NOT_FOUND);
    }

    @Transactional
    public PartnerApi create(PartnerApiVO vo) {
        Optional<Partner> optional = partnerRepository.findById(vo.getPartnerId());
        if (optional.isPresent()) {
            Optional<Api> optionalApi = apiRepository.findById(vo.getApiId());
            if (optionalApi.isPresent()) {
                PartnerApi partnerApi = new PartnerApi(vo);
                partnerApi.setPartner(optional.get());
                partnerApi.setApi(optionalApi.get());
                partnerApi = repository.save(partnerApi);

                List<Integer> billerIds = vo.getBillerApiId();
                List<BillerList> billerList = new ArrayList<>();
                int priority = 0;
                for (int billerId : billerIds) {
                    Optional<BillerApi> optionalBillerApi = billerApiRepository.findByBillerAndApiId(billerId, vo.getApiId());
                    if (optionalBillerApi.isPresent()) {
                        BillerList biller = new BillerList();
                        biller.setPartnerApiId(partnerApi.getId());
                        biller.setBillerApiId(optionalBillerApi.get());
                        biller.setBillerId(billerId);
                        biller.setPriority(++priority);

                        billerList.add(billerListRepository.save(biller));
                    }
                }
                partnerApi.setBillerApiId(billerList);
                refreshConfig();
                //return repository.save(partnerApi);
                return partnerApi;
            }
            throw new DataNotFoundException(ResultMessage.API_ID, StatusCode.DATA_NOT_FOUND);
        }

        throw new DataNotFoundException(ResultMessage.PARTNER_ID, StatusCode.DATA_NOT_FOUND);
    }

    @Transactional
    public PartnerApi update(PartnerApiVO vo){
        String id = vo.getId();
        Optional<PartnerApi> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            Optional<Api> optionalApi = apiRepository.findById(vo.getApiId());
            if (optionalApi.isPresent()) {
                PartnerApi partnerApi = optional.get();
                partnerApi.setApi(optionalApi.get());

                List<Integer> billerIds = vo.getBillerApiId();
                List<BillerList> billerList = new ArrayList<>();
                int priority = 0;
                for (int billerId : billerIds) {
                    Optional<BillerApi> optionalBillerApi = billerApiRepository.findByBillerAndApiId(billerId, vo.getApiId());
                    //Optional<Biller> optionalBiller = billerRepository.findById(billerId);
                    if (optionalBillerApi.isPresent()) {
                        //BillerList bille
                        // ApiId(Integer.parseInt(id));
                        //biller.setBillerId(optionalBiller.get());

                        BillerList biller = new BillerList();
                        biller.setPartnerApiId(Integer.parseInt(id));
                        biller.setBillerApiId(optionalBillerApi.get());
                        biller.setBillerId(billerId);
                        biller.setPriority(++priority);

                        billerList.add(billerListRepository.save(biller));
                    }
                }
                partnerApi.setBillerApiId(billerList);
                partnerApi.update(vo);

                billerListRepository.deleteNullPartnerApiId();

                PartnerApi res = repository.save(partnerApi);

                refreshConfig();

                return res;
            }
            throw new DataNotFoundException(ResultMessage.API_ID, StatusCode.DATA_NOT_FOUND);
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    @Transactional
    public String delete(PartnerApiVO vo){
        String id = vo.getId();
        Optional<PartnerApi> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            repository.delete(optional.get());
            billerListRepository.deleteNullPartnerApiId();

            refreshConfig();

            return ResultMessage.SUCCESS_DEL;
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public List<PartnerApi> findAll(){
        return repository.findAll();
    }

    public PartnerApi findById(int id) {
        Optional<PartnerApi> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public List<PartnerApi> findByPartnerId(int id) {
        return repository.findByPartnerId(id);
    }

    public List<PartnerApi> findByPartnerIdAndCategory(int id, String category) {
        return repository.findByPartnerIdAndCategory(id, category);
    }

    private void refreshConfig() {
        configUpdater.updateAllInstance();
        configController.setInstanceConfig();
    }
}
