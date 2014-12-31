/*
 * Copyright (c) 2006-2015 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.ui.core.feedback;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.StatusBarMessageEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.StatusMessage;
import com.dmdirc.util.io.Downloader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends feedback back to the developers.
 */
public class FeedbackSender implements Runnable {

    private final Map<String, String> postData;
    private final DMDircMBassador eventBus;
    private final AggregateConfigProvider config;
    private final Downloader downloader;

    public FeedbackSender(
            final AggregateConfigProvider config,
            final Downloader downloader,
            final DMDircMBassador eventBus,
            final String name,
            final String email,
            final String feedback,
            final String version,
            final String serverInfo,
            final String dmdircInfo) {
        this.downloader = downloader;
        this.config = config;
        this.eventBus = eventBus;
        this.postData = new HashMap<>(6);

        if (!name.isEmpty()) {
            postData.put("name", name);
        }
        if (!email.isEmpty()) {
            postData.put("email", email);
        }
        if (!feedback.isEmpty()) {
            postData.put("feedback", feedback);
        }
        if (!version.isEmpty()) {
            postData.put("version", version);
        }
        if (!serverInfo.isEmpty()) {
            postData.put("serverInfo", serverInfo);
        }
        if (!dmdircInfo.isEmpty()) {
            postData.put("dmdircInfo", dmdircInfo);
        }
    }

    /**
     * Sends the error data to the server appending returned information to the global error
     * variable.
     *
     * @param postData Feedback data to send
     */
    private String sendData(final Map<String, String> postData) {
        try {
            final List<String> response = downloader.getPage(
                    "https://feedback.dmdirc.com/dialog/", postData);
            if (response.isEmpty()) {
                return "Feedback failure: Unknown response from the server.";
            } else {
                return response.get(0);
            }
        } catch (IOException ex) {
            return "Feedback failure: " + ex.getMessage();
        }
    }

    @Override
    public void run() {
        eventBus.publishAsync(new StatusBarMessageEvent(
                new StatusMessage(sendData(postData), config)));
    }

}
