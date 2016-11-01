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
package com.imagehistorica.cache;

import static com.imagehistorica.util.Constants.DELIMITER;
import static com.imagehistorica.util.model.HistoricaType.ROOT;

import com.imagehistorica.util.view.HistoricaComparator;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.util.controller.ThreadCreator;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.databases.model.HistoricaDir;

import javafx.beans.binding.NumberBinding;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class TreeCache {

    private ConcurrentMap<Integer, Integer> historicaNumsMap = null;
    private ConcurrentMap<Integer, Integer> historicaMeaningsMap = null;
    private ConcurrentMap<Integer, Integer> historicaNumsMapNew = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, String> historicaDirsMap = null;
    private List<TreeMap<String, Integer>> treeCacheList = null;
    private List<TreeMap<String, Integer>> newTreeCacheList = null;
    private TreeItem<HistoricaProperty> rootNode = null;
    private TreeItem<HistoricaProperty> newTreeItem = null;
    private final ConcurrentMap<Integer, TreeItem<HistoricaProperty>> branchNodes = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, TreeItem<HistoricaProperty>> branchNodesNew = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, TreeItem<HistoricaProperty>> leafNodes = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, TreeItem<HistoricaProperty>> leafNodesNew = new ConcurrentHashMap<>();
    private AtomicInteger treeCacheListSize;
    private AtomicInteger newTreeCacheListSize;
    private final Object mutex = new Object();

    private final Logger logger = LoggerFactory.getLogger(TreeCache.class);
    private static TreeCache treeCache = new TreeCache();

    private TreeCache() {
    }

    public static TreeCache getInstance() {
        return treeCache;
    }

    public void startInitialSetup() {
        if (rootNode == null) {
            createRootNode();
        }
    }

    public void createRootNode() {
        logger.debug("Start createRootNode()...");
        rootNode = new TreeItem<>(new HistoricaProperty("ImageHistorica", "", 0, ROOT, false));
        putBranchNode(0, rootNode);

        if (historicaNumsMap.isEmpty() || historicaDirsMap.isEmpty() || historicaMeaningsMap.isEmpty()) {
            logger.debug("HistoricaDirCache is empty...");
            List<HistoricaDir> historicaDirs = Backend.getHistoricaDirs();
            int mapSize = historicaDirs.size();
            historicaNumsMap = new ConcurrentHashMap<>(mapSize * 4 / 3);
            historicaDirsMap = new ConcurrentHashMap<>(mapSize * 4 / 3);
            historicaMeaningsMap = new ConcurrentHashMap<>(mapSize * 4 / 3);
            for (HistoricaDir historicaDir : historicaDirs) {
                int historicaDirId = historicaDir.getHistoricaDirId();
                historicaNumsMap.put(historicaDirId, Backend.count(historicaDirId));
                historicaDirsMap.put(historicaDirId, historicaDir.getHisDirPathPart() + historicaDir.getHisDirSuffix());
                if (historicaDir.getMeaningId() != 0) {
                    historicaMeaningsMap.put(historicaDirId, historicaDir.getMeaningId());
                }
            }
        }

        if (logger.isDebugEnabled()) {
            historicaDirsMap.entrySet().stream().forEach((m) -> {
                logger.debug("historicaDirsMap: {} : {}", m.getKey(), m.getValue());
            });
        }

        logger.debug("Create treeCacheList...");
        treeCacheList = new ArrayList<>();
        List<String> firstBranches = new ArrayList<>();
        for (Entry<Integer, String> map : historicaDirsMap.entrySet()) {
            String firstBranch = map.getValue();
            if (firstBranch.contains(DELIMITER)) {
                firstBranch = map.getValue().substring(0, map.getValue().indexOf(DELIMITER));
            }
            int index = firstBranches.indexOf(firstBranch);
            if (index < 0) {
                firstBranches.add(firstBranch);
                index = firstBranches.indexOf(firstBranch);
                treeCacheList.add(index, new TreeMap<>((String o1, String o2) -> {
                    int length1 = o1.split(DELIMITER).length;
                    int length2 = o2.split(DELIMITER).length;
                    return length1 < length2 ? 1 : -1;
                }));
            }
            treeCacheList.get(index).put(map.getValue(), map.getKey());
        }
        treeCacheListSize = new AtomicInteger(treeCacheList.size());

        ThreadCreator t = new ThreadCreator();
        t.createTreeItem();
    }

    public void createNewTreeItem(Map<Integer, String> hisDirsMap) {
        newTreeItem = new TreeItem<>(new HistoricaProperty("ImageHistorica", "", 0, ROOT, false));
        newTreeCacheList = new ArrayList<>();
        List<String> firstBranches = new ArrayList<>();
        for (Entry<Integer, String> e : hisDirsMap.entrySet()) {
            logger.debug("Called createNewTreeItem()... hisDirsMap: {}:{}", e.getKey(), e.getValue());
            String firstBranch = e.getValue();
            if (firstBranch.contains(DELIMITER)) {
                firstBranch = e.getValue().substring(0, e.getValue().indexOf(DELIMITER));
            }

            int index = firstBranches.indexOf(firstBranch);
            if (index < 0) {
                logger.debug("Called createNewTreeItem()... index: {}, new firstBranch: {}:{}", index, e.getKey(), e.getValue());
                firstBranches.add(firstBranch);
                index = firstBranches.indexOf(firstBranch);
                newTreeCacheList.add(index, new TreeMap<>((String o1, String o2) -> {
                    int length1 = o1.split(DELIMITER).length;
                    int length2 = o2.split(DELIMITER).length;
                    return length1 < length2 ? 1 : -1;
                }));
            }
            newTreeCacheList.get(index).put(e.getValue(), e.getKey());
        }
        newTreeCacheListSize = new AtomicInteger(newTreeCacheList.size());

        if (logger.isDebugEnabled()) {
            newTreeCacheList.stream().forEach((map) -> {
                map.entrySet().stream().forEach((e) -> {
                    logger.debug("Called createNewTreeItem()... treeCacheList: {}:{}", e.getKey(), e.getValue());
                });
            });
        }

        ThreadCreator t = new ThreadCreator();
        t.createNewTreeItem();

        hisDirsMap = null;
    }

    public int getHistoricaNumsMap(int historicaDirId) {
        return historicaNumsMap.getOrDefault(historicaDirId, 0);
    }

    public void putHistoricaNumsMap(int historicaDirId, int value) {
        this.historicaNumsMap.put(historicaDirId, value);
    }

    public void removeHistoricaNumsMap(int historicaDirId) {
        this.historicaNumsMap.remove(historicaDirId);
    }

    public void clearHistoricaNumsMap() {
        this.historicaNumsMap.clear();
    }

    public void increHistoricaNumsMap(int historicaDirId, int addition) {
        if (historicaNumsMap.containsKey(historicaDirId)) {
            this.historicaNumsMap.replace(historicaDirId, (historicaNumsMap.get(historicaDirId) + addition));
        }
    }

    public void decreHistoricaNumsMap(int historicaDirId, int deduction) {
        if (historicaNumsMap.containsKey(historicaDirId)) {
            this.historicaNumsMap.replace(historicaDirId, (historicaNumsMap.get(historicaDirId) - deduction));
        }
    }

    public int getHistoricaNumsMapNew(int historicaDirId) {
        return historicaNumsMapNew.getOrDefault(historicaDirId, 0);
    }

    public void putHistoricaNumsMapNew(int historicaDirId, int value) {
        this.historicaNumsMapNew.put(historicaDirId, value);
    }

    public void clearHistoricaNumsMapNew() {
        this.historicaNumsMapNew.clear();
    }

    public int getHistoricaMeaningsMap(int historicaDirId) {
        return historicaMeaningsMap.getOrDefault(historicaDirId, 0);
    }

    public void putHistoricaMeaningsMap(int historicaDirId, int meaningId) {
        this.historicaMeaningsMap.put(historicaDirId, meaningId);
    }

    public void removeHistoricaMeaningsMap(int historicaDirId) {
        this.historicaMeaningsMap.remove(historicaDirId);
    }

    public void clearHistoricaMeaningsMap() {
        this.historicaMeaningsMap.clear();
    }

    public String getHistoricaDirsMap(int historicaDirId) {
        return historicaDirsMap.getOrDefault(historicaDirId, null);
    }

    public void putHistoricaDirsMap(int historicaDirId, String historicaPath) {
        this.historicaDirsMap.put(historicaDirId, historicaPath);
    }

    public void putHistoricaDirsMap(Map<Integer, String> historicaDirsMap) {
        this.historicaDirsMap.putAll(historicaDirsMap);
    }

    public void replaceHistoricaDirsMap(int historicaDirId, String historicaPath) {
        this.historicaDirsMap.replace(historicaDirId, historicaPath);
    }

    public void removeHistoricaDirsMap(int historicaDirId) {
        this.historicaDirsMap.remove(historicaDirId);
    }

    public void clearHistoricaDirsMap() {
        this.historicaDirsMap.clear();
    }

    public TreeItem<HistoricaProperty> getBranchNode(int historicaDirId) {
        return branchNodes.getOrDefault(historicaDirId, null);
    }

    public void putBranchNode(int historicaDirId, TreeItem<HistoricaProperty> treeItem) {
        synchronized (mutex) {
            branchNodes.putIfAbsent(historicaDirId, treeItem);
        }
    }

    public void removeBranchNode(int historicaDirId) {
        branchNodes.remove(historicaDirId);
    }

    public void clearBranchNode() {
        branchNodes.clear();
    }

    public TreeItem<HistoricaProperty> getBranchNodeNew(int historicaDirId) {
        return branchNodesNew.getOrDefault(historicaDirId, null);
    }

    public ConcurrentMap<Integer, TreeItem<HistoricaProperty>> getBranchNodesNew() {
        return this.branchNodesNew;
    }

    public void putBranchNodeNew(int historicaDirId, TreeItem<HistoricaProperty> treeItem) {
        synchronized (mutex) {
            branchNodesNew.putIfAbsent(historicaDirId, treeItem);
        }
    }

    public void removeBranchNodeNew(int historicaDirId) {
        branchNodesNew.remove(historicaDirId);
    }

    public void clearBranchNodesNew() {
        branchNodesNew.clear();
    }

    public TreeItem<HistoricaProperty> getLeafNode(int historicaId) {
        return leafNodes.getOrDefault(historicaId, null);
    }

    public void putLeafNode(int historicaId, TreeItem<HistoricaProperty> treeItem) {
        synchronized (mutex) {
            leafNodes.putIfAbsent(historicaId, treeItem);
        }
    }

    public void replaceLeafNode(int historicaId, TreeItem<HistoricaProperty> treeItem) {
        synchronized (mutex) {
            leafNodes.replace(historicaId, treeItem);
        }
    }

    public void removeLeafNode(int historicaId) {
        leafNodes.remove(historicaId);
    }

    public void clearLeafNode() {
        leafNodes.clear();
    }

    public TreeItem<HistoricaProperty> getLeafNodeNew(int historicaId) {
        return leafNodesNew.getOrDefault(historicaId, null);
    }

    public ConcurrentMap<Integer, TreeItem<HistoricaProperty>> getLeafNodesNew() {
        return this.leafNodesNew;
    }

    public void putLeafNodeNew(int historicaId, TreeItem<HistoricaProperty> treeItem) {
        synchronized (mutex) {
            leafNodesNew.putIfAbsent(historicaId, treeItem);
        }
    }

    public void removeLeafNodeNew(int historicaId) {
        leafNodesNew.remove(historicaId);
    }

    public void clearLeafNodesNew() {
        leafNodesNew.clear();
    }

    public TreeMap<String, Integer> getTreeCacheList() {
        synchronized (mutex) {
//            int index = treeCacheListSize.getAndDecrement() - 1;
            int index = treeCacheListSize.decrementAndGet();
            return index > -1 ? treeCacheList.get(index) : null;
        }
    }

    public int getTreeCacheListSize() {
        synchronized (mutex) {
            int size = treeCacheListSize.get();
            if (size == 0) {
                treeCacheList = null;
            }
            return size;
        }
    }

    public TreeMap<String, Integer> getNewTreeCacheList() {
        synchronized (mutex) {
//            int index = newTreeCacheListSize.getAndDecrement() - 1;
            int index = newTreeCacheListSize.decrementAndGet();
            return index > -1 ? newTreeCacheList.get(index) : null;
        }
    }

    public int getNewTreeCacheListSize() {
        synchronized (mutex) {
            int size = newTreeCacheListSize.get();
            if (size == 0) {
                newTreeCacheList = null;
            }
            return size;
        }
    }

    public TreeItem<HistoricaProperty> getRootNode() {
        synchronized (mutex) {
            return rootNode;
        }
    }

    public void setRootNode(List<TreeItem<HistoricaProperty>> branchNodes) {
        synchronized (mutex) {
            branchNodes.stream().forEach((branchNode) -> {
                NumberBinding numOfChildren = rootNode.getValue().getNumOfChildren().add(branchNode.getValue().getNumOfChildren());
                rootNode.getValue().setNumOfChildren(numOfChildren);
                rootNode.getChildren().add(branchNode);
            });
            rootNode.getChildren().sort(new HistoricaComparator());
        }
    }

    public TreeItem<HistoricaProperty> getNewTreeItem() {
        synchronized (mutex) {
            return newTreeItem;
        }
    }

    public void setNewTreeItem(List<TreeItem<HistoricaProperty>> newBranchNodes) {
        synchronized (mutex) {
            if (newBranchNodes == null) {
                newTreeItem = null;
                return;
            }
            newBranchNodes.stream().forEach((newBranchNode) -> {
                NumberBinding numOfChildren = newTreeItem.getValue().getNumOfChildren().add(newBranchNode.getValue().getNumOfChildren());
                newTreeItem.getValue().setNumOfChildren(numOfChildren);
                newTreeItem.getChildren().add(newBranchNode);
            });
            newTreeItem.getChildren().sort(new HistoricaComparator());
        }
    }

    public void updateNumOfImages(int historicaDirId) {
        synchronized (mutex) {
            TreeItem<HistoricaProperty> treeItem = branchNodes.getOrDefault(historicaDirId, null);
            if (treeItem != null) {
                treeItem.getValue().getNumOfLeaves().set(getHistoricaNumsMap(treeItem.getValue().getHistoricaDirId()));
            }
        }
    }

    public ConcurrentMap<Integer, Integer> backupHistoricaNumsMap() {
        return historicaNumsMap;
    }

    public void restoreHistoricaNumsMap(ConcurrentMap<Integer, Integer> historicaNumMap) {
        this.historicaNumsMap = historicaNumMap;
    }

    public ConcurrentMap<Integer, Integer> backupHistoricaMeaningsMap() {
        return historicaMeaningsMap;
    }

    public void restoreHistoricaMeaningsMap(ConcurrentMap<Integer, Integer> historicaMeaningsMap) {
        this.historicaMeaningsMap = historicaMeaningsMap;
    }

    public ConcurrentMap<Integer, String> backupHistoricaDirsMap() {
        return historicaDirsMap;
    }

    public void restoreHistoricaDirsMap(ConcurrentMap<Integer, String> historicaDirsMap) {
        this.historicaDirsMap = historicaDirsMap;
    }

    public Map<Integer, TreeItem<HistoricaProperty>> getBranchNodes() {
        return this.branchNodes;
    }

    public Map<Integer, TreeItem<HistoricaProperty>> getLeafNodes() {
        return this.leafNodes;
    }
}
