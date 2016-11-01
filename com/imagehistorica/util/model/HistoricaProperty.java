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
package com.imagehistorica.util.model;

import com.imagehistorica.databases.Backend;

import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class HistoricaProperty {

    private final ReadOnlyIntegerWrapper historicaId = new ReadOnlyIntegerWrapper(this, "historicaId");
    private final ReadOnlyIntegerWrapper historicaDirId = new ReadOnlyIntegerWrapper(this, "historicaDirId");
    private final StringProperty imageName = new SimpleStringProperty(this, "imageName", null);
    private final StringProperty realImagePath = new SimpleStringProperty(this, "realImagePath", null);
    private final StringProperty historicaPath = new SimpleStringProperty(this, "historicaPath", null);
    private final ObjectProperty<LocalDate> lastModified = new SimpleObjectProperty<>(this, "lastModified", null);
    private final Integer length;
    private final String url;
    private Integer freqViewed = null;
    private IntegerProperty numOfLeaves = new SimpleIntegerProperty();
    private NumberBinding numOfChildren = numOfLeaves.add(0);
    private HistoricaType type;

    public HistoricaProperty(String hisDirSuffix, String hisDirPathPart, int historicaDirId, HistoricaType type, boolean isNew) {
        this.imageName.set(hisDirSuffix);
        this.historicaPath.set(hisDirPathPart);
        this.historicaDirId.set(historicaDirId);
        this.type = type;
        this.length = null;
        this.url = null;
        if (type == HistoricaType.ROOT) {
            numOfLeaves.set(0);
        } else {
            if (isNew) {
                numOfLeaves.set(Backend.getHistoricaNumsMapNew(this.historicaDirId.get()));
            } else {
                numOfLeaves.set(Backend.getHistoricaNumsMap(this.historicaDirId.get()));
            }
        }
    }

    public HistoricaProperty(int historicaId, int historicaDirId, String imageName, String realImagePath,
            String historicaPath, LocalDate lastModified, int freqViewed, int length, String hyperlink, HistoricaType type) {
        this.historicaId.set(historicaId);
        this.historicaDirId.set(historicaDirId);
        this.imageName.set(imageName);
        this.realImagePath.set(realImagePath);
        this.historicaPath.set(historicaPath);
        this.lastModified.set(lastModified);
        this.freqViewed = freqViewed;
        this.length = length;
        this.url = hyperlink;
        this.type = type;
        this.numOfLeaves.set(0);
    }

    public int getHistoricaId() {
        return historicaId.get();
    }

    public ReadOnlyIntegerProperty historicaIdProperty() {
        return historicaId.getReadOnlyProperty();
    }

    public int getHistoricaDirId() {
        return historicaDirId.get();
    }

    public void setHistoricaDirId(int id) {
        historicaDirId.set(id);
    }

    public ReadOnlyIntegerProperty historicaDirIdProperty() {
        return historicaDirId.getReadOnlyProperty();
    }

    public String getImageName() {
        return imageName.get();
    }

    public void setImageName(String imageName) {
        imageNameProperty().set(imageName);
    }

    public StringProperty imageNameProperty() {
        return imageName;
    }

    /* realPath Property */
    public String getRealImagePath() {
        return realImagePath.get();
    }

    public void setRealImagePath(String realImagePath) {
        realImagePathProperty().set(realImagePath);
    }

    public StringProperty realImagePathProperty() {
        return realImagePath;
    }

    /*  historicaPath Property */
    public String getHistoricaPath() {
        return historicaPath.get();
    }

    public void setHistoricaPath(String historicaPath) {
        historicaPathProperty().set(historicaPath);
    }

    public StringProperty historicaPathProperty() {
        return historicaPath;
    }

    public LocalDate getLastModified() {
        return lastModified.get();
    }

    public void setLastModified(LocalDate lastModified) {
        lastModifiedProperty().set(lastModified);
    }

    public ObjectProperty<LocalDate> lastModifiedProperty() {
        return lastModified;
    }

    public Integer getLength() {
        return length;
    }

    public Integer getFreqViewed() {
        return freqViewed;
    }

    public void setFreqViewed(int freqViewed) {
        this.freqViewed = freqViewed;
    }

    public String getUrl() {
        return this.url;
    }

    public HistoricaType getType() {
        return this.type;
    }

    public void setType(HistoricaType type) {
        this.type = type;
    }

    public IntegerProperty getNumOfLeaves() {
        return numOfLeaves;
    }

    public void setNumOfLeaves(int numOfLeaves) {
        numOfLeavesProperty().set(numOfLeaves);
    }

    public IntegerProperty numOfLeavesProperty() {
        return numOfLeaves;
    }

    public NumberBinding getNumOfChildren() {
        return this.numOfChildren;
    }

    public void setNumOfChildren(NumberBinding children) {
        this.numOfChildren = children;
    }
}
