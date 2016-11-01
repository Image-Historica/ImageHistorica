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
public class Request {

    @PrimaryKey
    int historicaId = 0;

    @SecondaryKey(relate = MANY_TO_ONE)
    int realImageId = 0;

    SchemeType type;
    String country;
    String lang;
    String signature;
    String realImagePath0;
    String realImagePath1;
    int lmod;
    String alt;
    String title;
    String snippet;
    String urlTitle;
    String html;
    String tmpImageName;
    int length = 0;
    byte[] feature = null;
    byte[] sha1 = null;
    byte imgio;
    byte[] binaryImage = null;
    byte major;
    byte minor;
    byte revision;

    public Request(SchemeType type, String imageId, String signature, String country, String lang, byte major, byte minor, byte revision) {
        this.type = type;
        this.realImagePath0 = imageId;
        this.signature = signature;
        this.country = country;
        this.lang = lang;
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    public Request(int historicaId, int realImageId, SchemeType type, String realImagePath0, int lmod, String country, String lang, byte major, byte minor, byte revision) {
        this.historicaId = historicaId;
        this.realImageId = realImageId;
        this.type = type;
        this.realImagePath0 = realImagePath0;
        this.realImagePath1 = null;
        this.lmod = lmod;
        this.country = country;
        this.lang = lang;
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    public Request(int historicaId, int realImageId, SchemeType type, String realImagePath0, String realImagePath1, int lmod, String country, String lang,
            String alt, String title, String snippet, String tmpImageName, byte[] binaryImage, byte major, byte minor, byte revision) {
        this.historicaId = historicaId;
        this.realImageId = realImageId;
        this.type = type;
        this.realImagePath0 = realImagePath0;
        this.realImagePath1 = realImagePath1;
        this.lmod = lmod;
        this.country = country;
        this.lang = lang;
        this.alt = alt;
        this.title = title;
        this.snippet = snippet;
        this.tmpImageName = tmpImageName;
        this.binaryImage = binaryImage;
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    public Request(int historicaId, int realImageId, SchemeType type, String realImagePath0, String realImagePath1, int lmod, String country, String lang,
            String alt, String title, String snippet, String urlTitle, String html, String tmpImageName, byte[] binaryImage, byte major, byte minor, byte revision) {
        this.historicaId = historicaId;
        this.realImageId = realImageId;
        this.type = type;
        this.realImagePath0 = realImagePath0;
        this.realImagePath1 = realImagePath1;
        this.lmod = lmod;
        this.country = country;
        this.lang = lang;
        this.alt = alt;
        this.title = title;
        this.snippet = snippet;
        this.urlTitle = urlTitle;
        this.html = html;
        this.tmpImageName = tmpImageName;
        this.binaryImage = binaryImage;
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    private Request() {
    }

    public int getHistoricaId() {
        return this.historicaId;
    }

    public void setHistoricaId(int historicaId) {
        this.historicaId = historicaId;
    }

    public int getRealImageId() {
        return this.realImageId;
    }

    public void setRealImageId(int realImageId) {
        this.realImageId = realImageId;
    }

    public SchemeType getType() {
        return this.type;
    }

    public void setType(SchemeType type) {
        this.type = type;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLang() {
        return this.lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
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

    public String getUrlTitle() {
        return this.urlTitle;
    }

    public void setUrlTitle(String urlTitle) {
        this.urlTitle = urlTitle;
    }

    public String getHtml() {
        return this.html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getTmpImageName() {
        return this.tmpImageName;
    }

    public void setTmpImageName(String tmpImageName) {
        this.tmpImageName = tmpImageName;
    }

    public int getLength() {
        return this.length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getFeature() {
        return this.feature;
    }

    public void setFeature(byte[] feature) {
        this.feature = feature;
    }

    public byte[] getSha1() {
        return this.sha1;
    }

    public void setSha1(byte[] sha1) {
        this.sha1 = sha1;
    }

    public byte getImgio() {
        return this.imgio;
    }

    public void setImgio(byte imgio) {
        this.imgio = imgio;
    }

    public byte[] getBinaryImage() {
        return this.binaryImage;
    }

    public void setBinaryImage(byte[] binaryImage) {
        this.binaryImage = binaryImage;
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
