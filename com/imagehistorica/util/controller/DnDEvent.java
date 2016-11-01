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
package com.imagehistorica.util.controller;

import static com.imagehistorica.util.Constants.DELIMITER;

import com.imagehistorica.common.state.AnalyzeState;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.util.model.HistoricaType;
import com.imagehistorica.util.view.CommonAlert;

import javafx.beans.binding.NumberBinding;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class DnDEvent {

    private TreeTableView<HistoricaProperty> tree;
    private static TreeItem<HistoricaProperty> targetItem = null;
    private static List<TreeItem<HistoricaProperty>> droppedItems = null;
    private static List<TreeItem<HistoricaProperty>> branchItems = null;
    private static List<TreeItem<HistoricaProperty>> leafItems = null;
    private static int fixedIndex = 0;
    private final DataFormat TREEITEM_INDEX_LIST;

    private TreeCache treeCache = TreeCache.getInstance();

    private final Logger logger = LoggerFactory.getLogger(DnDEvent.class);

    public DnDEvent(TreeTableView<HistoricaProperty> tree, DataFormat dataFormat) {
        this.TREEITEM_INDEX_LIST = dataFormat;
        this.tree = tree;
        this.tree.setRowFactory(this::rowFactory);
    }

    public TreeTableRow<HistoricaProperty> rowFactory(TreeTableView<HistoricaProperty> tree) {
        TreeTableRow<HistoricaProperty> row = new TreeTableRow<>();
        row.setOnDragDetected(e -> {
            logger.debug("Start setOnDragDetected...");
            AnalyzeState status = AnalyzeState.getInstance();
            if (status.inProgress()) {
                Alert alert = CommonAlert.makeAlert(Alert.AlertType.INFORMATION, Rsc.get("common_util_DDE_title"), Rsc.get("common_util_DDE_header"), null);
                alert.showAndWait();
            }

            int selectedCount = tree.getSelectionModel().getSelectedIndices().size();
            if (selectedCount == 0) {
                e.consume();
                return;
            }

            ObservableList<Integer> draggedItems = tree.getSelectionModel().getSelectedIndices();
            int itemsNum = draggedItems.size();
            int[] indices = new int[itemsNum];
            Iterator<Integer> iter = draggedItems.iterator();
            for (int i = 0; i < itemsNum; i++) {
                indices[i] = iter.next();
            }

            Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.put(TREEITEM_INDEX_LIST, indices);
            db.setContent(cc);
            e.consume();
        });

        row.setOnDragOver(e -> {
            logger.debug("Start setOnDragOver...");
            Dragboard db = e.getDragboard();
            int targetIndex = row.getIndex();
            if (acceptable(db, targetIndex)) {
                e.acceptTransferModes(TransferMode.MOVE);
            } else {
                targetItem = null;
                droppedItems = null;
                branchItems = null;
                leafItems = null;
            }
            e.consume();
        });

        row.setOnDragDropped(e -> {
            logger.debug("Start setOnDragDropped...");
            Dragboard db = e.getDragboard();
            int targetIndex = row.getIndex();
            logger.debug("Call acceptable()...");
            if (acceptable(db, targetIndex)) {
                int[] items = (int[]) db.getContent(TREEITEM_INDEX_LIST);
                int num = 0;
                targetItem = tree.getTreeItem(targetIndex);
                droppedItems = new ArrayList<>();
                for (int item : items) {
                    droppedItems.add(tree.getTreeItem(item));
                    if (item < targetIndex) {
                        num++;
                    }
                }

                // for debug
                if (logger.isDebugEnabled()) {
                    targetItem.getParent().getChildren().stream().forEach((item) -> {
                        logger.debug("Parent's child of targetItem: {}", item.getValue().getImageName());
                    });

                    targetItem.getChildren().stream().forEach((item) -> {
                        logger.debug("targetItem's child: {}", item.getValue().getImageName());
                    });
                }

                fixedIndex = targetIndex - num;

            } else {
                targetItem = null;
                droppedItems = null;
                branchItems = null;
                leafItems = null;
            }

            e.setDropCompleted(true);
            e.consume();
        });

        row.setOnDragDone(e -> {
            logger.debug("Start setOnDragDone...");
            if (droppedItems != null) {
                branchItems = new ArrayList<>();
                leafItems = new ArrayList<>();

                droppedItems.stream().forEach((droppedItem) -> {
                    if (droppedItem.getValue().getType() == HistoricaType.LEAF) {
                        leafItems.add(droppedItem);
                    } else {
                        branchItems.add(droppedItem);
                    }
                });

                if (!leafItems.isEmpty()) {
                    for (TreeItem<HistoricaProperty> leafItem : leafItems) {
                        int prevVal = treeCache.getHistoricaNumsMap(leafItem.getParent().getValue().getHistoricaDirId());
                        treeCache.putHistoricaNumsMap(leafItem.getParent().getValue().getHistoricaDirId(), prevVal - 1);
                        leafItem.getParent().getValue().setNumOfLeaves(leafItem.getParent().getValue().getNumOfLeaves().get() - 1);
                        leafItem.getParent().getChildren().remove(leafItem);
                    }
                }

                if (!branchItems.isEmpty()) {
                    for (TreeItem<HistoricaProperty> branchItem : branchItems) {
                        TreeItem<HistoricaProperty> parentItem = branchItem.getParent();
                        branchItem.getParent().getChildren().remove(branchItem);

                        // Change historica type from BRANCH to BRANCH_LEAF if the parent has no branch after removed.
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

                        while (parentItem != null) {
                            List<NumberBinding> numOfChildrens = new ArrayList<>();
                            int i = 1;
                            for (TreeItem<HistoricaProperty> child : parentItem.getChildren()) {
                                if (child.getValue().getType() != HistoricaType.LEAF) {
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
                }

                String updatePath;
                HistoricaProperty targetProp = targetItem.getValue();
                for (TreeItem<HistoricaProperty> droppedItem : droppedItems) {
                    HistoricaProperty droppedProp = droppedItem.getValue();

                    // Update parent of droppedItem.
                    targetItem.getChildren().add(droppedItem);

                    // Update path.
                    updatePath = targetProp.getHistoricaPath() + targetProp.getImageName();
                    if (droppedProp.getType() == HistoricaType.LEAF) {
                        droppedProp.setHistoricaPath(updatePath);
                        droppedProp.setHistoricaDirId(targetProp.getHistoricaDirId());
                        Backend.changeDirIdOfHistorica(droppedProp.getHistoricaId(), targetProp.getHistoricaDirId());
                        targetProp.setNumOfLeaves(targetProp.getNumOfLeaves().get() + 1);
                        int prevVal = treeCache.getHistoricaNumsMap(targetProp.getHistoricaDirId());
                        treeCache.putHistoricaNumsMap(targetProp.getHistoricaDirId(), prevVal + 1);
                    } else {
                        String newBaseName = updatePath + DELIMITER;
                        logger.debug("newbasename: {}", newBaseName);

                        // Update child path.
                        String oldBaseName = !droppedProp.getHistoricaPath().isEmpty() ? Pattern.quote(droppedProp.getHistoricaPath()) : "^";
                        logger.debug("oldbasename: {}", oldBaseName);
                        updateChildren(droppedItem, oldBaseName, newBaseName);

                        // Update droppedItem.
                        droppedProp.setHistoricaPath(newBaseName);
                        Backend.changePathPartOfHistoricaDir(droppedProp.getHistoricaDirId(), newBaseName);
                        treeCache.replaceHistoricaDirsMap(droppedProp.getHistoricaDirId(), newBaseName.substring(0, newBaseName.lastIndexOf(DELIMITER)));

                        targetProp.setNumOfChildren(targetProp.getNumOfChildren().add(droppedProp.getNumOfChildren()));
                        if (targetProp.getType() != HistoricaType.ROOT) {
                            targetProp.setType(HistoricaType.BRANCH);
                        }
                        TreeItem<HistoricaProperty> parentItem = targetItem.getParent();
                        while (parentItem != null) {
                            List<NumberBinding> numOfChildrens = new ArrayList<>();
                            int i = 1;
                            for (TreeItem<HistoricaProperty> child : parentItem.getChildren()) {
                                if (child.getValue().getType() != HistoricaType.LEAF) {
                                    logger.debug("Add...i: {}, child: {}", i, child.getValue().getImageName());
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
                }
                tree.refresh();
                tree.getSelectionModel().clearAndSelect(fixedIndex);
                tree.scrollTo(fixedIndex);
            }
            e.consume();
        });

        return row;
    }

    private boolean acceptable(Dragboard db, int targetIndex) {
        logger.debug("Start acceptable()...");
        if (!db.hasContent(TREEITEM_INDEX_LIST)) {
            return false;
        } else {
            int[] indices = (int[]) db.getContent(TREEITEM_INDEX_LIST);
            for (int item : indices) {
                if (item == targetIndex) {
                    return false;
                }
            }

            targetItem = tree.getTreeItem(targetIndex);
            if (targetItem.getValue().getType() == HistoricaType.LEAF) {
                return false;
            }

            return acceptableDetails(indices);
        }
    }

    private boolean acceptableDetails(int[] indices) {
        for (int index : indices) {
            TreeItem<HistoricaProperty> draggedItem = tree.getTreeItem(index);
            HistoricaProperty draggedProp = draggedItem.getValue();
            if (draggedProp.getType() == HistoricaType.BRANCH || draggedProp.getType() == HistoricaType.BRANCH_LEAF) {
                // Check whether draggedItem and targetItem are same parent.
                if (targetItem == draggedItem.getParent()) {
                    logger.debug("Forbidden due to same parent...");
                    return false;
                }

                // Check whether existing of same path.
                for (TreeItem<HistoricaProperty> treeItem : targetItem.getChildren()) {
                    if (draggedProp.getImageName().equals(treeItem.getValue().getImageName())) {
                        if (draggedProp.getType() == HistoricaType.LEAF) {
                            if (draggedProp.getType() == treeItem.getValue().getType()) {
                                logger.debug("Forbidden due to existing of same leaf path...");
                                return false;
                            }
                        } else {
                            if (treeItem.getValue().getType() != HistoricaType.LEAF) {
                                logger.debug("Forbidden due to existing of same branch(_leaf) path...");
                                return false;
                            }
                        }
                    }
                }

                // Check whether moving draggedItem to its child.
                TreeItem<HistoricaProperty> parentItem = targetItem;
                while (parentItem != null) {
                    logger.debug("parentItem: {}", parentItem.getValue().getImageName());
                    if (parentItem == draggedItem) {
                        logger.debug("Forbidden because draggedItem is target's child...");
                        return false;
                    }
                    parentItem = parentItem.getParent();
                }
            } else if (draggedProp.getType() == HistoricaType.LEAF) {
                if (targetItem == draggedItem.getParent() || targetItem.getValue().getType() == HistoricaType.ROOT) {
                    logger.debug("Forbidden due to same parent or moving leaf item to root...");
                    return false;
                }
            } else if (draggedProp.getType() == HistoricaType.ROOT) {
                logger.debug("Forbidden due to root item...");
                return false;
            }
        }
        return true;
    }

    private void updateChildren(TreeItem<HistoricaProperty> treeItem, String oldBaseName, String newBaseName) {
        if (treeItem.getValue().getType() == HistoricaType.LEAF) {
            String historicaPath = treeItem.getValue().getHistoricaPath().replaceFirst(oldBaseName, newBaseName);
            logger.debug("Before leaf 1: {}", treeItem.getValue().getHistoricaPath());
            logger.debug("After  leaf 1: {}", historicaPath);
            treeItem.getValue().setHistoricaPath(historicaPath);
            return;
        }

        for (TreeItem<HistoricaProperty> item : treeItem.getChildren()) {
            if (item.getValue().getType() == HistoricaType.LEAF) {
                updateChildren(item, oldBaseName, newBaseName);
                return;
            }

            String historicaPath = item.getValue().getHistoricaPath().replaceFirst(oldBaseName, newBaseName);
            logger.debug("Original historicaPath: {}", item.getValue().getHistoricaPath());
            logger.debug("Updated  historicaPath: {}", historicaPath);
            logger.debug("ImageName: {}", item.getValue().getImageName());
            item.getValue().setHistoricaPath(historicaPath);
            Backend.changePathPartOfHistoricaDir(item.getValue().getHistoricaDirId(), historicaPath);
            treeCache.replaceHistoricaDirsMap(item.getValue().getHistoricaDirId(), historicaPath.substring(0, historicaPath.lastIndexOf(DELIMITER)));
            updateChildren(item, oldBaseName, newBaseName);
        }
    }
}
