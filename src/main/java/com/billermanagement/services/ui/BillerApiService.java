package com.billermanagement.services.ui;

import com.billermanagement.controller.InitConfigController;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.DataNotFoundException;
import com.billermanagement.persistance.domain.*;
import com.billermanagement.persistance.repository.ApiRepository;
import com.billermanagement.persistance.repository.BillerApiRepository;
import com.billermanagement.persistance.repository.BillerRepository;
import com.billermanagement.services.handler.ConfigUpdater;
import com.billermanagement.vo.ui.BillerApiVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BillerApiService {
    @Autowired
    BillerApiRepository repository;

    @Autowired
    BillerRepository billerRepository;

    @Autowired
    ApiRepository apiRepository;

    @Autowired
    ConfigUpdater configUpdater;

    @Autowired
    InitConfigController configController;

    @Transactional
    public BillerApi create(BillerApiVO vo) {
        Optional<Biller> optional = billerRepository.findById(vo.getBillerId());
        if (optional.isPresent()) {
            Optional<Api> optionalApi = apiRepository.findById(vo.getApiId());
            if (optionalApi.isPresent()) {
                /*Optional<Transform> optionalTransform = transformRepository.findById(vo.getTransformId());
                if (optionalTransform.isPresent()) {

                }
                throw new DataNotFoundException(ResultMessage.TRANSFORM_ID, StatusCode.DATA_NOT_FOUND);*/

                BillerApi billerApi = new BillerApi(vo);
                billerApi.setBiller(optional.get());
                billerApi.setApi(optionalApi.get());
                //billerApi.setTransform(optionalTransform.get());

                BillerApi res = repository.save(billerApi);

                refreshConfig();

                return res;
            }
            throw new DataNotFoundException(ResultMessage.API_ID, StatusCode.DATA_NOT_FOUND);
        }

        throw new DataNotFoundException(ResultMessage.BILLER_ID, StatusCode.DATA_NOT_FOUND);
    }

    @Transactional
    public BillerApi update(BillerApiVO vo){
        String id = vo.getId();
        Optional<BillerApi> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            Optional<Api> optionalApi = apiRepository.findById(vo.getApiId());
            if (optionalApi.isPresent()) {
                /*Optional<Transform> optionalTransform = transformRepository.findById(vo.getTransformId());
                if (optionalTransform.isPresent()) {

                }
                throw new DataNotFoundException(ResultMessage.TRANSFORM_ID, StatusCode.DATA_NOT_FOUND);*/

                BillerApi billerApi = optional.get();
                billerApi.setApi(optionalApi.get());
                //billerApi.setTransform(optionalTransform.get());
                billerApi.update(vo);
                BillerApi res = repository.save(billerApi);

                refreshConfig();

                return res;
            }
            throw new DataNotFoundException(ResultMessage.API_ID, StatusCode.DATA_NOT_FOUND);
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public String delete(BillerApiVO vo){
        String id = vo.getId();
        Optional<BillerApi> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            repository.delete(optional.get());

            refreshConfig();

            return ResultMessage.SUCCESS_DEL;
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public List<BillerApi> findAll(){
        return repository.findAll();
    }

    public BillerApi findById(int id) {
        Optional<BillerApi> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public List<BillerApi> findByBillerId(int id) {
        return repository.findByBillerId(id);
    }

    public List<String> findDistinctCategoryByBillerId(int id) {
        return repository.findDistinctCategoryByBillerId(id);
    }

    public List<BillerApi> findByCategory(String category) {
        return repository.findByCategory(category);
    }

    public List<BillerApi> findByBillerIdAndCategory(int id, String category) {
        return repository.findByBillerIdAndCategory(id, category);
    }

    public BillerApi toggleStatus(int id) {
        Optional<BillerApi> optionalBillerApi = repository.findById(id);
        if (optionalBillerApi.isPresent()) {
            BillerApi billerApi = optionalBillerApi.get();

            /*if (billerApi.getStatus().equals("ACTIVE")) {
                billerApi.setStatus("INACTIVE");
            } else {
                billerApi.setStatus("ACTIVE");
            }*/
            if (billerApi.isStatus()) {
                billerApi.setStatus(false);
            } else {
                billerApi.setStatus(true);
            }
            repository.save(billerApi);

            refreshConfig();

            return billerApi;
        }
        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public List<BillerApi> toggleCategoryStatus(int id, String category, String status) {
        List<BillerApi> billerApis = repository.findByBillerIdAndCategory(id, category);

        for (BillerApi billerApi : billerApis) {
            billerApi.setStatus(status.equals("active"));
            repository.save(billerApi);
        }

        refreshConfig();

        return billerApis;
    }

    private void refreshConfig() {
        configUpdater.updateAllInstance();
        configController.setInstanceConfig();
    }
}
