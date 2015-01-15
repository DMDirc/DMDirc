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

package com.dmdirc;

import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.DMDircEvent;
import com.dmdirc.logger.ErrorLevel;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;

/**
 * Generified MBassador.
 */
public class DMDircMBassador extends MBassador<DMDircEvent> {

    public DMDircMBassador() {
        super(new BusConfiguration().addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default(1, 1)).addFeature(
                        Feature.AsynchronousMessageDispatch.Default()
                                .setNumberOfMessageDispatchers(1)));
        setupErrorHandler();
    }

    @SuppressWarnings("TypeMayBeWeakened")
    public DMDircMBassador(final BusConfiguration configuration) {
        super(configuration);
        setupErrorHandler();
    }

    private void setupErrorHandler() {
        addErrorHandler(e -> {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            final Throwable error = e.getCause().getCause();
            publish(new AppErrorEvent(ErrorLevel.HIGH, error.getCause(), error.getMessage(), ""));
        });
    }
}
