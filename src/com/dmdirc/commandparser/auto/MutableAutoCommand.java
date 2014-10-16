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

package com.dmdirc.commandparser.auto;

import com.google.common.base.Optional;

/**
 * Mutable version of an {@link AutoCommand}.
 */
public class MutableAutoCommand extends AutoCommand {

    public MutableAutoCommand(final Optional<String> server,
            final Optional<String> network,
            final Optional<String> profile,
            final String response) {
        super(server, network, profile, response);
    }

    public void setServer(final Optional<String> server) {
        this.server = server;
    }

    public void setNetwork(final Optional<String> network) {
        this.network = network;
    }

    public void setProfile(final Optional<String> profile) {
        this.profile = profile;
    }

    /**
     * Sets the response to this auto command. This can be multi line and should not include
     * command characters before the commands.
     *
     * @param response New response to execute
     */
    public void setResponse(final String response) {
        this.response = response;
    }
}
