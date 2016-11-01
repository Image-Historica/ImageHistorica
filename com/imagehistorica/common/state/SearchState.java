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

package com.imagehistorica.common.state;

import com.imagehistorica.search.SearchImage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class SearchState extends State {

    private final ConcurrentMap<String, SearchImage> searchImages = new ConcurrentHashMap<>();
    private final AtomicBoolean isOOM = new AtomicBoolean(false);
    private String curId = null;
    private String curSearchTxt = null;
    private boolean isCentral = true;
    private boolean isImporting = false;

    private static final SearchState state = new SearchState();

    private SearchState() {
    }

    public static SearchState getInstance() {
        return state;
    }

    public ConcurrentMap<String, SearchImage> getSeachImages() {
        return this.searchImages;
    }
    
    public boolean containSearchImages(String id) {
        return this.searchImages.containsKey(id);
    }

    public void putSearchImage(String id, SearchImage searchImage) {
        if (id != null) {
            this.searchImages.putIfAbsent(id, searchImage);
        }
    }

    public void removeSearchImage(String id) {
        if (id != null) {
            this.searchImages.remove(id);
        }
    }

    public String getCurId() {
        return this.curId;
    }

    public void setCurId(String curId) {
        this.curId = curId;
    }

    public String getCurSearchTxt() {
        return this.curSearchTxt;
    }

    public void setCurSearchTxt(String curSearchTxt) {
        this.curSearchTxt = curSearchTxt;
    }

    public boolean isOOM() {
        return this.isOOM.get();
    }

    public void setOOM(boolean isOOM) {
        this.isOOM.set(isOOM);
    }

    public boolean isCentral() {
        return this.isCentral;
    }

    public void setCentral(boolean isCentral) {
        this.isCentral = isCentral;
    }

    public boolean isImporting() {
        return this.isImporting;
    }

    public void setImporting(boolean isImporting) {
        this.isImporting = isImporting;
    }
}
