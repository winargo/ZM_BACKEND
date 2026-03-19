package com.billermanagement.services.EbayService;

import com.billermanagement.util.InitDB;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

@Slf4j
@Service
public class EbayHTTPService {

    public String callHttpPost(String url, Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String valueAsString = mapper.writeValueAsString(object);
            log.info("Request : "+valueAsString);
            return callHttpPost(url, MediaType.APPLICATION_JSON_VALUE, new StringEntity(valueAsString));
        } catch (JsonProcessingException e) {
            log.warn("Got Json Processing Exception " + e);
            return null;
        } catch (UnsupportedEncodingException e) {
            log.warn("Got Unsupported Encoding Exception " + e);
            return null;
        }
    }

    private String callHttpPost(String url, String mediaType, HttpEntity entity) {
        InitDB initDB=InitDB.getInstance();
        String ebayHostUrl=initDB.get("ebay.url.host");

        String completeUrl = ebayHostUrl +  url;
        HttpPost httpPost = new HttpPost(completeUrl);
        httpPost.setHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
        httpPost.setHeader("Content-type", mediaType);

        httpPost.setEntity(entity);

        log.info("Calling {} with body {}", url, entity);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpEntity responseEntity = httpclient.execute(httpPost).getEntity();
            String responseBody = EntityUtils.toString(responseEntity, "UTF-8");
            httpclient.close();
            log.info("Finish Calling with response {}", responseBody);
            return responseBody;
        } catch (Exception e) {
            log.warn("Got Exception " + e);
        }
        return null;
    }
}
