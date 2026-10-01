package com.gempukku.lotro.async;

import org.w3c.dom.Document;

import java.io.File;
import java.util.Map;

public interface ResponseWriter {
    void writeError(int status);
    void writeError(int status, Map<String, String> headers);

    void writeFile(File file, Map<String, String> headers);

    void writeHtmlResponse(String html);
    void writeJsonResponse(String json);

    /**
     * A JSON body under a status other than 200, for the endpoints whose contract gives errors a readable body
     * (writeError sends an empty body and puts the message in a header).
     */
    void writeJsonResponse(int status, String json);

    void writeByteResponse(byte[] bytes, Map<? extends CharSequence, String> headers);

    void sendOK();
    void sendXmlOK();
    void sendJsonOK();
    void writeXmlResponse(Document document);

    void writeXmlResponse(Document document, Map<? extends CharSequence, String> addHeaders);
}
