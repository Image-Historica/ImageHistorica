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
package com.imagehistorica.common.menu.imports;

import static com.imagehistorica.util.model.SchemeType.FILE;
import static com.imagehistorica.util.Constants.HISTORICA_FETCH_ACCESS_POINT;

import com.imagehistorica.Config;
import com.imagehistorica.common.state.SearchState;
import com.imagehistorica.util.model.SchemeType;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.databases.model.Historica;
import com.imagehistorica.httpclient.CentralHttpClient;
import com.imagehistorica.httpclient.GeneralHttpClient;
import com.imagehistorica.search.Selectable;
import com.imagehistorica.search.SelectableImage;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imagehistorica.Key;
import com.imagehistorica.util.view.CommonAlert;
import com.imagehistorica.util.LmodMaker;
import static com.imagehistorica.util.model.SchemeType.HTTP;
import static com.imagehistorica.util.model.SchemeType.HTTPS;
import com.imagehistorica.controller.resources.Rsc;
import static com.imagehistorica.util.Constants.DELIMITER;
import com.imagehistorica.databases.model.Request;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.entity.ByteArrayEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class ImportAsyncTask {

    private final SearchState state = SearchState.getInstance();
    private final Set<Selectable> selectables;
    private final LmodMaker lmodMaker = new LmodMaker();
    private final String fileSeparator = File.separator;
    
    private final byte major = Backend.major;
    private final byte minor = Backend.minor;
    private final byte revision = Backend.revision;

    private final Logger logger = LoggerFactory.getLogger(ImportAsyncTask.class);

    public ImportAsyncTask(Set<Selectable> selectables) {
        this.selectables = selectables;
    }

    public void importHistorica() {
        List<Request> reqs = new ArrayList<>();
        List<String[]> imgInfos = new ArrayList<>();
        Key key = Key.getInstance();
        String secKey = key.getProperty("SECRET_KEY") + key.getProperty("TOKEN");
        SecretKeySpec signingKey = new SecretKeySpec(secKey.getBytes(), "HmacSHA1");
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            for (Selectable selectable : selectables) {
                SelectableImage si = (SelectableImage) selectable;
                String imageId = si.getImageId();
                String[] imgInfo = si.getImgInfos();
                imgInfos.add(imgInfo);

                byte[] b = imageId.getBytes(StandardCharsets.UTF_8);
                byte[] data = ByteBuffer.allocate(b.length).put(b).array();
                byte[] rawHmac = mac.doFinal(data);
                String signature = Base64.encodeBase64String(rawHmac);
                if (imgInfo[1].startsWith("http://")) {
                    reqs.add(new Request(HTTP, imageId, signature, Config.getCountry(), Config.getLang(), major, minor, revision));
                } else if (imgInfo[1].startsWith("https://")) {
                    reqs.add(new Request(HTTPS, imageId, signature, Config.getCountry(), Config.getLang(), major, minor, revision));
                }
                mac.reset();
            }
        } catch (NoSuchAlgorithmException ex) {
            logger.debug("", ex);
        } catch (InvalidKeyException ex) {
            logger.debug("", ex);
        } catch (Exception ex) {
            logger.debug("", ex);
        }

        if (reqs.isEmpty()) {
            return;
        }

        for (Request req : reqs) {
            logger.debug("ImageId: {}", req.getRealImagePath0());
            logger.debug("Signature: {}", req.getSignature());
        }

        List<Historica> incompHistoricas = fetchHistorica(new TreeMap<String, List<Request>>() {
            {
                put(Key.accessKey, reqs);
            }
        });
        if (incompHistoricas == null || incompHistoricas.isEmpty() || incompHistoricas.size() != reqs.size()) {
            logger.error("Colud not get proper historicas...");
            state.setImporting(false);
            return;
        }

        List<Historica> historicas = new ArrayList<>();
        for (int i = 0; i < incompHistoricas.size(); i++) {
            Historica historica = incompHistoricas.get(i);
            String[] imgInfo = imgInfos.get(i);
            Path path = null;
            path = definePath(incompHistoricas.get(i));
            if (!fetchImage(imgInfo, path, historica.getLmod())) {
                logger.error("Could not fetch the image... Host: {}, imgUrl: {}, parentUrl: {}", imgInfo[0], imgInfo[1], imgInfo[2]);
                continue;
            }

            if (Config.isSaveImage()) {
                historica.setType(SchemeType.FILE);
                int realImageId = Backend.getRealImageId(path.toUri());
                if (realImageId == -1) {
                    historica.setRealImageId(Backend.getNewImageId(path.toUri(), null, FILE));
                } else {
                    historica.setRealImageId(realImageId);
                }
            } else {
                try {
                    URI realImagePath0 = new URI(imgInfo[1].replaceFirst("\\d+\\.\\d+\\.\\d+\\.\\d+", imgInfo[0]));
                    int realImageId = Backend.getRealImageId(realImagePath0);
                    if (realImageId == -1) {
                        historica.setRealImageId(Backend.getNewImageId(realImagePath0,
                                new URI(imgInfo[2]), historica.getType()));
                    } else {
                        historica.setRealImageId(realImageId);
                    }
                } catch (URISyntaxException ex) {
                    logger.error("Host: {}, imgUrl: {}, parentUrl: {}", imgInfo[0], imgInfo[1], imgInfo[2]);
                    logger.error("", ex);
                }
            }

            historica.setHistoricaId(Backend.getNewHistoricaId());
            historica.setMajor(major);
            historica.setMinor(minor);
            historica.setRevision(revision);
            historicas.add(historica);
        }

        Backend b = new Backend();
        b.storeHistorica(historicas, "import");

        state.setImporting(false);
    }

    private List<Historica> fetchHistorica(Map<String, List<Request>> requests) {
        List<Historica> historicas = null;
        try {
            ByteArrayEntity entity = new ByteArrayEntity(new ObjectMapper().writeValueAsBytes(requests));
            byte[] rbody = CentralHttpClient.doPost(HISTORICA_FETCH_ACCESS_POINT, entity, state, 30000);
            if (rbody != null) {
                historicas = new ObjectMapper().readValue(rbody, new TypeReference<List<Historica>>() {
                });
            }
        } catch (NullPointerException ex) {
            logger.error("", ex);
        } catch (IOException ex) {
            logger.error("", ex);
        }
        return historicas;
    }

    private Path definePath(Historica historica) {
        String historicaFullPath = historica.getImageName();
        String baseName = historicaFullPath.substring(historicaFullPath.lastIndexOf(DELIMITER) + 1);
        Path fileName = Paths.get(Config.getRootDir() + fileSeparator + historicaFullPath + "." + historica.getExt().toLowerCase());
        Path path = Paths.get(Config.getRootDir() + fileSeparator + historicaFullPath.substring(0, historicaFullPath.lastIndexOf(DELIMITER)));
        while (true) {
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (Exception e) {
                    logger.error("", e);
                    CommonAlert ca = new CommonAlert();
                    ca.showAnyError(Rsc.get("common_menu_IAT_header_1"), Config.getRootDir() + Rsc.get("common_menu_IAT_content_1"));
                    break;
                }
            }
            if (!Files.exists(fileName)) {
                break;
            }
            baseName = baseName + "_" + RandomStringUtils.randomAlphanumeric(6);
            fileName = Paths.get(path.toString() + fileSeparator + baseName + "." + historica.getExt().toLowerCase());
        }
        return fileName;
    }

    private boolean fetchImage(String[] imgInfos, Path path, int lmod) {
        boolean isSucceeded = false;
        String host = imgInfos[0];
        String imgUrl = imgInfos[1];
        String parentUrl = imgInfos[2];

        CloseableHttpResponse res = null;
        HttpEntity entity = null;
        HttpContext context = HttpClientContext.create();
        HttpGet get = new HttpGet(imgUrl);
        get.addHeader("Host", host);
        get.addHeader("User-Agent", Config.getUserAgent());
        get.addHeader("Connection", "close");
        try {
            logger.debug("httpGet first time... Host: {}, Request: {}", host, get.getURI());
            res = GeneralHttpClient.httpClient.execute(get, context);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                entity = res.getEntity();
                try (InputStream is = entity.getContent();
                        FileOutputStream fos = new FileOutputStream(path.toFile(), false);
                        BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    byte[] b = new byte[4096];
                    int read = 0;
                    while ((read = is.read(b)) != -1) {
                        bos.write(b, 0, read);
                    }
                    bos.flush();
                }
            } else {
                get.reset();
                get.addHeader("Referer", parentUrl);
                logger.debug("httpGet second time... Host: {}, Request: {}", host, get.getURI());
                res = GeneralHttpClient.httpClient.execute(get, context);
                if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    entity = res.getEntity();
                    try (InputStream is = entity.getContent();
                            FileOutputStream fos = new FileOutputStream(path.toFile(), false);
                            BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                        byte[] b = new byte[4096];
                        int read = 0;
                        while ((read = is.read(b)) != -1) {
                            bos.write(b, 0, read);
                        }
                        bos.flush();
                    }
                }
            }

            if (Files.exists(path)) {
                isSucceeded = true;
            }
        } catch (IOException ex) {
            logger.error("", ex);
        } finally {
            try {
                if (entity != null) {
                    EntityUtils.consume(entity);
                }
                if (res != null) {
                    res.close();
                }
            } catch (IOException ex) {
                logger.error("", ex);
            }
        }

        if (isSucceeded) {
            try {
                Files.setLastModifiedTime(path, FileTime.fromMillis(lmodMaker.getLmod(lmod)));
            } catch (IOException e) {
                logger.error("", e);
            }
        }

        return isSucceeded;
    }
}
