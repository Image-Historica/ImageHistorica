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

import com.imagehistorica.controller.Factory;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class ResizeStage {

    private static double xOffset;
    private static double yOffset;

    private static boolean isResize = false;
    private static boolean isResizeRange = false;

    private static boolean top = false;
    private static boolean bottom = false;

    private static boolean left = false;
    private static boolean right = false;

    private static boolean topLeft = false;
    private static boolean topRight = false;
    private static boolean bottomLeft = false;
    private static boolean bottomRight = false;

    public static GridPane setResizeStage(Stage stage, Factory factory, GridPane grid) {
        grid.setOnMouseMoved(e -> {
            double x = e.getX();
            double y = e.getY();
            double width = stage.getWidth();
            double height = stage.getHeight();

            isResize = false;
            isResizeRange = false;
            Cursor cursor = Cursor.DEFAULT;

            // bottom
            if (y > (height - 10) && y < (height + 10)) {
                cursor = Cursor.S_RESIZE;

                // right
            } else if (x > (width - 10) && x < (width + 10)) {
                cursor = Cursor.E_RESIZE;

                // left
            } else if (x > -10 && x < 10) {
                cursor = Cursor.W_RESIZE;

                // top
            } else if (y > -10 && y < 10) {
                cursor = Cursor.N_RESIZE;
            }

            if (cursor == Cursor.DEFAULT) {
                grid.setCursor(cursor);
                return;
            }

            // bottomRight
            if (x > (width - 10) && x < (width + 10) && y > (height - 10) && y < (height + 10)) {
                cursor = Cursor.SE_RESIZE;

                // bottomLeft
            } else if (x > -10 && x < 10 && y > (height - 10) && y < (height + 10)) {
                cursor = Cursor.SW_RESIZE;

                // topRight
            } else if (x > (width - 10) && x < (width + 10) && y > -10 && y < 10) {
                cursor = Cursor.NE_RESIZE;

                // topLeft
            } else if (x > -10 && x < 10 && y > -10 && y < 10) {
                cursor = Cursor.NW_RESIZE;
            }

            grid.setCursor(cursor);
            isResizeRange = true;
        });

        grid.setOnMousePressed(e -> {
            if (isResizeRange) {
                isResize = false;

                top = false;
                bottom = false;

                left = false;
                right = false;

                topLeft = false;
                topRight = false;
                bottomLeft = false;
                bottomRight = false;

                double x = e.getX();
                double y = e.getY();
                double width = stage.getWidth();
                double height = stage.getHeight();

                // bottomRight
                if (x > (width - 10) && x < (width + 10) && y > (height - 10) && y < (height + 10)) {
                    isResize = true;
                    bottomRight = true;

                    // bottomLeft
                } else if (x > -10 && x < 10 && y > (height - 10) && y < (height + 10)) {
                    isResize = true;
                    bottomLeft = true;

                    // topRight
                } else if (x > (width - 10) && x < (width + 10) && y > -10 && y < 10) {
                    isResize = true;
                    topRight = true;

                    // topLeft
                } else if (x > -10 && x < 10 && y > -10 && y < 10) {
                    isResize = true;
                    topLeft = true;

                    // top
                } else if (y > -10 && y < 10) {
                    isResize = true;
                    top = true;

                    // bottom
                } else if (y > (height - 10) && y < (height + 10)) {
                    isResize = true;
                    bottom = true;

                    // left
                } else if (x > -10 && x < 10) {
                    isResize = true;
                    left = true;

                    // right
                } else if (x > (width - 10) && x < (width + 10)) {
                    isResize = true;
                    right = true;

                } else {
                    isResize = false;
                    xOffset = e.getSceneX();
                    yOffset = e.getSceneY();
                }
            }
        });

        grid.setOnMouseDragged(e -> {
            if (isResizeRange) {
                if (isResize == false) {
                    stage.setX(e.getScreenX() - xOffset);
                    stage.setY(e.getScreenY() - yOffset);
                } else {
                    if (bottomRight) {
                        stage.setWidth(e.getX());
                        stage.setHeight(e.getY());

                    } else if (bottomLeft) {
                        stage.setWidth(stage.getX() - e.getScreenX() + stage.getWidth());
                        stage.setX(e.getScreenX());
                        stage.setHeight(e.getY());

                    } else if (topRight) {
                        stage.setWidth(e.getX());
                        stage.setHeight(stage.getY() - e.getScreenY() + stage.getHeight());
                        stage.setY(e.getScreenY());

                    } else if (topLeft) {
                        stage.setWidth(stage.getX() - e.getScreenX() + stage.getWidth());
                        stage.setX(e.getScreenX());
                        stage.setHeight(stage.getY() - e.getScreenY() + stage.getHeight());
                        stage.setY(e.getScreenY());

                    } else if (top) {
                        stage.setHeight(stage.getY() - e.getScreenY() + stage.getHeight());
                        stage.setY(e.getScreenY());

                    } else if (bottom) {
                        stage.setHeight(e.getY());

                    } else if (right) {
                        stage.setWidth(e.getX());

                    } else if (left) {
                        stage.setWidth(stage.getX() - e.getScreenX() + stage.getWidth());
                        stage.setX(e.getScreenX());
                    }
                }
            }
        });

        grid.setOnMouseReleased(e -> {
            if (isResize) {
                factory.adjustLayout();
            }
        });

        return grid;
    }

    public static Scene setResizeStage(Stage stage, Scene scene) {
        scene.setOnMouseMoved(e -> {
            double x = e.getX();
            double y = e.getY();
            double width = stage.getWidth();
            double height = stage.getHeight();

            isResize = false;
            isResizeRange = false;
            Cursor cursor = Cursor.DEFAULT;

            // bottom
            if (y > (height - 10) && y < (height + 10)) {
                cursor = Cursor.S_RESIZE;

                // right
            } else if (x > (width - 10) && x < (width + 10)) {
                cursor = Cursor.E_RESIZE;

                // left
            } else if (x > -10 && x < 10) {
                cursor = Cursor.W_RESIZE;

                // top
            } else if (y > -10 && y < 10) {
                cursor = Cursor.N_RESIZE;
            }

            if (cursor == Cursor.DEFAULT) {
                scene.setCursor(cursor);
                return;
            }

            // bottomRight
            if (x > (width - 10) && x < (width + 10) && y > (height - 10) && y < (height + 10)) {
                cursor = Cursor.SE_RESIZE;

                // bottomLeft
            } else if (x > -10 && x < 10 && y > (height - 10) && y < (height + 10)) {
                cursor = Cursor.SW_RESIZE;

                // topRight
            } else if (x > (width - 10) && x < (width + 10) && y > -10 && y < 10) {
                cursor = Cursor.NE_RESIZE;

                // topLeft
            } else if (x > -10 && x < 10 && y > -10 && y < 10) {
                cursor = Cursor.NW_RESIZE;
            }

            scene.setCursor(cursor);
            isResizeRange = true;

        });

        scene.setOnMousePressed(e -> {
            if (isResizeRange) {
                isResize = false;

                top = false;
                bottom = false;

                left = false;
                right = false;

                topLeft = false;
                topRight = false;
                bottomLeft = false;
                bottomRight = false;

                double x = e.getX();
                double y = e.getY();
                double width = stage.getWidth();
                double height = stage.getHeight();

                // bottomRight
                if (x > (width - 10) && x < (width + 10) && y > (height - 10) && y < (height + 10)) {
                    isResize = true;
                    bottomRight = true;

                    // bottomLeft
                } else if (x > -10 && x < 10 && y > (height - 10) && y < (height + 10)) {
                    isResize = true;
                    bottomLeft = true;

                    // topRight
                } else if (x > (width - 10) && x < (width + 10) && y > -10 && y < 10) {
                    isResize = true;
                    topRight = true;

                    // topLeft
                } else if (x > -10 && x < 10 && y > -10 && y < 10) {
                    isResize = true;
                    topLeft = true;

                    // top
                } else if (y > -10 && y < 10) {
                    isResize = true;
                    top = true;

                    // bottom
                } else if (y > (height - 10) && y < (height + 10)) {
                    isResize = true;
                    bottom = true;

                    // left
                } else if (x > -10 && x < 10) {
                    isResize = true;
                    left = true;

                    // right
                } else if (x > (width - 10) && x < (width + 10)) {
                    isResize = true;
                    right = true;

                } else {
                    isResize = false;
                    xOffset = e.getSceneX();
                    yOffset = e.getSceneY();
                }
            }
        });

        scene.setOnMouseDragged(e -> {
            if (isResizeRange) {
                if (isResize == false) {
                    stage.setX(e.getScreenX() - xOffset);
                    stage.setY(e.getScreenY() - yOffset);
                } else {
                    if (bottomRight) {
                        stage.setWidth(e.getX());
                        stage.setHeight(e.getY());

                    } else if (bottomLeft) {
                        stage.setWidth(stage.getX() - e.getScreenX() + stage.getWidth());
                        stage.setX(e.getScreenX());
                        stage.setHeight(e.getY());

                    } else if (topRight) {
                        stage.setWidth(e.getX());
                        stage.setHeight(stage.getY() - e.getScreenY() + stage.getHeight());
                        stage.setY(e.getScreenY());

                    } else if (topLeft) {
                        stage.setWidth(stage.getX() - e.getScreenX() + stage.getWidth());
                        stage.setX(e.getScreenX());
                        stage.setHeight(stage.getY() - e.getScreenY() + stage.getHeight());
                        stage.setY(e.getScreenY());

                    } else if (top) {
                        stage.setHeight(stage.getY() - e.getScreenY() + stage.getHeight());
                        stage.setY(e.getScreenY());

                    } else if (bottom) {
                        stage.setHeight(e.getY());

                    } else if (right) {
                        stage.setWidth(e.getX());

                    } else if (left) {
                        stage.setWidth(stage.getX() - e.getScreenX() + stage.getWidth());
                        stage.setX(e.getScreenX());
                    }
                }
            }
        });

        return scene;
    }
}
