package com.billermanagement.services.handler;

import com.billermanagement.persistance.domain.resultset.BillerResult;
import com.billermanagement.persistance.domain.resultset.PartnerSelectionResult;
import com.billermanagement.persistance.repository.BillerApiRepository;
import com.billermanagement.persistance.repository.PartnerApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillerApiConfig {
    private static final Map<String, String> mapSelection = new HashMap<>();
    private static final Map<String, List<BillerResult>> mapPrice = new HashMap<>();
    private static final Map<String, List<BillerResult>> mapPriority = new HashMap<>();
    private static final Object _lock = new Object();

    @Autowired
    private PartnerApiRepository partnerApiRepository;

    @Autowired
    private BillerApiRepository billerApiRepository;

    @PostConstruct
    private void init() {
        loadData();
    }

    private void loadData() {
        long start = System.currentTimeMillis();
        List<PartnerSelectionResult> partnerResultList = partnerApiRepository.findPartnerIdAndSelection();
        mapSelection.clear();
        mapPrice.clear();
        mapPriority.clear();
        for (PartnerSelectionResult resultMapping : partnerResultList) {
            String keyMap = resultMapping.getApiId();

            String[] keyMapping = keyMap.split("\\.");
            int partnerId = Integer.parseInt(keyMapping[0]);
            String productCode = keyMapping[1];

            mapSelection.put(keyMap, resultMapping.getApiSelection());
            mapPriority.put(keyMap, billerApiRepository.findByPriority(partnerId, productCode));
            mapPrice.put(keyMap, billerApiRepository.findByPrice(partnerId, productCode));
        }
        System.out.println(">>>>> BillerApiConfig.Selection loadData:"  + partnerApiRepository + "," + mapSelection.size() + "," + (System.currentTimeMillis()-start) + "ms");
        System.out.println(">>>>> BillerApiConfig.Priority loadData:"  + billerApiRepository + "," + mapPriority.size() + "," + (System.currentTimeMillis()-start) + "ms");
        System.out.println(">>>>> BillerApiConfig.Price loadData:"  + billerApiRepository + "," + mapPrice.size() + "," + (System.currentTimeMillis()-start) + "ms");
    }

    public void refresh() {
        synchronized (_lock) {
            loadData();
        }
    }

    public String get(int partnerId, String productCode) {
        String key = new StringBuilder().append(partnerId).append('.').append(productCode).toString();

        if (mapSelection.containsKey(key)) {
            return mapSelection.get(key);
        } else {
            synchronized (_lock) {
                if (!mapSelection.containsKey(key))
                    loadData();
            }
            return mapSelection.getOrDefault(key, null);
        }
    }

    public List<BillerResult> getByPrice(int partnerId, String productCode) {
        String key = new StringBuilder().append(partnerId).append('.').append(productCode).toString();

        if (mapPrice.containsKey(key)) {
            return mapPrice.get(key);
        } else {
            synchronized (_lock) {
                if (!mapPrice.containsKey(key))
                    loadData();
            }
            return mapPrice.getOrDefault(key, null);
        }
    }

    public List<BillerResult> getByPriority(int partnerId, String productCode) {
        String key = new StringBuilder().append(partnerId).append('.').append(productCode).toString();

        if (mapPriority.containsKey(key)) {
            return mapPriority.get(key);
        } else {
            synchronized (_lock) {
                if (!mapPriority.containsKey(key))
                    loadData();
            }
            return mapPriority.getOrDefault(key, null);
        }
    }
}
