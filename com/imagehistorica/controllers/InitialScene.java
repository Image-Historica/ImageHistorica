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

import static com.imagehistorica.util.Constants.APPRECIATE_IMAGE_CONTROLLER;
import static com.imagehistorica.util.Constants.ANALYZE_IMAGE_CONTROLLER;

import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.ControllerType;
import com.imagehistorica.controller.type.None;
import com.imagehistorica.util.view.ResizeStage;
import com.imagehistorica.common.view.GridPanePanel;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.Config;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class InitialScene extends Factory implements None {

    private ImageHistoricaController parentController;
    private VBox vbox = new VBox();
    private BorderPane bp = new BorderPane();

    private GridPanePanel grid;
    private int gridColumn = 5;
    private int gridRow = 4;

    private Label analyzeImage = new Label(Rsc.get("con_IS_analyzeImage"));
    private Label appreciateImage = new Label(Rsc.get("con_IS_appreciateImage"));
    private Label close = new Label(Rsc.get("con_IS_close"));
    private Label space = new Label("\n\n");

    private ImageView imageView = new ImageView(new Image(
            getClass().getResourceAsStream("/resources/images/image-historica.png"),
            Config.getDefaultWidth() * 0.95,
            Config.getDefaultHeight() * 0.6,
            true, true));

    @Override
    public Scene createLayout(ImageHistoricaController parentController) {
        this.parentController = parentController;

        analyzeImage.setStyle("-fx-text-fill: gray;");
        appreciateImage.setStyle("-fx-text-fill: gray;");
        close.setStyle("-fx-text-fill: gray;");

        vbox.getChildren().addAll(analyzeImage, appreciateImage, close, space);
        vbox.setAlignment(Pos.BOTTOM_CENTER);

        createPanel();
        grid.setId("initial-scene");

        scene = new Scene(grid);
        setScene(scene);

        resizeStage();
        attachEvents();

        return scene;
    }

    @Override
    public Scene restoreScene() {
        return this.scene;
    }

    @Override
    protected void attachEvents() {
        analyzeImage.setOnMouseEntered(e -> {
            analyzeImage.setStyle("-fx-text-fill: orange;");
        });

        analyzeImage.setOnMouseExited(e -> {
            analyzeImage.setStyle("-fx-text-fill: gray;");
        });

        analyzeImage.setOnMouseClicked(e -> {
            AnalyzeImage analyzeImage = parentController.getAnalyzeImage();
            if (analyzeImage == null) {
                parentController.setAnalyzeImage((AnalyzeImage) parentController.createScene(this, ANALYZE_IMAGE_CONTROLLER));
            } else {
                Backend.setPrevController(this);
                Backend.getNextController(analyzeImage);
                parentController.restoreScene(analyzeImage);
            }
        });

        appreciateImage.setOnMouseEntered(e -> {
            appreciateImage.setStyle("-fx-text-fill: orange;");
        });

        appreciateImage.setOnMouseExited(e -> {
            appreciateImage.setStyle("-fx-text-fill: gray;");
        });

        appreciateImage.setOnMouseClicked(e -> {
            AppreciateImage appreciateImage = parentController.getAppreciateImage();
            if (appreciateImage == null) {
                parentController.setAppreciateImage((AppreciateImage) parentController.createScene(this, APPRECIATE_IMAGE_CONTROLLER));
            } else {
                Backend.setPrevController(this);
                Backend.getNextController(appreciateImage);
                parentController.restoreScene(appreciateImage);
            }
        });

        close.setOnMouseEntered(e -> {
            close.setStyle("-fx-text-fill: orange;");
        });

        close.setOnMouseExited(e -> {
            close.setStyle("-fx-text-fill: gray;");
        });

        close.setOnMouseClicked(e -> {
            parentController.closeRequest();
        });
    }

    @Override
    public ControllerType getControllerType() {
        return controllerType;
    }

    @Override
    protected void resizeStage() {
        ResizeStage.setResizeStage(this.parentController.getStage(), this, grid);
    }

    @Override
    public void adjustLayout() {
    }

    private void createPanel() {
        double width = parentController.getStage().getWidth();
        double height = parentController.getStage().getHeight();
        grid = new GridPanePanel(gridColumn, gridRow, width, height);
        imageView.fitWidthProperty().bind(grid.widthProperty().multiply(0.75).subtract(10));
        imageView.fitHeightProperty().bind(grid.heightProperty().multiply(0.5).subtract(30));
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        GridPane.setConstraints(imageView, 1, 1, 3, 2, HPos.CENTER, VPos.CENTER);
        GridPane.setConstraints(vbox, 2, 3, 1, 1, HPos.CENTER, VPos.CENTER);

        grid.getChildren().addAll(imageView, vbox);
    }
}
