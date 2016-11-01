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
package com.imagehistorica.common.view;

import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.util.model.HistoricaType;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.Backend;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.converter.LocalDateStringConverter;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class TreeTableColumnUtil {

    private final Image imgBranch = new Image(getClass().getResourceAsStream("/resources/images/ImageHistorica_16.png"));
    private final Image imgLeaf = new Image(getClass().getResourceAsStream("/resources/images/ImageHistorica_16_leaf.png"));

    private TreeTableView<HistoricaProperty> tree;
    private ContextMenu cxtMenu = new ContextMenu();
    private ImageView imageViewOfFolder = new ImageView(imgBranch);
    private ImageView imageViewOfFile = new ImageView(imgLeaf);

    private static String sortTypeINameCol = "ASCENDING";
    private static String sortTypeLDateCol = "ASCENDING";
    private static String sortTypeLength = "ASCENDING";
    private static String sortTypeFreqViewedCol = "ASCENDING";
    private static String sortTypeRealImagePathCol = "ASCENDING";
    private final Logger logger = LoggerFactory.getLogger(TreeTableColumnUtil.class);

    public TreeTableColumnUtil() {
    }

    public TreeTableColumnUtil(TreeTableView<HistoricaProperty> treeTableView, ContextMenu contextMenu) {
        this.tree = treeTableView;
        this.cxtMenu = contextMenu;
    }

    public TreeTableColumn<HistoricaProperty, String> getImageNameColumnNormal() {
        TreeTableColumn<HistoricaProperty, String> iNameCol = new TreeTableColumn<HistoricaProperty, String>(Rsc.get("common_view_TTC_iName"));
        iNameCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("imageName"));
        iNameCol.setPrefWidth(270);
        iNameCol.setEditable(true);
        iNameCol.setCellFactory(new Callback<TreeTableColumn<HistoricaProperty, String>, TreeTableCell<HistoricaProperty, String>>() {
            @Override
            public TreeTableCell<HistoricaProperty, String> call(TreeTableColumn<HistoricaProperty, String> param) {
                return new TreeTableCell<HistoricaProperty, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item != null) {
                            if (imageViewOfFolder == null) {
                                imageViewOfFolder = new ImageView(imgBranch);
                            }
                            if (imageViewOfFile == null) {
                                imageViewOfFile = new ImageView(imgLeaf);
                            }

                            if (this.getTreeTableRow().getItem() != null) {
                                HistoricaType type = this.getTreeTableRow().getItem().getType();

                                if (type == HistoricaType.LEAF) {
                                    setText(item);
                                    setGraphic(imageViewOfFile);
                                } else {
                                    setText(item + " (" + this.getTreeTableRow().getItem().getNumOfChildren().getValue() + ")");
                                    setGraphic(imageViewOfFolder);
                                }
                            }
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }
                };
            }
        });

        return iNameCol;
    }

    public TreeTableColumn<HistoricaProperty, String> getImageNameColumn() {
        TreeTableColumn<HistoricaProperty, String> iNameCol = new TreeTableColumn<>(Rsc.get("common_view_TTC_iName"));
        iNameCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("imageName"));
        iNameCol.setPrefWidth(500);
        iNameCol.setCellFactory(new Callback<TreeTableColumn<HistoricaProperty, String>, TreeTableCell<HistoricaProperty, String>>() {
            @Override
            public TreeTableCell<HistoricaProperty, String> call(TreeTableColumn<HistoricaProperty, String> param) {
                return new TreeTableCell<HistoricaProperty, String>() {
                    private HistoricaCellGraph graph;
                    private ImageView imageViewOfFolder;
                    private ImageView imageViewOfFile;

                    @Override
                    public void startEdit() {
                        super.startEdit();
                        if (this.graph == null) {
                            this.graph = new HistoricaCellGraph(this);
                        }
                        this.graph.startEdit();
                        setText(null);
                        setGraphic(this.graph);
                    }

                    @Override
                    public void commitEdit(String text) {
                        super.commitEdit(text);
                        logger.debug("committed edit...");
                        tree.setEditable(false);
                    }

                    @Override
                    public void cancelEdit() {
                        super.cancelEdit();
                        logger.debug("cancel edit...");
                        tree.setEditable(false);

                        String item = null;
                        try {
                            item = this.getTreeTableRow().getItem().getImageName();
                        } catch (NullPointerException e) {
                            return;
                        }

                        HistoricaType type = this.getTreeTableRow().getItem().getType();
                        if (type == HistoricaType.LEAF) {
                            setText(item);
                            setGraphic(imageViewOfFile);
                        } else {
                            setText(item);
                            setGraphic(imageViewOfFolder);
                        }
                    }

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item != null) {
                            if (imageViewOfFolder == null) {
                                imageViewOfFolder = new ImageView(imgBranch);
                            }
                            if (imageViewOfFile == null) {
                                imageViewOfFile = new ImageView(imgLeaf);
                            }

                            if (this.getTreeTableRow().getItem() != null) {
                                HistoricaType type = this.getTreeTableRow().getItem().getType();
                                if (type == HistoricaType.LEAF) {
                                    setText(item);
                                    setGraphic(imageViewOfFile);
                                    setContextMenu(cxtMenu);
                                } else {
                                    setText(item + " (" + this.getTreeTableRow().getItem().getNumOfChildren().getValue() + ")");
                                    setGraphic(imageViewOfFolder);
                                    setContextMenu(cxtMenu);
                                }
                            }
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }
                };
            }
        });

        return iNameCol;
    }

    public TreeTableColumn<HistoricaProperty, LocalDate> getLastModifiedColumn() {
        TreeTableColumn<HistoricaProperty, LocalDate> lDateCol = new TreeTableColumn<>(Rsc.get("common_view_TTC_lDate"));
        lDateCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("lastModified"));
        lDateCol.setPrefWidth(150);
        LocalDateStringConverter converter = new LocalDateStringConverter();
        lDateCol.setCellFactory(TextFieldTreeTableCell.<HistoricaProperty, LocalDate>forTreeTableColumn(converter));
        lDateCol.setComparator((LocalDate t1, LocalDate t2) -> {
            if (t1 != null && t2 != null) {
                if (t1.isAfter(t2)) {
                    return 1;
                } else if (t1.isEqual(t2)) {
                    return 0;
                } else if (t1.isBefore(t2)) {
                    return -1;
                }
            }
            return 0;
        });

        return lDateCol;
    }

    public TreeTableColumn<HistoricaProperty, Integer> getLengthColumn() {
        TreeTableColumn<HistoricaProperty, Integer> lengthCol = new TreeTableColumn<>(Rsc.get("common_view_TTC_length"));
        lengthCol.setPrefWidth(100);
        lengthCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("length"));
        return lengthCol;
    }

    public TreeTableColumn<HistoricaProperty, Integer> getFreqViewedColumn() {
        TreeTableColumn<HistoricaProperty, Integer> freqViewedCol = new TreeTableColumn<>(Rsc.get("common_view_TTC_freqView"));
        freqViewedCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("freqViewed"));
        freqViewedCol.setPrefWidth(100);
        return freqViewedCol;
    }

    public TreeTableColumn<HistoricaProperty, String> getUrlColumn() {
        TreeTableColumn<HistoricaProperty, String> urlCol = new TreeTableColumn<>(Rsc.get("common_view_TTC_url"));
        urlCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("url"));
        urlCol.setCellFactory(
                new Callback<TreeTableColumn<HistoricaProperty, String>, TreeTableCell<HistoricaProperty, String>>() {
                    @Override
                    public TreeTableCell<HistoricaProperty, String> call(TreeTableColumn<HistoricaProperty, String> param) {
                        return new TreeTableCell<HistoricaProperty, String>() {
                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                this.setText(item);
                                this.setTextFill(Color.DEEPSKYBLUE);
                            }
                        };
                    }
                });
        return urlCol;
    }

    public TreeTableColumn<HistoricaProperty, String> getRealImagePathColumn() {
        TreeTableColumn<HistoricaProperty, String> realImagePathCol = new TreeTableColumn<>(Rsc.get("common_view_TTC_path"));
        realImagePathCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("realImagePath"));
        return realImagePathCol;
    }

    public static String getSortTypeINameCol() {
        return sortTypeINameCol;
    }

    public static void setSortTypeINameCol(String iNameCol) {
        sortTypeINameCol = iNameCol;
    }

    public static String getSortTypeLDateCol() {
        return sortTypeLDateCol;
    }

    public static void setSortTypeLDateCol(String lDateCol) {
        sortTypeLDateCol = lDateCol;
    }

    public static String getSortTypeLength() {
        return sortTypeLength;
    }

    public static void setSortTypeLength(String lengthCol) {
        sortTypeLength = lengthCol;
    }

    public static String getSortTypeFreqViewedCol() {
        return sortTypeFreqViewedCol;
    }

    public static void setSortTypeFreqViewedCol(String freqViewedCol) {
        sortTypeFreqViewedCol = freqViewedCol;
    }

    public static String getSortTypeRealImagePathCol() {
        return sortTypeRealImagePathCol;
    }

    public static void setSortTypeRealImagePathCol(String realImagePathCol) {
        sortTypeRealImagePathCol = realImagePathCol;
    }

    public static void renameTreeItem(TreeItem<HistoricaProperty> treeItem, String name) {
        if (treeItem.getValue().getType() == HistoricaType.LEAF) {
            Backend.changeNameOfHistorica(treeItem.getValue().getHistoricaId(), name);
        } else {
            TreeCache treeCache = TreeCache.getInstance();
            treeCache.replaceHistoricaDirsMap(treeItem.getValue().getHistoricaDirId(), treeItem.getValue().getHistoricaPath() + name);
            Backend.changeSuffixOfHistoricaDir(treeItem.getValue().getHistoricaDirId(), name);
        }
    }
}
