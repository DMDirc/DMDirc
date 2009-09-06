/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.common.CallbackNotFoundException;
import com.dmdirc.parser.interfaces.callbacks.DataInListener;
import com.dmdirc.parser.interfaces.callbacks.DataOutListener;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.InputWindow;

import java.io.Serializable;

/**
 * Handles the raw window (which shows the user raw data being sent and
 * received to/from the server).
 * @author chris
 */
public final class Raw extends WritableFrameContainer implements DataInListener,
        DataOutListener, Serializable {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** The server object that's being monitored. */
    private Server server;

    /** An InputWindow used for displaying the raw data.*/
    private InputWindow window;

    /**
     * Creates a new instance of Raw.
     *
     * @param newServer the server to monitor
     * @param commandParser Command parser to use
     */
    public Raw(final Server newServer, final CommandParser commandParser) {
        super("raw", "Raw", newServer.getConfigManager());

        this.server = newServer;

        window = Main.getUI().getInputWindow(this, commandParser);
        WindowManager.addWindow(server.getFrame(), window);
        window.setTitle("(Raw log)");
        window.getInputHandler().setTabCompleter(server.getTabCompleter());

        window.open();
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
        // 1: Make the window non-visible
        window.setVisible(false);

        // 2: Remove any callbacks or listeners
        if (server != null && server.getParser() != null) {
            server.getParser().getCallbackManager().delAllCallback(this);
        }

        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing

        // 5: Inform any parents that the window is closing
        server.delRaw();

        // 6: Remove the window from the window manager
        WindowManager.removeWindow(window);

        // 7: Remove any references to the window and parents
        window = null;
        server = null;
    }

    /** {@inheritDoc} */
    @Override
    public InputWindow getFrame() {
        return window;
    }

    /** {@inheritDoc} */
    @Override
    public void onDataIn(final Parser tParser, final String sData) {
        addLine("rawIn", sData);
    }

    /** {@inheritDoc} */
    @Override
    public void onDataOut(final Parser tParser, final String sData,
            final boolean bFromParser) {
        addLine("rawOut", sData);
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
            server.sendLine(window.getTranscoder().encode(line));
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxLineLength() {
        return server.getMaxLineLength();
    }

}
