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

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class StatusException extends BorderPane {

    private final Label exception = new Label(Rsc.get("status_ES_ex1"));
    private final TextArea value = new TextArea("");

    public StatusException() {
        createLayout();
    }

    public void createLayout() {
        value.setWrapText(true);
        value.setPrefHeight(2000);
        value.setEditable(false);

        setTop(exception);
        setCenter(value);
        setCursor(Cursor.DEFAULT);
    }
    
    public TextArea getValue() {
        return this.value;
    }
}
