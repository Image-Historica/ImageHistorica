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
package com.imagehistorica.common.menu.embody;

import com.imagehistorica.common.state.EmbodyState;
import com.imagehistorica.controller.resources.Rsc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class EmbodyProcessTask extends Task<ObservableList<String>> {

    private EmbodyProcess process;
    private EmbodyState status;
    private int pathLength;
    private List<String> exceptions = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(EmbodyProcessTask.class);

    EmbodyProcessTask(EmbodyProcess process, EmbodyState status, int pathLength) {
        this.process = process;
        this.status = status;
        this.pathLength = pathLength;
    }

    @Override
    protected ObservableList<String> call() {
        ObservableList<String> paths = FXCollections.<String>observableArrayList();
        int processedNum = 0;
        boolean isFinished = false;
        paths.add(Rsc.get("common_menu_EPT_message_1"));
        while (true) {
            logger.debug("ProcessedNum: {}", processedNum);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                if (this.isCancelled()) {
                    break;
                }
            }

            while (status.isInterrupted()) {
                logger.debug("Waiting due to interrupted...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            }

            if (status.peekPathQueue() != null) {
                while (true) {
                    String[] processedPath = status.pollPathQueue();
                    if (processedPath != null) {
                        processedNum++;
                        String src = "\n" + processedPath[0] + ", src=" + processedPath[1];
                        String dst = "\n" + processedPath[0] + ", dst=" + processedPath[2];
                        paths.add(src + dst);
                        updateValue(FXCollections.<String>unmodifiableObservableList(paths));
                    } else {
                        break;
                    }
                }
            }

            if (status.peekExQueue() != null) {
                while (true) {
                    String[] ex = status.pollExQueue();
                    if (ex != null) {
                        processedNum++;
                        if (ex[3] != null) {
                            String message = ex[3] + "\n";
                            String src = ex[0] + ", src=" + ex[1] + "\n";
                            String dst = ex[0] + ", dst=" + ex[2] + "\n\n";
                            exceptions.add(message + src + dst);
                        } else {
                            String message = Rsc.get("common_menu_EPT_message_2");
                            String src = ex[0] + ", src=" + ex[1] + "\n";
                            String dst = ex[0] + ", dst=" + ex[2] + "\n\n";
                            exceptions.add(message + src + dst);
                        }
                    } else {
                        break;
                    }
                }
            }

            updateProgress(processedNum, pathLength);

            if (processedNum == pathLength) {
                isFinished = true;
            }

            if (this.isCancelled() || isFinished) {
                break;
            }
        }

        return paths;
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage(Rsc.get("taskCancelled"));
        process.getException().clear();
        exceptions.clear();
    }

    @Override
    protected void failed() {
        super.failed();
        updateMessage(Rsc.get("taskFailed"));
        display();
    }

    @Override
    public void succeeded() {
        super.succeeded();
        updateMessage(Rsc.get("taskSucceeded"));
        display();
    }

    private void display() {
        String ex = "" + process.getException().getText();
        ex = exceptions.stream().map((s) -> s).reduce(ex, String::concat);
        process.getException().setText(ex);
        exceptions.clear();
    }
}
