/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.util;

import com.billermanagement.persistance.domain.Transform;
import java.util.Calendar;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sulaeman
 */
public class GlobalHashmap {

    private final Logger log = LoggerFactory.getLogger(GlobalHashmap.class);
    private final HashMap<String, Object[]> globalMap;

    public GlobalHashmap() {
        globalMap = new HashMap<>();
    }

    public void setHashMap(String key, Object[] obj) {
        globalMap.put(key, obj);
    }

    public void updaHashMap(String key, Object[] obj) {
        globalMap.replace(key, obj);
    }

    public void removeHashMap(String key) {
        globalMap.remove(key);
    }

    public Object[] getHashMap(String key) {
        return globalMap.get(key);
    }

    public boolean containsKey(String key) {
        return globalMap.containsKey(key);
    }

    public long getExpiredTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        cal.add(Calendar.HOUR_OF_DAY, 6);

        log.info("Now                    : " + Calendar.getInstance().getTimeInMillis());
        log.info("Time after add 6 hours : " + cal.getTimeInMillis());
        return cal.getTimeInMillis();
    }

    public long getTimeNow() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public void setTransformHashMap(String id, Transform t) {
        Object[] objMap = new Object[8];
        objMap[0] = t.getId();
        objMap[1] = t.getType();
        objMap[2] = t.getFileRequest();
        objMap[3] = t.getFileResponse();
        objMap[4] = t.getFileCallback();
        objMap[5] = t.getUrl();
        objMap[6] = t.getFlowType();
        objMap[7] = getExpiredTime();
        setHashMap(id, objMap);
    }

    public void updateTransformHashMap(String id, Transform t) {
        Object[] objMap = new Object[8];
        objMap[0] = t.getId();
        objMap[1] = t.getType();
        objMap[2] = t.getFileRequest();
        objMap[3] = t.getFileResponse();
        objMap[4] = t.getFileCallback();
        objMap[5] = t.getUrl();
        objMap[6] = t.getFlowType();
        objMap[7] = getExpiredTime();
        updaHashMap(id, objMap);
    }

}
