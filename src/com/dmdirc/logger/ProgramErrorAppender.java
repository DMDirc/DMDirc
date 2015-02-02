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

package com.dmdirc.logger;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ProgramError;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import static com.dmdirc.util.LogUtils.APP_ERROR;
import static com.dmdirc.util.LogUtils.FATAL_APP_ERROR;
import static com.dmdirc.util.LogUtils.FATAL_USER_ERROR;
import static com.dmdirc.util.LogUtils.USER_ERROR;
import static com.dmdirc.util.LogUtils.getErrorLevel;
import static com.dmdirc.util.LogUtils.getThrowable;

/**
 * Converts log states into {@link ProgramError}s so they can be displayed to the user.
 */
public class ProgramErrorAppender extends AppenderBase<ILoggingEvent> {

    private DMDircMBassador eventBus;

    @Override
    public void start() {
        if (eventBus == null) {
            addError("No eventBus set for the appender named ["+ name +"].");
            return;
        }
        super.start();
    }

    @Override
    protected void append(final ILoggingEvent eventObject) {
        if (eventObject.getMarker() == null) {
            return;
        }
        if (eventObject.getMarker() == APP_ERROR) {
            eventBus.publish(new AppErrorEvent(
                    getErrorLevel(eventObject.getLevel()),
                    getThrowable(eventObject), eventObject.getFormattedMessage(), ""));
        } else if (eventObject.getMarker() == USER_ERROR) {
            eventBus.publish(new UserErrorEvent(getErrorLevel(eventObject.getLevel()),
                    getThrowable(eventObject), eventObject.getFormattedMessage(), ""));
        } else if (eventObject.getMarker() == FATAL_APP_ERROR) {
            eventBus.publish(new AppErrorEvent(ErrorLevel.FATAL, getThrowable(eventObject),
                    eventObject.getFormattedMessage(), ""));
        } else if (eventObject.getMarker() == FATAL_USER_ERROR) {
            eventBus.publish(new UserErrorEvent(ErrorLevel.FATAL, getThrowable(eventObject),
                    eventObject.getFormattedMessage(), ""));
        }
    }

    public void setEventBus(final DMDircMBassador eventBus) {
        this.eventBus = eventBus;
    }
}
