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

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
import com.imagehistorica.common.state.SearchState;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;

public class SuggestTextField extends TextField {

    private SearchState state = SearchState.getInstance();

    private ContextMenu popup = new ContextMenu();
    private SuggestHandler handler = null;
    private SearchMenu searchMenu;

    public SuggestTextField() {
        super();
        this.handler = new SuggestHandler(this, popup);
        attachEvents();
    }

    private void attachEvents() {
        textProperty().addListener((ObservableValue<? extends String> ob, String oldVal, String newVal) -> {
            if (!oldVal.equals(newVal)) {
                if (!newVal.isEmpty()) {
                    if (state.isCentral()) {
                        handler.doGetCentral(newVal);
                    } else {
                        handler.doGetBackend(newVal);
                    }
                } else {
                    popup.hide();
                }
            }
        });

        this.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER:
                    this.searchMenu.callSearchResult(getText());
            }
        });

        focusedProperty().addListener((ob, oldVal, newVal) -> {
            if (newVal) {
                if (state.isCentral()) {
                    handler.open();
                }
            } else {
                popup.hide();
            }
        });
    }

    public void setSearchMenu(SearchMenu searchMenu) {
        this.searchMenu = searchMenu;
    }

    public String getSuggestId() {
        return handler.getSuggestId();
    }

    public boolean checkSuggest(String searchTxt) {
        return handler.checkSuggest(searchTxt);
    }
}
