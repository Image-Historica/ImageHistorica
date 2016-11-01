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

import com.imagehistorica.cache.TreeCache;
import static com.imagehistorica.util.Constants.DELIMITER;
import static com.imagehistorica.util.Constants.HISTORICA_DIR_ID_SEQ;

import com.imagehistorica.databases.model.HistoricaDir;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Sequence;
import com.sleepycat.je.SequenceConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class HistoricaDirsDB {

    private final Environment env;
    private final EntityStore store;
    private final Sequence seq;
    private final PrimaryIndex<Integer, HistoricaDir> historicaDirsByHistoricaDirId;
    private final SecondaryIndex<String, Integer, HistoricaDir> historicaDirsByHisDirSuffix;
    private final Object mutex = new Object();

    private final byte major;
    private final byte minor;
    private final byte revision;

    private final Logger logger = LoggerFactory.getLogger(HistoricaDirsDB.class);

    public HistoricaDirsDB(Environment env) throws DatabaseException {
        this.env = env;
        this.major = Backend.major;
        this.minor = Backend.minor;
        this.revision = Backend.revision;

        StoreConfig stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        stConf.setDeferredWrite(false);
        store = new EntityStore(this.env, "HistoricaDirsDB", stConf);

        SequenceConfig seqConf = new SequenceConfig();
        seqConf.setAllowCreate(true);
        seqConf.setInitialValue(1);
        store.setSequenceConfig(HISTORICA_DIR_ID_SEQ, seqConf);
        seq = store.getSequence(HISTORICA_DIR_ID_SEQ);

        historicaDirsByHistoricaDirId = store.getPrimaryIndex(Integer.class, HistoricaDir.class);
        historicaDirsByHisDirSuffix = store.getSecondaryIndex(historicaDirsByHistoricaDirId, String.class, "hisDirSuffix");
    }

    public List<HistoricaDir> get() {
        List<HistoricaDir> historicaDirs = new ArrayList<>();
        try (EntityCursor<HistoricaDir> hisDirCur = historicaDirsByHistoricaDirId.entities()) {
            for (HistoricaDir historicaDir : hisDirCur) {
                historicaDirs.add(historicaDir);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return historicaDirs;
    }

    public int get(String suffix, String historicaPathPart) {
        logger.debug("Suffix_1: {}, PathPart_1: {}", suffix, historicaPathPart);
        int historicaDirId = -1;
        try (EntityCursor<HistoricaDir> hisDirCur = historicaDirsByHisDirSuffix.entities(suffix, true, suffix, true)) {
            for (HistoricaDir historicaDir : hisDirCur) {
                if (historicaDir.getHisDirPathPart().equals(historicaPathPart)) {
                    logger.debug("Suffix_2: {}, PathPart_2: {}", suffix, historicaPathPart);
                    historicaDirId = historicaDir.getHistoricaDirId();
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return historicaDirId;
    }

    public HistoricaDir get(int historicaDirId) {
        HistoricaDir historicaDir = null;
        try {
            historicaDir = historicaDirsByHistoricaDirId.get(historicaDirId);
            if (historicaDir == null) {
                logger.info("There's no entry in HistoricaDirsDB...{}", historicaDirId);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return historicaDir;
    }

    public void delete(int historicaDirId) {
        if (historicaDirsByHistoricaDirId.delete(historicaDirId)) {
            logger.debug("Delete... historicaDirId: {}", historicaDirId);
        } else {
            logger.debug("Could not delete... historicaDirId: {}", historicaDirId);
        }
    }

    public void changeSuffix(int historicaDirId, String suffix) {
        Transaction txn = null;
        try {
            txn = env.beginTransaction(null, null);
            HistoricaDir historicaDir = historicaDirsByHistoricaDirId.get(txn, historicaDirId, LockMode.RMW);
            historicaDir.setHisDirSuffix(suffix);
            historicaDirsByHistoricaDirId.putNoReturn(txn, historicaDir);
            txn.commit();
        } catch (Exception e) {
            logger.error("", e);
            if (txn != null) {
                txn.abort();
            }
        }
    }

    public void changePathPart(int historicaDirId, String pathPart) {
        Transaction txn = null;
        try {
            txn = env.beginTransaction(null, null);
            HistoricaDir historicaDir = historicaDirsByHistoricaDirId.get(txn, historicaDirId, LockMode.RMW);
            historicaDir.setHisDirPathPart(pathPart);
            historicaDirsByHistoricaDirId.putNoReturn(txn, historicaDir);
            txn.commit();
        } catch (Exception e) {
            logger.error("", e);
            if (txn != null) {
                txn.abort();
            }
        }
    }

    public Set<String> suggest(String prefix) {
        logger.debug("Suggest()...{}", prefix);
        Set<String> historicaDirs = new TreeSet<>();
        char[] ca = prefix.toCharArray();
        logger.debug("Char1: {}", ca);
        final int lastCharIndex = ca.length - 1;
        ca[lastCharIndex]++;
        logger.debug("Char2: {}", ca);
        try (EntityCursor<HistoricaDir> hisDirCur = historicaDirsByHisDirSuffix.entities(prefix, true, String.valueOf(ca), false)) {
            for (HistoricaDir historicaDir : hisDirCur) {
                String suffix = historicaDir.getHisDirSuffix();
                if (!historicaDirs.contains(suffix)) {
                    historicaDirs.add(suffix);
                }
                if (historicaDirs.size() > 19) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return historicaDirs;
    }

    public List<HistoricaDir> search(String searchText) {
        logger.debug("SearchText: {}", searchText);
        List<HistoricaDir> historicaDirs = new ArrayList<>();
        try (EntityCursor<HistoricaDir> hisDirCur = historicaDirsByHisDirSuffix.entities(searchText, true, searchText, true)) {
            for (HistoricaDir historicaDir : hisDirCur) {
                if (!historicaDirs.contains(historicaDir)) {
                    historicaDirs.add(historicaDir);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return historicaDirs;
    }

    public List<HistoricaDir> getExistingHistoricaDirs(Map<String, Integer[]> hisDirPaths) {
        synchronized (mutex) {
            List<HistoricaDir> existingHisDirs = new ArrayList<>();
            String hisDirSuffix = null;
            String hisDirPathPart = null;
            try {
                for (Entry<String, Integer[]> hisDirPath : hisDirPaths.entrySet()) {
                    String path = hisDirPath.getKey();
                    logger.debug("Path: {}", path);
                    Integer[] meaningIds = hisDirPath.getValue();
                    if (path.contains(DELIMITER)) {
                        // Branches
                        hisDirSuffix = path.substring(path.lastIndexOf(DELIMITER)).replaceAll(DELIMITER, "");
                        hisDirPathPart = path.substring(0, path.lastIndexOf(DELIMITER) + 1);
                    } else {
                        // Root of each category
                        hisDirSuffix = path;
                        hisDirPathPart = "";
                    }

                    try (EntityCursor<HistoricaDir> hisDirCur = historicaDirsByHisDirSuffix.entities(hisDirSuffix, true, hisDirSuffix, true)) {
                        for (HistoricaDir historicaDir : hisDirCur) {
                            if (historicaDir.getHisDirPathPart().equals(hisDirPathPart)) {
                                if (meaningIds != null && meaningIds.length > 0) {
                                    if (historicaDir.getMeaningId() == meaningIds[meaningIds.length - 1]) {
                                        existingHisDirs.add(historicaDir);
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            } catch (StringIndexOutOfBoundsException e) {
                logger.error("", e);
            } catch (NullPointerException e) {
                logger.error("", e);
            } catch (Exception e) {
                logger.error("", e);
            }
            return existingHisDirs;
        }
    }

    public List<HistoricaDir> getNewHistoricaDirs(Map<String, Integer[]> newHisDirPaths) {
        TreeCache treeCache = TreeCache.getInstance();
        synchronized (mutex) {
            List<HistoricaDir> historicaDirs = new ArrayList<>();
            HistoricaDir historicaDir;
            int historicaDirId = 0;
            String hisDirSuffix = null;
            String hisDirPathPart = null;
            try {
                for (Entry<String, Integer[]> newHisDirPath : newHisDirPaths.entrySet()) {
                    String path = newHisDirPath.getKey();
                    int numOfHierarchy = newHisDirPath.getValue() == null ? 0 : newHisDirPath.getValue().length;
                    int meaningId = 0;
                    if (path.contains(DELIMITER)) {
                        String[] branches = path.split(DELIMITER);
                        for (String branch : branches) {
                            logger.debug("Branch: {}", branch);
                        }

                        for (int i = branches.length; i > 0; i--) {
                            numOfHierarchy--;
                            meaningId = numOfHierarchy > -1 ? newHisDirPath.getValue()[numOfHierarchy] : 0;

                            // Make new path to check whether it is existing.
                            String newPath = "";
                            for (int j = 0; j < i; j++) {
                                newPath = newPath + branches[j] + DELIMITER;
                            }
                            newPath = newPath.substring(0, newPath.lastIndexOf(DELIMITER));
                            logger.debug("NewPath: {}", newPath);

                            String[] changedHisDir = Backend.changeHistoricaPath(newPath);
                            hisDirPathPart = changedHisDir[0];
                            hisDirSuffix = changedHisDir[1];

                            boolean isExisting = false;
                            try (EntityCursor<HistoricaDir> hisDirCur = historicaDirsByHisDirSuffix.entities(hisDirSuffix, true, hisDirSuffix, true)) {
                                for (HistoricaDir h : hisDirCur) {
                                    if (h.getHisDirPathPart().equals(hisDirPathPart)) {
                                        logger.debug("#getNewHistoricaDirs() Suffix: {}, PathPart(): {}", hisDirSuffix, hisDirPathPart);
                                        historicaDirs.add(h);
                                        if (h.getMeaningId() == 0 && meaningId != 0) {
                                            h.setMeaningId(meaningId);
                                            historicaDirsByHistoricaDirId.putNoReturn(h);
                                        }
                                        isExisting = true;
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("", e);
                            }

                            if (isExisting) {
                                continue;
                            }

                            // Branches
                            historicaDirId = (int) seq.get(null, 1);
                            logger.debug("New historicaDirId     : {}", historicaDirId);
                            logger.debug("New pathPart   : {}", hisDirPathPart);
                            logger.debug("New suffix   : {}", hisDirSuffix);

                            historicaDir = new HistoricaDir(historicaDirId, hisDirSuffix, hisDirPathPart, meaningId, major, minor, revision);
                            if (historicaDirsByHistoricaDirId.putNoOverwrite(historicaDir)) {
                                logger.info("Inserted new historicaDir... id: {}, suffix: {}, pathpart: {}, meaningId: {}", historicaDir.getHistoricaDirId(),
                                        historicaDir.getHisDirSuffix(), historicaDir.getHisDirPathPart(), historicaDir.getMeaningId());
                                historicaDirs.add(historicaDir);
                                if (meaningId != 0) {
                                    treeCache.putHistoricaMeaningsMap(historicaDirId, meaningId);
                                }
                            } else {
                                logger.error("Already entry in the HistoricaDirsDB: {}", path);
                                break;
                            }
                        }
                    } else {
                        // Root of each category
                        boolean isExisting = false;
                        try (EntityCursor<HistoricaDir> hisDirCur = historicaDirsByHisDirSuffix.entities(path, true, path, true)) {
                            for (HistoricaDir h : hisDirCur) {
                                if (h.getHisDirPathPart().equals("")) {
                                    logger.debug("#getNewHistoricaDirs() Suffix: {}, PathPart(): {}", hisDirSuffix, hisDirPathPart);
                                    historicaDirs.add(h);
                                    isExisting = true;
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            logger.error("", e);
                        }

                        if (isExisting) {
                            continue;
                        }

                        historicaDirId = (int) seq.get(null, 1);
                        hisDirSuffix = path;
                        hisDirPathPart = "";

                        logger.debug("Root historicaDirId     : {}", historicaDirId);
                        logger.debug("Root Suffix   : {}", hisDirSuffix);

                        meaningId = newHisDirPath.getValue()[0];
                        historicaDir = new HistoricaDir(historicaDirId, hisDirSuffix, hisDirPathPart, meaningId, major, minor, revision);
                        if (historicaDirsByHistoricaDirId.putNoOverwrite(historicaDir)) {
                            logger.info("Inserted new historicaDir... id: {}, suffix: {}, pathpart: {}, meaningId: {}", historicaDir.getHistoricaDirId(),
                                    historicaDir.getHisDirSuffix(), historicaDir.getHisDirPathPart(), historicaDir.getMeaningId());
                            historicaDirs.add(historicaDir);
                            if (meaningId != 0) {
                                treeCache.putHistoricaMeaningsMap(historicaDirId, meaningId);
                            }
                        } else {
                            logger.error("Already entry in the HistoricaDirsDB: {}", path);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Exception thrown while putting HistoricaDirsDB", e);
            }

            return historicaDirs;
        }
    }

    public HistoricaDir getNewHistoricaDir(String hisDirPath) {
        List<HistoricaDir> historicaDirs = getNewHistoricaDirs(new HashMap<String, Integer[]>() {
            {
                put(hisDirPath, new Integer[]{0});
            }
        });
        HistoricaDir historicaDir = null;
        if (!historicaDirs.isEmpty()) {
            historicaDir = historicaDirs.get(0);
        }
        return historicaDir;
    }

    public List<HistoricaDir> changePathToHisDir(Map<String, Integer[]> hisDirPaths) {
        synchronized (mutex) {
            Map<String, Integer[]> newHisDirPaths = new HashMap<>(hisDirPaths);
            List<HistoricaDir> existingHisDirs = getExistingHistoricaDirs(hisDirPaths);
            if (!existingHisDirs.isEmpty()) {
                existingHisDirs.stream().map((historicaDir) -> historicaDir.getHisDirPathPart() + historicaDir.getHisDirSuffix())
                        .filter((historicaFullPath) -> (newHisDirPaths.containsKey(historicaFullPath)))
                        .forEach((historicaFullPath) -> {
                            newHisDirPaths.remove(historicaFullPath);
                        });
            }

            List<HistoricaDir> historicaDirs = new ArrayList<>();
            if (!newHisDirPaths.isEmpty()) {
                historicaDirs = getNewHistoricaDirs(newHisDirPaths);
            }

            historicaDirs.addAll(existingHisDirs);
            return historicaDirs;
        }
    }

    public void close() {
        try {
            store.sync();
            store.close();
        } catch (DatabaseException e) {
            logger.error("DatabaseException thrown while closing HistoricaDirsDB...", e);
        } catch (Exception e) {
            logger.error("Exception thrown while closing HistoricaDirsDB...", e);
        }
    }
}
