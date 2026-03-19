package com.billermanagement.services.ui;

import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.DataNotFoundException;
import com.billermanagement.persistance.domain.Biller;
import com.billermanagement.persistance.domain.BillerPic;
import com.billermanagement.persistance.repository.BillerPicRepository;
import com.billermanagement.persistance.repository.BillerRepository;
import com.billermanagement.vo.ui.BillerPicVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BillerPicService {
    @Autowired
    BillerPicRepository repository;

    @Autowired
    BillerRepository billerRepository;

    @Transactional
    public BillerPic create(BillerPicVO vo) {
        Optional<Biller> optional = billerRepository.findById(vo.getBillerId());
        if (optional.isPresent()) {
            BillerPic billerPic = new BillerPic(vo);
            billerPic.setBiller(optional.get());

            return repository.save(billerPic);
        }

        throw new DataNotFoundException(ResultMessage.BILLER_ID, StatusCode.DATA_NOT_FOUND);
    }

    @Transactional
    public BillerPic update(BillerPicVO vo){
        String id = vo.getId();
        Optional<BillerPic> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            BillerPic billerPic = optional.get();
            billerPic.update(vo);

            return repository.save(billerPic);
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    @Transactional
    public String delete(BillerPicVO vo){
        String id = vo.getId();
        Optional<BillerPic> optional = repository.findById(Integer.parseInt(id));
        if (optional.isPresent()) {
            repository.delete(optional.get());

            return ResultMessage.SUCCESS_DEL;
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public List<BillerPic> findAll(){
        return repository.findAll();
    }

    public BillerPic findById(int id) {
        Optional<BillerPic> optional = repository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }

        throw new DataNotFoundException(ResultMessage.RECORD, StatusCode.DATA_NOT_FOUND);
    }

    public List<BillerPic> findByBillerId(int id) {
        return repository.findByBillerId(id);
    }
}
