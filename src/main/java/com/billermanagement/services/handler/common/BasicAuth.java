package com.billermanagement.services.handler.common;

import com.billermanagement.util.Base64Util;
import com.billermanagement.util.InitDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BasicAuth {

    @Autowired
    Base64Util base64Util;

    InitDB initDB = InitDB.getInstance();

    public String getBasicAuth(String billerAlias){
        String username = initDB.get(billerAlias+".Username");
        String password = initDB.get(billerAlias+".Password");
        return base64Util.encode(username + ":" + password);
    }
}
