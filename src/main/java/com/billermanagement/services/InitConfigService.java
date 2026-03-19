package com.billermanagement.services;

import com.billermanagement.enums.StatusCode;
import com.billermanagement.exception.NostraException;
import com.billermanagement.persistance.domain.BMConfig;
import com.billermanagement.persistance.repository.BMConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class InitConfigService {

    @Autowired
    private BMConfigRepository bmRepo;

    Object target;
    Logger logger = LoggerFactory.getLogger(InitConfigService.class);

    public List<BMConfig> findAll(){
        List<BMConfig> result = bmRepo.findAll();
        logger.info("get initial param "+Thread.currentThread().getName());
        return result;
    }



    public BMConfig add(BMConfig kasprobankConfig){
        BMConfig saved = bmRepo.save(kasprobankConfig);
        return saved;
    }

    public BMConfig getByName (String name){
        BMConfig kasprobankConfig=bmRepo.selectByParamName(name);
        return kasprobankConfig;
    }

    public BMConfig detail(int id) {
        return bmRepo.findById(id).get();
    }

    public BMConfig update(BMConfig kasprobankConfig){
        Optional<BMConfig> saved = bmRepo.findById(kasprobankConfig.getId());
        BMConfig config = saved.get();
        config.setParam_value(kasprobankConfig.getParam_value());
        config.setParam_name(config.getParam_name());
        bmRepo.save(config);
        return config;
    }

    private List<BMConfig> parseCSVFile(final MultipartFile file, String prefix) throws Exception {
        final List<BMConfig> bms = new ArrayList<>();
        logger.info("Parsing CSV File");
        try {
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    final String[] data = line.split(",");
                    final BMConfig bm = new BMConfig();
                    bm.setParam_name(prefix+data[0]);
                    logger.info(bm.getParam_name().toString());
                    bm.setParam_value(data[1]);
                    logger.info(bm.getParam_value().toString());
                    bms.add(bm);
                }
                return bms;
            }
        } catch (final IOException e) {
            logger.error("Failed to parse CSV file {}", e);
            throw new Exception("Failed to parse CSV file {}", e);
        }
    }

    @Async
    public CompletableFuture<List<BMConfig>> saveBms(MultipartFile file, String prefix) throws Exception {
        logger.info("Start Uploading");
        List<BMConfig> bms = parseCSVFile(file, prefix);
        for(BMConfig bm:bms){
            this.add(bm);
        }
        return CompletableFuture.completedFuture(bms);
    }

    public List<String> findNameByPrefix(String prefix){
        List<String> names = bmRepo.selectNameByPrefix(prefix);
        List<String> result = new ArrayList<>();
        if(names==null){
            throw new NostraException("Prefix not found", StatusCode.DATA_NOT_FOUND);
        }else{
            for(String s:names){
                String name = s.replace(prefix,"");
                result.add(name);
            }
            return result;
        }
    }

    public List<BMConfig> findConfigByPrefix(String prefix){
        List<BMConfig> configs = bmRepo.selectByPrefix(prefix);
        if(configs==null){
            throw new NostraException("Prefix not found", StatusCode.DATA_NOT_FOUND);
        }else{
            return configs;
        }
    }
}
