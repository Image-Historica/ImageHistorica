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

import static com.imagehistorica.util.Constants.*;

import com.imagehistorica.controller.Factory;
import com.imagehistorica.controller.ImageHistoricaController;
import com.imagehistorica.appreciate.order.AdditionalStage;
import com.imagehistorica.controller.resources.Rsc;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class AdditionalToolBar extends BaseToolBar {

    private Button clearBtn = new Button();
    private Tooltip clearTip = new Tooltip(Rsc.get("toolbar_ATB_clear"));

    public AdditionalToolBar(ImageHistoricaController parentController, Factory factory) {
        super(parentController, factory);
        createLayout();
    }

    public void createLayout() {
        SVGPath svgClear = new SVGPath();
        svgClear.setStroke(Color.AQUA);
        svgClear.setContent(CLEAR);
        clearBtn.setGraphic(svgClear);
        clearBtn.setTooltip(clearTip);

        attachEvents();
        attachBinds();

        this.getItems().addAll(back, forward, clearBtn, new Separator(), optionsMenu, space, minimize, maximize, close);
    }

    private void attachEvents() {
        clearBtn.setOnAction(e -> {
            AdditionalStage.listOfStages.stream().forEach((stage) -> {
                stage.close();
            });
            AdditionalStage.listOfStages.clear();
        });
    }

    private void attachBinds() {
        clearBtn.disableProperty().bind(Bindings.size(AdditionalStage.listOfStages).lessThanOrEqualTo(0));
    }
}
