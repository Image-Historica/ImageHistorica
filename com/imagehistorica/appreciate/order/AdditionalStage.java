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
package com.imagehistorica.appreciate.order;

import static com.imagehistorica.util.Constants.DELIMITER;

import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.common.state.ImageState;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.util.model.HistoricaType;
import com.imagehistorica.details.DetailsImage;
import com.imagehistorica.Config;
import com.imagehistorica.controller.resources.Rsc;
import java.net.URI;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class AdditionalStage {

    private ImageHistoricaController controller;
    private ImageState imageState = ImageState.getInstance();
    private TreeCache treeCache = TreeCache.getInstance();
    private ImageView imageView = DetailsImage.getImageView();

    private DetailsImage keyImage = new DetailsImage();
    private BorderPane bp = new BorderPane();
    private Stage stage = new Stage();
    private Scene scene;
    private TreeItem<HistoricaProperty> lastSelectedItem;
    private List<TreeItem<HistoricaProperty>> treeItems = new ArrayList<>();
    private IntegerProperty numOfPrefetchedImgs = new SimpleIntegerProperty(0);
    private double widthPrimary;
    private double heightPrimary;
    private double xlocation;
    private double ylocation;
    private double widthDisplay;
    private double heightDisplay;
    private boolean isRightSide = false;

    public static ObservableList<Stage> listOfStages = FXCollections.observableArrayList();

    private final Logger logger = LoggerFactory.getLogger(AdditionalStage.class);

    public AdditionalStage(ImageHistoricaController controller, TreeItem<HistoricaProperty> lastSelectedItem) {
        this.controller = controller;
        this.lastSelectedItem = lastSelectedItem;
        this.widthPrimary = this.controller.getStage().getWidth();
        this.heightPrimary = this.controller.getStage().getHeight();
        this.xlocation = this.controller.getStage().getX();
        this.ylocation = this.controller.getStage().getY();
        this.widthDisplay = Screen.getPrimary().getVisualBounds().getWidth();
        this.heightDisplay = Screen.getPrimary().getVisualBounds().getHeight();

        double relativeloc = widthDisplay - (xlocation + widthPrimary);

        logger.debug("xlocation: {}", xlocation);
        logger.debug("widthDisplay: {}", widthDisplay);
        logger.debug("display width - (current loc + stage width): {}", relativeloc);

        if (xlocation > relativeloc) {
            isRightSide = true;
        }
    }

    public void createLayout() {
        HistoricaProperty prop = lastSelectedItem.getValue();
        stage.setTitle(prop.getHistoricaPath() + DELIMITER + prop.getImageName());

        Image image = imageState.getImage(prop.getRealImagePath());
        if (image != null) {
            imageView.setImage(image);
        } else {
            image = new Image(prop.getRealImagePath());
            imageView.setImage(image);
            imageState.setImage(prop.getRealImagePath(), image);
        }

        for (TreeItem<HistoricaProperty> treeItem : lastSelectedItem.getParent().getChildren()) {
            if (treeItem.getValue().getType() == HistoricaType.LEAF) {
                treeItems.add(treeItem);
            }
        }
        Collections.sort(treeItems, new Comparator<TreeItem<HistoricaProperty>>() {
            @Override
            public int compare(TreeItem<HistoricaProperty> o1, TreeItem<HistoricaProperty> o2) {
                return o1.getValue().getImageName().compareTo(o2.getValue().getImageName());
            }
        });

//        doPrefetchImages();
        imageView.fitWidthProperty().bind(bp.widthProperty());
        imageView.fitHeightProperty().bind(bp.heightProperty());
        bp.setCenter(imageView);

        scene = new Scene(bp);
        stage.setScene(this.controller.applyStyleSheet(scene));

        attachEvents();

        stage.setWidth(widthPrimary * Config.getAdditionalStageWidthRatio());
        stage.setHeight(heightPrimary * Config.getAdditionalStageHeightRatio());
        if (listOfStages.isEmpty()) {
            if (isRightSide) {
                stage.setX(xlocation - widthPrimary * Config.getAdditionalStageWidthRatio());
                stage.setY(ylocation);
            } else {
                stage.setX(xlocation + widthPrimary);
                stage.setY(ylocation);
            }
        } else {
            stage.setX(listOfStages.get(listOfStages.size() - 1).getX());
            stage.setY(listOfStages.get(listOfStages.size() - 1).getY() + 300);
        }
        listOfStages.add(stage);
        stage.show();
    }

    private void attachEvents() {
        stage.setOnCloseRequest((e) -> {
            List<Stage> listOfStages_tmp = new ArrayList<>();
            for (Stage s : listOfStages) {
                if (stage == s) {
                    logger.debug("Stage is equal...");
                    continue;
                }
                listOfStages_tmp.add(s);
            }
            listOfStages.clear();
            listOfStages.addAll(listOfStages_tmp);
        });

        scene.setOnKeyReleased((e) -> {
            logger.debug("KeyReleased...{}", e.getCode());
            int index = -1;
            switch (e.getCode()) {
                case LEFT:
                    index = treeItems.indexOf(lastSelectedItem) - 1;
                    if (index > -1) {
                        changeToNextImage(index);
                    }
                    break;
                case RIGHT:
                    index = treeItems.indexOf(lastSelectedItem) + 1;
                    if (index < treeItems.size()) {
                        changeToNextImage(index);
                    }
                    break;
            }
        });

        ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();
        double width_ = imageView.getImage().getWidth();
        double height_ = imageView.getImage().getHeight();

        reset(imageView, width_, height_);
        imageView.setOnMousePressed((e) -> {
            try {
                boolean primaryClicked = e.getButton().equals(MouseButton.PRIMARY);
                boolean secondaryClicked = e.getButton().equals(MouseButton.SECONDARY);

                if (secondaryClicked) {
                    if (stage.isFullScreen()) {
                        stage.setFullScreen(false);
                    }
                }

                if (primaryClicked) {
                    switch (e.getClickCount()) {
                        case 1:
                            Point2D mousePress = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
                            mouseDown.set(mousePress);
                            break;
                        case 2:
                            if (!stage.isFullScreen()) {
                                stage.setFullScreenExitHint(Rsc.get("exitHint"));
                                stage.setFullScreen(true);
                            }
                            break;
                    }
                }
            } catch (NullPointerException ex) {
                logger.debug("setOnMousePressed: {}", ex.getMessage());
            } catch (Exception ex) {
                logger.error("", ex);
            }
        });

        imageView.setOnMouseDragged(e -> {
            try {
                Point2D dragPoint = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
                shift(imageView, dragPoint.subtract(mouseDown.get()));
                mouseDown.set(imageViewToImage(imageView, new Point2D(e.getX(), e.getY())));
            } catch (NullPointerException ex) {
                logger.debug("setOnMouseDragged: {}", ex.getMessage());
            } catch (Exception ex) {
                logger.error("", ex);
            }
        });

        imageView.setOnScroll(e -> {
            double width = imageView.getImage().getWidth();
            double height = imageView.getImage().getHeight();
            double scaleBase = e.isControlDown() ? 10.0 : e.isAltDown() ? 5.0 : 1.0;
            double delta = e.getDeltaY() / 10 * scaleBase;
            Rectangle2D viewport = imageView.getViewport();

            try {
                double scale = clamp(Math.pow(1.01, delta),
                        Math.min(10 / viewport.getWidth(), 10 / viewport.getHeight()),
                        Math.max(width / viewport.getWidth(), height / viewport.getHeight())
                );

                Point2D mouse = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));

                double newWidth = viewport.getWidth() * scale;
                double newHeight = viewport.getHeight() * scale;

                double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale,
                        0, width - newWidth);
                double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale,
                        0, height - newHeight);

                imageView.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
            } catch (NullPointerException ex) {
                logger.debug("setScroll: {}", ex.getMessage());
            } catch (Exception ex) {
                logger.error("", ex);
            }
        });
    }

    private void reset(ImageView imageView, double width, double height) {
        imageView.setViewport(new Rectangle2D(0, 0, width, height));
    }

    private void shift(ImageView imageView, Point2D delta) {
        Rectangle2D viewport = imageView.getViewport();

        double width = imageView.getImage().getWidth();
        double height = imageView.getImage().getHeight();

        double maxX = width - viewport.getWidth();
        double maxY = height - viewport.getHeight();

        double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
        double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

        imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = imageView.getViewport();
        Point2D mouse = null;
        try {
            mouse = new Point2D(viewport.getMinX() + xProportion * viewport.getWidth(),
                    viewport.getMinY() + yProportion * viewport.getHeight());
        } catch (NullPointerException e) {
            logger.debug("imageViewToImage: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("", e);
        }
        return mouse;
    }

    private void changeToNextImage(int index) {
        logger.debug("KeyReleased..., index: {}, treeItem's size: {}", index, treeItems.size());
        lastSelectedItem = treeItems.get(index);
        HistoricaProperty neighborProp = lastSelectedItem.getValue();
        logger.debug("KeyReleased...after: {}", neighborProp.getImageName());

        Image image = imageState.getImage(neighborProp.getRealImagePath());
        if (image != null) {
            imageView.setImage(image);
        } else {
            image = new Image(URI.create(neighborProp.getRealImagePath()).toString());
            imageView.setImage(image);
            imageState.setImage(neighborProp.getRealImagePath(), image);
        }
        imageState.increFreqViewed(neighborProp.getHistoricaId(), 1);
        neighborProp.setFreqViewed(neighborProp.getFreqViewed() + 1);

        stage.setTitle(neighborProp.getImageName());

        double width = image.getWidth();
        double height = image.getHeight();
        reset(imageView, width, height);

//        doPrefetchImages();
    }

    /*
     private void doPrefetchImages() {
     int index = treeItems.indexOf(lastSelectedItem);
     List<String> realImagePaths = new ArrayList<>();

     int totalNums = Config.getPrefetchImagesInStage();
     int halfNums = Config.getPrefetchImagesInStage() / 2;

     // Prefetch images in case of younger number than index
     if (index < treeItems.size() / 2) {
     // Prefetch previous indices
     if (index < halfNums) {
     for (int i = index - 1; i >= 0; i--) {
     realImagePaths.add(treeItems.get(i).getValue().getRealImagePath());
     totalNums--;
     }
     } else {
     for (int i = index - 1; i > index - halfNums; i--) {
     realImagePaths.add(treeItems.get(i).getValue().getRealImagePath());
     totalNums--;
     }
     }

     // Prefetch after indices
     if ((index + totalNums) < treeItems.size()) {
     for (int i = index + 1; i < index + totalNums; i++) {
     realImagePaths.add(treeItems.get(i).getValue().getRealImagePath());
     }
     } else {
     for (int i = index + 1; i < treeItems.size(); i++) {
     realImagePaths.add(treeItems.get(i).getValue().getRealImagePath());
     }
     }

     // Prefetch images in case of older number than index
     } else {
     // Prefetch after indices
     if ((index + halfNums) < treeItems.size()) {
     for (int i = index + 1; i < index + halfNums; i++) {
     realImagePaths.add(treeItems.get(i).getValue().getRealImagePath());
     totalNums--;
     }
     } else {
     for (int i = index + 1; i < treeItems.size(); i++) {
     realImagePaths.add(treeItems.get(i).getValue().getRealImagePath());
     totalNums--;
     }
     }

     // Prefetch previous indices
     if (index < totalNums) {
     for (int i = index - 1; i >= 0; i--) {
     realImagePaths.add(treeItems.get(i).getValue().getRealImagePath());
     }
     } else {
     for (int i = index - 1; i > index - totalNums; i--) {
     realImagePaths.add(treeItems.get(i).getValue().getRealImagePath());
     }
     }
     }

     if (!realImagePaths.isEmpty()) {
     keyImage.createImagesMap(realImagePaths);
     }
     }
     */
}
