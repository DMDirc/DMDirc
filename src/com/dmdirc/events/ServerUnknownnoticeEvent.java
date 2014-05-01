/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

/**
 * Fired when an unknown notice is received.
 */
public class ServerUnknownnoticeEvent extends ServerDisplayableEvent {

    private final String source;
    private final String target;
    private final String message;

    public ServerUnknownnoticeEvent(final long timestamp, final Connection connection,
            final String source, final String target, final String message) {
        super(timestamp, connection);
        this.source = source;
        this.target = target;
        this.message = message;
    }

    public ServerUnknownnoticeEvent(final Connection connection, final String source,
            final String target, final String message) {
        super(connection);
        this.source = source;
        this.target = target;
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getMessage() {
        return message;
    }

}
