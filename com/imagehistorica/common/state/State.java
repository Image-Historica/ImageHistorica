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

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class State {

    private ConcurrentLinkedDeque<String> exceptions = new ConcurrentLinkedDeque<>();

    private boolean isSkipReq = false;
    private boolean hasResponse = true;

    public String peekExceptions() {
        return this.exceptions.peekFirst();
    }

    public String pollExceptions() {
        return this.exceptions.pollFirst();
    }

    public void offerExceptions(String exceptions) {
        this.exceptions.offerLast(exceptions);
    }

    public boolean isSkipReq() {
        return isSkipReq;
    }

    public void setSkipReq(boolean isSkipReq) {
        this.isSkipReq = isSkipReq;
    }

    public boolean hasResponse() {
        return this.hasResponse;
    }

    public void setHasResponse(boolean hasResponse) {
        this.hasResponse = hasResponse;
    }
}
