/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.dcc;

import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.addons.ui_swing.components.frames.InputTextFrame;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.InputWindow;

import com.dmdirc.util.ReturnableThread;
import java.awt.Container;

import javax.swing.JPopupMenu;

/**
 * This class links DCC objects to a window.
 *
 * @author Shane 'Dataforce' McCormack
 */
public abstract class DCCFrame extends WritableFrameContainer {

    /**
     * Empty Frame.
     */
    private class EmptyFrame extends InputTextFrame {

        /** A version number for this class. */
        private static final long serialVersionUID = 200711271;

        /**
         * Creates a new instance of EmptyFrame.
         *
         * @param owner The frame container that owns this frame
         */
        public EmptyFrame(final WritableFrameContainer owner) {
            super(owner, (SwingController) Main.getUI());
            setTextPane(null);
            pack();
        }

        /**
         * Retrieves the command Parser for this input window.
         *
         * @return This window's command parser
         */
        @Override
        public final CommandParser getCommandParser() {
            return GlobalCommandParser.getGlobalCommandParser();
        }

        /** {@inheritDoc} */
        @Override
        public PopupType getNicknamePopupType() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public PopupType getChannelPopupType() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public PopupType getHyperlinkPopupType() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public PopupType getNormalPopupType() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public void addCustomPopupItems(final JPopupMenu popupMenu) {
            //Add no custom popup items
        }

    }

    /** The Window we're using. */
    protected InputWindow myWindow = null;

    /** The dcc plugin that owns this frame */
    protected final DCCPlugin plugin;

    /** The Window we're using. */
    private boolean windowClosing = false;

    /**
     * Creates a new instance of DCCFrame with an empty window.
     *
     * @param plugin The DCCPlugin that owns this frame
     * @param title The title of this window
     * @param icon The icon to use
     */
    public DCCFrame(final DCCPlugin plugin, final String title, final String icon) {
        this(plugin, title, icon, true);
    }

    /**
     * Creates a new instance of DCCFrame.
     *
     * @param plugin The DCCPlugin that owns this frame
     * @param title The title of this window
     * @param defaultWindow Create default (empty) window. (non-default = chat frame)
     * @param icon The icon to use
     */
    public DCCFrame(final DCCPlugin plugin, final String title, final String icon, final boolean defaultWindow) {
        super(icon, title, IdentityManager.getGlobalConfig());
        this.plugin = plugin;

        if (defaultWindow) {
            myWindow = UIUtilities.invokeAndWait(new ReturnableThread<EmptyFrame>() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    final EmptyFrame frame = new EmptyFrame(DCCFrame.this);
                    frame.setTitle(title);
                    setObject(frame);
                }

            });
        }
    }

    /**
     * Sends a line of text to this container's source.
     *
     * @param line The line to be sent
     */
    @Override
    public void sendLine(final String line) {
    }

    /**
     * Returns the maximum length that a line passed to sendLine() should be,
     * in order to prevent it being truncated or causing protocol violations.
     *
     * @return The maximum line length for this container
     */
    @Override
    public int getMaxLineLength() {
        return 512;
    }

    /**
     * Returns the internal frame associated with this object.
     *
     * @return The internal frame associated with this object
     */
    @Override
    public InputWindow getFrame() {
        return myWindow;
    }

    /**
     * Returns the content pane of the internal frame associated with this object.
     *
     * @return The content pane of the internal frame associated with this object
     */
    public Container getContentPane() {
        return ((TextFrame) getFrame()).getContentPane();
    }

    /**
     * Returns the server instance associated with this container.
     *
     * @return the associated server connection
     */
    @Override
    public Server getServer() {
        return null;
    }

    /**
     * Is the window closing?
     *
     * @return True if windowClosing has been called.
     */
    public final boolean isWindowClosing() {
        return windowClosing;
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        windowClosing = true;

        // 1: Make the window non-visible
        myWindow.setVisible(false);

        // 2: Remove any callbacks or listeners
        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing

        // 5: Inform any parents that the window is closing
        plugin.delWindow(this);

        // 6: Remove the window from the window manager
        WindowManager.removeWindow(myWindow);

        // 7: Remove any references to the window and parents
        myWindow = null; // NOPMD
    }

}
