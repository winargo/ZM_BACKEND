package com.billermanagement.services.handler;

import com.billermanagement.persistance.domain.Partner;
import com.billermanagement.persistance.repository.PartnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PartnerConfig {
    private static final Map<String,Integer> map = new HashMap<>();
    private static final Object _lock = new Object();

    @Autowired
    private PartnerRepository partnerRepository;

    @PostConstruct
    private void init() {
        loadData();
    }

    private void loadData() {
        long start = System.currentTimeMillis();
        List<Partner> partnerList = partnerRepository.findActivePartner();
        map.clear();
        for (Partner partnerMapping : partnerList) {
            map.put(partnerMapping.getAlias(), partnerMapping.getId());
        }
        System.out.println(">>>>> PartnerConfig loadData:"  + partnerRepository + "," + map.size() + "," + (System.currentTimeMillis()-start) + "ms");
    }

    public void refresh() {
        synchronized (_lock) {
            loadData();
        }
    }

    public int get(String partnerAlias) {
        if (map.containsKey(partnerAlias)) {
            return map.get(partnerAlias);
        } else {
            synchronized (_lock) {
                if (!map.containsKey(partnerAlias))
                    loadData();
            }
            return map.getOrDefault(partnerAlias, -1);
        }
    }
}
