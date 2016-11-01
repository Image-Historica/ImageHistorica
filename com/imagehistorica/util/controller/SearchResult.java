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
package com.imagehistorica.util.controller;

import com.imagehistorica.Config;
import static com.imagehistorica.util.Constants.SEARCH_ACCESS_POINT;

import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.databases.model.Historica;
import com.imagehistorica.databases.model.HistoricaDir;
import com.imagehistorica.httpclient.CentralHttpClient;
import com.imagehistorica.Key;
import com.imagehistorica.common.menu.CxtMenu;
import com.imagehistorica.common.state.ImageState;
import com.imagehistorica.common.state.SearchState;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.search.SearchImage;
import com.imagehistorica.search.Selectable;
import com.imagehistorica.search.SelectableHistorica;
import com.imagehistorica.search.SelectableImage;
import com.imagehistorica.util.view.CommonAlert;
import java.nio.charset.StandardCharsets;

import javafx.scene.image.Image;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import org.apache.http.entity.StringEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class SearchResult extends FlowPane {

    private SearchState searchState = SearchState.getInstance();
    private ImageState imageState = ImageState.getInstance();
    private ImageHistoricaController parentController;
    private SearchImage searchImage;
    private List<Historica> historicas;
    private Set<Selectable> selectables = new HashSet<>();
    private DropShadow ds = new DropShadow();
    private int index = 0;
    private boolean isSelected = false;
    private boolean isShowing = false;

    private CxtMenu cxtMenu = new CxtMenu(selectables);

    private Logger logger = LoggerFactory.getLogger(SearchResult.class);

    public SearchResult(ImageHistoricaController parentController, SearchImage searchImage) {
        this.parentController = parentController;
        this.searchImage = searchImage;
        this.setHgap(40);
        this.setVgap(40);
        ds.setOffsetY(7.5);
        ds.setOffsetX(7.5);
        ds.setColor(Color.YELLOW);
        createLayout();
    }

    public void createLayout() {
        if (searchImage.isCentral()) {
            searchCentral();
            cxtMenu.createLayoutImport();
        } else {
            searchBackend();
            cxtMenu.createLayoutDisplay();
        }

    }

    private void searchCentral() {
        logger.debug("Start searchCentral()...");
        if (!Config.isFetchImage()) {
            Label label = new Label(Rsc.get("common_util_SR_disable"));
            label.setAlignment(Pos.CENTER);
            this.getChildren().clear();
            this.getChildren().add(label);
            return;
        }
        // (x)000`123456`|1a25852/xxx.jpg`image-historica.com/xxx.html^000`123456`|image-historica.com/xxx.jpg`image-historica.com/xxx.html`1.0256
        int tryNum = searchImage.getTryNum().getAndIncrement();
        if (tryNum > 3) {
            return;
        }
        String query = Key.accessKey + ":" + searchImage.getId() + ":" + tryNum + ":" + searchImage.getSearchTxt();
        logger.debug("Request to central... {}", query);
        String rbody = CentralHttpClient.doPost(SEARCH_ACCESS_POINT, new StringEntity(query, StandardCharsets.UTF_8), searchState, 5000);
        String[] rbodies = null;
        if (rbody != null && !rbody.isEmpty()) {
            if (rbody.substring(rbody.lastIndexOf("^") + 1).equals("end")) {
                rbody = rbody.substring(0, rbody.lastIndexOf("^"));
                searchImage.setEnd(true);
            }
            rbodies = rbody.split("\\^");
            if (rbodies != null && rbodies.length > 0) {
                for (String url : rbodies) {
                    searchImage.offerUrl(url);
                }

                ThreadCreator t = new ThreadCreator();
                t.createSearches(searchImage);
            }
        } else {
            if (!searchState.hasResponse()) {
                Label label = new Label(Rsc.get("socketTimeout"));
                label.setAlignment(Pos.CENTER);
                this.getChildren().clear();
                this.getChildren().add(label);
            } else if (searchState.peekExceptions() != null) {
                Label label = new Label(searchState.pollExceptions());
                label.setAlignment(Pos.CENTER);
                this.getChildren().clear();
                this.getChildren().add(label);
            }
        }
    }

    private void searchBackend() {
        logger.debug("Start searchBackend()...");
        if (index == 0) {
            historicas = new ArrayList<>();
            List<HistoricaDir> historicaDirs = Backend.searchHistoricaDirs(searchImage.getSearchTxt());
            historicaDirs.stream().map((historicaDir) -> Backend.getHistoricasByHistoricaDirId(historicaDir.getHistoricaDirId())).forEach((historicas_part) -> {
                historicas.addAll(historicas_part);
            });
        }

        for (; index < historicas.size(); index++) {
            String path = Backend.getRealImage(historicas.get(index).getRealImageId()).getRealImagePath0();
            logger.debug("Get real image: {}", path);
            Image image = imageState.getImage(path);
            Selectable selectable;
            if (image != null) {
                selectable = new SelectableHistorica(historicas.get(index), image);
            } else {
                image = new Image(path, 300, 300, true, false);
                selectable = new SelectableHistorica(historicas.get(index), image);
            }
            attachEvents(selectable);
            this.getChildren().add(selectable);

            if (index % 200 == 0 && index != 0) {
                break;
            }
        }
    }

    public void attachEvents(Selectable selectable) {
        selectable.setOnMouseClicked(e -> {
            boolean secondaryClicked = e.getButton().equals(MouseButton.SECONDARY);
            if (secondaryClicked) {
                if (!selectables.isEmpty()) {
                    logger.debug("Selected num of Selectables: {}", selectables.size());
                    cxtMenu.show(parentController.getStage(), e.getScreenX(), e.getScreenY());
                    isShowing = true;
                    isSelected = false;
                }
                return;
            }

            if (isShowing) {
                cxtMenu.hide();
                isSelected = false;
                isShowing = false;
                for (Selectable s : selectables) {
                    s.setEffect(null);
                    s.setSelected(false);
                }
                selectables.clear();
                return;
            }

            if (!selectable.isSelected()) {
                selectables.add(selectable);
                selectable.setEffect(ds);
                selectable.setSelected(true);
                isSelected = true;
            } else {
                selectables.remove(selectable);
                selectable.setEffect(null);
                selectable.setSelected(false);
            }
        });

        selectable.setOnMouseEntered(e -> {
            if (isSelected) {
                if (!selectable.isSelected()) {
                    selectables.add(selectable);
                    selectable.setEffect(ds);
                    selectable.setSelected(true);
                } else {
                    selectables.remove(selectable);
                    selectable.setEffect(null);
                    selectable.setSelected(false);
                }
            }
        });
    }

    public void integrate() {
        if (searchState.isOOM()) {
            CommonAlert ca = new CommonAlert();
            ca.showOOMAlert();
            searchState.setOOM(false);
        }
        while (searchImage.peekCentral() != null) {
            Pair<String, Image> pair = searchImage.pollCentral();
            if (pair != null) {
                String[] imgInfos = pair.getKey().split("`");
                String imageId = imgInfos[0] + imgInfos[1];
                Selectable selectable = new SelectableImage(imageId, pair.getKey(), pair.getValue());
                attachEvents(selectable);
                this.getChildren().add(selectable);
            }
        }
    }

    public void addImages() {
        if (searchImage.isCentral()) {
            if (!searchImage.inProgress() && (!searchImage.isEnd() || searchImage.getTryNum().get() < 4)) {
                searchCentral();
            }
        } else {
            if (index <= historicas.size()) {
                searchBackend();
            }
        }
    }
}
