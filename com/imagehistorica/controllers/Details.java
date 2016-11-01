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
package com.imagehistorica.controllers;

import static com.imagehistorica.util.Constants.MAXIMIZE;
import static com.imagehistorica.util.Constants.WEB_BROWSER_CONTROLLER;

import com.imagehistorica.details.DetailsDesc;
import com.imagehistorica.details.DetailsTree;
import com.imagehistorica.details.DetailsImage;
import com.imagehistorica.common.view.GridPanePanel;
import com.imagehistorica.common.state.ImageState;
import com.imagehistorica.common.state.WebState;
import com.imagehistorica.controller.ControllerType;
import com.imagehistorica.controller.type.Red;
import com.imagehistorica.util.view.ResizeStage;
import com.imagehistorica.util.model.HistoricaType;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.common.state.SearchState;
import com.imagehistorica.common.toolbar.BaseToolBar;
import com.imagehistorica.common.toolbar.BlueToolBar;
import com.imagehistorica.common.toolbar.GreenToolBar;
import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.search.SearchImage;
import com.imagehistorica.util.controller.SearchResult;

import javafx.scene.Scene;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.SVGPath;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.javafx.scene.control.skin.LabeledText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class Details extends Factory implements Red {

    private ImageHistoricaController parentController;
    private Factory prevController = null;
    private ImageState imageState = ImageState.getInstance();
    private SearchState searchState = SearchState.getInstance();
    private TreeCache treeCache = TreeCache.getInstance();

    private GridPanePanel grid;
    private int gridColumn = 4;
    private int gridRow = 3;

    private BaseToolBar toolMenuBar;
    private DetailsTree historicaTree = null;
    private DetailsDesc desc = new DetailsDesc();
    private SearchResult searchResult;
    private ScrollPane scrollPane = new ScrollPane();
    private Button meaningBtn = new Button();
    private Button locationBtn = new Button();
    private SVGPath meaningSvg = new SVGPath();
//    private SVGPath locationSvg = new SVGPath();
    private Tab meaningCtx = new Tab();
//    private Tab locationCtx = new Tab();
    private TabPane tabPane = new TabPane();
    private SplitPane splitPane = new SplitPane();
    private BorderPane bp;

    private TreeTableView<HistoricaProperty> tree;
    private TreeItem<HistoricaProperty> parentItem;
    private TreeItem<HistoricaProperty> lastSelectedItem;
    private List<TreeItem<HistoricaProperty>> treeItems = new ArrayList<>();
    private Map<Integer, List<TreeItem<HistoricaProperty>>> treeItemsMap = new HashMap<>();

    private boolean isPrevRelation = false;
    private boolean isExpandDetailsRelation = false;
    private boolean isExpandImageToStage = false;
    private boolean isExpandImageToFullScreen = false;

    private Image image;
    private ImageView imageView = DetailsImage.getImageView();

    private String history = Rsc.get("common_view_TTC_url");

    private final Logger logger = LoggerFactory.getLogger(Details.class);

    @Override
    public Scene createLayout(ImageHistoricaController parentController) {
        this.parentController = parentController;
//        this.toolMenuBar = new GreenToolBar(this.parentController, this);

        if (!isPrevRelation) {
            prevController = Backend.checkPrevController();
            if (prevController instanceof AnalyzeResult) {
                this.toolMenuBar = new GreenToolBar(this.parentController, this);
//                historicaPropertyUtil = this.parentController.getAnalyzeResult().getHistoricaPropertyUtil();
//                treeTableView = new TreeTableView<HistoricaProperty>(this.parentController.getAnalyzeResult().getLastSelectedNode());
                tree = this.parentController.getAnalyzeResult().getResultHistorica().getTreeNoListener();
//                tree = this.parentController.getAnalyzeResult().getResultHistorica().getTree();
                lastSelectedItem = this.parentController.getAnalyzeResult().getLastSelectedItem();
            } else if (prevController instanceof AppreciateImage) {
                this.toolMenuBar = new BlueToolBar(this.parentController, this);
//                historicaPropertyUtil = this.parentController.getAppreciateImage().getHistoricaPropertyUtil();
                tree = new TreeTableView<>(this.parentController.getAppreciateImage().getLastSelectedNode());
                lastSelectedItem = this.parentController.getAppreciateImage().getLastSelectedItem();
            }
            tree.getSelectionModel().select(lastSelectedItem);
            int index = tree.getSelectionModel().getSelectedIndex();
            if (index > 8) {
                tree.scrollTo(index);
            }
            try {
                historicaTree = new DetailsTree(this.parentController, tree);
            } catch (NullPointerException e) {
                if (historicaTree == null) {
                    historicaTree = new DetailsTree(this.parentController);
                }
            }
            isPrevRelation = true;
        }

        desc.setText(lastSelectedItem);

        try {
            if (lastSelectedItem.getValue().getRealImagePath() != null) {
                parentItem = lastSelectedItem.getParent();
                parentItem.getChildren().stream().filter((treeItem) -> (treeItem.getValue().getType() == HistoricaType.LEAF)).forEach((treeItem) -> {
                    treeItems.add(treeItem);
                });
                Collections.sort(treeItems, (TreeItem<HistoricaProperty> o1, TreeItem<HistoricaProperty> o2) -> o1.getValue().getImageName().compareTo(o2.getValue().getImageName()));

                treeItemsMap.putIfAbsent(parentItem.getValue().getHistoricaDirId(), treeItems);
//                doPrefetchImages();

                image = new Image(new URI(lastSelectedItem.getValue().getRealImagePath()).toString());
                imageView.setImage(image);
                imageState.setImage(lastSelectedItem.getValue().getRealImagePath(), image);
                imageState.increFreqViewed(lastSelectedItem.getValue().getHistoricaId(), 1);
                lastSelectedItem.getValue().setFreqViewed(lastSelectedItem.getValue().getFreqViewed() + 1);

                /*
                 keyImage.createImagesMap(imagesCounter.getLeafNodes(), lastSelectedItem.getValue().getRealImagePath());
                 imageView.setImage(imagesMap.getImage(lastSelectedItem.getValue().getRealImagePath()));
                 */
            }
        } catch (NullPointerException e) {
            logger.info("lastSelectedImage is null...");
            imageView.setImage(new Image(getClass().getResourceAsStream("/resources/images/HelpIcon_128.png"), 300, 300, true, false));
        } catch (URISyntaxException ex) {
            logger.error("", ex);
        }

        String meaningId = String.valueOf(treeCache.getHistoricaMeaningsMap(parentItem.getValue().getHistoricaDirId()));
        searchState.setCurId(meaningId);
        SearchImage searchImage = new SearchImage(meaningId, parentItem.getValue().getImageName(), true);
        this.searchResult = new SearchResult(this.parentController, searchImage);

        scrollPane = new ScrollPane();
        scrollPane.setContent(searchResult);
        scrollPane.viewportBoundsProperty().addListener((ObservableValue<? extends Bounds> bounds, Bounds oldBounds, Bounds newBounds) -> {
            searchResult.setPrefWidth(newBounds.getWidth());
        });

        ChangeListener<Object> changeListener = (ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
            if (scrollPane.getVvalue() > 0.8) {
                searchResult.addImages();
            }
        };
        scrollPane.viewportBoundsProperty().addListener(changeListener);
        scrollPane.hvalueProperty().addListener(changeListener);
        scrollPane.vvalueProperty().addListener(changeListener);
        scrollPane.setOnMouseMoved((e) -> searchResult.integrate());

        meaningSvg = new SVGPath();
        meaningSvg.setStroke(Color.ORANGE);
        meaningSvg.setContent(MAXIMIZE);
        meaningBtn.setGraphic(meaningSvg);

        meaningCtx = new Tab();
        meaningCtx.setText(Rsc.get("con_D_meaningCtx"));
        meaningCtx.setGraphic(meaningBtn);
        meaningCtx.setContent(scrollPane);

        /*
        locationSvg = new SVGPath();
        locationSvg.setStroke(Color.ORANGE);
        locationSvg.setContent(MAXIMIZE);
        locationBtn.setGraphic(locationSvg);

        locationCtx = new Tab();
        locationCtx.setText(Rsc.get("con_D_locationCtx"));
        locationCtx.setGraphic(locationBtn);
        */

        tabPane = new TabPane();
        tabPane.getTabs().addAll(meaningCtx);
//        tabPane.getStyleClass().add("floating");

        splitPane = new SplitPane();
        splitPane.setDividerPositions(0.3f);
        splitPane.getItems().addAll(historicaTree, tabPane);

        createPanel();

        scene = new Scene(grid);
        setScene(scene);

        resizeStage();
        attachEvents();
        attachEventsSVG();

        return scene;
    }

    @Override
    public Scene restoreScene() {
        return this.scene;
    }

    @Override
    protected void attachEvents() {
        scene.setOnKeyPressed((e) -> {
            if (isExpandImageToFullScreen || isExpandImageToStage) {
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
                e.consume();
            }
        });

        EventHandler<MouseEvent> mouseEventHandle = (MouseEvent e) -> {
            handleMouseMoved(e, tree);
            e.consume();
        };

        tree.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEventHandle);

        tree.setOnMousePressed((e) -> {
            try {
                ObservableList<TreeTablePosition<HistoricaProperty, ?>> cells = tree.getSelectionModel().getSelectedCells();
                for (TreeTablePosition<HistoricaProperty, ?> cell : cells) {
                    if (cell == null || cell.getTreeItem().getValue() == null || cell.getTableColumn() == null) {
                        break;
                    }
                    if (cell.getTableColumn().getText().equals(history)) {
                        loadWebpage(cell.getTreeItem().getValue().getUrl());
                        return;
                    }
                }
            } catch (NullPointerException ex) {
            } catch (Exception ex) {
                logger.debug("", ex);
            }

            e.consume();
        });

        tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<HistoricaProperty>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<HistoricaProperty>> observable,
                    TreeItem<HistoricaProperty> oldItem, TreeItem<HistoricaProperty> newItem) {

                if (newItem.getValue() != null && newItem.getValue().getType() == HistoricaType.LEAF) {
                    treeItems = treeItemsMap.get(newItem.getParent().getValue().getHistoricaDirId());
                    if (treeItems != null) {
                        int index = treeItems.indexOf(newItem);
                        if (index > -1) {
                            changeToNextImage(index);
                        }
                    } else {
                        List<TreeItem<HistoricaProperty>> newTreeItems = new ArrayList<>();
                        TreeItem<HistoricaProperty> parentItem = newItem.getParent();
                        parentItem.getChildren().stream().filter((treeItem) -> (treeItem.getValue().getType() == HistoricaType.LEAF)).forEach((treeItem) -> {
                            newTreeItems.add(treeItem);
                        });
                        Collections.sort(newTreeItems, (TreeItem<HistoricaProperty> o1, TreeItem<HistoricaProperty> o2) -> o1.getValue().getImageName().compareTo(o2.getValue().getImageName()));

                        treeItems = newTreeItems;
                        int index = newTreeItems.indexOf(newItem);
                        if (index > -1) {
                            changeToNextImage(index);
                        }

                        treeItemsMap.put(parentItem.getValue().getHistoricaDirId(), newTreeItems);
                    }

                    imageState.increFreqViewed(newItem.getValue().getHistoricaId(), 1);
                    newItem.getValue().setFreqViewed(newItem.getValue().getFreqViewed() + 1);
                }
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
                    if (isExpandImageToStage && isExpandImageToFullScreen) {
                        isExpandImageToFullScreen = false;
                        this.parentController.getStage().setFullScreen(false);
                    } else if (isExpandImageToStage && !isExpandImageToFullScreen) {
                        isExpandImageToStage = false;
                        replaceLayout();
                    }
                }

                if (primaryClicked) {
                    switch (e.getClickCount()) {
                        case 1:
                            Point2D mousePress = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
                            mouseDown.set(mousePress);
                            break;
                        case 2:
                            if (!isExpandImageToStage && !isExpandImageToFullScreen) {
                                isExpandImageToStage = true;
                                replaceLayout();
                            } else if (isExpandImageToStage && !isExpandImageToFullScreen) {
                                isExpandImageToFullScreen = true;
                                this.parentController.getStage().setFullScreenExitHint(Rsc.get("exitHint"));
                                this.parentController.getStage().setFullScreen(true);
                            }
                            break;
                    }
                }
            } catch (NullPointerException ex) {
            } catch (Exception ex) {
                logger.debug("", ex);
            }

            e.consume();
        });

        imageView.setOnMouseDragged(e -> {
            try {
                Point2D dragPoint = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
                shift(imageView, dragPoint.subtract(mouseDown.get()));
                mouseDown.set(imageViewToImage(imageView, new Point2D(e.getX(), e.getY())));
            } catch (NullPointerException ex) {
            } catch (Exception ex) {
                logger.debug("", ex);
            }

            e.consume();
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
            } catch (Exception ex) {
                logger.debug("", ex);
            }

            e.consume();
        });
    }

    protected void attachEventsSVG() {
        meaningBtn.setOnMouseClicked(e -> {
            if (isExpandDetailsRelation) {
                isExpandDetailsRelation = false;
            } else {
                isExpandDetailsRelation = true;
            }
            maximizeTab();
            e.consume();
        });
    }

    @Override
    public ControllerType getControllerType() {
        return controllerType;
    }

    @Override
    protected void resizeStage() {
        ResizeStage.setResizeStage(this.parentController.getStage(), this, grid);
    }

    protected void resizeStageBP() {
        ResizeStage.setResizeStage(this.parentController.getStage(), scene);
    }

    protected void expandImageToFullScreen() {
        this.parentController.getStage().setFullScreenExitHint(Rsc.get("exitHint"));
        this.parentController.getStage().setFullScreen(isExpandImageToFullScreen);
    }

    public void maximizeTab() {
        if (!isExpandDetailsRelation) {
            adjustLayout();
        } else {
            bp = new BorderPane(tabPane, null, null, null, null);

            scene = new Scene(bp);
            setScene(this.parentController.applyStyleSheet(scene));

            resizeStageBP();
            attachEventsSVG();

            this.parentController.restoreScene(this.parentController.getDetails());
        }
    }

    public void replaceLayout() {
        if (!isExpandImageToStage && !isExpandImageToFullScreen) {
            adjustLayout();
        } else {
            bp = new BorderPane(imageView, null, null, null, null);
            imageView.fitWidthProperty().bind(bp.widthProperty());
            imageView.fitHeightProperty().bind(bp.heightProperty());
            imageView.setCursor(Cursor.HAND);

            scene = new Scene(bp);
            setScene(this.parentController.applyStyleSheet(scene));

            resizeStageBP();
            attachEvents();

            this.parentController.restoreScene(this.parentController.getDetails());
        }
    }

    @Override
    public void adjustLayout() {
        createPanel();

        scene = new Scene(grid);
        setScene(this.parentController.applyStyleSheet(scene));

        resizeStage();
        attachEvents();
        attachEventsSVG();

        this.parentController.restoreScene(this.parentController.getDetails());
    }

    private void createPanel() {
        double width = this.parentController.getStage().getWidth();
        double height = this.parentController.getStage().getHeight();
        grid = new GridPanePanel(gridColumn, gridRow, width, height);
        imageView.fitWidthProperty().bind(grid.widthProperty().multiply(0.75).subtract(10));
        imageView.fitHeightProperty().bind(grid.heightProperty().multiply(0.5).subtract(30));
        imageView.setCursor(Cursor.HAND);

        if (!isExpandDetailsRelation) {
            tabPane = new TabPane();
            tabPane.getTabs().addAll(meaningCtx);

            splitPane = new SplitPane();
            splitPane.setDividerPositions(0.3f);
            splitPane.getItems().addAll(historicaTree, tabPane);
        }

        GridPane.setConstraints(toolMenuBar, 0, 0, 4, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(desc, 0, 1, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(imageView, 1, 1, 3, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(splitPane, 0, 2, 4, 1, HPos.LEFT, VPos.TOP);

        grid.getChildren().addAll(toolMenuBar, desc, imageView, splitPane);
    }

    private void handleMouseMoved(MouseEvent e, TreeTableView<HistoricaProperty> tree) {
        tree.setCursor(Cursor.DEFAULT);
        Node node = e.getPickResult().getIntersectedNode();
        if (node instanceof Text) {
            String hyperlink = ((LabeledText) node).getText();
            if (hyperlink.contains("http://") || hyperlink.contains("https://")) {
                tree.setCursor(Cursor.HAND);
            }
        }
    }

    private void loadWebpage(String page) {
        WebState state = WebState.getInstance();
        state.setLoadPage(page);
        this.parentController.setWebBrowser((WebBrowser) this.parentController.createScene(this, WEB_BROWSER_CONTROLLER));
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
        } catch (Exception e) {
            logger.debug("", e);
        }
        return mouse;
    }

    private void changeToNextImage(int index) {
        lastSelectedItem = treeItems.get(index);
        HistoricaProperty neighborProp = lastSelectedItem.getValue();
        Image image = imageState.getImage(neighborProp.getRealImagePath());
        if (image != null) {
            imageView.setImage(image);
        } else {
            image = new Image(URI.create(neighborProp.getRealImagePath()).toString());
            imageView.setImage(image);
            imageState.setImage(neighborProp.getRealImagePath(), image);
        }

        desc.setText(lastSelectedItem);

        double width = image.getWidth();
        double height = image.getHeight();
        reset(imageView, width, height);

//        doPrefetchImages();
    }
    
    @Override
    public Factory getPrevController() {
        return this.prevController;
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
