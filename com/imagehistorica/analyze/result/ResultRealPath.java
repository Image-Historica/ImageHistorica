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

package com.imagehistorica.analyze.result;

import com.imagehistorica.common.state.AnalyzeState;
import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.controller.ImageHistoricaController;

import javafx.scene.Cursor;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.util.Map;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class ResultRealPath extends BorderPane {

    private ImageHistoricaController parentController;
    private AnalyzeState state = AnalyzeState.getInstance();
    private Map<Integer, HistoricaProperty> props;
    private ListView<HistoricaProperty> resultView = new ListView<>();

    public ResultRealPath(ImageHistoricaController parentController) {
        this.parentController = parentController;
        createLayout();
    }

    public void createLayout() {
        props = state.createLeaves();
        if (props != null && !props.isEmpty()) {
            state.getCreatedHistoricas().stream().forEach((historica) -> {
                resultView.getItems().add(props.get(historica.getHistoricaId()));
            });
        }

        attachEvents();

        this.setCursor(Cursor.DEFAULT);
        this.setCenter(resultView);
    }

    private void attachEvents() {
        resultView.setCellFactory(new Callback<ListView<HistoricaProperty>, ListCell<HistoricaProperty>>() {
            @Override
            public ListCell<HistoricaProperty> call(ListView<HistoricaProperty> listView) {
                return new ListCell<HistoricaProperty>() {
                    @Override
                    public void updateItem(HistoricaProperty item, boolean empty) {
                        super.updateItem(item, empty);

                        int index = this.getIndex();
                        String name = null;
                        if (item != null && !empty) {
                            name = (index + 1) + ". "
                                    + item.getRealImagePath();
                        }

                        this.setText(name);
                        setGraphic(null);
                    }
                };
            }
        });
    }

    public Map<Integer, HistoricaProperty> getHistoricaProperty() {
        return this.props;
    }

    public ListView<HistoricaProperty> getResultView() {
        return this.resultView;
    }
}
