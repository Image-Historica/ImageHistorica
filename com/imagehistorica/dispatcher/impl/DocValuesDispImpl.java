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

package com.imagehistorica.dispatcher.impl;

import static com.imagehistorica.util.model.SchemeType.*;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;
import com.imagehistorica.databases.model.Request;
import com.imagehistorica.Config;

import java.awt.color.CMMException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.JCD;
import net.semanticmetadata.lire.imageanalysis.features.global.PHOG;
import org.apache.lucene.document.Document;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kazuhito Kojima, kojima@image-historica.com
 */

public class DocValuesDispImpl extends DispImplRequest {

    private boolean isSkipReq = false;
    private List<Request> assignedReqs;
    private GlobalDocumentBuilder globalDocumentBuilder;
    private BufferedImage img;
    private Document doc;
    private byte imgio;
    private String imageIdentifier;
    private int length = 0;
    private byte[] feature;
    private byte[] sha1;
    private GlobalFeature globalFeature;
    private MessageDigest sha1Digest = null;
    private Path imagePath;

    private List<Request> requests_tmp = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(DocValuesDispImpl.class);

    public DocValuesDispImpl(List<Request> requests) {
        this.assignedReqs = requests;
    }

    @Override
    public void implOpen() {
        if (assignedReqs.isEmpty()) {
            isSkipReq = true;
            return;
        }

        switch (Config.getDefaultFeature()) {
            case "ColorLayout":
                globalDocumentBuilder = new GlobalDocumentBuilder(ColorLayout.class, true);
                globalFeature = new ColorLayout();
                break;
            case "CEDD":
                globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class, true);
                break;
            case "FCTH":
                globalDocumentBuilder = new GlobalDocumentBuilder(FCTH.class, true);
                break;
            case "JCD":
                globalDocumentBuilder = new GlobalDocumentBuilder(JCD.class, true);
                break;
            case "PHOG":
                globalDocumentBuilder = new GlobalDocumentBuilder(PHOG.class, true);
                break;
            default:
                break;
        }

