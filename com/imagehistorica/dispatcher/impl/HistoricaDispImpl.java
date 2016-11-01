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

import static com.imagehistorica.util.Constants.HISTORICA_MAKE_ACCESS_POINT;

import com.imagehistorica.common.state.AnalyzeState;
import com.imagehistorica.databases.model.Historica;
import com.imagehistorica.databases.model.Request;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import org.apache.http.entity.ByteArrayEntity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.httpclient.CentralHttpClient;

import org.apache.commons.codec.binary.Hex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class HistoricaDispImpl extends DispImplHistorica {

    private AnalyzeState state = AnalyzeState.getInstance();
    private boolean isSkipReq = false;
    private TreeMap<String, List<Request>> requests_map;
    private List<Request> requests;

    private final Logger logger = LoggerFactory.getLogger(HistoricaDispImpl.class);

    public HistoricaDispImpl(TreeMap<String, List<Request>> requests) {
        this.requests_map = requests;
        this.requests = this.requests_map.firstEntry().getValue();
    }

    @Override
    public void implOpen() {
        logger.debug("Called HistoricaDispImpl()...request size: {}", requests_map.size());
        if (requests_map.isEmpty()) {
            isSkipReq = true;
        }
    }

    @Override
    public void implProcess() {
        if (!isSkipReq) {
            if (logger.isDebugEnabled()) {
                for (Request request : requests) {
                    logger.debug("historicaId: {}", request.getHistoricaId());
                    logger.debug("realImageId: {}", request.getRealImageId());
                    logger.debug("type: {}", request.getType());
                    logger.debug("realImagePath0: {}", request.getRealImagePath0());
                    logger.debug("realImagepath1: {}", request.getRealImagePath1());
                    logger.debug("lmod: {}", request.getLmod());
                    logger.debug("alt: {}", request.getAlt());
                    logger.debug("signature: {}", request.getSignature());
                    if (request.getFeature() != null) {
                        logger.debug("feature: {}", new String(Hex.encodeHex(request.getFeature())));
                    } else {
                        logger.debug("feature: null");
                    }
                    logger.debug("sha1: {}", new String(Hex.encodeHex(request.getSha1())));
                }
            }

            try {
                ByteArrayEntity entity = new ByteArrayEntity(new ObjectMapper().writeValueAsBytes(requests_map));
                byte[] rbody = CentralHttpClient.doPost(HISTORICA_MAKE_ACCESS_POINT, entity, state, 60000);
                if (rbody != null) {
                    historicas = new ObjectMapper().readValue(rbody, new TypeReference<List<Historica>>() {
                    });
                }
            } catch (IOException ex) {
                logger.error("", ex);
            }
        }
    }

    @Override
    public void implClose() {
        if (!isSkipReq) {
            if (!state.isSkipReq()) {
                logger.debug("historicas size: {}", historicas.size());
                if (!historicas.isEmpty()) {
                    for (Historica historica : historicas) {
                        for (Request request : requests) {
                            if (historica.getHistoricaId() == request.getHistoricaId()) {
                                historica.setMajor(Backend.major);
                                historica.setMinor(Backend.minor);
                                historica.setRevision(Backend.revision);
                                historica.setType(request.getType());
                                historica.setLength(request.getLength());
                                historica.setFeature(request.getFeature());
                                historica.setSha1(request.getSha1());
                                historica.setImgio(request.getImgio());
                                break;
                            }
                        }
                    }

                    if (logger.isDebugEnabled()) {
                        historicas.stream().forEach((historica) -> {
                            logger.debug(historica.toString());
                        });
                    }
                }
            }
        }
    }
}
