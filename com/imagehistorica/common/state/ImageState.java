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

import com.imagehistorica.cache.ImageCache;
import java.util.HashMap;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class ImageState extends State {

    private final ConcurrentLinkedDeque<String> realImagePaths = new ConcurrentLinkedDeque<>();
    private final ImageCache imageCache = ImageCache.getInstance();
    private final Map<Integer, Short> freqViewed = new HashMap<>();
    private final Object mutex = new Object();

    private static final ImageState imageState = new ImageState();

    private ImageState() {
    }

    public static ImageState getInstance() {
        return imageState;
    }

    public String getRealImagePath() {
        return realImagePaths.poll();
    }

    public void setRealImagePath(List<String> realImagePath) {
        this.realImagePaths.addAll(realImagePath);
    }

    public int getRealImagePathsSize() {
        return this.realImagePaths.size();
    }

    public Image getImage(String realImagePath) {
        synchronized (mutex) {
            return imageCache.getOrDefault(realImagePath, null);
        }
    }

    public void setImage(String realImagePath, Image image) {
        synchronized (mutex) {
            this.imageCache.putIfAbsent(realImagePath, image);
        }
    }

    public boolean hasExistingEntry(String realImagePath) {
        synchronized (mutex) {
            return imageCache.containsKey(realImagePath);
        }
    }

    public Map<Integer, Short> getFreqViewd() {
        return this.freqViewed;
    }

    public void increFreqViewed(int historicaId, int addition) {
        if (this.freqViewed.containsKey(historicaId)) {
            short s = freqViewed.get(historicaId);
            this.freqViewed.replace(historicaId, (short) (s + addition));
        } else {
            this.freqViewed.put(historicaId, (short) addition);
        }
    }

    // for debug
    public ImageCache getImageCache() {
        synchronized (mutex) {
            return this.imageCache;
        }
    }
}
