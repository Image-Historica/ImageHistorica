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

import com.imagehistorica.analyze.status.StatusButtonBox;
import com.imagehistorica.common.view.GridPanePanel;
import com.imagehistorica.analyze.status.StatusProcessTask;
import com.imagehistorica.analyze.status.StatusHistorica;
import com.imagehistorica.analyze.status.StatusException;
import com.imagehistorica.analyze.status.StatusProcess;
import com.imagehistorica.analyze.status.StatusReal;
import com.imagehistorica.common.toolbar.GreenToolBar;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.ControllerType;
import com.imagehistorica.controller.type.Green;
import com.imagehistorica.util.view.ResizeStage;
import javafx.application.Platform;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class AnalyzeStatus extends Factory implements Green {

    private ImageHistoricaController parentController;
    private GreenToolBar toolMenuBar;
    private GridPanePanel grid;
    private int gridColumn = 4;
    private int gridRow = 7;

    private StatusException exceptionState = new StatusException();
    private StatusHistorica analyzeState = new StatusHistorica();
    private StatusReal requestState;
    private StatusProcess processState;
    private StatusButtonBox buttonBox;

    public AnalyzeStatus() {
        Backend.initializeProcNum();
    }

    public final Service<ObservableList<String>> service = new Service<ObservableList<String>>() {
        @Override
        public Task<ObservableList<String>> createTask() {
            return new StatusProcessTask(analyzeState, exceptionState, Backend.getQueueLength());
        }
    };

    @Override
    public Scene createLayout(ImageHistoricaController parentController) {
        this.parentController = parentController;
        toolMenuBar = new GreenToolBar(this.parentController, this);
        processState = new StatusProcess(service, exceptionState);
        buttonBox = new StatusButtonBox(this.parentController, this, service);
        requestState = new StatusReal(service);

        analyzeState.getStyleClass().add("analyze-status");
        exceptionState.getStyleClass().add("analyze-status");
        buttonBox.getStyleClass().add("analyze-status");
        processState.getStyleClass().add("analyze-status");
        requestState.getStyleClass().add("analyze-status");

        createPanel();

        scene = new Scene(grid);
        setScene(scene);

        resizeStage();
        attachEvents();

        Platform.runLater(() -> buttonBox.requestFocus());

        return scene;
    }

    @Override
    public Scene restoreScene() {
        return this.scene;
    }

    @Override
    protected void attachEvents() {
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
        createPanel();

        scene = new Scene(grid);
        setScene(this.parentController.applyStyleSheet(scene));

        resizeStage();
        attachEvents();

        this.parentController.restoreScene(this.parentController.getAnalyzeStatus());
    }

    public Service<ObservableList<String>> getService() {
        return this.service;
    }

    private void createPanel() {
        double width = this.parentController.getStage().getWidth();
        double height = this.parentController.getStage().getHeight();
        grid = new GridPanePanel(gridColumn, gridRow, width, height);

        GridPane.setConstraints(toolMenuBar, 0, 0, 4, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(processState, 0, 1, 2, 2, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(exceptionState, 0, 3, 2, 3, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(buttonBox, 0, 6, 2, 1, HPos.CENTER, VPos.CENTER);
        GridPane.setConstraints(requestState, 2, 1, 2, 3, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(analyzeState, 2, 4, 2, 3, HPos.LEFT, VPos.TOP);

        grid.getChildren().addAll(toolMenuBar, processState, exceptionState, buttonBox, analyzeState, requestState);
    }
}
