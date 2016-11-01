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

import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
@Entity
public class Historica {

    @PrimaryKey
    int historicaId;

    @SecondaryKey(relate = MANY_TO_ONE)
    int historicaDirId = 0;

    @SecondaryKey(relate = MANY_TO_ONE)
    int realImageId = 0;

    @SecondaryKey(relate = MANY_TO_ONE)
    int lmod = 0;

    SchemeType type;
    String imageName = "";
    String ext = "";
    String srcUrl = "";
    String keyPrefix = "";
    int keyId;
    int length;
    long locationId;
    short freqViewed = 0;
    byte[] feature = null;
    byte[] sha1 = null;
    byte imgio;
    byte major;
    byte minor;
    byte revision;
    Integer[] meaningIds = null;

    public Historica(int historicaId, int realImageId, int length, byte[] feature, byte[] sha1, byte imgio, byte major, byte minor, byte revision) {
        this.historicaId = historicaId;
        this.realImageId = realImageId;
        this.length = length;
        this.feature = feature;
        this.sha1 = sha1;
        this.imgio = imgio;
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    private Historica() {
    }

    public int getHistoricaId() {
        return this.historicaId;
    }

    public void setHistoricaId(int historicaId) {
        this.historicaId = historicaId;
    }

    public int getHistoricaDirId() {
        return this.historicaDirId;
    }

    public void setHistoricaDirId(int historicaDirId) {
        this.historicaDirId = historicaDirId;
    }

    public int getRealImageId() {
        return this.realImageId;
    }

    public void setRealImageId(int realImageId) {
        this.realImageId = realImageId;
    }

    public int getLmod() {
        return this.lmod;
    }

    public void setLmod(int lmod) {
        this.lmod = lmod;
    }

    public SchemeType getType() {
        return this.type;
    }

    public void setType(SchemeType type) {
        this.type = type;
    }

    public String getImageName() {
        return this.imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getExt() {
        return this.ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getSrcUrl() {
        return this.srcUrl;
    }

    public void setSrcUrl(String srcUrl) {
        this.srcUrl = srcUrl;
    }

    public String getKeyPrefix() {
        return this.keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public int getKeyId() {
        return this.keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getLength() {
        return this.length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getLocationId() {
        return this.locationId;
    }

    public void setLocationId(long locationId) {
        this.locationId = locationId;
    }

    public short getFreqViewed() {
        return this.freqViewed;
    }

    public void setFreqViewed(short freqViewed) {
        this.freqViewed = freqViewed;
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

    public Integer[] getMeaningIds() {
        return this.meaningIds;
    }

    public void setMeaningIds(Integer[] meaningIds) {
        this.meaningIds = meaningIds;
    }

    @Override
    public String toString() {
        String cld = feature != null ? new String(Hex.encodeHex(feature)) : null;
        String sha = sha1 != null ? new String(Hex.encodeHex(sha1)) : null;
        return "\n\n"
                + "[Version: " + major + "." + minor + "." + revision + ", Type: " + type + ", historicaId: " + historicaId + ", historicaDirId: " + historicaDirId + ", realImageId: " + realImageId + " ]\n"
                + "[imageName: " + imageName + ", ext: " + ext + " ]\n"
                + "[keyPrefix: " + keyPrefix + ", keyId: " + keyId + ", locId: " + locationId + ", lmod: " + lmod + ", freqViewed: " + freqViewed + ", length: " + length + " ]\n"
                + "[feature: " + cld + ", imgio: " + imgio + " ]\n"
                + "[sha1: " + sha + " ]\n"
                + "[srcUrl: " + srcUrl + ", meaningId: " + meaningIds + " ]\n";
    }
}
