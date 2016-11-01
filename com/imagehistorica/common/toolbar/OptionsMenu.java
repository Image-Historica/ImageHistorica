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
package com.imagehistorica.common.toolbar;

import static com.imagehistorica.util.Constants.OPTIONS;

import com.imagehistorica.configuration.Configuration;
import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.Config;
import com.imagehistorica.Key;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class OptionsMenu extends MenuButton {

    private final ImageHistoricaController parentController;
    private final Factory factory;

    private MenuItem toTopItem = new MenuItem(Rsc.get("toolbar_OM_toTop"));
    private MenuItem optionsItem = new MenuItem(Rsc.get("toolbar_OM_options"));
    private MenuItem helpItem = new MenuItem(Rsc.get("toolbar_OM_help"));

    private SVGPath svgOptions = new SVGPath();

    public OptionsMenu(ImageHistoricaController parentController, Factory factory) {
        this.parentController = parentController;
        this.factory = factory;
        createLayout();
    }

    public void createLayout() {
        svgOptions.setStroke(Color.ORANGE);
        svgOptions.setContent(OPTIONS);
        this.setGraphic(svgOptions);

        attachEvents();

        this.getItems().addAll(toTopItem, optionsItem, helpItem);
    }

    private void attachEvents() {
        toTopItem.setOnAction(e -> {
            parentController.restoreScene(parentController.getInitialScene());
        });

        optionsItem.setOnAction(e -> {
            Configuration conf = new Configuration(parentController);
            conf.createLayout();
        });

        helpItem.setOnAction(e -> {
            String acsKey = Key.accessKey;
            String contact = "https://image-historica.com/contact";

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(parentController.getStage());
            alert.setTitle(Rsc.get("toolbar_OM_title"));
            alert.getDialogPane().setContentText(Rsc.get("toolbar_OM_content_1") + Config.getVersion() + "\n"
                    + Rsc.get("toolbar_OM_content_2") + acsKey + "\n"
                    + Rsc.get("toolbar_OM_content_3") + "\n  " + contact
            );

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    getClass().getResource("/resources/css/stylesheet.css").toExternalForm());
            dialogPane.getStyleClass().add("dialog");
            GridPane grid = new GridPane();
            ColumnConstraints graphicColumn = new ColumnConstraints();
            graphicColumn.setFillWidth(false);
            graphicColumn.setHgrow(Priority.NEVER);
            ColumnConstraints textColumn = new ColumnConstraints();
            textColumn.setFillWidth(true);
            textColumn.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().setAll(graphicColumn, textColumn);
            grid.setPadding(new Insets(5));

            Image image = new Image(getClass().getResourceAsStream("/resources/images/HelpIcon_128.png"));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(64);
            imageView.setFitHeight(64);
            StackPane stackPane = new StackPane(imageView);
            stackPane.setAlignment(Pos.CENTER);
            grid.add(stackPane, 0, 0);

            Label headerLabel = new Label(Rsc.get("toolbar_OM_header"));
            headerLabel.setWrapText(true);
            headerLabel.setAlignment(Pos.CENTER_RIGHT);
            headerLabel.setMaxWidth(Double.MAX_VALUE);
            headerLabel.setMaxHeight(Double.MAX_VALUE);
            grid.add(headerLabel, 1, 0);

            dialogPane.setHeader(grid);
            dialogPane.setGraphic(null);

            ButtonType b1 = new ButtonType(Rsc.get("okBtn"));
            alert.getButtonTypes().setAll(b1);
            alert.showAndWait();
        });
    }
}
