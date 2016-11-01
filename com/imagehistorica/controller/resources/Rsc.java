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
package com.imagehistorica.controller.resources;

import static com.imagehistorica.util.Constants.RESOURCE_BUNDLE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class Rsc extends ResourceBundle {

    private static final ResourceBundle.Control UTF8_ENCODING_CONTROL = new ResourceBundle.Control() {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(loader.getResourceAsStream(resourceName), "UTF-8"))) {
                return new PropertyResourceBundle(br);
            }
        }
    };

    public static final ResourceBundle rb = ResourceBundle.getBundle(RESOURCE_BUNDLE, UTF8_ENCODING_CONTROL);

    public static String get(String key) {
        return rb.getString(key);
    }

    @Override
    protected Object handleGetObject(String key) {
        return null;
    }

    @Override
    public Enumeration<String> getKeys() {
        return null;
    }
}
