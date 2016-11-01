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
package com.imagehistorica.databases;

import static com.imagehistorica.util.Constants.HISTORICA_ID_SEQ;
import static com.imagehistorica.util.Constants.REAL_IMAGE_ID_SEQ;
import static com.imagehistorica.util.model.SchemeType.*;

import com.imagehistorica.util.model.RealImageProperty;
import com.imagehistorica.util.model.SchemeType;
import com.imagehistorica.databases.model.Request;
import com.imagehistorica.databases.model.RealImage;
import com.imagehistorica.databases.model.Historica;
import com.imagehistorica.Config;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.CRC32;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Sequence;
import com.sleepycat.je.SequenceConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import javafx.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class RealImagesDB {

    private final Environment env;
    private final EntityStore store;
    private final Sequence seq_realImage;
    private final Sequence seq_historica;
    private final PrimaryIndex<Integer, RealImage> realImageByRealImageId;
    private final SecondaryIndex<Long, Integer, RealImage> realImageByRealImagePathCrc;
    private final Object mutex = new Object();

    private final byte major;
    private final byte minor;
    private final byte revision;

    private final Logger logger = LoggerFactory.getLogger(RealImagesDB.class);

    public RealImagesDB(Environment env) throws DatabaseException {
        this.env = env;
        this.major = Backend.major;
        this.minor = Backend.minor;
        this.revision = Backend.revision;

        StoreConfig stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        stConf.setDeferredWrite(false);
        store = new EntityStore(env, "RealImagesDB", stConf);

        SequenceConfig seqConf = new SequenceConfig();
        seqConf.setAllowCreate(true);
        seqConf.setInitialValue(1);
        store.setSequenceConfig(REAL_IMAGE_ID_SEQ, seqConf);
        seq_realImage = store.getSequence(REAL_IMAGE_ID_SEQ);
        store.setSequenceConfig(HISTORICA_ID_SEQ, seqConf);
        seq_historica = store.getSequence(HISTORICA_ID_SEQ);

        realImageByRealImageId = store.getPrimaryIndex(Integer.class, RealImage.class);
        realImageByRealImagePathCrc = store.getSecondaryIndex(realImageByRealImageId, Long.class, "realImagePathCrc");

        long imageCount = getImageCount();
        if (imageCount > 0) {
            logger.info("Total {} images in RealImagesDB that had been requested...", imageCount);
        }
    }

    public long getImageCount() {
        long count = -1L;
        try {
            count = realImageByRealImageId.count();
        } catch (Exception e) {
            logger.error("Exception thrown while getting Image Count", e);
        }
        return count;
    }

    public RealImage get(int realImageId) {
        RealImage realImage = null;
        try {
            realImage = realImageByRealImageId.get(realImageId);
        } catch (Exception e) {
            logger.error("", e);
        }
        return realImage;
    }

    public int getRealImageId(URI realImagePath) {
        int realImageId = -1;
        long crc = getCRC32(realImagePath);
        if (crc == 0) {
            logger.info("Could not get crc for the path...{}", realImagePath);
            return realImageId;
        }

        try (EntityCursor<RealImage> realImageCur = realImageByRealImagePathCrc.entities(crc, true, crc, true)) {
            for (RealImage realImage : realImageCur) {
                if (realImage.getRealImagePath0().equals(realImagePath.toString())) {
                    realImageId = realImage.getRealImageId();
                    logger.debug("realImageId: {}, path: {}", realImageId, realImage.getRealImagePath0());
                    break;
                }
            }
        } catch (NullPointerException ex) {
            logger.info("No entry imageId: {}", realImagePath.toString());
        } catch (Exception ex) {
            logger.error("", ex);
        }

        return realImageId;
    }

    public boolean delete(int realImageId) {
        if (realImageByRealImageId.delete(realImageId)) {
            logger.info("Deleted realImageId {}", realImageId);
            return true;
        } else {
            logger.info("Could not delete realImageId {}", realImageId);
            return false;
        }
    }

    public String getImagePath(int realImageId) {
        String realImagePath = null;
        try {
            realImagePath = realImageByRealImageId.get(realImageId).getRealImagePath0();
        } catch (NullPointerException e) {
            logger.error("No realImageId '{}'in the RealImagesDB", realImageId, e);
        } catch (Exception e) {
            logger.error("", e);
        }
        return realImagePath;
    }

    public Map<String, String> getImagePaths(List<Historica> historicas, Map<Integer, String> imagePathMap) {
        Map<String, String> realImagePaths = new HashMap<>();
        try {
            for (Historica historica : historicas) {
                String realImagePath = realImageByRealImageId.get(historica.getRealImageId()).getRealImagePath0();
                if (realImagePath != null) {
                    realImagePaths.put(realImagePath, imagePathMap.get(historica.getHistoricaId()));
                } else {
                    throw new NullPointerException();
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return realImagePaths;
    }

    public Map<RealImageProperty, Long> getExistingImageId(Map<RealImageProperty, Long> realImageCrcs) {
        synchronized (mutex) {
            Map<RealImageProperty, Long> existingRealImages = new HashMap<>();
            EntityCursor<RealImage> realImageCur = null;
            try {
                for (Entry<RealImageProperty, Long> e : realImageCrcs.entrySet()) {
                    RealImageProperty prop = e.getKey();
                    long crc = e.getValue();
                    realImageCur = realImageByRealImagePathCrc.entities(crc, true, crc, true);
                    for (RealImage realImage : realImageCur) {
                        if (realImage.getRealImagePath0().equals(prop.getRealImagePath0().toString())) {
                            prop.setId(realImage.getRealImageId());
                            existingRealImages.put(prop, crc);
                            break;
                        }
                    }
                    realImageCur.close();
                }
            } catch (Exception ex) {
                logger.error("", ex);
            } finally {
                if (realImageCur != null) {
                    realImageCur.close();
                }
            }

            return existingRealImages;
        }
    }

    public int getNewHistoricaId() {
        synchronized (mutex) {
            return (int) seq_historica.get(null, 1);
        }
    }

    public int getNewImageId(URI realImagePath0, URI realImagePath1, SchemeType type) {
        synchronized (mutex) {
            long crc = getCRC32(realImagePath0);
            if (crc == 0) {
                logger.error("Could not get crc for the path...{}", realImagePath0);
            }
            int realImageId = (int) seq_realImage.get(null, 1);
            RealImage realImage;
            if (realImagePath1 == null) {
                realImage = new RealImage(realImageId, crc, type, realImagePath0.toString(), null, major, minor, revision);
            } else {
                realImage = new RealImage(realImageId, crc, type, realImagePath0.toString(), realImagePath1.toString(), major, minor, revision);
            }

            if (realImageByRealImageId.putNoOverwrite(realImage)) {
                logger.info("Insert a new realImage for the path...{}", realImagePath0);
                realImageId = realImage.getRealImageId();
            } else {
                logger.error("Could not insert a realImage for the path due to duplicate entry...{}", realImagePath0);
                realImageId = -1;
            }

            return realImageId;
        }
    }

    public List<Request> getNewImageID(Map<RealImageProperty, Long> newImagePaths) {
        synchronized (mutex) {
            List<Request> requests = new ArrayList<>();
            RealImage realImage;
            try {
                for (Entry<RealImageProperty, Long> newImagePath : newImagePaths.entrySet()) {
                    int realImageId = (int) seq_realImage.get(null, 1);
                    RealImageProperty prop = newImagePath.getKey();
                    Long realImagePathCrc = newImagePath.getValue();
                    SchemeType scheme = prop.getType();
                    String realImagePath0 = prop.getRealImagePath0().toString();
                    String realImagePath1 = null;
                    if (scheme == HTTP || scheme == HTTPS) {
                        if (prop.getRealImagePath1() != null) {
                            realImagePath1 = prop.getRealImagePath1().toString();
                        }
                    }

                    /*
                     logger.info("realImageId     : {}", realImageId);
                     logger.info("realImagePathCrc: {}", Long.toHexString(realImagePathCrc));
                     logger.info("scheme          : {}", scheme);
                     logger.info("realImagePath0  : {}", realImagePath0);
                     logger.info("realImagePath1  : {}", realImagePath1);
                     logger.debug("realImagePath0Tmp: {}", prop.getRealImagePath0Tmp());
                     logger.debug("realImagePath1Tmp: {}", prop.getRealImagePath1Tmp());
                     logger.debug("alt              : {}", prop.getAlt());
                     logger.debug("title            : {}", prop.getTitle());
                     logger.debug("snippet          : {}", prop.getSnippet());
                     logger.debug("tmpImageName     : {}", prop.getTmpImageName());
                     */
                    realImage = new RealImage(realImageId, realImagePathCrc, scheme, realImagePath0, realImagePath1, major, minor, revision);

                    if (realImageByRealImageId.putNoOverwrite(realImage)) {
                        Request req = makeRequest(realImageId, scheme, prop);
                        if (req == null) {
                            logger.warn("No support scheme at present... {}", prop.getRealImagePath0Tmp().getScheme());
                            continue;
                        }
                        requests.add(req);
                    } else {
                        logger.error("The entry exists already in the RealImageDB: realImageId: {}, realImagePath0: {}", realImageId, realImagePath0);
                    }
                }
            } catch (Exception e) {
                logger.error("Exception thrown while putting on RealImageDB...", e);
            }
            return requests;
        }
    }

    public Request makeRequest(int realImageId, SchemeType scheme, RealImageProperty prop) {
        Request req = null;
        int historicaId = (int) seq_historica.get(null, 1);
        String realImagePath1 = null;
        if (scheme == HTTP || scheme == HTTPS) {
            if (prop.getRealImagePath1() != null) {
                realImagePath1 = prop.getRealImagePath1().toString();
            }
        }
        if (scheme == FILE && prop.getRealImagePath0Tmp() != null) {
            // Request(int historicaId, int realImageId, SchemeType type, String realImagePath0, String realImagePath1, int lmod, String country, String lang,
            // String alt, String title, String snippet, String url_title, String html, String tmpImageName, byte[] binaryImage)
            String realImagePath1Tmp = null;
            if (prop.getRealImagePath1Tmp() != null) {
                realImagePath1Tmp = prop.getRealImagePath1Tmp().toString();
            }
            switch (prop.getRealImagePath0Tmp().getScheme()) {
                case "http":
                    req = new Request(historicaId, realImageId, HTTP, prop.getRealImagePath0Tmp().toString(), realImagePath1Tmp, prop.getLmod(),
                            Config.getCountry(), Config.getLang(), prop.getAlt(), prop.getTitle(), prop.getSnippet(), prop.getTmpImageName(), prop.getBinaryImage(),
                            major, minor, revision);
                    break;
                case "https":
                    req = new Request(historicaId, realImageId, HTTPS, prop.getRealImagePath0Tmp().toString(), realImagePath1Tmp, prop.getLmod(),
                            Config.getCountry(), Config.getLang(), prop.getAlt(), prop.getTitle(), prop.getSnippet(), prop.getTmpImageName(), prop.getBinaryImage(),
                            major, minor, revision);
                    break;
                default:
                    break;
            }
        } else if (scheme == FILE) {
            req = new Request(historicaId, realImageId, scheme, prop.getRealImagePath0().toString(), prop.getLmod(), Config.getCountry(), Config.getLang(), major, minor, revision);
        } else if (scheme == HTTP || scheme == HTTPS) {
            req = new Request(historicaId, realImageId, scheme, prop.getRealImagePath0().toString(), realImagePath1, prop.getLmod(),
                    Config.getCountry(), Config.getLang(), prop.getAlt(), prop.getTitle(), prop.getSnippet(), prop.getTmpImageName(), prop.getBinaryImage(),
                    major, minor, revision);
        }

        return req;
    }

    public Pair<Map<RealImageProperty, Long>, Map<RealImageProperty, Long>> checkExisting(Set<RealImageProperty> realImageProps) {
        Map<RealImageProperty, Long> realImageCrcs = getCRC32Prop(realImageProps);
        Map<RealImageProperty, Long> existingRealImages = getExistingImageId(realImageCrcs);
        if (!existingRealImages.isEmpty()) {
            logger.debug("#checkExisting() - Images exists...");
            for (Entry<RealImageProperty, Long> existingRealImage : existingRealImages.entrySet()) {
                logger.info("This image is already seen. {}", existingRealImage);
                if (realImageCrcs.containsKey(existingRealImage.getKey())) {
                    realImageCrcs.remove(existingRealImage.getKey());
                }
            }
        } else {
            logger.debug("#checkExisting() - No existing images...");
        }

        Pair<Map<RealImageProperty, Long>, Map<RealImageProperty, Long>> checkResults
                = new Pair<>(existingRealImages, realImageCrcs);

        return checkResults;
    }

    public long getCRC32(URI realImagePath0) {
        long crc32 = 0;
        CRC32 crc = new CRC32();
        try {
            crc.update(realImagePath0.toString().getBytes("UTF-8"));
            crc32 = crc.getValue();
        } catch (UnsupportedEncodingException e) {
            logger.error("Can't encode...", e);
        }

        return crc32;
    }

    public Map<RealImageProperty, Long> getCRC32Prop(Set<RealImageProperty> realImageProps) {
        Map<RealImageProperty, Long> realImageCrcs = new HashMap<>();
        CRC32 crc = new CRC32();
        try {
            for (RealImageProperty prop : realImageProps) {
                crc.update(prop.getRealImagePath0().toString().getBytes("UTF-8"));
                realImageCrcs.put(prop, crc.getValue());
                crc.reset();
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Can't encode...", e);
        }

        return realImageCrcs;
    }

    public void close() {
        try {
            store.sync();
            store.close();
        } catch (DatabaseException e) {
            logger.error("DatabaseException thrown while closing RealImagesDB...", e);
        } catch (Exception e) {
            logger.error("Exception thrown while closing RealImagesDB...", e);
        }
    }
}
