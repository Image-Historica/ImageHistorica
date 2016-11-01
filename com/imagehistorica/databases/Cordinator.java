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

import static com.imagehistorica.util.Constants.IMPORTED_REQUEST;
import static com.imagehistorica.util.Constants.PROCESSED_REQUEST;
import static com.imagehistorica.util.Constants.SCHEDULED_REQUEST;

import com.imagehistorica.databases.model.Request;
import com.imagehistorica.databases.model.Historica;

import java.util.List;
import java.util.ArrayList;

import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class Cordinator {

    private Environment env;
    private Queues queues;
    private QueuesInProcDB queuesInProc;
    private StatisticsDB statisticsDb;
    private final Object mutex = new Object();
    private final Logger logger = LoggerFactory.getLogger(Cordinator.class);

    public Cordinator(Environment env) {
        this.env = env;
        this.statisticsDb = new StatisticsDB(env);
        try {
            queues = new Queues(env, "WaitingImagesDB");
            queuesInProc = new QueuesInProcDB(env);
            reschedule();
        } catch (Exception e) {
            logger.error("Error while initializing the queue: {}", Thread.currentThread().getName(), e);
            queues = null;
        }
    }

    public void reschedule() {
        synchronized (mutex) {
            long numInProcessImages = queuesInProc.getLength();
            logger.info("getQueueLength(): {}", getQueueLength());
            logger.info("getNumOfAssignedImages(): {}", getNumOfAssignedImages());
            logger.info("getNumOfProcessedImages(): {}", getNumOfProcessedImages());

            try {
                if (numInProcessImages > 0) {
                    logger.info("Rescheduling {} images from previous session...", numInProcessImages);
                    List<Request> requests = queuesInProc.get(0);
                    while (!requests.isEmpty()) {
                        queues.put(requests);
                        queuesInProc.delete(requests);
                        requests = queuesInProc.get(0);
                        logger.debug("Reqs: {}", requests.size());
                    }
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    public void schedule(List<Request> requests) {
        synchronized (mutex) {
            int newScheduledPage = 0;
            try {
                queues.put(requests);
                newScheduledPage = newScheduledPage + requests.size();
            } catch (Exception e) {
                logger.error("Error while putting the image in the queue: {}", Thread.currentThread().getName(), e);
            }
            if (newScheduledPage > 0) {
                statisticsDb.increment(SCHEDULED_REQUEST, newScheduledPage);
            }
        }
    }

    public List<Request> getNextReq(int procUnitMaking) {
        synchronized (mutex) {
            List<Request> waitingImages = new ArrayList<>();
            try {
                waitingImages = queues.get(procUnitMaking);
                if (waitingImages.isEmpty()) {
                    return waitingImages;
                }
                queues.delete(waitingImages);
                queuesInProc.put(waitingImages);
            } catch (Exception e) {
                logger.error("Error while getting next images", e);
            }
            return waitingImages;
        }
    }

    public boolean setProcessed(List<Historica> historicas, int processedNum, String type, Transaction txn) {
        synchronized (mutex) {
            boolean isSucceeded = false;
            try {
                switch (type) {
                    case "create":
                        if (statisticsDb.increment(PROCESSED_REQUEST, processedNum, txn)) {
                            logger.debug("Processed deleteReq()...");
                            isSucceeded = queuesInProc.deleteReq(historicas, txn);
                        }
                        break;
                    case "import":
                        isSucceeded = statisticsDb.increment(IMPORTED_REQUEST, processedNum, txn);
                        break;
                }
            } catch (Exception e) {
                logger.error("", e);
            }
            return isSucceeded;
        }
    }

    public long getScheduledReqs() {
        return statisticsDb.getStatCounterMap(SCHEDULED_REQUEST);
    }

    public long getProcessedReqs() {
        return statisticsDb.getStatCounterMap(PROCESSED_REQUEST);
    }

    public int getProcessedNum() {
        return statisticsDb.getProcessedNum();
    }

    public void initializeProcNum() {
        statisticsDb.initializeProcNum();
    }

    public List<Request> getQueue() {
        return queues.getRequests();
    }

    public void deleteQueue(List<Request> requests) {
        queues.deleteRequests(requests);
    }

    public long getQueueLength() {
        return queues.getLength();
    }

    public long getNumOfAssignedImages() {
        return queuesInProc.getLength();
    }

    public long getNumOfProcessedImages() {
        return statisticsDb.getStatCounterMap(PROCESSED_REQUEST);
    }

    public void close() {
        queues.close();
        queuesInProc.close();
        statisticsDb.close();
    }
}
