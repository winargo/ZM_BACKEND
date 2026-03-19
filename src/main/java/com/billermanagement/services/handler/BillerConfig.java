package com.billermanagement.services.handler;

import com.billermanagement.persistance.domain.Biller;
import com.billermanagement.persistance.repository.BillerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillerConfig {
    private static final Map<Integer,String> map = new HashMap<>();
    private static final Object _lock = new Object();

    @Autowired
    private BillerRepository billerRepository;

    @PostConstruct
    private void init() {
        loadData();
    }

    private void loadData() {
        long start = System.currentTimeMillis();
        List<Biller> billerList = billerRepository.findActiveBiller();
        map.clear();
        for (Biller billerMapping : billerList) {
            map.put(billerMapping.getId(), billerMapping.getBillerAlias());
        }
        System.out.println(">>>>> BillerConfig loadData:"  + billerRepository + "," + map.size() + "," + (System.currentTimeMillis()-start) + "ms");
    }

    public void refresh() {
        synchronized (_lock) {
            loadData();
        }
    }

    public String get(int billerId) {
        if (map.containsKey(billerId)) {
            return map.get(billerId);
        } else {
            synchronized (_lock) {
                if (!map.containsKey(billerId))
                    loadData();
            }
            return map.getOrDefault(billerId, null);
        }
    }
}
