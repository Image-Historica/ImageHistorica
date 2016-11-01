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
package com.imagehistorica.common.state;

import com.imagehistorica.Config;
import static com.imagehistorica.util.model.HistoricaType.*;
import static com.imagehistorica.util.Constants.DELIMITER;

import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.util.model.HistoricaType;
import com.imagehistorica.util.view.HistoricaComparator;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.databases.model.Historica;
import com.imagehistorica.databases.model.HistoricaDir;

import javafx.beans.binding.NumberBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class AnalyzeState extends State {

    private TreeCache treeCache = TreeCache.getInstance();
    private ConcurrentMap<Integer, HistoricaDir> historicaDirs;
    private ConcurrentMap<Integer, String> historicaPaths;
    private ConcurrentMap<Integer, String> realImagePaths;
    private ConcurrentMap<Integer, Integer> hisRealPaths;
    private CopyOnWriteArrayList<Historica> historicas;
    private List<Historica> createdHistoricas;
    private Map<Integer, HistoricaProperty> props = new HashMap<>();
    private TreeItem<HistoricaProperty> newTreeItemCopy = null;
    private ConcurrentMap<Integer, TreeItem<HistoricaProperty>> tmpHistoricaDirs = new ConcurrentHashMap<>();
    private AtomicInteger numOfThread;

    // On the way of image analysis.
    private boolean inProgress = false;

    // Interrupted the process.
    private boolean isSuspended = false;

    // Whether created new historicas.
    private boolean isUpdated = false;

    // Whether created new tree items.
    private boolean isCreated = false;

    // Whether merged existing trees and new trees.
    private boolean isMerged = false;

    private final Logger logger = LoggerFactory.getLogger(AnalyzeState.class);

    private final static AnalyzeState state = new AnalyzeState();

    private AnalyzeState() {
        historicaDirs = new ConcurrentHashMap<>();
        historicaPaths = new ConcurrentHashMap<>();
        realImagePaths = new ConcurrentHashMap<>();
        hisRealPaths = new ConcurrentHashMap<>();
        historicas = new CopyOnWriteArrayList<>();
        createdHistoricas = new ArrayList<>();
    }

    public static AnalyzeState getInstance() {
        return state;
    }

    public Map<Integer, HistoricaProperty> getProps() {
        return this.props;
    }

    public void putAllHistoricaDirs(List<HistoricaDir> historicaDirs) {
        for (HistoricaDir historicaDir : historicaDirs) {
            this.historicaDirs.putIfAbsent(historicaDir.getHistoricaDirId(), historicaDir);
        }
    }

    public void putRealImagePaths(ConcurrentMap<Integer, String> realImagePaths) {
        this.realImagePaths.putAll(realImagePaths);
    }

    public String getRealImagePath(int historicaId) {
        return this.realImagePaths.getOrDefault(historicaId, null);
    }

    public void putHistoricaPaths(ConcurrentMap<Integer, String> historicaPaths) {
        this.historicaPaths.putAll(historicaPaths);
    }

    public String getHistoricaPath(int historicaId) {
        return this.historicaPaths.getOrDefault(historicaId, null);
    }

    public void addHistoricas(List<Historica> historicas) {
        this.historicas.addAll(historicas);
    }

    public CopyOnWriteArrayList<Historica> getHistoricas() {
        return this.historicas;
    }

    public void addCreatedHistoricas(List<Historica> historicas) {
        this.createdHistoricas.addAll(historicas);
    }

    public List<Historica> getCreatedHistoricas() {
        return this.createdHistoricas;
    }

    public void decreNumOfThread() {
        numOfThread.decrementAndGet();
    }

    public boolean inProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public void setSuspended(boolean isSuspended) {
        this.isSuspended = isSuspended;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean isUpdated) {
        this.isUpdated = isUpdated;
    }

    public boolean isCreated() {
        return isCreated;
    }

    public void setCreated(boolean isCreated) {
        this.isCreated = isCreated;
    }

    public boolean isMerged() {
        return isMerged;
    }

    public void setMerged(boolean isMerged) {
        this.isMerged = isMerged;
    }

    public Map<Integer, HistoricaProperty> createLeaves() {
        logger.debug("Called createdLeaves()...1");
        if (historicas != null && !historicas.isEmpty()) {
            logger.debug("Called createdLeaves()...2");
            props = new HashMap<>();
            createdHistoricas = new ArrayList<>();
            for (Historica historica : historicas) {
                HistoricaProperty prop = Backend.makeHistoricaProperty(historica, LEAF);
                props.putIfAbsent(historica.getHistoricaId(), prop);
                createdHistoricas.add(historica);
            }

            historicaPaths.clear();
            realImagePaths.clear();
            historicas.clear();
        }

        return props;
    }

    public TreeItem<HistoricaProperty> requestNewTreeItem() {
        logger.debug("Called requestNewTreeItem()...1");
        if (isUpdated) {
            logger.debug("Called requestNewTreeItem()...2");

            Map<Integer, String> hisDirsMap = new HashMap<>();
            Set<String> historicaPaths = new HashSet<>();

            logger.debug("props size: {}", props.size());
            for (Entry<Integer, HistoricaProperty> prop : props.entrySet()) {
                String branches[] = prop.getValue().getHistoricaPath().split(DELIMITER);
                for (int i = branches.length; i > 0; i--) {
                    String newPath = "";
                    for (int j = 0; j < i; j++) {
                        newPath = newPath + branches[j] + DELIMITER;
                    }
                    newPath = newPath.substring(0, newPath.lastIndexOf(DELIMITER));
                    historicaPaths.add(newPath);
                }
            }

            for (String historicaPath : historicaPaths) {
                String[] changedHisDir = Backend.changeHistoricaPath(historicaPath);
                String hisDirPathPart = changedHisDir[0];
                String hisDirSuffix = changedHisDir[1];
                int historicaDirId = Backend.getHistoricaDirId(hisDirSuffix, hisDirPathPart);
                hisDirsMap.putIfAbsent(historicaDirId, historicaPath);
            }

            inProgress = true;
            isCreated = false;
            numOfThread = new AtomicInteger(Config.getNumOfThreads());

            treeCache.createNewTreeItem(hisDirsMap);
            while (true) {
                logger.debug("Waiting for creating new tree items...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (numOfThread.get() == 0) {
                    break;
                }
            }
            addLeavesToBranch();
            isUpdated = false;
            inProgress = false;
        }

        return treeCache.getNewTreeItem();
    }

    public void addLeavesToBranch() {
        for (Entry<Integer, HistoricaProperty> prop : props.entrySet()) {
            TreeItem<HistoricaProperty> treeItem = treeCache.getBranchNodeNew(prop.getValue().getHistoricaDirId());
            if (treeItem != null) {
                logger.debug("Matched...{}", treeItem.getValue().getHistoricaPath());
                logger.debug("Matched...{}", treeItem.getValue().getImageName());
                TreeItem<HistoricaProperty> newLeaf = treeCache.getLeafNodeNew(prop.getKey());
                if (newLeaf == null) {
                    logger.debug("newLeaf null...");
                    newLeaf = new TreeItem<>(prop.getValue());
                    treeCache.putLeafNodeNew(prop.getKey(), newLeaf);
                } else {
                    logger.debug("newLeaf exists...");
                }
                treeItem.getChildren().add(newLeaf);
            } else {
                logger.debug("Not matched...");
            }
        }
    }

    public void mergeTreeItem() {
        if (!isMerged) {
            if (!historicaDirs.isEmpty()) {
                List<TreeMap<String, Integer>> paths = sortHistoricaPaths(historicaDirs);
                Set<Integer> existingDirs = mergeBranches(paths);
                treeCache.getRootNode().getChildren().sort(new HistoricaComparator());
                ConcurrentMap<Integer, TreeItem<HistoricaProperty>> branches = treeCache.getBranchNodesNew();
                ConcurrentMap<Integer, TreeItem<HistoricaProperty>> leaves = treeCache.getLeafNodesNew();

                for (Entry<Integer, TreeItem<HistoricaProperty>> e : leaves.entrySet()) {
                    int historicaDirId = e.getValue().getValue().getHistoricaDirId();
                    if (!existingDirs.contains(historicaDirId)) {
                        logger.debug("Already mergeed...{}", historicaDirId);
                        continue;
                    } else {
                        logger.debug("Not merged...{}", historicaDirId);
                    }
                    TreeItem<HistoricaProperty> mainParent = treeCache.getBranchNode(historicaDirId);
                    if (mainParent != null) {
                        logger.debug("mainParent id: {}", mainParent.getValue().getHistoricaDirId());
                        logger.debug("mainParent path: {}", mainParent.getValue().getHistoricaPath());
                        logger.debug("mainParent name: {}", mainParent.getValue().getImageName());
                        mainParent.getChildren().add(e.getValue());
                    } else {
                        logger.debug("mainParent is null...");
                    }
                }

                branches.entrySet().stream().forEach((e) -> {
                    treeCache.putBranchNode(e.getKey(), e.getValue());
                });
                leaves.entrySet().stream().forEach((e) -> {
                    treeCache.putLeafNode(e.getKey(), e.getValue());
                });

                branches.clear();
                leaves.clear();

                historicaDirs.clear();
                isMerged = true;
            }
        }
    }

    public Set<Integer> mergeBranches(List<TreeMap<String, Integer>> paths) {
        Set<Integer> existingDirs = new HashSet<>();
        for (TreeMap<String, Integer> path : paths) {
            for (Entry<String, Integer> e : path.entrySet()) {
                logger.debug("mergeTreeItem()_1 historicaPath: {}", e.getKey());
                String[] historicaPaths = e.getKey().split(DELIMITER);
                TreeItem<HistoricaProperty> tmpNode = treeCache.getRootNode();
                for (int i = 0; i < historicaPaths.length; i++) {
                    boolean found = false;
                    for (TreeItem<HistoricaProperty> treeItem : tmpNode.getChildren()) {
                        logger.debug("mergeTreeItem()_2...in for loop...");
                        if (treeItem.getValue().getImageName().equals(historicaPaths[i])) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("mergeTreeItem()_3...found...{}", treeItem.getValue().getImageName());
                                treeItem.getChildren().stream().forEach((b) -> {
                                    logger.debug("mergeTreeItem()_3 child: {}", b.getValue().getImageName());
                                });
                            }

                            if (treeCache.getBranchNode(e.getValue()) != null) {
                                existingDirs.add(e.getValue());
                            }

                            tmpNode = treeItem;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        try {
                            TreeItem<HistoricaProperty> branchNode = treeCache.getBranchNodeNew(e.getValue());
                            if (logger.isDebugEnabled()) {
                                logger.debug("mergeTreeItem()_4 branchNode name: {}", branchNode.getValue().getImageName());
                                branchNode.getChildren().stream().forEach((b) -> {
                                    logger.debug("mergeTreeItem()_5 child: {}", b.getValue().getImageName());
                                });
                            }
                            attachEvents(branchNode);
                            tmpNode.getChildren().add(branchNode);
                            TreeItem<HistoricaProperty> parentItem = branchNode.getParent();
                            while (parentItem != null) {
                                logger.debug("mergeTreeItem()_6 parentItem name: {}", parentItem.getValue().getImageName());
                                List<NumberBinding> numOfChildrens = new ArrayList<>();
                                for (TreeItem<HistoricaProperty> child : parentItem.getChildren()) {
                                    if (child.getValue().getType() != HistoricaType.LEAF) {
                                        numOfChildrens.add(child.getValue().getNumOfChildren());
                                    }
                                }

                                NumberBinding numOfChildren = parentItem.getValue().getNumOfLeaves().add(0);;
                                for (NumberBinding numBinding : numOfChildrens) {
                                    numOfChildren = numOfChildren.add(numBinding);
                                }
                                parentItem.getValue().setNumOfChildren(numOfChildren);
                                parentItem = parentItem.getParent();
                            }
                        } catch (NullPointerException ex) {
                            logger.error("{}", ex.getMessage());
                            logger.error("AnalyzeState key: {}", e.getKey());
                            logger.error("AnalyzeState valie: {}", e.getValue());
                        }
                    }
                }
            }
        }
        return existingDirs;
    }

    public List<TreeMap<String, Integer>> sortHistoricaPaths(Map<Integer, HistoricaDir> historicaDirs) {
        List<String> firstBranches = new ArrayList<>();
        List<TreeMap<String, Integer>> updateHistoricaPaths = new ArrayList<>();
        for (Entry<Integer, HistoricaDir> map : historicaDirs.entrySet()) {
            int historicaDirId = map.getValue().getHistoricaDirId();
            String historicaPath = map.getValue().getHisDirPathPart() + map.getValue().getHisDirSuffix();
            String firstBranch = historicaPath;
            if (firstBranch.contains(DELIMITER)) {
                firstBranch = firstBranch.substring(0, firstBranch.indexOf(DELIMITER));
            }
            int index = firstBranches.indexOf(firstBranch);
            if (index < 0) {
                firstBranches.add(firstBranch);
                index = firstBranches.indexOf(firstBranch);
                updateHistoricaPaths.add(index, new TreeMap<>((String o1, String o2) -> {
                    int length1 = o1.split(DELIMITER).length;
                    int length2 = o2.split(DELIMITER).length;

                    if (length1 < length2) {
                        return -1;
                    } else {
                        return 1;
                    }
                }));
            }
            updateHistoricaPaths.get(index).put(historicaPath, historicaDirId);
        }

        return updateHistoricaPaths;
    }

    private HistoricaProperty createHistoricaProperty(Historica historica, TreeItem<HistoricaProperty> treeItem) {
        LocalDate date = null;
        try {
            String lmod = String.valueOf(historica.getLmod());
            date = lmod.matches("\\d{8}") ? LocalDate.parse(lmod, DateTimeFormatter.BASIC_ISO_DATE) : LocalDate.of(1999, 01, 01);
        } catch (DateTimeParseException e) {
            date = LocalDate.of(1999, 01, 01);
        }

        String url = historica.getLocationId() != 0 ? historica.getSrcUrl() : "unknown";

        HistoricaProperty prop = new HistoricaProperty(
                historica.getHistoricaId(),
                historica.getHistoricaDirId(),
                historica.getImageName(),
                Backend.getRealImage(historica.getRealImageId()).getRealImagePath0(),
                treeItem.getValue().getHistoricaPath() + treeItem.getValue().getImageName(),
                date,
                (int) historica.getFreqViewed(),
                historica.getLength(),
                url,
                LEAF);

        return prop;
    }

    @SuppressWarnings("unchecked")
    private void attachEvents(TreeItem<HistoricaProperty> branchParent) {
        branchParent.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldVal, Boolean newVal) -> {
            if (newVal) {
                BooleanProperty bp = (BooleanProperty) observable;
                TreeItem<HistoricaProperty> sourceItem = (TreeItem<HistoricaProperty>) bp.getBean();
                ObservableList<TreeItem<HistoricaProperty>> branchLeaves = FXCollections.<TreeItem<HistoricaProperty>>observableArrayList();
                sourceItem.getChildren().stream().filter((treeItem) -> (treeItem.getValue().getType() == HistoricaType.BRANCH_LEAF)).forEach((treeItem) -> {
                    branchLeaves.add(treeItem);
                });
                int historicaDirId = sourceItem.getValue().getHistoricaDirId();
                List<Historica> historicas1 = Backend.getHistoricasByHistoricaDirId(historicaDirId);
                if (historicas1 != null) {
                    historicas1.stream().forEach((historica) -> {
                        TreeItem<HistoricaProperty> item = treeCache.getLeafNode(historica.getHistoricaId());
                        if (!(item != null)) {
                            HistoricaProperty prop = createHistoricaProperty(historica, branchParent);
                            TreeItem<HistoricaProperty> treeItem = new TreeItem<>(prop);
                            treeCache.putLeafNode(treeItem.getValue().getHistoricaId(), treeItem);
                            sourceItem.getChildren().add(treeItem);
                        }
                    });
                }
                sourceItem.getChildren().sort(Comparator.comparing(t -> t.getValue().getType().name().length()));
                for (TreeItem<HistoricaProperty> branchLeaf : branchLeaves) {
                    historicaDirId = branchLeaf.getValue().getHistoricaDirId();
                    List<Historica> historicasOfLeaf = Backend.getHistoricasByHistoricaDirId(historicaDirId);
                    if (historicasOfLeaf != null) {
                        historicasOfLeaf.stream().forEach((historica) -> {
                            TreeItem<HistoricaProperty> item = treeCache.getLeafNode(historica.getHistoricaId());
                            if (!(item != null)) {
                                HistoricaProperty propOfLeaf = createHistoricaProperty(historica, branchLeaf);
                                TreeItem<HistoricaProperty> treeItem = new TreeItem<>(propOfLeaf);
                                treeCache.putLeafNode(treeItem.getValue().getHistoricaId(), treeItem);
                                branchLeaf.getChildren().add(treeItem);
                            }
                        });
                    }
                    branchLeaf.getChildren().sort(Comparator.comparing(t -> t.getValue().getType().name().length()));
                }
            }
        });
    }
}
