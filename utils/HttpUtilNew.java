package com.demo.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HttpClient工具类
 */
@Slf4j
public class HttpUtilNew {

    public static <T> T doGet(String url, Class<T> clazz) {
        T t = null;
        String response = null;
        try {
            response = doGet(url);
            t = JSONObject.parseObject(response, clazz);
        } catch (Exception e) {
            log.error("请求结果转换失败:{}|{}", url, response);
        }
        return t;
    }

    public static <T> T doPost(String url, String jsonParam, String cookie, Class<T> clazz) {
        return JSONObject.parseObject(doPost(url, jsonParam, cookie), clazz);
    }

    public static String doGet(String url) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            log.error("GET请求失败:{} 异常详情:{}", url, e);
        }
        return null;
    }


    /**
     * post请求(用于key-value格式的参数)
     */
    public static String doPost(String url, Map params) {

        BufferedReader in = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost();
            request.setURI(new URI(url));
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (Iterator iter = params.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String value = String.valueOf(params.get(name));
                nvps.add(new BasicNameValuePair(name, value));
            }
            request.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            HttpResponse response = client.execute(request);
            int code = response.getStatusLine().getStatusCode();
            if (code == 200) { // 请求成功
                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                String NL = System.getProperty("line.separator");
                while ((line = in.readLine()) != null) {
                    sb.append(line).append(NL);
                }
                return sb.toString();
            } else {
                log.info("状态码：" + code);
                return null;
            }
        } catch (Exception e) {
            log.error("POST请求失败：", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("关闭异常：", e);
                }
            }
        }
        return null;
    }

    /**
     * post请求（用于请求json格式的参数）
     */
    public static String doPost(String url, String jsonParam, String cookie) {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);// 创建httpPost
        httpPost.setHeader("Accept", "application/json, text/plain, */*");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Cookie", cookie);

        StringEntity entity = new StringEntity(jsonParam, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        CloseableHttpResponse response = null;

        try {

            response = httpclient.execute(httpPost);
            StatusLine status = response.getStatusLine();
            int state = status.getStatusCode();
            if (state >= HttpStatus.SC_OK && state < HttpStatus.SC_MULTIPLE_CHOICES) {
                HttpEntity responseEntity = response.getEntity();
                return EntityUtils.toString(responseEntity, "UTF-8");
            }
        } catch (Exception e) {
            log.error("POST请求报错:{}异常信息:{}", url, jsonParam, e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpclient.close();
            } catch (IOException e) {
                log.error("关闭异常：", e);
            }
        }
        return null;
    }
}