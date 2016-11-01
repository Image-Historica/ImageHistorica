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
package com.imagehistorica.dispatcher.impl;

import static com.imagehistorica.util.model.HistoricaType.*;
import static com.imagehistorica.util.Constants.DELIMITER;

import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.cache.TreeCache;
import com.imagehistorica.util.model.HistoricaType;
import com.imagehistorica.databases.model.Historica;

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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class TreeItemDispImpl extends DispImplTreeItem {

    private TreeCache treeCache = TreeCache.getInstance();
    private List<Map<String, Integer>> maplist = new ArrayList<>();
    private List<Map<String, TreeItem<HistoricaProperty>>> treeItems = new ArrayList<>();
    private TreeMap<String, Integer> treeCacheMap = null;
    private Pattern pattern = Pattern.compile(DELIMITER);
    private Matcher matcher;
    private int maxDepth = 0;
    private String rootNodeOfCategory = null;
    private boolean isSkipReq = false;
    private boolean isNewTreeItem = false;

    private final Logger logger = LoggerFactory.getLogger(TreeItemDispImpl.class);

    public TreeItemDispImpl(boolean isNewTreeItem) {
        this.isNewTreeItem = isNewTreeItem;
    }

    @Override
    public void implOpen() {
        treeCacheMap = isNewTreeItem ? treeCache.getNewTreeCacheList() : treeCache.getTreeCacheList();
        if (treeCacheMap == null) {
            isSkipReq = true;
            rootNode = null;
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[implOpen] treeCacheMap: {}", treeCacheMap.firstKey());
            for (Entry<String, Integer> map : treeCacheMap.entrySet()) {
                logger.debug("[implOpen] key: {}", map.getKey());
                logger.debug("[implOpen] value: {}", map.getValue());
            }
        }

        String firstKey = treeCacheMap.firstKey();
        if (firstKey.contains(DELIMITER)) {
            String[] firstEntry = firstKey.split(DELIMITER);
            rootNodeOfCategory = firstEntry[0];
        } else {
            rootNodeOfCategory = firstKey;
        }
        matcher = pattern.matcher(firstKey);

        while (matcher.find()) {
            maxDepth++;
        }
        int listSize = maxDepth + 1;
        while (listSize > 0) {
            maplist.add(new HashMap<>());
            treeItems.add(new HashMap<>());
            listSize--;
        }

        for (Entry<String, Integer> e : treeCacheMap.entrySet()) {
            matcher = pattern.matcher(e.getKey());
            int depth = 0;
            while (matcher.find()) {
                depth++;
            }
            maplist.get(depth).put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void implProcess() {
        if (!isSkipReq) {
            if (logger.isDebugEnabled()) {
                logger.debug("[implProcess] maplist size: {}", maplist.size());
                maplist.stream().forEach((map1) -> {
                    map1.entrySet().stream().forEach((map2) -> {
                        logger.debug("[implProcess] maplist, key: {}, value: {}", map2.getKey(), map2.getValue());
                    });
                });
            }

            boolean deepestBranch = true;
            String[] changedHisDir;
            String hisDirPathPart;
            String hisDirSuffix;
            Map<String, Integer> mapChild_tmp = null;
            for (int i = maxDepth - 1; i > -1; i--) {
                if (deepestBranch) {
                    mapChild_tmp = new HashMap<>(maplist.get(i + 1));
                }
                for (Entry<String, Integer> mapParent : maplist.get(i).entrySet()) {
                    logger.debug("[implProcess] mapParent, key: {}, value: {}", mapParent.getKey(), mapParent.getValue());
                    changedHisDir = Backend.changeHistoricaPath(mapParent.getKey());
                    hisDirPathPart = changedHisDir[0];
                    hisDirSuffix = changedHisDir[1];
                    HistoricaProperty propParent = new HistoricaProperty(hisDirSuffix, hisDirPathPart, mapParent.getValue(), BRANCH, isNewTreeItem);
                    NumberBinding numOfChildren = propParent.getNumOfChildren();
                    TreeItem<HistoricaProperty> branchParent = new TreeItem<>(propParent);

                    if (isNewTreeItem) {
                        branchParent.setExpanded(true);
                        branchParent.getChildren().sort(Comparator.comparing(t -> t.getValue().getType().name().length()));
                        treeCache.putBranchNodeNew(branchParent.getValue().getHistoricaDirId(), branchParent);
                    } else {
                        attachEvents(branchParent);
                        treeCache.putBranchNode(branchParent.getValue().getHistoricaDirId(), branchParent);
                    }
                    treeItems.get(i).put(mapParent.getKey(), branchParent);

                    if (deepestBranch) {
                        for (Entry<String, Integer> mapChild : mapChild_tmp.entrySet()) {
                            if (mapParent.getKey().equals(mapChild.getKey().substring(0, mapChild.getKey().lastIndexOf(DELIMITER)))) {
                                changedHisDir = Backend.changeHistoricaPath(mapChild.getKey());
                                hisDirPathPart = changedHisDir[0];
                                hisDirSuffix = changedHisDir[1];
                                HistoricaProperty propChild = new HistoricaProperty(
                                        hisDirSuffix, hisDirPathPart, mapChild.getValue(), BRANCH_LEAF, isNewTreeItem);
                                TreeItem<HistoricaProperty> branchChild = new TreeItem<>(propChild);

                                if (isNewTreeItem) {
                                    branchChild.setExpanded(true);
                                    branchChild.getChildren().sort(Comparator.comparing(t -> t.getValue().getType().name().length()));
                                    treeCache.putBranchNodeNew(branchChild.getValue().getHistoricaDirId(), branchChild);
                                } else {
                                    treeCache.putBranchNode(branchChild.getValue().getHistoricaDirId(), branchChild);
                                }
                                treeItems.get(i + 1).put(mapChild.getKey(), branchChild);

                                numOfChildren = numOfChildren.add(propChild.getNumOfChildren());
                                propParent.setNumOfChildren(numOfChildren);
                                branchParent.getChildren().add(branchChild);
                            }
                        }
                    } else {
                        for (Entry<String, TreeItem<HistoricaProperty>> mapChild : treeItems.get(i + 1).entrySet()) {
                            if (mapParent.getKey().contentEquals(mapChild.getKey().substring(0, mapChild.getKey().lastIndexOf(DELIMITER)))) {
                                if (isNewTreeItem) {
                                    mapChild.getValue().setExpanded(true);
                                    mapChild.getValue().getChildren().sort(Comparator.comparing(t -> t.getValue().getType().name().length()));
                                }
                                numOfChildren = numOfChildren.add(mapChild.getValue().getValue().getNumOfChildren());
                                propParent.setNumOfChildren(numOfChildren);
                                branchParent.getChildren().add(mapChild.getValue());
                            }
                        }
                    }
                }
                deepestBranch = false;
            }
        }
    }

    @Override
    public void implClose() {
        if (!isSkipReq) {
            for (int i = 0; i < treeItems.size() - 1; i++) {
                logger.debug("[implClose] map1 index: {}, key: {}", i, treeItems.get(i).keySet());
                for (Map.Entry<String, TreeItem<HistoricaProperty>> m : treeItems.get(i).entrySet()) {
                    boolean matched = false;
                    logger.debug("[implClose] map2 index: {}, key: {}", i, treeItems.get(i + 1).keySet());
                    for (Map.Entry<String, TreeItem<HistoricaProperty>> n : treeItems.get(i + 1).entrySet()) {
                        logger.debug("[implClose] check... m:{}, n:{}", m.getKey(), n.getKey());
                        if (n.getKey().contains(m.getKey())) {
                            logger.debug("[implClose] matched... m:{}, n:{}", m.getKey(), n.getKey());
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        m.getValue().getValue().setType(BRANCH_LEAF);
                    }
                }
            }

            logger.debug("[implClose] treeItems.size: {}", treeItems.size());
            treeItems.stream().forEach((map1) -> {
                for (Entry<String, TreeItem<HistoricaProperty>> map2 : map1.entrySet()) {
                    logger.debug("[implClose] key: {}", map2.getKey());
                    logger.debug("[implClose] ImageName: {}", map2.getValue().getValue().getImageName());
                    logger.debug("[implClose] HistoricaPath: {}", map2.getValue().getValue().getHistoricaPath());
                }
            });

            if (treeItems.size() != 1) {
                rootNode = treeItems.get(0).get(rootNodeOfCategory);
            } else {
                int historicaDirId = treeCacheMap.firstEntry().getValue();
                HistoricaProperty rootProp = new HistoricaProperty(
                        rootNodeOfCategory, "", historicaDirId, BRANCH_LEAF, isNewTreeItem);
                rootNode = new TreeItem<>(rootProp);
                List<Historica> historicas = Backend.getHistoricasByHistoricaDirId(historicaDirId);
                if (historicas != null) {
                    historicas.stream().forEach((historica) -> {
                        TreeItem<HistoricaProperty> item = treeCache.getLeafNode(historica.getHistoricaId());
                        if (!(item != null)) {
                            HistoricaProperty prop = createHistoricaProperty(historica, rootNode);
                            TreeItem<HistoricaProperty> treeItem = new TreeItem<>(prop);
                            treeCache.putLeafNode(treeItem.getValue().getHistoricaId(), treeItem);
                            rootNode.getChildren().add(treeItem);
                        }
                    });
                }
                if (isNewTreeItem) {
                    rootNode.setExpanded(true);
                    treeCache.putBranchNodeNew(historicaDirId, rootNode);
                } else {
                    attachEvents(rootNode);
//                    rootNode.setExpanded(true);
                    treeCache.putBranchNode(historicaDirId, rootNode);
                }
            }
        }
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
                List<Historica> historicas = Backend.getHistoricasByHistoricaDirId(historicaDirId);
                if (historicas != null) {
                    historicas.stream().forEach((historica) -> {
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
