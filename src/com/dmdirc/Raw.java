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

package com.dmdirc;

import com.dmdirc.commandparser.parsers.ServerCommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.parser.common.CallbackNotFoundException;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.DataInListener;
import com.dmdirc.parser.interfaces.callbacks.DataOutListener;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.messages.ColourManagerFactory;
import com.dmdirc.util.URLBuilder;

import java.util.Arrays;
import java.util.Date;

import javax.annotation.Nonnull;

/**
 * Handles the raw window (which shows the user raw data being sent and received to/from the
 * server).
 */
public class Raw extends FrameContainer implements DataInListener, DataOutListener {

    /** The server object that's being monitored. */
    private final Server server;

    /**
     * Creates a new instance of Raw.
     *
     * @param newServer          the server to monitor
     * @param commandController  The controller to load commands from.
     * @param messageSinkManager The sink manager to use to dispatch messages.
     * @param urlBuilder         The URL builder to use when finding icons.
     */
    public Raw(
            final Server newServer,
            final CommandController commandController,
            final MessageSinkManager messageSinkManager,
            final URLBuilder urlBuilder,
            final ColourManagerFactory colourManagerFactory) {
        super(newServer, "raw", "Raw", "(Raw log)", newServer.getConfigManager(),
                colourManagerFactory, urlBuilder,
                new ServerCommandParser(newServer.getConfigManager(), commandController, newServer.
                        getEventBus()),
                newServer.getTabCompleter(),
                messageSinkManager,
                newServer.getEventBus(),
                Arrays.asList(
                        WindowComponent.TEXTAREA.getIdentifier(),
                        WindowComponent.INPUTFIELD.getIdentifier()));

        this.server = newServer;

        getCommandParser().setOwner(server);
    }

    /**
     * Registers the data callbacks for this raw window.
     */
    public void registerCallbacks() {
        try {
            server.getParser().getCallbackManager().addCallback(DataInListener.class, this);
            server.getParser().getCallbackManager().addCallback(DataOutListener.class, this);
        } catch (CallbackNotFoundException ex) {
            Logger.appError(ErrorLevel.HIGH, "Unable to register raw callbacks", ex);
        }
    }

    @Override
    public void close() {
        super.close();

        // Remove any callbacks or listeners
        if (server.getParser() != null) {
            server.getParser().getCallbackManager().delAllCallback(this);
        }

        // Inform any parents that the window is closing
        server.delRaw();
    }

    @Override
    public void onDataIn(final Parser parser, final Date date, final String data) {
        addLine("rawIn", data);
    }

    @Override
    public void onDataOut(final Parser parser, final Date date, final String data,
            final boolean fromParser) {
        addLine("rawOut", data);
    }

    @Nonnull
    @Override
    public Connection getConnection() {
        return server;
    }

    @Override
    public void sendLine(final String line) {
        server.sendLine(line);
    }

    @Override
    public int getMaxLineLength() {
        return server.getMaxLineLength();
    }

}
