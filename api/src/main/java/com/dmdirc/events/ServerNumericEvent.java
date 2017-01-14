/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

package com.dmdirc.events;

import com.dmdirc.interfaces.Connection;

import java.time.LocalDateTime;

/**
 * Event raised when a server numeric occurs.
 */
public class ServerNumericEvent extends ServerDisplayableEvent {

    private final int numeric;
    private final String[] args;

    public ServerNumericEvent(final LocalDateTime timestamp, final Connection server,
            final int numeric, final String[] args) {
        super(timestamp, server);
        this.numeric = numeric;
        this.args = args;
    }

    public ServerNumericEvent(final Connection server, final int numeric, final String[] args) {
        super(server);
        this.numeric = numeric;
        this.args = args;
    }

    public int getNumeric() {
        return numeric;
    }

    public String[] getArgs() {
        return args;
    }

}
