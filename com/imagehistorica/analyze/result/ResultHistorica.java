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

package com.imagehistorica.analyze.result;

import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.common.state.AnalyzeState;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.common.view.TreeTableColumnUtil;
import com.imagehistorica.util.model.HistoricaProperty;
import static com.imagehistorica.util.model.HistoricaType.ROOT;

import javafx.scene.Cursor;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;

import java.time.LocalDate;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class ResultHistorica extends BorderPane {

    private AnalyzeState state = AnalyzeState.getInstance();
    private TreeCache treeCache = TreeCache.getInstance();

    private TreeTableView<HistoricaProperty> tree;
    private TreeTableView<HistoricaProperty> treeNoListener = null;
    private TreeItem<HistoricaProperty> rootNode = null;
    private TreeTableColumn<HistoricaProperty, String> iNameCol;
    private TreeTableColumn<HistoricaProperty, LocalDate> lDateCol;
    private TreeTableColumn<HistoricaProperty, Integer> freqViewedCol;
    private TreeTableColumn<HistoricaProperty, String> urlCol;
    private TreeTableColumn<HistoricaProperty, String> realImagePathCol;
    private TreeTableColumn<HistoricaProperty, String> iNameColNoListener;
    private TreeTableColumn<HistoricaProperty, LocalDate> lDateColNoListener;
    private TreeTableColumn<HistoricaProperty, Integer> freqViewedColNoListener;
    private TreeTableColumn<HistoricaProperty, String> urlColNoListener;
    private TreeTableColumn<HistoricaProperty, String> realImagePathColNoListener;
    private TreeTableColumnUtil treeTableColumnUtil = new TreeTableColumnUtil();
    private Label placeHolder = new Label(Rsc.get("noData"));

    public ResultHistorica() {
        createLayout();
    }

    @SuppressWarnings("unchecked")
    public void createLayout() {
        rootNode = state.requestNewTreeItem();
        if (rootNode == null) {
            rootNode = new TreeItem<>(new HistoricaProperty("ImageHistorica", "", 0, ROOT, false));
        }

        rootNode.setExpanded(true);

        tree = new TreeTableView<>(rootNode);
        tree.setEditable(false);
        tree.setShowRoot(false);
        tree.setTableMenuButtonVisible(true);
        tree.setPlaceholder(placeHolder);

        iNameCol = treeTableColumnUtil.getImageNameColumn();
        lDateCol = treeTableColumnUtil.getLastModifiedColumn();
        freqViewedCol = treeTableColumnUtil.getFreqViewedColumn();
        urlCol = treeTableColumnUtil.getUrlColumn();
        realImagePathCol = treeTableColumnUtil.getRealImagePathColumn();

        tree.getColumns().addAll(iNameCol, lDateCol, freqViewedCol, urlCol, realImagePathCol);

        treeNoListener = new TreeTableView<>(rootNode);
        treeNoListener.setEditable(false);
        treeNoListener.setShowRoot(false);
        treeNoListener.setTableMenuButtonVisible(true);
        treeNoListener.setPlaceholder(placeHolder);

        iNameColNoListener = treeTableColumnUtil.getImageNameColumn();
        lDateColNoListener = treeTableColumnUtil.getLastModifiedColumn();
        freqViewedColNoListener = treeTableColumnUtil.getFreqViewedColumn();
        urlColNoListener = treeTableColumnUtil.getUrlColumn();
        realImagePathColNoListener = treeTableColumnUtil.getRealImagePathColumn();

        treeNoListener.getColumns().addAll(iNameColNoListener, lDateColNoListener, freqViewedColNoListener, urlColNoListener, realImagePathColNoListener);

        this.setCursor(Cursor.DEFAULT);
        this.setCenter(tree);
    }

    public TreeTableView<HistoricaProperty> getTree() {
        return this.tree;
    }

    public TreeTableView<HistoricaProperty> getTreeNoListener() {
        return this.treeNoListener;
    }

    public TreeTableColumnUtil getTreeTableColumnUtil() {
        return this.treeTableColumnUtil;
    }

    public TreeItem<HistoricaProperty> getRootNode() {
        return this.rootNode;
    }
}
