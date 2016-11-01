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
package com.imagehistorica.util;

import com.imagehistorica.util.view.CommonAlert;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.client.utils.DateUtils;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class LmodMaker {

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final Pattern p = Pattern.compile("^(199|20\\d)\\d(0[1-9]|1[012])(0[1-9]|[12]\\d|3[01])$");

    public synchronized int getLmod(final Header lastModified) {
        int lmod = 0;
        if (lastModified != null) {
            String httpDate = lastModified.getValue().trim();
            try {
                if (httpDate != null) {
                    LocalDate date = LocalDate.from(DateUtils.parseDate(httpDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

                    // Check rfc850-date format.
                    int year = date.getYear();
                    // 1958 is the beginning of Internet history.
                    if (year >= 2058) {
                        lmod = verify(dateFormat.format(date)) - 1_000_000;
                    } else {
                        lmod = verify(dateFormat.format(date));
                    }
                }
            } catch (NullPointerException e) {
                CommonAlert.getErrorLog(e);
            }
        }

        if (lmod == 0) {
            lmod = Integer.parseInt(dateFormat.format(LocalDate.now()));
        }

        return lmod;
    }

    public long getLmod(int lastModified) {
        return verify(lastModified).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public int getLmod(long epochMilli) {
        if (epochMilli != 0) {
            return verify((dateFormat.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault()))));
        } else {
            return verify((dateFormat.format(LocalDate.now())));
        }
    }

    private LocalDateTime verify(int lastModified) {
        String lmod = String.valueOf(lastModified);
        if (p.matcher(lmod).matches()) {
            return LocalDateTime.parse(lmod + "000000", dateTimeFormat);
        } else {
            return LocalDateTime.now();
        }
    }

    private int verify(String lastModified) {
        if (p.matcher(lastModified).matches()) {
            return Integer.parseInt(lastModified);
        } else {
            return Integer.parseInt(dateFormat.format(LocalDate.now()));
        }
    }
}
