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

import com.imagehistorica.common.state.ImageState;
import java.net.URI;

import javafx.scene.image.Image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class ImagesMapDispImpl extends DispImpl {

    private boolean isSkipReq = false;
    private ImageState imageState = ImageState.getInstance();
    private final Logger logger = LoggerFactory.getLogger(ImagesMapDispImpl.class);

    @Override
    public void implOpen() {
    }

    @Override
    public void implProcess() {
        if (!isSkipReq) {
            logger.debug("[implProcess] Start...");
            while (true) {
                String realImagePath = imageState.getRealImagePath();
                if (realImagePath == null) {
                    isSkipReq = true;
                    break;
                }
                System.out.println("Called uri.create...");
                Image image = new Image(URI.create(realImagePath).toString());
                imageState.setImage(realImagePath, image);
            }
        }
        logger.debug("[implProcess] End...");
    }

    @Override
    public void implClose() {
    }
}
