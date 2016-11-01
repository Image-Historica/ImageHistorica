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

package com.imagehistorica.search;

import com.imagehistorica.databases.model.Historica;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.image.Image;
import javafx.util.Pair;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class SearchImage {

    private final ConcurrentLinkedDeque<String> urls = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Pair<String, Image>> imagesCentral;
    private final ConcurrentLinkedDeque<Historica> imagesBackend;
    private final ConcurrentMap<String, Byte> failedReqs = new ConcurrentHashMap<>();
    private final AtomicInteger tryNum = new AtomicInteger(1);
    private final AtomicBoolean inProgress = new AtomicBoolean(false);
    private final String id;
    private final String searchTxt;
    private final boolean isCentral;
    private boolean isEnd = false;

    public SearchImage(String id, String searchTxt, boolean isCentral) {
        this.id = id;
        this.searchTxt = searchTxt;
        this.isCentral = isCentral;
        if (isCentral) {
            imagesCentral = new ConcurrentLinkedDeque<>();
            imagesBackend = null;
        } else {
            imagesCentral = null;
            imagesBackend = new ConcurrentLinkedDeque<>();
        }
    }

    public String peekUrl() {
        return this.urls.peekFirst();
    }

    public String pollUrl() {
        return this.urls.pollFirst();
    }

    public void offerUrl(String url) {
        this.urls.offerLast(url);
    }

    public Pair<String, Image> peekCentral() {
        return this.imagesCentral.peekFirst();
    }

    public Pair<String, Image> pollCentral() {
        return this.imagesCentral.pollFirst();
    }

    public void offerCentral(Pair<String, Image> image) {
        this.imagesCentral.offerLast(image);
    }

    public void clearCentral() {
        this.imagesCentral.clear();
    }

    public Historica peekBackend() {
        return this.imagesBackend.peekFirst();
    }

    public Historica pollBackend() {
        return this.imagesBackend.pollFirst();
    }

    public void offerBackend(Historica image) {
        this.imagesBackend.offerLast(image);
    }

    public void clearBackend() {
        this.imagesBackend.clear();
    }

    public ConcurrentMap<String, Byte> getFailedReqs() {
        return this.failedReqs;
    }

    public void putFailedReqs(String imageId, Byte failedReq) {
        this.failedReqs.put(imageId, failedReq);
    }

    public AtomicInteger getTryNum() {
        return this.tryNum;
    }

    public boolean inProgress() {
        return this.inProgress.get();
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress.set(inProgress);
    }

    public String getId() {
        return this.id;
    }

    public String getSearchTxt() {
        return this.searchTxt;
    }

    public boolean isCentral() {
        return this.isCentral;
    }

    public boolean isEnd() {
        return this.isEnd;
    }

    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }
}
