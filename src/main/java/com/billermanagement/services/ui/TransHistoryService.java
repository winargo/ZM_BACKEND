package com.billermanagement.services.ui;

import com.billermanagement.persistance.domain.TransHistory;
import com.billermanagement.persistance.domain.resultset.TransHistoryResp;
import com.billermanagement.persistance.domain.resultset.TransHistoryResponse;
import com.billermanagement.persistance.domain.resultset.TransHistoryResult;
import com.billermanagement.persistance.repository.TransHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TransHistoryService {
    @Autowired
    TransHistoryRepository repository;

    Logger logger = LoggerFactory.getLogger(TransHistoryService.class);

    public List<TransHistory> findAll() {
        List<TransHistory> resultList = new ArrayList<>();
        repository.findAll().forEach(resultList::add);
        return resultList;
    }

    public TransHistoryResp findAll2(Integer page, Integer size,String sortBy,String order) {
        String sortByField=sortBy;
        if (sortBy.isEmpty() || sortBy.equals("") || sortBy == null){
            sortByField="id";
        }
        String sortOrder = order;
        Sort sort;
        if (sortOrder.equalsIgnoreCase("asc")){
            sort = Sort.by(sortByField).ascending();
        }else{
            sort = Sort.by(sortByField).descending();
        }

        Pageable pageable = PageRequest.of(page-1,size,sort);
        Page<TransHistory> resultList = repository.findAll(pageable);
        TransHistoryResp transHistoryResponse = new TransHistoryResp();
        transHistoryResponse.setTotalPages(resultList.getTotalPages());
        transHistoryResponse.setTotalElements(resultList.getTotalElements());
        transHistoryResponse.setTransHistoryResults(resultList.getContent());
        return transHistoryResponse;
    }

    public List<TransHistoryResult> findRecord(Integer partnerId, Integer billerId, String category, String startDate, String endDate) {
        if (startDate.indexOf("-") != -1) {
            if (startDate.length() == 10) {
                startDate = startDate + " 00:00:00";
                endDate = endDate + " 23:59:59";
            }
        } else {
            if (startDate.length() == 8) {
                startDate = startDate + "000000";
                endDate = endDate + "235959";
            }
        }

        return repository.findRecord(partnerId, billerId, category, startDate, endDate);
    }

    public TransHistoryResponse findRecord2(Integer partnerId, Integer billerId, String category, String startDate, String endDate, Integer page, Integer size,String sortBy,String order,String search) {
        if (startDate.indexOf("-") != -1) {
            if (startDate.length() == 10) {
                startDate = startDate + " 00:00:00";
                endDate = endDate + " 23:59:59";
            }
        } else {
            if (startDate.length() == 8) {
                startDate = startDate + "000000";
                endDate = endDate + "235959";
            }
        }

        String sortByField=sortBy;
        if (sortBy.isEmpty() || sortBy.equals("") || sortBy == null){
            sortByField="DATE_CREATED";
        }
        String sortOrder = order;
        Sort sort;
        if (sortOrder.equalsIgnoreCase("asc")){
            sort = Sort.by(sortByField).ascending();
        }else{
            sort = Sort.by(sortByField).descending();
        }

        Pageable pageable = PageRequest.of(page-1,size,sort);

        List<TransHistoryResult> transHistoryResults = repository.findRecord2(partnerId, billerId, category, startDate, endDate,search,pageable);
        int rowCount = repository.countFindRecord2(partnerId, billerId, category, startDate, endDate,search);
        logger.info("rowCount of countFindRecord2:"+rowCount);
        logger.info("Size:"+size);
        double bagi = Double.valueOf(rowCount)/Double.valueOf(size);
        logger.info("Hasil Bagi (rowCount/size):"+bagi);
        double totalPages = Math.ceil(bagi);
        logger.info("totalPages:"+totalPages);

        TransHistoryResponse transHistoryResponse = new TransHistoryResponse();
        transHistoryResponse.setTotalPages((int) totalPages);
        transHistoryResponse.setTotalElements(rowCount);
        transHistoryResponse.setTransHistoryResults(transHistoryResults);

        return transHistoryResponse;
    }
}
