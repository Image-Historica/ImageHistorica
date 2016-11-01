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

package com.imagehistorica.common.menu.embody;

import com.imagehistorica.controller.resources.Rsc;

import javafx.beans.binding.When;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class EmbodyProcess extends GridPane {

    private final Worker<ObservableList<String>> worker;

    private final Label status = new Label("");
    private final Label message = new Label("");
    private final Label totalWork = new Label("");
    private final Label workDone = new Label("");
    private final Label progress = new Label("");
    private final TextArea srcdst = new TextArea("");
    private final TextArea exception = new TextArea("");
    private final ProgressBar progressBar = new ProgressBar();

    public EmbodyProcess(Worker<ObservableList<String>> worker) {
        this.worker = worker;
        createLayout();
    }

    private void createLayout() {
        Label l0 = new Label(Rsc.get("common_menu_EP_status"));
        Label l1 = new Label(Rsc.get("common_menu_EP_progStatus"));
        Label l2 = new Label(Rsc.get("common_menu_EP_progress"));
        Label l3 = new Label(Rsc.get("common_menu_EP_total"));
        Label l4 = new Label(Rsc.get("common_menu_EP_workDone"));
        Label l5 = new Label(Rsc.get("common_menu_EP_result"));
        Label l6 = new Label("");
        Label l7 = new Label(Rsc.get("common_menu_EP_error"));

        HBox hbox = new HBox(2, progressBar, progress);

        GridPane.setConstraints(l0, 0, 1, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(l1, 0, 2, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(l2, 0, 3, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(l3, 0, 4, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(l4, 0, 5, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(l5, 0, 6, 1, 6, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(l6, 0, 7, 1, 6, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(l7, 0, 8, 1, 2, HPos.LEFT, VPos.TOP);

        GridPane.setConstraints(status, 1, 1, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(message, 1, 2, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(totalWork, 1, 3, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(workDone, 1, 4, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(hbox, 1, 5, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(srcdst, 1, 6, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(exception, 1, 8, 1, 1, HPos.LEFT, VPos.TOP);

        l0.setPrefWidth(120);
        l1.setPrefWidth(120);
        l2.setPrefWidth(120);
        l3.setPrefWidth(120);
        l4.setPrefWidth(120);
        l5.setPrefWidth(120);
        l6.setPrefWidth(120);
        l7.setPrefWidth(120);

        srcdst.setPrefColumnCount(40);
        srcdst.setPrefRowCount(12);
        srcdst.setWrapText(false);
        srcdst.setEditable(false);

        exception.setPrefColumnCount(40);
        exception.setPrefRowCount(5);
        exception.setWrapText(false);
        exception.setEditable(false);

        attachBinds();
        
        this.setHgap(5);
        this.setVgap(7);
        this.setPadding(new Insets(10, 10, 10, 10));

        this.getChildren().addAll(l0, l1, l2, l3, l4, l5, l6, l7, status, message, totalWork, workDone, hbox, srcdst, exception);
    }

    public void attachBinds() {
        message.textProperty().bind(worker.messageProperty());
        status.textProperty().bind(worker.stateProperty().asString());
        totalWork.textProperty().bind(new When(worker.totalWorkProperty().isEqualTo(-1))
                .then("Unknown")
                .otherwise(worker.totalWorkProperty().asString()));
        workDone.textProperty().bind(new When(worker.workDoneProperty().isEqualTo(-1))
                .then("Unknown")
                .otherwise(worker.workDoneProperty().asString()));
        progress.textProperty().bind(new When(worker.progressProperty().isEqualTo(-1))
                .then("Unknown")
                .otherwise(worker.progressProperty().multiply(100.0)
                        .asString("%.2f%%")));
        progressBar.progressProperty().bind(worker.progressProperty());

        srcdst.textProperty().bind(new When(worker.progressProperty().isEqualTo(-1))
                .then("")
                .otherwise(worker.valueProperty().asString()));

        worker.exceptionProperty().addListener((ob, oldVal, newVal) -> {
            if (newVal != null) {
                exception.setText(newVal.getMessage());
            } else {
                exception.setText("");
            }
        });
    }

    public TextArea getException() {
        return this.exception;
    }
}
