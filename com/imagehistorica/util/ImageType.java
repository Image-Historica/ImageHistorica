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
package com.imagehistorica.util;

import com.imagehistorica.util.view.CommonAlert;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.http.HttpEntity;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class ImageType {

    private final static long jpeg = 0xffd8000000000000L;
    private final static long png = 0x89504E470D0A1A0AL;
    private final static long gif = 0x4749460000000000L;
    private final static long tiff1 = 0x4949000000000000L;
    private final static long tiff2 = 0x4D4D000000000000L;
    private final static long bmp = 0x424D000000000000L;
    private final static long pic = 0x5049430000000000L;

    public enum ImageFormat {

        JPEG("jpg"), BMP("bmp"), GIF("gif"), PNG("png"), TIFF("tif"), PICT("pic"), UNKNOWN("unknown");

        public String name;

        private ImageFormat(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static synchronized boolean isImage(Path path) {
        try (InputStream is = Files.newInputStream(path);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8];
            int len = is.read(buffer);
            baos.write(buffer, 0, len);
            baos.flush();
            return getFormat(baos.toByteArray()) != ImageFormat.UNKNOWN;
        } catch (IndexOutOfBoundsException ex) {
            CommonAlert ca = new CommonAlert();
            ca.showIOException(path.toString());
            return false;
        } catch (IOException ex) {
            CommonAlert ca = new CommonAlert();
            ca.showIOException(path.toString());
            return false;
        }
    }

    public static synchronized boolean isImage(HttpEntity entity) throws IOException {
        try (InputStream is = entity.getContent();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8];
            int len = is.read(buffer);
            baos.write(buffer, 0, len);
            baos.flush();
            return getFormat(baos.toByteArray()) != ImageFormat.UNKNOWN;
        }
    }

    public static ImageFormat getFormat(byte[] b) {
        long l;
        if (b.length < 8) {
            return ImageFormat.UNKNOWN;
        }
        l = (long) b[0] << 56 | ((long) b[1] & 0xffL) << 48 | ((long) b[2] & 0xffL) << 40 | ((long) b[3] & 0xffL) << 32
                | ((long) b[4] & 0xffL) << 24 | ((long) b[5] & 0xffL) << 16 | ((long) b[6] & 0xffL) << 8 | ((long) b[7] & 0xffL);
        if ((l & jpeg) == jpeg) {
            return ImageFormat.JPEG;
        } else if ((l & png) == png) {
            return ImageFormat.PNG;
        } else if ((l & gif) == gif) {
            return ImageFormat.GIF;
        } else if ((l & tiff1) == tiff1 || (l & tiff2) == tiff2) {
            return ImageFormat.TIFF;
        } else if ((l & bmp) == bmp) {
            return ImageFormat.BMP;
        } else if ((l & pic) == pic) {
            return ImageFormat.PICT;
        }
        return ImageFormat.UNKNOWN;
    }
}
