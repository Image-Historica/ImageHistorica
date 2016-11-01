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

import com.imagehistorica.controller.resources.Rsc;
import javafx.beans.binding.When;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class StatusProcess extends GridPane {

    private final Worker<ObservableList<String>> worker;
    private final StatusException exceptionState;

    private final Label title = new Label("");
    private final Label message = new Label("");
    private final Label running = new Label("");
    private final Label state = new Label("");
    private final Label totalWork = new Label("");
    private final Label workDone = new Label("");
    private final Label progress = new Label("");
    private final ProgressBar progressBar = new ProgressBar();

    public StatusProcess(Worker<ObservableList<String>> worker, StatusException exceptionState) {
        this.worker = worker;
        this.exceptionState = exceptionState;
        createLayout();
    }

    private void createLayout() {
        setHgap(5);
        setVgap(5);
        addRow(0, new Label("Title:"), title);
        addRow(1, new Label("Message:"), message);
        addRow(2, new Label(Rsc.get("status_SP_progStatus")), running);
        addRow(3, new Label(Rsc.get("status_SP_status")), state);
        addRow(4, new Label(Rsc.get("status_SP_total")), totalWork);
        addRow(5, new Label(Rsc.get("status_SP_workDone")), workDone);
        addRow(6, new Label(Rsc.get("status_SP_progress")), new HBox(2, progressBar, progress));
        setCursor(Cursor.DEFAULT);
        setMouseTransparent(true);

        attachBinds();
    }

    public void attachBinds() {
        title.textProperty().bind(worker.titleProperty());
        message.textProperty().bind(worker.messageProperty());
        running.textProperty().bind(worker.runningProperty().asString());
        state.textProperty().bind(worker.stateProperty().asString());
        totalWork.textProperty().bind(new When(worker.totalWorkProperty().isEqualTo(-1))
                .then("Unknown")
                .otherwise(worker.totalWorkProperty().asString()));
        workDone.textProperty().bind(new When(worker.workDoneProperty().isEqualTo(-1))
                .then("Unknown")
                .otherwise(worker.workDoneProperty().asString()));
        progress.textProperty().bind(new When(worker.progressProperty().isEqualTo(-1))
                .then("Unknown")
                .otherwise(worker.progressProperty().multiply(100.0).asString("%.2f%%")));
        progressBar.progressProperty().bind(worker.progressProperty());

        worker.exceptionProperty().addListener((ob, oldVal, newVal) -> {
            if (newVal != null) {
                exceptionState.getValue().setText(newVal.getMessage());
            } else {
                exceptionState.getValue().setText("");
            }
        });
    }
}
