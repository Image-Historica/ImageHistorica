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

package com.imagehistorica.databases.model;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

@Entity
public class Statistics {

    @PrimaryKey
    String statItem;
    
    long statCounter = 0;
    
    public Statistics(String statItem, long statCounter) {
        this.statItem = statItem;
        this.statCounter = statCounter;
    }

    private Statistics() {}
    
    public String getStatItem() {
        return this.statItem;
    }
    
    public long getStatCounter() {
        return this.statCounter;
    }
}
