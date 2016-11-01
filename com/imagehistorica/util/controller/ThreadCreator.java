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
package com.imagehistorica.util.controller;

import com.imagehistorica.databases.Backend;
import com.imagehistorica.common.menu.embody.EmbodyProcessAsyncTask;
import com.imagehistorica.Config;
import com.imagehistorica.common.menu.imports.ImportAsyncTask;
import com.imagehistorica.search.SearchImage;
import com.imagehistorica.util.view.CommonAlert;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class ThreadCreator {

    public void createHistoricas() {
        CommonAlert.getDebugLog("[ThreadCreator] Called createHistoricas()...");
        for (int i = 0; i < Config.getNumOfThreads(); i++) {
            new Thread() {
                @Override
                public void run() {
                    Backend b = new Backend();
                    b.createHistoricas();
                }
            }.start();
        }
    }

    public void createImagesMap() {
        CommonAlert.getDebugLog("[ThreadCreator] Called createImagesMap()...");
        for (int i = 0; i < Config.getNumOfThreads(); i++) {
            new Thread() {
                @Override
                public void run() {
                    Backend b = new Backend();
                    b.createImagesMap();
                }
            }.start();
        }
    }

    public void createSearches(SearchImage searchImage) {
        CommonAlert.getDebugLog("[ThreadCreator] Called createSearches()...");
        for (int i = 0; i < 50; i++) {
            new Thread() {
                @Override
                public void run() {
                    Backend b = new Backend();
                    b.createSearches(searchImage);
                }
            }.start();
        }
    }

    public void createTreeItem() {
        CommonAlert.getDebugLog("[ThreadCreator] Called createTreeItem()...");
        for (int i = 0; i < Config.getNumOfThreads(); i++) {
            new Thread() {
                @Override
                public void run() {
                    Backend b = new Backend();
                    b.createTreeItem();
                }
            }.start();
        }
    }

    public void createNewTreeItem() {
        CommonAlert.getDebugLog("[ThreadCreator] Called createNewTreeItem()...");
        for (int i = 0; i < Config.getNumOfThreads(); i++) {
            new Thread() {
                @Override
                public void run() {
                    Backend b = new Backend();
                    b.createNewTreeItem();
                }
            }.start();
        }
    }

    public void startAsyncTask(Object o) {
        CommonAlert.getDebugLog("[ThreadCreator] Called startAsyncTask()...");
        new Thread() {
            @Override
            public void run() {
                if (o instanceof EmbodyProcessAsyncTask) {
                    EmbodyProcessAsyncTask emb = (EmbodyProcessAsyncTask) o;
                    emb.embody();
                } else if (o instanceof ImportAsyncTask) {
                    ImportAsyncTask imp = (ImportAsyncTask) o;
                    imp.importHistorica();
                }
            }
        }.start();
    }
}