        try {
            sha1Digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Cant find the algorithm...", e);
        }
    }

    @Override
    public void implProcess() {
        if (!isSkipReq) {
            for (Request request : assignedReqs) {
                String realImagePath0 = request.getRealImagePath0();
                byte[] bytes = null;
                if (request.getType() == FILE) {
                    try {
                        imagePath = Paths.get(new URI(realImagePath0));
                    } catch (URISyntaxException ex) {
                        logger.error("", ex);
                    }
                    if (Files.exists(imagePath)) {
                        if (imagePath.toFile().isFile() && imagePath.toFile().canRead()) {
                            try (InputStream is = Files.newInputStream(imagePath)) {
                                length = is.available();
                                bytes = IOUtils.toByteArray(is);
                                sha1 = getSha1(bytes);
                            } catch (IOException ex) {
                                logger.error("", ex);
                            }
                        } else {
                            logger.warn("Can't read the file: {}", imagePath);
                            continue;
                        }
                    } else {
                        logger.warn("The file doesn't exist...: {}", imagePath);
                        continue;
                    }
                } else if (request.getType() == HTTP || request.getType() == HTTPS) {
                    bytes = request.getBinaryImage();
                    length = request.getBinaryImage().length;
                    sha1 = getSha1(bytes);
                }

                try {
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                        img = ImageIO.read(bais);
                        imgio = 0;
                    } catch (IllegalArgumentException e) {
                        logger.info("Call readJAI() type 1... {}, {}", realImagePath0, e.getMessage());
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                            img = readJAI(bais);
                            imgio = 1;
                        } catch (Exception ex) {
                            throw new Exception(ex);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.info("Call readJAI() type 2... {}, {}", realImagePath0, e.getMessage());
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                            img = readJAI(bais);
                            imgio = 2;
                        } catch (Exception ex) {
                            throw new Exception(ex);
                        }

                    } catch (IndexOutOfBoundsException e) {
                        logger.info("Call readJAI() type 3... {}, {}", realImagePath0, e.getMessage());
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                            img = readJAI(bais);
                            imgio = 3;
                        } catch (Exception ex) {
                            throw new Exception(ex);
                        }
                    } catch (CMMException e) {
                        logger.info("Call readJAI() type 4... {}, {}", realImagePath0, e.getMessage());
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                            img = readJAI(bais);
                            imgio = 4;
                        } catch (Exception ex) {
                            throw new Exception(ex);
                        }
                    } catch (IIOException e) {
                        logger.info("IIOException occured. {}, {}", realImagePath0, e.getMessage());
                        imgio = 99;
                        requests.add(makeRequest(request, length, null, sha1, imgio));
                        continue;
                    } catch (Exception e) {
                        throw new Exception(e);
                    }
                } catch (Exception e) {
                    imgio = 101;
                    requests.add(makeRequest(request, length, null, sha1, imgio));
                    continue;
                }

                try {
                    doc = globalDocumentBuilder.createDocument(img, realImagePath0);
                } catch (IllegalArgumentException e) {
                    try {
                        logger.info("Call readJAI() type 5... {}, {}", realImagePath0, e.getMessage());
                        img = null;
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                            img = readJAI(bais);
                            imgio = 3;
                            doc = globalDocumentBuilder.createDocument(img, realImagePath0);
                        } catch (Exception ex) {
                            throw new Exception(ex);
                        }
                    } catch (IllegalArgumentException ex) {
                        logger.info("Try again type 1, but fail... {}", realImagePath0, ex.getMessage());
                        imgio = 98;
                        requests.add(makeRequest(request, length, null, sha1, imgio));
                        continue;
                    } catch (Exception ex) {
                        logger.info("Exception occurred... {}, {}", realImagePath0, ex.getMessage());
                        imgio = 102;
                        requests.add(makeRequest(request, length, null, sha1, imgio));
                        continue;
                    }
                } catch (NullPointerException e) {
                    logger.info("NullPointerException occurred {}, {}", realImagePath0, e.getMessage());
                    try {
                        logger.info("Call readJAI() type 6... {}, {}", realImagePath0, e.getMessage());
                        img = null;
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                            img = readJAI(bais);
                            imgio = 4;
                        } catch (Exception ex) {
                            throw new Exception(ex);
                        }
                        try {
                            doc = globalDocumentBuilder.createDocument(img, realImagePath0);
                        } catch (IllegalArgumentException ex) {
                            logger.info("Try again type 2, but fail. {}", realImagePath0, ex.getMessage());
                            imgio = 95;
                            requests.add(makeRequest(request, length, null, sha1, imgio));
                            continue;
                        } catch (Exception ex) {
                            throw new Exception(ex);
                        }
                    } catch (IllegalArgumentException ex) {
                        logger.info("IllegalArgumentException occurred... {}, {}", realImagePath0, ex.getMessage());
                        imgio = 97;
                        requests.add(makeRequest(request, length, null, sha1, imgio));
                        continue;
                    } catch (NullPointerException ex) {
                        logger.error("NullPointerException occurred again... {}, {}", realImagePath0, ex.getMessage());
                        imgio = 103;
                        requests.add(makeRequest(request, length, null, sha1, imgio));
                        continue;
                    } catch (Exception ex) {
                        logger.info("Exception type 1 occurred... {}, {}", realImagePath0, ex.getMessage());
                        imgio = 96;
                        requests.add(makeRequest(request, length, null, sha1, imgio));
                        continue;
                    }
                } catch (OutOfMemoryError e) {
                    logger.error("", e);
                    imgio = 126;
                    requests.add(makeRequest(request, length, null, sha1, imgio));
                    continue;
                } catch (Exception e) {
                    logger.error("Exception type 2 occurred {}", realImagePath0, e);
                    imgio = 127;
                    requests.add(makeRequest(request, length, null, sha1, imgio));
                    continue;
                }

                imageIdentifier = doc.getField("ImageIdentifier").stringValue();
                feature = doc.getField("CLD").binaryValue().bytes;

                globalFeature.extract(img);
//                BytesRef bytes = new BytesRef(globalFeature.getByteArrayRepresentation());
                logger.debug("imginfo: " + imageIdentifier);
                logger.debug("cld    : " + new String(Hex.encodeHex(feature)));
                logger.debug("imgio  : " + imgio);
                requests.add(makeRequest(request, length, feature, sha1, imgio));

                imagePath = null;
                img = null;
                doc = null;
            }
        }
    }

    @Override
    public void implClose() {
        if (!isSkipReq) {
            logger.debug("requests size: {}", requests.size());
            for (Request request : requests) {
                logger.debug("[Request] {}", request.getRealImagePath0());
                request.setBinaryImage(null);
            }
        }
    }

    private BufferedImage readJAI(InputStream is) throws Exception {
        SeekableStream ss = null;
        RenderedOp ro = null;
        BufferedImage bi = null;
        try {
            ss = getSeekableStream(is);
            ro = JAI.create("stream", ss);
            ss.close();
            ss = null;
            bi = ro.getAsBufferedImage();
            ro = null;
        } finally {
            if (ss != null) {
                ss.close();
            }
            ss = null;
            ro = null;
        }
        return bi;
    }

    private SeekableStream getSeekableStream(InputStream stream) throws Exception {
        if (stream instanceof SeekableStream) {
            return (SeekableStream) stream;
        } else {
            return new MemoryCacheSeekableStream(stream);
        }
    }

    private byte[] getSha1(byte[] image) {
        sha1Digest.update(image);
        byte[] sha1Hash = sha1Digest.digest();
        sha1Digest.reset();
        return sha1Hash;
    }

    private Request makeRequest(Request request, int length, byte[] feature, byte[] sha1, byte imgio) {
        request.setLength(length);
        request.setFeature(feature);
        request.setSha1(sha1);
        request.setImgio(imgio);
        return request;
    }

}
