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
package com.imagehistorica.common.menu;

import static com.imagehistorica.util.Constants.APPRECIATE_ORDER_CONTROLLER;
import static com.imagehistorica.util.Constants.DELIMITER;
import static com.imagehistorica.common.menu.embody.EmbodyHistoricaType.*;

import com.imagehistorica.common.menu.embody.EmbodyDialogs;
import com.imagehistorica.common.menu.embody.EmbodyHistoricaType;
import com.imagehistorica.common.state.AnalyzeState;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controllers.AppreciateOrder;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.util.model.HistoricaType;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.databases.model.Historica;
import com.imagehistorica.databases.model.HistoricaDir;
import com.imagehistorica.Config;
import com.imagehistorica.common.menu.imports.ImportAsyncTask;
import com.imagehistorica.common.state.SearchState;
import com.imagehistorica.databases.model.RealImage;
import com.imagehistorica.util.controller.ThreadCreator;
import com.imagehistorica.search.Selectable;
import com.imagehistorica.search.SelectableHistorica;
import com.imagehistorica.util.view.CommonAlert;

import javafx.beans.binding.NumberBinding;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Hex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class CxtMenu extends ContextMenu {

    private ImageHistoricaController parentController;
    private TreeCache treeCache = TreeCache.getInstance();
    private AnalyzeState analyzeState = AnalyzeState.getInstance();

    private EmbodyDialogs dialogs;
    private File dstDirectory;
    private TreeTableView<HistoricaProperty> tree;
    private Set<Integer> historicaDirIds;
    private List<Historica> historicas;
    private long totalLength = 0;
    private String eliminatePath = null;

    private Set<Selectable> selectables;

    private final Logger logger = LoggerFactory.getLogger(CxtMenu.class);

    public CxtMenu(Set<Selectable> selectables) {
        this.selectables = selectables;
    }

    public CxtMenu(ImageHistoricaController controller, TreeTableView<HistoricaProperty> tree) {
        this.parentController = controller;
        this.tree = tree;
    }

    public void createLayoutDisplay() {
        logger.debug("Called createLayoutDisplay()...");
        MenuItem displayItem = displayItem();
        this.getItems().addAll(displayItem);
    }

    public void createLayoutImport() {
        logger.debug("Called createLayoutImport()...");
        MenuItem importItem = importItem();
        this.getItems().addAll(importItem);
    }

    public void createLayoutAppOdr() {
        logger.debug("Called createLayoutAppOdr()...");
        MenuItem newItem = newItem();
        MenuItem editItem = editItem();
        MenuItem deleteItem = deleteItem();
        MenuItem embodyHistoricaCopy = embodyHistoricaCopy();
        MenuItem embodyHistoricaMove = embodyHistoricaMove();
        if (logger.isDebugEnabled()) {
            MenuItem forDebug = forDebug();
            this.getItems().addAll(newItem, editItem, deleteItem, embodyHistoricaCopy, embodyHistoricaMove, forDebug);
        } else {
            this.getItems().addAll(newItem, editItem, deleteItem, embodyHistoricaCopy, embodyHistoricaMove);
        }
    }

    public void createLayoutAppImg() {
        logger.debug("Called createLayoutAppImg()...");
        MenuItem newItem = newItem();
        MenuItem editItem = editItem();
        MenuItem deleteItem = deleteItem();
        MenuItem embodyHistoricaCopy = embodyHistoricaCopy();
        MenuItem embodyHistoricaMove = embodyHistoricaMove();
        MenuItem appreciateOrder = appreciateOrder();
        MenuItem replaceLayout = replaceLayout();
        if (logger.isDebugEnabled()) {
            MenuItem forDebug = forDebug();
            this.getItems().addAll(newItem, editItem, deleteItem, embodyHistoricaCopy, embodyHistoricaMove, appreciateOrder, replaceLayout, forDebug);
        } else {
            this.getItems().addAll(newItem, editItem, deleteItem, embodyHistoricaCopy, embodyHistoricaMove, appreciateOrder, replaceLayout);
        }
    }

    private MenuItem newItem() {
        MenuItem newItem = new MenuItem(Rsc.get("common_menu_CM_new"));
        newItem.setOnAction(e -> {
            if (analyzeState.inProgress()) {
                Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("common_menu_CM_new"), Rsc.get("common_menu_CM_new_header_1"), null);
                alert.showAndWait();
                e.consume();
                return;
            }
            TreeItem<HistoricaProperty> selectedItem = tree.getSelectionModel().getSelectedItem();
            if (selectedItem.getValue().getType() == HistoricaType.LEAF) {
                Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("common_menu_CM_new"), Rsc.get("common_menu_CM_new_header_2"), null);
                alert.showAndWait();
            } else {
                String hisDirSuffix = "New";
                String hisDirPathPart = "";
                if (selectedItem.getValue().getType() != HistoricaType.ROOT) {
                    hisDirPathPart = selectedItem.getValue().getHistoricaPath() + selectedItem.getValue().getImageName() + DELIMITER;
                }
                logger.debug("make New...");
                int historicaDirId = Backend.getHistoricaDirId(hisDirSuffix, hisDirPathPart);
                logger.debug("make New dirId: {}", historicaDirId);
                if (historicaDirId != -1) {
                    int i = 1;
                    while (historicaDirId != -1) {
                        hisDirSuffix = "New_" + i;
                        historicaDirId = Backend.getHistoricaDirId(hisDirSuffix, hisDirPathPart);
                        i++;
                    }
                }

                HistoricaDir historicaDir = Backend.getNewHistoricaDir(hisDirPathPart + hisDirSuffix);
                historicaDirId = historicaDir.getHistoricaDirId();
                TreeItem<HistoricaProperty> newTreeItem = new TreeItem<>(
                        new HistoricaProperty(hisDirSuffix, hisDirPathPart, historicaDirId, HistoricaType.BRANCH_LEAF, false));
                treeCache.putBranchNode(historicaDirId, newTreeItem);
                treeCache.putHistoricaDirsMap(historicaDirId, hisDirPathPart + hisDirSuffix);
                treeCache.putHistoricaNumsMap(historicaDirId, 0);

                selectedItem.getChildren().add(newTreeItem);

                TreeItem<HistoricaProperty> parentItem = newTreeItem.getParent();
                if (parentItem.getValue().getType() == HistoricaType.BRANCH_LEAF) {
                    parentItem.getValue().setType(HistoricaType.BRANCH);
                }
                updateBinding(parentItem);

                selectedItem.setExpanded(true);
                int newRowIndex = tree.getRow(newTreeItem);
                tree.scrollTo(newRowIndex);

                TreeTableColumn<HistoricaProperty, ?> firstCol = tree.getColumns().get(0);
                tree.getSelectionModel().clearAndSelect(newRowIndex);
                tree.getFocusModel().focus(newRowIndex, firstCol);
            }
            e.consume();
        });

        return newItem;
    }

    @SuppressWarnings("unchecked")
    private MenuItem editItem() {
        MenuItem editItem = new MenuItem(Rsc.get("common_menu_CM_edit"));
        editItem.setOnAction(e -> {
            logger.debug("Called editItem()...");
            if (analyzeState.inProgress()) {
                Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("common_menu_CM_edit"), Rsc.get("common_menu_CM_edit_header_1"), null);
                alert.showAndWait();
                e.consume();
                return;
            }
            TreeItem<HistoricaProperty> treeItem = tree.getSelectionModel().getSelectedItem();
            logger.debug("Called editItem()... {}", treeItem.getValue().getImageName());
            if (treeItem.getValue().getType() == HistoricaType.ROOT) {
                Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("common_menu_CM_edit"), Rsc.get("common_menu_CM_edit_header_2"), null);
                alert.showAndWait();
                e.consume();
                return;
            }
            tree.setEditable(true);
            TreeTablePosition focusedCellPosition = tree.getFocusModel().getFocusedCell();
            tree.edit(focusedCellPosition.getRow(), focusedCellPosition.getTableColumn());
            e.consume();
        });

        return editItem;
    }

    private MenuItem deleteItem() {
        MenuItem deleteItem = new MenuItem(Rsc.get("common_menu_CM_del"));
        deleteItem.setOnAction(e -> {
            logger.debug("Called deleteItem()...");
            if (analyzeState.inProgress()) {
                Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("common_menu_CM_del"), Rsc.get("common_menu_CM_del_header_1"), null);
                alert.showAndWait();
                e.consume();
                return;
            }
            ObservableList<TreeItem<HistoricaProperty>> treeItems = tree.getSelectionModel().getSelectedItems();
            logger.debug("Called delItem()... size: {}", treeItems.size());
            Set<Integer> delHistoricaIds = new HashSet<>();
            Set<Integer> delHistoricaDirIds = new HashSet<>();
            for (TreeItem<HistoricaProperty> treeItem : treeItems) {
                if (treeItem != null) {
                    logger.debug("Classify deleting items... historicaId: {}, imageName: {}", treeItem.getValue().getHistoricaId(), treeItem.getValue().getImageName());
                    if (treeItem.getValue().getType() == HistoricaType.LEAF) {
                        delHistoricaIds.add(treeItem.getValue().getHistoricaId());
                    } else if (treeItem.getValue().getType() == HistoricaType.BRANCH || treeItem.getValue().getType() == HistoricaType.BRANCH_LEAF) {
                        delHistoricaDirIds.add(treeItem.getValue().getHistoricaDirId());
                    } else if (treeItem.getValue().getType() == HistoricaType.ROOT) {
                        Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("common_menu_CM_del"), Rsc.get("common_menu_CM_del_header_2"), Rsc.get("common_menu_CM_del_content_2"));
                        alert.showAndWait();
                        e.consume();
                        return;
                    }
                }
            }

            ButtonType ok = new ButtonType("OK");
            ButtonType cancel = new ButtonType(Rsc.get("cancelBtn"));
            Optional<ButtonType> result = null;
            boolean isFirst = true;
            for (int historicaId : delHistoricaIds) {
                TreeItem<HistoricaProperty> leaf = treeCache.getLeafNode(historicaId);
                if (leaf != null) {
                    TreeItem<HistoricaProperty> parent = leaf.getParent();
                    if (isFirst) {
                        String historicaPath = leaf.getValue().getHistoricaPath();
                        String imageName = leaf.getValue().getImageName();
                        String historicaFullPath = shapeString(historicaPath + imageName);
                        if (delHistoricaIds.size() > 1) {
                            historicaFullPath += Rsc.get("common_menu_CM_del_desc_1") + delHistoricaIds.size() + Rsc.get("common_menu_CM_del_desc_2");
                        }
                        Alert alert = CommonAlert.makeAlert(AlertType.CONFIRMATION, Rsc.get("common_menu_CM_del"), Rsc.get("common_menu_CM_del_header_3"),
                                Rsc.get("common_menu_CM_del_content_3_a") + historicaFullPath + Rsc.get("common_menu_CM_del_content_3_b"), ok, cancel);
                        result = alert.showAndWait();
                        isFirst = false;
                    }
                    if (result.get() == ok) {
                        Backend.deleteHistoricaByHistoricaId(historicaId);
                        parent.getValue().setNumOfLeaves(parent.getValue().getNumOfLeaves().get() - 1);
                        parent.getChildren().remove(leaf);
                        treeCache.decreHistoricaNumsMap(parent.getValue().getHistoricaDirId(), 1);
                    } else {
                        break;
                    }
                }
            }

            isFirst = true;
            for (int historicaDirId : delHistoricaDirIds) {
                TreeItem<HistoricaProperty> branch = treeCache.getBranchNode(historicaDirId);
                if (branch != null) {
                    TreeItem<HistoricaProperty> parent = branch.getParent();
                    if (isFirst) {
                        String historicaPath = branch.getValue().getHistoricaPath();
                        String imageName = branch.getValue().getImageName();
                        String historicaFullPath = shapeString(historicaPath + imageName);
                        if (delHistoricaDirIds.size() > 1) {
                            historicaFullPath += Rsc.get("common_menu_CM_del_desc_1") + delHistoricaDirIds.size() + Rsc.get("common_menu_CM_del_desc_3");
                        }
                        Alert alert = CommonAlert.makeAlert(AlertType.CONFIRMATION, Rsc.get("common_menu_CM_del"), Rsc.get("common_menu_CM_del_header_3"),
                                Rsc.get("common_menu_CM_del_content_3_a") + historicaFullPath + Rsc.get("common_menu_CM_del_content_3_b"), ok, cancel);
                        result = alert.showAndWait();
                        isFirst = false;
                    }
                    if (result.get() == ok) {
                        if (branch.getValue().getType() == HistoricaType.BRANCH) {
                            historicaDirIds = new HashSet<>();
                            historicaDirIds.add(historicaDirId);
                            searchTreeItem(branch);
                            for (int id : historicaDirIds) {
                                Backend.deleteHistoricaByHistoricaDirId(id);
                                Backend.deleteHistoricaDir(id);
                                treeCache.removeBranchNode(id);
                                treeCache.removeBranchNodeNew(id);
                                treeCache.removeHistoricaDirsMap(id);
                                treeCache.removeHistoricaNumsMap(id);
                                treeCache.removeHistoricaMeaningsMap(id);
                            }
                            parent.getChildren().remove(branch);
                            updateBinding(parent);
                            checkType(parent);
                        } else if (branch.getValue().getType() == HistoricaType.BRANCH_LEAF) {
                            int id = branch.getValue().getHistoricaDirId();
                            Backend.deleteHistoricaByHistoricaDirId(id);
                            Backend.deleteHistoricaDir(id);
                            treeCache.removeBranchNode(id);
                            treeCache.removeBranchNodeNew(id);
                            treeCache.removeHistoricaDirsMap(id);
                            treeCache.removeHistoricaNumsMap(id);
                            treeCache.removeHistoricaMeaningsMap(id);
                            parent.getChildren().remove(branch);
                            updateBinding(parent);
                            checkType(parent);
                        }
                    } else {
                        break;
                    }
                }
            }
            tree.getSelectionModel().clearSelection();
            e.consume();
        });

        return deleteItem;
    }

    private MenuItem embodyHistoricaCopy() {
        MenuItem embodyHistoricaCopy = new MenuItem(Rsc.get("common_menu_CM_embCopy"));
        embodyHistoricaCopy.setOnAction(e -> {
            embodyHistorica(embodyHistoricaCopy.getText());
            e.consume();
        });
        return embodyHistoricaCopy;
    }

    private MenuItem embodyHistoricaMove() {
        MenuItem embodyHistoricaMove = new MenuItem(Rsc.get("common_menu_CM_embMove"));
        embodyHistoricaMove.setOnAction(e -> {
            embodyHistorica(embodyHistoricaMove.getText());
            e.consume();
        });
        return embodyHistoricaMove;
    }

    private void embodyHistorica(String menuItem) {
        logger.debug("Called embodyHistorica()...");
        TreeItem<HistoricaProperty> treeItem = tree.getSelectionModel().getSelectedItem();
        HistoricaType historicaType = treeItem.getValue().getType();
        boolean isCopy;
        if (menuItem.contentEquals(Rsc.get("common_menu_CM_embCopy"))) {
            isCopy = true;
        } else if (menuItem.contentEquals(Rsc.get("common_menu_CM_embMove"))) {
            isCopy = false;
        } else {
            logger.error("Not copy or move...");
            return;
        }

        if (historicaType == HistoricaType.ROOT) {
            if (isCopy) {
                logger.info("Select COPY_HISTORICA_WITH_PATH of ROOT...");
                if (checkHistoricas(treeItem, COPY_HISTORICA_WITH_PATH)) {
                    dialogs = new EmbodyDialogs(treeItem.getValue(), historicaType, COPY_HISTORICA_WITH_PATH, historicas, dstDirectory, totalLength);
                    dialogs.embodyHistorica_1st();
                }
            } else {
                logger.info("Select COPY_HISTORICA_WITH_PATH of ROOT...");
                if (checkHistoricas(treeItem, MOVE_HISTORICA_WITH_PATH)) {
                    dialogs = new EmbodyDialogs(treeItem.getValue(), historicaType, MOVE_HISTORICA_WITH_PATH, historicas, dstDirectory, totalLength);
                    dialogs.embodyHistorica_1st();
                }
            }
        } else {
            if (historicaType == HistoricaType.LEAF) {
                if (isCopy) {
                    logger.info("Select COPY_HISTORICA_WITH_PATH of LEAF...");
                    if (checkHistoricas(treeItem, COPY_HISTORICA_ONLY)) {
                        dialogs = new EmbodyDialogs(treeItem.getValue(), historicaType, COPY_HISTORICA_ONLY, historicas, dstDirectory, totalLength, eliminatePath);
                        dialogs.embodyHistorica_1st();
                    }
                } else {
                    logger.info("Select COPY_HISTORICA_WITH_PATH of LEAF...");
                    if (checkHistoricas(treeItem, MOVE_HISTORICA_ONLY)) {
                        dialogs = new EmbodyDialogs(treeItem.getValue(), historicaType, MOVE_HISTORICA_ONLY, historicas, dstDirectory, totalLength, eliminatePath);
                        dialogs.embodyHistorica_1st();
                    }
                }
            } else {
                if (isCopy) {
                    logger.info("Select COPY_HISTORICA_WITH_PATH of BRANCH(_LEAF)...");
                    if (checkHistoricas(treeItem, COPY_HISTORICA_WITH_PATH)) {
                        dialogs = new EmbodyDialogs(treeItem.getValue(), historicaType, COPY_HISTORICA_WITH_PATH, historicas, dstDirectory, totalLength, eliminatePath);
                        dialogs.embodyHistorica_1st();
                    }
                } else {
                    logger.info("Select COPY_HISTORICA_WITH_PATH of BRANCH(_LEAF)...");
                    if (checkHistoricas(treeItem, MOVE_HISTORICA_WITH_PATH)) {
                        dialogs = new EmbodyDialogs(treeItem.getValue(), historicaType, MOVE_HISTORICA_WITH_PATH, historicas, dstDirectory, totalLength, eliminatePath);
                        dialogs.embodyHistorica_1st();
                    }
                }
            }
        }
    }

    private MenuItem appreciateOrder() {
        MenuItem appreciateOrder = new MenuItem(Rsc.get("common_menu_CM_appOrder"));
        appreciateOrder.setOnAction(e -> {
            logger.debug("Called appreciateOrder()...");
            TreeItem<HistoricaProperty> lastSelectedNode = tree.getRoot();
            parentController.getAppreciateImage().setLastSelectedNode(lastSelectedNode);
            parentController.setAppreciateOrder((AppreciateOrder) parentController.createScene(parentController.getAppreciateImage(), APPRECIATE_ORDER_CONTROLLER));
            e.consume();
        });
        return appreciateOrder;
    }

    private MenuItem replaceLayout() {
        MenuItem replaceLayout = new MenuItem(Rsc.get("common_menu_CM_repLayout"));
        replaceLayout.setOnAction(e -> {
            logger.debug("Called replaceLayout()...");
            TreeItem<HistoricaProperty> selectedItem = tree.getSelectionModel().getSelectedItem();
            if (selectedItem.getValue().getType() != HistoricaType.LEAF) {
                parentController.getAppreciateImage().replaceLayout(selectedItem, selectedItem.getValue().getImageName());
            }
            e.consume();
        });
        return replaceLayout;

    }

    private MenuItem displayItem() {
        MenuItem display = new MenuItem(Rsc.get("common_menu_CM_display"));
        display.setOnAction(e -> {
            logger.debug("Called displayItem()...");
            for (Selectable selectable : selectables) {
                SelectableHistorica s = (SelectableHistorica) selectable;
                Historica h = s.getHistorica();
                HistoricaDir historicaDir = Backend.getHistoricaDir(h.getHistoricaDirId());
                RealImage realImage = Backend.getRealImage(h.getRealImageId());
                String sha1 = h.getSha1() != null ? new String(Hex.encodeHex(h.getSha1())) : null;
                String cld = h.getFeature() != null ? new String(Hex.encodeHex(h.getFeature())) : null;
                Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("common_menu_CM_display"), Rsc.get("common_menu_CM_display_header"),
                        "HistoricaPath:\n  " + historicaDir.getHisDirPathPart() + historicaDir.getHisDirSuffix() + "\n\n"
                        + "Historica:\n  " + h.getImageName() + "\n\n"
                        + "RealImagePath:\n  " + Paths.get(URI.create(realImage.getRealImagePath0())) + "\n\n"
                        + "SHA-1:\n  " + sha1 + "\n\n" + "ColorLayoutDiagram:\n  " + cld + "\n");
                alert.showAndWait();
            }
            e.consume();
        });

        return display;
    }

    private MenuItem importItem() {
        MenuItem importItem = new MenuItem(Rsc.get("common_menu_CM_import"));
        importItem.setOnAction(e -> {
            logger.debug("Called importItem()...");
            SearchState searchState = SearchState.getInstance();
            if (searchState.isImporting()) {
                Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("common_menu_CM_import"), Rsc.get("common_menu_CM_import_header_1"), null);
                alert.showAndWait();
                e.consume();
                return;
            }

            searchState.setImporting(true);
            ImportAsyncTask imp = new ImportAsyncTask(selectables);
            ThreadCreator t = new ThreadCreator();
            t.startAsyncTask(imp);

            e.consume();
        });

        return importItem;
    }

    private MenuItem forDebug() {
        MenuItem forDebug = new MenuItem(Rsc.get("common_menu_CM_forDebug"));
        forDebug.setOnAction(e -> {
            TreeItem<HistoricaProperty> item = tree.getSelectionModel().getSelectedItem();
            logger.info("Type: {}", item.getValue().getType());
            logger.info("historicaId: {}", item.getValue().getHistoricaId());
            logger.info("historicaDirId: {}", item.getValue().getHistoricaDirId());
            logger.info("historicaPath: {}", item.getValue().getHistoricaPath());
            logger.info("imageName: {}", item.getValue().getImageName());
            e.consume();
        });
        return forDebug;
    }

    private boolean checkHistoricas(TreeItem<HistoricaProperty> treeItem, EmbodyHistoricaType embHisType) {
        logger.debug("Called checkHistoricas()...");
        File rootDir = new File(Config.getRootDir());
        if (rootDir.exists()) {
            dstDirectory = rootDir;
        } else {
            final DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(Rsc.get("dirChooser"));
            try {
                Config.removeRootDir();
                rootDir = new File(Config.getRootDir());
                if (!rootDir.exists()) {
                    if (rootDir.mkdir()) {
                        directoryChooser.setInitialDirectory(rootDir);
                        dstDirectory = directoryChooser.showDialog(parentController.getStage());
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            } catch (IllegalArgumentException e) {
                directoryChooser.setInitialDirectory(new File(Config.getInitialDirectory()));
                dstDirectory = directoryChooser.showDialog(parentController.getStage());
            } catch (SecurityException e) {
                directoryChooser.setInitialDirectory(new File(Config.getInitialDirectory()));
                dstDirectory = directoryChooser.showDialog(parentController.getStage());
            } catch (NullPointerException e) {
                logger.debug("Null directory selected...");
                return false;
            }
        }

        if (dstDirectory.exists()) {
            Config.setRootDir(dstDirectory.getAbsolutePath());
            if (treeItem.getValue().getType() == HistoricaType.ROOT) {
                totalLength = 0;
                historicas = Backend.getHistoricas();
                if (!historicas.isEmpty()) {
                    historicas.stream().map((historica) -> historica.getLength()).forEach((length) -> {
                        totalLength += length;
                    });
                }
                return true;
            }

            if (embHisType == COPY_HISTORICA_ONLY || embHisType == MOVE_HISTORICA_ONLY) {
                totalLength = treeItem.getValue().getLength();
                historicas = new ArrayList<>();
                historicas.add(Backend.getHistoricaByHistoricaId(treeItem.getValue().getHistoricaId()));
            } else if (embHisType == COPY_HISTORICA_WITH_PATH || embHisType == MOVE_HISTORICA_WITH_PATH) {
                totalLength = 0;
                historicas = new ArrayList<>();
                eliminatePath = treeItem.getValue().getHistoricaPath();

                historicaDirIds = new HashSet<>();
                searchTreeItem(treeItem);
                historicaDirIds.stream().map((historicaDirId) -> {
                    logger.debug("historicaDirId: {}", historicaDirId);
                    return historicaDirId;
                }).forEach((historicaDirId) -> {
                    historicas.addAll(Backend.getHistoricasByHistoricaDirId(historicaDirId));
                });

                if (!historicas.isEmpty()) {
                    historicas.stream().forEach((historica) -> {
                        totalLength += historica.getLength();
                    });
                }
            }
            return true;
        } else {
            EmbodyDialogs ed = new EmbodyDialogs(Rsc.get("common_menu_CM_dialog_1"));
            ed.errorAvailable();
            return false;
        }
    }

    private void searchTreeItem(TreeItem<HistoricaProperty> treeItem) {
        logger.debug("Called searchTreeItem()...");
        if (treeItem.getValue().getType() == HistoricaType.LEAF) {
            return;
        }

        if (treeItem.getValue().getType() == HistoricaType.BRANCH_LEAF) {
            logger.debug("Called searchTreeItem()... BRANCH_LEAF: {}", treeItem.getValue().getHistoricaPath() + treeItem.getValue().getImageName());
            historicaDirIds.add(treeItem.getValue().getHistoricaDirId());
            return;
        }

        treeItem.getChildren().stream().forEach((child) -> {
            logger.debug("Called searchTreeItem()... BRANCH: {}", treeItem.getValue().getHistoricaPath() + treeItem.getValue().getImageName());
            historicaDirIds.add(treeItem.getValue().getHistoricaDirId());
            searchTreeItem(child);
        });
    }

    private void updateBinding(TreeItem<HistoricaProperty> parentItem) {
        logger.debug("Called updateBinding()...");
        while (parentItem != null) {
            List<NumberBinding> numOfChildrens = new ArrayList<>();
            int i = 1;
            for (TreeItem<HistoricaProperty> child : parentItem.getChildren()) {
                if (child.getValue().getType() != HistoricaType.LEAF) {
                    logger.debug("Added binding...i: {}, child: {}", i, child.getValue().getImageName());
                    numOfChildrens.add(child.getValue().getNumOfChildren());
                    i++;
                }
            }

            NumberBinding numOfChildren = parentItem.getValue().getNumOfLeaves().add(0);;
            for (NumberBinding numBinding : numOfChildrens) {
                numOfChildren = numOfChildren.add(numBinding);
            }
            parentItem.getValue().setNumOfChildren(numOfChildren);
            parentItem = parentItem.getParent();
        }
    }

    private void checkType(TreeItem<HistoricaProperty> parentItem) {
        boolean noBranch = true;
        for (TreeItem<HistoricaProperty> child : parentItem.getChildren()) {
            if (child.getValue().getType() == HistoricaType.BRANCH || child.getValue().getType() == HistoricaType.BRANCH_LEAF) {
                noBranch = false;
                break;
            }
        }
        if (noBranch) {
            parentItem.getValue().setType(HistoricaType.BRANCH_LEAF);
        }
    }

    private String shapeString(String path) {
        Matcher m = Pattern.compile("[\\s\\S]{1,50}").matcher(path);
        String s = "";
        while (m.find()) {
            s += m.group() + "\n";
        }
        return s;
    }
}
