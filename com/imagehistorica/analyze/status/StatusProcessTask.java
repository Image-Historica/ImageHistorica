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
package com.imagehistorica.analyze.status;

import com.imagehistorica.common.state.AnalyzeState;
import com.imagehistorica.databases.Backend;
import com.imagehistorica.controller.resources.Rsc;
import com.imagehistorica.databases.model.Historica;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class StatusProcessTask extends Task<ObservableList<String>> {

    private final AnalyzeState state = AnalyzeState.getInstance();
    private final CopyOnWriteArrayList<Historica> historicas;
    private final StatusHistorica analyze;
    private final StatusException exception;
    private final long queueLength;
    private final List<String> historicaPaths = new ArrayList<>();
    private final List<String> exceptions = new ArrayList<>();

    private final ObservableList<String> realImagePaths = FXCollections.<String>observableArrayList();
    private final List<Historica> tmpHistoricas = new ArrayList<>();
    private int i = 1;
    private boolean isCompleted = false;
    private boolean isFinished = false;

    private final Logger logger = LoggerFactory.getLogger(StatusProcessTask.class);

    public StatusProcessTask(StatusHistorica analyze, StatusException exception, long queueLength) {
        this.analyze = analyze;
        this.exception = exception;
        this.queueLength = queueLength;
        this.historicas = state.getHistoricas();
        realImagePaths.add(Rsc.get("status_SPT_real"));
        historicaPaths.add(Rsc.get("status_SPT_historica"));
        updateTitle(Rsc.get("status_SPT_title"));
    }

    @Override
    public ObservableList<String> call() {
        int procNum = 0;
        int lastProcNum = 0;
        int cost = 0;
        while (true) {
            logger.debug("ProcessedNum: {}", procNum);
            logger.debug("queueLength: {}", queueLength);
            logger.debug("getProcessedReqs(): {}", Backend.getProcessedReqs());

            procNum = Backend.getProcessedNum();
            if (procNum == lastProcNum) {
                cost++;
                if (cost > 120) {
                    logger.info("Task is cancelled because no process seems to be done more than 1 minute.");
                    state.offerExceptions(Rsc.get("socketTimeout"));
                    doTask();
                    this.cancel();
                    break;
                }
            } else {
                cost = 0;
            }
            lastProcNum = procNum;

            while (state.isSuspended()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    logger.debug("", ex);
                }
            }

            if (state.isSkipReq()) {
                doTask();
                this.cancel();
                break;
            }

            doTask();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                if (this.isCancelled()) {
                    doTask();
                    break;
                }
            }

            if (this.isCancelled() || (isFinished && isCompleted)) {
                doTask();
                break;
            }

            if (state.isUpdated() && historicas.isEmpty()) {
                isCompleted = true;
            }

            if (procNum == queueLength) {
                isFinished = true;
            }

            updateProgress(procNum, queueLength);
        }

        historicas.addAll(tmpHistoricas);

        return realImagePaths;
    }

    @Override
    public void cancelled() {
        super.cancelled();
        updateMessage(Rsc.get("taskCancelled"));
        display();
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
        String historicaPath = "";
        historicaPath = historicaPaths.stream().map((s) -> s).reduce(historicaPath, String::concat);
        analyze.getValue().setText(historicaPath);

        String ex = "" + exception.getValue().getText();
        ex = exceptions.stream().map((s) -> s).reduce(ex, String::concat);
        exception.getValue().setText(ex);
    }

    private void doTask() {
        if (!historicas.isEmpty()) {
            int size = historicas.size();
            for (int index = 0; index < size; index++) {
                String realImagePath = state.getRealImagePath(historicas.get(index).getHistoricaId());
                String historicaPath = state.getHistoricaPath(historicas.get(index).getHistoricaId());
                if (realImagePath != null) {
                    realImagePath = "\n" + i + ": " + realImagePath;
                    realImagePaths.add(realImagePath);
                    updateValue(FXCollections.<String>unmodifiableObservableList(realImagePaths));
                }
                if (historicaPath != null) {
                    historicaPath = "\n" + i + ": " + historicaPath;
                    historicaPaths.add(historicaPath);
                }
                i++;
            }
            for (int num = 0; num < size; num++) {
                tmpHistoricas.add(historicas.get(0));
                historicas.remove(0);
            }
        }

        if (state.peekExceptions() != null) {
            while (true) {
                String ex = state.pollExceptions();
                if (ex != null) {
                    if (!exceptions.contains(ex)) {
                        exceptions.add(ex);
                    }
                    if (ex.contains("failed to respond")) {
                        this.cancel();
                    }
                } else {
                    break;
                }
            }
        }
    }
}
