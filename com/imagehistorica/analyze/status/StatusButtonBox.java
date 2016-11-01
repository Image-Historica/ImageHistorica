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
package com.imagehistorica.analyze.status;

import static com.imagehistorica.util.Constants.*;
import static javafx.concurrent.Worker.State.*;

import com.imagehistorica.common.state.AnalyzeState;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.util.controller.ThreadCreator;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.controllers.AnalyzeResult;
import com.imagehistorica.controllers.AnalyzeStatus;
import com.imagehistorica.databases.Backend;

import javafx.geometry.Pos;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.scene.layout.HBox;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class StatusButtonBox extends HBox {

    private ImageHistoricaController parentController;
    private AnalyzeState state = AnalyzeState.getInstance();
    private AnalyzeStatus analyzeStatus;
    private Service<ObservableList<String>> service;

    private Button startBtn = new Button(Rsc.get("status_BB_start"));
    private Button suspendBtn = new Button(Rsc.get("status_BB_suspend"));
    private Button cancelBtn = new Button(Rsc.get("status_BB_cancel"));
    private Button nextBtn = new Button(Rsc.get("status_BB_next"));

    private boolean onceStarted = false;

    public StatusButtonBox(ImageHistoricaController parentController, AnalyzeStatus analyzeStatus, Service<ObservableList<String>> service) {
        this.parentController = parentController;
        this.analyzeStatus = analyzeStatus;
        this.service = service;
        createLayout();
    }

    public void createLayout() {
        setHgrow(startBtn, Priority.ALWAYS);
        setHgrow(suspendBtn, Priority.ALWAYS);
        setHgrow(cancelBtn, Priority.ALWAYS);
        setHgrow(nextBtn, Priority.ALWAYS);
        startBtn.setMaxWidth(Double.MAX_VALUE);
        suspendBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        nextBtn.setMaxWidth(Double.MAX_VALUE);

        startBtn.setAlignment(Pos.CENTER);
        suspendBtn.setAlignment(Pos.CENTER);
        cancelBtn.setAlignment(Pos.CENTER);
        nextBtn.setAlignment(Pos.CENTER);

        attachEvents();
        attachBinds();

        setSpacing(5);
        setCursor(Cursor.DEFAULT);
        setAlignment(Pos.CENTER);
        getChildren().addAll(startBtn, suspendBtn, cancelBtn, nextBtn);
    }

    protected void attachEvents() {
        startBtn.setOnAction(e -> {
            TreeCache treeCache = TreeCache.getInstance();
            state.setSkipReq(false);
            state.setSuspended(false);

            ThreadCreator t = new ThreadCreator();
            t.createHistoricas();

            if (onceStarted) {
                service.restart();
            } else {
                treeCache.clearHistoricaNumsMapNew();
                treeCache.setNewTreeItem(null);
                state.getProps().clear();
                state.getHistoricas().clear();

                service.start();
                onceStarted = true;
                startBtn.setText(Rsc.get("con_AS_restart"));
            }
        });

        suspendBtn.setOnAction(e -> {
            state.setSuspended(true);
        });

        cancelBtn.setOnAction(e -> {
            state.setSkipReq(true);
            state.setSuspended(false);
            service.cancel();
            Backend.reschedule();
        });

        nextBtn.setOnAction(e -> {
            parentController.setAnalyzeResult((AnalyzeResult) parentController.createScene(analyzeStatus, ANALYZE_RESULT_CONTROLLER));
        });
    }

    private void attachBinds() {
        startBtn.disableProperty().bind(Bindings.or(service.stateProperty().isEqualTo(RUNNING),
                service.stateProperty().isEqualTo(SUCCEEDED)));
        suspendBtn.disableProperty().bind(service.stateProperty().isNotEqualTo(RUNNING));
        cancelBtn.disableProperty().bind(Bindings.or(service.stateProperty().isEqualTo(READY),
                service.stateProperty().isEqualTo(SUCCEEDED)));
        nextBtn.disableProperty().bind(Bindings.or(service.stateProperty().isEqualTo(RUNNING),
                service.stateProperty().isEqualTo(SCHEDULED)));
    }
}
