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
import com.dmdirc.parser.interfaces.ClientInfo;

/**
 * Fired when receiving a server wallops.
 */
public class ServerWallopsEvent extends ServerDisplayableEvent {

    private final ClientInfo client;
    private final String message;

    public ServerWallopsEvent(final long timestamp, final Connection connection,
            final ClientInfo client, final String message) {
        super(timestamp, connection);
        this.client = client;
        this.message = message;
    }

    public ServerWallopsEvent(final Connection connection, final ClientInfo client,
            final String message) {
        super(connection);
        this.client = client;
        this.message = message;
    }

    public ClientInfo getClient() {
        return client;
    }

    public String getMessage() {
        return message;
    }

}
