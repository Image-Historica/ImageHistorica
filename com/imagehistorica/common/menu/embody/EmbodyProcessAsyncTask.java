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

import com.imagehistorica.cache.TreeCache;
import static com.imagehistorica.common.menu.embody.EmbodyHistoricaType.*;

import com.imagehistorica.common.state.EmbodyState;
import com.imagehistorica.util.model.HistoricaProperty;
import static com.imagehistorica.util.model.SchemeType.FILE;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.Backend;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.TreeItem;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class EmbodyProcessAsyncTask {

    private final TreeCache treeCache = TreeCache.getInstance();
    private final EmbodyState status;
    private final File dstDirectory;
    private final Map<String, String> paths;
    private final Map<String, Integer> historicaIds;
    private final EmbodyHistoricaType type;
    private final boolean pathIncluding;
    private final String fileSeparator;

    private Path srcPath;
    private Path dstPath;
    private String ex;

    private final Logger logger = LoggerFactory.getLogger(EmbodyProcessAsyncTask.class);

    public EmbodyProcessAsyncTask(EmbodyState status, File dstDirectory, Map<String, String> paths,
            Map<String, Integer> historicaIds, EmbodyHistoricaType type, boolean pathIncluding, String fileSeparator) {
        this.status = status;
        this.dstDirectory = dstDirectory;
        this.paths = paths;
        this.historicaIds = historicaIds;
        this.type = type;
        this.pathIncluding = pathIncluding;
        if (fileSeparator.equals("\\\\")) {
            fileSeparator = "\\";
        }
        this.fileSeparator = fileSeparator;
    }

    public void embody() {
        logger.debug("Start EmbodyProcessAsyncTask...");
        int i = 1;
        for (Entry<String, String> path : paths.entrySet()) {
            logger.debug("embody()... {} times", i);
            boolean isSucceeded = false;
            srcPath = null;
            dstPath = null;
            ex = null;

            while (status.isInterrupted()) {
                logger.debug("Waiting due to interrupted...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    logger.info("", ex);
                }
            }

            if (status.isCanceled()) {
                logger.debug("Cancelled...");
                break;
            }

            String src = path.getKey();
            String dst = dstDirectory + fileSeparator + path.getValue();
            String ext = dst.substring(dst.lastIndexOf(".") + 1);
            if (type == COPY_HISTORICA_ONLY || (type == COPY_HISTORICA_WITH_PATH)) {
                if (checkPreConditions(src, dst, ext)) {
                    if (copyHistoricaOnly(src)) {
                        isSucceeded = true;
                    }
                }
            } else if (type == MOVE_HISTORICA_ONLY || (type == MOVE_HISTORICA_WITH_PATH)) {
                if (checkPreConditions(src, dst, ext)) {
                    if (moveHistoricaOnly(src)) {
                        isSucceeded = true;
                    }
                }
            }

            if (isSucceeded) {
                String[] paths = {String.valueOf(i), srcPath.toString(), dstPath.toString()};
                status.offerPathQueue(paths);
            } else {
                if (srcPath == null || dstPath == null) {
                    String[] paths = {String.valueOf(i), src, dst, ex};
                    status.offerExQueue(paths);
                } else {
                    String[] paths = {String.valueOf(i), srcPath.toString(), dstPath.toString(), ex};
                    status.offerExQueue(paths);
                }
            }

            i++;
        }
        logger.debug("End EmbodyProcessAsyncTask...{} times", i);
    }

    private boolean copyHistoricaOnly(String src) {
        logger.debug("Start copyHistoricaOnly()...");
        try {
            Files.copy(srcPath, dstPath, StandardCopyOption.COPY_ATTRIBUTES);
            if (Files.exists(dstPath)) {
                logger.debug("dstPath to URI: {}", dstPath.toUri());
                int dstRealImageId = Backend.getNewImageId(dstPath.toUri(), null, FILE);
                Integer historicaId = historicaIds.get(src);
                if (dstRealImageId == -1 || historicaId == null) {
                    String exception = "Could not get realImageId or historicaId for the path...src: " + src + ", dst: " + dstPath;
                    ex = exception;
                    return false;
                }
                Backend.changeImageIdOfHistorica(historicaId, dstRealImageId);
                TreeItem<HistoricaProperty> prop = treeCache.getLeafNode(historicaId);
                prop.getValue().setRealImagePath(dstPath.toUri().toString());
                treeCache.replaceLeafNode(historicaId, prop);

                int srcRealImageId = Backend.getRealImageId(srcPath.toUri());
                if (srcRealImageId != -1) {
                    Backend.deleteRealImage(srcRealImageId);
                    logger.debug("Change realImageId of historicaId: {} ... from realImageId: {}, to realImageId: {}", historicaId, srcRealImageId, dstRealImageId);
                }

                return true;
            }
        } catch (NoSuchFileException e) {
            String exception
                    = Rsc.get("common_menu_EPAT_noFileEx_1")
                    + Rsc.get("common_menu_EPAT_noFileEx_2")
                    + ", " + srcPath + "\n"
                    + Rsc.get("common_menu_EPAT_noFileEx_3")
                    + ", " + dstPath + "\n\n";
            ex = exception;
        } catch (FileAlreadyExistsException e) {
            logger.debug("FileAlreadyExistsException...{}", e.getMessage());
        } catch (FileSystemException e) {
            ex = e.getMessage();
        } catch (IOException e) {
            ex = Rsc.get("common_menu_EPAT_ioEx_1");
        } catch (Exception e) {
            logger.error("", e);
            ex = e.getMessage();
        }
        return false;
    }

    private boolean moveHistoricaOnly(String src) {
        logger.debug("Start moveHistoricaOnly()...");
        try {
            Files.move(srcPath, dstPath, StandardCopyOption.ATOMIC_MOVE);
            if (Files.exists(dstPath)) {
                logger.debug("dstPath to URI: {}", dstPath.toUri());
                int dstRealImageId = Backend.getNewImageId(dstPath.toUri(), null, FILE);
                Integer historicaId = historicaIds.get(src);
                if (dstRealImageId == -1 || historicaId == null) {
                    String exception = "Could not get realImageId or historicaId for the path...src: " + src + ", dst: " + dstPath;
                    ex = exception;
                    return false;
                }
                Backend.changeImageIdOfHistorica(historicaId, dstRealImageId);
                TreeItem<HistoricaProperty> prop = treeCache.getLeafNode(historicaId);
                prop.getValue().setRealImagePath(dstPath.toUri().toString());
                treeCache.replaceLeafNode(historicaId, prop);

                int srcRealImageId = Backend.getRealImageId(srcPath.toUri());
                if (srcRealImageId != -1) {
                    Backend.deleteRealImage(srcRealImageId);
                    logger.debug("Change realImageId of historicaId: {} ... from realImageId: {}, to realImageId: {}", historicaId, srcRealImageId, dstRealImageId);
                }

                return true;
            }
        } catch (AtomicMoveNotSupportedException e) {
            try {
                FileUtils.moveFile(srcPath.toFile(), dstPath.toFile());
                if (Files.exists(dstPath)) {
                    logger.debug("dstPath to URI: {}", dstPath.toUri());
                    int dstRealImageId = Backend.getNewImageId(dstPath.toUri(), null, FILE);
                    Integer historicaId = historicaIds.get(src);
                    if (dstRealImageId == -1 || historicaId == null) {
                        String exception = "Could not get realImageId or historicaId for the path...src: " + src + ", dst: " + dstPath;
                        ex = exception;
                        return false;
                    }
                    Backend.changeImageIdOfHistorica(historicaId, dstRealImageId);
                    TreeItem<HistoricaProperty> prop = treeCache.getLeafNode(historicaId);
                    prop.getValue().setRealImagePath(dstPath.toUri().toString());
                    treeCache.replaceLeafNode(historicaId, prop);

                    int srcRealImageId = Backend.getRealImageId(srcPath.toUri());
                    if (srcRealImageId != -1) {
                        Backend.deleteRealImage(srcRealImageId);
                        logger.debug("Change realImageId of historicaId: {} ... from realImageId: {}, to realImageId: {}", historicaId, srcRealImageId, dstRealImageId);
                    }

                    return true;
                }
            } catch (IOException exe) {
                ex = Rsc.get("common_menu_EPAT_ioEx_1");
            }
        } catch (AccessDeniedException e) {
            ex = Rsc.get("common_menu_EPAT_acsDenied_1");
        } catch (NoSuchFileException e) {
            String exception
                    = Rsc.get("common_menu_EPAT_noFileEx_1")
                    + Rsc.get("common_menu_EPAT_noFileEx_2")
                    + ", " + srcPath + "\n"
                    + Rsc.get("common_menu_EPAT_noFileEx_3")
                    + ", " + dstPath + "\n\n";
            ex = exception;
        } catch (FileAlreadyExistsException e) {
            logger.debug("FileAlreadyExistsException...{}", e.getMessage());
        } catch (FileSystemException e) {
            ex = e.getMessage();
        } catch (IOException e) {
            ex = Rsc.get("common_menu_EPAT_ioEx_1");
        } catch (Exception e) {
            logger.error("", e);
            ex = e.getMessage();
        }

        return false;
    }

    private boolean checkPreConditions(String src, String destination, String ext) {
        logger.debug("Start checkPreConditions() src: {}, destination: {}, ext: {}", src, destination, ext);
        Path realHistoricaPath = null;
        try {
            srcPath = Paths.get(new URI(src));
            dstPath = Paths.get(destination);
            logger.debug("srcPath: {}", srcPath);
            logger.debug("dstPath: {}", dstPath);

            if (!Files.exists(srcPath)) {
                ex = Rsc.get("common_menu_EPAT_noFileEx_1");
                return false;
            }

            if (srcPath.getParent().equals(dstPath.getParent())) {
                ex = Rsc.get("common_menu_EPAT_samePathEx");
                return false;
            }

            if (pathIncluding) {
                realHistoricaPath = Paths.get(destination.substring(0, destination.lastIndexOf(fileSeparator)));
                if (!Files.exists(realHistoricaPath)) {
                    logger.debug("path: {}", realHistoricaPath);
                    Files.createDirectories(realHistoricaPath);
                }
            }
            if (Files.exists(dstPath)) {
                logger.debug("Destination exists... destination: {}", destination);
                String origFile = destination.substring(destination.lastIndexOf(fileSeparator) + 1);
                String baseName = origFile.substring(0, origFile.lastIndexOf("."));
                logger.debug("Make suffix for the file: {}", baseName);

                Pattern pattern = Pattern.compile("(" + Pattern.quote(baseName) + "_)([0-9]+)(\\.(bmp|gif|jpe?g|png|tiff?))$");
                List<Integer> suffixes = realHistoricaPath == null ? getSuffixes(dstDirectory.toPath(), pattern) : getSuffixes(realHistoricaPath, pattern);
                logger.debug("Suffixes: {}", suffixes);

                boolean isOmmitted = false;
                int num = 1;
                for (int i : suffixes) {
                    if (i != num) {
                        isOmmitted = true;
                        break;
                    }
                    num++;
                }

                int suffix = isOmmitted ? num : suffixes.size() + 1;
                String dst = destination.substring(0, destination.lastIndexOf("."));
                dstPath = Paths.get(dst + "_" + suffix + "." + ext);
            }

            return true;

        } catch (InvalidPathException e) {
            e.printStackTrace();
            ex = e.getMessage();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            ex = e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            ex = Rsc.get("common_menu_EPAT_ioEx_1");
        } catch (Exception e) {
            logger.error("", e);
            ex = e.getMessage();
        }

        return false;
    }

    private List<Integer> getSuffixes(Path path, Pattern pattern) throws IOException {
        List<Integer> suffixes = new ArrayList<>();
        logger.debug("Called getSuffixes()...");

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                String name = entry.getFileName().toString();
                Matcher matcher = pattern.matcher(name);
                if (matcher.find()) {
                    logger.debug("Match the pattern...{}", matcher.group());
                    suffixes.add(Integer.parseInt(matcher.group(2)));
                    return true;
                } else {
                    return false;
                }
            }
        })) {
            for (Path p : ds) {
            }
        }

        Collections.sort(suffixes);
        return suffixes;
    }
}
