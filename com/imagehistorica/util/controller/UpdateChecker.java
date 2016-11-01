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

import com.imagehistorica.util.Downloader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.imagehistorica.Activator;
import static com.imagehistorica.util.Constants.NOTIFY_ACCESS_POINT;

import com.imagehistorica.databases.Backend;
import com.imagehistorica.httpclient.CentralHttpClient;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imagehistorica.Config;
import com.imagehistorica.Key;
import com.imagehistorica.util.view.CommonAlert;
import com.imagehistorica.controller.resources.Rsc;
import static com.imagehistorica.util.Constants.ACTIVATOR_PROPS;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class UpdateChecker {

    private final int appOptions;
    private final Activator activator = Activator.getInstance();
    private final Logger logger = LoggerFactory.getLogger(UpdateChecker.class);

    protected UpdateChecker(int appOptions) {
        this.appOptions = appOptions;
    }

    public Map<String, String> getUpdateInfos() {
        Map<String, String> updateInfos = null;
        String query = NOTIFY_ACCESS_POINT + "/check/" + Key.accessKey + "~" + appOptions + "~" + Backend.major + "~" + Backend.minor + "~" + Backend.revision;
        logger.debug("Start updateCheck()... {}", query);
        String rbody = CentralHttpClient.doGet(query, 10000);
        if (rbody != null && !rbody.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                updateInfos = mapper.readValue(rbody, new TypeReference<Map<String, String>>() {
                });
            } catch (IOException e) {
                logger.error("", e);
            }
        } else {
            logger.debug("Response of updateChecker is null or empty...");
        }
        return updateInfos;
    }

    protected void update(String downloadUrl, String[] latestLibs) {
        logger.info("Required update due to minor version updated...");
        Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("c_UC_title"), Rsc.get("c_UC_header_2"), Rsc.get("c_UC_content_2"));
        alert.showAndWait();
        logger.info("Prepare update operation for next activation...");
        List<String> requiredLibs = setUpdateRequest(latestLibs);
        if (requiredLibs != null && !requiredLibs.isEmpty()) {
            System.out.println("requiredsize: " + requiredLibs.size());
            logger.info("Finished preparation for update... Start to download required libraries asynchronously...");
            Downloader downloader = new Downloader(downloadUrl, requiredLibs);
            downloader.start();
        } else {
            logger.info("Finished preparation for update due to no requiredLibs...");
        }
    }

    protected void forcedUpdate(String[] latestLibs) {
        logger.warn("Required forceUpdate due to major version updated...");
        Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("c_UC_title"), Rsc.get("c_UC_header_1"), Rsc.get("c_UC_content_1"));
        alert.showAndWait();
        logger.warn("Prepare update operation for next activation...");
        setUpdateRequest(latestLibs);
        logger.warn("Finished preparation for update and exit application...");
    }

    private List<String> setUpdateRequest(String[] latestLibs) {
        logger.info("Start setUpdateRequest()... appOptions: {}", appOptions);
        logger.info("Latest Libraries: {}", Arrays.asList(latestLibs));
        List<String> requiredLibs = new ArrayList<>();
        try {
            if (updateActivator()) {
                String[] libs = activator.getProperty("libs_" + appOptions).split(",");
                for (String latestLib : latestLibs) {
                    boolean found = false;
                    for (String lib : libs) {
                        if (lib.equals(latestLib)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        requiredLibs.add(latestLib);
                    }
                }

                if (!requiredLibs.isEmpty()) {
                    File lib = new File(Config.getInitialDirectory() + "/lib");
                    File libTmp = new File(Config.getInitialDirectory() + "/lib_tmp");
                    if (!libTmp.exists()) {
                        libTmp.mkdir();
                    } else {
                        File[] files = libTmp.listFiles();
                        if (files.length > 0) {
                            FileUtils.cleanDirectory(libTmp);
                        }
                    }

                    if (requiredLibs.size() > 1) {
                        FileUtils.copyDirectory(lib, libTmp);
                        File[] existingTmpLibs = libTmp.listFiles();
                        for (String requiredLib : requiredLibs) {
                            String requiredLibName = requiredLib.substring(0, requiredLib.lastIndexOf("-"));
                            for (File existingTmpLib : existingTmpLibs) {
                                try {
                                    String fileName = existingTmpLib.getName();
                                    String existingName = fileName.substring(0, fileName.lastIndexOf("-"));
                                    if (existingName.equals(requiredLibName)) {
                                        Files.delete(existingTmpLib.toPath());
                                    }
                                } catch (StringIndexOutOfBoundsException e) {
                                    Alert alert = CommonAlert.makeAlert(AlertType.ERROR, Rsc.get("c_UC_title"), Rsc.get("c_UC_header_3"), existingTmpLib.getName());
                                    alert.showAndWait();
                                    return null;
                                }
                            }
                        }
                    }
                }
            } else {
                CommonAlert ca = new CommonAlert();
                ca.showIOException(ACTIVATOR_PROPS);
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        logger.info("Required Libraries: {}", requiredLibs);
        return requiredLibs;
    }

    protected boolean updateActivator() throws IOException {
        File prop = new File(ACTIVATOR_PROPS);
        activator.loadActivator(prop);
        activator.replace("isUpdateRequested_" + appOptions, "true");
        File tmp = new File(ACTIVATOR_PROPS + "_tmp");
        activator.storeActivator(activator, tmp);
        if (tmp.exists() && tmp.canRead()) {
            Files.move(tmp.toPath(), prop.toPath(), StandardCopyOption.ATOMIC_MOVE);
        }
        return prop.exists();
    }
}
