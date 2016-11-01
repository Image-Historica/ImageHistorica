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
package com.imagehistorica;

import static com.imagehistorica.util.Constants.KEY_PROPS;

import com.imagehistorica.util.view.CommonAlert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class Key extends Properties {

    public static String accessKey;
    private static final Key key = new Key();

    private Key() {
        try (FileInputStream fis = new FileInputStream(KEY_PROPS);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader br = new BufferedReader(isr)) {
            load(br);
            accessKey = this.getProperty("ACCESS_KEY");
        } catch (IOException e) {
            CommonAlert ca = new CommonAlert();
            ca.showIOException(KEY_PROPS);
        }
    }

    public static Key getInstance() {
        return key;
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
    }

    public void storeKey(Properties env, File file) {
        String comment = "This file is to identify your ownership.";
        try (FileOutputStream fos = new FileOutputStream(file);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                BufferedWriter bw = new BufferedWriter(osw)) {
            putAll(env);
            store(bw, comment);
        } catch (IOException e) {
            CommonAlert.getErrorLog(e);
        }
    }
}
