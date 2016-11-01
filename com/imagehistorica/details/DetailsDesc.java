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

package com.imagehistorica.details;

import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.controller.resources.Rsc;
import java.net.URI;
import java.nio.file.Paths;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class DetailsDesc extends ScrollPane {

    private Label historicaPath = new Label(Rsc.get("details_DD_historicaPath"));
    private Label historica = new Label(Rsc.get("details_DD_historica"));
    private Label lmod = new Label(Rsc.get("details_DD_lmod"));
    private Label length = new Label(Rsc.get("details_DD_length"));
    private Label freq = new Label(Rsc.get("details_DD_freq"));
    private Label url = new Label(Rsc.get("details_DD_url"));
    private Label real = new Label(Rsc.get("details_DD_real"));

    private Label historicaPathS = new Label();
    private Label historicaS = new Label();
    private Label lmodS = new Label();
    private Label lengthS = new Label();
    private Label freqS = new Label();
    private Label urlS = new Label();
    private Label realS = new Label();

    public DetailsDesc() {
        createLayout();
    }

    public void createLayout() {
        historicaPath.setStyle("-fx-text-fill: #96b946;");
        historica.setStyle("-fx-text-fill: #96b946;");
        lmod.setStyle("-fx-text-fill: #96b946;");
        length.setStyle("-fx-text-fill: #96b946;");
        freq.setStyle("-fx-text-fill: #96b946;");
        url.setStyle("-fx-text-fill: #96b946;");
        real.setStyle("-fx-text-fill: #96b946;");

        historicaPath.setUnderline(true);
        historica.setUnderline(true);
        lmod.setUnderline(true);
        length.setUnderline(true);
        freq.setUnderline(true);
        url.setUnderline(true);
        real.setUnderline(true);

        historicaPathS.setWrapText(true);
        historicaS.setWrapText(true);
        lmodS.setWrapText(true);
        lengthS.setWrapText(true);
        freqS.setWrapText(true);
        urlS.setWrapText(true);
        realS.setWrapText(true);

        VBox v = new VBox(historicaPath, historicaPathS, historica, historicaS, lmod, lmodS, length, lengthS, freq, freqS, url, urlS, real, realS);
        this.setContent(v);
        this.setFitToWidth(true);
    }

    public void setText(TreeItem<HistoricaProperty> treeItem) {
        HistoricaProperty prop = treeItem.getValue();
        
        historicaPathS.setText(prop.getHistoricaPath() + "\n ");
        historicaS.setText(prop.getImageName() + "\n ");
        lmodS.setText(prop.getLastModified().toString() + "\n ");
        lengthS.setText(prop.getLength() + "\n ");
        freqS.setText(prop.getFreqViewed() + "\n ");
        urlS.setText(prop.getUrl() + "\n ");
        realS.setText(Paths.get(URI.create(prop.getRealImagePath())) + "\n ");
    }
}
