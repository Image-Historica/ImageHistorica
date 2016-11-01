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

import static com.imagehistorica.util.Constants.DELIMITER;
import static com.imagehistorica.common.menu.embody.EmbodyHistoricaType.*;
import static javafx.concurrent.Worker.State.*;

import com.imagehistorica.common.state.EmbodyState;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.util.controller.ThreadCreator;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.databases.model.Historica;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class EmbodyHistorica {

    public EmbodyProcess process;

    private TreeCache treeCache = TreeCache.getInstance();
    private final EmbodyState state = new EmbodyState();

    private final Stage dialogStage = new Stage();
    private final String title;
    private final EmbodyHistoricaType type;
    private final boolean pathIncluding;
    private final File dstDirectory;
    private final List<Historica> historicas;
    private final String fileSeparator;
    private final String fileSeparatorForReplace;
    private final String eliminatePath;

    private final Map<String, String> paths = new HashMap<>();
    private final Map<String, Integer> historicaIds = new HashMap<>();
    private Scene scene;

    private HBox btnBox;
    private BorderPane bp = new BorderPane();

    private Button startBtn = new Button(Rsc.get("common_menu_EH_start"));
    private Button resumeBtn = new Button(Rsc.get("common_menu_EH_cancel"));
    private Button resetBtn = new Button(Rsc.get("common_menu_EH_reset"));
    private Button exitBtn = new Button(Rsc.get("common_menu_EH_exit"));
    private boolean onceStarted = false;

    private final Image img16 = new Image(getClass().getResourceAsStream("/resources/images/ImageHistorica_16.png"));
    private final Image img32 = new Image(getClass().getResourceAsStream("/resources/images/ImageHistorica_32.png"));

    private final Logger logger = LoggerFactory.getLogger(EmbodyDialogs.class);

    Service<ObservableList<String>> service = new Service<ObservableList<String>>() {
        @Override
        protected Task<ObservableList<String>> createTask() {
            return new EmbodyProcessTask(process, state, paths.size());
        }
    };

    // For HistoricaType ROOT
    public EmbodyHistorica(String title, EmbodyHistoricaType type, boolean pathIncluding,
            List<Historica> historicas, File dstDirectory, String fileSeparator) {

        this.title = title;
        this.type = type;
        this.pathIncluding = pathIncluding;
        this.historicas = historicas;
        this.dstDirectory = dstDirectory;
        this.eliminatePath = null;
        this.fileSeparator = fileSeparator;
        if (fileSeparator.equals("\\")) {
            this.fileSeparatorForReplace = "\\\\";
        } else {
            this.fileSeparatorForReplace = fileSeparator;
        }

        this.historicas.stream().forEach((historica) -> {
            String realImagePath = Backend.getImagePath(historica.getRealImageId());
            String historicaPath = treeCache.getHistoricaDirsMap(historica.getHistoricaDirId());
            String historicaFullPath = "ImageHistorica" + this.fileSeparator + historicaPath.replaceAll(DELIMITER, this.fileSeparatorForReplace)
                    + this.fileSeparator + historica.getImageName() + "." + historica.getExt().toLowerCase();
            paths.put(realImagePath, historicaFullPath);
            historicaIds.put(realImagePath, historica.getHistoricaId());
        });

        createLayout();
    }

    public EmbodyHistorica(String title, EmbodyHistoricaType type, boolean pathIncluding,
            List<Historica> historicas, File dstDirectory, String fileSeparator, String eliminatePath) {

        logger.debug("EmbodyHistorica()...eliminatePath: {}", eliminatePath);
        this.title = title;
        this.type = type;
        this.pathIncluding = pathIncluding;
        this.historicas = historicas;
        this.dstDirectory = dstDirectory;
        this.eliminatePath = eliminatePath;
        this.fileSeparator = fileSeparator;
        if (fileSeparator.equals("\\")) {
            this.fileSeparatorForReplace = "\\\\";
        } else {
            this.fileSeparatorForReplace = fileSeparator;
        }

        this.historicas.stream().forEach((historica) -> {
            if (type == COPY_HISTORICA_WITH_PATH || (type == MOVE_HISTORICA_WITH_PATH)) {
                String realImagePath = Backend.getImagePath(historica.getRealImageId());
                String historicaPath = treeCache.getHistoricaDirsMap(historica.getHistoricaDirId());
                logger.debug("EmbodyHistorica()... historicaPath: {}", historicaPath);
                String eliminatedPath = historicaPath.replaceFirst(this.eliminatePath, "");
                logger.debug("EmbodyHistorica()...eliminatePath: {}", eliminatePath);
                String historicaFullPath = eliminatedPath.replaceAll(DELIMITER, this.fileSeparatorForReplace) + this.fileSeparator
                        + historica.getImageName() + "." + historica.getExt().toLowerCase();
                paths.put(realImagePath, historicaFullPath);
                historicaIds.put(realImagePath, historica.getHistoricaId());
            } else if (type == COPY_HISTORICA_ONLY || (type == MOVE_HISTORICA_ONLY)) {
                if (pathIncluding) {
                    String realImagePath = Backend.getImagePath(historica.getRealImageId());
                    String historicaPath = treeCache.getHistoricaDirsMap(historica.getHistoricaDirId());
                    logger.debug("EmbodyHistorica()... historicaPath: {}", historicaPath);
                    String historicaFullPath = historicaPath.replaceAll(DELIMITER, this.fileSeparatorForReplace) + this.fileSeparator
                            + historica.getImageName() + "." + historica.getExt().toLowerCase();
                    paths.put(realImagePath, historicaFullPath);
                    historicaIds.put(realImagePath, historica.getHistoricaId());
                } else {
                    String realImagePath = Backend.getImagePath(historica.getRealImageId());
                    paths.put(realImagePath, historica.getImageName() + "." + historica.getExt().toLowerCase());
                    historicaIds.put(realImagePath, historica.getHistoricaId());
                }
            }
        });

        createLayout();
    }

    private void createLayout() {
        process = new EmbodyProcess(service);
        btnBox = new HBox(8, startBtn, resumeBtn, resetBtn, exitBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        bp.setCenter(process);
        bp.setBottom(btnBox);

        scene = new Scene(bp);
        String style = EmbodyHistorica.class.
                getResource("/resources/css/stylesheet.css").toExternalForm();
        scene.getStylesheets().add(style);

        dialogStage.getIcons().addAll(img16, img32);
        dialogStage.setScene(scene);
        dialogStage.setTitle(title);
        dialogStage.setResizable(true);
        dialogStage.initStyle(StageStyle.DECORATED);
//        dialogStage.initModality(Modality.APPLICATION_MODAL);

        attachEvents();
        attachBinds();

        dialogStage.show();
    }

    public void attachEvents() {
        startBtn.setOnAction(e -> {
            state.clear();
            state.setProgress(true);
            EmbodyProcessAsyncTask emb = new EmbodyProcessAsyncTask(state, dstDirectory, paths, historicaIds, type, pathIncluding, fileSeparator);
            ThreadCreator t = new ThreadCreator();
            t.startAsyncTask(emb);
            if (onceStarted) {
                service.restart();
            } else {
                service.start();
                onceStarted = true;
                startBtn.setText(Rsc.get("common_menu_EH_restart"));
            }
        });

        resumeBtn.setOnAction(e -> {
            if (state.isInterrupted()) {
                logger.debug("resumeBtn...to false...");
                state.setInterrupted(false);
            } else {
                logger.debug("resumeBtn...to true...");
                state.setInterrupted(true);
            }
        });

        resetBtn.setOnAction(e -> {
            process.getException().clear();
            state.setCanceled(true);
            service.cancel();
            service.reset();
        });

        exitBtn.setOnAction(e -> {
            dialogStage.close();
        });
    }

    public void attachBinds() {
        startBtn.disableProperty().bind(service.stateProperty().isEqualTo(SUCCEEDED));
        resumeBtn.disableProperty().bind(service.stateProperty().isNotEqualTo(RUNNING));
        resetBtn.disableProperty().bind(
                Bindings.or(service.stateProperty().isEqualTo(RUNNING),
                        service.stateProperty().isEqualTo(SCHEDULED)));
    }
}
