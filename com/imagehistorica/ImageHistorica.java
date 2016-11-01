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
package com.imagehistorica;

import com.imagehistorica.controller.Controller;
import com.imagehistorica.controller.ImageHistoricaController;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class ImageHistorica extends Application {

    private Controller controller;
    private boolean isInitializing = false;
    private boolean isUpdateRequested = false;
    private boolean isForcedUpdateRequested = false;
    private String application;
    private String oldVersion;
    private String newVersion;
    private String libraries;
    private static Logger logger;

    public static void main(String[] args) {
        try {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            JoranConfigurator jc = new JoranConfigurator();
            jc.setContext(lc);
            lc.reset();
            jc.doConfigure(Files.newInputStream(Paths.get("conf/logback.xml")));
            logger = LoggerFactory.getLogger(ImageHistorica.class);
            logger.info("Start application...");
            launch(args);
        } catch (JoranException ex) {
            System.out.println("Could not make custom logger...");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Could not get conf/logback.xml");
            ex.printStackTrace();
        } catch (Throwable t) {
            logger.error("", t);
        }
    }

    @Override
    public void init() {
        logger.info("Start initial setup...");
        try {
            validate(getParameters().getNamed());
            if (application.equals("ImageHistorica")) {
                int appOptions = 0;
                Config.loadConfig();
                Config.setVersion(newVersion);
                if (Config.isInitializing() || isInitializing) {
                    try {
                        FileUtils.deleteDirectory(new File(Config.getImageHistoricaDb() + "/ImageHistoricaDB"));
                        Config.setInitializing(false);
                        Config.removeShortCutTabs();
                        Config.removeLastSelectedTab();
                    } catch (IOException ex) {
                        logger.error("Could not delete the directory of ImageHistoricaDB...", ex);
                    }
                }
                controller = new ImageHistoricaController();
                controller.start(appOptions, newVersion);

            } else {

            }

            try {
                Path activateCmp = Paths.get(Config.getInitialDirectory() + "/activateCmp");
                if (!Files.exists(activateCmp)) {
                    logger.info("Create activateCmp...");
                    Files.createFile(activateCmp);
                }
            } catch (IOException ex) {
                logger.error("Could not create activateCmp...", ex);
            }
        } catch (Exception e) {
            logger.error("Could not launch application...", e);
            System.exit(1);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Called ImageHistorica start()...");
        if (application.equals("ImageHistorica")) {
            Image img16 = new Image(getClass().getResourceAsStream("/resources/images/ImageHistorica_16.png"));
            Image img32 = new Image(getClass().getResourceAsStream("/resources/images/ImageHistorica_32.png"));
            primaryStage.getIcons().addAll(img16, img32);
            primaryStage.setWidth(Config.getDefaultWidth());
            primaryStage.setHeight(Config.getDefaultHeight());
            primaryStage.initStyle(StageStyle.UNDECORATED);
            controller.createInitialScene(primaryStage, libraries, oldVersion, newVersion, isUpdateRequested, isForcedUpdateRequested);
        } else {

        }
    }

    private void validate(Map<String, String> initialParams) {
        try {
            String initializing = initialParams.get("isInitializing");
            String updateRequested = initialParams.get("isUpdateRequested");
            String forcedUpdateRequested = initialParams.get("isForcedUpdateRequested");
            application = initialParams.get("application");
            oldVersion = initialParams.get("oldVersion");
            newVersion = initialParams.get("newVersion");
            libraries = initialParams.get("libraries");

            if (initializing == null) {
                throw new NullPointerException("isInitializing is null...");
            } else {
                logger.info("isInitializing: {}", initializing);
                if (initializing.equals("true")) {
                    isInitializing = true;
                }
            }

            if (updateRequested == null) {
                throw new NullPointerException("isUpdateRequested is null...");
            } else {
                logger.info("isUpdateRequested: {}", updateRequested);
                if (updateRequested.equals("true")) {
                    isUpdateRequested = true;
                }
            }

            if (forcedUpdateRequested == null) {
                throw new NullPointerException("isForcedUpdateRequested is null...");
            } else {
                logger.info("isForcedUpdateRequested: {}", forcedUpdateRequested);
                if (forcedUpdateRequested.equals("true")) {
                    isForcedUpdateRequested = true;
                }
            }

            if (application == null) {
                throw new NullPointerException("application is null...");
            } else {
                logger.info("application: {}", application);
            }

            if (oldVersion == null) {
                throw new NullPointerException("oldVersion is null...");
            } else {
                logger.info("oldVersion: {}", oldVersion);
                String[] vers = oldVersion.split("\\.");
                if (vers.length != 3) {
                    throw new Exception("Invalid oldVersion..." + oldVersion);
                }
                try {
                    Byte.parseByte(vers[0]);
                    Byte.parseByte(vers[1]);
                    Byte.parseByte(vers[2]);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Invalid oldVersion" + oldVersion);
                }
            }

            if (newVersion == null) {
                throw new NullPointerException("newVersion is null...");
            } else {
                logger.info("newVersion: {}", newVersion);
                String[] vers = newVersion.split("\\.");
                if (vers.length != 3) {
                    throw new Exception("Invalid newVersion..." + newVersion);
                }
                try {
                    Byte.parseByte(vers[0]);
                    Byte.parseByte(vers[1]);
                    Byte.parseByte(vers[2]);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Invalid oldVersion" + newVersion);
                }
            }

            if (libraries == null) {
                throw new NullPointerException("libraries is null...");
            }

        } catch (Exception e) {
            logger.error("", e);
        }
    }
}
