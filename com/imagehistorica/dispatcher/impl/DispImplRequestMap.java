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

package com.imagehistorica.dispatcher.impl;

import com.imagehistorica.databases.model.Request;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public abstract class DispImplRequestMap {

    public TreeMap<String, List<Request>> requests = new TreeMap<>();
    
    public abstract void implOpen();

    public abstract void implProcess();

    public abstract void implClose();
    
    public Map<String, List<Request>> getRequests() {
        return requests;
    }
}
