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

package com.imagehistorica.cache;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class HistoricaDirCache implements Serializable {
    private static final long serialVersionUID = -1699705327694339977L;

    private ConcurrentMap<Integer, Integer> historicaNumsMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, Integer> historicaMeaningsMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, String> historicaDirsMap = new ConcurrentHashMap<>();

    private static final HistoricaDirCache map = new HistoricaDirCache();

    private HistoricaDirCache() {
    }

    public static HistoricaDirCache getInstance() {
        return map;
    }

    public ConcurrentMap<Integer, Integer> getHistoricaNumsMap() {
        return this.historicaNumsMap;
    }

    public void setHistoricaNumsMap(ConcurrentMap<Integer, Integer> historicaNumsMap) {
        this.historicaNumsMap = historicaNumsMap;
    }

    public ConcurrentMap<Integer, Integer> getHistoricaMeaningsMap() {
        return this.historicaMeaningsMap;
    }

    public void setHistoricaMeaningsMap(ConcurrentMap<Integer, Integer> historicaMeaningsMap) {
        this.historicaMeaningsMap = historicaMeaningsMap;
    }

    public ConcurrentMap<Integer, String> getHistoricaDirMap() {
        return this.historicaDirsMap;
    }

    public void setHistoricaDirsMap(ConcurrentMap<Integer, String> historicaDirsMap) {
        this.historicaDirsMap = historicaDirsMap;
    }
}
