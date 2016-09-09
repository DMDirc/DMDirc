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

package com.dmdirc.commandparser.auto;


import com.google.auto.value.AutoValue;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Describes a command that is executed automatically in response to either the client opening, or a
 * server connecting.
 */
@AutoValue
public abstract class AutoCommand {

    AutoCommand() {
    }

    public abstract Optional<String> getServer();

    public abstract Optional<String> getNetwork();

    public abstract Optional<String> getProfile();

    public abstract String getResponse();

    public abstract AutoCommandType getType();

    public static AutoCommand create(
            final Optional<String> server,
            final Optional<String> network,
            final Optional<String> profile,
            @Nonnull final String response) {
        final AutoCommandType type;
        if (!server.isPresent() && !network.isPresent()) {
            type = AutoCommandType.GLOBAL;
        } else if (server.isPresent() && !network.isPresent()) {
            type = AutoCommandType.SERVER;
        } else if (!server.isPresent()) {
            type = AutoCommandType.NETWORK;
        } else {
            type = AutoCommandType.UNKNOWN;
        }

        return new AutoValue_AutoCommand(server, network, profile, response, type);
    }

}
