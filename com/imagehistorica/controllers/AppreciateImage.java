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
import static com.imagehistorica.util.Constants.WEB_BROWSER_CONTROLLER;

import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.type.Blue;
import com.imagehistorica.common.toolbar.BlueToolBar;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.common.state.AnalyzeState;
import com.imagehistorica.appreciate.image.BranchTab;
import com.imagehistorica.appreciate.image.GeneralTab;
import com.imagehistorica.controller.ControllerType;
import com.imagehistorica.util.view.ResizeStage;
import com.imagehistorica.util.model.HistoricaType;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.common.state.WebState;
import com.imagehistorica.Config;

import javafx.scene.Scene;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.scene.control.Button;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class AppreciateImage extends Factory implements Blue {

    private ImageHistoricaController parentController;
    private TreeCache treeCache = TreeCache.getInstance();
    private AnalyzeState state = AnalyzeState.getInstance();

    private GeneralTab generalTab;
    private BranchTab abstractTab;
    private TreeTableView<HistoricaProperty> tree;
    private TreeTableView<HistoricaProperty> treeNoListener;
    private TreeItem<HistoricaProperty> lastSelectedNode = null;
    private TreeItem<HistoricaProperty> lastSelectedItem = null;
    private BlueToolBar toolMenuBar;

    private TabPane tabPane = new TabPane();
    private BorderPane bp;

    private final Logger logger = LoggerFactory.getLogger(AppreciateImage.class);

    @Override
    public Scene createLayout(ImageHistoricaController parentController) {
        this.parentController = parentController;
        toolMenuBar = new BlueToolBar(this.parentController, this);

        generalTab = new GeneralTab(this.parentController, "General", null);
        generalTab.setId("0");
        generalTab.setClosable(false);
        tree = generalTab.getTreeTableView();
        treeNoListener = generalTab.getTreeTableView();

        tabPane.getTabs().add(generalTab);

        bp = new BorderPane(tabPane, toolMenuBar, null, null, null);

        toolMenuBar.toFront();

        scene = new Scene(bp);
        setScene(scene);

        resizeStage();
        attachEvents(tree);

        String[] shortCutTabs = Config.getShortCutTabs();
        if (shortCutTabs != null) {
            for (String shortCutTab : shortCutTabs) {
                TreeItem<HistoricaProperty> treeItem = treeCache.getBranchNode(Integer.parseInt(shortCutTab));
                if (treeItem != null) {
                    replaceLayout(treeItem, treeItem.getValue().getImageName());
                }
            }

            String lastSelectedTab = Config.getLastSelectedTab();
            if (!lastSelectedTab.isEmpty()) {
                tabPane.getSelectionModel().select(Integer.parseInt(lastSelectedTab));
            }
        }

        return scene;
    }

    private void checkTask() {
        state.createLeaves();
        state.requestNewTreeItem();
        if (!state.isMerged()) {
            state.mergeTreeItem();
            tree.refresh();
        }
    }

    @Override
    public Scene restoreScene() {
        checkTask();
        return this.scene;
    }

    @Override
    protected void attachEvents() {
    }

    protected void attachEvents(TreeTableView<HistoricaProperty> tree) {
        EventHandler<MouseEvent> mouseEventHandle = (MouseEvent e) -> {
            tree.setCursor(Cursor.DEFAULT);
            Node node = e.getPickResult().getIntersectedNode();
            if (node instanceof Text) {
                String hyperlink = ((LabeledText) node).getText();
                if (hyperlink.contains("http://") || hyperlink.contains("https://")) {
                    tree.setCursor(Cursor.HAND);
                }
            }
        };
        tree.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEventHandle);

        tree.setOnMousePressed(e -> {
            try {
                tree.setCursor(Cursor.DEFAULT);
                boolean isHyperlink = false;
                Node node = e.getPickResult().getIntersectedNode();
                if (node != null) {
                    if (node instanceof Text) {
                        String text = ((LabeledText) node).getText();
                        if (text.contains("http://") || text.contains("https://")) {
                            isHyperlink = true;
                        }
                        logger.debug("Text: {}", text);
                    } else if ((node instanceof TreeTableCell && ((TreeTableCell) node).getText() != null)) {
                        logger.debug("TreeTableCell: {}", ((TreeTableCell) node).getText());
                    } else if (node instanceof Button) {
                        logger.debug("Node is button...{}", node);
                    } else {
                        logger.debug("Can't handle the node...{}", node);
                        e.consume();
                        return;
                    }

                    ObservableList<TreeTablePosition<HistoricaProperty, ?>> cells = tree.getSelectionModel().getSelectedCells();
                    if (cells.size() < 1 || cells.size() > 1) {
                        logger.debug("Null or multiple cells selected...");
                        e.consume();
                        return;
                    }

                    if (isHyperlink) {
                        loadWebpage(cells.get(0).getTreeItem().getValue().getUrl());
                        logger.debug("Called loadWebpage()...");
                        e.consume();
                        return;
                    }

                    boolean primaryClicked = e.getButton().equals(MouseButton.PRIMARY);
                    if (primaryClicked) {
                        lastSelectedItem = tree.getSelectionModel().getSelectedItem();
                        HistoricaType type = lastSelectedItem.getValue().getType();
                        switch (e.getClickCount()) {
                            case 2:
                                if (type == HistoricaType.LEAF) {
                                    lastSelectedNode = tree.getRoot();
                                    moveToNextScreen();
                                }
                                break;
                            case 3:
                                if (type == HistoricaType.ROOT || type == HistoricaType.BRANCH || type == HistoricaType.BRANCH_LEAF) {
                                    replaceLayout(lastSelectedItem, lastSelectedItem.getValue().getImageName());
                                }
                                break;
                        }
                    }
                }
            } catch (NullPointerException ex) {
            } catch (Exception ex) {
                logger.debug("", ex);
            }
            e.consume();
        });

        tree.setOnDragDone(e -> {
            tree.refresh();
            e.consume();
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
    }

    public void replaceLayout(TreeItem<HistoricaProperty> selectedNode, String title) {
        logger.debug("Called replaceLayout: {}", title);
        abstractTab = new BranchTab(this.parentController, selectedNode, title, null);
        abstractTab.setId(String.valueOf(selectedNode.getValue().getHistoricaDirId()));
        abstractTab.setClosable(true);
        abstractTab.setOnClosed(e -> abstractTab = null);

        tabPane.getTabs().add(abstractTab);
        attachEvents(abstractTab.getTreeTableView());

        tabPane.getSelectionModel().select(abstractTab);
    }

    protected void moveToNextScreen() {
        logger.debug("Called moveToNextScreen()...1");
        this.parentController.setDetails((Details) this.parentController.createScene(this, DETAILS_CONTROLLER));
    }

    public TreeTableView<HistoricaProperty> getTree() {
        return this.tree;
    }

    public TreeTableView<HistoricaProperty> getTreeNoListener() {
        return this.treeNoListener;
    }

    public TreeItem<HistoricaProperty> getLastSelectedItem() {
        return this.lastSelectedItem;
    }

    public void loadWebpage(String page) {
        WebState state = WebState.getInstance();
        state.setLoadPage(page);
        this.parentController.setWebBrowser((WebBrowser) this.parentController.createScene(this, WEB_BROWSER_CONTROLLER));
    }

    public TreeItem<HistoricaProperty> getLastSelectedNode() {
        return this.lastSelectedNode;
    }

    public void setLastSelectedNode(TreeItem<HistoricaProperty> lastSelectedNode) {
        this.lastSelectedNode = lastSelectedNode;
    }

    public GeneralTab getGeneralTab() {
        return this.generalTab;
    }

    public TabPane getTabPane() {
        return this.tabPane;
    }

    public Scene getScene() {
        return this.scene;
    }
}
