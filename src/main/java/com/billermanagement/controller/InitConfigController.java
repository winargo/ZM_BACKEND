package com.billermanagement.controller;

import com.billermanagement.persistance.domain.BMConfig;
import com.billermanagement.services.InitConfigService;
import com.billermanagement.services.TransformService;
import com.billermanagement.services.handler.*;
import com.billermanagement.util.InitDB;
import com.billermanagement.util.InitDBHandler;
import com.billermanagement.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Slf4j
@RestController
public class InitConfigController {

    @Autowired
    private InitConfigService initConfigService;

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private PartnerConfig partnerConfig;

    @Autowired
    private PartnerInfoConfig partnerInfo;

    @Autowired
    private BillerConfig billerConfig;

    @Autowired
    private BillerApiConfig billerApiConfig;

    @Autowired
    private StatusMapping statusMapping;
    
    @Autowired
    private TransformService transformService;

    @Autowired
    ConfigUpdater configUpdater;

    @Autowired
    Environment environment;

    private Logger logger = LoggerFactory.getLogger(InitConfigController.class);

 //   @Autowired
//    private OGPEncryptionService ogpService;

    @PostMapping(value = "/BMConfig", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BMConfig add(@RequestBody BMConfig input){


        return initConfigService.add(input);
    }

    @PostMapping(value = "/api/v1/BMConfig/FileUpload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public ResponseEntity saveUsers(@RequestParam(value = "files") MultipartFile[] files,
                                    @RequestParam(value = "prefix") String prefix) throws Exception {
        for (MultipartFile file : files) {
            initConfigService.saveBms(file, prefix);
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(value = "/api/v1/BM/FindName/{prefix}", produces = "application/json")
    public ResponseEntity<ResultVO> findNameByPrefix(@PathVariable(value = "prefix") String prefix) throws Exception {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return initConfigService.findNameByPrefix(prefix);
            }
        };
        return handler.getResult();
    }

    @GetMapping(value = "/api/v1/BM/FindConfig/{prefix}", produces = "application/json")
    public ResponseEntity<ResultVO> findConfigByPrefix(@PathVariable(value = "prefix") String prefix) throws Exception {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return initConfigService.findConfigByPrefix(prefix);
            }
        };
        return handler.getResult();
    }

    @GetMapping(value = "/KasprobankConfig", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BMConfig> findAll(){

        return initConfigService.findAll();
    }

//    @Scheduled(cron = "0 0/15 * * * *")
    @GetMapping(value = "/api/v1/BMConfigReload", produces = MediaType.APPLICATION_JSON_VALUE)
    public String reLoad(){
        List<BMConfig> listX = initConfigService.findAll();
        InitDB x  = InitDB.getInstance();
        for (BMConfig i : listX) {
            x.put(i.getParam_name(), i.getParam_value());
        }
        //String result = "Reload Config ok";
        return "Reload Config ok";
    }

    @GetMapping(value = "/BMConfigGet",produces = MediaType.APPLICATION_JSON_VALUE)
    public String get(@RequestParam String Name){
        String result = InitDBHandler.paramName(Name);
        log.info("Loading Param :" +result);
        return result;
    }

    @GetMapping(value = "/api/v1/BMConfig", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BMConfig> findAllConfig(){

        return initConfigService.findAll();
    }

    @GetMapping(value = "/api/v1/BMConfigGet",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultVO> getDetail(@RequestParam (value="id", required = true) int id) {
        AbstractRequestHandler handler = new AbstractRequestHandler() {
            @Override
            public Object processRequest() {
                return initConfigService.detail(id).getParam_value();
            }
        };
        return handler.getResult();
    }

    @PostMapping(value = "/api/v1/BMConfig/Update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BMConfig update(@RequestBody BMConfig input){
        return initConfigService.update(input);
    }

//    @Scheduled(cron = "0 25 0 * * *")
    @GetMapping(value = "/api/v1/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public String refreshConfig() {
        long start = System.currentTimeMillis();

        apiConfig.refresh();
        partnerConfig.refresh();
        partnerInfo.refresh();
        billerConfig.refresh();
        billerApiConfig.refresh();
        statusMapping.refresh();
        transformService.refresh();

        String result = "Refresh Config OK";
        System.out.println(result + "," + (System.currentTimeMillis()-start) + "ms");
        return result;
    }

    @GetMapping(value = "/api/v1/refreshConfig", produces = MediaType.APPLICATION_JSON_VALUE)
    public String reloadAllConfig() {
        long start = System.currentTimeMillis();
        reLoad();
        refreshConfig();

        String result = "Reload All Config OK";
        System.out.println(result + "," + (System.currentTimeMillis()-start) + "ms");
        return result;
    }

//    @Scheduled(cron = "0 1/5 * * * *")
    @GetMapping(value = "/api/v1/setInstanceConfig", produces = MediaType.APPLICATION_JSON_VALUE)
    public String setInstanceConfig() {
        long start = System.currentTimeMillis();

        String status = configUpdater.getInstanceStatus();
        if (status != null && status.equals("1")) {
            reloadAllConfig();
            configUpdater.reset();
        }

        String result = "Set Instance Config OK";
        System.out.println(result + "," + (System.currentTimeMillis()-start) + "ms");
        return result;
    }

    @Scheduled(cron = "0 */10 * * * *")
    @GetMapping(value = "/api/v1/newRefreshConfig", produces = MediaType.APPLICATION_JSON_VALUE)
    public String newRefreshConfig() {
        long start = System.currentTimeMillis();
        logger.info("Start New Refresh Config");
        String myIPs = environment.getProperty("MY_IPS");
        logger.info("MyIPs :" + myIPs);
        if (myIPs != null){
            List<BMConfig> listBMConfig = initConfigService.findConfigByPrefix("Refresh");
            String[] ips = myIPs.split(" ");
            if (listBMConfig != null || listBMConfig.size() !=0){
                for (BMConfig config : listBMConfig){
                    for (String ip : ips){
                        logger.info("ip : "+ ip);
                        if (config.getParam_name().contains(ip) && config.getParam_value().equalsIgnoreCase("1")){
                            logger.info("Config Refresh is running on : "+config.getParam_name());
                            reloadAllConfig();
                            config.setParam_value("0");
                            initConfigService.update(config);
                        }else{
                            logger.info("No Refresh Required.!");
                        }
//                        if (config.getParam_name().equalsIgnoreCase("Refresh.Dev."+ip) && config.getParam_value().equalsIgnoreCase("1")){
//                            reloadAllConfig();
//                            config.setParam_value("0");
//                            initConfigService.update(config);
//                            logger.info("Refresh.Dev."+ip+" is running...");
//                        }
//
//                        if (config.getParam_name().equalsIgnoreCase("Refresh.I."+ip) && config.getParam_value().equalsIgnoreCase("1")){
//                            reloadAllConfig();
//                            config.setParam_value("0");
//                            initConfigService.update(config);
//                            logger.info("Refresh.I."+ip+" is running...");
//                        }else if(config.getParam_name().equalsIgnoreCase("Refresh.II."+ip) && config.getParam_value().equalsIgnoreCase("1")){
//                            reloadAllConfig();
//                            config.setParam_value("0");
//                            initConfigService.update(config);
//                            logger.info("Refresh.II."+ip+" is running...");
//                        }
//                        else if(config.getParam_name().equalsIgnoreCase("Refresh.III."+ip) && config.getParam_value().equalsIgnoreCase("1")){
//                            reloadAllConfig();
//                            config.setParam_value("0");
//                            initConfigService.update(config);
//                            logger.info("Refresh.III."+ip+" is running...");
//                        }
//                        else if(config.getParam_name().equalsIgnoreCase("Refresh.IV."+ip) && config.getParam_value().equalsIgnoreCase("1")){
//                            reloadAllConfig();
//                            config.setParam_value("0");
//                            initConfigService.update(config);
//                            logger.info("Refresh.IV."+ip+" is running...");
//                        }
                    }
                }
            }else{
                logger.error("Config Refresh are not found.!");
                return "Config Refresh are not found.!";
            }
        }else{
            logger.error("Server IPs are not detected.!");
            return "Server IPs are not detected.!";
        }

        String result = "New Refresh Config OK";
        logger.info(result + "," + (System.currentTimeMillis()-start) + "ms");
        return result;
    }


//    @PostMapping(value = "/api/v1/KasprobankConfig/CertificateUpload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
//    public ResponseEntity uploadCertificate(@RequestParam(value = "files") MultipartFile[] files,
//                                            @RequestParam(value = "name") String name) throws Exception {
//        for (MultipartFile file : files) {
//            ogpService.uploadCertificate(file, name);
//        }
//        return ResponseEntity.status(HttpStatus.CREATED).build();
//    }
}
