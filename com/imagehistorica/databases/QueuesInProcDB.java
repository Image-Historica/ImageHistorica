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

import java.util.List;

import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;
import com.imagehistorica.databases.model.Historica;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class QueuesInProcDB extends Queues {

    public QueuesInProcDB(Environment env) throws Exception {
        super(env, "QueuesInProcDB");
        long imageCount = getLength();
        if (imageCount > 0) {
            logger.info("Loaded {} images from " + dbName + " that don't be processed in the previous session yet...", imageCount);
        }
    }

    public boolean deleteReq(List<Historica> historicas, Transaction txn) {
        synchronized (mutex) {
            try {
                for (Historica historica : historicas) {
                    if (!queueByHistoricaId.delete(txn, historica.getHistoricaId())) {
                        throw new Exception("Could not remove: {} from list of requests." + queueByHistoricaId.get(historica.getHistoricaId()));
                    }
                }
            } catch (Exception e) {
                logger.error("Error in the QueuesInProcessDB, {}", e.getMessage());
                return false;
            }

            return true;
        }
    }
}
