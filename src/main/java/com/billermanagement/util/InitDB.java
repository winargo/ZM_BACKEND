package com.billermanagement.util;

import com.billermanagement.persistance.domain.BMConfig;
import com.billermanagement.services.InitConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InitDB {
    private static final Map<String, String> params = new HashMap<>();
    //private static final Map<String, String> defaults = new HashMap<String, String>();
    private static final InitDB instance = new InitDB();
    //private static final int PERIOD = 20000;
    //private static final String EMPTY = "";
    //private static long lastupdate = 0;

    @Autowired
    private InitConfigService initConfigService;

    public static InitDB getInstance() {
        //instance.loadData();
        return instance;
    }

    /*private void loadData() {
        if ((0 - lastupdate) > PERIOD) {
            init();
        }
    }*/

    @PostConstruct
    public void init() {
        List<BMConfig> xx = initConfigService.findAll();
        for (BMConfig i : xx) {
            params.put(i.getParam_name(), i.getParam_value());
            log.info("BMConfig:" + i.getParam_name() + "," + i.getParam_value());
        }
        //lastupdate = 0;
    }

    public String get(String paramName) {
        //String result = params.get(paramName);
        /*if ((result == null) || (result.equals(EMPTY))) {
            result = defaults.get(paramName);
        }*/
        return params.getOrDefault(paramName, null);
    }

    public String put(String paramName, String paramValue) {
        System.out.println(paramName + "," + paramValue);
        params.put(paramName, paramValue);

        return paramValue;
    }
}


