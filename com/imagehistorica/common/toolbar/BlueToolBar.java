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

import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.controller.resources.Rsc;

import javafx.scene.Cursor;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class BlueToolBar extends BaseToolBar {

    private SearchMenu searchMenu;
    private SuggestTextField suggest;
    private Tooltip searchTip = new Tooltip(Rsc.get("toolbar_BTB_search"));

    public BlueToolBar(ImageHistoricaController parentController, Factory factory) {
        super(parentController, factory);
        this.suggest = new SuggestTextField();
        this.searchMenu = new SearchMenu(parentController, factory);
        this.suggest.setSearchMenu(searchMenu);
        this.searchMenu.setSuggestTextField(suggest);
        createLayout();
    }

    private void createLayout() {
        attachEvents();
        
        searchMenu.setTooltip(searchTip);
        this.getItems().addAll(back, forward, new Separator(), optionsMenu, suggest, searchMenu, space, minimize, maximize, close);
    }

    private void attachEvents() {
        searchTip.activatedProperty().addListener((obs, oldVal, newVal) -> {
            factory.getScene().setCursor(Cursor.DEFAULT);
        });
    }
}
