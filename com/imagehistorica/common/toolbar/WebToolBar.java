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

import static com.imagehistorica.util.Constants.BACK;
import static com.imagehistorica.util.Constants.HOME;

import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.Backend;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class WebToolBar extends BaseToolBar {

    private WebView webView;
    private WebEngine webEngine;
    private WebHistory webHistory;
    private ListView<WebHistory.Entry> historyView = new ListView<>();

    private WebOptionsMenu webOptionsMenu;
    private CheckMenuItem webHistoryMenu = new CheckMenuItem(Rsc.get("toolbar_WTB_history"));

    private Button back = new Button();

    private TextField pageUrl = new TextField();
    private Button homeBtn = new Button();
    private Tooltip homeTip = new Tooltip(Rsc.get("toolbar_WTB_home"));

    private String homePageUrl = "https://image-historica.com";
    private FileChooser fileChooser = new FileChooser();

    public WebToolBar(ImageHistoricaController parentController, Factory factory,
            WebView webView, WebEngine webEngine, WebHistory webHistory, ListView<WebHistory.Entry> historyView) {

        super(parentController, factory);
        this.webView = webView;
        this.webEngine = webEngine;
        this.webHistory = webHistory;
        this.historyView = historyView;
        this.webOptionsMenu = webOptionsMenu;

        createLayout();
    }

    private void createLayout() {
        SVGPath svgBack = new SVGPath();
        svgBack.setStroke(Color.ORANGE);
        svgBack.setContent(BACK);
        back.setGraphic(svgBack);
        back.setTooltip(backTip);

        SVGPath svgHome = new SVGPath();
        svgHome.setStroke(Color.ORANGE);
        svgHome.setContent(HOME);
        homeBtn.setGraphic(svgHome);
        homeBtn.setTooltip(homeTip);

        HBox.setHgrow(pageUrl, Priority.ALWAYS);
        webOptionsMenu = new WebOptionsMenu(webView);
        webHistoryMenu.setSelected(false);
        webOptionsMenu.getItems().add(webHistoryMenu);
        
        attachEvents();

        this.getItems().addAll(back, forward, new Separator(), pageUrl, space, homeBtn, webOptionsMenu, minimize, maximize, close);
    }

    private void attachEvents() {
        back.setOnAction(e -> {
            if (webHistory.getCurrentIndex() <= 0) {
                Backend.setNextController(factory);
                Factory prevFactory = Backend.getPrevController();
                this.parentController.restoreScene(prevFactory);
            } else {
                webHistory.go(-1);
            }
        });

        forward.disableProperty().bind(
                webHistory.currentIndexProperty().greaterThanOrEqualTo(Bindings.size(webHistory.getEntries()).subtract(1)));
        forward.setOnAction(e -> webHistory.go(1));

        pageUrl.setOnAction(e -> webEngine.load(pageUrl.getText()));
        homeBtn.setOnAction(e -> webEngine.load(homePageUrl));

        webHistory.currentIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex.intValue() != historyView.getSelectionModel().getSelectedIndex()) {
                historyView.getSelectionModel().clearAndSelect(newIndex.intValue());
            }
        });

        historyView.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.intValue() != webHistory.getCurrentIndex()) {
                webHistory.go(newValue.intValue() - webHistory.getCurrentIndex());
            }
        });

        // Configure the FileChooser
        fileChooser.setTitle("Open Web Content");
        fileChooser.getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("HTML Files", "*.html", "*.htm"));

        webEngine.locationProperty().addListener((ovs, oldVal, newVal) -> {
            pageUrl.setText(newVal);
        });
    }

    public void setPageUrl(String loadPage) {
        this.pageUrl.setText(loadPage);
    }

    public CheckMenuItem getWebHistoryMenu() {
        return this.webHistoryMenu;
    }
}
