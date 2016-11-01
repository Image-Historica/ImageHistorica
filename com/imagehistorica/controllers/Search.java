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
import com.imagehistorica.common.toolbar.BlueToolBar;
import com.imagehistorica.common.state.SearchState;
import com.imagehistorica.controller.ControllerType;
import com.imagehistorica.util.view.ResizeStage;
import com.imagehistorica.search.SearchImage;
import com.imagehistorica.util.controller.SearchResult;
import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.type.Yellow;
import com.imagehistorica.search.Selectable;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class Search extends Factory implements Yellow {

    private ImageHistoricaController parentController;
    private Factory prevController = null;

    private SearchState state = SearchState.getInstance();

    private BorderPane bp;

    private BlueToolBar toolMenuBar;
    private SearchResult searchResult;
    private Tab resultTab;
    private TabPane tabPane = new TabPane();

    private final Logger logger = LoggerFactory.getLogger(Search.class);

    @Override
    public Scene createLayout(ImageHistoricaController parentController) {
        this.parentController = parentController;
        this.toolMenuBar = new BlueToolBar(this.parentController, this);

        SearchImage searchImage = new SearchImage(state.getCurId(), state.getCurSearchTxt(), state.isCentral());
        this.searchResult = new SearchResult(this.parentController, searchImage);

        if (state.isCentral()) {
            createTab(searchResult, searchImage, state.getCurId());
            state.putSearchImage(state.getCurId(), searchImage);
        } else {
            createTab(searchResult, searchImage, state.getCurSearchTxt());
            state.putSearchImage(state.getCurSearchTxt(), searchImage);
        }
        resultTab.setText(state.getCurSearchTxt());
        tabPane = new TabPane();
        tabPane.getTabs().add(resultTab);
        tabPane.getStyleClass().add("floating");

        bp = new BorderPane(tabPane, toolMenuBar, null, null, null);
        scene = new Scene(bp);
        setScene(scene);

        resizeStage();
        attachEvents();

        return scene;
    }

    private void checkTask() {
        String curId;
        if (state.isCentral()) {
            curId = state.getCurId();
        } else {
            curId = state.getCurSearchTxt();
        }
        if (curId != null) {
            if (!state.containSearchImages(curId)) {
                SearchImage searchImage = new SearchImage(state.getCurId(), state.getCurSearchTxt(), state.isCentral());
                searchResult = new SearchResult(parentController, searchImage);
                createTab(searchResult, searchImage, curId);
                state.putSearchImage(curId, searchImage);
                resultTab.setText(state.getCurSearchTxt());
                tabPane.getTabs().add(resultTab);
                tabPane.getSelectionModel().select(resultTab);
            }
        }
    }

    @Override
    public Scene restoreScene() {
        checkTask();
        return this.scene;
    }

    @Override
    protected void attachEvents() {
        resultTab.setOnClosed(e -> {
            state.removeSearchImage(resultTab.getId());
            SearchResult searchResult = ((SearchResult) ((ScrollPane) resultTab.getContent()).getContent());
            for (Node n : searchResult.getChildren()) {
                if (n instanceof Selectable) {
                    Selectable s = (Selectable) n;
                    Image image = s.getImage();
                    image = null;
                    s.setImage(null);
                }
            }
            searchResult.getChildren().clear();
            searchResult = null;
            System.gc();
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((ob, oldTab, newTab) -> {
            if (newTab != null) {
                state.setCurId(newTab.getId());
                state.setCurSearchTxt(newTab.getText());
            }
        });
    }

    @Override
    public ControllerType getControllerType() {
        return controllerType;
    }

    private Tab createTab(final SearchResult searchResult, final SearchImage searchImage, final String id) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(searchResult);
        scrollPane.viewportBoundsProperty().addListener((ob, oldVal, newVal) -> {
            searchResult.setPrefWidth(newVal.getWidth());
        });

        ChangeListener<Object> changeListener = (ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
            if (scrollPane.getVvalue() > 0.8) {
                searchResult.addImages();
            }
        };
        scrollPane.viewportBoundsProperty().addListener(changeListener);
        scrollPane.hvalueProperty().addListener(changeListener);
        scrollPane.vvalueProperty().addListener(changeListener);
        if (searchImage.isCentral()) {
            scrollPane.setOnMouseMoved((e) -> searchResult.integrate());
        }
//        scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);

        resultTab = new Tab();
        resultTab.setId(id);
        resultTab.setText(state.getCurSearchTxt());
        resultTab.setContent(scrollPane);
        resultTab.setClosable(true);

        return resultTab;
    }

    @Override
    protected void resizeStage() {
        ResizeStage.setResizeStage(this.parentController.getStage(), scene);
    }

    @Override
    public void adjustLayout() {
    }
}
