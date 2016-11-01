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

import java.net.URI;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class RealImageProperty {

    SchemeType type;
    URI realImagePath0;
    URI realImagePath0Tmp;
    URI realImagePath1;
    URI realImagePath1Tmp;
    int id = 0;
    int lmod;
    String alt;
    String title;
    String snippet;
    String tmpImageName;
    byte[] binaryImage = null;
    String exception = null;

    public RealImageProperty(URI realImagePath0, int lmod) {
        this.type = SchemeType.FILE;
        this.realImagePath0 = realImagePath0;
        this.realImagePath0Tmp = null;
        this.realImagePath1 = null;
        this.realImagePath1Tmp = null;
        this.lmod = lmod;
    }

    public RealImageProperty(SchemeType type, URI realImagePath0, URI realImagePath1) {
        this.type = type;
        this.realImagePath0 = realImagePath0;
        this.realImagePath0Tmp = null;
        this.realImagePath1 = realImagePath1;
        this.realImagePath1Tmp = null;
    }

    public RealImageProperty(SchemeType type, URI realImagePath0, URI realImagePath1, String alt, String title, String snippet) {
        this.type = type;
        this.realImagePath0 = realImagePath0;
        this.realImagePath0Tmp = null;
        this.realImagePath1 = realImagePath1;
        this.realImagePath1Tmp = null;
        this.alt = alt;
        this.title = title;
        this.snippet = snippet;
    }

    public SchemeType getType() {
        return this.type;
    }

    public void setType(SchemeType type) {
        this.type = type;
    }

    public URI getRealImagePath0() {
        return this.realImagePath0;
    }

    public void setRealImagePath0(URI realImagePath0) {
        this.realImagePath0 = realImagePath0;
    }

    public URI getRealImagePath0Tmp() {
        return this.realImagePath0Tmp;
    }

    public void setRealImagePath0Tmp(URI realImagePath0Tmp) {
        this.realImagePath0Tmp = realImagePath0Tmp;
    }

    public URI getRealImagePath1() {
        return this.realImagePath1;
    }

    public void setRealImagePath1(URI realImagePath1) {
        this.realImagePath1 = realImagePath1;
    }

    public URI getRealImagePath1Tmp() {
        return this.realImagePath1Tmp;
    }

    public void setRealImagePath1Tmp(URI realImagePath1Tmp) {
        this.realImagePath1Tmp = realImagePath1Tmp;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLmod() {
        return this.lmod;
    }

    public void setLmod(int lmod) {
        this.lmod = lmod;
    }

    public String getAlt() {
        return this.alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return this.snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getTmpImageName() {
        return this.tmpImageName;
    }

    public void setTmpImageName(String tmpImageName) {
        this.tmpImageName = tmpImageName;
    }

    public byte[] getBinaryImage() {
        return this.binaryImage;
    }

    public void setBinaryImage(byte[] binaryImage) {
        this.binaryImage = binaryImage;
    }

    public String getException() {
        return this.exception;
    }

    public void setException(String ex) {
        this.exception = ex;
    }

    @Override
    public int hashCode() {
        return realImagePath0.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RealImageProperty prop = (RealImageProperty) o;
        return realImagePath0 != null && realImagePath0.equals(prop.getRealImagePath0());
    }

    @Override
    public String toString() {
        return realImagePath0.toString();
    }
}
