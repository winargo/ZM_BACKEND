package com.billermanagement.services;

import com.billermanagement.vo.IDN.Xresponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

@Service
@Slf4j
public class HttpProcessingService {

    Logger logger = LoggerFactory.getLogger(HttpProcessingService.class);
    private static final int CONNECT_TIMEOUT = 1000;
    private static final int READ_TIMEOUT = 3000;

    public String postUser(String url, Map<String, String> header, String body, int connectTimeout, int readTimeout) throws IOException {
        if (connectTimeout == -1) {
            connectTimeout = CONNECT_TIMEOUT;
        }
        if (readTimeout == -1) {
            readTimeout = READ_TIMEOUT;
        }

        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }

            //print Header
            Header[] h = httpPost.getAllHeaders();
            for (Header h1 : h) {
                logger.info("--->> Headers: " + h1.getName() + "," + h1.getValue());
            }
        }

        StringEntity stringEntity = new StringEntity(body);
        httpPost.setEntity(stringEntity);

        String responseBody = null;
        long start = System.currentTimeMillis();

        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(readTimeout).build();
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {
            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                responseBody = EntityUtils.toString(entity, "UTF-8");
            }
            logger.info("postUser:" + httpPost.getRequestLine() + "," + responseBody + "," + (System.currentTimeMillis() - start) + "ms");
            logger.info("Http Status Code :" + response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            logger.error("postUser:" + httpPost.getRequestLine() + "," + e.getMessage() + "," + (System.currentTimeMillis() - start) + "ms");
            throw e;
        }

        return responseBody;
    }

    public String redirectPostUser(String url, Map<String, String> header, String body, int connectTimeout, int readTimeout) throws IOException {
        if (connectTimeout == -1) {
            connectTimeout = CONNECT_TIMEOUT;
        }
        if (readTimeout == -1) {
            readTimeout = READ_TIMEOUT;
        }

        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }

            //print Header
            Header[] h = httpPost.getAllHeaders();
            for (Header h1 : h) {
                logger.info("Headers: " + h1.getName() + "," + h1.getValue());
            }
        }

        StringEntity stringEntity = new StringEntity(body);
        httpPost.setEntity(stringEntity);

        String responseBody = null;
        long start = System.currentTimeMillis();

        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(readTimeout).build();
        try {
//            CloseableHttpClient httpclient = HttpClientBuilder
//                    .create()
//                    .setDefaultRequestConfig(config)
//                    .setRedirectStrategy(new LaxRedirectStrategy() {
//                        @Override
//                        public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
//                            final URI uri = getLocationURI(request, response, context);
//                            final String method = request.getRequestLine().getMethod();
//                            if (method.equalsIgnoreCase(HttpHead.METHOD_NAME)) {
//                                return new HttpHead(uri);
//                            } else if (method.equalsIgnoreCase(HttpGet.METHOD_NAME)) {
//                                return new HttpGet(uri);
//                            } else {
//                                final int status = response.getStatusLine().getStatusCode();
//                                if (status == 308 || status == HttpStatus.SC_TEMPORARY_REDIRECT || status == HttpStatus.SC_MOVED_TEMPORARILY) { //HttpStatus.SC_MOVED_TEMPORARILY == 302
//                                    return RequestBuilder.copy(request).setUri(uri).build();
//                                } else {
//                                    return new HttpGet(uri);
//                                }
//                            }
//                        }
//
//                    }).build();

//            HttpClient httpclient = HttpClientBuilder
//                    .create()
//                    .setDefaultRequestConfig(config)
//                    .setRedirectStrategy(new LaxRedirectStrategy())
//                    .build();
//            HttpClient httpclient = new DefaultHttpClient();
//            httpclient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_0);
//            httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
//            httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
            CloseableHttpClient httpclient = HttpClientBuilder
                    .create()
                    .setDefaultRequestConfig(config)
                    .build();

            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                responseBody = EntityUtils.toString(entity, "UTF-8");
            }
            logger.info("postUser:" + httpPost.getRequestLine() + "," + responseBody + "," + (System.currentTimeMillis() - start) + "ms");
            logger.info("Http Status Code :" + response.getStatusLine().getStatusCode());

