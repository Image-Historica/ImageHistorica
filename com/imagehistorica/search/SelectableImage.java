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

import javafx.scene.image.Image;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class SelectableImage extends Selectable {

    private String imgInfo;

    public SelectableImage(String imageId, String imgInfo, Image image) {
        this(imageId, image);
        this.imgInfo = imgInfo;
    }

    public SelectableImage(String imageId, Image image) {
        super(imageId, image);
    }

    public String[] getImgInfos() {
        String[] imgInfos = imgInfo.split("`");
        String host = imgInfos[2];
        String imgUrl;
        String parentUrl;
        if (host.startsWith("|")) {
            imgUrl = "https://" + imgInfos[3];
            host = host.replaceFirst("\\|", "");
        } else {
            imgUrl = "http://" + imgInfos[3];
        }
        if (imgInfos[4].startsWith("|")) {
            parentUrl = "https://" + imgInfos[4];
            parentUrl = parentUrl.replaceFirst("\\|", "");
        } else {
            parentUrl = "http://" + imgInfos[4];
        }
        return new String[]{host, imgUrl, parentUrl};
    }
}
