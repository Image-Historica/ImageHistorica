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

import com.imagehistorica.databases.model.Request;
import com.imagehistorica.Key;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class SignatureDispImpl extends DispImplRequestMap {

    private boolean isSkipReq = false;
    private String acsKey;
    private String secKey;
    private byte[] data;
    private List<Request> reqs;
    private List<Request> updatedReqs = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(SignatureDispImpl.class);

    public Map<String, Request> historicas_map = new HashMap<>();

    public SignatureDispImpl(List<Request> requests) {
        this.reqs = requests;
    }

    @Override
    public void implOpen() {
        if (reqs.isEmpty()) {
            isSkipReq = true;
            return;
        }

        Key key = Key.getInstance();
        acsKey = Key.accessKey;
        secKey = key.getProperty("SECRET_KEY") + key.getProperty("TOKEN");
    }

    @Override
    public void implProcess() {
        if (!isSkipReq) {
            try {
                SecretKeySpec signingKey = new SecretKeySpec(secKey.getBytes(), "HmacSHA1");
                Mac mac = Mac.getInstance("HmacSHA1");
                mac.init(signingKey);
                for (Request req : reqs) {
                    int len = req.getSha1().length;
                    data = ByteBuffer.allocate(len).put(req.getSha1()).array();

                    byte[] rawHmac = mac.doFinal(data);
                    String signature = Base64.encodeBase64String(rawHmac);
                    req.setSignature(signature);
                    updatedReqs.add(req);
                    mac.reset();
                }
            } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
                logger.debug("", ex);
            } catch (Exception ex) {
                logger.error("", ex);
            }
        }
    }

    @Override
    public void implClose() {
        if (!isSkipReq) {
            logger.debug("updatedReqs size: {}", updatedReqs.size());
            requests.put(acsKey, updatedReqs);
        }
    }
}
