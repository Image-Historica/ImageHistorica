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
package com.imagehistorica.controller;

import static com.imagehistorica.util.Constants.ACTIVATOR_PROPS;
import static com.imagehistorica.util.Constants.INITIAL_SCENE_CONTROLLER;
import static com.imagehistorica.util.Constants.KEY_PROPS;
import static com.imagehistorica.util.Constants.SEARCH_ACCESS_POINT;
import static com.imagehistorica.util.Constants.WEB_BROWSER_CONTROLLER;

import com.imagehistorica.Activator;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.cache.HistoricaDirCache;
import com.imagehistorica.common.state.ImageState;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.controllers.Details;
import com.imagehistorica.controllers.AnalyzeResult;
import com.imagehistorica.controllers.AppreciateImage;
import com.imagehistorica.controllers.AppreciateOrder;
import com.imagehistorica.controllers.AnalyzeImage;
import com.imagehistorica.controllers.AnalyzeStatus;
import com.imagehistorica.controllers.InitialScene;
import com.imagehistorica.controllers.WebBrowser;
import com.imagehistorica.controllers.Search;
import com.imagehistorica.httpclient.CentralHttpClient;
import com.imagehistorica.httpclient.GeneralHttpClient;
import com.imagehistorica.Config;
import com.imagehistorica.Key;
import com.imagehistorica.common.state.SearchState;
import com.imagehistorica.common.state.State;
import com.imagehistorica.common.state.WebState;
import com.imagehistorica.util.view.CommonAlert;
import com.imagehistorica.util.view.FreeAlert;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.search.SearchImage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.entity.ByteArrayEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class ImageHistoricaController extends Controller {

    private int appOptions;

    private TreeCache treeCache = TreeCache.getInstance();
    private HistoricaDirCache hisDirCache = null;

    private Stage primaryStage = null;
    private AnalyzeResult analyzeResult = null;
    private AppreciateImage appreciateImage = null;
    private AppreciateOrder appreciateOrder = null;
    private AnalyzeImage analyzeImage = null;
    private AnalyzeStatus analyzeStatus = null;
    private Details details = null;
    private InitialScene initialScene = null;
    private Search search = null;
    private WebBrowser webBrowser = null;

    private UpdateChecker uc;
    private Map<String, String> updateInfos = null;
    private FreeAlert fa = null;

    private final Logger logger = LoggerFactory.getLogger(ImageHistoricaController.class);

    @Override
    public void start(int appOptions, String newVersion) {
        this.appOptions = appOptions;

        Backend.setVersion(newVersion);
        Backend.startImageHistoricaDB();
        Backend.startExecutorService();

        hisDirCache = loadHistoricaDirCache();
        if (hisDirCache == null) {
            hisDirCache = HistoricaDirCache.getInstance();
        }

        treeCache.restoreHistoricaDirsMap(hisDirCache.getHistoricaDirMap());
        treeCache.restoreHistoricaMeaningsMap(hisDirCache.getHistoricaMeaningsMap());
        treeCache.restoreHistoricaNumsMap(hisDirCache.getHistoricaNumsMap());

        if (logger.isDebugEnabled()) {
            logger.debug("Total memory usage: {}", Runtime.getRuntime().totalMemory());
            logger.debug("Max memory usage: {}", Runtime.getRuntime().maxMemory());

            treeCache.backupHistoricaDirsMap().entrySet().stream().forEach((h) -> {
                logger.debug("[HistoricaDirMap] historicaDirId: {}, historicaPath: {}", h.getKey(), h.getValue());
            });

            treeCache.backupHistoricaMeaningsMap().entrySet().stream().forEach((h) -> {
                logger.debug("[HistoricaMeaningsMap] historicaDirId: {}, meaningId: {}", h.getKey(), h.getValue());
            });

            treeCache.backupHistoricaNumsMap().entrySet().stream().forEach((h) -> {
                logger.debug("[HistoricaNumsMap] historicaDirId: {}, numOfImages: {}", h.getKey(), h.getValue());
            });
        }

        treeCache.startInitialSetup();

        uc = new UpdateChecker(appOptions);
        updateInfos = uc.getUpdateInfos();
    }

    @Override
    public void createInitialScene(Stage primaryStage, String libraries, String oldVersion, String newVersion, boolean isUpdateRequested, boolean isForcedUpdateRequested) {
        this.primaryStage = primaryStage;
        initialScene = new InitialScene();
        setInitialScene((InitialScene) this.createScene(initialScene, INITIAL_SCENE_CONTROLLER));

        if (updateInfos != null) {
            boolean forcedUpdate = Boolean.valueOf(updateInfos.get("forcedUpdate"));
            boolean update = Boolean.valueOf(updateInfos.get("update"));
            String downloadUrl = updateInfos.get("downloadUrl");
            String latestLib = updateInfos.get("latestLibs");
            String[] latestLibs = latestLib.split(",");
            logger.info("User   libraries: {}", libraries);
            logger.info("Latest libraries: {}", latestLib);
            if (update || forcedUpdate) {
                if (forcedUpdate) {
                    logger.info("ForcedUpdate required...");
                    uc.forcedUpdate(latestLibs);
                    closeRequest();
                } else if (update) {
                    logger.info("Update required...");
                    uc.update(downloadUrl, latestLibs);
                }
            } else {
                Set<String> userLibs = new TreeSet<>(Arrays.asList(libraries.split(",")));
                Set<String> centralLibs = new TreeSet<>(Arrays.asList(latestLibs));
                if (!userLibs.equals(centralLibs)) {
                    logger.info("Update required due to different library info...");
                    uc.update(downloadUrl, latestLibs);
                } else {
                    if (isUpdateRequested || !oldVersion.equals(newVersion)) {
                        logger.info("Update activator.properties...");
                        File prop = new File(ACTIVATOR_PROPS);
                        Activator activator = Activator.getInstance();
                        activator.loadActivator(prop);
                        activator.replace("isUpdateRequested_" + appOptions, "false");
                        activator.replace("libs_" + appOptions, libraries);
                        activator.replace("version_" + appOptions, newVersion);
                        File tmp = new File(ACTIVATOR_PROPS + "_tmp");
                        activator.storeActivator(activator, tmp);
                        if (tmp.exists() && tmp.canRead()) {
                            try {
                                Files.move(tmp.toPath(), prop.toPath(), StandardCopyOption.ATOMIC_MOVE);
                            } catch (IOException e) {
                                logger.error("", e);
                            }
                        } else {
                            logger.error("Could not make activator.properties");
                        }
                    }
                }
            }

            boolean isFree = Boolean.valueOf(updateInfos.get("isFree"));
            if (!isFree) {
                Config.setFree(isFree);
            } else {
                fa = new FreeAlert(this);
            }

            String broadcast = updateInfos.get("broadcast");
            if (broadcast != null && !broadcast.isEmpty()) {
                String[] messages = broadcast.split(",");
                Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, messages[0], messages[1], messages[2]);
                alert.showAndWait();
            }

            Backend.setUpdated(true);

        } else {
            logger.info("UpdateInfos is null...");
            if (isForcedUpdateRequested) {
                logger.error("Could not launch ImageHistorica due to no update info and enabled flag of 'isForcedUpdateRequested'");
                Alert alert = CommonAlert.makeAlert(AlertType.ERROR, Rsc.get("c_IH_title_1"), Rsc.get("c_IH_header_1"), Rsc.get("c_IH_content_1"));
                alert.showAndWait();
                closeRequest();
            }

            Backend.setUpdated(false);
        }

        Key key = Key.getInstance();
        System.out.println("acs: " + key.getProperty("ACCESS_KEY"));
        System.out.println("sct: " + key.getProperty("SECRET_KEY"));
        String token = key.getProperty("TOKEN");
        if (token == null) {
            completeKey(key);
        }
    }

    public Factory createScene(Factory controller, String concreteScene) {
        Backend.setPrevController(controller);
        Backend.clearNextController(controller);
        logger.debug("setPrevControler()...{}", concreteScene);

        Factory factory = Factory.getController(concreteScene);
        Scene scene = factory.createLayout(this);
        logger.debug("ControllerType: {}", factory.getControllerType());

        primaryStage.setScene(applyStyleSheet(scene));
        primaryStage.show();

        return factory;
    }

    public Scene applyStyleSheet(Scene scene) {
        String style = ImageHistoricaController.class.
                getResource("/resources/css/stylesheet.css").toExternalForm();
        scene.getStylesheets().add(style);
        return scene;
    }

    public Stage getStage() {
        return this.primaryStage;
    }

    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    public AnalyzeResult getAnalyzeResult() {
        return this.analyzeResult;
    }

    public void setAnalyzeResult(AnalyzeResult analyzeResult) {
        this.analyzeResult = analyzeResult;
    }

    public AppreciateImage getAppreciateImage() {
        return this.appreciateImage;
    }

    public void setAppreciateImage(AppreciateImage appreciateImage) {
        this.appreciateImage = appreciateImage;
    }

    public AppreciateOrder getAppreciateOrder() {
        return this.appreciateOrder;
    }

    public void setAppreciateOrder(AppreciateOrder appreciateOrder) {
        this.appreciateOrder = appreciateOrder;
    }

    public AnalyzeImage getAnalyzeImage() {
        return this.analyzeImage;
    }

    public void setAnalyzeImage(AnalyzeImage analyzeImage) {
        this.analyzeImage = analyzeImage;
    }

    public AnalyzeStatus getAnalyzeStatus() {
        return this.analyzeStatus;
    }

    public void setAnalyzeStatus(AnalyzeStatus analyzeStatus) {
        this.analyzeStatus = analyzeStatus;
    }

    public Details getDetails() {
        return this.details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public InitialScene getInitialScene() {
        return this.initialScene;
    }

    public void setInitialScene(InitialScene initialScene) {
        this.initialScene = initialScene;
    }

    public Search getSearch() {
        return this.search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public WebBrowser getWebBrowser() {
        return this.webBrowser;
    }

    public void setWebBrowser(WebBrowser webBrowser) {
        this.webBrowser = webBrowser;
    }

    public void restoreScene(Factory factory) {
        primaryStage.setScene(factory.restoreScene());
        primaryStage.show();
    }

    public void removeHistoricaDirCache() {
        try {
            Path dat = Paths.get(Config.getImageHistoricaDb() + "/ImageHistoricaDB/HistoricaDirCache.dat");
            if (Files.exists(dat)) {
                Files.delete(dat);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public HistoricaDirCache loadHistoricaDirCache() {
        HistoricaDirCache cache = null;
        Path dat = Paths.get(Config.getImageHistoricaDb() + "/ImageHistoricaDB/HistoricaDirCache.dat");
        if (Files.exists(dat)) {
            try (ObjectInput input = new ObjectInputStream(new InflaterInputStream(new FileInputStream(dat.toFile())))) {
                cache = (HistoricaDirCache) input.readObject();
            } catch (Exception e) {
                logger.error("", e);
            }
        }

        return cache;
    }

    public void saveHistoricaDirCache(HistoricaDirCache historicaDirsMap) {
        try (ObjectOutput output = new ObjectOutputStream(new DeflaterOutputStream(new FileOutputStream(Config.getImageHistoricaDb() + "/ImageHistoricaDB/HistoricaDirCache.dat")))) {
            output.writeObject(historicaDirsMap);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public void closeRequest() {
        logger.info("Called closeRequest()...");
        if (analyzeStatus != null) {
            if (analyzeStatus.getService().isRunning()) {
                logger.info("Running analyzeStatus service...");
                analyzeStatus.getService().cancel();
            }
        }

        ImageState imageState = ImageState.getInstance();
        if (!imageState.getFreqViewd().isEmpty()) {
            imageState.getFreqViewd().entrySet().stream().forEach((e) -> {
                Backend.increFreqViewed(e.getKey(), e.getValue());
            });
        }

        SearchState searchState = SearchState.getInstance();
        Map<String, SearchImage> searchImages = searchState.getSeachImages();
        if (searchImages != null && !searchImages.isEmpty()) {
            Map<String, Byte> failedReqs = new HashMap<>();
            searchImages.values().stream().forEach((searchImage) -> {
                failedReqs.putAll(searchImage.getFailedReqs());
            });
            logger.info("failedreqs: {}", failedReqs.size());
            try {
                ByteArrayEntity entity = new ByteArrayEntity(new ObjectMapper().writeValueAsBytes(failedReqs));
                CentralHttpClient.doPost(SEARCH_ACCESS_POINT + "/fail", entity, new State(), 1000);
            } catch (IOException ex) {
                logger.debug("", ex);
            }
        }

        if (logger.isDebugEnabled()) {
            imageState.getImageCache().keySet().stream().forEach((h) -> {
                logger.debug("[imageCache] realImagePath: {}", h);
            });

            treeCache.getBranchNodes().entrySet().stream().forEach((h) -> {
                logger.debug("[BranchNodes] HistoricaPath: {}, ImageName: {}", h.getValue().getValue().getHistoricaPath(), h.getValue().getValue().getImageName());
            });

            treeCache.getLeafNodes().entrySet().stream().forEach((h) -> {
                logger.debug("[LeafNodes] HistoricaPath: {}, ImageName: {}", h.getValue().getValue().getHistoricaPath(), h.getValue().getValue().getImageName());
            });

            treeCache.backupHistoricaDirsMap().entrySet().stream().forEach((h) -> {
                logger.debug("[HistoricaDirMap] historicaDirId: {}, historicaPath: {}", h.getKey(), h.getValue());
            });

            treeCache.backupHistoricaNumsMap().entrySet().stream().forEach((h) -> {
                logger.debug("[HistoricaNumsMap] historicaDirId: {}, numOfImages: {}", h.getKey(), h.getValue());
            });

            treeCache.backupHistoricaMeaningsMap().entrySet().stream().forEach((h) -> {
                logger.debug("[HistoricaMeaningsMap] historicaDirId: {}, meaningsId: {}", h.getKey(), h.getValue());
            });
        }

        Backend.stopExecutorService();
        Backend.stopImageHistoricaDB();
        CentralHttpClient.close();
        GeneralHttpClient.close();

        hisDirCache.setHistoricaDirsMap(treeCache.backupHistoricaDirsMap());
        hisDirCache.setHistoricaNumsMap(treeCache.backupHistoricaNumsMap());
        hisDirCache.setHistoricaMeaningsMap(treeCache.backupHistoricaMeaningsMap());
        saveHistoricaDirCache(hisDirCache);

        if (appreciateImage != null) {
            TabPane tabPane = appreciateImage.getTabPane();
            ObservableList<Tab> tabs = tabPane.getTabs();
            String shortCutTabs = "";
            String lastSelectedTab = "";
            if (tabs.size() > 1) {
                shortCutTabs = tabs.stream().map((tab) -> tab.getId()).filter((id) -> (!id.equals("0"))).map((id) -> id + ",").reduce(shortCutTabs, String::concat);
                lastSelectedTab = String.valueOf(tabPane.getSelectionModel().getSelectedIndex());
            }
            if (shortCutTabs.isEmpty()) {
                Config.removeShortCutTabs();
            } else {
                Config.setShortCutTabs(shortCutTabs.substring(0, shortCutTabs.lastIndexOf(",")));
            }
            if (lastSelectedTab.isEmpty()) {
                Config.removeLastSelectedTab();
            } else {
                Config.setLastSelectedTab(lastSelectedTab);
            }
        }

        if (Config.isInitializing()) {
            logger.info("Config of 'isInitializing' is enabled... Will be initializing at next activation...");
            Config.removeShortCutTabs();
            Config.removeLastSelectedTab();
        }

        if (Config.isDelToken()) {
            logger.warn("Config of 'isDelToken' is enabled... Deleting token...");
            try {
                Key key = Key.getInstance();
                File keyProp = new File(KEY_PROPS);
                key.remove("TOKEN");
                File keyPropTmp = new File(KEY_PROPS + "_tmp");
                key.storeKey(key, keyPropTmp);
                if (keyPropTmp.exists() && keyPropTmp.canRead()) {
                    Files.move(keyPropTmp.toPath(), keyProp.toPath(), StandardCopyOption.ATOMIC_MOVE);
                }
                if (keyProp.exists()) {
                    Config.removeDelToken();
                    logger.warn("Config of 'isDelToken' is enabled... Deleted...");
                } else {
                    CommonAlert ca = new CommonAlert();
                    ca.showIOException(KEY_PROPS);
                }
            } catch (IOException ex) {
                logger.error("Could not update activator.properties...", ex);
            }
        }
        Config.storeConfig();

        Platform.exit();
        logger.info("Platform exit...");
        System.exit(0);
    }

    public boolean showFreeAlert(Factory factory) {
        if (fa != null) {
            fa.makeFreeAlert(factory);
            return true;
        } else {
            return false;
        }
    }

    private void completeKey(Key key) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(getStage());
        alert.setTitle(Rsc.get("c_IH_title_2"));

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

        Label labelH = new Label(Rsc.get("c_IH_header_2"));
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

        Label labelC_1 = new Label(Rsc.get("c_IH_label_1"));
        labelC_1.setWrapText(true);
        labelC_1.setAlignment(Pos.CENTER_LEFT);
        labelC_1.setMaxWidth(Double.MAX_VALUE);
        labelC_1.setMaxHeight(Double.MAX_VALUE);
        gridC.add(labelC_1, 0, 0);

        Label labelC_2 = new Label("");
        gridC.add(labelC_2, 0, 1);

        TextField tf = new TextField();
        tf.setPrefColumnCount(40);
        tf.setEditable(true);
        dialogPane.setContent(tf);
        gridC.add(tf, 0, 2);

        Label contentLabel_3 = new Label(Rsc.get("c_IH_label_2"));
        labelC_2.setWrapText(true);
        labelC_2.setAlignment(Pos.CENTER_LEFT);
        labelC_2.setMaxWidth(Double.MAX_VALUE);
        labelC_2.setMaxHeight(Double.MAX_VALUE);
        gridC.add(contentLabel_3, 0, 3);

        Hyperlink link = new Hyperlink("https://image-historica.com/");
        link.setOnMouseClicked(e -> {
            WebState state = WebState.getInstance();
            state.setLoadPage(link.getText());
            setWebBrowser((WebBrowser) createScene(getInitialScene(), WEB_BROWSER_CONTROLLER));
        });
        gridC.add(link, 0, 4);

        dialogPane.setHeader(gridH);
        dialogPane.setContent(gridC);
        dialogPane.setGraphic(null);

        ButtonType b1 = new ButtonType(Rsc.get("okBtn"));
        alert.getButtonTypes().setAll(b1);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == b1) {
            String token = tf.getText().trim();
            if (token != null && !token.isEmpty()) {
                File prop = new File(KEY_PROPS);
                key.put("TOKEN", token);
                File tmp = new File(KEY_PROPS + "_tmp");
                key.storeKey(key, tmp);
                if (tmp.exists() && tmp.canRead()) {
                    try {
                        Files.move(tmp.toPath(), prop.toPath(), StandardCopyOption.ATOMIC_MOVE);
                    } catch (AccessDeniedException e) {
                        Alert a = CommonAlert.makeAlert(AlertType.ERROR, "エラー", "アクセスが拒否されました。該当ファイルを開いている場合は閉じてください。", null);
                        a.showAndWait();
                        completeKey(key);
                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }
            }
        }
    }
}
