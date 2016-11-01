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
package com.imagehistorica.controllers;

import static com.imagehistorica.util.model.SchemeType.*;
import static com.imagehistorica.util.Constants.ANALYZE_STATUS_CONTROLLER;
import static com.imagehistorica.util.ImageType.ImageFormat.UNKNOWN;

import com.imagehistorica.common.state.AnalyzeState;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.ControllerType;
import com.imagehistorica.controller.type.Green;
import com.imagehistorica.util.view.ResizeStage;
import com.imagehistorica.databases.model.Historica;
import com.imagehistorica.common.toolbar.GreenToolBar;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.util.model.RealImageProperty;
import com.imagehistorica.util.model.SchemeType;
import com.imagehistorica.httpclient.GeneralHttpClient;
import com.imagehistorica.common.view.GridPanePanel;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.util.ImageType;
import com.imagehistorica.util.ImageType.ImageFormat;
import com.imagehistorica.databases.model.Request;
import com.imagehistorica.Config;
import com.imagehistorica.util.LmodMaker;
import com.imagehistorica.util.view.CommonAlert;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javafx.util.Pair;

import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class AnalyzeImage extends Factory implements Green {

    private ImageHistoricaController parentController;
    private AnalyzeState state = AnalyzeState.getInstance();

    private GridPanePanel grid;
    private int gridColumn = 1;
    private int gridRow = 6;
    private BorderPane bp;

    private GreenToolBar toolMenuBar;

    private VBox vbox;
    private HBox buttonBox;
    private Button runBtn = new Button(Rsc.get("con_AI_run"));
    private Button cancelBtn = new Button(Rsc.get("con_AI_cancel"));
    private Button dirBtn = new Button(Rsc.get("con_AI_dir"));
    private Button fileBtn = new Button(Rsc.get("con_AI_file"));
    private Button simpleBtn = new Button(Rsc.get("con_AI_file"));
    private Button existingBtn = new Button(Rsc.get("con_AI_existing"));
    private Label desc = new Label(Rsc.get("con_AI_desc_1"));
    private TextArea successTxt = new TextArea();
    private TextArea failureTxt = new TextArea();
    private TextArea duplicateTxt;

    private String dir_desc = Rsc.get("con_AI_dir_desc");
    private String file_desc = Rsc.get("con_AI_file_desc");
    private String succ_result1 = Rsc.get("con_AI_succ_result_1");
    private String succ_result2 = Rsc.get("con_AI_succ_result_2");
    private String succ_result3 = Rsc.get("con_AI_succ_result_3");
    private String succ_result4 = Rsc.get("con_AI_succ_result_4");
    private String fail_result1 = Rsc.get("con_AI_fail_result_1");
    private String ex1 = Rsc.get("con_AI_ex_1");
    private String ex2 = Rsc.get("con_AI_ex_2");

    private SplitPane splitPane = new SplitPane();

    private LmodMaker lmodMaker = new LmodMaker();
    private Set<RealImageProperty> successProp = new HashSet<>();
    private Set<RealImageProperty> failureProp = new HashSet<>();

    private boolean isReplaced = false;
    private Set<Path> dirlist = new TreeSet<>();

    private final Logger logger = LoggerFactory.getLogger(AnalyzeImage.class);

    @Override
    public Scene createLayout(ImageHistoricaController parentController) {
        this.parentController = parentController;
        toolMenuBar = new GreenToolBar(this.parentController, this);

        successTxt.setEditable(false);
        successTxt.setWrapText(true);
        successTxt.getStyleClass().add("extract-image-box-success");

        failureTxt.setEditable(false);
        failureTxt.setWrapText(true);
        failureTxt.getStyleClass().add("extract-image-box-failure");

        splitPane = new SplitPane();
        splitPane.setDividerPositions(0.75f);
        splitPane.getItems().addAll(successTxt, failureTxt);

        desc.setWrapText(true);

        runBtn.setDisable(true);
        if (Backend.getQueueLength() > 0) {
            runBtn.setDisable(true);
            cancelBtn.setDisable(false);
            dirBtn.setDisable(true);
            fileBtn.setDisable(true);
            existingBtn.setVisible(true);
            buttonBox = new HBox(runBtn, cancelBtn, dirBtn, fileBtn, existingBtn);
            displayRequested();
        } else {
            runBtn.setDisable(true);
            cancelBtn.setDisable(false);
            dirBtn.setDisable(false);
            fileBtn.setDisable(false);
            existingBtn.setVisible(false);
            buttonBox = new HBox(runBtn, cancelBtn, dirBtn, fileBtn);
        }

        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(20));

        vbox = new VBox(buttonBox, desc);
        bp = new BorderPane(splitPane, toolMenuBar, null, vbox, null);

        scene = new Scene(bp);
        setScene(scene);

        resizeStage();
        attachEvents();

        return scene;
    }

    public void checkTask() {
        logger.debug("Start checkTask()...");
        state.createLeaves();
        state.requestNewTreeItem();
        state.mergeTreeItem();

        if (state.isCreated()) {
            logger.debug("Newly created historicas...");
            successTxt.clear();
            failureTxt.clear();
            successProp.clear();
            failureProp.clear();
            dirlist.clear();
        }

        if (Backend.getQueueLength() > 0) {
            logger.debug("Still remaining requests in the waiting db...");
            runBtn.setDisable(true);
            cancelBtn.setDisable(false);
            dirBtn.setDisable(true);
            fileBtn.setDisable(true);
            existingBtn.setVisible(true);
            if (!buttonBox.getChildren().contains(existingBtn)) {
                buttonBox.getChildren().add(existingBtn);
            }
            displayRequested();
        } else {
            runBtn.setDisable(true);
            cancelBtn.setDisable(false);
            dirBtn.setDisable(false);
            fileBtn.setDisable(false);
            existingBtn.setVisible(false);
            if (buttonBox.getChildren().contains(existingBtn)) {
                buttonBox.getChildren().remove(existingBtn);
            }
        }
    }

    @Override
    public Scene restoreScene() {
        checkTask();
        return this.scene;
    }

    @Override
    public ControllerType getControllerType() {
        return controllerType;
    }

    @Override
    public void resizeStage() {
        ResizeStage.setResizeStage(this.parentController.getStage(), scene);
    }

    public void resizeStageGP() {
        ResizeStage.setResizeStage(this.parentController.getStage(), this, grid);
    }

    @Override
    public void adjustLayout() {
    }

    @Override
    protected void attachEvents() {
        runBtn.setOnAction(e -> {
            if (Config.isFree()) {
                if (parentController.showFreeAlert(this)) {
                    return;
                }
            }

            Pair<Map<RealImageProperty, Long>, Map<RealImageProperty, Long>> checkExistings = Backend.checkExisting(successProp);
            Map<RealImageProperty, Long> existingRealImage = checkExistings.getKey();
            Map<RealImageProperty, Long> newRealImage = checkExistings.getValue();
            if (logger.isDebugEnabled()) {
                existingRealImage.entrySet().stream().forEach((en) -> {
                    logger.debug("Existing... RealImageProperty: {}, CRC: {}", en.getKey(), en.getValue());
                });
                newRealImage.entrySet().stream().forEach((en) -> {
                    logger.debug("New... RealImageProperty: {}, CRC: {}", en.getKey(), en.getValue());
                });
            }

            if (!existingRealImage.isEmpty()) {
                ButtonType b1 = new ButtonType(Rsc.get("con_AI_btn_1"));
                ButtonType b2 = new ButtonType(Rsc.get("con_AI_btn_2"));
                ButtonType b3 = new ButtonType(Rsc.get("cancelBtn"));
                Alert checkExisting = CommonAlert.makeAlert(AlertType.CONFIRMATION, Rsc.get("con_AI_title"), Rsc.get("con_AI_header_1"), Rsc.get("con_AI_content_1"), b1, b2, b3);
                Optional<ButtonType> result = checkExisting.showAndWait();
                if (result.get() == b1) {
                    TreeCache treeCache = TreeCache.getInstance();
                    existingRealImage.entrySet().stream().map((entry) -> Backend.getRealImage(entry.getKey().getId())).forEach((realImage) -> {
                        Historica historica = Backend.getHistoricaByRealImageId(realImage.getRealImageId());
                        if (historica != null) {
                            Backend.deleteHistoricaByRealImage(realImage);

                            TreeItem<HistoricaProperty> treeItem = treeCache.getBranchNode(historica.getHistoricaDirId());
                            if (treeItem != null) {
                                TreeItem<HistoricaProperty> delItem = null;
                                for (TreeItem<HistoricaProperty> prop : treeItem.getChildren()) {
                                    if (prop.getValue().getHistoricaId() == historica.getHistoricaId()) {
                                        delItem = prop;
                                    }
                                }
                                if (delItem != null) {
                                    logger.debug("delItem: {}", delItem.getValue().getRealImagePath());
                                    if (treeItem.getChildren().remove(delItem)) {
                                        logger.debug("deleted leaf node...");
                                    }
                                }
                                treeItem.getValue().setNumOfLeaves(treeItem.getValue().getNumOfLeaves().get() - 1);
                            }
                            treeCache.decreHistoricaNumsMap(historica.getHistoricaDirId(), 1);
                            treeCache.removeLeafNode(historica.getHistoricaId());
                            treeCache.removeLeafNodeNew(historica.getHistoricaId());
                        }
                    });
                    Backend.schedule(newRealImage, existingRealImage);
                    parentController.setAnalyzeStatus((AnalyzeStatus) parentController.createScene(this, ANALYZE_STATUS_CONTROLLER));
                } else if (result.get() == b2) {
                    if (!newRealImage.isEmpty()) {
                        Backend.schedule(newRealImage, null);
                        parentController.setAnalyzeStatus((AnalyzeStatus) parentController.createScene(this, ANALYZE_STATUS_CONTROLLER));
                    } else {
                        Alert noNewImage = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("con_AI_title"), Rsc.get("con_AI_header_2"), null);
                        noNewImage.showAndWait();
                    }
                } else if (result.get() == b3) {
                    replaceLayout(existingRealImage);
                }
            } else {
                if (!newRealImage.isEmpty()) {
                    Backend.schedule(newRealImage, null);
                    parentController.setAnalyzeStatus((AnalyzeStatus) parentController.createScene(this, ANALYZE_STATUS_CONTROLLER));
                }
            }
            e.consume();
        });

        cancelBtn.setOnAction(e -> {
            if (!existingBtn.isVisible()) {
                successTxt.clear();
                failureTxt.clear();
                successProp.clear();
                failureProp.clear();
                dirlist.clear();
                runBtn.setDisable(true);
                if (isReplaced) {
                    replaceLayout();
                }
            } else {
                List<Request> requests = Backend.getQueue();
                for (Request req : requests) {
                    if (Backend.deleteRealImage(req.getRealImageId())) {
                        logger.info("Deleted real image... ID: {}, Path: {}", req.getRealImageId(), req.getRealImagePath0());
                    } else {
                        logger.info("Could not delete real image... ID: {}, Path: {}", req.getRealImageId(), req.getRealImagePath0());
                    }
                }
                Backend.deleteRequests(requests);
                successTxt.clear();
                failureTxt.clear();
                successProp.clear();
                failureProp.clear();
                dirlist.clear();
                runBtn.setDisable(true);
                dirBtn.setDisable(false);
                fileBtn.setDisable(false);
                existingBtn.setVisible(false);
                replaceLayout();
            }
            e.consume();
        });

        dirBtn.setOnAction(e -> {
            final DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle(dir_desc);
            File imageDir = new File(Config.getImageDir());
            if (imageDir.exists()) {
                dc.setInitialDirectory(imageDir);
            } else {
                Config.removeImageDir();
                imageDir = new File(Config.getTmpDir());
                if (imageDir.exists()) {
                    dc.setInitialDirectory(imageDir);
                } else {
                    if (imageDir.mkdir()) {
                        dc.setInitialDirectory(imageDir);
                    } else {
                        dc.setInitialDirectory(new File(Config.getInitialDirectory()));
                    }
                }
            }

            final File selectedDir = dc.showDialog(parentController.getStage());
            if (selectedDir != null) {
                Config.setImageDir(selectedDir.getAbsolutePath());
                searchDirs(selectedDir);
                displayResults();
                if (!successProp.isEmpty()) {
                    runBtn.setDisable(false);
                }
            }
            e.consume();
        });

        fileBtn.setOnAction(e -> {
            final FileChooser fc = new FileChooser();
            fc.setTitle(file_desc);
            fc.setInitialDirectory(new File(Config.getInitialDirectory()));
            List<File> files = fc.showOpenMultipleDialog(parentController.getStage());
            if (files != null) {
                files.stream().forEach((file) -> {
                    RealImageProperty prop = makeRealImageProp(file.toPath());
                    if (ImageType.isImage(file.toPath())) {
                        successProp.add(prop);
                    } else {
                        prop.setException(ex1);
                        failureProp.add(prop);
                    }
                });
                displayResults();
                if (!successProp.isEmpty()) {
                    runBtn.setDisable(false);
                }
            }
            e.consume();
        });

        existingBtn.setOnAction(e -> {
            parentController.setAnalyzeStatus((AnalyzeStatus) this.parentController.createScene(this, ANALYZE_STATUS_CONTROLLER));
            e.consume();
        });

        successTxt.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasHtml() || db.hasImage() || db.hasFiles() || db.hasUrl()) {
                e.acceptTransferModes(TransferMode.ANY);
            }
            e.consume();
        });

        successTxt.setOnDragDropped(e -> {
            logger.debug("Start setOnDragDropped...");
            if (existingBtn.isVisible()) {
                logger.debug("Forbidden due to existingBtn appeared...");
                e.setDropCompleted(false);
                e.consume();
                return;
            }

            try {
                boolean isCompleted = false;
                Dragboard db = e.getDragboard();
                if (db.hasUrl() || db.hasImage() || db.hasHtml() || db.hasFiles()) {
                    logger.debug("Start Type 1...");
                    logger.debug("getHtml()...{}", db.getHtml());
                    logger.debug("getUrl()...{}", db.getUrl());

                    // Type-1
                    if (!db.hasHtml() && db.hasUrl() && db.hasFiles()) {
                        logger.debug("Start Type-1...");
                        String url = db.getUrl();
                        if (url.startsWith("file")) {
                            logger.debug("Scheme is 'file'...");
                            int numSuccessProp = successProp.size();
                            int numDir = dirlist.size();
                            for (File file : db.getFiles()) {
                                if (file.isDirectory()) {
                                    searchDirs(file);
                                } else {
                                    Path path = file.toPath();
                                    logger.debug("Path(toString()): " + path.toString());
                                    logger.debug("Path(toUri()): " + path.toUri());
                                    RealImageProperty prop = makeRealImageProp(path);
                                    if (ImageType.isImage(file.toPath())) {
                                        successProp.add(prop);
                                    } else {
                                        prop.setException(ex1);
                                        failureProp.add(prop);
                                    }
                                }
                            }
                            if (successProp.size() > numSuccessProp || dirlist.size() > numDir) {
                                isCompleted = true;
                            }
                        }
                    }

                    // Type-2
                    if (!isCompleted && db.hasHtml() && db.hasImage()) {
                        logger.debug("Start Type-2...");
                        Document doc = Jsoup.parse(db.getHtml());
                        Elements atags = doc.getElementsByTag("a");
                        List<RealImageProperty> props = new ArrayList<>();
                        boolean isGoogle = false;
                        SchemeType scheme = null;
                        String href = null;
                        String src = null;
                        String alt = null;
                        String title = null;
                        String snippet = null;

                        // Type-2-1
                        if (!atags.isEmpty()) {
                            logger.debug("Start Type-2-1...");
                            for (Element atag : atags) {
//                                href = atag.absUrl("href");
                                href = atag.attr("href");
                                if (href.startsWith("http") && href.contains("google") && href.contains("imgurl") && href.contains("imgrefurl")) {
                                    logger.debug("Scheme is 'http' for google...");
                                    String[] urls = parseGoogle(href);
                                    src = urls[0];
                                    href = urls[1];

                                    if (src.startsWith("https")) {
                                        scheme = HTTPS;
                                    } else if (src.startsWith("http")) {
                                        scheme = HTTP;
                                    }

                                    Element img = atag.select("img").first();
                                    alt = img.attr("alt");
                                    title = img.attr("title");

                                    isGoogle = true;
                                }

                                if (!isGoogle) {
                                    logger.debug("Scheme is 'http' for others ...");
                                    Element img = atag.select("img").first();
                                    src = img.attr("src");
                                    if (src.startsWith("https")) {
                                        scheme = HTTPS;
                                    } else if (src.startsWith("http")) {
                                        scheme = HTTP;
                                    }
                                    alt = img.attr("alt");
                                    title = img.attr("title");
                                }

                                snippet = atag.text();

                                URI srcUri = null;
                                URI hrefUri = null;
                                try {
                                    if (src != null) {
                                        srcUri = new URI(src);
                                    } else {
                                        continue;
                                    }

                                    try {
                                        if (href != null && href.startsWith("http")) {
                                            hrefUri = new URI(href);
                                        }
                                    } catch (URISyntaxException ex) {
                                        logger.debug("No href of src...{}", src);
                                    }
                                } catch (URISyntaxException ex) {
                                    logger.debug("No img src...{}", src);
                                    continue;
                                }

                                logger.info("srcUri: {}", srcUri);
                                logger.info("hrefUri: {}", hrefUri);
                                RealImageProperty prop = new RealImageProperty(scheme, srcUri, hrefUri, alt, title, snippet);
                                props.add(prop);

                                isGoogle = false;
                                scheme = null;
                                href = null;
                                src = null;
                                alt = null;
                                title = null;
                                snippet = null;
                            }

                            // Type-2-2
                        } else {
                            logger.debug("Start Type-2-2...");
                            Elements img = doc.getElementsByTag("img");
                            src = img.attr("src");
                            if (src.startsWith("https")) {
                                scheme = HTTPS;
                            } else if (src.startsWith("http")) {
                                scheme = HTTP;
                            } else {
                                return;
                            }
                            alt = img.attr("alt");
                            title = img.attr("title");

                            snippet = img.text();

                            URI srcUri = null;
                            URI hrefUri = null;
                            try {
                                srcUri = new URI(src);
                                try {
                                    if (href != null) {
                                        hrefUri = new URI(href);
                                    }
                                } catch (URISyntaxException ex) {
                                    logger.debug("No href of src...{}", src);
                                }
                            } catch (URISyntaxException ex) {
                                logger.debug("No img src...{}", src);
                            }

                            logger.info("srcUri: {}", srcUri);
                            logger.info("hrefUri: {}", hrefUri);
                            RealImageProperty prop = new RealImageProperty(scheme, srcUri, hrefUri, alt, title, snippet);
                            props.add(prop);

                            isGoogle = false;
                            scheme = null;
                            href = null;
                            src = null;
                            alt = null;
                            title = null;
                            snippet = null;
                        }

                        if (!props.isEmpty()) {
                            logger.debug("RealImageProperty is not empty...");
                            if (fetchImages(props)) {
                                logger.debug("fetchImages() succeeded...");
                                isCompleted = true;
                            }
                        }
                    }

                    // Type-3
                    if (!isCompleted && db.hasUrl()) {
                        logger.debug("Start Type-3...");
                        String url = db.getUrl();
                        SchemeType scheme = null;
                        String href = null;
                        String src = null;

                        // Type-3-1
                        if (url.startsWith("http") && url.contains("google") && url.contains("imgurl") && url.contains("imgrefurl")) {
                            logger.debug("Start Type-3-1...");
                            String[] urls = parseGoogle(url);
                            src = urls[0];
                            href = urls[1];

                            if (src.startsWith("https")) {
                                scheme = HTTPS;
                            } else {
                                scheme = HTTP;
                            }

                            URI srcUri = null;
                            URI hrefUri = null;
                            try {
                                srcUri = new URI(src);
                                try {
                                    if (href != null && href.startsWith("http")) {
                                        hrefUri = new URI(href);
                                    }
                                } catch (URISyntaxException ex) {
                                    logger.debug("No href of src...{}", src);
                                }
                            } catch (URISyntaxException ex) {
                                logger.debug("No img src...{}", src);
                            }

                            if (fetchImages(new RealImageProperty(scheme, srcUri, hrefUri))) {
                                isCompleted = true;
                            }

                            // Type-3-2
                        } else if (url.startsWith("http")) {
                            logger.debug("Start Type-3-2...");
                            if (url.startsWith("https")) {
                                scheme = HTTPS;
                            } else {
                                scheme = HTTP;
                            }

                            URI srcUri = new URI(url);
                            if (fetchImages(new RealImageProperty(scheme, srcUri, null))) {
                                isCompleted = true;
                            }
                        }
                    }
                } else {
                    logger.info("No dragboard...");
                }

                displayResults();
                if (!successProp.isEmpty()) {
                    runBtn.setDisable(false);
                }
                e.setDropCompleted(isCompleted);
            } catch (URISyntaxException ex) {
                logger.debug("", ex);
                ex.printStackTrace();
            } catch (Exception ex) {
                logger.debug("", ex);
            }

            e.consume();
        });
    }

    private void searchDirs(File selectedDirectory) {
        dirlist.add(selectedDirectory.toPath());
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(selectedDirectory.toPath(), new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                File file = entry.toFile();
                if (file.isDirectory()) {
                    searchDirs(file);
                } else if (file.isFile()) {
                    RealImageProperty prop = makeRealImageProp(entry);
                    if (ImageType.isImage(entry)) {
                        successProp.add(prop);
                    } else {
                        prop.setException(ex1);
                        failureProp.add(prop);
                    }
                }
                return false;
            }
        })) {
            for (Path p : ds) {
            }
        } catch (IOException ex) {
            logger.error("", ex);
        }
    }

    /*
     private boolean verifyImage(Path path) {
     try {
            
     String mimeType = Files.probeContentType(path);
     if (mimeType != null && mimeType.startsWith("image/")) {
     return true;
     } else {
     logger.debug("mimeType: {}, path: {}", mimeType, path);
     }
     } catch (IOException ex) {
     logger.error("", ex);
     }

     return false;
     }
     */
    private String[] parseGoogle(String href) throws URISyntaxException, UnsupportedEncodingException {
        String[] urls = new String[2];
        URI uri = new URI(URLDecoder.decode(href, "utf-8"));
        String query = uri.getQuery();
        int index = query.indexOf("&imgrefurl=");
        urls[0] = query.substring(0, index).replaceFirst("imgurl=", "");
        urls[1] = query.substring(index).replaceFirst("&imgrefurl=", "");

        return urls;
    }

    private boolean fetchImages(RealImageProperty prop) {
        return fetchImages(new ArrayList<RealImageProperty>() {
            {
                add(prop);
            }
        });
    }

    private boolean fetchImages(List<RealImageProperty> props) {
        boolean isSucceeded = false;
        try {
            GeneralHttpClient.doGetImage(props);
            for (RealImageProperty prop : props) {
                if (prop.getBinaryImage() != null) {
                    if (makeImage(prop)) {
                        // evaluate it to success if only one time succeeded.
                        isSucceeded = true;
                        successProp.add(prop);
                    }
                } else {
                    failureProp.add(prop);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        return isSucceeded;
    }

    private boolean makeImage(RealImageProperty prop) {
        URI imgUri = prop.getRealImagePath0();
        URI hrefUri = prop.getRealImagePath1();
        byte[] byteImage = prop.getBinaryImage();
        boolean isSucceeded = false;
        try {
            ImageFormat format = ImageType.getFormat(byteImage);
            if (format == UNKNOWN) {
                return false;
            }
            Path path = null;
            String host = hrefUri != null ? hrefUri.getHost() : imgUri.getHost();
            String fileName = Paths.get(imgUri.getPath()).getFileName().toString();
            while (true) {
                if (verifyImage(Paths.get(fileName))) {
                    String fileName_tmp = fileName.substring(0, fileName.lastIndexOf("."));
                    String ext = fileName.substring(fileName.lastIndexOf('.'));
                    fileName = "[" + host + "] " + fileName_tmp + "_" + RandomStringUtils.randomAlphanumeric(6) + ext;
                } else {
                    fileName = "[" + host + "] " + fileName + "_" + RandomStringUtils.randomAlphanumeric(6) + "." + format;
                }

                path = Paths.get(Config.getTmpDir() + File.separator + fileName);
                if (!Files.exists(path)) {
                    prop.setTmpImageName(path.getFileName().toString());
                    break;
                }
            }

            if (Config.isSaveImage()) {
                if (path != null) {
                    path = Files.write(path, byteImage);
                    if (path.toFile().exists()) {
                        prop.setType(FILE);
                        prop.setRealImagePath0Tmp(prop.getRealImagePath0());
                        prop.setRealImagePath0(path.toUri());
                        prop.setRealImagePath1Tmp(prop.getRealImagePath1());
                        prop.setRealImagePath1(null);
                        Files.setLastModifiedTime(path, FileTime.fromMillis(lmodMaker.getLmod(prop.getLmod())));
                        isSucceeded = true;
                    }
                }
            } else {
                isSucceeded = true;
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        return isSucceeded;
    }

    private boolean verifyImage(Path path) {
        try {
            String mimeType = Files.probeContentType(path);
            if (mimeType != null && mimeType.startsWith("image/")) {
                return true;
            } else {
                logger.debug("mimeType: {}, path: {}", mimeType, path);
            }
        } catch (IOException ex) {
            logger.error("", ex);
        }
        return false;
    }

    private void displayResults() {
        logger.debug("successProp: {}", successProp.size());
        logger.debug("failureProp: {}", failureProp.size());
        Set<URI> httpUris = new TreeSet<>();
        Set<Path> fileUris = new TreeSet<>();
        String dir = "";
        String uri = "";
        String path = "";
        String result = "";
        String exception = "";

        dir = dirlist.stream().map((p) -> p.toAbsolutePath() + "\n").reduce(dir, String::concat);

        successProp.stream().forEach((prop) -> {
            String scheme = prop.getRealImagePath0().getScheme();
            logger.debug("Success scheme: {}", scheme);
            if (scheme.equals(SchemeType.HTTP.toString()) || scheme.equals(SchemeType.HTTPS.toString())) {
                httpUris.add(prop.getRealImagePath0());
            } else if (scheme.equals(SchemeType.FILE.toString())) {
                fileUris.add(Paths.get(prop.getRealImagePath0()));
            }
        });

        logger.debug("httpUris: {}", httpUris.size());
        logger.debug("fileUris: {}", fileUris.size());
        uri = httpUris.stream().map((h) -> h + "\n").reduce(uri, String::concat);
        path = fileUris.stream().map((f) -> f + "\n").reduce(path, String::concat);

        result = uri + path;
        successTxt.setText(succ_result1
                + fileUris.size() + "\n"
                + succ_result2 + httpUris.size() + "\n"
                + succ_result3 + dirlist.size() + "\n"
                + dir + "\n"
                + succ_result4 + "\n"
                + result + "\n");

        httpUris.clear();
        fileUris.clear();

        uri = "";
        for (RealImageProperty prop : failureProp) {
            if (prop.getException() != null) {
                exception += prop.getRealImagePath0() + Rsc.get("con_AI_cause") + prop.getException() + "\n";
                continue;
            }
            String scheme = prop.getRealImagePath0().getScheme();
            logger.debug("Fail scheme: {}", scheme);
            if (scheme.equals(SchemeType.HTTP.toString()) || scheme.equals(SchemeType.HTTPS.toString())) {
                httpUris.add(prop.getRealImagePath0());
            } else if (scheme.equals(SchemeType.FILE.toString())) {
                fileUris.add(Paths.get(prop.getRealImagePath0()));
            }
        }
        logger.debug("httpUris: {}", httpUris.size());
        logger.debug("fileUris: {}", fileUris.size());

        uri = httpUris.stream().map((h1) -> h1 + "\n").reduce(uri, String::concat);

        path = "";
        path = fileUris.stream().map((f) -> f + "\n").reduce(path, String::concat);

        result = uri + path;
        failureTxt.setText(fail_result1
                + result + "\n"
                + exception + "\n");
    }

    private RealImageProperty makeRealImageProp(Path path) {
        RealImageProperty prop;
        long epochMilli = 0;
        String exception = null;
        if (Files.exists(path)) {
            if (path.toFile().canRead()) {
                epochMilli = path.toFile().lastModified();
            } else {
                exception = ex2;
            }
        } else {
            exception = Rsc.get("con_AI_ex_3");
        }

        prop = new RealImageProperty(path.toUri(), lmodMaker.getLmod(epochMilli));
        if (exception != null) {
            prop.setException(exception);
        }

        return prop;
    }

    private void replaceLayout() {
        bp = new BorderPane(splitPane, toolMenuBar, null, vbox, null);
        scene = new Scene(bp);
        setScene(parentController.applyStyleSheet(scene));

        resizeStage();
        attachEvents();

        parentController.restoreScene(parentController.getAnalyzeImage());
        isReplaced = false;
    }

    private void replaceLayout(Map<RealImageProperty, Long> checkExisting) {
        duplicateTxt = new TextArea();
        String text = Rsc.get("con_AI_analyzed");
        for (Entry<RealImageProperty, Long> e : checkExisting.entrySet()) {
            text = text + e.getKey().getRealImagePath0() + "\n";
        }
        duplicateTxt.setText(text);
        duplicateTxt.setEditable(false);
        duplicateTxt.setWrapText(true);
        duplicateTxt.getStyleClass().add("extract-image-box-duplicate");

        createPanel();

        scene = new Scene(grid);
        setScene(parentController.applyStyleSheet(scene));

        resizeStageGP();
        attachEvents();

        parentController.restoreScene(parentController.getAnalyzeImage());

        isReplaced = true;
    }

    private void displayRequested() {
        List<Request> requests = Backend.getQueue();
        String header = Rsc.get("con_AI_remaining");
        String text = "";
        for (Request req : requests) {
            text = text + req.getRealImagePath0() + "\n";
        }
        successTxt.setText(header + text);
        failureTxt.clear();
    }

    private void createPanel() {
        double width = this.parentController.getStage().getWidth();
        double height = this.parentController.getStage().getHeight();
        grid = new GridPanePanel(gridColumn, gridRow, width, height);

        GridPane.setConstraints(toolMenuBar, 0, 0, 1, 1, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(duplicateTxt, 0, 1, 1, 2, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(splitPane, 0, 3, 1, 2, HPos.LEFT, VPos.TOP);
        GridPane.setConstraints(vbox, 0, 5, 1, 1, HPos.LEFT, VPos.TOP);

        grid.getChildren().addAll(toolMenuBar, duplicateTxt, splitPane, vbox);
    }
}
