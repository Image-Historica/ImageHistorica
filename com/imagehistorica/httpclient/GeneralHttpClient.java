/*
 * Copyright (C) 2016 Image-Historica.com
 *
 * This file is part of the ImageHistorica: https://image-historica.com
 * ImageHistorica is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.imagehistorica.httpclient;

import com.imagehistorica.util.model.RealImageProperty;
import com.imagehistorica.Config;
import com.imagehistorica.util.view.CommonAlert;
import com.imagehistorica.util.LmodMaker;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class GeneralHttpClient {

    public static final PoolingHttpClientConnectionManager connMgr;
    public static final CloseableHttpClient httpClient;
    public static final IdleConnectionMonitorThread connMonitorThread;

    static {
        RequestConfig requestConfig = RequestConfig.custom()
                .setExpectContinueEnabled(false)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setRedirectsEnabled(false)
                .setConnectTimeout(Config.getConnectionTimeout())
                .setSocketTimeout(Config.getSocketTimeout())
                .build();

        RegistryBuilder<ConnectionSocketFactory> connRegistryBuilder = RegistryBuilder.create();
        connRegistryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE);
        try {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, (TrustStrategy) (final X509Certificate[] chain, String authType) -> true).build();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            connRegistryBuilder.register("https", sslsf);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            CommonAlert.getErrorLog(e);
        }

        Registry<ConnectionSocketFactory> connRegistry = connRegistryBuilder.build();
        connMgr = new PoolingHttpClientConnectionManager(connRegistry);
        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(3);

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setDefaultRequestConfig(requestConfig);
        clientBuilder.setConnectionManager(connMgr);

        httpClient = clientBuilder.build();

        connMonitorThread = new IdleConnectionMonitorThread(connMgr);
        connMonitorThread.start();
    }

    public static void doGetImage(List<RealImageProperty> props) {
        LmodMaker lmodMaker = new LmodMaker();
        OneTimeThread[] threads = new OneTimeThread[props.size()];
        for (int i = 0; i < threads.length; i++) {
            RealImageProperty prop = props.get(i);
            threads[i] = new OneTimeThread(prop, lmodMaker);
        }

        for (OneTimeThread thread : threads) {
            thread.start();
        }

        for (OneTimeThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                CommonAlert.getDebugLog(ex.toString());
            }
        }
    }

    public static void close() {
        connMonitorThread.shutdown();
        HttpClientUtils.closeQuietly(httpClient);
        connMgr.shutdown();
    }
}
