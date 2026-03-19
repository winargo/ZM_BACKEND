package com.billermanagement.services.ui;

import com.billermanagement.controller.InitConfigController;
import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.DataNotFoundException;
import com.billermanagement.persistance.domain.Api;
import com.billermanagement.persistance.repository.ApiRepository;
import com.billermanagement.services.handler.ConfigUpdater;
import com.billermanagement.vo.ui.ApiVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ApiService {
    @Autowired
    ApiRepository repository;

    @Autowired
    ConfigUpdater configUpdater;

    @Autowired
    InitConfigController configController;

    @Transactional
    public Api create(ApiVO vo) {
        Api api = new Api(vo);
        Api res = repository.save(api);

        refreshConfig();

        return res;
    }

    @Transactional
    public Api update(ApiVO vo){
        String id = vo.getId();
        Optional<Api> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            Api api = optional.get();
            api.update(vo);
            Api res = repository.save(api);

            refreshConfig();

            return res;
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    @Transactional
    public String delete(ApiVO vo){
        String id = vo.getId();
        Optional<Api> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            repository.delete(optional.get());

            refreshConfig();

            return ResultMessage.SUCCESS_DEL;
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public List<Api> findAll(){
        return repository.findAll();
    }

    public Api findById(int id) {
        Optional<Api> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public Api findByApiId(String id) {
        Optional<Api> optional = repository.findByApiId(id);
        if (optional.isPresent()) {
            return optional.get();
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public List<Api> findByApiCategory(String category) {
        return repository.findByApiCategory(category);
    }

    public List<String> findDistinctCategory() {
        return repository.findDistinctCategory();
    }

    private void refreshConfig() {
        configUpdater.updateAllInstance();
        configController.setInstanceConfig();
    }

    public String findCategoryByProductCode(String productCode){
        return repository.findCategoryByProductCode(productCode);
    }
}
