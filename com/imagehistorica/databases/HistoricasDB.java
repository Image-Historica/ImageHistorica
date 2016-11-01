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

import com.imagehistorica.databases.model.Historica;
import com.imagehistorica.databases.model.RealImage;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
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
public class HistoricasDB {

    private final Environment env;
    private final EntityStore store;
    private final PrimaryIndex<Integer, Historica> historicasByHistoricaId;
    private final SecondaryIndex<Integer, Integer, Historica> historicasByHistoricaDirId;
    private final SecondaryIndex<Integer, Integer, Historica> historicasByRealImageId;
    private final SecondaryIndex<Integer, Integer, Historica> historicasByLmod;
    private final Object mutex = new Object();
    private final Logger logger = LoggerFactory.getLogger(HistoricasDB.class);

    public HistoricasDB(Environment env) throws DatabaseException {
        this.env = env;

        StoreConfig stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        stConf.setDeferredWrite(false);
        store = new EntityStore(this.env, "HistoricasDB", stConf);

        historicasByHistoricaId = store.getPrimaryIndex(Integer.class, Historica.class);
        historicasByHistoricaDirId = store.getSecondaryIndex(historicasByHistoricaId, Integer.class, "historicaDirId");
        historicasByRealImageId = store.getSecondaryIndex(historicasByHistoricaId, Integer.class, "realImageId");
        historicasByLmod = store.getSecondaryIndex(historicasByHistoricaId, Integer.class, "lmod");
    }

    public List<Historica> get() {
        List<Historica> historicas = new ArrayList<>();
        try (EntityCursor<Historica> historicaCur = historicasByHistoricaId.entities()) {
            for (Historica historica : historicaCur) {
                historicas.add(historica);
                logger.debug("[HistoricasDB] get: " + historica.toString());
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return historicas;
    }

    public Historica getByHistoricaId(int historicaId) {
        Historica historica = null;
        try {
            historica = historicasByHistoricaId.get(historicaId);
        } catch (Exception e) {
            logger.error("", e);
        }
        return historica;
    }

    public Historica getByRealImageId(int realImageId) {
        Historica historica = null;
        try {
            historica = historicasByRealImageId.get(realImageId);
        } catch (Exception e) {
            logger.error("", e);
        }
        return historica;
    }

    public List<Historica> getByHistoricaDirId(int historicaDirId) {
        List<Historica> historicas = new ArrayList<>();
        try (EntityCursor<Historica> historicaCur = historicasByHistoricaDirId.entities(historicaDirId, true, historicaDirId, true)) {
            for (Historica historica : historicaCur) {
                historicas.add(historica);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return historicas;
    }

    public void deleteByHistoricaId(int historicaId) {
        if (historicasByHistoricaId.delete(historicaId)) {
            logger.debug("Delete... historicaId: {}", historicaId);
        } else {
            logger.debug("Could not delete or no entry... historicaId: {}", historicaId);
        }
    }

    public void deleteByHistoricaDirId(int historicaDirId) {
        if (historicasByHistoricaDirId.delete(historicaDirId)) {
            logger.debug("Delete... historicaDirId: {}", historicaDirId);
        } else {
            logger.debug("Could not delete or no entry... historicaDirId: {}", historicaDirId);
        }
    }

    public boolean deleteByRealImage(RealImage realImage) {
        synchronized (mutex) {
            boolean isDeleted = false;
            try {
                if (historicasByRealImageId.delete(realImage.getRealImageId())) {
                    isDeleted = true;
                    logger.info("Delete by RealImage... ID: {}, URI: {}", realImage.getRealImageId(), realImage.getRealImagePath0());
                } else {
                    isDeleted = false;
                    logger.info("Could not delete by RealImage... ID: {}, URI: {}", realImage.getRealImageId(), realImage.getRealImagePath0());
                }
            } catch (Exception e) {
                logger.error("", e);
            }

            return isDeleted;
        }
    }

    public boolean put(List<Historica> historicas, Transaction txn) {
        logger.debug("Called historicasDB put...");
        // Commit by Backend#storeHistorica()
        synchronized (mutex) {
            boolean isSucceeded = false;
            for (Historica historica : historicas) {
                try {
                    if (historicasByHistoricaId.putNoOverwrite(txn, historica)) {
                        logger.debug("[HistoricasDB] put: " + historica);
                        isSucceeded = true;
                    } else {
                        logger.error("[HistoricasDB] Can't complete process due to duplicate entry.");
                        isSucceeded = false;
                    }
                } catch (Exception e) {
                    logger.error("Historica's RealImageid: {}", historica.getRealImageId(), e);
                    break;
                }
            }
            return isSucceeded;
        }
    }

    public void increFreqViewed(int historicaId, short addition) {
        Historica historica = historicasByHistoricaId.get(historicaId);
        int freqViewed = historica.getFreqViewed() + addition;
        if (freqViewed > 32767) {
            freqViewed = 32767;
        }
        historica.setFreqViewed((short) freqViewed);
        historicasByHistoricaId.putNoReturn(historica);
    }

    public int count(int historicaDirId) {
        int count = 0;
        try (EntityCursor<Integer> historicaCur = historicasByHistoricaDirId.keys(historicaDirId, true, historicaDirId, true)) {
            for (int i : historicaCur) {
                count++;
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        logger.debug("HistoricaDirId: {}, Count: {}", historicaDirId, count);
        return count;
    }

    public void changeName(int historicaId, String name) {
        Transaction txn = null;
        try {
            txn = env.beginTransaction(null, null);
            Historica historica = historicasByHistoricaId.get(txn, historicaId, LockMode.RMW);
            historica.setImageName(name);
            historicasByHistoricaId.putNoReturn(txn, historica);
            txn.commit();
        } catch (Exception e) {
            logger.error("", e);
            if (txn != null) {
                txn.abort();
            }
        }
    }

    public void changeDirId(int historicaId, int historicaDirId) {
        Transaction txn = null;
        try {
            txn = env.beginTransaction(null, null);
            Historica historica = historicasByHistoricaId.get(historicaId);
            historica.setHistoricaDirId(historicaDirId);
            historicasByHistoricaId.putNoReturn(historica);
            txn.commit();
        } catch (Exception e) {
            logger.error("", e);
            if (txn != null) {
                txn.abort();
            }
        }
    }

    public void changeImageId(int historicaId, int realImageId) {
        Transaction txn = null;
        try {
            txn = env.beginTransaction(null, null);
            Historica historica = historicasByHistoricaId.get(historicaId);
            historica.setRealImageId(realImageId);
            historicasByHistoricaId.putNoReturn(historica);
            txn.commit();
        } catch (Exception e) {
            logger.error("", e);
            if (txn != null) {
                txn.abort();
            }
        }
    }

    public long getCount() {
        try {
            return historicasByHistoricaId.count();
        } catch (Exception e) {
            logger.error("Error in the DB: {}", Thread.currentThread().getName(), e);
            return -1;
        }
    }

    public void close() {
        try {
            store.sync();
            store.close();
        } catch (DatabaseException e) {
            logger.error("DatabaseException thrown while closing HistoricasDB...", e);
        } catch (Exception e) {
            logger.error("Exception thrown while closing HistoricasDB...", e);
        }
    }
}
