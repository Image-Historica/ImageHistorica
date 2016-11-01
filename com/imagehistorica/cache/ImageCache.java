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

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.scene.image.Image;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class ImageCache extends LinkedHashMap<String, Image> {

    private static final ImageCache cache = new ImageCache(201, 1.1f, true);

    private ImageCache() {
        super();
    }

    private ImageCache(int i, float f, boolean b) {
        super(i, f, b);
    }

    public static ImageCache getInstance() {
        return cache;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > 200;
    }
}