//            Redirect Handler
            if (String.valueOf(response.getStatusLine().getStatusCode()).substring(0, 1).equalsIgnoreCase("3")) {
                // RESPONE THAT WORKS WITH JAVA
                String LocationHeader = response.getFirstHeader("location").getValue();
                logger.info("Location:" + LocationHeader);

                // To get the BODY I would have to parse that again - since its not REDIRECTING automatically
//            HttpClient httpclient2 = new DefaultHttpClient();
                CloseableHttpClient httpclient2 = HttpClientBuilder
                        .create()
                        .setDefaultRequestConfig(config)
                        .build();
                HttpPost httppost2 = new HttpPost(LocationHeader);
                httppost2.setHeader("Accept", "application/json");
                httppost2.setHeader("Content-Type", "application/json");
                if (header != null) {
                    for (Map.Entry<String, String> entry : header.entrySet()) {
                        httppost2.setHeader(entry.getKey(), entry.getValue());
                    }

                    //print Header
                    Header[] h = httppost2.getAllHeaders();
                    for (Header h1 : h) {
                        logger.info("Headers: " + h1.getName() + "," + h1.getValue());
                    }
                }

                httppost2.setEntity(stringEntity);
//            httpclient2.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_0);
                response = httpclient2.execute(httppost2);
                HttpEntity entity2 = response.getEntity();
                if (entity2 != null) {
                    responseBody = EntityUtils.toString(entity2, "UTF-8");
                }
                logger.info("redirectPostUser:" + httppost2.getRequestLine() + "," + responseBody + "," + (System.currentTimeMillis() - start) + "ms");
                logger.info("Http Status Code:" + response.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {
            logger.error("redirectPostUser:" + httpPost.getRequestLine() + "," + e.getMessage() + "," + (System.currentTimeMillis() - start) + "ms");
            throw e;
        }

        return responseBody;
    }

    public String postUserBypasSSLCert(String url, Map<String, String> header, String body, int connectTimeout, int readTimeout) throws IOException {
        if (connectTimeout == -1) {
            connectTimeout = CONNECT_TIMEOUT;
        }
        if (readTimeout == -1) {
            readTimeout = READ_TIMEOUT;
        }

        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }

            //print Header
            Header[] h = httpPost.getAllHeaders();
            for (Header h1 : h) {
                logger.info("--->> Headers: " + h1.getName() + "," + h1.getValue());
            }
        }

        StringEntity stringEntity = new StringEntity(body);
        httpPost.setEntity(stringEntity);

        String responseBody = null;
        long start = System.currentTimeMillis();

        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(readTimeout).build();
        try (CloseableHttpClient httpclient = getCloseableHttpClient(url, config)) {
            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                responseBody = EntityUtils.toString(entity, "UTF-8");
            }
            logger.info("postUser:" + httpPost.getRequestLine() + "," + responseBody + "," + (System.currentTimeMillis() - start) + "ms");
            logger.info("Http Status Code :" + response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            logger.error("postUser:" + httpPost.getRequestLine() + "," + e.getMessage() + "," + (System.currentTimeMillis() - start) + "ms");
            throw e;
        }

        return responseBody;
    }

    private CloseableHttpClient getCloseableHttpClient(String url, RequestConfig config) {
        if (url.startsWith("https://")) {
            try {
                return HttpClients.custom().
                        setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).
                        setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build()).build();
            } catch (KeyManagementException e) {
                log.error("KeyManagementException in creating http client instance", e);
            } catch (NoSuchAlgorithmException e) {
                log.error("NoSuchAlgorithmException in creating http client instance", e);
            } catch (KeyStoreException e) {
                log.error("KeyStoreException in creating http client instance", e);
            }
        }
        return HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    public Xresponse postUserV2(String url, Map<String, String> header, String body, int connectTimeout, int readTimeout) {
        Xresponse xres = new Xresponse();
        xres.setStatusCode(1999);

        if (connectTimeout == -1) {
            connectTimeout = CONNECT_TIMEOUT;
        }
        if (readTimeout == -1) {
            readTimeout = READ_TIMEOUT;
        }

        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }

            //print Header
            Header[] h = httpPost.getAllHeaders();
            for (Header h1 : h) {
                logger.info("--->> Headers: " + h1.getName() + "," + h1.getValue());
            }
        }

        String responseBody = null;
        long start = System.currentTimeMillis();

        try {
            StringEntity stringEntity = new StringEntity(body);
            httpPost.setEntity(stringEntity);
        } catch (UnsupportedEncodingException e) {
            xres.setStatusCode(1999);
            responseBody = e.getMessage();
            logger.info("StringEntity UnsupportedEncodingException error " + e.getMessage());
        }

        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(readTimeout).build();
        HttpResponse response = null;
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                responseBody = EntityUtils.toString(entity, "UTF-8");
            }
            xres.setStatusCode(response.getStatusLine().getStatusCode());
            logger.info("postUser:" + httpPost.getRequestLine() + "," + responseBody + "," + (System.currentTimeMillis() - start) + "ms");
            logger.info("Http Status Code :" + response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            logger.error("postUser:" + httpPost.getRequestLine() + "," + e.getMessage() + "," + (System.currentTimeMillis() - start) + "ms");
            logger.error("Http Status Code :" + response.getStatusLine().getStatusCode());
//            throw e;
            xres.setStatusCode(1999);
            responseBody = e.getMessage();
        }

        xres.setResponseBody(responseBody);
        return xres;
    }

    public String postUser(String url, String body, int connectTimout, int readTimout) throws IOException {
        return postUser(url, null, body, connectTimout, readTimout);
    }

    public String postUser(String url, String body) throws IOException {
        return postUser(url, null, body, -1, -1);
    }

    public String postUser(String url, Map<String, String> header, String body) throws IOException {
        return postUser(url, header, body, -1, -1);
    }

    public String get(String url, Map<String, String> header, int connectTimeout, int readTimeout) throws IOException {
        if (connectTimeout == -1) {
            connectTimeout = CONNECT_TIMEOUT;
        }
        if (readTimeout == -1) {
            readTimeout = READ_TIMEOUT;
        }

        HttpGet httpGet = new HttpGet(url);

        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-Type", "application/json");
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }

            //print Header
            Header[] h = httpGet.getAllHeaders();
            for (Header h1 : h) {
                logger.info("--->> Headers: " + h1.getName() + "," + h1.getValue());
            }
        }

        String responseBody = null;
        long start = System.currentTimeMillis();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(readTimeout).build();
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {
            HttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                responseBody = EntityUtils.toString(entity, "UTF-8");
            }
            logger.info("getUser:" + url + "," + responseBody + "," + (System.currentTimeMillis() - start) + "ms");
            logger.info("Http Status Code :" + response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            logger.error("getUser:" + url + "," + e.getMessage() + "," + (System.currentTimeMillis() - start) + "ms");
            throw e;
        }
        return responseBody;
    }

    public Xresponse getV2(String url, Map<String, String> header, int connectTimeout, int readTimeout) {
        Xresponse xres = new Xresponse();
        xres.setStatusCode(1999);

        if (connectTimeout == -1) {
            connectTimeout = CONNECT_TIMEOUT;
        }
        if (readTimeout == -1) {
            readTimeout = READ_TIMEOUT;
        }

        HttpGet httpGet = new HttpGet(url);

        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-Type", "application/json");
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }

            //print Header
            Header[] h = httpGet.getAllHeaders();
            for (Header h1 : h) {
                logger.info("--->> Headers: " + h1.getName() + "," + h1.getValue());
            }
        }

        String responseBody = null;
        long start = System.currentTimeMillis();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(readTimeout).build();
        HttpResponse response = null;
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                responseBody = EntityUtils.toString(entity, "UTF-8");
            }
            xres.setStatusCode(response.getStatusLine().getStatusCode());
            logger.info("getUser:" + url + "," + responseBody + "," + (System.currentTimeMillis() - start) + "ms");
            logger.info("Http Status Code :" + response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            logger.error("getUser:" + url + "," + e.getMessage() + "," + (System.currentTimeMillis() - start) + "ms");
            logger.error("Http Status Code :" + response.getStatusLine().getStatusCode());
//            throw new IOException(e.getMessage());
            responseBody = e.getMessage();
        }

        xres.setResponseBody(responseBody);
        return xres;
    }

    public String get(String url) throws IOException {
        return get(url, null, -1, -1);
    }

    /*public String postUser(String url, String body) throws IOException {
        String timeout = InitDB.getInstance().get("http.connect.timeout");
        int connectTimeout = (timeout == null) ? 3000 : Integer.parseInt(timeout);
        timeout = InitDB.getInstance().get("http.read.timeout");
        int readTimeout = (timeout == null) ? 5000 : Integer.parseInt(timeout);

        long start = System.currentTimeMillis();
        String responseBody="";
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(readTimeout).build();
        //try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            StringEntity stringEntity = new StringEntity(body);
            httpPost.setEntity(stringEntity);

            HttpResponse response;

            System.out.println("Executing request " + httpPost.getRequestLine());

            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                responseBody = EntityUtils.toString(entity, "UTF-8");
            }
            System.out.println("----------------------------------------");
            logger.info("postUser:" + responseBody + "," + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            logger.error("postUser:" + e.getMessage() + "," + (System.currentTimeMillis() - start) + "ms");
            throw e;
        }

        return responseBody;
    }*/

 /*public String kasproValidate(String msisdn) throws IOException {
        String responseBody="";
        String url="http://dev.kaspro.id/DCZ4DmJMPVKsX75/"+msisdn+"/validate";
        String token="WLu28cXFYvrdtQ7KFNxDUI3hpufmj+EbNknAEL9i7pfdjx69s/lnu3YSScaxUv+7Iere9Or5f1AvNC3rO8l+U3gkcU87vUrlHu6llGJeZiolpM2mD1ZePTlPyjVrArkmlK5Ui8vnGmu55anh2jq2Y4KD9HIj2FI8ENzfFqPX3/vmVH2e8ImkxsDuK1Ot+oH6BVxUKThhqcVPFfv3Qe52AA==";
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept-Language", "EN");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("token", token);

            HttpResponse response;

            System.out.println("Executing request " + httpGet.getRequestLine());

            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if(entity !=null){
                responseBody = EntityUtils.toString(entity, "UTF-8");
            }
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
        }
        return responseBody;
    }

    public String kasproPayu(String account) throws IOException {
        String responseBody="";
        String url="http://dev.kaspro.id/DCZ4DmJMPVKsX75/"+account+"/payu";
        String token="WLu28cXFYvrdtQ7KFNxDUI3hpufmj+EbNknAEL9i7pfdjx69s/lnu3YSScaxUv+7Iere9Or5f1AvNC3rO8l+U3gkcU87vUrlHu6llGJeZiolpM2mD1ZePTlPyjVrArkmlK5Ui8vnGmu55anh2jq2Y4KD9HIj2FI8ENzfFqPX3/vmVH2e8ImkxsDuK1Ot+oH6BVxUKThhqcVPFfv3Qe52AA==";
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept-Language", "EN");
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("token", token);

            HttpResponse response;

            System.out.println("Executing request " + httpGet.getRequestLine());

            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if(entity !=null){
                responseBody = EntityUtils.toString(entity, "UTF-8");
            }
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
        }
        return responseBody;
    }

    public String kasproCashIn(String body) throws IOException {
        String responseBody="";
        String url="http://dev.kaspro.id/DCZ4DmJMPVKsX75/923733080012/kaspro/transfers";
        String token="WLu28cXFYvrdtQ7KFNxDUI3hpufmj+EbNknAEL9i7pfdjx69s/lnu3YSScaxUv+7Iere9Or5f1AvNC3rO8l+U3gkcU87vUrlHu6llGJeZiolpM2mD1ZePTlPyjVrArkmlK5Ui8vnGmu55anh2jq2Y4KD9HIj2FI8ENzfFqPX3/vmVH2e8ImkxsDuK1Ot+oH6BVxUKThhqcVPFfv3Qe52AA==";
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Accept-Language", "EN");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("token", token);
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
    }*/
}
