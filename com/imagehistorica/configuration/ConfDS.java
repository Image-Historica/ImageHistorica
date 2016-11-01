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

package com.imagehistorica.configuration;

import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.Config;
import com.imagehistorica.util.view.CommonAlert;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;

import java.io.File;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class ConfDS extends HBox {

    private ImageHistoricaController parentController;

    private TextField ds;
    private Button dsBtn = new Button(Rsc.get("conf_CDS_ref"));
    private String r_ds_desc = Rsc.get("conf_CDS_ds_desc");
    private String r_a_title = Rsc.get("conf_CDS_a_title");
    private String r_a_header = Rsc.get("conf_CDS_a_header");
    private String r_a_content = Rsc.get("conf_CDS_a_content");
    private String r_a_btn = Rsc.get("conf_CDS_a_btn");

    public ConfDS(ImageHistoricaController parentController) {
        this.parentController = parentController;
        createLayout();
    }

    private void createLayout() {
        ds = new TextField(Config.getImageHistoricaDb() + "");
        ds.setEditable(false);
        ds.setPrefWidth(800);

        attachEvents();

        this.getChildren().addAll(ds, dsBtn);
        HBox.setHgrow(this, Priority.ALWAYS);

    }

    private void attachEvents() {
        dsBtn.setOnAction(e -> {
            String curText = ds.getText();
            final DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle(r_ds_desc);
            File dir = new File(Config.getImageHistoricaDb());
            if (dir.exists()) {
                dc.setInitialDirectory(dir);
            } else {
                dc.setInitialDirectory(new File(System.getProperty("user.dir")));
            }
            final File selectedDirectory = dc.showDialog(parentController.getStage());
            if (selectedDirectory != null) {
                ds.setText(selectedDirectory.getAbsolutePath());
            }

            if (!curText.equals(ds.getText())) {
                Alert alert = CommonAlert.makeAlert(AlertType.WARNING, r_a_title, r_a_header, r_a_content);
                alert.showAndWait();
            }
        });
    }

    public String getDataStore() {
        return this.ds.getText();
    }

    public void setDataStore(String dir) {
        this.ds.setText(dir);
    }
}
