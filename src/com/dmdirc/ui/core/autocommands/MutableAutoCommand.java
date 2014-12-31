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

package com.dmdirc.ui.core.autocommands;

import com.dmdirc.commandparser.auto.AutoCommand;
import com.dmdirc.commandparser.auto.AutoCommandType;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Mutable version of an {@link AutoCommand}.
 */
public class MutableAutoCommand {

    private Optional<String> server;
    private Optional<String> network;
    private Optional<String> profile;
    private String response;
    private AutoCommandType type;

    public MutableAutoCommand(@Nonnull final AutoCommand command) {
        this(command.getServer(),
                command.getNetwork(),
                command.getProfile(),
                command.getResponse(),
                command.getType());
    }

    public MutableAutoCommand(
            final Optional<String> server,
            final Optional<String> network,
            final Optional<String> profile,
            @Nonnull final String response,
            final AutoCommandType type) {
        this.server = server;
        this.network = network;
        this.profile = profile;
        this.response = response;
        this.type = type;
    }

    public Optional<String> getServer() {
        return server;
    }

    public void setServer(final Optional<String> server) {
        this.server = server;
    }

    public Optional<String> getNetwork() {
        return network;
    }

    public void setNetwork(final Optional<String> network) {
        this.network = network;
    }

    public Optional<String> getProfile() {
        return profile;
    }

    public void setProfile(final Optional<String> profile) {
        this.profile = profile;
    }

    @Nonnull
    public String getResponse() {
        return response;
    }

    public void setResponse(@Nonnull final String response) {
        this.response = response;
    }

    public AutoCommandType getType() {
        return type;
    }

    public void setType(final AutoCommandType type) {
        this.type = type;
    }

    public AutoCommand getAutoCommand() {
        return AutoCommand.create(server, network, profile, response);
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final MutableAutoCommand command = (MutableAutoCommand) object;
        return Objects.equals(server, command.getServer()) &&
                Objects.equals(network, command.getNetwork()) &&
                Objects.equals(profile, command.getProfile()) &&
                Objects.equals(response, command.getResponse());
    }

    public boolean equalsAutoCommand(final AutoCommand command) {
        return command != null && Objects.equals(server, command.getServer()) &&
                Objects.equals(network, command.getNetwork()) &&
                Objects.equals(profile, command.getProfile()) &&
                Objects.equals(response, command.getResponse());
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
