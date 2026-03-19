package com.billermanagement.services.handler;

import com.billermanagement.persistance.domain.Api;
import com.billermanagement.persistance.repository.ApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiConfig {
    private static final Map<String,String> map = new HashMap<>();
    private static final Object _lock = new Object();

    @Autowired
    ApiRepository repository;

    @PostConstruct
    private void init() {
        loadData();
    }

    private void loadData() {
        long start = System.currentTimeMillis();
        List<Api> apiList = repository.findAll();
        map.clear();
        for (Api api : apiList) {
            map.put(api.getApiId(), api.getApiCategory());
        }
        System.out.println(">>>>> ApiConfig loadData:"  + repository + "," + map.size() + "," + (System.currentTimeMillis()-start) + "ms");
    }

    public void refresh() {
        synchronized (_lock) {
            loadData();
        }
    }

    public String get(String partnerCode) {
        if (map.containsKey(partnerCode)) {
            return map.get(partnerCode);
        } else {
            synchronized (_lock) {
                if (!map.containsKey(partnerCode))
                    loadData();
            }
            return map.getOrDefault(partnerCode, null);
        }
    }
}
