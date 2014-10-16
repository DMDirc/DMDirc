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

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

import java.util.Objects;

/**
 * Describes a command that is executed automatically in response to either the client opening, or a
 * server connecting.
 */
public class AutoCommand {

    /** The name of the server for connection events. */
    protected Optional<String> server;
    /** The name of the network for connection events. */
    protected Optional<String> network;
    /** The name of the profile for connection events. */
    protected Optional<String> profile;
    /** The commands to execute. */
    protected String response;

    public AutoCommand(
            final Optional<String> server,
            final Optional<String> network,
            final Optional<String> profile,
            final String response) {
        this.server = server;
        this.network = network;
        this.profile = profile;
        this.response = response;
    }

    public Optional<String> getServer() {
        return server;
    }

    public Optional<String> getNetwork() {
        return network;
    }

    public Optional<String> getProfile() {
        return profile;
    }

    public String getResponse() {
        return response;
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final AutoCommand command = (AutoCommand) object;
        return  Objects.equals(server, command.getServer())
                && Objects.equals(network, command.getNetwork())
                && Objects.equals(profile, command.getProfile())
                && Objects.equals(response, command.getResponse());

    }

    @Override
    public int hashCode() {
        return Objects.hash(server, network, profile, response);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Connection", server)
                .add("Network", network)
                .add("Profile", profile)
                .add("Response", response)
                .toString();
    }

}
