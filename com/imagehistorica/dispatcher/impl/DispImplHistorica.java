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

package com.imagehistorica.dispatcher.impl;

import com.imagehistorica.databases.model.Historica;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public abstract class DispImplHistorica {

    public List<Historica> historicas = new ArrayList<>();

    public abstract void implOpen();

    public abstract void implProcess();

    public abstract void implClose();

    public List<Historica> getHistoricas() {
        return historicas;
    }
}
