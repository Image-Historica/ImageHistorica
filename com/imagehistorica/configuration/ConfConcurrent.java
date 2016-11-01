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

package com.imagehistorica.configuration;

import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.Config;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class ConfConcurrent extends HBox {

    private Label l_thread = new Label(Rsc.get("conf_CC_thread"));
    private Label l_unitMaking = new Label(Rsc.get("conf_CC_unitMaking"));
    private Label l_unitGetting = new Label(Rsc.get("conf_CC_unitGetting"));

    private ChoiceBox<Integer> thread = new ChoiceBox<>();
    private ChoiceBox<Integer> unitMaking = new ChoiceBox<>();
    private ChoiceBox<Integer> unitGetting = new ChoiceBox<>();

    public ConfConcurrent() {
        createLayout();
    }

    public void createLayout() {

        int cpuCore = Runtime.getRuntime().availableProcessors();
        Integer[] num = new Integer[cpuCore];
        for (int i = 1; i <= cpuCore; i++) {
            num[i - 1] = i;
        }

        thread.getItems().addAll(num);
        thread.getSelectionModel().select(Config.getNumOfThreads() - 1);

        num = new Integer[12];
        int j = 1;
        for (int i = 1; i <= 12; i++) {
            num[i - 1] = j;
            j = j * 2;
        }

        unitMaking.getItems().addAll(num);
        unitMaking.getSelectionModel().select(Integer.valueOf(Config.getProcUnitInMaking()));

        unitGetting.getItems().addAll(num);
        unitGetting.getSelectionModel().select(Integer.valueOf(Config.getProcUnitInGetting()));

        this.setSpacing(25);
        this.getChildren().addAll(l_thread, thread, l_unitMaking, unitMaking, l_unitGetting, unitGetting);

    }

    public int getNumOfThreads() {
        return this.thread.getValue();
    }

    public void setNumOfThreads(int num) {
        this.thread.setValue(num);
    }

    public int getNumOfUnitMaking() {
        return this.unitMaking.getValue();
    }

    public void setNumOfUnitMaking(int num) {
        this.unitMaking.setValue(num);
    }

    public int getNumOfUnitGetting() {
        return this.unitGetting.getValue();
    }

    public void setNumOfUnitGetting(int num) {
        this.unitGetting.setValue(num);
    }
}
