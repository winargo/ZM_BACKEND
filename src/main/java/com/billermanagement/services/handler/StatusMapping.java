package com.billermanagement.services.handler;

import com.billermanagement.persistance.domain.ResultMapping;
import com.billermanagement.persistance.repository.ResultMappingRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatusMapping {
    private static final Map<String,BMCode> map = new HashMap<>();
    private static final Object _lock = new Object();
    private static final int i = 900000;
    private static long start = 0;

    @Autowired
    private ResultMappingRepository resultMappingRepository;

    @PostConstruct
    private void init() {
        loadData();
    }

    private void loadData() {
        start = System.currentTimeMillis();
        List<ResultMapping> resultMappingList = resultMappingRepository.findAll();
        map.clear();
        for (ResultMapping resultMapping : resultMappingList) {
            String key = new StringBuilder(resultMapping.getBiller().getId()).append('.').append(resultMapping.getBillerCode()).toString();
            BMCode bmCode = new BMCode();
            bmCode.setCode(resultMapping.getBmCode());
            bmCode.setDescription(resultMapping.getDescription());
            map.put(key, bmCode);
        }
        System.out.println(">>>>> StatusMapping loadData:"  + resultMappingRepository + "," + map.size() + "," + (System.currentTimeMillis()-start) + "ms");
        start = System.currentTimeMillis();
    }

    public void refresh() {
        synchronized (_lock) {
            loadData();
        }
    }

    public BMCode get(int billerId, String billerCode) {
        String key = new StringBuilder(billerId).append('.').append(billerCode).toString();

        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            if ((System.currentTimeMillis() - start) > i) {
                synchronized (_lock) {
                    if (((System.currentTimeMillis() - start) > i) && !map.containsKey(key))
                        loadData();
                }
            }
            return map.getOrDefault(key, null);
        }
    }
}

@Data
class BMCode {
    private String code;
    private String description;
}
