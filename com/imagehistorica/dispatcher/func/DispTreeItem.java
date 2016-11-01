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

package com.imagehistorica.dispatcher.func;

import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.dispatcher.impl.DispImplTreeItem;

import java.util.concurrent.Callable;
import javafx.scene.control.TreeItem;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class DispTreeItem implements Disp, Callable<TreeItem<HistoricaProperty>> {

    protected DispImplTreeItem impl;

    public DispTreeItem(DispImplTreeItem impl) {
        this.impl = impl;
    }

    public void open() {
        impl.implOpen();
    }

    public void process() {
        impl.implProcess();
    }

    public void close() {
        impl.implClose();
    }

    @Override
    public final TreeItem<HistoricaProperty> call() {
        open();
        process();
        close();
        
        return impl.getTreeItem();
    }
}
