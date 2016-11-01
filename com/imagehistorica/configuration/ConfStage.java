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

package com.imagehistorica.configuration;

import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.resources.Rsc;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class ConfStage extends VBox {

    private ImageHistoricaController parentController;
    private double widthCurrent;
    private double heightCurrent;
    private double widthDisplay;
    private double heightDisplay;

    private HBox boxH = new HBox();
    private HBox boxW = new HBox();
    private Label r_width = new Label(Rsc.get("conf_CS_width"));
    private Label r_height = new Label(Rsc.get("conf_CS_height"));
    private Label stageW = new Label();
    private Label stageH = new Label();
    private Slider sliderW;
    private Slider sliderH;

    private final Logger logger = LoggerFactory.getLogger(ConfStage.class);

    public ConfStage(ImageHistoricaController parentController) {
        this.parentController = parentController;
        this.widthCurrent = Math.floor(this.parentController.getStage().getWidth());
        this.heightCurrent = Math.floor(this.parentController.getStage().getHeight());
        this.widthDisplay = Math.floor(Screen.getPrimary().getVisualBounds().getWidth());
        this.heightDisplay = Math.floor(Screen.getPrimary().getVisualBounds().getHeight());
        createLayout();
    }

    public void createLayout() {
        sliderW = new Slider(100, widthDisplay, widthCurrent);
        sliderH = new Slider(200, heightDisplay, heightCurrent);

        sliderW.setPrefWidth(700);
        sliderW.setOrientation(Orientation.HORIZONTAL);
        sliderW.setShowTickMarks(true);
        sliderW.setShowTickLabels(true);
        sliderW.setMajorTickUnit(400.0f);
        sliderW.setBlockIncrement(10.0f);
        stageW.setText(String.valueOf(sliderW.getValue()));

        boxW.getChildren().addAll(r_width, sliderW, stageW);

        sliderH.setPrefWidth(700);
        sliderH.setOrientation(Orientation.HORIZONTAL);
        sliderH.setShowTickMarks(true);
        sliderH.setShowTickLabels(true);
        sliderH.setMajorTickUnit(200.0f);
        sliderH.setBlockIncrement(10.0f);
        stageH.setText(String.valueOf(sliderH.getValue()));

        boxH.getChildren().addAll(r_height, sliderH, stageH);

        attachEvents();

        this.setSpacing(20.0);
        this.getChildren().addAll(boxW, boxH);
    }

    private void attachEvents() {
        sliderW.setOnMouseClicked(e -> {
            double w = Math.floor(sliderW.getValue());
            adjustWidth(w);
        });

        sliderW.setOnKeyReleased(e -> {
            double w = Math.floor(sliderW.getValue());
            adjustWidth(w);
        });

        sliderH.setOnMouseClicked(e -> {
            double h = Math.floor(sliderH.getValue());
            adjustHeight(h);
        });

        sliderH.setOnKeyReleased(e -> {
            double h = Math.floor(sliderH.getValue());
            adjustHeight(h);
        });
    }

    public double getWidthCurrent() {
        return Math.floor(sliderW.getValue());
    }

    public double getHeightCurrent() {
        return Math.floor(sliderH.getValue());
    }

    public void adjustWidth(double width) {
        sliderW.setValue(width);
        stageW.setText(String.valueOf(width));
        parentController.getStage().setWidth(width);
    }

    public void adjustHeight(double height) {
        sliderH.setValue(height);
        stageH.setText(String.valueOf(height));
        parentController.getStage().setHeight(height);
    }
}
