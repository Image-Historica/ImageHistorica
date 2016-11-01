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
public class ConfHistoricaRoot extends HBox {

    private ImageHistoricaController parentController;

    private TextField rootDir;
    private Button rootDirBtn = new Button(Rsc.get("conf_CHR_ref"));
    private String dir_desc = Rsc.get("conf_CHR_dir_desc");

    public ConfHistoricaRoot(ImageHistoricaController parentController) {
        this.parentController = parentController;
        createLayout();
    }

    private void createLayout() {
        rootDir = new TextField(Config.getRootDir());
        rootDir.setEditable(false);
        rootDir.setPrefWidth(800);

        attachEvents();

        this.getChildren().addAll(rootDir, rootDirBtn);
        HBox.setHgrow(this, Priority.ALWAYS);
    }

    private void attachEvents() {
        rootDirBtn.setOnAction(e -> {
            final DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle(dir_desc);
            File dir = new File(Config.getRootDir());
            System.out.println("root: " + Config.getRootDir());
            if (dir.exists()) {
                System.out.println("dir: " + dir.getAbsolutePath());
                dc.setInitialDirectory(dir);
            } else {
                System.out.println("dir: " + dir.getAbsolutePath());
                dc.setInitialDirectory(new File(System.getProperty("user.dir")));
            }
            final File selectedDirectory = dc.showDialog(parentController.getStage());
            if (selectedDirectory != null) {
                rootDir.setText(selectedDirectory.getAbsolutePath());
            }
        });
    }

    public String getRootDir() {
        return this.rootDir.getText();
    }

    public void setRootDir(String dir) {
        this.rootDir.setText(dir);
    }
}
