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
package com.imagehistorica.common.toolbar;

import com.imagehistorica.util.view.CommonAlert;
import static com.imagehistorica.util.Constants.*;

import com.imagehistorica.controller.ControllerType;
import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.Backend;

import javafx.beans.binding.Bindings;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class BaseToolBar extends ToolBar {

    protected final ImageHistoricaController parentController;
    protected final Factory factory;
    protected Scene scene;

    protected double dragStartX;
    protected double dragStartY;

    protected Button back = new Button();
    protected Button forward = new Button();
    protected Button minimize = new Button();
    protected Button maximize = new Button();
    protected Button close = new Button();
    protected HBox space = new HBox();
    protected OptionsMenu optionsMenu;

    protected SVGPath svgBack = new SVGPath();
    protected SVGPath svgForward = new SVGPath();
    protected SVGPath svgMinimize = new SVGPath();
    protected SVGPath svgMaximize = new SVGPath();
    protected SVGPath svgClose = new SVGPath();

    protected Tooltip backTip = new Tooltip(Rsc.get("toolbar_BTB_back"));
    protected Tooltip forwardTip = new Tooltip(Rsc.get("toolbar_BTB_forward"));
    protected Tooltip optionsTip = new Tooltip(Rsc.get("toolbar_BTB_options"));
    protected Tooltip minimizeTip = new Tooltip(Rsc.get("toolbar_BTB_minimize"));
    protected Tooltip maximizeTip_1 = new Tooltip(Rsc.get("toolbar_BTB_maximize1"));
    protected Tooltip maximizeTip_2 = new Tooltip(Rsc.get("toolbar_BTB_maximize2"));
    protected Tooltip closeTip = new Tooltip(Rsc.get("toolbar_BTB_close"));

    public BaseToolBar(ImageHistoricaController parentController, Factory factory) {
        this.parentController = parentController;
        this.factory = factory;
        this.optionsMenu = new OptionsMenu(parentController, factory);
        createParts();
    }

    public void createParts() {
        svgBack.setStroke(Color.ORANGE);
        svgBack.setContent(BACK);
        back.setGraphic(svgBack);

        svgForward.setStroke(Color.ORANGE);
        svgForward.setContent(FORWARD);
        forward.setGraphic(svgForward);

        svgMinimize.setStroke(Color.ORANGE);
        svgMinimize.setContent(MINIMIZE);
        minimize.setGraphic(svgMinimize);

        svgMaximize.setStroke(Color.ORANGE);
        svgMaximize.setContent(MAXIMIZE);
        maximize.setGraphic(svgMaximize);

        svgClose.setStroke(Color.ORANGE);
        svgClose.setContent(CLOSE);
        close.setGraphic(svgClose);
        close.getStyleClass().add("close-button");

        HBox.setHgrow(space, Priority.ALWAYS);

        back.setTooltip(backTip);
        forward.setTooltip(forwardTip);
        optionsMenu.setTooltip(optionsTip);
        minimize.setTooltip(minimizeTip);
        close.setTooltip(closeTip);

        setCursor(Cursor.DEFAULT);

        attachEvents();
        attachBinds();
    }

    private void attachEvents() {
        back.setOnAction(e -> {
            try {
                Backend.setNextController(factory);
                CommonAlert.getDebugLog("[BaseToolBar] Back: next stored " + factory.getClass().getName());
                Factory prevFactory = Backend.getPrevController();
                CommonAlert.getDebugLog("[BaseToolBar] Back: prev restored " + prevFactory.getClass().getName());
                if (prevFactory != null) {
                    parentController.restoreScene(prevFactory);
                } else {
                    parentController.restoreScene(parentController.getInitialScene());
                }
            } catch (NullPointerException ex) {
            } catch (Exception ex) {
                CommonAlert.getErrorLog(ex);
            }
            e.consume();
        });

        forward.setOnAction(e -> {
            try {
                Backend.setPrevController(factory);
                CommonAlert.getDebugLog("[BaseToolBar] Forward: prev stored " + factory.getClass().getName());
                Factory nextFactory = Backend.getNextController(factory);
                if (nextFactory != null) {
                    CommonAlert.getDebugLog("[BaseToolBar] Forward: next restored " + nextFactory.getClass().getName());
                    parentController.restoreScene(nextFactory);
                } else {
                    CommonAlert.getDebugLog("[BaseToolBar] Forward: null... ");
                }
            } catch (NullPointerException ex) {
            } catch (Exception ex) {
                CommonAlert.getErrorLog(ex);
            }
            e.consume();
        });

        space.setOnMousePressed(e -> {
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
            e.consume();
        });
        space.setOnMouseDragged(e -> {
            parentController.getStage().setX(e.getScreenX() - dragStartX);
            parentController.getStage().setY(e.getScreenY() - dragStartY);
            e.consume();
        });

        minimize.setOnAction(e -> {
            parentController.getStage().setIconified(true);
            e.consume();
        });

        maximize.setOnMouseEntered(e -> {
            if (!parentController.getStage().isMaximized()) {
                maximize.setTooltip(maximizeTip_2);
            } else {
                maximize.setTooltip(maximizeTip_1);
            }
            e.consume();
        });

        maximize.setOnAction(e -> {
            if (!parentController.getStage().isMaximized()) {
                parentController.getStage().setMaximized(true);
                factory.adjustLayout();
            } else {
                parentController.getStage().setMaximized(false);
            }
            e.consume();
        });

        close.setOnAction(e -> {
            parentController.closeRequest();
            e.consume();
        });

        backTip.activatedProperty().addListener((obs, oldVal, newVal) -> {
            factory.getScene().setCursor(Cursor.DEFAULT);
        });
        forwardTip.activatedProperty().addListener((obs, oldVal, newVal) -> {
            factory.getScene().setCursor(Cursor.DEFAULT);
        });
        optionsTip.activatedProperty().addListener((obs, oldVal, newVal) -> {
            factory.getScene().setCursor(Cursor.DEFAULT);
        });
        minimizeTip.activatedProperty().addListener((obs, oldVal, newVal) -> {
            factory.getScene().setCursor(Cursor.DEFAULT);
        });
        maximizeTip_1.activatedProperty().addListener((obs, oldVal, newVal) -> {
            factory.getScene().setCursor(Cursor.DEFAULT);
        });
        maximizeTip_2.activatedProperty().addListener((obs, oldVal, newVal) -> {
            factory.getScene().setCursor(Cursor.DEFAULT);
        });
        closeTip.activatedProperty().addListener((obs, oldVal, newVal) -> {
            factory.getScene().setCursor(Cursor.DEFAULT);
        });
    }

    private void attachBinds() {
        switch (factory.getControllerType()) {
            case GREEN:
                forward.disableProperty().bind(Bindings.size(Backend.greenStack).lessThanOrEqualTo(0));
                break;
            case BLUE:
                forward.disableProperty().bind(Bindings.size(Backend.blueStack).lessThanOrEqualTo(0));
                break;
            case YELLOW:
                forward.setDisable(true);
            case RED:
                if (Backend.checkPrevController().getControllerType() == ControllerType.GREEN) {
                    forward.disableProperty().bind(Bindings.size(Backend.greenStack).lessThanOrEqualTo(0));
                } else if (Backend.checkPrevController().getControllerType() == ControllerType.BLUE) {
                    forward.disableProperty().bind(Bindings.size(Backend.blueStack).lessThanOrEqualTo(0));
                }
                break;
        }
    }
}
