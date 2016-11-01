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

import com.imagehistorica.Config;
import com.imagehistorica.util.view.CommonAlert;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */
public class Downloader extends Thread {

    private final List<String> requiredLibs;
    private final String downloadUrl;

    public Downloader(String downloadUrl, List<String> requiredLibs) {
        this.downloadUrl = downloadUrl;
        this.requiredLibs = requiredLibs;
    }

    @Override
    public void run() {
        CommonAlert.getDebugLog("Start Downloader...");
        HashChecker hc = new HashChecker();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            File lib_tmp = new File(Config.getInitialDirectory() + "/lib_tmp/");
            if (!lib_tmp.exists()) {
                if (!lib_tmp.mkdir()) {
                    throw new IOException();
                }
            }
            for (String requiredLib : requiredLibs) {
                if (requiredLib != null || !requiredLib.isEmpty()) {
                    HttpGet get = new HttpGet(downloadUrl + requiredLib);
                    try (CloseableHttpResponse res = httpClient.execute(get)) {
                        if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            CommonAlert.getDebugLog("Response is OK when downloading " + requiredLib);
                            File file = new File(lib_tmp.getAbsolutePath() + "/" + requiredLib);
                            File hash = new File(lib_tmp.getAbsolutePath() + "/" + "md5hash.txt");
                            try (InputStream is = res.getEntity().getContent();
                                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, false));
                                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hash, true), "UTF-8"))) {

                                byte[] b = new byte[4096];
                                int read;
                                while ((read = is.read(b)) != -1) {
                                    bos.write(b, 0, read);
                                }
                                bos.flush();

                                String md5 = hc.createDigest(file);
                                bw.write(requiredLib + "=" + md5);
                                bw.newLine();
                                bw.flush();

                            } catch (NoSuchAlgorithmException e) {
                                CommonAlert.getErrorLog(e);
                            }
                        } else {
                            CommonAlert.getDebugLog("Response is NG when downloading " + requiredLib);
                        }
                    } catch (NoHttpResponseException e) {
                        CommonAlert.getErrorLog(e);
                    }
                }
            }

            File completeFlg = new File(lib_tmp.getAbsolutePath() + "/" + "downloadCmp");
            if (!completeFlg.createNewFile()) {
                FileUtils.deleteQuietly(lib_tmp);
            } else {
                CommonAlert.getDebugLog("Created downloadCmp...");
            }
        } catch (IOException e) {
            CommonAlert.getErrorLog(e);
        }
    }
}
