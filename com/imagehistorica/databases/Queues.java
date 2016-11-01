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

import com.imagehistorica.databases.model.Request;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class Queues {

    protected String dbName;
    protected Environment env;
    protected EntityStore store;
    protected Transaction txn_delete = null;
    protected Transaction txn_put = null;
    protected PrimaryIndex<Integer, Request> queueByHistoricaId;
    protected final Object mutex = new Object();
    protected Logger logger = LoggerFactory.getLogger(Queues.class);

    public Queues(Environment env, String dbName) throws Exception {
        this.env = env;
        this.dbName = dbName;

        StoreConfig stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        stConf.setDeferredWrite(false);
        store = new EntityStore(this.env, dbName, stConf);

        queueByHistoricaId = store.getPrimaryIndex(Integer.class, Request.class);
    }

    public List<Request> get(int procUnit) throws Exception {
        synchronized (mutex) {
            List<Request> queues = new ArrayList<>();
            try (EntityCursor<Request> queueCur = queueByHistoricaId.entities()) {
                int i = 1;
                for (Request req : queueCur) {
                    queues.add(req);
                    logger.debug("[" + dbName + "]" + " get: " + req.getHistoricaId() + ", " + req.getRealImagePath0());
                    i++;
                    if (procUnit != 0) {
                        if (i > procUnit) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                throw e;
            }
            return queues;
        }
    }

    public void delete(List<Request> queues) throws Exception {
        synchronized (mutex) {
            try {
                txn_delete = env.beginTransaction(null, null);
                queues.stream().forEach((req) -> {
                    queueByHistoricaId.delete(txn_delete, req.getHistoricaId());
                    logger.debug("[" + dbName + "]" + " delete: " + req.getHistoricaId() + ", " + req.getRealImagePath0());
                });
                txn_delete.commit();
                txn_delete = null;
            } catch (Exception e) {
                if (txn_delete != null) {
                    txn_delete.abort();
                    txn_delete = null;
                }
                throw e;
            }
        }
    }

    public void put(List<Request> queues) throws Exception {
        synchronized (mutex) {
            try {
                txn_put = env.beginTransaction(null, null);
                queues.stream().forEach((req) -> {
                    queueByHistoricaId.put(txn_put, req);
                    logger.debug("[" + dbName + "]" + " put: " + req.getHistoricaId() + ", " + req.getRealImagePath0());
                });
                txn_put.commit();
                txn_put = null;
            } catch (DatabaseException e) {
                if (txn_put != null) {
                    txn_put.abort();
                    txn_put = null;
                }
                throw e;
            }
        }
    }

    public long getLength() {
        try {
            return queueByHistoricaId.count();
        } catch (Exception e) {
            logger.error("Error in the DB: {}", Thread.currentThread().getName(), e);
            return -1;
        }
    }

    public List<Request> getRequests() {
        try {
            return get(0);
        } catch (Exception e) {
            logger.error("Error in the DB: {}", Thread.currentThread().getName(), e);
            return null;
        }
    }

    public void deleteRequests(List<Request> queues) {
        try {
            delete(queues);
        } catch (Exception e) {
            logger.error("Error in the DB: {}", Thread.currentThread().getName(), e);
        }
    }

    public void close() {
        try {
            if (txn_delete != null) {
                txn_delete.abort();
            }
            if (txn_put != null) {
                txn_put.abort();
            }
            store.sync();
            store.close();
        } catch (Exception e) {
            logger.error("Error in DB: {}", Thread.currentThread().getName(), e);
        }
    }
}
