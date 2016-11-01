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

import com.imagehistorica.Key;
import com.imagehistorica.common.state.SearchState;
import com.imagehistorica.common.state.State;
import com.imagehistorica.util.view.CommonAlert;
import com.imagehistorica.controller.resources.Rsc;
import static com.imagehistorica.util.Constants.ACCESS_POINT;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class CentralHttpClient {

    private static final HttpClientContext context = HttpClientContext.create();
    private static final BasicHttpClientConnectionManager connMgr = new BasicHttpClientConnectionManager();
    private static final HttpHost accessPoint = new HttpHost(ACCESS_POINT, 443, "https");
    private static final HttpRoute route = new HttpRoute(accessPoint);
    private static final HttpRequestExecutor exeRequest = new HttpRequestExecutor();
    private static final ImmutableHttpProcessor proc = new ImmutableHttpProcessor(new RequestTargetHost(), new RequestContent());
    private static ConnectionRequest connRequest;
    private static HttpClientConnection conn;
    private static final String acsKey = Key.accessKey;

    private static final Logger logger = LoggerFactory.getLogger(CentralHttpClient.class);

    public static String doGet(String input, int timeout) {
        String rbody = null;
        HttpResponse res = null;
        HttpEntity entity = null;
        try {
            connRequest = connMgr.requestConnection(route, null);
            conn = connRequest.get(1, TimeUnit.SECONDS);
            if (!conn.isOpen()) {
                connMgr.connect(conn, route, 2000, context);
                connMgr.routeComplete(conn, route, context);
            }
            context.setTargetHost(accessPoint);
            HttpGet get = new HttpGet(input);
            get.addHeader("Cookie", "HISTORICA=" + acsKey);
            conn.setSocketTimeout(timeout);
            res = exeRequest.execute(get, conn, context);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = res.getEntity();
                rbody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            } else if (res.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                entity = res.getEntity();
                rbody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                CommonAlert ca = new CommonAlert();
                ca.showAnyError(rbody, null);
            }
        } catch (IllegalStateException | HttpException ex) {
            logger.debug(ex.getMessage() + " " + input);
        } catch (SocketTimeoutException ex) {
            logger.debug(ex.getMessage() + " " + input);
        } catch (IOException ex) {
            logger.debug(ex.getMessage() + " " + input);
        } catch (InterruptedException | ExecutionException ex) {
            logger.error("", ex);
        } finally {
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException ex) {
                    logger.error("", ex);
                }
            }
            if (res != null) {
                HttpClientUtils.closeQuietly(res);
            }
            connMgr.releaseConnection(conn, null, 1, TimeUnit.MINUTES);
        }

        return rbody;
    }

    public static String doPost(String url, StringEntity postEntity, State state, int timeout) {
        logger.info("Start doPost() string...");
        String rbody = null;
        HttpResponse res = null;
        HttpEntity entity = null;

        BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", url);
        request.addHeader("Content-Type", "text/plain; charset=UTF-8");
        request.addHeader("X-Requested-With", "ImageHistorica");
        request.addHeader("Cookie", "HISTORICA=" + acsKey);
        request.setEntity(postEntity);
        try {
            connRequest = connMgr.requestConnection(route, null);
            conn = connRequest.get(1, TimeUnit.SECONDS);
            if (!conn.isOpen()) {
                connMgr.connect(conn, route, 2000, context);
                connMgr.routeComplete(conn, route, context);
            }
            context.setTargetHost(accessPoint);
            exeRequest.preProcess(request, proc, context);
            conn.setSocketTimeout(timeout);
            res = exeRequest.execute(request, conn, context);
            exeRequest.postProcess(res, proc, context);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = res.getEntity();
                rbody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            } else if (res.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                entity = res.getEntity();
                CommonAlert ca = new CommonAlert();
                ca.showAnyError(EntityUtils.toString(entity, StandardCharsets.UTF_8), null);
                if (state instanceof SearchState) {
                    state.offerExceptions(EntityUtils.toString(entity, StandardCharsets.UTF_8));
                }
            } else {
                logger.info("doPost error... Code: {}, Reason: {}", res.getStatusLine().getStatusCode(), res.getStatusLine().getReasonPhrase());
            }
        } catch (IllegalStateException ex) {
            logger.debug(ex.getMessage() + " " + url);
        } catch (SocketTimeoutException ex) {
            logger.debug(ex.getMessage() + " " + url);
            state.setHasResponse(false);
            state.offerExceptions(ex.getMessage());
            state.offerExceptions(Rsc.get("socketTimeout"));
        } catch (IOException ex) {
            logger.debug(ex.getMessage() + " " + url);
        } catch (HttpException ex) {
            logger.debug(ex.getMessage() + " " + url);
        } catch (InterruptedException ex) {
            logger.debug("", ex);
        } catch (ExecutionException ex) {
            logger.debug("", ex);
        } finally {
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException ex) {
                    logger.debug("", ex);
                }
            }
            if (res != null) {
                HttpClientUtils.closeQuietly(res);
            }
            connMgr.releaseConnection(conn, null, 1, TimeUnit.MINUTES);
        }

        logger.info("End doPost() string...");

        return rbody;
    }

    public static synchronized byte[] doPost(String url, ByteArrayEntity postEntity, State state, int timeout) {
        logger.info("Start doPost() byte...");
        byte[] rbody = null;
        HttpResponse res = null;
        HttpEntity entity = null;

        BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", url);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-Requested-With", "ImageHistorica");
        request.addHeader("Cookie", "HISTORICA=" + acsKey);
        request.setEntity(postEntity);
        try {
            connRequest = connMgr.requestConnection(route, null);
            conn = connRequest.get(5, TimeUnit.SECONDS);
            if (!conn.isOpen()) {
                connMgr.connect(conn, route, 5000, context);
                connMgr.routeComplete(conn, route, context);
            }
            context.setTargetHost(accessPoint);
            exeRequest.preProcess(request, proc, context);
            conn.setSocketTimeout(timeout);
            res = exeRequest.execute(request, conn, context);
            exeRequest.postProcess(res, proc, context);
            int statusCode = res.getStatusLine().getStatusCode();
            logger.debug("StatusCode: {}", statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                entity = res.getEntity();
                rbody = EntityUtils.toByteArray(entity);
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                logger.debug("Unauthorized...");
                state.setSkipReq(true);
                entity = res.getEntity();
                if (state instanceof SearchState) {
                    CommonAlert ca = new CommonAlert();
                    ca.showAnyError(EntityUtils.toString(entity, StandardCharsets.UTF_8), null);
                }
                state.offerExceptions(EntityUtils.toString(entity, StandardCharsets.UTF_8));
            } else if (statusCode == HttpStatus.SC_BAD_REQUEST) {
                logger.debug("Bad request...");
                state.setSkipReq(true);
                entity = res.getEntity();
                logger.debug("entity: {}", EntityUtils.toString(entity, StandardCharsets.UTF_8));
                state.offerExceptions(EntityUtils.toString(entity, StandardCharsets.UTF_8));
            } else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                logger.debug("Internal server error...");
                state.setSkipReq(true);
                entity = res.getEntity();
                state.offerExceptions(EntityUtils.toString(entity, StandardCharsets.UTF_8));
            } else {
                state.setSkipReq(true);
                String s = "doPost error... Code: " + statusCode + ", Reason: " + res.getStatusLine().getReasonPhrase();
                logger.error(s);
                state.offerExceptions(s);
            }
        } catch (IllegalStateException ex) {
            logger.debug(ex.getMessage() + " " + url);
        } catch (HttpException ex) {
            logger.debug(ex.getMessage() + " " + url);
        } catch (NoHttpResponseException ex) {
            logger.debug(ex.getMessage() + " " + url);
            state.offerExceptions(ex.getMessage());
            state.offerExceptions(Rsc.get("noHttpResponse"));
        } catch (SocketTimeoutException ex) {
            logger.debug(ex.getMessage() + " " + url);
            state.offerExceptions(ex.getMessage());
            state.offerExceptions(Rsc.get("socketTimeout"));
        } catch (IOException ex) {
            logger.debug(ex.getMessage() + " " + url);
        } catch (InterruptedException ex) {
            logger.error("", ex);
        } catch (ExecutionException ex) {
            logger.error("", ex);
        } finally {
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException ex) {
                    logger.error("", ex);
                }
            }
            if (res != null) {
                HttpClientUtils.closeQuietly(res);
            }
            connMgr.releaseConnection(conn, null, 1, TimeUnit.MINUTES);
        }

        logger.info("End doPost() byte...");

        return rbody;
    }

    public static void close() {
        connMgr.shutdown();
    }
}
