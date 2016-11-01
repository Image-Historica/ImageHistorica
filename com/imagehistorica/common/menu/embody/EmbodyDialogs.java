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

import com.imagehistorica.Config;
import static com.imagehistorica.util.Constants.DELIMITER;
import static com.imagehistorica.common.menu.embody.EmbodyHistoricaType.*;

import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.util.model.HistoricaType;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.model.Historica;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class EmbodyDialogs extends Dialog {

    private final HistoricaProperty prop;
    private final HistoricaType historicaType;
    private final EmbodyHistoricaType embHisType;
    private final List<Historica> historicas;
    private final File dstDirectory;
    private final long totalLength;
    private final String eliminatePath;
    private EmbodyHistorica embodyHistorica;

    private long freeSpace = 0;

    private String fileSeparator;
    private String historicaPath;
    private double pow1 = Math.pow(1024, 1);
    private double pow2 = Math.pow(1024, 2);
    private double pow3 = Math.pow(1024, 3);
    private String errorTitle = Rsc.get("common_menu_ED_errTitle");
    private String errorHeader = Rsc.get("common_menu_ED_errHeader");
    private String errorContent = null;

    private String historicaOnly = Rsc.get("common_menu_ED_historicaOnly");
    private String pathIncluding = Rsc.get("common_menu_ED_pathIncluding");
    private String ok = Rsc.get("okBtn");
    private String cancel = Rsc.get("cancelBtn");

    private final Logger logger = LoggerFactory.getLogger(EmbodyDialogs.class);

    public EmbodyDialogs(String errorContent) {
        this.prop = null;
        this.historicaType = null;
        this.embHisType = null;
        this.historicas = null;
        this.dstDirectory = null;
        this.totalLength = 0;
        this.eliminatePath = null;

        this.errorContent = errorContent;
    }

    // For HistoricaType ROOT
    public EmbodyDialogs(HistoricaProperty prop, HistoricaType historicaType, EmbodyHistoricaType embHisType,
            List<Historica> historicas, File dstDirectory, long totalLength) {

        this.prop = prop;
        this.historicaType = historicaType;
        this.historicas = historicas;
        this.dstDirectory = dstDirectory;
        this.embHisType = embHisType;
        this.totalLength = totalLength;
        this.eliminatePath = null;

        this.fileSeparator = File.separator;
        if (fileSeparator.equals("\\")) {
            fileSeparator = "\\\\";
        }

        historicaPath = this.prop.getImageName();
    }

    public EmbodyDialogs(HistoricaProperty prop, HistoricaType historicaType, EmbodyHistoricaType embHisType,
            List<Historica> historicas, File dstDirectory, long totalLength, String eliminatePath) {

        this.prop = prop;
        this.historicaType = historicaType;
        this.historicas = historicas;
        this.dstDirectory = dstDirectory;
        this.embHisType = embHisType;
        this.totalLength = totalLength;
        this.eliminatePath = eliminatePath;

        this.fileSeparator = File.separator;
        if (fileSeparator.equals("\\")) {
            fileSeparator = "\\\\";
        }

        if (this.historicaType == HistoricaType.LEAF) {
            historicaPath = this.prop.getHistoricaPath().replaceAll(DELIMITER, fileSeparator);
        } else if (this.historicaType == HistoricaType.ROOT) {
            historicaPath = this.prop.getImageName();
        } else {
            String hisDirPathPart = this.prop.getHistoricaPath();
            String hisDirSuffix = this.prop.getImageName();
            historicaPath = hisDirPathPart + hisDirSuffix;
            historicaPath = historicaPath.replaceAll(DELIMITER, fileSeparator);
        }
    }

    public void embodyHistorica_1st() {
        if (!hasFreeSpace()) {
            return;
        }

        if (fileSeparator.equals("\\\\")) {
            fileSeparator = "\\";
        }

        String copyTitle = Rsc.get("common_menu_ED_copyTitle");
        String moveTitle = Rsc.get("common_menu_ED_moveTitle");

        String historicaOnlyHeader = Rsc.get("common_menu_ED_historicaOnlyHeader_1");
        String historicaWithPathHeader = Rsc.get("common_menu_ED_historicaWithPathHeader");

        String historicaOnlyContent = Rsc.get("common_menu_ED_historicaOnlyContent_1") + prop.getImageName() + "\n\n"
                + Rsc.get("common_menu_ED_historicaOnlyContent_2") + historicaPath + fileSeparator + prop.getImageName() + "\n\n\n\n";
        String historicaWithPathContent
                = Rsc.get("common_menu_ED_historicaCopy_1") + historicaPath + "\n\n"
                + Rsc.get("common_menu_ED_historicaCopy_2") + dstDirectory.getAbsolutePath() + "\n\n"
                + Rsc.get("common_menu_ED_historicaCopy_3") + getSizeToStr(totalLength) + "\n\n"
                + Rsc.get("common_menu_ED_historicaCopy_4") + getSizeToStr(freeSpace) + "\n\n\n\n";

        Alert embHisAlert = new Alert(Alert.AlertType.CONFIRMATION);
        embHisAlert.setResizable(true);
        embHisAlert.getDialogPane().getStylesheets().add(EmbodyDialogs.class.
                getResource("/resources/css/stylesheet.css").toExternalForm());

        if (embHisType == COPY_HISTORICA_ONLY) {
            embHisAlert.setTitle(copyTitle);
            embHisAlert.setHeaderText(historicaOnlyHeader);
            embHisAlert.setContentText(historicaOnlyContent);

            ButtonType b1 = new ButtonType(historicaOnly);
            ButtonType b2 = new ButtonType(pathIncluding);
            ButtonType b3 = new ButtonType(cancel);
            embHisAlert.getButtonTypes().setAll(b1, b2, b3);

            Optional<ButtonType> result = embHisAlert.showAndWait();
            if (result.get() == b1) {
                logger.debug("Selected COPY_HISTORICA_ONLY without path...");
                embodyHistorica_2nd(embHisType, b1);
            } else if (result.get() == b2) {
                logger.debug("Selected COPY_HISTORICA_ONLY including path...");
                embodyHistorica_2nd(embHisType, b2);
            }

        } else if (embHisType == MOVE_HISTORICA_ONLY) {
            embHisAlert.setTitle(moveTitle);
            embHisAlert.setHeaderText(historicaOnlyHeader);
            embHisAlert.setContentText(historicaOnlyContent);

            ButtonType b1 = new ButtonType(historicaOnly);
            ButtonType b2 = new ButtonType(pathIncluding);
            ButtonType b3 = new ButtonType(cancel);
            embHisAlert.getButtonTypes().setAll(b1, b2, b3);

            Optional<ButtonType> result = embHisAlert.showAndWait();
            if (result.get() == b1) {
                logger.debug("Selected MOVE_HISTORICA_ONLY without path...");
                embodyHistorica_2nd(embHisType, b1);
            } else if (result.get() == b2) {
                logger.debug("Selected MOVE_HISTORICA_ONLY including path...");
                embodyHistorica_2nd(embHisType, b2);
            }

        } else if (embHisType == COPY_HISTORICA_WITH_PATH) {

            embHisAlert.setTitle(copyTitle);
            embHisAlert.setHeaderText(historicaWithPathHeader);
            embHisAlert.setContentText(historicaWithPathContent);

            ButtonType b1 = new ButtonType(ok);
            ButtonType b2 = new ButtonType(cancel);
            embHisAlert.getButtonTypes().setAll(b1, b2);

            Optional<ButtonType> result = embHisAlert.showAndWait();
            if (result.get() == b1) {
                logger.debug("Selected COPY_HISTORICA_WITH_PATH...");
                boolean pathIncluding = true;
                if (historicaType == HistoricaType.ROOT) {
                    embodyHistorica = new EmbodyHistorica(copyTitle, COPY_HISTORICA_WITH_PATH, pathIncluding, historicas, dstDirectory, fileSeparator);
                } else {
                    embodyHistorica = new EmbodyHistorica(copyTitle, COPY_HISTORICA_WITH_PATH, pathIncluding, historicas, dstDirectory, fileSeparator, eliminatePath);
                }
            }

        } else if (embHisType == MOVE_HISTORICA_WITH_PATH) {

            embHisAlert.setTitle(moveTitle);
            embHisAlert.setHeaderText(historicaWithPathHeader);
            embHisAlert.setContentText(historicaWithPathContent);

            ButtonType b1 = new ButtonType(ok);
            ButtonType b2 = new ButtonType(cancel);
            embHisAlert.getButtonTypes().setAll(b1, b2);

            Optional<ButtonType> result = embHisAlert.showAndWait();
            if (result.get() == b1) {
                logger.debug("Selected MOVE_HISTORICA_WITH_PATH...");
                boolean pathIncluding = true;
                if (historicaType == HistoricaType.ROOT) {
                    embodyHistorica = new EmbodyHistorica(moveTitle, MOVE_HISTORICA_WITH_PATH, pathIncluding, historicas, dstDirectory, fileSeparator);
                } else {
                    embodyHistorica = new EmbodyHistorica(moveTitle, MOVE_HISTORICA_WITH_PATH, pathIncluding, historicas, dstDirectory, fileSeparator, eliminatePath);
                }
            }
        }
    }

    public void embodyHistorica_2nd(EmbodyHistoricaType type, ButtonType buttonType) {
        boolean copyHistoricaOnly = false;
        boolean moveHistoricaOnly = false;
        boolean copyPathIncluding = false;
        boolean movePathIncluding = false;

        String imageName = null;
        if (type == COPY_HISTORICA_ONLY) {
            if (buttonType.getText().equals(historicaOnly)) {
                logger.debug("COPY_HISTORICA_ONLY...historicaOnly");
                imageName = prop.getImageName();
                copyHistoricaOnly = true;
            } else if (buttonType.getText().equals(pathIncluding)) {
                logger.debug("COPY_HISTORICA_ONLY...pathIncluding");
                imageName = historicaPath + fileSeparator + prop.getImageName();
                copyPathIncluding = true;
            }
        } else if (type == MOVE_HISTORICA_ONLY) {
            if (buttonType.getText().equals(historicaOnly)) {
                logger.debug("MOVE_HISTORICA_ONLY...historicaOnly");
                imageName = prop.getImageName();
                moveHistoricaOnly = true;
            } else if (buttonType.getText().equals(pathIncluding)) {
                logger.debug("MOVE_HISTORICA_ONLY...pathIncluding");
                imageName = historicaPath + fileSeparator + prop.getImageName();
                movePathIncluding = true;
            }
        }

        String copyTitle = Rsc.get("common_menu_ED_copyTitle");
        String moveTitle = Rsc.get("common_menu_ED_moveTitle");

        String historicaOnlyHeader = Rsc.get("common_menu_ED_historicaWithPathHeader");
        String historicaOnlyContentCopy
                = Rsc.get("common_menu_ED_historicaCopy_1") + imageName + "\n\n"
                + Rsc.get("common_menu_ED_historicaCopy_2") + dstDirectory.getAbsolutePath() + "\n\n"
                + Rsc.get("common_menu_ED_historicaCopy_3") + getSizeToStr(totalLength) + "\n\n"
                + Rsc.get("common_menu_ED_historicaCopy_4") + getSizeToStr(freeSpace) + "\n\n\n\n";

        String historicaOnlyContentMove
                = Rsc.get("common_menu_ED_historicaMove_1") + imageName + "\n\n"
                + Rsc.get("common_menu_ED_historicaMove_2") + prop.getRealImagePath() + "\n\n"
                + Rsc.get("common_menu_ED_historicaMove_3") + dstDirectory.getAbsolutePath() + "\n\n"
                + Rsc.get("common_menu_ED_historicaMove_4") + getSizeToStr(totalLength) + "\n\n"
                + Rsc.get("common_menu_ED_historicaMove_5") + getSizeToStr(freeSpace) + "\n\n\n\n";

        Alert embHisAlert = new Alert(Alert.AlertType.CONFIRMATION);
        embHisAlert.setResizable(true);
        embHisAlert.getDialogPane().getStylesheets().add(EmbodyDialogs.class.
                getResource("/resources/css/stylesheet.css").toExternalForm());

        ButtonType b1 = new ButtonType(ok);
        ButtonType b2 = new ButtonType(cancel);
        embHisAlert.getButtonTypes().setAll(b1, b2);

        if (type == COPY_HISTORICA_ONLY) {
            embHisAlert.setTitle(copyTitle);
            embHisAlert.setHeaderText(historicaOnlyHeader);
            embHisAlert.setContentText(historicaOnlyContentCopy);

            Optional<ButtonType> result = embHisAlert.showAndWait();
            if (result.get() == b1) {
                if (hasWritePermission()) {
                    if (hasFreeSpace()) {
                        if (copyHistoricaOnly) {
                            logger.debug("Called embodyHistorica_2nd() COPY_HISTORICA_ONLY without path...");
                            boolean pathIncluding = false;
                            embodyHistorica = new EmbodyHistorica(copyTitle, COPY_HISTORICA_ONLY, pathIncluding, historicas, dstDirectory, fileSeparator, eliminatePath);
                        } else if (copyPathIncluding) {
                            logger.debug("Called embodyHistorica_2nd() COPY_HISTORICA_ONLY including path...");
                            boolean pathIncluding = true;
                            embodyHistorica = new EmbodyHistorica(copyTitle, COPY_HISTORICA_ONLY, pathIncluding, historicas, dstDirectory, fileSeparator, eliminatePath);
                        }
                    }
                }
            }

        } else if (type == EmbodyHistoricaType.MOVE_HISTORICA_ONLY) {
            embHisAlert.setTitle(moveTitle);
            embHisAlert.setHeaderText(historicaOnlyHeader);
            embHisAlert.setContentText(historicaOnlyContentMove);

            Optional<ButtonType> result = embHisAlert.showAndWait();
            if (result.get() == b1) {
                if (hasWritePermission()) {
                    if (hasFreeSpace()) {
                        if (moveHistoricaOnly) {
                            logger.debug("Called embodyHistorica_2nd() MOVE_HISTORICA_ONLY without path...");
                            boolean pathIncluding = false;
                            embodyHistorica = new EmbodyHistorica(moveTitle, MOVE_HISTORICA_ONLY, pathIncluding, historicas, dstDirectory, fileSeparator, eliminatePath);
                        } else if (movePathIncluding) {
                            logger.debug("Called embodyHistorica_2nd() MOVE_HISTORICA_ONLY including path...");
                            boolean pathIncluding = true;
                            embodyHistorica = new EmbodyHistorica(moveTitle, MOVE_HISTORICA_ONLY, pathIncluding, historicas, dstDirectory, fileSeparator, eliminatePath);
                        }
                    }
                }
            }
        }
    }

    public void errorAvailable() {
        Alert errorAvailable = new Alert(Alert.AlertType.ERROR);
        errorAvailable.setResizable(true);
        errorAvailable.getDialogPane().getStylesheets().add(EmbodyDialogs.class.
                getResource("/resources/css/stylesheet.css").toExternalForm());

        errorAvailable.setTitle(errorTitle);
        errorAvailable.setHeaderText(errorHeader);
        errorAvailable.setContentText(errorContent);

        ButtonType ok = new ButtonType("OK");
        errorAvailable.getButtonTypes().setAll(ok);

        errorAvailable.showAndWait();
    }

    private String getSizeToStr(long size) {
        double dsize = size;
        if (pow1 > size) {
            return size + " Byte";
        } else if (pow2 > size) {
            dsize = dsize / pow1;
            BigDecimal bd = new BigDecimal(String.valueOf(dsize));
            double value = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            return value + " KiB";
        } else if (pow3 > size) {
            dsize = dsize / pow2;
            BigDecimal bd = new BigDecimal(String.valueOf(dsize));
            double value = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            return value + " MiB";
        } else {
            dsize = dsize / pow3;
            BigDecimal bd = new BigDecimal(String.valueOf(dsize));
            double value = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            return value + " GiB";
        }
    }

    private boolean hasWritePermission() {
        if (dstDirectory.canWrite()) {
            logger.debug("Has write permission...");
            return true;
        } else {
            logger.error("No write permission...");
            Config.removeRootDir();
            errorContent = Rsc.get("common_menu_ED_writePerErr");
            errorAvailable();
            return false;
        }
    }

    private boolean hasFreeSpace() {
        freeSpace = dstDirectory.getFreeSpace();
        if (freeSpace > totalLength) {
            logger.debug("Free space available...");
            return true;
        } else {
            logger.error("No free space...");
            errorContent = Rsc.get("common_menu_ED_freeSpcErr_1")
                    + Rsc.get("common_menu_ED_freeSpcErr_2") + getSizeToStr(freeSpace) + "\n\n"
                    + Rsc.get("common_menu_ED_freeSpcErr_3") + getSizeToStr(totalLength) + "\n\n";

            errorAvailable();
            return false;
        }
    }
}
