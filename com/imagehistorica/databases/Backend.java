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

import static com.imagehistorica.util.Constants.DELIMITER;

import com.imagehistorica.common.state.AnalyzeState;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.controller.Factory;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.common.state.ImageState;
import com.imagehistorica.controller.ControllerType;
import com.imagehistorica.util.model.HistoricaType;
import com.imagehistorica.util.model.RealImageProperty;
import com.imagehistorica.dispatcher.func.Disp;
import com.imagehistorica.dispatcher.func.DispHistorica;
import com.imagehistorica.databases.model.HistoricaDir;
import com.imagehistorica.databases.model.Historica;
import com.imagehistorica.databases.model.RealImage;
import com.imagehistorica.databases.model.Request;
import com.imagehistorica.dispatcher.func.Dispatcher;
import com.imagehistorica.dispatcher.func.DispRequest;
import com.imagehistorica.dispatcher.func.DispRequestMap;
import com.imagehistorica.dispatcher.func.DispTreeItem;
import com.imagehistorica.dispatcher.func.HistoricaDispatcher;
import com.imagehistorica.dispatcher.func.DocValuesDispatcher;
import com.imagehistorica.dispatcher.func.ImagesMapDispatcher;
import com.imagehistorica.dispatcher.func.SearchDispatcher;
import com.imagehistorica.dispatcher.func.SignatureDispatcher;
import com.imagehistorica.dispatcher.func.TreeItemDispatcher;
import com.imagehistorica.dispatcher.impl.HistoricaDispImpl;
import com.imagehistorica.dispatcher.impl.DocValuesDispImpl;
import com.imagehistorica.dispatcher.impl.ImagesMapDispImpl;
import com.imagehistorica.dispatcher.impl.SearchDispImpl;
import com.imagehistorica.dispatcher.impl.SignatureDispImpl;
import com.imagehistorica.dispatcher.impl.TreeItemDispImpl;
import com.imagehistorica.Config;
import com.imagehistorica.controller.type.Red;
import com.imagehistorica.util.model.SchemeType;
import com.imagehistorica.search.SearchImage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ExecutionException;
import javafx.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class Backend {

    public static boolean hasUpdated;
    public static byte major;
    public static byte minor;
    public static byte revision;
    
    private final AnalyzeState analyzeState = AnalyzeState.getInstance();

    private static final TreeCache treeCache = TreeCache.getInstance();
    private static Cordinator cordinator;
    private static RealImagesDB realImagesDb;
    private static HistoricaDirsDB historicaDirsDb;
    private static HistoricasDB historicasDb;
    private static Environment env;
    private static ExecutorService executorService;

    private static final ConcurrentLinkedDeque<Factory> prevController = new ConcurrentLinkedDeque<>();
    public static final ObservableList<Factory> greenStack = FXCollections.observableArrayList();
    public static final ObservableList<Factory> blueStack = FXCollections.observableArrayList();

    private static final Logger logger = LoggerFactory.getLogger(Backend.class);

    public static void startImageHistoricaDB() {
        File storageDir = null;
        try {
            storageDir = new File(Config.getImageHistoricaDb() + "/ImageHistoricaDB");
            if (!storageDir.exists()) {
                if (!storageDir.mkdir()) {
                    throw new Exception();
                } else {
                    logger.info("Created the database directory: {}", storageDir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            logger.error("Failed creating the database directory: {}", Config.getImageHistoricaDb() + "/ImageHistoricaDB", e);
        }

        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);
        envConfig.setLocking(true);
        envConfig.setLockTimeout(10, TimeUnit.SECONDS);
        envConfig.setConfigParam(EnvironmentConfig.STATS_COLLECT, String.valueOf(Config.isCollectDbStats()));

        env = new Environment(storageDir, envConfig);
        cordinator = new Cordinator(env);
        realImagesDb = new RealImagesDB(env);
        historicaDirsDb = new HistoricaDirsDB(env);
        historicasDb = new HistoricasDB(env);
    }

    public static void stopImageHistoricaDB() {
        try {
            cordinator.close();
            realImagesDb.close();
            historicaDirsDb.close();
            historicasDb.close();
            env.sync();
            env.close();
        } catch (IllegalStateException e) {
            logger.error("Backend closing error...", e);
        } catch (Exception e) {
            logger.error("Can't close database...", e);
        }
    }

    public static void startExecutorService() {
        executorService = Executors.newFixedThreadPool(10, (Runnable r) -> {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });
    }

    public static void stopExecutorService() {
        try {
            logger.debug("Before Shutting down ExecutorService...");
            executorService.shutdown();
            if (!executorService.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        logger.debug("After Shutting down ExecutorService...");
    }

    public void createTreeItem() {
        List<TreeItem<HistoricaProperty>> branchNodes = new ArrayList<>();
        while (treeCache.getTreeCacheListSize() > 0) {
            DispTreeItem treeItemDisp = new TreeItemDispatcher(new TreeItemDispImpl(false));
            TreeItem<HistoricaProperty> branchNode = doCast(submit(treeItemDisp));
            if (branchNode != null) {
                branchNodes.add(branchNode);
            }
        }
        if (!branchNodes.isEmpty()) {
            treeCache.setRootNode(branchNodes);
        }
    }

    public void createNewTreeItem() {
        List<TreeItem<HistoricaProperty>> branchNodes = new ArrayList<>();
        while (treeCache.getNewTreeCacheListSize() > 0) {
            DispTreeItem treeItemDisp = new TreeItemDispatcher(new TreeItemDispImpl(true));
            TreeItem<HistoricaProperty> branchNode = doCast(submit(treeItemDisp));
            if (branchNode != null) {
                branchNodes.add(branchNode);
            }
        }
        if (!branchNodes.isEmpty()) {
            treeCache.setNewTreeItem(branchNodes);
            logger.debug("Set isCreated() to true...");
            logger.debug("Set isMerged() to false...");
            analyzeState.setCreated(true);
            analyzeState.setMerged(false);
        }
        analyzeState.decreNumOfThread();
    }

    public void createImagesMap() {
        ImageState imageState = ImageState.getInstance();
        while (imageState.getRealImagePathsSize() > 0) {
            logger.debug("deque.size: {}", imageState.getRealImagePathsSize());
            Dispatcher imagesMapDisp = new ImagesMapDispatcher(new ImagesMapDispImpl());
            execute(imagesMapDisp);
        }
    }

    public void createSearches(SearchImage searchImage) {
        Dispatcher searchDisp = new SearchDispatcher(new SearchDispImpl(searchImage));
        execute(searchDisp);
    }

    public void createHistoricas() {
        Set<Integer> historicaDirIds = new HashSet<>();
        while (cordinator.getQueueLength() > 0) {
            while (analyzeState.isSuspended()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    logger.debug("", ex);
                }
            }

            if (!analyzeState.isSkipReq()) {
                List<Request> docValResult;
                List<Historica> historicaResult;
                TreeMap<String, List<Request>> signatureResult;
                List<Request> requests = cordinator.getNextReq(Config.getProcUnitInMaking());
                if (requests.isEmpty()) {
                    break;
                }

                ConcurrentMap<Integer, String> map = new ConcurrentHashMap<>();
                requests.stream().forEach((request) -> {
                    int historicaId = request.getHistoricaId();
                    String realImagePath0 = request.getRealImagePath0();
                    map.put(historicaId, realImagePath0);
                });
                analyzeState.putRealImagePaths(map);

                DispRequest docValues = new DocValuesDispatcher(new DocValuesDispImpl(requests));
                docValResult = doCast(submit(docValues));
                DispRequestMap signatures = new SignatureDispatcher(new SignatureDispImpl(docValResult));
                signatureResult = doCast(submit(signatures));
                DispHistorica historicas = new HistoricaDispatcher(new HistoricaDispImpl(signatureResult));
                historicaResult = doCast(submit(historicas));

                if (historicaResult != null && !historicaResult.isEmpty()) {
                    storeHistorica(historicaResult, "create");
                    historicaResult.stream().forEach((historica) -> {
                        historicaDirIds.add(historica.getHistoricaDirId());
                    });
                } else {
                    logger.debug("createHistoricas() empty return...");
                    return;
                }
            }
        }

        if (!historicaDirIds.isEmpty()) {
            historicaDirIds.stream().forEach((historicaDirId) -> {
                treeCache.updateNumOfImages(historicaDirId);
            });
        }
    }

    public void storeHistorica(final List<Historica> historicas, final String type) {
        Map<String, Integer[]> hisDirPaths = new HashMap<>();
        ConcurrentMap<Integer, String> map = new ConcurrentHashMap<>();
        historicas.stream().forEach((historica) -> {
            int historicaId = historica.getHistoricaId();
            String historicaPath = historica.getImageName();
            map.put(historicaId, historicaPath);

            logger.debug("storeHistorica(), path: {}", historicaPath);
            String hisDirPath = historicaPath.substring(0, historicaPath.lastIndexOf(DELIMITER));
            hisDirPaths.putIfAbsent(hisDirPath, historica.getMeaningIds());
        });

        List<HistoricaDir> historicaDirs = changePathToHisDir(hisDirPaths);
        analyzeState.putHistoricaPaths(map);
        analyzeState.putAllHistoricaDirs(historicaDirs);

        Map<Integer, String> historicaDirsMap = new HashMap<>();
        historicas.stream().forEach((historica) -> {
            String fullPathByHistorica = historica.getImageName().substring(0, historica.getImageName().lastIndexOf(DELIMITER));
            String imageName = historica.getImageName().substring(historica.getImageName().lastIndexOf(DELIMITER)).replaceAll(DELIMITER, "");

            historicaDirs.stream().forEach((historicaDir) -> {
                String fullPathByHistoricaDir = historicaDir.getHisDirPathPart() + historicaDir.getHisDirSuffix();
                if (fullPathByHistorica.equals(fullPathByHistoricaDir)) {
                    historica.setHistoricaDirId(historicaDir.getHistoricaDirId());
                    historica.setImageName(imageName);
                    historica.setMeaningIds(null);
                }
                historicaDirsMap.putIfAbsent(historicaDir.getHistoricaDirId(), fullPathByHistoricaDir);
            });
        });

        Transaction txn = null;
        try {
            txn = env.beginTransaction(null, null);
            if (!historicasDb.put(historicas, txn)) {
                throw new Exception("[HistoricasDB] Can't complete process due to duplicate entry.");
            }

            if (!cordinator.setProcessed(historicas, historicas.size(), type, txn)) {
                throw new Exception("[StatisticsDB or inProcessingDB] Can't complete process of type '" + type + "' due to no entry.");
            }

            historicas.stream().forEach((historica) -> {
                synchronized (treeCache) {
                    int prevVal = treeCache.getHistoricaNumsMap(historica.getHistoricaDirId());
                    int prevValNew = treeCache.getHistoricaNumsMapNew(historica.getHistoricaDirId());
                    treeCache.putHistoricaNumsMap(historica.getHistoricaDirId(), prevVal + 1);
                    treeCache.putHistoricaNumsMapNew(historica.getHistoricaDirId(), prevValNew + 1);
                    treeCache.putHistoricaDirsMap(historicaDirsMap);
                }
            });

            txn.commit();
            analyzeState.addHistoricas(historicas);
            analyzeState.setUpdated(true);
            analyzeState.setMerged(false);
            logger.debug("Set isUpdated() to true...");
            logger.debug("Set isMerged() to false...");
        } catch (Exception e) {
            logger.error("", e);
            if (txn != null) {
                txn.abort();
            }
        }
    }

    public static Pair<Map<RealImageProperty, Long>, Map<RealImageProperty, Long>> checkExisting(Set<RealImageProperty> props) {
        return realImagesDb.checkExisting(props);
    }

    public static HistoricaDir getNewHistoricaDir(String hisDirPath) {
        return historicaDirsDb.getNewHistoricaDir(hisDirPath);
    }

    //////////////////////////////////////////////////////////////////
    // Cordinator
    public static List<Request> getQueue() {
        return cordinator.getQueue();
    }

    public static void schedule(Map<RealImageProperty, Long> newImagePaths, Map<RealImageProperty, Long> existingPaths) {
        List<Request> requests = realImagesDb.getNewImageID(newImagePaths);
        if (existingPaths != null) {
            existingPaths.forEach((k, v) -> requests.add(realImagesDb.makeRequest(k.getId(), k.getType(), k)));
        }
        cordinator.schedule(requests);
        if (logger.isDebugEnabled()) {
            logger.debug("getScheduledReqs(): {}", cordinator.getScheduledReqs());
            logger.debug("getNumOfAssignedImages(): {}", cordinator.getNumOfAssignedImages());
            logger.debug("getNumOfProcessedImages(): {}", cordinator.getNumOfProcessedImages());
            logger.debug("getProcessedReqs(): {}", cordinator.getProcessedReqs());
            logger.debug("getQueueLength(): {}", cordinator.getQueueLength());
        }
    }

    public static void reschedule() {
        cordinator.reschedule();
    }

    public static void deleteRequests(List<Request> requests) {
        cordinator.deleteQueue(requests);
    }

    public static long getQueueLength() {
        return cordinator.getQueueLength();
    }

    public static int getProcessedNum() {
        return cordinator.getProcessedNum();
    }

    public static void initializeProcNum() {
        cordinator.initializeProcNum();
    }

    public static long getProcessedReqs() {
        return cordinator.getProcessedReqs();
    }

    //////////////////////////////////////////////////////////////////
    // HistoricasDB
    public static int count(int historicaDirId) {
        return historicasDb.count(historicaDirId);
    }

    public static List<Historica> getHistoricas() {
        return historicasDb.get();
    }

    public static List<Historica> getHistoricasByHistoricaDirId(int historicaDirId) {
        return historicasDb.getByHistoricaDirId(historicaDirId);
    }

    public static Historica getHistoricaByHistoricaId(int historicaId) {
        return historicasDb.getByHistoricaId(historicaId);
    }

    public static Historica getHistoricaByRealImageId(int realImageId) {
        return historicasDb.getByRealImageId(realImageId);
    }

    public static void deleteHistoricaByHistoricaId(int historicaId) {
        historicasDb.deleteByHistoricaId(historicaId);
    }

    public static void deleteHistoricaByHistoricaDirId(int historicaDirId) {
        historicasDb.deleteByHistoricaDirId(historicaDirId);
    }

    public static boolean deleteHistoricaByRealImage(RealImage realImage) {
        return historicasDb.deleteByRealImage(realImage);
    }

    public static void changeNameOfHistorica(int historicaId, String name) {
        historicasDb.changeName(historicaId, name);
    }

    public static void increFreqViewed(int historicaId, short addition) {
        historicasDb.increFreqViewed(historicaId, addition);
    }

    public static void changeDirIdOfHistorica(int historicaId, int historicaDirId) {
        historicasDb.changeDirId(historicaId, historicaDirId);
    }

    public static void changeImageIdOfHistorica(int historicaId, int realImageId) {
        historicasDb.changeImageId(historicaId, realImageId);
    }

    //////////////////////////////////////////////////////////////////
    // RealImagesDB
    public static RealImage getRealImage(int realImageId) {
        return realImagesDb.get(realImageId);
    }

    public static int getRealImageId(URI realImagePath) {
        return realImagesDb.getRealImageId(realImagePath);
    }

    public static String getImagePath(int realImageId) {
        return realImagesDb.getImagePath(realImageId);
    }

    public static boolean deleteRealImage(int realImageId) {
        return realImagesDb.delete(realImageId);
    }

    public static int getNewHistoricaId() {
        return realImagesDb.getNewHistoricaId();
    }

    public static int getNewImageId(URI realImagePath0, URI realImagePath1, SchemeType type) {
        return realImagesDb.getNewImageId(realImagePath0, realImagePath1, type);
    }

    //////////////////////////////////////////////////////////////////
    // HistoricaDirsDB
    public static HistoricaDir getHistoricaDir(int historicaDirId) {
        return historicaDirsDb.get(historicaDirId);
    }

    public static int getHistoricaDirId(String suffix, String historicaPathPart) {
        return historicaDirsDb.get(suffix, historicaPathPart);
    }

    public static List<HistoricaDir> getHistoricaDirs() {
        return historicaDirsDb.get();
    }

    public static void deleteHistoricaDir(int historicaDirId) {
        historicaDirsDb.delete(historicaDirId);
    }

    public static List<HistoricaDir> changePathToHisDir(Map<String, Integer[]> hisDirPaths) {
        return historicaDirsDb.changePathToHisDir(hisDirPaths);
    }

    public static void changeSuffixOfHistoricaDir(int historicaDirId, String suffix) {
        historicaDirsDb.changeSuffix(historicaDirId, suffix);
    }

    public static void changePathPartOfHistoricaDir(int historicaDirId, String pathPart) {
        historicaDirsDb.changePathPart(historicaDirId, pathPart);
    }

    public static Factory getPrevController() {
        return prevController.pollFirst();
    }

    public static Factory checkPrevController() {
        return prevController.peekFirst();
    }

    public static void setPrevController(Factory controller) {
        prevController.offerFirst(controller);
    }

    public static Factory getNextController(Factory controller) {
        Factory factory = null;
        int index;
        switch (controller.getControllerType()) {
            case GREEN:
                index = greenStack.size() - 1;
                if (index > -1) {
                    factory = greenStack.get(index);
                    logger.debug("getNextController()...greenStack: {}", factory.getClass().getName());
                    greenStack.remove(index);
                }
                break;
            case BLUE:
                index = blueStack.size() - 1;
                if (index > -1) {
                    factory = blueStack.get(index);
                    logger.debug("getNextController()...blueStack: {}", factory.getClass().getName());
                    blueStack.remove(index);
                }
                break;
            case RED:
                ControllerType prev = ((Red) checkPrevController()).getPrevController().getControllerType();
                if (prev == ControllerType.GREEN) {
                    index = greenStack.size() - 1;
                    if (index > -1) {
                        factory = greenStack.get(index);
                        logger.debug("getNextController()...greenStack: {}", factory.getClass().getName());
                        greenStack.remove(index);
                    }
                } else if (prev == ControllerType.BLUE) {
                    index = blueStack.size() - 1;
                    if (index > -1) {
                        factory = blueStack.get(index);
                        logger.debug("getNextController()...blueStack: {}", factory.getClass().getName());
                        blueStack.remove(index);
                    }
                }
                break;
            case YELLOW:
                if (checkPrevController().getControllerType() == ControllerType.GREEN) {
                    index = greenStack.size() - 1;
                    if (index > -1) {
                        factory = greenStack.get(index);
                        logger.debug("getNextController()...greenStack: {}", factory.getClass().getName());
                        greenStack.remove(index);
                    }
                } else if (checkPrevController().getControllerType() == ControllerType.BLUE) {
                    index = blueStack.size() - 1;
                    if (index > -1) {
                        factory = blueStack.get(index);
                        logger.debug("getNextController()...blueStack: {}", factory.getClass().getName());
                        blueStack.remove(index);
                    }
                } else if (checkPrevController().getControllerType() == ControllerType.RED) {
                    Red c = (Red) checkPrevController();
                    if (c.getPrevController().getControllerType() == ControllerType.GREEN) {
                        index = greenStack.size() - 1;
                        if (index > -1) {
                            factory = greenStack.get(index);
                            logger.debug("getNextController()...greenStack: {}", factory.getClass().getName());
                            greenStack.remove(index);
                        }
                    } else if (c.getPrevController().getControllerType() == ControllerType.BLUE) {
                        index = blueStack.size() - 1;
                        if (index > -1) {
                            factory = blueStack.get(index);
                            logger.debug("getNextController()...blueStack: {}", factory.getClass().getName());
                            blueStack.remove(index);
                        }
                    }
                }
                break;
        }

        return factory;
    }

    public static Factory checkNextController(Factory controller) {
        Factory factory = null;
        int index;
        switch (controller.getControllerType()) {
            case GREEN:
                index = greenStack.size() - 1;
                if (index > -1) {
                    factory = greenStack.get(index);
                    logger.debug("checkNextController()...greenStack: {}", factory.getClass().getName());
                }
                break;
            case BLUE:
                index = blueStack.size() - 1;
                if (index > -1) {
                    factory = blueStack.get(index);
                    logger.debug("checkNextController()...blueStack: {}", factory.getClass().getName());
                }
                break;
            case RED:
                if (checkPrevController().getControllerType() == ControllerType.GREEN) {
                    index = greenStack.size() - 1;
                    if (index > -1) {
                        factory = greenStack.get(index);
                        logger.debug("checkNextController()...greenStack: {}", factory.getClass().getName());
                    }
                } else if (checkPrevController().getControllerType() == ControllerType.BLUE) {
                    index = blueStack.size() - 1;
                    if (index > -1) {
                        factory = blueStack.get(index);
                        logger.debug("checkNextController()...blueStack: {}", factory.getClass().getName());
                    }
                }
                break;
            case YELLOW:
                if (checkPrevController().getControllerType() == ControllerType.GREEN) {
                    index = greenStack.size() - 1;
                    if (index > -1) {
                        factory = greenStack.get(index);
                        logger.debug("checkNextController()...greenStack: {}", factory.getClass().getName());
                    }
                } else if (checkPrevController().getControllerType() == ControllerType.BLUE) {
                    index = blueStack.size() - 1;
                    if (index > -1) {
                        factory = blueStack.get(index);
                        logger.debug("checkNextController()...blueStack: {}", factory.getClass().getName());
                    }
                } else if (checkPrevController().getControllerType() == ControllerType.RED) {
                    Red c = (Red) checkPrevController();
                    if (c.getPrevController().getControllerType() == ControllerType.GREEN) {
                        index = greenStack.size() - 1;
                        if (index > -1) {
                            factory = greenStack.get(index);
                            logger.debug("checkNextController()...greenStack: {}", factory.getClass().getName());
                        }
                    } else if (c.getPrevController().getControllerType() == ControllerType.BLUE) {
                        index = blueStack.size() - 1;
                        if (index > -1) {
                            factory = blueStack.get(index);
                            logger.debug("checkNextController()...blueStack: {}", factory.getClass().getName());
                        }
                    }
                }
                break;
        }

        return factory;
    }

    public static void setNextController(Factory controller) {
        int index;
        switch (controller.getControllerType()) {
            case GREEN:
                index = greenStack.size();
                greenStack.add(index, controller);
                logger.debug("setNextController()...greenStack: {}", controller.getClass().getName());
                break;
            case BLUE:
                index = blueStack.size();
                blueStack.add(index, controller);
                logger.debug("setNextController()...blueStack: {}", controller.getClass().getName());
                break;
            case RED:
                if (checkPrevController().getControllerType() == ControllerType.GREEN) {
                    index = greenStack.size();
                    if (index > -1) {
                        greenStack.add(index, controller);
                        logger.debug("setNextController()...greenStack: {}", controller.getClass().getName());
                    }
                } else if (checkPrevController().getControllerType() == ControllerType.BLUE) {
                    index = blueStack.size();
                    if (index > -1) {
                        blueStack.add(index, controller);
                        logger.debug("setNextController()...blueStack: {}", controller.getClass().getName());
                    }
                }
                break;
            case YELLOW:
                if (checkPrevController().getControllerType() == ControllerType.GREEN) {
                    index = greenStack.size();
                    if (index > -1) {
                        greenStack.add(index, controller);
                        logger.debug("setNextController()...greenStack: {}", controller.getClass().getName());
                    }
                } else if (checkPrevController().getControllerType() == ControllerType.BLUE) {
                    index = blueStack.size();
                    if (index > -1) {
                        blueStack.add(index, controller);
                        logger.debug("setNextController()...blueStack: {}", controller.getClass().getName());
                    }
                } else if (checkPrevController().getControllerType() == ControllerType.RED) {
                    Red c = (Red) checkPrevController();
                    if (c.getPrevController().getControllerType() == ControllerType.GREEN) {
                        index = greenStack.size();
                        if (index > -1) {
                            greenStack.add(index, controller);
                            logger.debug("setNextController()...greenStack: {}", controller.getClass().getName());
                        }
                    } else if (c.getPrevController().getControllerType() == ControllerType.BLUE) {
                        index = blueStack.size();
                        if (index > -1) {
                            blueStack.add(index, controller);
                            logger.debug("setNextController()...blueStack: {}", controller.getClass().getName());
                        }
                    }
                }
                break;
        }
    }

    public static void clearNextController(Factory controller) {
        switch (controller.getControllerType()) {
            case GREEN:
                greenStack.clear();
                logger.debug("clearNextController()...greenStack: {}", controller.getClass().getName());
                break;
            case BLUE:
                blueStack.clear();
                logger.debug("clearNextController()...blueStack: {}", controller.getClass().getName());
                break;
            case RED:
                if (checkPrevController().getControllerType() == ControllerType.GREEN) {
                    greenStack.clear();
                    logger.debug("clearNextController()...greenStack: {}", controller.getClass().getName());
                } else if (checkPrevController().getControllerType() == ControllerType.BLUE) {
                    blueStack.clear();
                    logger.debug("clearNextController()...blueStack: {}", controller.getClass().getName());
                }
                break;
            case YELLOW:
                if (checkPrevController().getControllerType() == ControllerType.GREEN) {
                    greenStack.clear();
                    logger.debug("clearNextController()...greenStack: {}", controller.getClass().getName());
                } else if (checkPrevController().getControllerType() == ControllerType.BLUE) {
                    blueStack.clear();
                    logger.debug("clearNextController()...blueStack: {}", controller.getClass().getName());
                } else if (checkPrevController().getControllerType() == ControllerType.RED) {
                    Red c = (Red) checkPrevController();
                    if (c.getPrevController().getControllerType() == ControllerType.GREEN) {
                        greenStack.clear();
                        logger.debug("clearNextController()...greenStack: {}", controller.getClass().getName());
                    } else if (c.getPrevController().getControllerType() == ControllerType.BLUE) {
                        blueStack.clear();
                        logger.debug("clearNextController()...blueStack: {}", controller.getClass().getName());
                    }
                }
                break;
        }
    }

    public static Set<String> suggestHistoricaDirs(String prefix) {
        return historicaDirsDb.suggest(prefix);
    }

    public static List<HistoricaDir> searchHistoricaDirs(String searchText) {
        return historicaDirsDb.search(searchText);
    }

    public static int getHistoricaNumsMap(int historicaDirId) {
        return treeCache.getHistoricaNumsMap(historicaDirId);
    }

    public static int getHistoricaNumsMapNew(int historicaDirId) {
        return treeCache.getHistoricaNumsMapNew(historicaDirId);
    }

    public static String[] changeHistoricaPath(String historicaPath) {
        String[] changedHisDir = new String[2];
        String hisDirSuffix;
        String hisDirPathPart;
        if (historicaPath.contains(DELIMITER)) {
            hisDirSuffix = historicaPath.substring(historicaPath.lastIndexOf(DELIMITER)).replaceAll(DELIMITER, "");
            hisDirPathPart = historicaPath.substring(0, historicaPath.lastIndexOf(DELIMITER) + 1);
        } else {
            hisDirSuffix = historicaPath;
            hisDirPathPart = "";
        }

        changedHisDir[0] = hisDirPathPart;
        changedHisDir[1] = hisDirSuffix;

        return changedHisDir;
    }

    public static void setUpdated(boolean updated) {
        hasUpdated = updated;
    }
    
    public static void setVersion(String newVersion) {
        String[] version = newVersion.split("\\.");
        major = Byte.parseByte(version[0]);
        minor = Byte.parseByte(version[1]);
        revision = Byte.parseByte(version[2]);
    }
    
    public static HistoricaProperty makeHistoricaProperty(int historicaId, HistoricaType type) {
        return makeHistoricaProperty(getHistoricaByHistoricaId(historicaId), type);
    }

    public static HistoricaProperty makeHistoricaProperty(Historica historica, HistoricaType type) {
        RealImage realImage = getRealImage(historica.getRealImageId());
        HistoricaDir historicaDir = getHistoricaDir(historica.getHistoricaDirId());
        HistoricaProperty prop = null;
        if (type == HistoricaType.LEAF) {
            LocalDate date = null;
            try {
                String lmod = String.valueOf(historica.getLmod());
                date = lmod.matches("\\d{8}") ? LocalDate.parse(lmod, DateTimeFormatter.BASIC_ISO_DATE) : LocalDate.of(1999, 01, 01);
            } catch (DateTimeParseException e) {
                date = LocalDate.of(1999, 01, 01);
            }

            String url = historica.getLocationId() != 0 ? historica.getSrcUrl() : "unknown";

            prop = new HistoricaProperty(
                    historica.getHistoricaId(),
                    historica.getHistoricaDirId(),
                    historica.getImageName(),
                    realImage.getRealImagePath0(),
                    historicaDir.getHisDirPathPart() + historicaDir.getHisDirSuffix(),
                    date,
                    (int) historica.getFreqViewed(),
                    historica.getLength(),
                    url,
                    type);
        } else {
            prop = new HistoricaProperty(
                    historicaDir.getHisDirSuffix(),
                    historicaDir.getHisDirPathPart(),
                    historica.getHistoricaDirId(),
                    type,
                    false);
        }

        return prop;
    }

    private void execute(final Dispatcher dispatcher) {
        try {
            executorService.execute(dispatcher);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    private Object submit(final Disp dispatcher) {
        Object o = null;
        try {
            if (dispatcher instanceof DispRequest) {
                o = executorService.submit((DispRequest) dispatcher).get();
            } else if (dispatcher instanceof DispRequestMap) {
                o = executorService.submit((DispRequestMap) dispatcher).get();
            } else if (dispatcher instanceof DispHistorica) {
                o = executorService.submit((DispHistorica) dispatcher).get();
            } else if (dispatcher instanceof DispTreeItem) {
                o = executorService.submit((DispTreeItem) dispatcher).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("", e);
        }
        return o;
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T doCast(Object obj) {
        T castObj = (T) obj;
        return castObj;
    }
}
