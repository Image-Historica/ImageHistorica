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
package com.imagehistorica.databases.model;

import static com.sleepycat.persist.model.Relationship.MANY_TO_ONE;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
@Entity
public class HistoricaDir {

    @PrimaryKey
    int historicaDirId;

    @SecondaryKey(relate = MANY_TO_ONE)
    String hisDirSuffix;

    String hisDirPathPart;
    int meaningId = 0;
    byte major;
    byte minor;
    byte revision;

    public HistoricaDir(int historicaDirId, String hisDirSuffix, String hisDirPathPart, int meaningId, byte major, byte minor, byte revision) {
        this.historicaDirId = historicaDirId;
        this.hisDirSuffix = hisDirSuffix;
        this.hisDirPathPart = hisDirPathPart;
        this.meaningId = meaningId;
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    private HistoricaDir() {
    }

    public int getHistoricaDirId() {
        return historicaDirId;
    }

    public void setHistoricaDirId(int historicaDirId) {
        this.historicaDirId = historicaDirId;
    }

    public String getHisDirSuffix() {
        return this.hisDirSuffix;
    }

    public void setHisDirSuffix(String hisDirSuffix) {
        this.hisDirSuffix = hisDirSuffix;
    }

    public String getHisDirPathPart() {
        return this.hisDirPathPart;
    }

    public void setHisDirPathPart(String hisDirPathPart) {
        this.hisDirPathPart = hisDirPathPart;
    }

    public int getMeaningId() {
        return this.meaningId;
    }

    public void setMeaningId(int meaningId) {
        this.meaningId = meaningId;
    }

    public byte getMajor() {
        return this.major;
    }

    public void setMajor(byte major) {
        this.major = major;
    }

    public byte getMinor() {
        return this.minor;
    }

    public void setMinor(byte minor) {
        this.minor = minor;
    }

    public byte getRevision() {
        return this.revision;
    }

    public void setRevision(byte revision) {
        this.revision = revision;
    }
}
