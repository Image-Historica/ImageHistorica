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

import com.imagehistorica.util.model.SchemeType;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
@Entity
public class RealImage {

    @PrimaryKey
    int realImageId;

    @SecondaryKey(relate = MANY_TO_ONE)
    long realImagePathCrc;

    SchemeType type;
    String realImagePath0;
    String realImagePath1;
    byte major;
    byte minor;
    byte revision;

    public RealImage(int realImageId, long realImagePathCrc, SchemeType type, String realImagePath0, String realImagePath1, byte major, byte minor, byte revision) {
        this.realImageId = realImageId;
        this.realImagePathCrc = realImagePathCrc;
        this.type = type;
        this.realImagePath0 = realImagePath0;
        this.realImagePath1 = realImagePath1;
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    private RealImage() {
    }

    public int getRealImageId() {
        return this.realImageId;
    }

    public void setRealImageId(int realImageId) {
        this.realImageId = realImageId;
    }

    public long getRealImagePathCrc() {
        return this.realImagePathCrc;
    }

    public void setRealImagePathCrc(long realImagePathCrc) {
        this.realImagePathCrc = realImagePathCrc;
    }

    public SchemeType getType() {
        return this.type;
    }

    public void setType(SchemeType type) {
        this.type = type;
    }

    public String getRealImagePath0() {
        return this.realImagePath0;
    }

    public void setRealImagePath0(String realImagePath0) {
        this.realImagePath0 = realImagePath0;
    }

    public String getRealImagePath1() {
        return this.realImagePath1;
    }

    public void setRealImagePath1(String realImagePath1) {
        this.realImagePath1 = realImagePath1;
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
