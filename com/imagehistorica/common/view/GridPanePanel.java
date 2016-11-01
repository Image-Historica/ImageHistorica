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

import javafx.scene.Cursor;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class GridPanePanel extends GridPane {

    public GridPanePanel() {
    }

    public GridPanePanel(int column, int row, double stageWidth, double stageHeight) {
        createLayout(column, row, stageWidth, stageHeight);
    }

    public void createLayout(int column, int row, double stageWidth, double stageHeight) {
        this.setHgap(5);
        this.setVgap(5);
        this.setCursor(Cursor.DEFAULT);
//        this.setGridLinesVisible(true);

//        double wPercent = (((stageWidth - 10) / stageWidth) * 100);
        double wPercent = (((stageWidth - 5) / stageWidth) * 100);
        for (int i = 0; i < column; i++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(wPercent);
            this.getColumnConstraints().add(constraints);
        }

        double hPercent = (((stageHeight - 45 - 5) / stageHeight) * 100) / (row - 1);
        for (int i = 0; i < row; i++) {
            if (i == 0) {
                RowConstraints constraints = new RowConstraints();
                constraints.setPrefHeight(50);
                this.getRowConstraints().add(constraints);

            } else {
                RowConstraints constraints = new RowConstraints();
                constraints.setPercentHeight(hPercent);
                this.getRowConstraints().add(constraints);
            }
        }
    }

    protected ColumnConstraints columnWithPercentage(final double percentage) {
        final ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(percentage);
        return constraints;
    }

    protected RowConstraints rowWithPercentage(final double percentage) {
        final RowConstraints constraints = new RowConstraints();
        constraints.setPercentHeight(percentage);
        return constraints;
    }
}
