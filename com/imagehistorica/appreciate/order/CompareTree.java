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

import com.imagehistorica.common.menu.CxtMenu;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.common.view.TreeTableColumnUtil;
import com.imagehistorica.controller.resources.Rsc;

import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import java.time.LocalDate;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class CompareTree {

    private ImageHistoricaController parentController;
    
    private TreeItem<HistoricaProperty> lastSelectedNode;
    private TreeTableView<HistoricaProperty> leftTree;
    private TreeTableView<HistoricaProperty> rightTree;
    private TreeTableColumn<HistoricaProperty, String> iNameCol_left;
    private TreeTableColumn<HistoricaProperty, LocalDate> lDateCol_left;
    private TreeTableColumn<HistoricaProperty, Integer> freqViewedCol_left;
    private TreeTableColumn<HistoricaProperty, String> urlCol_left;
    private TreeTableColumn<HistoricaProperty, String> realImagePathCol_left;
    private TreeTableColumn<HistoricaProperty, String> iNameCol_right;
    private TreeTableColumn<HistoricaProperty, LocalDate> lDateCol_right;
    private TreeTableColumn<HistoricaProperty, Integer> freqViewedCol_right;
    private TreeTableColumn<HistoricaProperty, String> urlCol_right;
    private TreeTableColumn<HistoricaProperty, String> realImagePathCol_right;
    private TreeTableColumnUtil treeTableColumnUtil = new TreeTableColumnUtil();

    private CxtMenu cxtMenu;
    private Label placeHolder = new Label(Rsc.get("noData"));

    public CompareTree(ImageHistoricaController controller) {
        this.parentController = controller;
        createLayout();
    }

    @SuppressWarnings("unchecked")
    public void createLayout() {
        lastSelectedNode = this.parentController.getAppreciateImage().getLastSelectedNode();
        this.leftTree = new TreeTableView<>(lastSelectedNode);
        this.rightTree = new TreeTableView<>(lastSelectedNode);

        leftTree.setEditable(false);
        leftTree.setShowRoot(true);
        leftTree.setTableMenuButtonVisible(true);
        leftTree.setPlaceholder(placeHolder);
        leftTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        cxtMenu = new CxtMenu(parentController, leftTree);
        cxtMenu.createLayoutAppOdr();
        treeTableColumnUtil = new TreeTableColumnUtil(leftTree, cxtMenu);

        iNameCol_left = treeTableColumnUtil.getImageNameColumn();
        iNameCol_left.setMinWidth(350);
        lDateCol_left = treeTableColumnUtil.getLastModifiedColumn();
        lDateCol_left.setVisible(false);
        freqViewedCol_left = treeTableColumnUtil.getFreqViewedColumn();
        freqViewedCol_left.setVisible(false);
        urlCol_left = treeTableColumnUtil.getUrlColumn();
        urlCol_left.setVisible(false);
        realImagePathCol_left = treeTableColumnUtil.getRealImagePathColumn();
        realImagePathCol_left.setVisible(false);

        rightTree.setEditable(false);
        rightTree.setShowRoot(true);
        rightTree.setTableMenuButtonVisible(true);
        rightTree.setPlaceholder(placeHolder);
        rightTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        iNameCol_right = treeTableColumnUtil.getImageNameColumn();
        iNameCol_right.setMinWidth(350);
        lDateCol_right = treeTableColumnUtil.getLastModifiedColumn();
        lDateCol_right.setVisible(false);
        freqViewedCol_right = treeTableColumnUtil.getFreqViewedColumn();
        freqViewedCol_right.setVisible(false);
        urlCol_right = treeTableColumnUtil.getUrlColumn();
        urlCol_right.setVisible(false);
        realImagePathCol_right = treeTableColumnUtil.getRealImagePathColumn();
        realImagePathCol_right.setVisible(false);

        leftTree.getColumns().addAll(iNameCol_left, lDateCol_left, freqViewedCol_left, urlCol_left, realImagePathCol_left);
        rightTree.getColumns().addAll(iNameCol_right, lDateCol_right, freqViewedCol_right, urlCol_right, realImagePathCol_right);
    }

    public TreeTableView<HistoricaProperty> getLeftTree() {
        return this.leftTree;
    }

    public TreeTableView<HistoricaProperty> getRightTree() {
        return this.rightTree;
    }
}
