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

import com.imagehistorica.Config;
import static com.imagehistorica.util.Constants.SEARCH;
import static com.imagehistorica.util.Constants.SEARCH_SCENE_CONTROLLER;

import com.imagehistorica.common.state.SearchState;
import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.controllers.Search;
import com.imagehistorica.databases.Backend;

import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class SearchMenu extends SplitMenuButton {

    private final ImageHistoricaController parentController;
    private final Factory factory;

    private SearchState state = SearchState.getInstance();
    private SuggestTextField stf;

    protected RadioMenuItem central = new RadioMenuItem(Rsc.get("toolbar_SM_item_1"));
    protected RadioMenuItem localdb = new RadioMenuItem(Rsc.get("toolbar_SM_item_2"));

    public SearchMenu(ImageHistoricaController parentController, Factory factory) {
        this.parentController = parentController;
        this.factory = factory;
        createLayout();
    }

    public void createLayout() {
        SVGPath svgSearch = new SVGPath();
        svgSearch.setStroke(Color.ORANGE);
        svgSearch.setContent(SEARCH);
        this.setGraphic(svgSearch);

        if (state.isCentral()) {
            central.setSelected(true);
        } else {
            localdb.setSelected(true);
        }

        new ToggleGroup().getToggles().addAll(central, localdb);
        attachEvents();

        this.getItems().addAll(central, localdb);
    }

    private void attachEvents() {
        central.setOnAction(e -> state.setCentral(true));
        localdb.setOnAction(e -> state.setCentral(false));

        this.setOnAction(e -> {
            callSearchResult(stf.getText());
        });
    }

    public void callSearchResult(String searchTxt) {
        if (state.isCentral()) {
            if (Config.isFree()) {
                if (parentController.showFreeAlert(factory)) {
                    return;
                }
            }
            
            String suggestId = stf.getSuggestId();
            if (searchTxt != null && stf.checkSuggest(searchTxt) && suggestId != null) {
                if (state.getCurId() != null) {
                    if (state.getCurId().equals(suggestId) && state.containSearchImages(suggestId)) {
                        return;
                    }
                }

                state.setCurId(suggestId);
                state.setCurSearchTxt(searchTxt);

                if (parentController.getSearch() != null) {
                    parentController.restoreScene(parentController.getSearch());
                } else {
                    parentController.setSearch((Search) this.parentController.createScene(factory, SEARCH_SCENE_CONTROLLER));
                }
            }
        } else {
            if (searchTxt != null) {
                state.setCurSearchTxt(searchTxt);

                if (parentController.getSearch() != null) {
                    parentController.restoreScene(parentController.getSearch());
                } else {
                    parentController.setSearch((Search) this.parentController.createScene(factory, SEARCH_SCENE_CONTROLLER));
                }
            }
        }
        Backend.setPrevController(factory);
    }

    public void setSuggestTextField(SuggestTextField stf) {
        this.stf = stf;
    }
}
