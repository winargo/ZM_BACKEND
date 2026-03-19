package com.billermanagement.services.handler;

import com.billermanagement.persistance.domain.BMConfig;
import com.billermanagement.persistance.repository.BMConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.net.InetAddress;

@Service
public class ConfigUpdater {
    private static String instanceId;
    //private int port;

    @Autowired
    private BMConfigRepository bmConfigRepository;

    //@Autowired
    //private ServletWebServerApplicationContext server;

    //@EventListener
    //public void onApplicationEvent(final ServletWebServerInitializedEvent event) {
    //    port = event.getWebServer().getPort();
    //}


    @PostConstruct
    private void init() { refresh(); }

    private String refresh() {
        String ipAddress = null;
        //System.out.println(">>>>>>>>> Port: " + server.getWebServer().getPort());
        //System.out.println(">>>>>>>>> Port: " + port);
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();

            if (instanceId == null) instanceId = "instance." + ipAddress;
            BMConfig bmConfig = bmConfigRepository.selectByParamName(instanceId);
            if (bmConfig != null) {
                bmConfig.setParam_value("0");
            } else {
                bmConfig = new BMConfig();
                bmConfig.setParam_name(instanceId);
                bmConfig.setParam_value("0");
            }
            bmConfigRepository.save(bmConfig);
        } catch (Exception e) {
            System.err.println("ConfigUpdater:" + e.getMessage());
        }

        return instanceId;
    }

    public String getInstanceStatus() {
        BMConfig bmConfig = bmConfigRepository.selectByParamName(instanceId);
        String value = (bmConfig != null) ? bmConfig.getParam_value() : null;

        return value;
    }

    @Transactional
    public void reset() {
        bmConfigRepository.setInstanceStatus("0", instanceId);
    }

    @Transactional
    public void updateAllInstance() {
        bmConfigRepository.updateInstanceStatus();
    }
}
