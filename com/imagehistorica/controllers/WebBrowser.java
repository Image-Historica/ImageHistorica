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

package com.imagehistorica.controllers;

import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.ControllerType;
import com.imagehistorica.controller.type.Yellow;
import com.imagehistorica.util.view.ResizeStage;
import com.imagehistorica.common.state.WebState;
import com.imagehistorica.controller.Factory;
import com.imagehistorica.common.toolbar.WebToolBar;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class WebBrowser extends Factory implements Yellow {

    private ImageHistoricaController parentController;
    private WebState state = WebState.getInstance();
    private BorderPane bp;

    private WebView webView = new WebView();
    private WebEngine webEngine;
    private WebHistory webHistory;
    private ListView<WebHistory.Entry> historyView = new ListView<>();

    private WebToolBar webToolBar;
    private CheckMenuItem webHistoryMenu;

    private final Logger logger = LoggerFactory.getLogger(WebBrowser.class);

    @Override
    public Scene createLayout(ImageHistoricaController parentController) {
        this.parentController = parentController;
        webEngine = webView.getEngine();
        webHistory = webEngine.getHistory();
        webEngine.load(state.getLoadPage());

        historyView.setItems(webHistory.getEntries());
        historyView.setCellFactory(lv -> new ListCell<WebHistory.Entry>() {
            @Override
            public void updateItem(WebHistory.Entry entry, boolean empty) {
                super.updateItem(entry, empty);
                textProperty().unbind();
                if (empty) {
                    setText(null);
                } else {
                    textProperty().bind(entry.titleProperty());
                }
            }
        });

        webToolBar = new WebToolBar(this.parentController, this, webView, webEngine, webHistory, historyView);
        webToolBar.setPageUrl(state.getLoadPage());
        webHistoryMenu = webToolBar.getWebHistoryMenu();

        bp = new BorderPane(webView, webToolBar, null, null, null);
        bp.getStyleClass().add("mylistview");

        scene = new Scene(bp);
        setScene(scene);

        resizeStage();
        attachEvents();

        return scene;

    }

    @Override
    public Scene restoreScene() {
        return this.scene;
    }

    @Override
    protected void attachEvents() {
        historyView.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.intValue() != webHistory.getCurrentIndex()) {
                webHistory.go(newValue.intValue() - webHistory.getCurrentIndex());
            }
        });

        webHistoryMenu.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == true) {
                replaceLayout(true);
            } else {
                replaceLayout(false);
            }
        });

        webView.getEngine().titleProperty().addListener((obs, oldTitle, newTitle) -> {
            parentController.getStage().setTitle(newTitle);
        });
    }

    @Override
    public ControllerType getControllerType() {
        return controllerType;
    }

    @Override
    protected void resizeStage() {
        ResizeStage.setResizeStage(this.parentController.getStage(), scene);
    }

    @Override
    public void adjustLayout() {
    }

    private void replaceLayout(boolean history) {
        if (history) {
            SplitPane s = new SplitPane();
            s.setDividerPositions(0.3f);
            s.getItems().addAll(historyView, webView);
            bp = new BorderPane(s, webToolBar, null, null, null);
        } else {
            bp = new BorderPane(webView, webToolBar, null, null, null);
        }

        bp.getStyleClass().add("mylistview");
        scene = new Scene(bp);
        setScene(this.parentController.applyStyleSheet(scene));

        resizeStage();
        attachEvents();

        this.parentController.restoreScene(this.parentController.getWebBrowser());
    }
}
