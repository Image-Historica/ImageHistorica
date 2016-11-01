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

import com.imagehistorica.controller.resources.Rsc;
import static com.imagehistorica.util.Constants.WEB_OPTIONS;
import static javafx.scene.text.FontSmoothingType.GRAY;
import static javafx.scene.text.FontSmoothingType.LCD;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.web.WebView;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class WebOptionsMenu extends MenuButton {

    private final WebView webView;

    private Menu scalingMenu;
    private Menu smoothingMenu;
    private Menu zoomMenu;
    private CheckMenuItem ctxMenu;
    private CheckMenuItem scriptMenu;
    private Tooltip optionTip = new Tooltip(Rsc.get("toolbar_WOM_tooltip"));

    public WebOptionsMenu(WebView webView) {
        this.webView = webView;
        createLayout();
    }

    public void createLayout() {
        SVGPath svgWebOptions = new SVGPath();
        svgWebOptions.setStroke(Color.ORANGE);
        svgWebOptions.setContent(WEB_OPTIONS);
        this.setGraphic(svgWebOptions);
        this.setTooltip(optionTip);

        createScalingMenu();
        createSmootingMenu();
        createZoomMenu();
        createCxtMenu();
        createScriptMenu();

        this.getItems().addAll(scalingMenu, smoothingMenu,
                zoomMenu, new SeparatorMenuItem(), ctxMenu, scriptMenu);
    }

    private void createScalingMenu() {
        scalingMenu = new Menu("Font Scale");
        scalingMenu.textProperty().bind(
                new SimpleStringProperty("Font Scale ")
                .concat(webView.fontScaleProperty().multiply(100.0))
                .concat("%"));
        MenuItem normalFontMenu = new MenuItem("Normal");
        MenuItem biggerFontMenu = new MenuItem("10% Bigger");
        MenuItem smallerFontMenu = new MenuItem("10% Smaller");
        normalFontMenu.setOnAction(e -> webView.setFontScale(1.0));
        biggerFontMenu.setOnAction(e -> webView.setFontScale(webView.getFontScale() + 0.10));
        smallerFontMenu.setOnAction(e -> webView.setFontScale(webView.getFontScale() - 0.10));
        scalingMenu.getItems().addAll(normalFontMenu, biggerFontMenu, smallerFontMenu);
    }

    private void createSmootingMenu() {
        smoothingMenu = new Menu("Font Smoothing");
        RadioMenuItem grayMenu = new RadioMenuItem("GRAY");
        grayMenu.setSelected(true);
        RadioMenuItem lcdMenu = new RadioMenuItem("LCD");
        grayMenu.setOnAction(e -> webView.setFontSmoothingType(GRAY));
        lcdMenu.setOnAction(e -> webView.setFontSmoothingType(LCD));
        new ToggleGroup().getToggles().addAll(lcdMenu, grayMenu);
        smoothingMenu.getItems().addAll(grayMenu, lcdMenu);
    }

    private void createZoomMenu() {
        zoomMenu = new Menu("Zoom");
        zoomMenu.textProperty().bind(
                new SimpleStringProperty("Zoom ")
                .concat(webView.zoomProperty().multiply(100.0))
                .concat("%"));
        MenuItem normalZoomMenu = new MenuItem("Normal");
        MenuItem biggerZoomMenu = new MenuItem("10% Bigger");
        MenuItem smallerZoomMenu = new MenuItem("10% Smaller");
        normalZoomMenu.setOnAction(e -> webView.setZoom(1.0));
        biggerZoomMenu.setOnAction(e -> webView.setZoom(webView.getZoom() + 0.10));
        smallerZoomMenu.setOnAction(e -> webView.setZoom(webView.getZoom() - 0.10));
        zoomMenu.getItems().addAll(normalZoomMenu, biggerZoomMenu, smallerZoomMenu);
    }

    private void createCxtMenu() {
        ctxMenu = new CheckMenuItem("Enable Context Menu");
        ctxMenu.setSelected(true);
        webView.contextMenuEnabledProperty().bind(ctxMenu.selectedProperty());
    }

    private void createScriptMenu() {
        scriptMenu = new CheckMenuItem("Enable JavaScript");
        scriptMenu.setSelected(true);
        webView.getEngine().javaScriptEnabledProperty()
                .bind(scriptMenu.selectedProperty());
    }
}
