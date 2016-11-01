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
package com.imagehistorica.test;

import com.imagehistorica.databases.model.RealImage;
import com.imagehistorica.databases.model.Request;
import com.imagehistorica.databases.model.Statistics;
import com.imagehistorica.databases.model.Historica;
import com.imagehistorica.databases.model.HistoricaDir;

import java.io.File;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class jeTestMain {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(jeTestMain.class);

        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setConfigParam(EnvironmentConfig.STATS_COLLECT, "false");
        Environment env = new Environment(new File("ImageHistoricaDB"), envConfig);
        StoreConfig stConf = new StoreConfig();
        stConf.setAllowCreate(true);

        EntityStore storeWaiting = new EntityStore(env, "WaitingImagesDB", stConf);
        EntityStore storeProcessing = new EntityStore(env, "QueuesInProcDB", stConf);
        EntityStore storeStat = new EntityStore(env, "StatisticsDB", stConf);
        EntityStore storeRealImage = new EntityStore(env, "RealImagesDB", stConf);
        EntityStore storeHistoricaDir = new EntityStore(env, "HistoricaDirsDB", stConf);
        EntityStore storeHistorica = new EntityStore(env, "HistoricasDB", stConf);

        /* Request Accessors */
        PrimaryIndex<Integer, Request> requestOfWaiting = storeWaiting.getPrimaryIndex(Integer.class, Request.class);
        PrimaryIndex<Integer, Request> requestOfProcessing = storeProcessing.getPrimaryIndex(Integer.class, Request.class);

        /* Statistics Accessors */
        PrimaryIndex<String, Statistics> statisticsByStatItem = storeStat.getPrimaryIndex(String.class, Statistics.class);

        /* RealImage Accessors */
        PrimaryIndex<Integer, RealImage> realImageByRealImageId = storeRealImage.getPrimaryIndex(Integer.class, RealImage.class);
        SecondaryIndex<Long, Integer, RealImage> realImagesByRealImagePathCrc = storeRealImage.getSecondaryIndex(realImageByRealImageId, Long.class, "realImagePathCrc");

        /* HistoricaDir Accessors */
        PrimaryIndex<Integer, HistoricaDir> historicaDirByHistoricaDirId = storeHistoricaDir.getPrimaryIndex(Integer.class, HistoricaDir.class);
        SecondaryIndex<String, Integer, HistoricaDir> historicaDirsByHisDirSuffix = storeHistoricaDir.getSecondaryIndex(historicaDirByHistoricaDirId, String.class, "hisDirSuffix");

        /* Historica Accessors */
        PrimaryIndex<Integer, Historica> historicaByHistoricaId = storeHistorica.getPrimaryIndex(Integer.class, Historica.class);
        SecondaryIndex<Integer, Integer, Historica> historicasByHistoricaDirId = storeHistorica.getSecondaryIndex(historicaByHistoricaId, Integer.class, "historicaDirId");
        SecondaryIndex<Integer, Integer, Historica> historicasByRealImageId = storeHistorica.getSecondaryIndex(historicaByHistoricaId, Integer.class, "realImageId");
        SecondaryIndex<Integer, Integer, Historica> historicasByLmod = storeHistorica.getSecondaryIndex(historicaByHistoricaId, Integer.class, "lmod");

        try (EntityCursor<Request> reqCur1 = requestOfWaiting.entities()) {
            reqCur1.forEach((e) -> logger.info("Waiting... RealImageId: {}, RealImagePath0: {}", e.getRealImageId(), e.getRealImagePath0()));
        } finally {
            storeWaiting.close();
        }

        try (EntityCursor<Request> reqCur2 = requestOfProcessing.entities()) {
            reqCur2.forEach((e) -> logger.info("Processing... RealImageId: {}, RealImagePath0: {}", e.getRealImageId(), e.getRealImagePath0()));
        } finally {
            storeProcessing.close();
        }

        try (EntityCursor<Statistics> statCur = statisticsByStatItem.entities()) {
            statCur.forEach((e) -> logger.info("[Statstics] Stat: {}, Counter: {}", e.getStatItem(), e.getStatCounter()));
        } finally {
            storeStat.close();
        }

        try (EntityCursor<RealImage> realImageCur = realImageByRealImageId.entities()) {
            realImageCur.forEach((e) -> logger.info("[RealImage] ID: {}, CRC: {}, Path0: {}", e.getRealImageId(), e.getRealImagePathCrc(), e.getRealImagePath0()));
        } finally {
            storeRealImage.close();
        }

        try (EntityCursor<HistoricaDir> historicaDirCur = historicaDirByHistoricaDirId.entities()) {
            historicaDirCur.forEach((e) -> logger.info("[HistoricaDir] ID: {}, MeaningID: {}, PathPart: {}, Suffix: {}",
                    e.getHistoricaDirId(), e.getMeaningId(), e.getHisDirPathPart(), e.getHisDirSuffix()));
        } finally {
            storeRealImage.close();
        }

        try (EntityCursor<Historica> historicaCur = historicaByHistoricaId.entities()) {
            historicaCur.forEach((e) -> logger.info(e.toString()));
        } finally {
            storeHistorica.close();
        }
    }
}
