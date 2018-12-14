package com.Jayce.Redis.Utils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.*;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @author s on 2018/6/7
 */
public class HttpClientUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);
    private static CloseableHttpClient httpClient = null;

    public HttpClientUtil() {
    }

    public static String doGet(String url) throws HttpException {
        return doGetForString(url, new HashMap());
    }

    public static InputStream doGetForStream(String url) throws HttpException {
        return doGetForStream(url, new HashMap());
    }

    public static String doGetForString(String url, Map<String, String> params) throws HttpException {
        HttpResponse response = doGet(url, paramsConverter(params), (List) null);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new HttpException(
                    "HttpClientUtil#doGetForString - 请求返回状态码异常 : " + response.getStatusLine().getStatusCode());
        } else {
            return readResponseContent(response);
        }
    }

    public static InputStream doGetForStream(String url, Map<String, String> queryParams) throws HttpException {
        HttpResponse response = doGet(url, queryParams);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new HttpException(
                    "HttpClientUtil#doGetForStream - 请求返回状态码异常 : " + response.getStatusLine().getStatusCode());
        } else {
            return readInputStream(response);
        }
    }

    public static HttpResponse doGet(String url, Map<String, String> queryParams) throws HttpException {
        return doGet(url, paramsConverter(queryParams), (List) null);
    }

    public static HttpResponse doGet(String url, List<NameValuePair> queryParams, List<Header> headers)
            throws HttpException {
        HttpGet method = new HttpGet();
        builderRequestMethodURI(method, url, queryParams);
        builderRequestMethodHeader(method, headers);

        try {
            return httpClient.execute(method);
        } catch (IOException e) {
            throw new HttpException("HttpClientUtil#doGet - 网络请求错误 : ", e);
        }
    }

    public static String doPost(String url, Map<String, String> formParams) throws HttpException {
        HttpResponse response = doPost(url, (List) null, (List) paramsConverter(formParams), (List) null);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new HttpException("HttpClientUtil#doPost - 请求返回状态码异常 : " + response.getStatusLine().getStatusCode());
        } else {
            return readResponseContent(response);
        }
    }

    public static String doPostForString(String url, String entityText) throws HttpException {
        HttpResponse response = doPost(url, (List) null, (String) entityText, (List) null);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new HttpException(
                    "HttpClientUtil#doPostForString - 请求返回状态码异常 : " + response.getStatusLine().getStatusCode());
        } else {
            return readResponseContent(response);
        }
    }

    public static InputStream doPostForStream(String url, Map<String, String> formParams) throws HttpException {
        HttpResponse response = doPost(url, (List) null, (List) paramsConverter(formParams), (List) null);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new HttpException(
                    "HttpClientUtil#doPostForStream - 请求返回状态码异常 : " + response.getStatusLine().getStatusCode());
        } else {
            return readInputStream(response);
        }
    }

    public static InputStream doPostForStream(String url, Map<String, String> queryParams,
                                              Map<String, String> formParams) throws HttpException {
        HttpResponse response = doPost(url, paramsConverter(queryParams), (List) paramsConverter(formParams),
                (List) null);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new HttpException(
                    "HttpClientUtil#doPostForStream - 请求返回状态码异常 : " + response.getStatusLine().getStatusCode());
        } else {
            return readInputStream(response);
        }
    }

    public static HttpResponse doPost(String url, List<NameValuePair> formParams) throws HttpException {
        return doPost(url, (List) null, (List) formParams, (List) null);
    }

    public static HttpResponse doPost(String url, List<NameValuePair> formParams, List<Header> headers)
            throws HttpException {
        return doPost(url, (List) null, (List) formParams, headers);
    }

    public static HttpResponse doPost(String url, List<NameValuePair> queryParams, List<NameValuePair> formParams,
                                      List<Header> headers) throws HttpException {
        return _doPost(url, queryParams, formParams, headers);
    }

    public static HttpResponse doPost(String url, List<NameValuePair> queryParams, String entityText,
                                      List<Header> headers) throws HttpException {
        return _doPost(url, queryParams, entityText, headers);
    }

    private static HttpResponse _doPost(String url, List<NameValuePair> queryParams, Object formParams,
                                        List<Header> headers) throws HttpException {
        HttpPost method = new HttpPost();
        builderRequestMethodURI(method, url, queryParams);
        builderRequestMethodHeader(method, headers);
        builderRequestMethodEntityData(method, formParams);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(1000)
                .setSocketTimeout(5000)
                .build();
        method.setConfig(requestConfig);

        try {
            return httpClient.execute(method);
        } catch (IOException e) {
            throw new HttpException("HttpClientUtil#doPost - 网络请求错误 : ", e);
        }
    }

    public static HttpResponse doPost(String url, List<NameValuePair> queryParams, List<NameValuePair> formParams,
                                      List<Header> headers, int connectTimeout, int connectionRequestTimeout,
                                      int socketTimeout) throws HttpException {
        return _doPost(url, queryParams, formParams, headers, connectTimeout, connectionRequestTimeout, socketTimeout);
    }

    private static HttpResponse _doPost(String url, List<NameValuePair> queryParams, Object formParams,
                                        List<Header> headers, int connectTimeout, int connectionRequestTimeout,
                                        int socketTimeout) throws HttpException {
        HttpPost method = new HttpPost();
        builderRequestMethodURI(method, url, queryParams);
        builderRequestMethodHeader(method, headers);
        builderRequestMethodEntityData(method, formParams);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setSocketTimeout(socketTimeout)
                .build();
        method.setConfig(requestConfig);

        try {
            return httpClient.execute(method);
        } catch (IOException e) {
            throw new HttpException("HttpClientUtil#doPost - 网络请求错误 : ", e);
        }
    }

    public static String doPost(String url, Map<String, String> formParams, int connectTimeout,
                                int connectionRequestTimeout, int socketTimeout) throws HttpException {
        HttpResponse response = doPost(url, (List) null, paramsConverter(formParams), (List) null, connectTimeout,
                connectionRequestTimeout, socketTimeout);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new HttpException("HttpClientUtil#doPost - 请求返回状态码异常 : " + response.getStatusLine().getStatusCode());
        } else {
            return readResponseContent(response);
        }
    }

    private static void builderRequestMethodURI(HttpRequestBase method, String url, List<NameValuePair> queryParams)
            throws HttpException {
        try {
            URIBuilder builder = new URIBuilder(url);
            if (queryParams != null && !queryParams.isEmpty()) {
                builder.addParameters(queryParams);
            }

            method.setURI(builder.build());
        } catch (URISyntaxException e) {
            throw new HttpException("HttpClientUtil#builderRequestMethodURI - 构建请求地址及参数错误 : ", e);
        }
    }

    private static void builderRequestMethodHeader(HttpRequestBase method, List<Header> headers) {
        if (headers != null && !headers.isEmpty()) {
            Iterator it = headers.iterator();

            while (it.hasNext()) {
                Header header = (Header) it.next();
                method.addHeader(header);
            }
        }

    }

    private static void builderRequestMethodEntityData(HttpPost method, Object formParams) throws HttpException {
        if (formParams != null) {
            if (formParams instanceof List) {
                if (!((List) formParams).isEmpty()) {
                    method.setEntity(new UrlEncodedFormEntity((List) formParams, Charset.forName("UTF-8")));
                }
            } else {
                if (!(formParams instanceof String)) {
                    throw new HttpException(
                            "HttpClientUtil#builderRequestMethodEntityData - 不支持的HttpPostEntity参数 : " + JSON.toJSONString(
                                    formParams));
                }

                if (StringUtils.isNotEmpty(formParams.toString())) {
                    method.setEntity(new StringEntity(formParams.toString(), Charset.forName("UTF-8")));
                }
            }

        }
    }

    private static List<NameValuePair> paramsConverter(Map<String, String> params) {
        List<NameValuePair> nvps = new LinkedList();
        Set<Map.Entry<String, String>> paramsSet = params.entrySet();
        Iterator it = paramsSet.iterator();

        while (it.hasNext()) {
            Map.Entry<String, String> paramEntry = (Map.Entry) it.next();
            nvps.add(new BasicNameValuePair((String) paramEntry.getKey(), (String) paramEntry.getValue()));
        }

        return nvps;
    }

    public static InputStream readInputStream(HttpResponse response) throws HttpException {
        if (response != null) {
            try {
                return response.getEntity().getContent();
            } catch (IOException e) {
                throw new HttpException("HttpClientUtil ## 读取数据流失败", e);
            }
        } else {
            throw new HttpException("HttpClientUtil ## 读取数据流失败,传入的HttpResponse对象为空");
        }
    }

    public static String readResponseContent(HttpResponse response) {
        HttpEntity entity;
        if (response != null && (entity = response.getEntity()) != null) {
            try {
                byte[] content = EntityUtils.toByteArray(entity);
                ContentType contentType = ContentType.getOrDefault(entity);
                Charset charset = contentType.getCharset();
                if (charset == null) {
                    charset = HTTP.DEF_CONTENT_CHARSET;
                }

                return new String(content, charset.name());
            } catch (IOException e) {
                LOGGER.error("读取返回内容出错 : ", Throwables.getStackTraceAsString(e));
                return "";
            }
        } else {
            return "";
        }
    }

    static {
        try {
            SSLContext sslContext = (new SSLContextBuilder()).loadTrustMaterial(
                    KeyStore.getInstance(KeyStore.getDefaultType()), new TrustStrategy() {
                        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            return true;
                        }
                    }).build();
            Registry socketFactoryRegistry = RegistryBuilder.create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslContext))
                    .build();
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);
            SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
            connManager.setDefaultSocketConfig(socketConfig);
            MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(200).setMaxLineLength(
                    2000).build();
            ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(
                    CodingErrorAction.IGNORE).setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(
                    Consts.UTF_8).setMessageConstraints(messageConstraints).build();
            connManager.setDefaultConnectionConfig(connectionConfig);
            connManager.setMaxTotal(200);
            connManager.setDefaultMaxPerRoute(20);
            httpClient = HttpClients.custom().setConnectionManager(connManager).build();
        } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
            LOGGER.warn("HttpClientUil ## 初始化HttpClient配置失败 : ", Throwables.getStackTraceAsString(e));
        }
    }
}
