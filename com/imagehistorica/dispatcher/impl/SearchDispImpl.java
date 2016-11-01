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
package com.imagehistorica.dispatcher.impl;

import com.imagehistorica.Config;
import com.imagehistorica.common.state.SearchState;
import com.imagehistorica.util.ImageType;
import com.imagehistorica.httpclient.GeneralHttpClient;
import com.imagehistorica.search.SearchImage;

import javafx.scene.image.Image;
import javafx.util.Pair;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.ConnectionClosedException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class SearchDispImpl extends DispImpl {

    private boolean isSkipReq = false;
    private final SearchState state = SearchState.getInstance();
    private final SearchImage searchImage;
    private final String id;
    private final Logger logger = LoggerFactory.getLogger(SearchDispImpl.class);

    public SearchDispImpl(SearchImage searchImage) {
        this.searchImage = searchImage;
        this.id = this.searchImage.getId();
    }

    @Override
    public void implOpen() {
        if (!id.equals(state.getCurId())) {
            isSkipReq = true;
        }
    }

    @Override
    public void implProcess() {
        if (!isSkipReq) {
            while (searchImage.peekUrl() != null) {
                if (!id.equals(state.getCurId()) || state.isOOM()) {
                    searchImage.setInProgress(false);
                    break;
                }

                String imgInfo = searchImage.pollUrl();
                if (imgInfo != null) {
                    searchImage.setInProgress(true);
                    // 
                    String imgInfoId = null;
                    String host = null;
                    String imgUrl = null;
                    String parentUrl = null;
                    try {
                        String[] imgInfos = imgInfo.split("`");
                        imgInfoId = imgInfos[0] + imgInfos[1];
                        host = imgInfos[2];
                        if (host.startsWith("|")) {
                            imgUrl = "https://" + imgInfos[3];
                            host = host.replaceFirst("\\|", "");
                        } else {
                            imgUrl = "http://" + imgInfos[3];
                        }
                        if (imgInfos[4].startsWith("|")) {
                            parentUrl = "https://" + imgInfos[4];
                            parentUrl = parentUrl.replaceFirst("\\|", "");
                        } else {
                            parentUrl = "http://" + imgInfos[4];
                        }
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        logger.debug("imgInfo: {}", imgInfo);
                    }

                    CloseableHttpResponse response = null;
                    HttpEntity entity = null;
                    HttpContext context = HttpClientContext.create();
                    HttpGet get = new HttpGet(imgUrl);
                    get.addHeader("Host", host);
                    get.addHeader("User-Agent", Config.getUserAgent());
                    get.addHeader("Connection", "close");
                    try {
                        response = GeneralHttpClient.httpClient.execute(get, context);
                        int statusCode = response.getStatusLine().getStatusCode();
                        if (statusCode == HttpStatus.SC_OK) {
                            entity = response.getEntity();
                            if (entity != null) {
                                entity = new BufferedHttpEntity(entity);
                                long len = entity.getContent().available();
                                // limit under 20MB
                                if (len == -1 || len > 20971520) {
                                    searchImage.putFailedReqs(imgInfoId, (byte) 11);
                                    throw new Exception();
                                }
                                if (ImageType.isImage(entity)) {
                                    searchImage.offerCentral(new Pair<>(imgInfo, new Image(entity.getContent(), 300, 300, true, false)));
                                } else {
                                    searchImage.putFailedReqs(imgInfoId, (byte) 12);
                                }
                            }
                        } else if (statusCode == HttpStatus.SC_FORBIDDEN || statusCode == HttpStatus.SC_NOT_ACCEPTABLE
                                || statusCode == HttpStatus.SC_PRECONDITION_FAILED || statusCode == HttpStatus.SC_UNAUTHORIZED || statusCode == HttpStatus.SC_BAD_REQUEST) {
                            get.reset();
                            get.addHeader("Referer", parentUrl);
                            response = GeneralHttpClient.httpClient.execute(get, context);
                            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                entity = response.getEntity();
                                if (entity != null) {
                                    entity = new BufferedHttpEntity(entity);
                                    long len = entity.getContentLength();
                                    // limit under 20MB
                                    if (len == -1 || len > 20971520) {
                                        searchImage.putFailedReqs(imgInfoId, (byte) 11);
                                        throw new Exception();
                                    }
                                    if (ImageType.isImage(entity)) {
                                        searchImage.offerCentral(new Pair<>(imgInfo, new Image(entity.getContent(), 300, 300, true, false)));
                                    } else {
                                        searchImage.putFailedReqs(imgInfoId, (byte) 12);
                                    }
                                }
                            } else {
                                searchImage.putFailedReqs(imgInfoId, (byte) 13);
                            }
                        } else {
                            searchImage.putFailedReqs(imgInfoId, (byte) 20);
                        }
                    } catch (ClientProtocolException ex) {
                        searchImage.putFailedReqs(imgInfoId, (byte) 21);
                    } catch (SSLHandshakeException ex) {
                        searchImage.putFailedReqs(imgInfoId, (byte) 22);
                    } catch (SSLException ex) {
                        searchImage.putFailedReqs(imgInfoId, (byte) 23);
                    } catch (NoHttpResponseException ex) {
                        searchImage.putFailedReqs(imgInfoId, (byte) 24);
                    } catch (HttpHostConnectException ex) {
                        searchImage.putFailedReqs(imgInfoId, (byte) 25);
                    } catch (SocketTimeoutException ex) {
                        searchImage.putFailedReqs(imgInfoId, (byte) 26);
                    } catch (SocketException ex) {
                        searchImage.putFailedReqs(imgInfoId, (byte) 27);
                    } catch (ConnectTimeoutException ex) {
                        searchImage.putFailedReqs(imgInfoId, (byte) 28);
                    } catch (ConnectionClosedException ex) {
                        searchImage.putFailedReqs(imgInfoId, (byte) 29);
                    } catch (RequestAbortedException ex) {
                        get.completed();
                        get.releaseConnection();
                    } catch (OutOfMemoryError ex) {
                        searchImage.clearCentral();
                        System.gc();
                        state.setOOM(true);
                        isSkipReq = true;
                        String s = "130: Total memory usage: " + Runtime.getRuntime().totalMemory();
                        logger.warn(s, ex);
                        searchImage.putFailedReqs(imgInfoId, (byte) 30);
                    } catch (IOException ex) {
                        logger.debug("", ex);
                    } catch (Exception ex) {
                        logger.debug("", ex);
                    } finally {
                        try {
                            if (entity != null) {
                                EntityUtils.consume(entity);
                            }
                            if (response != null) {
                                response.close();
                            }
                        } catch (ConnectionClosedException ex) {
                            logger.debug(ex.getMessage() + " requested to... Host: " + host + ", imgUrl: " + imgUrl);
                        } catch (IOException ex) {
                            logger.debug(ex.getMessage() + " requested to... Host: " + host + ", imgUrl: " + imgUrl);
                        }
                    }
                } else {
                    searchImage.setInProgress(false);
                    break;
                }
            }
        }
    }

    @Override
    public void implClose() {
        if (!isSkipReq) {
            searchImage.setInProgress(false);
        }
    }
}
