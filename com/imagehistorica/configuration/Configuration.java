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

import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.controllers.AppreciateImage;
import com.imagehistorica.Config;
import com.imagehistorica.util.view.CommonAlert;
import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class Configuration {

    private final Image img16 = new Image(getClass().getResourceAsStream("/resources/images/ImageHistorica_16.png"));
    private final Image img32 = new Image(getClass().getResourceAsStream("/resources/images/ImageHistorica_32.png"));

    private final Map<String, String> curConf = new HashMap<>();

    private final ImageHistoricaController parentController;
    private final Stage configStage = new Stage();
    private final StackPane s = new StackPane();
    private final ScrollPane sp = new ScrollPane();
    private final VBox vbox = new VBox();
    private final HBox hbox = new HBox();

    private final CheckBox isHQ = new CheckBox(Rsc.get("conf_CON_quality"));
//    private final CheckBox isSkipDocValuesImpl = new CheckBox(Rsc.get("conf_CON_docValues"));
//    private final CheckBox isSaveImage = new CheckBox(Rsc.get("conf_CON_saveimage"));
    private final CheckBox isFetchImage = new CheckBox(Rsc.get("conf_CON_fetch"));
//    private final CheckBox isReconstruct = new CheckBox(Rsc.get("conf_CON_reconstruct"));
    private final CheckBox isInitializing = new CheckBox(Rsc.get("conf_CON_initialize"));
    private final CheckBox isDelToken = new CheckBox(Rsc.get("conf_CON_delToken"));

    private final String title = Rsc.get("conf_CON_title");
    private final Button refreshBtn = new Button(Rsc.get("conf_CON_run"));
    private final Button saveBtn = new Button(Rsc.get("conf_CON_save"));
    private final Button cancelBtn = new Button(Rsc.get("cancelBtn"));
    private final Button defaultBtn = new Button(Rsc.get("conf_CON_default"));
//    private final Button closeBtn = new Button(Rsc.get("closeBtn"));
    private final HBox buttonBox = new HBox(saveBtn, cancelBtn, defaultBtn);

    private final Label l_general = new Label(Rsc.get("conf_CON_general"));
    private final Label l_cs = new Label(Rsc.get("conf_CON_cs"));
    private final Label l_cds = new Label(Rsc.get("conf_CON_cds"));
    private final Label l_chr1 = new Label(Rsc.get("conf_CON_chr1"));
    private final Label l_chr2 = new Label(Rsc.get("conf_CON_chr2"));
    private final Label l_chr3 = new Label(Rsc.get("conf_CON_chr3"));
    private final Label l_ctd = new Label(Rsc.get("conf_CON_ctd"));
//    private final Label l_cc = new Label(Rsc.get("conf_CON_cc"));
    private final Label l_refresh1 = new Label(Rsc.get("conf_CON_ref"));
    private final HBox l_refresh2 = new HBox();
    private final Label l_refresh3 = new Label(Rsc.get("conf_CON_refresh"));
//    private final Label l_recon = new Label(Rsc.get("conf_CON_recon"));
    private final Label l_init = new Label(Rsc.get("conf_CON_init"));

    private final String r_a_title_hq = Rsc.get("conf_CON_a_title_hq");
    private final String r_a_header_hq = Rsc.get("conf_CON_a_header_hq");
    private final String r_a_content_hq = Rsc.get("conf_CON_a_content_hq");

    /*
     private final String r_a_title_saveimage = Rsc.get("conf_CON_a_title_saveimage");
     private final String r_a_header_saveimage = Rsc.get("conf_CON_a_header_saveimage");
     private final String r_a_content_saveimage = Rsc.get("conf_CON_a_content_saveimage");
     */
    private final String r_a_title_refresh = Rsc.get("conf_CON_a_title_refresh");
    private final String r_a_header_refresh = Rsc.get("conf_CON_a_header_refresh");
    private final String r_a_content_refresh = Rsc.get("conf_CON_a_content_refresh");

    /*
     private final String r_a_title_recon = Rsc.get("conf_CON_a_title_recon");
     private final String r_a_header_recon = Rsc.get("conf_CON_a_header_recon");
     private final String r_a_content_recon = Rsc.get("conf_CON_a_content_recon");
     */
    private final String r_a_title_init = Rsc.get("conf_CON_a_title_init");
    private final String r_a_header_init = Rsc.get("conf_CON_a_header_init");
    private final String r_a_content_init = Rsc.get("conf_CON_a_content_init");

    private final String r_a_title_delToken = Rsc.get("conf_CON_a_title_delToken");
    private final String r_a_header_delToken = Rsc.get("conf_CON_a_header_delToken");
    private final String r_a_content_delToken = Rsc.get("conf_CON_a_content_delToken");

    private Scene scene;
    private ConfStage cs;
    private ConfDS cds;
    private ConfHistoricaRoot chr;
    private ConfTmpDir ctd;
//    private ConfConcurrent cc;

    public Configuration(ImageHistoricaController parentController) {
        this.parentController = parentController;

        curConf.put("isHQ", String.valueOf(Config.isHighQualityImage()));
        curConf.put("fetchImage", String.valueOf(Config.isFetchImage()));
        curConf.put("defaultWidth", String.valueOf(Config.getDefaultWidth()));
        curConf.put("defaultHeight", String.valueOf(Config.getDefaultHeight()));
        curConf.put("imageHistoricaDb", Config.getImageHistoricaDb());
        curConf.put("rootDir", Config.getRootDir());
        curConf.put("tmpDir", Config.getTmpDir());
//        curConf.put("numOfThreads", String.valueOf(Config.getNumOfThreads()));
//        curConf.put("procUnitInMaking", String.valueOf(Config.getProcUnitInMaking()));
//        curConf.put("procUnitInGetting", String.valueOf(Config.getProcUnitInGetting()));
//        curConf.put("reConstruct", String.valueOf(Config.isReconstruct()));
        curConf.put("initializing", String.valueOf(Config.isInitializing()));
        curConf.put("delToken", String.valueOf(Config.isDelToken()));
    }

    public void createLayout() {
        Separator s1 = new Separator();
        Separator s2 = new Separator();
        Separator s3 = new Separator();
        Separator s4 = new Separator();
        Separator s5 = new Separator();
        Separator s6 = new Separator();
        Separator s7 = new Separator();
        Separator s8 = new Separator();
//        Separator s9 = new Separator();

        isHQ.setSelected(Config.isHighQualityImage());
//        isSaveImage.setSelected(Config.isSaveImage());
        isFetchImage.setSelected(Config.isFetchImage());
//        isReconstruct.setSelected(Config.isReconstruct());
        isInitializing.setSelected(Config.isInitializing());
        isDelToken.setSelected(Config.isDelToken());

        cs = new ConfStage(parentController);
        cds = new ConfDS(parentController);
        chr = new ConfHistoricaRoot(parentController);
        ctd = new ConfTmpDir(parentController);
//        cc = new ConfConcurrent();

        cs.setAlignment(Pos.TOP_LEFT);
        cds.setAlignment(Pos.TOP_LEFT);
        chr.setAlignment(Pos.TOP_LEFT);
        ctd.setAlignment(Pos.TOP_LEFT);
//        cc.setAlignment(Pos.TOP_LEFT);

        l_general.setUnderline(true);
        l_cs.setUnderline(true);
        l_cds.setUnderline(true);
        l_chr1.setUnderline(true);
        l_ctd.setUnderline(true);
//        l_cc.setUnderline(true);
        l_refresh1.setUnderline(true);
//        l_recon.setUnderline(true);
        l_init.setUnderline(true);
        l_general.setStyle("-fx-font-size: 16pt; -fx-text-fill: #96b946;");
        l_cs.setStyle("-fx-font-size: 16pt; -fx-text-fill: #96b946;");
        l_cds.setStyle("-fx-font-size: 16pt; -fx-text-fill: #96b946;");
        l_chr1.setStyle("-fx-font-size: 16pt; -fx-text-fill: #96b946;");
        l_ctd.setStyle("-fx-font-size: 16pt; -fx-text-fill: #96b946;");
//        l_cc.setStyle("-fx-font-size: 16pt; -fx-text-fill: #96b946;");
        l_refresh1.setStyle("-fx-font-size: 16pt; -fx-text-fill: #96b946;");
//        l_recon.setStyle("-fx-font-size: 16pt; -fx-text-fill: #96b946;");
        l_init.setStyle("-fx-font-size: 16pt; -fx-text-fill: #96b946;");

        HBox.setHgrow(l_refresh2, Priority.ALWAYS);
        hbox.getChildren().addAll(l_refresh3, l_refresh2, refreshBtn);

        buttonBox.setSpacing(20);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);

        vbox.setSpacing(20);
        vbox.getChildren().addAll(l_general, isHQ, isFetchImage, s1, l_cs, cs, s2, l_cds, cds, s3, l_chr1, l_chr2, l_chr3,
                //                chr, s4, l_ctd, ctd, s5, l_cc, cc, s6, l_refresh1, hbox, s7, l_recon, isReconstruct, s8, l_init, isInitializing, s9, buttonBox);
                chr, s4, l_ctd, ctd, s5, l_refresh1, hbox, s6, l_init, isInitializing, isDelToken, s8, buttonBox);
        sp.setContent(vbox);
        sp.setPadding(new Insets(10));
        s.getChildren().add(sp);

        String style = Configuration.class.
                getResource("/resources/css/stylesheet.css").toExternalForm();
        scene = new Scene(s);
        scene.getStylesheets().add(style);
        configStage.setScene(scene);
        configStage.getIcons().addAll(img16, img32);
        configStage.setTitle(title);
        configStage.setResizable(true);
        configStage.initStyle(StageStyle.DECORATED);
        configStage.initModality(Modality.APPLICATION_MODAL);

        attachEvents();

        configStage.setScene(scene);
        configStage.show();
    }

    public void attachEvents() {
        saveBtn.setOnAction(e -> {
            if (isHQ.isSelected()) {
                Config.setHighQualityImage(true);
            } else {
                Config.setHighQualityImage(false);
            }
            if (isFetchImage.isSelected()) {
                Config.setFetchImage(true);
            } else {
                Config.setFetchImage(false);
            }
            /*
             if (isReconstruct.isSelected()) {
             Config.setReconstruct(true);
             } else {
             Config.setReconstruct(false);
             }
             */
            if (isInitializing.isSelected()) {
                Config.setInitializing(true);
            } else {
                Config.setInitializing(false);
            }
            if (isDelToken.isSelected()) {
                Config.setDelToken(true);
            } else {
                Config.setDelToken(false);
            }
            Config.setDefaultWidth(cs.getWidthCurrent());
            Config.setDefaultHeight(cs.getHeightCurrent());
            Config.setImageHistoricaDb(cds.getDataStore());
            Config.setRootDir(chr.getRootDir());
            Config.setTmpDir(ctd.getTmpDir());
            /*
             Config.setNumOfThreads(cc.getNumOfThreads());
             Config.setProcUnitInMaking(cc.getNumOfUnitMaking());
             Config.setProcUnitInGetting(cc.getNumOfUnitGetting());
             */

            Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("conf_CON_save"), Rsc.get("conf_CON_a_header_save"), null);
            alert.showAndWait();
            e.consume();
        });

        cancelBtn.setOnAction(e -> {
            isHQ.setSelected(Boolean.valueOf(curConf.get("isHQ")));
            isFetchImage.setSelected(Boolean.valueOf(curConf.get("fetchImage")));
            cs.adjustWidth(Double.parseDouble(curConf.get("defaultWidth")));
            cs.adjustHeight(Double.parseDouble(curConf.get("defaultHeight")));
            cds.setDataStore(curConf.get("imageHistoricaDb"));
            chr.setRootDir(curConf.get("rootDir"));
            ctd.setTmpDir(curConf.get("tmpDir"));
            /*
             cc.setNumOfThreads(Integer.parseInt(curConf.get("numOfThreads")));
             cc.setNumOfUnitMaking(Integer.parseInt(curConf.get("procUnitInMaking")));
             cc.setNumOfUnitGetting(Integer.parseInt(curConf.get("procUnitInGetting")));
             isReconstruct.setSelected(Boolean.valueOf(curConf.get("reConstruct")));
             */
            isInitializing.setSelected(Boolean.valueOf(curConf.get("initializing")));
            isDelToken.setSelected(Boolean.valueOf(curConf.get("delToken")));
            e.consume();
        });

        defaultBtn.setOnAction(e -> {
            Config.removeHighQualityImage();
            isHQ.setSelected(Config.isHighQualityImage());

            Config.removeFetchImage();
            isFetchImage.setSelected(Config.isFetchImage());

            Config.removeDefaultWidth();
            cs.adjustWidth(Config.getDefaultWidth());

            Config.removeDefaultHeight();
            cs.adjustHeight(Config.getDefaultHeight());

            Config.removeImageHistoricaDb();
            cds.setDataStore(Config.getImageHistoricaDb());

            Config.removeRootDir();
            chr.setRootDir(Config.getRootDir());

            Config.removeTmpDir();
            ctd.setTmpDir(Config.getTmpDir());

            /*
             Config.removeNumOfThreads();
             cc.setNumOfThreads(Config.getNumOfThreads());

             Config.removeProcUnitInMaking();
             cc.setNumOfUnitMaking(Config.getProcUnitInMaking());

             Config.removeProcUnitInGetting();
             cc.setNumOfUnitGetting(Config.getProcUnitInGetting());

             Config.removeReconstruct();
             isReconstruct.setSelected(Config.isReconstruct());
             */
            Config.removeInitializing();
            isInitializing.setSelected(Config.isInitializing());

            Config.removeDelToken();
            isDelToken.setSelected(Config.isDelToken());

            e.consume();
        });

        refreshBtn.setOnAction(e -> {
            ButtonType b1 = new ButtonType(Rsc.get("okBtn"));
            ButtonType b2 = new ButtonType(Rsc.get("cancelBtn"));
            Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, r_a_title_refresh, r_a_header_refresh, r_a_content_refresh, b1, b2);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == b1) {
                parentController.removeHistoricaDirCache();
                TreeCache treeCache = TreeCache.getInstance();
                treeCache.clearHistoricaDirsMap();
                treeCache.clearHistoricaNumsMap();
                treeCache.clearHistoricaMeaningsMap();
                treeCache.clearHistoricaNumsMapNew();
                treeCache.clearBranchNode();
                treeCache.clearBranchNodesNew();
                treeCache.clearLeafNode();
                treeCache.clearLeafNodesNew();
                treeCache.createRootNode();
                AppreciateImage appreciateImage = parentController.getAppreciateImage();
                if (appreciateImage != null) {
                    appreciateImage.getTree().setRoot(treeCache.getRootNode());
                    appreciateImage.getTreeNoListener().setRoot(treeCache.getRootNode());
                }
            }
            e.consume();
        });

        isHQ.setOnMouseClicked(e -> {
            Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, r_a_title_hq, r_a_header_hq, r_a_content_hq);
            alert.showAndWait();
            e.consume();
        });

        /*
         isSaveImage.setOnMouseClicked(e -> {
         if (!isSaveImage.isSelected()) {
         ctd.setDisable(true);
         Alert alert = CommonAlert.makeAlert(AlertType.WARNING, r_a_title_saveimage, r_a_header_saveimage, r_a_content_saveimage);
         alert.showAndWait();
         }
         });
         isReconstruct.setOnAction(e -> {
         if (isReconstruct.isSelected()) {
         Alert alert = CommonAlert.makeAlert(AlertType.WARNING, r_a_title_recon, r_a_header_recon, r_a_content_recon);
         alert.showAndWait();
         }
         });
         */
        isInitializing.setOnAction(e -> {
            if (isInitializing.isSelected()) {
                Alert alert = CommonAlert.makeAlert(AlertType.WARNING, r_a_title_init, r_a_header_init, r_a_content_init + "\n\n" + Config.getImageHistoricaDb() + File.separator + "ImageHistoricaDB");
                alert.showAndWait();
            }
            e.consume();
        });

        isDelToken.setOnAction(e -> {
            if (isDelToken.isSelected()) {
                Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, r_a_title_delToken, r_a_header_delToken, r_a_content_delToken);
                alert.showAndWait();
            }
            e.consume();
        });

    }
}
