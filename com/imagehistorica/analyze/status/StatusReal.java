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

import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.scene.Cursor;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class StatusReal extends BorderPane {

    private final Worker<ObservableList<String>> worker;
    private final TextArea value = new TextArea("");

    public StatusReal(Worker<ObservableList<String>> worker) {
        this.worker = worker;
        createLayout();
    }

    public void createLayout() {
        attachEvents();

        value.setPrefSize(USE_PREF_SIZE, USE_PREF_SIZE);
        value.setEditable(false);
        value.setCursor(Cursor.DEFAULT);
        setCenter(value);
        setCursor(Cursor.DEFAULT);
    }

    private void attachEvents() {
        value.textProperty().bind(worker.valueProperty().asString());
    }

    public TextArea getValue() {
        return this.value;
    }
}
