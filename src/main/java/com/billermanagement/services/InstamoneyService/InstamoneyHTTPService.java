package com.billermanagement.services.InstamoneyService;

import com.billermanagement.util.InitDB;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
public class InstamoneyHTTPService {

    public String callHttpPost(String url, Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String valueAsString = mapper.writeValueAsString(object);
            log.info("Request : "+valueAsString);
            return test(object.toString(),url);
//            return callHttpPost(url, MediaType.APPLICATION_FORM_URLENCODED_VALUE, new StringEntity(valueAsString));
        } catch (JsonProcessingException e) {
            log.warn("Got Json Processing Exception " + e);
            return null;
        }
//        catch (UnsupportedEncodingException e) {
//            log.warn("Got Unsupported Encoding Exception " + e);
//            return null;
//        }
    }

    private String callHttpPost(String url, String mediaType, HttpEntity entity) {
        InitDB initDB=InitDB.getInstance();
        String ebayHostUrl=initDB.get("instamoney.url.host");
        String username=initDB.get("instamoney.username");
        String password=initDB.get("instamoney.password");

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")) );
        String authHeader = "Basic " + new String( encodedAuth );

        String completeUrl = ebayHostUrl +  url;
        HttpPost httpPost = new HttpPost(completeUrl);
        httpPost.setHeader("Accept", MediaType.ALL_VALUE);
        httpPost.setHeader("Content-type", mediaType);
        httpPost.setHeader("Authorization",authHeader);

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

    public String test(String params,String path) {
        InitDB initDB=InitDB.getInstance();
        String ebayHostUrl=initDB.get("instamoney.url.host");
        URL url = null;
        InputStream stream = null;
        HttpURLConnection conn = null;
        try {
            url = new URL(ebayHostUrl+path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Authorization", "Basic aWx1bWFfZGV2ZWxvcG1lbnRfdGhydno4SGVCZU9JdE93YU50QWt3OFd4eTk1bDdHV1pqaThGOEVVbk9OYk1JbFE4azBoWUZGa0NSclhJMU06");
//            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
            conn.setUseCaches(false);


            conn.connect();

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(params);
            wr.flush();

            stream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), 8);
            String result = reader.readLine();
            log.info("Response "+result);
            return result;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("Result", "SLEEP ERROR");
        }
        return null;
    }

    public String postUser(String url, Map<String,String> header, String body) throws IOException {
        String responseBody="";
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            if (header!=null) {
                for (Map.Entry<String,String> entry : header.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            //print Header
            Header[] headers = httpPost.getAllHeaders();
            for (Header header1 : headers) {
                System.out.println("--->> Headers: name,value: "+header1.getName() + "," + header1.getValue());
            }
            //----------

            StringEntity stringEntity = new StringEntity(body);
            httpPost.setEntity(stringEntity);

            HttpResponse response;

            System.out.println("Executing request " + httpPost.getRequestLine());

            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if(entity !=null){
                responseBody = EntityUtils.toString(entity, "UTF-8");
            }
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
        }
        return responseBody;
    }
}
