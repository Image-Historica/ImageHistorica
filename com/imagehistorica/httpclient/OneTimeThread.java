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

import com.imagehistorica.Config;
import com.imagehistorica.util.view.CommonAlert;
import com.imagehistorica.util.LmodMaker;
import com.imagehistorica.util.model.RealImageProperty;
import com.imagehistorica.controller.resources.Rsc;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class OneTimeThread extends Thread {

    private final RealImageProperty prop;
    private final HttpContext context;
    private final LmodMaker lmodMaker;

    private final Logger logger = LoggerFactory.getLogger(OneTimeThread.class);

    public OneTimeThread(RealImageProperty prop, LmodMaker lmodMaker) {
        this.prop = prop;
        this.context = HttpClientContext.create();
        this.lmodMaker = lmodMaker;
    }

    @Override
    public void run() {
        try {
            HttpGet get = new HttpGet(prop.getRealImagePath0());
            get.addHeader("Host", prop.getRealImagePath0().getHost());
            get.addHeader("User-Agent", Config.getUserAgent());
            get.addHeader("Connection", "close");

            logger.debug("httpGet first time: {}", get.getRequestLine());
            CloseableHttpResponse res = GeneralHttpClient.httpClient.execute(get, context);
            HttpEntity entity = null;
            try {
                if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    prop.setLmod(lmodMaker.getLmod(res.getFirstHeader("last-modified")));
                    entity = res.getEntity();
                    prop.setBinaryImage(EntityUtils.toByteArray(entity));
                } else {
                    if (prop.getRealImagePath1() != null) {
                        get.reset();
                        get.addHeader("Referer", prop.getRealImagePath1().toString());
                        logger.debug("httpGet second time: {}", get.getRequestLine());
                        res = GeneralHttpClient.httpClient.execute(get, context);
                        if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            prop.setLmod(lmodMaker.getLmod(res.getFirstHeader("last-modified")));
                            entity = res.getEntity();
                            prop.setBinaryImage(EntityUtils.toByteArray(entity));
                        }
                    }
                }
            } finally {
                if (entity != null) {
                    EntityUtils.consume(entity);
                }
                res.close();
            }
        } catch (UnknownHostException ex) {
            // multi byte domain issue
            logger.debug("May be not able to resolve multi byte domain...{}", ex.getMessage());
            prop.setException(Rsc.get("unknownHost") + ex.getMessage());
        } catch (IOException ex) {
            CommonAlert.getErrorLog(ex);
        }
    }
}
