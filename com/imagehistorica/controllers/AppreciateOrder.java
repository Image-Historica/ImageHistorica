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

import static com.imagehistorica.util.Constants.WEB_BROWSER_CONTROLLER;

import com.imagehistorica.appreciate.order.AdditionalStage;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.common.state.ImageState;
import com.imagehistorica.common.toolbar.AdditionalToolBar;
import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.ControllerType;
import com.imagehistorica.util.view.ResizeStage;
import com.imagehistorica.appreciate.order.CompareTree;
import com.imagehistorica.common.state.WebState;
import com.imagehistorica.util.controller.DnDEvent;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.util.model.HistoricaType;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.controller.type.Yellow;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.sun.javafx.scene.control.skin.LabeledText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class AppreciateOrder extends Factory implements Yellow {

    private ImageHistoricaController parentController;
    private ImageState imageState = ImageState.getInstance();
    private TreeCache treeCache = TreeCache.getInstance();
    private AdditionalToolBar toolBar;

    private SplitPane sp = new SplitPane();
    private BorderPane bp = new BorderPane();

    private CompareTree compareTree;
    private TreeTableView<HistoricaProperty> leftTree;
    private TreeTableView<HistoricaProperty> rightTree;

    private BorderPane left = new BorderPane();
    private BorderPane right = new BorderPane();
    private Label placeHolder = new Label(Rsc.get("noData"));
    private String history = Rsc.get("common_view_TTC_url");

    private DnDEvent dndEvent_left;
    private DnDEvent dndEvent_right;
    private final DataFormat TREEITEM_INDEX;

    private TreeItem<HistoricaProperty> lastSelectedItem;
    private boolean isExpandImageToStage = false;
    private boolean isExpandImageToFullScreen = false;

    private final Logger logger = LoggerFactory.getLogger(AppreciateOrder.class);

    public AppreciateOrder() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        TREEITEM_INDEX = new DataFormat("treeitem-index-" + sdf.format(date));
    }

    @Override
    public Scene createLayout(ImageHistoricaController parentController) {
        this.parentController = parentController;
        toolBar = new AdditionalToolBar(this.parentController, this);
        compareTree = new CompareTree(this.parentController);

        leftTree = compareTree.getLeftTree();
        rightTree = compareTree.getRightTree();

        sp.setDividerPositions(0.5f);
        sp.getItems().addAll(leftTree, rightTree);
        bp.setTop(toolBar);
        bp.setCenter(sp);

//        createPanel();
        scene = new Scene(bp);
        setScene(scene);

        resizeStage();
        attachEvents();

        return scene;
    }

    @Override
    public Scene restoreScene() {
        return this.scene;
    }

    @Override
    protected void attachEvents() {
        dndEvent_left = new DnDEvent(leftTree, TREEITEM_INDEX);
        dndEvent_right = new DnDEvent(rightTree, TREEITEM_INDEX);

        leftTree.setOnDragDone(e -> {
            leftTree.refresh();
            rightTree.refresh();
            e.consume();
        });

        rightTree.setOnDragDone(e -> {
            rightTree.refresh();
            leftTree.refresh();
            e.consume();
        });

        EventHandler<MouseEvent> leftMouseEventHandle = (MouseEvent e) -> {
            handleMouseMoved(e, leftTree);
        };
        leftTree.addEventHandler(MouseEvent.MOUSE_MOVED, leftMouseEventHandle);

        EventHandler<MouseEvent> rightMouseEventHandle = (MouseEvent e) -> {
            handleMouseMoved(e, rightTree);
        };
        rightTree.addEventHandler(MouseEvent.MOUSE_MOVED, rightMouseEventHandle);

        leftTree.setOnMousePressed(e -> {
            boolean doubleClicked = e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2;
            try {
                ObservableList<TreeTablePosition<HistoricaProperty, ?>> cells = leftTree.getSelectionModel().getSelectedCells();
                for (TreeTablePosition<HistoricaProperty, ?> cell : cells) {
                    if (cell == null || cell.getTreeItem().getValue() == null || cell.getTableColumn() == null) {
                        break;
                    }
                    if (cell.getTableColumn().getText().equals(history)) {
                        loadWebpage(cell.getTreeItem().getValue().getUrl());
                        return;
                    }
                }

                if (doubleClicked) {
                    lastSelectedItem = leftTree.getSelectionModel().getSelectedItem();
                    if (lastSelectedItem != null) {
                        HistoricaType type = lastSelectedItem.getValue().getType();
                        if (type == HistoricaType.LEAF) {
                            AdditionalStage additionalStage = new AdditionalStage(this.parentController, lastSelectedItem);
                            additionalStage.createLayout();
                        }
                    }
                }
            } catch (NullPointerException ex) {
            } catch (Exception ex) {
                logger.debug("", ex);
            }
        });

        rightTree.setOnMousePressed(e -> {
            boolean doubleClicked = e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2;
            try {
                ObservableList<TreeTablePosition<HistoricaProperty, ?>> cells = rightTree.getSelectionModel().getSelectedCells();
                for (TreeTablePosition<HistoricaProperty, ?> cell : cells) {
                    if (cell == null || cell.getTreeItem().getValue() == null || cell.getTableColumn() == null) {
                        break;
                    }
                    if (cell.getTableColumn().getText().equals(history)) {
                        loadWebpage(cell.getTreeItem().getValue().getUrl());
                        return;
                    }
                }

                if (doubleClicked) {
                    lastSelectedItem = rightTree.getSelectionModel().getSelectedItem();
                    if (lastSelectedItem != null) {
                        HistoricaType type = lastSelectedItem.getValue().getType();
                        if (type == HistoricaType.LEAF) {
                            AdditionalStage additionalStage = new AdditionalStage(this.parentController, lastSelectedItem);
                            additionalStage.createLayout();
                        }
                    }
                }
            } catch (NullPointerException ex) {
            } catch (Exception ex) {
                logger.debug("", ex);
            }
        });
    }

    @Override
    public ControllerType getControllerType() {
        return controllerType;
    }

    @Override
    protected void resizeStage() {
        ResizeStage.setResizeStage(this.parentController.getStage(), scene);
    }

    @Override
    public void adjustLayout() {
        scene = new Scene(bp);
        setScene(scene);

        resizeStage();
        attachEvents();

        setScene(this.parentController.applyStyleSheet(scene));
        this.parentController.restoreScene(this.parentController.getAppreciateOrder());
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

    public void loadWebpage(String page) {
        WebState state = WebState.getInstance();
        state.setLoadPage(page);
        this.parentController.setWebBrowser((WebBrowser) this.parentController.createScene(this, WEB_BROWSER_CONTROLLER));
    }
}
