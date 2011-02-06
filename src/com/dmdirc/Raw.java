/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.common.CallbackNotFoundException;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.DataInListener;
import com.dmdirc.parser.interfaces.callbacks.DataOutListener;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.Arrays;
import java.util.Date;

/**
 * Handles the raw window (which shows the user raw data being sent and
 * received to/from the server).
 */
public final class Raw extends WritableFrameContainer
        implements DataInListener, DataOutListener {

    /** The server object that's being monitored. */
    private Server server;

    /**
     * Creates a new instance of Raw.
     *
     * @param newServer the server to monitor
     */
    public Raw(final Server newServer) {
        super("raw", "Raw", "(Raw log)", InputWindow.class,
                newServer.getConfigManager(), new ServerCommandParser(),
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                WindowComponent.INPUTFIELD.getIdentifier()));

        this.server = newServer;

        getCommandParser().setOwner(server);

        WindowManager.addWindow(server, this);
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

    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        // 2: Remove any callbacks or listeners
        if (server.getParser() != null) {
            server.getParser().getCallbackManager().delAllCallback(this);
        }

        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing

        // 5: Inform any parents that the window is closing
        server.delRaw();
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosed() {
        // 7: Remove any references to the window and parents
        server = null;
    }

    /** {@inheritDoc} */
    @Override
    public void onDataIn(final Parser parser, final Date date, final String data) {
        addLine("rawIn", data);
    }

    /** {@inheritDoc} */
    @Override
    public void onDataOut(final Parser parser, final Date date, final String data,
            final boolean fromParser) {
        addLine("rawOut", data);
    }

    /** {@inheritDoc} */
    @Override
    public Server getServer() {
        return server;
    }

    /** {@inheritDoc} */
    @Override
    public void sendLine(final String line) {
        if (!line.isEmpty()) {
            sendLine(line);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxLineLength() {
        return server.getMaxLineLength();
    }

    /** {@inheritDoc} */
    @Override
    public TabCompleter getTabCompleter() {
        return server.getTabCompleter();
    }

}
