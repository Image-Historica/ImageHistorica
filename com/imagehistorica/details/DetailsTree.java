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
package com.imagehistorica.details;

import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controllers.AnalyzeResult;
import com.imagehistorica.controllers.AppreciateImage;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.common.view.TreeTableColumnUtil;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.Backend;

import javafx.scene.control.TreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;

import java.time.LocalDate;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class DetailsTree extends BorderPane {

    private ImageHistoricaController controller = null;
    private TreeTableView<HistoricaProperty> treeTableView;
    private TreeItem<HistoricaProperty> rootNode;
    private TreeTableColumn<HistoricaProperty, String> iNameCol;
    private TreeTableColumn<HistoricaProperty, LocalDate> lDateCol;
    private TreeTableColumn<HistoricaProperty, Integer> lengthCol;
    private TreeTableColumn<HistoricaProperty, Integer> freqViewedCol;
    private TreeTableColumn<HistoricaProperty, String> urlCol;
    private TreeTableColumn<HistoricaProperty, String> realImagePathCol;
    private TreeTableColumnUtil treeTableColumnUtil = new TreeTableColumnUtil();
    private Label placeHolder = new Label(Rsc.get("noData"));

    public DetailsTree(ImageHistoricaController controller) {
        this.controller = controller;

    }

    public DetailsTree(ImageHistoricaController controller, TreeTableView<HistoricaProperty> treeTableView) {
        this.controller = controller;
        this.treeTableView = treeTableView;

        if (Backend.checkPrevController() instanceof AnalyzeResult) {
            createLayoutOfAnalyzeResult();
        } else if (Backend.checkPrevController() instanceof AppreciateImage) {
            this.treeTableView.setOnSort(e -> {
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
            createLayoutOfAppreciateImage();
        }
    }

    public void createLayoutOfAnalyzeResult() {
        this.setCenter(treeTableView);
    }

    @SuppressWarnings("unchecked")
    public void createLayoutOfAppreciateImage() {
        treeTableView.setEditable(false);
        treeTableView.setShowRoot(true);
        treeTableView.setTableMenuButtonVisible(true);
        treeTableView.setPlaceholder(placeHolder);

        iNameCol = treeTableColumnUtil.getImageNameColumn();
        iNameCol.setMinWidth(270);
        lDateCol = treeTableColumnUtil.getLastModifiedColumn();
        lDateCol.setVisible(false);
        lengthCol = treeTableColumnUtil.getLengthColumn();
        lengthCol.setVisible(false);
        freqViewedCol = treeTableColumnUtil.getFreqViewedColumn();
        freqViewedCol.setVisible(false);
        urlCol = treeTableColumnUtil.getUrlColumn();
        urlCol.setVisible(false);
        realImagePathCol = treeTableColumnUtil.getRealImagePathColumn();
        realImagePathCol.setVisible(false);

        treeTableView.getColumns().addAll(iNameCol, lDateCol, lengthCol, freqViewedCol, urlCol, realImagePathCol);
        this.setCenter(treeTableView);
    }

    public TreeTableView<HistoricaProperty> getTreeTableView() {
        return this.treeTableView;
    }
}
