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
package com.imagehistorica.appreciate.image;

import com.imagehistorica.common.menu.CxtMenu;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.util.controller.DnDEvent;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.common.view.TreeTableColumnUtil;
import com.imagehistorica.controller.resources.Rsc;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.DataFormat;

import java.time.LocalDate;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class BranchTab extends Tab {

    private ImageHistoricaController controller;

    private TreeTableView<HistoricaProperty> tree;
    private TreeItem<HistoricaProperty> rootNode;
    private TreeTableColumn<HistoricaProperty, String> iNameCol;
    private TreeTableColumn<HistoricaProperty, LocalDate> lDateCol;
    private TreeTableColumn<HistoricaProperty, Integer> lengthCol;
    private TreeTableColumn<HistoricaProperty, Integer> freqViewedCol;
    private TreeTableColumn<HistoricaProperty, String> urlCol;
    private TreeTableColumn<HistoricaProperty, String> realImagePathCol;
    private TreeTableColumnUtil treeTableColumnUtil = new TreeTableColumnUtil();
    private DnDEvent dndEvent;
    private CxtMenu cxtMenu;
    private Label placeHolder = new Label(Rsc.get("noData"));

    private static final String dataFormatString = "treeitem-index-branch";
    private static int dataFormatSuffix = 0;
    private final DataFormat TREEITEM_INDEX_BRANCH = new DataFormat(dataFormatString + dataFormatSuffix);

    public BranchTab(ImageHistoricaController controller, TreeItem<HistoricaProperty> rootNode, String title, Node graphic) {
        this.controller = controller;
        this.rootNode = rootNode;
        this.setText(title);
        this.setGraphic(graphic);
        createLayout();
    }

    @SuppressWarnings("unchecked")
    public void createLayout() {
        rootNode.setExpanded(true);

        tree = new TreeTableView<>(rootNode);
        tree.setEditable(false);
        tree.setShowRoot(true);
        tree.setTableMenuButtonVisible(true);
        tree.setPlaceholder(placeHolder);
        tree.setCursor(Cursor.DEFAULT);
        tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tree.setOnSort(e -> {
            if (!iNameCol.getSortType().name().equals(TreeTableColumnUtil.getSortTypeINameCol())) {
                this.controller.getAppreciateImage().getTree().getSortOrder().clear();
                this.controller.getAppreciateImage().getTree().getSortOrder().add(iNameCol);
                TreeTableColumnUtil.setSortTypeINameCol(iNameCol.getSortType().name());
            }
            if (!lDateCol.getSortType().name().equals(TreeTableColumnUtil.getSortTypeLDateCol())) {
                this.controller.getAppreciateImage().getTree().getSortOrder().clear();
                this.controller.getAppreciateImage().getTree().getSortOrder().add(lDateCol);
                TreeTableColumnUtil.setSortTypeLDateCol(lDateCol.getSortType().name());
            }
            if (!lengthCol.getSortType().name().equals(TreeTableColumnUtil.getSortTypeLength())) {
                this.controller.getAppreciateImage().getTree().getSortOrder().clear();
                this.controller.getAppreciateImage().getTree().getSortOrder().add(lengthCol);
                TreeTableColumnUtil.setSortTypeLength(lengthCol.getSortType().name());
            }
            if (!freqViewedCol.getSortType().name().equals(TreeTableColumnUtil.getSortTypeFreqViewedCol())) {
                this.controller.getAppreciateImage().getTree().getSortOrder().clear();
                this.controller.getAppreciateImage().getTree().getSortOrder().add(freqViewedCol);
                TreeTableColumnUtil.setSortTypeFreqViewedCol(freqViewedCol.getSortType().name());
            }
            if (!realImagePathCol.getSortType().name().equals(TreeTableColumnUtil.getSortTypeRealImagePathCol())) {
                this.controller.getAppreciateImage().getTree().getSortOrder().clear();
                this.controller.getAppreciateImage().getTree().getSortOrder().add(realImagePathCol);
                TreeTableColumnUtil.setSortTypeRealImagePathCol(realImagePathCol.getSortType().name());
            }
        });

        dndEvent = new DnDEvent(tree, TREEITEM_INDEX_BRANCH);
        dataFormatSuffix++;
        cxtMenu = new CxtMenu(controller, tree);
        cxtMenu.createLayoutAppImg();
        treeTableColumnUtil = new TreeTableColumnUtil(tree, cxtMenu);

        iNameCol = treeTableColumnUtil.getImageNameColumn();
        lDateCol = treeTableColumnUtil.getLastModifiedColumn();
        lengthCol = treeTableColumnUtil.getLengthColumn();
        freqViewedCol = treeTableColumnUtil.getFreqViewedColumn();
        urlCol = treeTableColumnUtil.getUrlColumn();
        realImagePathCol = treeTableColumnUtil.getRealImagePathColumn();

        tree.getColumns().addAll(iNameCol, lDateCol, lengthCol, freqViewedCol, urlCol, realImagePathCol);

        setContent(tree);
    }

    public TreeTableView<HistoricaProperty> getTreeTableView() {
        return this.tree;
    }
}
