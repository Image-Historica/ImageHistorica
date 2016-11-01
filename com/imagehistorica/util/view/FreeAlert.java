/*
 * Copyright (C) 2016 和人
 *
 * This program is free software; you can redistribute it and/or
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

import com.imagehistorica.common.state.WebState;
import com.imagehistorica.controller.Controller;
import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.resources.Rsc;
import static com.imagehistorica.util.Constants.WEB_BROWSER_CONTROLLER;
import com.imagehistorica.controllers.WebBrowser;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;

/**
 *
 * @author 和人
 */
public class FreeAlert {

    private final Controller controller;

    public FreeAlert(Controller controller) {
        this.controller = controller;
    }

    public void makeFreeAlert(Factory factory) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(Rsc.get("common_util_CA_title_b"));

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/resources/css/stylesheet.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog");
        GridPane gridH = new GridPane();
        ColumnConstraints graphicColumnH = new ColumnConstraints();
        graphicColumnH.setFillWidth(false);
        graphicColumnH.setHgrow(Priority.NEVER);
        ColumnConstraints textColumnH = new ColumnConstraints();
        textColumnH.setFillWidth(true);
        textColumnH.setHgrow(Priority.NEVER);
        gridH.getColumnConstraints().setAll(graphicColumnH, textColumnH);
        gridH.setPadding(new Insets(5));

        Image image = new Image(getClass().getResourceAsStream("/resources/images/HelpIcon_128.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(64);
        imageView.setFitHeight(64);
        StackPane sp = new StackPane(imageView);
        sp.setAlignment(Pos.CENTER_RIGHT);
        gridH.add(sp, 0, 0);

        Label labelH = new Label(Rsc.get("common_util_CA_header_1"));
        labelH.setWrapText(true);
        labelH.setAlignment(Pos.CENTER);
        labelH.setMaxWidth(Double.MAX_VALUE);
        labelH.setMaxHeight(Double.MAX_VALUE);
        gridH.add(labelH, 1, 0);

        GridPane gridC = new GridPane();
        ColumnConstraints textColumnC = new ColumnConstraints();
        textColumnC.setFillWidth(true);
        textColumnC.setHgrow(Priority.ALWAYS);
        gridC.getColumnConstraints().setAll(textColumnC);
        gridC.setPadding(new Insets(5));

        Label labelC_1 = new Label(Rsc.get("common_util_CA_content_1"));
        labelC_1.setWrapText(true);
        labelC_1.setAlignment(Pos.CENTER_LEFT);
        labelC_1.setMaxWidth(Double.MAX_VALUE);
        labelC_1.setMaxHeight(Double.MAX_VALUE);
        gridC.add(labelC_1, 0, 0);

        Hyperlink link = new Hyperlink("https://image-historica.com/");
        link.setOnMouseClicked(e -> {
            WebState state = WebState.getInstance();
            state.setLoadPage(link.getText());
            ImageHistoricaController c = (ImageHistoricaController) controller;
            c.setWebBrowser((WebBrowser) c.createScene(factory, WEB_BROWSER_CONTROLLER));
        });
        gridC.add(link, 0, 1);

        dialogPane.setHeader(gridH);
        dialogPane.setContent(gridC);
        dialogPane.setGraphic(null);

        ButtonType b1 = new ButtonType(Rsc.get("okBtn"));
        alert.getButtonTypes().setAll(b1);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == b1) {
        }
    }

}
