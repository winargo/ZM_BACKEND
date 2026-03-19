/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.initializator;

import com.billermanagement.persistance.domain.Transform;
import com.billermanagement.services.TransformService;
import com.billermanagement.util.GlobalHashmap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulaeman
 */
@Component
public class AppInitializator {

    @Autowired
    private TransformService transformService;

    @Autowired
    private GlobalHashmap globalMap;

    private final Logger log = LoggerFactory.getLogger(AppInitializator.class);

    @PostConstruct
    private void init() {
        log.info("AppInitializator start....");
        List<Transform> listData = transformService.getAll();
        for (Transform t : listData) {
            String id = t.getTransformId()+ "." + t.getMethod();
            globalMap.setTransformHashMap(id, t);
        }

        log.info("AppInitializator finish....");
    }

}
