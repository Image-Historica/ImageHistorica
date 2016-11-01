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
package com.imagehistorica.util.view;

import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.Backend;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class CommonAlert {

    private static final Logger logger = LoggerFactory.getLogger(CommonAlert.class);

    public void showAnyError(String header, String content) {
        Alert alert = makeAlert(Alert.AlertType.ERROR, Rsc.get("common_util_CA_title_a"), header, content);
        alert.showAndWait();
    }

    public void showOOMAlert() {
        Alert alert = makeAlert(Alert.AlertType.ERROR, Rsc.get("common_util_CA_title_a"), Rsc.get("common_util_CA_header_2"), null);
        alert.showAndWait();
    }

    public void showNoWritePermission(String path) {
        Alert alert = makeAlert(Alert.AlertType.ERROR, Rsc.get("common_util_CA_title_a"), Rsc.get("common_util_CA_header_3"), path + Rsc.get("common_util_CA_content_3"));
        alert.showAndWait();
    }

    public void showIOException(String fileName) {
        Alert alert = makeAlert(Alert.AlertType.ERROR, Rsc.get("common_util_CA_title_a"), Rsc.get("common_util_CA_header_4_a")
                + fileName + Rsc.get("common_util_CA_header_4_b"), Rsc.get("common_util_CA_content_4"));
        alert.showAndWait();
    }

    public static void getDebugLog(String e) {
        logger.debug("{}", e);
    }

    public static void getErrorLog(Throwable e) {
        logger.error("", e);
    }

    public static Alert makeAlert(Alert.AlertType type, String title, String header, String content, ButtonType... buttons) {
        Alert alert = new Alert(type);
        alert.setResizable(true);
        alert.getDialogPane().getStylesheets().add(Backend.class.
                getResource("/resources/css/stylesheet.css").toExternalForm());

        if (title != null) {
            alert.setTitle(title);
        }
        if (header != null) {
            alert.setHeaderText(header);
        }
        if (content != null) {
            alert.getDialogPane().setContent(new Label(content));
        }

        if (buttons.length > 0) {
            alert.getButtonTypes().setAll(buttons);
        } else {
            ButtonType b1 = new ButtonType("OK");
            alert.getButtonTypes().setAll(b1);
        }

        return alert;
    }
}
