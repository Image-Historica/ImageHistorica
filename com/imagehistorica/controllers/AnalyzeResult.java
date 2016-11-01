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

import static com.imagehistorica.util.Constants.DETAILS_CONTROLLER;

import com.imagehistorica.analyze.result.ResultHistorica;
import com.imagehistorica.analyze.result.ResultRealPath;
import com.imagehistorica.common.state.AnalyzeState;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.common.state.ImageState;
import com.imagehistorica.common.toolbar.GreenToolBar;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.common.view.GridPanePanel;
import com.imagehistorica.controller.ControllerType;
import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.type.Green;
import com.imagehistorica.util.view.ResizeStage;
import com.imagehistorica.Config;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.Cursor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class AnalyzeResult extends Factory implements Green {

    private ImageHistoricaController parentController;
    private AnalyzeState analyzeState = AnalyzeState.getInstance();
    private ImageState imageState = ImageState.getInstance();
    private TreeCache treeCache = TreeCache.getInstance();

    private GreenToolBar toolMenuBar;
    private GridPanePanel grid;
    private int gridColumn = 1;
    private int gridRow = 3;

    private SplitPane sp;
    private ResultRealPath resultRealPath;
    private ResultHistorica resultHistorica;
    private Map<Integer, HistoricaProperty> props;
    private TreeTableView<HistoricaProperty> tree;
    private ListView<HistoricaProperty> resultView;
    private TreeItem<HistoricaProperty> rootNode;
    private ImageView imageView;
    private TreeItem<HistoricaProperty> lastSelectedItem = null;

    private Map<Integer, TreeItem<HistoricaProperty>> tmpHistoricaDirs = new HashMap<>();
    private TreeItem<HistoricaProperty> childs = null;

    private final Logger logger = LoggerFactory.getLogger(AnalyzeResult.class);

    @Override
    public Scene createLayout(ImageHistoricaController parentController) {
        this.parentController = parentController;
        this.toolMenuBar = new GreenToolBar(this.parentController, this);

        resultRealPath = new ResultRealPath(parentController);
        props = resultRealPath.getHistoricaProperty();
        resultView = resultRealPath.getResultView();
        resultHistorica = new ResultHistorica();
        tree = resultHistorica.getTree();
        rootNode = resultHistorica.getRootNode();
        logger.debug("rootNode: {}", rootNode);

//        imageView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/HelpIcon_128.png"), 300, 300, true, false));
        imageView = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/HelpIcon_128.png")));
        imageView.setCursor(Cursor.HAND);

        sp = new SplitPane();
        sp.setDividerPositions(0.5f);
        sp.getItems().addAll(resultRealPath, resultHistorica);

        createPanel();

        scene = new Scene(grid);
        setScene(scene);

        attachEvents();
        resizeStage();

        return scene;
    }

    private void checkTask() {
        props = analyzeState.createLeaves();
        analyzeState.requestNewTreeItem();
        if (props != null && !props.isEmpty()) {
            analyzeState.getCreatedHistoricas().stream().forEach((historica) -> {
                resultView.getItems().add(props.get(historica.getHistoricaId()));
            });
        }
    }

    @Override
    public Scene restoreScene() {
//        checkTask();
        return this.scene;
    }

    @Override
    protected void attachEvents() {
        imageView.setOnMouseClicked(e -> {
            parentController.setDetails((Details) parentController.createScene(this, DETAILS_CONTROLLER));
            e.consume();
        });

        tree.getSelectionModel().selectedItemProperty().addListener((ob, oldVal, newVal) -> {
            logger.debug("Start change listener of treetableview...");
            try {
                if (newVal != null && newVal.getValue() != null) {
                    logger.debug("NewVal... historicaId: {}, path: {}, imageName: {}", newVal.getValue().getHistoricaId(),
                            newVal.getValue().getHistoricaPath(), newVal.getValue().getImageName());
                    String realImagePath = newVal.getValue().getRealImagePath();
                    if (realImagePath == null) {
                        return;
                    }

                    Image image = imageState.getImage(realImagePath);
                    if (image != null) {
                        imageView.setImage(image);
                    } else {
                        image = new Image(new URI(realImagePath).toString());
                        imageView.setImage(image);
                        imageState.setImage(realImagePath, image);
                    }
                    resultRealPath.getResultView().getSelectionModel().select(props.get(newVal.getValue().getHistoricaId()));
                    resultRealPath.getResultView().scrollTo(props.get(newVal.getValue().getHistoricaId()));
                    lastSelectedItem = newVal;
                }
            } catch (NullPointerException e) {
            } catch (Exception e) {
                logger.debug("", e);
            }
        });

        resultView.getSelectionModel().selectedItemProperty().addListener((ob, oldVal, newVal) -> {
            logger.debug("Start change listener of listview...");
            try {
                if (newVal != null) {
                    logger.debug("NewVal... historicaId: {}, path: {}, imageName: {}", newVal.getHistoricaId(),
                            newVal.getHistoricaPath(), newVal.getImageName());
                    TreeItem<HistoricaProperty> treeItem = treeCache.getLeafNodeNew(newVal.getHistoricaId());
                    if (treeItem != null) {
                        String realImagePath = newVal.getRealImagePath();
                        if (realImagePath == null) {
                            return;
                        }
                        logger.debug("realImagePath: {}", realImagePath);

                        Image image = imageState.getImage(realImagePath);
                        if (image != null) {
                            imageView.setImage(image);
                        } else {
                            image = new Image(new URI(realImagePath).toString());
                            imageView.setImage(image);
                            imageState.setImage(realImagePath, image);
                        }
                        tree.getSelectionModel().select(treeItem);
                        tree.scrollTo(tree.getSelectionModel().getSelectedIndex());
                        lastSelectedItem = treeItem;
                    }
                }
            } catch (NullPointerException e) {
            } catch (Exception e) {
                logger.debug("", e);
            }
        });
    }

    @Override
    public ControllerType getControllerType() {
        return controllerType;
    }

    @Override
    protected void resizeStage() {
        ResizeStage.setResizeStage(parentController.getStage(), this, grid);
    }

    @Override
    public void adjustLayout() {
        createPanel();

        scene = new Scene(grid);
        setScene(scene);

        attachEvents();
        resizeStage();

        setScene(parentController.applyStyleSheet(scene));
        parentController.restoreScene(parentController.getAnalyzeResult());
    }

    private void createPanel() {
        double width = parentController.getStage().getWidth();
        double height = parentController.getStage().getHeight();
        grid = new GridPanePanel(gridColumn, gridRow, width, height);

        imageView.fitWidthProperty().bind(grid.widthProperty());
        imageView.fitHeightProperty().bind(grid.heightProperty().multiply(0.5).subtract(35));
        imageView.setPreserveRatio(true);
        if (Config.isHighQualityImage()) {
            imageView.setSmooth(true);
        }

        GridPane.setConstraints(toolMenuBar, 0, 0, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(imageView, 0, 1, 1, 1, HPos.CENTER, VPos.CENTER);
        GridPane.setConstraints(sp, 0, 2, 1, 1, HPos.LEFT, VPos.TOP);

        grid.getChildren().addAll(toolMenuBar, imageView, sp);
    }

    public ResultHistorica getResultHistorica() {
        return this.resultHistorica;
    }

    public TreeItem<HistoricaProperty> getLastSelectedItem() {
        return this.lastSelectedItem;
    }
}
