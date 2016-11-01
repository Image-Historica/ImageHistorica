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

package com.imagehistorica.controller;

import com.imagehistorica.util.view.CommonAlert;
import javafx.scene.Scene;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public abstract class Factory {

    protected Scene scene = null;

    public static Factory getController(String classname) {
        Factory factory = null;
        try {
            factory = (Factory) Class.forName(classname).newInstance();
        } catch (Exception e) {
            CommonAlert.getErrorLog(e);
        }
        return factory;
    }

    protected abstract Scene createLayout(ImageHistoricaController controller);

    protected abstract void attachEvents();

    protected abstract void resizeStage();

    protected void setScene(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return this.scene;
    }
    
    public abstract Scene restoreScene();

    public abstract ControllerType getControllerType();

    public abstract void adjustLayout();

}
