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

import com.imagehistorica.dispatcher.impl.DispImplRequestMap;
import com.imagehistorica.databases.model.Request;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class DispRequestMap implements Disp, Callable<Map<String, List<Request>>> {

    protected DispImplRequestMap impl;

    public DispRequestMap(DispImplRequestMap impl) {
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
    public final Map<String, List<Request>> call() {
        open();
        process();
        close();

        return impl.getRequests();
    }
}
