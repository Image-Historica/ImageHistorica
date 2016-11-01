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

package com.imagehistorica.util.view;

import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.controller.resources.Rsc;

import javafx.scene.control.TreeItem;

import java.util.Comparator;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class HistoricaComparator implements Comparator<TreeItem<HistoricaProperty>> {

    final String other = Rsc.get("other");
    
    @Override
    public int compare(TreeItem<HistoricaProperty> t1, TreeItem<HistoricaProperty> t2) {
        String imageName_1 = t1.getValue().getImageName();
        if (imageName_1.equals(other)) {
            return 1;
        }
        String imageName_2 = t2.getValue().getImageName();
        if (imageName_2.equals(other)) {
            return -1;
        }

        int numOfChildren_1 = t1.getValue().getNumOfChildren().intValue();
        int numOfChildren_2 = t2.getValue().getNumOfChildren().intValue();
        if (numOfChildren_1 < numOfChildren_2) {
            return 1;
        } else if (numOfChildren_1 == numOfChildren_2) {
            return 0;
        } else {
            return -1;
        }
    }
}
