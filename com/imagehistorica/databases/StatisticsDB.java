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

import com.imagehistorica.databases.model.Statistics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class StatisticsDB {

    private Environment env;
    private EntityStore store;
    private Transaction txn = null;
    private PrimaryIndex<String, Statistics> statisticsByStatItem;
    private ConcurrentMap<String, Long> statCounterMap = new ConcurrentHashMap<>();
    private AtomicInteger processedNum = new AtomicInteger();
    private final Object mutex = new Object();
    private Logger logger = LoggerFactory.getLogger(StatisticsDB.class);

    public StatisticsDB(Environment env) throws DatabaseException {
        this.env = env;

        StoreConfig stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        stConf.setDeferredWrite(false);
        store = new EntityStore(env, "StatisticsDB", stConf);

        statisticsByStatItem = store.getPrimaryIndex(String.class, Statistics.class);

        try (EntityCursor<Statistics> statCursor = statisticsByStatItem.entities()) {
            for (Statistics stat : statCursor) {
                String statItem = stat.getStatItem();
                long statCounter = stat.getStatCounter();
                statCounterMap.put(statItem, statCounter);
            }
        } catch (Exception e) {
            logger.error("Can't initialize StatisticsDB...", e);
        }
    }

    public long getStatCounterMap(String name) {
        synchronized (mutex) {
            return statCounterMap.get(name) == null ? 0 : statCounterMap.get(name);
        }
    }

    public void setStatCounterMap(String name, long value) {
        synchronized (mutex) {
            statCounterMap.put(name, value);
            logger.debug("#setStatCounterMap() - name: {}, value: {}", name, value);
        }
    }

    public boolean setStatValue(String name, long value, Transaction txn) {
        // Commit by Backend#storeHistorica()
        synchronized (mutex) {
            try {
                Statistics stat = new Statistics(name, value);
                statisticsByStatItem.put(txn, stat);
                return true;
            } catch (Exception e) {
                logger.error("", e);
                return false;
            }
        }
    }

    public boolean setStatValue(String name, long value) {
        synchronized (mutex) {
            try {
                txn = env.beginTransaction(null, null);
                Statistics stat = new Statistics(name, value);
                logger.info("#setStatValue() - name: {}, value: {}", name, value);
                statisticsByStatItem.put(txn, stat);
                txn.commit();
                txn = null;
                return true;
            } catch (Exception e) {
                logger.error("", e);
                if (txn != null) {
                    txn.abort();
                    txn = null;
                }
                return false;
            }
        }
    }

    public boolean increment(String name, int addition, Transaction txn) {
        synchronized (mutex) {
            long prevValue = getStatCounterMap(name);
            logger.debug("StatName: {}, prevValue: {}, addition: {}", name, prevValue, addition);
            setStatCounterMap(name, prevValue + addition);
            processedNum.getAndAdd(addition);
            return setStatValue(name, prevValue + addition, txn);
        }
    }

    public boolean increment(String name, long addition) {
        synchronized (mutex) {
            long prevValue = getStatCounterMap(name);
            logger.debug("StatName: {}, prevValue: {}, addition: {}", name, prevValue, addition);
            statCounterMap.put(name, prevValue + addition);
            return setStatValue(name, prevValue + addition);
        }
    }

    public int getProcessedNum() {
        return this.processedNum.get();
    }

    public void initializeProcNum() {
        this.processedNum.set(0);
    }

    public void close() {
        try {
            if (txn != null) {
                txn.abort();
            }
            store.sync();
            store.close();
        } catch (DatabaseException e) {
            logger.error("DatabaseException thrown while trying to close StatisticsDB...", e);
        } catch (Exception e) {
            logger.error("Exception thrown while trying to close StatisticsDB...", e);
        }
    }
}
