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
package com.imagehistorica.common.view;

import com.imagehistorica.util.model.HistoricaProperty;
import com.imagehistorica.util.model.HistoricaType;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.util.view.CommonAlert;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TreeTableCell;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class HistoricaCellGraph extends GridPane {

    private final TreeTableCell cell;
    private final TextField textField = new TextField();
    private TextFocusHandler focusHandler;

    private String origText;

    private final Logger logger = LoggerFactory.getLogger(HistoricaCellGraph.class);

    HistoricaCellGraph(TreeTableCell cell) {
        this.cell = cell;
        this.textField.setOnKeyPressed(new TextFieldKeyPressedHandler());
        add(this.textField, 0, 0);
        logger.debug("textField: {}", this.textField.getText());
    }

    void startEdit() {
        final TextField textField = this.textField;
        this.focusHandler = new TextFocusHandler();
        textField.focusedProperty().addListener(this.focusHandler);
        try {
            origText = this.cell.getItem().toString();
            textField.setText(origText);
        } catch (NullPointerException e) {
            logger.debug("startEdit: {}", e.getMessage());
        }

        logger.debug("startEdit()...textField: {}", textField.getText());
        Platform.runLater(() -> {
            textField.selectAll();
            textField.requestFocus();
        });
    }

    @SuppressWarnings("unchecked")
    private void endingEdit(boolean isContinued) {
        logger.debug("Start endingEdit()...");
        // Edit continues if the text is empty.
        final String currentText = textField.getText();
        if (currentText.isEmpty()) {
            logger.debug("End edit due to empty...");
            if (isContinued) {
                return;
            } else {
                removeFocusHandler();
                cell.cancelEdit();
                return;
            }
        }

        if (currentText.equals(origText) == true) {
            logger.debug("End edit due to no change...");
            removeFocusHandler();
            cell.cancelEdit();
            return;
        }

        if (currentText.matches(".*[/:;*?<>|\"\\\\].*")) {
            logger.debug("End edit due to containing a forbidden character...");
            if (isContinued) {
                Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("common_view_HCG_title"), Rsc.get("common_view_HCG_header_1"), Rsc.get("common_view_HCG_content_1"));
                alert.showAndWait();
                return;
            } else {
                removeFocusHandler();
                cell.cancelEdit();
                return;
            }
        }

        final TreeItem<HistoricaProperty> treeItem = cell.getTreeTableRow().getTreeItem();
        final Iterator<TreeItem<HistoricaProperty>> children = treeItem.getParent().getChildren().iterator();
        while (children.hasNext()) {
            final TreeItem<HistoricaProperty> child = children.next();
            if (treeItem != child && currentText.equals(child.getValue().getImageName())) {
                if (child.getValue().getType() != HistoricaType.LEAF) {
                    logger.debug("End edit due to existing of same name path...");
                    if (isContinued) {
                        Alert alert = CommonAlert.makeAlert(AlertType.INFORMATION, Rsc.get("common_view_HCG_title"), Rsc.get("common_view_HCG_header_2"), null);
                        alert.showAndWait();
                        return;
                    } else {
                        removeFocusHandler();
                        cell.cancelEdit();
                        return;
                    }
                }
            }
        }

        TreeTableColumnUtil.renameTreeItem(treeItem, currentText);

        removeFocusHandler();
        cell.commitEdit(currentText);
        logger.debug("End edit after commit: {}", currentText);
    }

    private void removeFocusHandler() {
        this.textField.focusedProperty().removeListener(this.focusHandler);
        this.focusHandler = null;
    }

    final class TextFieldKeyPressedHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent e) {
            switch (e.getCode()) {
                case ENTER:
                    HistoricaCellGraph.this.endingEdit(true);
                    break;
                case ESCAPE:
                    HistoricaCellGraph.this.cell.cancelEdit();
                    break;
            }
        }
    }

    final class TextFocusHandler implements ChangeListener<Boolean> {

        @Override
        public void changed(ObservableValue<? extends Boolean> o, Boolean b1, Boolean b2) {
            if (b2 == false) {
                try {
                    HistoricaCellGraph.this.endingEdit(false);
                } catch (Exception e) {
                    logger.debug("TextFocusHandler#changed()...", e);
                }
            }
        }
    }
}
