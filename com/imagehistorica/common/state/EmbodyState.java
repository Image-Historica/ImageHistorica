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

package com.imagehistorica.common.state;

import com.imagehistorica.util.view.CommonAlert;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class EmbodyState extends State {

    private final ConcurrentLinkedDeque<String[]> pathQueue = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<String[]> exQueue = new ConcurrentLinkedDeque<>();

    private boolean inProgress = false;
    private boolean isInterrupted = false;
    private boolean isCanceled = false;

    public String[] peekPathQueue() {
        return pathQueue.peekFirst();
    }

    public String[] pollPathQueue() {
        return pathQueue.pollFirst();
    }

    public void offerPathQueue(String[] path) {
        pathQueue.offerLast(path);
    }

    public String[] peekExQueue() {
        return exQueue.peekFirst();
    }

    public String[] pollExQueue() {
        return exQueue.pollFirst();
    }

    public void offerExQueue(String[] path) {
        exQueue.offerLast(path);
    }

    public boolean inProgress() {
        return this.inProgress;
    }

    public void setProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isInterrupted() {
        return this.isInterrupted;
    }

    public void setInterrupted(boolean isInterrupted) {
        CommonAlert.getDebugLog("[EmbodyState] Called set interrupted of status...");
        this.isInterrupted = isInterrupted;
    }

    public boolean isCanceled() {
        return this.isCanceled;
    }

    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }
    
    public void clear() {
        pathQueue.clear();
        exQueue.clear();
        inProgress = false;
        isInterrupted = false;
        isCanceled = false;
    }
}
