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

import static com.imagehistorica.util.Constants.CONFIG_PROPS;

import com.imagehistorica.util.view.CommonAlert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;
import javafx.stage.Screen;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class Config extends Properties {

    private static String defaultWidth = "1000.0";
    private static String defaultHeight = "800.0";
    private static String userAgent = "ImageHistorica/1.0.0";
    private static final String isFree = "true";
    private static final String version = "1.0.0";
    private static final String highQualityImage = "true";
    private static final String fetchImage = "true";
    private static final String refresh = "false";
    private static final String reconstruct = "false";
    private static final String initializing = "false";
    private static final String delToken = "false";
    private static final String numOfThreads = String.valueOf(Runtime.getRuntime().availableProcessors());
    private static final String initialDirectory = System.getProperty("user.dir");
    private static final String imageHistoricaDb = initialDirectory;
    private static final String rootDir = initialDirectory + File.separator + "ImageHistorica";
    private static final String tmpDir = initialDirectory + File.separator + "tmp";
    private static final String country = System.getProperty("user.country");
    private static final String lang = System.getProperty("user.language");
    private static final String imageDir = tmpDir;
    private static final String collectDbStats = "false";
    private static final String defaultFeature = "ColorLayout";
    private static final String shortCutTabs = "";
    private static final String lastSelectedTab = "";

    private static final String procUnitInMaking = "2";
    private static final String procUnitInGetting = "128";

    private static final String prefetchImagesInFlowPane = "100";
    private static final String prefetchImagesInStage = "10";

    private static final String additionalStageWidthRatio = "0.5";
    private static final String additionalStageHeightRatio = "0.5";

    private static final String saveImage = "true";

    private static final String socketTimeout = "10000";
    private static final String connectionTimeout = "5000";

    private static final Config config = new Config();

    private Config() {
        Double widthDisplay = Screen.getPrimary().getVisualBounds().getWidth();
        Double heightDisplay = Screen.getPrimary().getVisualBounds().getHeight();
        defaultWidth = widthDisplay < 1000.0 ? String.valueOf(widthDisplay - 100) : "1000.0";
        defaultHeight = heightDisplay < 800.0 ? String.valueOf(heightDisplay - 100) : "800.0";
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
    }

    public static void loadConfig() {
        try (FileInputStream fis = new FileInputStream(CONFIG_PROPS);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader br = new BufferedReader(isr)) {
            config.load(br);
        } catch (IOException e) {
            CommonAlert.getErrorLog(e);
        }
    }

    public static void storeConfig() {
        String comments = "This file is to store customized config.";
        try (FileOutputStream fos = new FileOutputStream(CONFIG_PROPS);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                BufferedWriter bw = new BufferedWriter(osw)) {
            config.store(bw, comments);
        } catch (IOException e) {
            CommonAlert ca = new CommonAlert();
            ca.showIOException(CONFIG_PROPS);
        }
    }

    public static String getVersion() {
        return config.getProperty("version", version);
    }

    public static void setVersion(String version) {
        if (config.containsKey("version")) {
            config.replace("version", version);
        } else {
            config.put("version", version);
        }
        userAgent = "ImageHistorica/" + version;
    }

    public static boolean isFree() {
        return Boolean.parseBoolean(config.getProperty("isFree", isFree));
    }

    public static void setFree(boolean isFree) {
        if (config.containsKey("isFree")) {
            config.replace("isFree", String.valueOf(isFree));
        } else {
            config.put("isFree", String.valueOf(isFree));
        }
    }

    public static double getDefaultWidth() {
        return Double.parseDouble(config.getProperty("defaultWidth", defaultWidth));
    }

    public static void setDefaultWidth(double defaultWidth) {
        if (config.containsKey("defaultWidth")) {
            config.replace("defaultWidth", String.valueOf(defaultWidth));
        } else {
            config.put("defaultWidth", String.valueOf(defaultWidth));
        }
    }

    public static void removeDefaultWidth() {
        config.remove("defaultWidth");
    }

    public static double getDefaultHeight() {
        return Double.parseDouble(config.getProperty("defaultHeight", defaultHeight));
    }

    public static void setDefaultHeight(double height) {
        if (config.containsKey("defaultHeight")) {
            config.replace("defaultHeight", String.valueOf(height));
        } else {
            config.put("defaultHeight", String.valueOf(height));
        }
    }

    public static void removeDefaultHeight() {
        config.remove("defaultHeight");
    }

    public static boolean isHighQualityImage() {
        return Boolean.parseBoolean(config.getProperty("highQualityImage", highQualityImage));
    }

    public static void setHighQualityImage(boolean highQualityImage) {
        if (config.containsKey("highQualityImage")) {
            config.replace("highQualityImage", String.valueOf(highQualityImage));
        } else {
            config.put("highQualityImage", String.valueOf(highQualityImage));
        }
    }

    public static void removeHighQualityImage() {
        config.remove("highQualityImage");
    }

    public static boolean isFetchImage() {
        return Boolean.parseBoolean(config.getProperty("fetchImage", fetchImage));
    }

    public static void setFetchImage(boolean fetchImage) {
        if (config.containsKey("fetchImage")) {
            config.replace("fetchImage", String.valueOf(fetchImage));
        } else {
            config.put("fetchImage", String.valueOf(fetchImage));
        }
    }

    public static void removeFetchImage() {
        config.remove("fetchImage");
    }

    public static boolean isRefresh() {
        return Boolean.parseBoolean(config.getProperty("refresh", refresh));
    }

    public static void setRefresh(boolean refresh) {
        if (config.containsKey("refresh")) {
            config.replace("refresh", String.valueOf(refresh));
        } else {
            config.put("refresh", String.valueOf(refresh));
        }
    }

    public static void removeRefresh() {
        config.remove("refresh");
    }

    public static boolean isReconstruct() {
        return Boolean.parseBoolean(config.getProperty("reconstruct", reconstruct));
    }

    public static void setReconstruct(boolean reconstruct) {
        if (config.containsKey("reconstruct")) {
            config.replace("reconstruct", String.valueOf(reconstruct));
        } else {
            config.put("reconstruct", String.valueOf(reconstruct));
        }
    }

    public static void removeReconstruct() {
        config.remove("reconstruct");
    }

    public static boolean isInitializing() {
        return Boolean.parseBoolean(config.getProperty("initializing", initializing));
    }

    public static void setInitializing(boolean initializing) {
        if (config.containsKey("initializing")) {
            config.replace("initializing", String.valueOf(initializing));
        } else {
            config.put("initializing", String.valueOf(initializing));
        }
    }

    public static void removeInitializing() {
        config.remove("initializing");
    }

    public static boolean isDelToken() {
        return Boolean.parseBoolean(config.getProperty("delToken", delToken));
    }

    public static void setDelToken(boolean delToken) {
        if (config.containsKey("delToken")) {
            config.replace("delToken", String.valueOf(delToken));
        } else {
            config.put("delToken", String.valueOf(delToken));
        }
    }

    public static void removeDelToken() {
        config.remove("delToken");
    }

    public static boolean isCollectDbStats() {
        return Boolean.parseBoolean(config.getProperty("collectDbStats", collectDbStats));
    }

    public static void setCollectDbStats(boolean collectDbStats) {
        if (config.containsKey("collectDbStats")) {
            config.replace("collectDbStats", String.valueOf(collectDbStats));
        } else {
            config.put("collectDbStats", String.valueOf(collectDbStats));
        }
    }

    public static void removeCollectDbStats() {
        config.remove("collectDbStats");
    }

    public static int getNumOfThreads() {
        return Integer.parseInt(config.getProperty("numOfThreads", numOfThreads));
    }

    public static void setNumOfThreads(int numOfThreads) {
        if (config.containsKey("numOfThreads")) {
            config.replace("numOfThreads", String.valueOf(numOfThreads));
        } else {
            config.put("numOfThreads", String.valueOf(numOfThreads));
        }
    }

    public static void removeNumOfThreads() {
        config.remove("numOfThreads");
    }

    public static String getInitialDirectory() {
        return config.getProperty("initialDirectory", initialDirectory);
    }

    public static void setInitialDirectory(String initialDirectory) {
        if (config.containsKey("initialDirectory")) {
            config.replace("initialDirectory", initialDirectory);
        } else {
            config.put("initialDirectory", initialDirectory);
        }
    }

    public static void removeInitialDirectory() {
        config.remove("initialDirectory");
    }

    public static String getImageHistoricaDb() {
        return config.getProperty("imageHistoricaDb", imageHistoricaDb);
    }

    public static void setImageHistoricaDb(String imageHistoricaDb) {
        if (config.containsKey("imageHistoricaDb")) {
            config.replace("imageHistoricaDb", imageHistoricaDb);
        } else {
            config.put("imageHistoricaDb", imageHistoricaDb);
        }
    }

    public static void removeImageHistoricaDb() {
        config.remove("imageHistoricaDb");
    }

    public static String getRootDir() {
        return config.getProperty("rootDir", rootDir);
    }

    public static void setRootDir(String rootDir) {
        if (config.containsKey("rootDir")) {
            config.replace("rootDir", rootDir);
        } else {
            config.put("rootDir", rootDir);
        }
    }

    public static void removeRootDir() {
        config.remove("rootDir");
    }

    public static String getTmpDir() {
        return config.getProperty("tmpDir", tmpDir);
    }

    public static void setTmpDir(String tmpDir) {
        if (config.containsKey("tmpDir")) {
            config.replace("tmpDir", tmpDir);
        } else {
            config.put("tmpDir", tmpDir);
        }
    }

    public static void removeTmpDir() {
        config.remove("tmpDir");
    }

    public static String getImageDir() {
        return config.getProperty("imageDir", imageDir);
    }

    public static void setImageDir(String imageDir) {
        if (config.containsKey("imageDir")) {
            config.replace("imageDir", imageDir);
        } else {
            config.put("imageDir", imageDir);
        }
    }

    public static void removeImageDir() {
        config.remove("imageDir");
    }

    public static int getProcUnitInMaking() {
        return Integer.parseInt(config.getProperty("procUnitInMaking", procUnitInMaking));
    }

    public static void setProcUnitInMaking(int procUnitInMaking) {
        if (config.containsKey("procUnitInMaking")) {
            config.replace("procUnitInMaking", String.valueOf(procUnitInMaking));
        } else {
            config.put("procUnitInMaking", String.valueOf(procUnitInMaking));
        }
    }

    public static void removeProcUnitInMaking() {
        config.remove("procUnitInMaking");
    }

    public static int getProcUnitInGetting() {
        return Integer.parseInt(config.getProperty("procUnitInGetting", procUnitInGetting));
    }

    public static void setProcUnitInGetting(int procUnitInGetting) {
        if (config.containsKey("procUnitInGetting")) {
            config.replace("procUnitInGetting", String.valueOf(procUnitInGetting));
        } else {
            config.put("procUnitInGetting", String.valueOf(procUnitInGetting));
        }
    }

    public static void removeProcUnitInGetting() {
        config.remove("procUnitInGetting");
    }

    public static String getDefaultFeature() {
        return config.getProperty("defaultFeature", defaultFeature);
    }

    public static void setDefaultFeature(String defaultFeature) {
        if (config.containsKey("defaultFeature")) {
            config.replace("defaultFeature", defaultFeature);
        } else {
            config.put("defaultFeature", defaultFeature);
        }
    }

    public static void removeDefaultFeature() {
        config.remove("defaultFeature");
    }

    public static int getPrefetchImagesInFlowPane() {
        return Integer.parseInt(config.getProperty("prefetchImagesInFlowPane", prefetchImagesInFlowPane));
    }

    public static void setPrefetchImagesInFlowPane(int prefetchImagesInFlowPane) {
        if (config.containsKey("prefetchImagesInFlowPane")) {
            config.replace("prefetchImagesInFlowPane", String.valueOf(prefetchImagesInFlowPane));
        } else {
            config.put("prefetchImagesInFlowPane", String.valueOf(prefetchImagesInFlowPane));
        }
    }

    public static void removePrefetchImagesInFlowPane() {
        config.remove("prefetchImagesInFlowPane");
    }

    public static int getPrefetchImagesInStage() {
        return Integer.parseInt(config.getProperty("prefetchImagesInFlowPane", prefetchImagesInStage));
    }

    public static void setPrefetchImagesInStage(int prefetchImagesInStage) {
        if (config.containsKey("prefetchImagesInStage")) {
            config.replace("prefetchImagesInStage", String.valueOf(prefetchImagesInStage));
        } else {
            config.put("prefetchImagesInStage", String.valueOf(prefetchImagesInStage));
        }
    }

    public static void removePrefetchImagesInStage() {
        config.remove("prefetchImagesInStage");
    }

    public static double getAdditionalStageWidthRatio() {
        return Double.parseDouble(config.getProperty("additionalStageWidthRatio", additionalStageWidthRatio));
    }

    public static void setAdditionalStageWidthRatio(double additionalStageWidthRatio) {
        if (config.containsKey("additionalStageWidthRatio")) {
            config.replace("additionalStageWidthRatio", String.valueOf(additionalStageWidthRatio));
        } else {
            config.put("additionalStageWidthRatio", String.valueOf(additionalStageWidthRatio));
        }
    }

    public static void removeAdditionalStageWidthRatio() {
        config.remove("additionalStageWidthRatio");
    }

    public static double getAdditionalStageHeightRatio() {
        return Double.parseDouble(config.getProperty("additionalStageWidthRatio", additionalStageHeightRatio));
    }

    public static void setAdditionalStageHeightRatio(double additionalStageHeightRatio) {
        if (config.containsKey("additionalStageHeightRatio")) {
            config.replace("additionalStageHeightRatio", String.valueOf(additionalStageHeightRatio));
        } else {
            config.put("additionalStageHeightRatio", String.valueOf(additionalStageHeightRatio));
        }
    }

    public static void removeAdditionalStageHeightRatio() {
        config.remove("additionalStageHeightRatio");
    }

    public static String[] getShortCutTabs() {
        String tab = config.getProperty("shortCutTabs", shortCutTabs);
        String[] tabs = null;
        if (!tab.isEmpty()) {
            tabs = tab.split(",");
        }
        return tabs;
    }

    public static void setShortCutTabs(String shortCutTabs) {
        if (config.containsKey("shortCutTabs")) {
            config.replace("shortCutTabs", shortCutTabs);
        } else {
            config.put("shortCutTabs", shortCutTabs);
        }
    }

    public static void removeShortCutTabs() {
        config.remove("shortCutTabs");
    }

    public static String getLastSelectedTab() {
        return config.getProperty("lastSelectedTab", lastSelectedTab);
    }

    public static void setLastSelectedTab(String lastSelectedTab) {
        if (config.containsKey("lastSelectedTab")) {
            config.replace("lastSelectedTab", lastSelectedTab);
        } else {
            config.put("lastSelectedTab", lastSelectedTab);
        }
    }

    public static void removeLastSelectedTab() {
        config.remove("lastSelectedTab");
    }

    public static boolean isSaveImage() {
        return Boolean.parseBoolean(config.getProperty("saveImage", saveImage));
    }

    public static void setSaveImage(boolean saveImage) {
        if (config.containsKey("saveImage")) {
            config.replace("saveImage", String.valueOf(saveImage));
        } else {
            config.put("saveImage", String.valueOf(saveImage));
        }
    }

    public static void removeSaveImage() {
        config.remove("saveImage");
    }

    public static int getSocketTimeout() {
        return Integer.parseInt(config.getProperty("socketTimeout", socketTimeout));
    }

    public static void setSocketTimeout(int socketTimeout) {
        if (config.containsKey("socketTimeout")) {
            config.replace("socketTimeout", String.valueOf(socketTimeout));
        } else {
            config.put("socketTimeout", String.valueOf(socketTimeout));
        }
    }

    public static int getConnectionTimeout() {
        return Integer.parseInt(config.getProperty("connectionTimeout", connectionTimeout));
    }

    public static void setConnectionTimeout(int connectionTimeout) {
        if (config.containsKey("connectionTimeout")) {
            config.replace("connectionTimeout", String.valueOf(connectionTimeout));
        } else {
            config.put("connectionTimeout", String.valueOf(connectionTimeout));
        }
    }

    public static String getUserAgent() {
        return config.getProperty("userAgent", userAgent);
    }

    public static String getCountry() {
        return config.getProperty("country", country);
    }

    public static void setCountry(String country) {
        if (config.containsKey("country")) {
            config.replace("country", country);
        } else {
            config.put("country", country);
        }
    }

    public static void removeCountry() {
        config.remove("country");
    }

    public static String getLang() {
        return config.getProperty("lang", lang);
    }

    public static void setLang(String lang) {
        if (config.containsKey("lang")) {
            config.replace("lang", lang);
        } else {
            config.put("lang", lang);
        }
    }

    public static void removeLang() {
        config.remove("lang");
    }

    public static void clearConfig() {
        config.clear();
    }
}
