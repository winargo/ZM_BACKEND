package com.billermanagement.services.handler;

import com.billermanagement.persistance.domain.resultset.PartnerResult;
import com.billermanagement.persistance.repository.PartnerApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PartnerInfoConfig {
    private static final Map<String,PartnerResult> map = new HashMap<>();
    private static final Object _lock = new Object();

    @Autowired
    private PartnerApiRepository partnerApiRepository;

    @PostConstruct
    private void init() {
        loadData();
    }

    private void loadData() {
        long start = System.currentTimeMillis();
        List<PartnerResult> partnerResultList = partnerApiRepository.findPartnerInfo();
        map.clear();
        for (PartnerResult partnerResult : partnerResultList) {
            map.put(partnerResult.getPartner(), partnerResult);
        }
        System.out.println(">>>>> PartnerInfoConfig loadData:"  + partnerApiRepository + "," + map.size() + "," + (System.currentTimeMillis()-start) + "ms");
    }

    public void refresh() {
        synchronized (_lock) {
            loadData();
        }
    }

    public PartnerResult get(String partnerName, String productCode) {
        String key = new StringBuilder(partnerName).append('.').append(productCode).toString();

        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            synchronized (_lock) {
                if (!map.containsKey(key))
                    loadData();
            }
            return map.getOrDefault(key, null);
        }
    }
}
