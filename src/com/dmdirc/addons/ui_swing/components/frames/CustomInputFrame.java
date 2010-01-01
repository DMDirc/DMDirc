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
package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.addons.ui_swing.components.SwingInputHandler;

import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

/**
 * A custom frame that includes an input field (for use with writable
 * containers).
 * 
 * @author chris
 */
public class CustomInputFrame extends InputTextFrame {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** The command parser to use. */
    private final CommandParser commandParser;

    /**
     * Creates a new instance of CustomInputFrame.
     * 
     * @param owner The frame container that owns this frame
     * @param commandParser The command parser to use for this frmae
     * @param controller Swing controller
     */
    public CustomInputFrame(final WritableFrameContainer owner,
            final CommandParser commandParser, final SwingController controller) {
        super(owner, controller);

        this.commandParser = commandParser;

        setInputHandler(new SwingInputHandler(getInputField(), commandParser, this));

        initComponents();
    }

    /** {@inheritDoc} */
    @Override
    public final CommandParser getCommandParser() {
        return commandParser;
    }

    /**
     * Initialises components in this frame.
     */
    private void initComponents() {
        setTitle("Custom Input Frame");

        getContentPane().setLayout(new MigLayout("ins 0, fill, hidemode 3, wrap 1"));
        getContentPane().add(getTextPane(), "grow, push");
        getContentPane().add(getSearchBar(), "growx, pushx");
        getContentPane().add(inputPanel, "growx, pushx");

        pack();
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
