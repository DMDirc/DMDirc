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

import com.dmdirc.interfaces.EventBus;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.util.io.Downloader;

import javax.inject.Inject;

import static com.dmdirc.ClientModule.GlobalConfig;

/**
 * Factory for {@link com.dmdirc.ui.core.feedback.FeedbackSender}s
 */
public class FeedbackSenderFactory {

    private final AggregateConfigProvider config;
    private final Downloader downloader;
    private final EventBus eventBus;

    @Inject
    public FeedbackSenderFactory(
            @GlobalConfig final AggregateConfigProvider config,
            final Downloader downloader, final EventBus eventBus) {
        this.config = config;
        this.downloader = downloader;
        this.eventBus = eventBus;
    }

    public FeedbackSender getFeedbackSender(
            final String name,
            final String email,
            final String feedback,
            final String version,
            final String serverInfo,
            final String dmdircInfo) {
        return new FeedbackSender(config, downloader, eventBus, name, email, feedback, version,
                serverInfo, dmdircInfo);
    }
}
