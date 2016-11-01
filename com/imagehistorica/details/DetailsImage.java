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

package com.imagehistorica.details;

import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.util.controller.ThreadCreator;
import com.imagehistorica.common.state.ImageState;
import com.imagehistorica.Config;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TreeItem;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class DetailsImage extends ImageView {

    private ImageState imageState = ImageState.getInstance();

    private final Logger logger = LoggerFactory.getLogger(DetailsImage.class);

    public void createImagesMap(Map<Integer, TreeItem<HistoricaProperty>> leafNodes, String lastSelectedItem) {
        logger.debug("Start createImagesMap()...");
        List<String> realImagePaths = new ArrayList<>();

        Image image = new Image(Paths.get(lastSelectedItem).toUri().toString());
        imageState.setImage(lastSelectedItem, image);

        for (Entry<Integer, TreeItem<HistoricaProperty>> leafNode : leafNodes.entrySet()) {
            String realImagePath = leafNode.getValue().getValue().getRealImagePath();
            logger.debug("realImagePath: {}", realImagePath);
            if (imageState.hasExistingEntry(realImagePath) || realImagePath.equals(lastSelectedItem)) {
                continue;
            }
            realImagePaths.add(realImagePath);
        }

        if (!realImagePaths.isEmpty()) {
            imageState.setRealImagePath(realImagePaths);

            ThreadCreator t = new ThreadCreator();
            t.createImagesMap();
        }
    }

    public void createImagesMap(List<String> realImagePathsOfaHisDir) {
        List<String> realImagePaths = new ArrayList<>();

        realImagePathsOfaHisDir.stream().filter((realImagePath) -> !(imageState.hasExistingEntry(realImagePath))).forEach((realImagePath) -> {
            realImagePaths.add(realImagePath);
        });

        if (!realImagePaths.isEmpty()) {
            imageState.setRealImagePath(realImagePaths);

            ThreadCreator t = new ThreadCreator();
            t.createImagesMap();
        }
    }

    public static ImageView getImageView() {
        boolean isHighQualityImage = Config.isHighQualityImage();
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        if (isHighQualityImage) {
            imageView.setSmooth(true);
        }

        return imageView;
    }
}
