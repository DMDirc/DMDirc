/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.swing.components;

import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.interfaces.InputField;
import com.dmdirc.ui.interfaces.InputWindow;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.text.JTextComponent;

/**
 * Swing input handler.
 */
public class SwingInputHandler extends InputHandler implements KeyListener {

    /**
     * Creates a new instance of InputHandler. Adds listeners to the target
     * that we need to operate.
     *
     * @param target The text field this input handler is dealing with.
     * @param commandParser The command parser to use for this text field.
     * @param parentWindow The window that owns this input handler
     */
    public SwingInputHandler(final InputField target,
            final CommandParser commandParser, final InputWindow parentWindow) {
        super(target, commandParser, parentWindow);
    }

    /** {@inheritDoc} */
    @Override
    protected void addUpHandler() {
        JTextComponent localTarget = null;
        if (target instanceof JTextComponent) {
            localTarget = (JTextComponent) target;
        } else if (target instanceof SwingInputField) {
            localTarget = ((SwingInputField) target).getTextField();
        }

        localTarget.getActionMap().put("upArrow", new AbstractAction() {

            /**
             * A version number for this class. It should be changed whenever the class
             * structure is changed (or anything else that would prevent serialized
             * objects being unserialized with the new class).
             */
            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent e) {
                doBufferUp();
            }
        });
        localTarget.getInputMap(JComponent.WHEN_FOCUSED).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "upArrow");
    }

    /** {@inheritDoc} */
    @Override
    protected void addDownHandler() {
        JTextComponent localTarget = null;
        if (target instanceof JTextComponent) {
            localTarget = (JTextComponent) target;
        } else if (target instanceof SwingInputField) {
            localTarget = ((SwingInputField) target).getTextField();
        }
        localTarget.getActionMap().put("downArrow", new AbstractAction() {

            /**
             * A version number for this class. It should be changed whenever the class
             * structure is changed (or anything else that would prevent serialized
             * objects being unserialized with the new class).
             */
            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent e) {
                doBufferDown();
            }
        });
        localTarget.getInputMap(JComponent.WHEN_FOCUSED).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "downArrow");
    }

    /** {@inheritDoc} */
    @Override
    protected void addTabHandler() {
        JTextComponent localTarget = null;
        if (target instanceof JTextComponent) {
            localTarget = (JTextComponent) target;
        } else if (target instanceof SwingInputField) {
            localTarget = ((SwingInputField) target).getTextField();
        }
        localTarget.getActionMap().put("tabPressed", new AbstractAction() {

            /**
             * A version number for this class. It should be changed whenever the class
             * structure is changed (or anything else that would prevent serialized
             * objects being unserialized with the new class).
             */
            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent e) {
                doTabCompletion();
            }
        });
        localTarget.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "tabPressed");
    }

    /** {@inheritDoc} */
    @Override
    protected void addEnterHandler() {
        JTextComponent localTarget = null;
        if (target instanceof JTextComponent) {
            localTarget = (JTextComponent) target;
        } else if (target instanceof SwingInputField) {
            localTarget = ((SwingInputField) target).getTextField();
        }
        localTarget.getActionMap().put("enterButton", new AbstractAction() {

            /**
             * A version number for this class. It should be changed whenever the class
             * structure is changed (or anything else that would prevent serialized
             * objects being unserialized with the new class).
             */
            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new SwingWorker() {

                        /** {@inheritDoc} */
                        @Override
                        protected Object doInBackground() throws Exception {
                            enterPressed(target.getText());
                            return null;
                        }
                    }.execute();
                } catch (Exception ex) {
                //Crap
                }
            }
        });
        localTarget.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterButton");
    }

    /** {@inheritDoc} */
    @Override
    protected void addKeyHandler() {
        target.addKeyListener(this);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Key event
     */
    @Override
    public void keyTyped(final KeyEvent e) {
    //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Key event
     */
    @Override
    public void keyPressed(final KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_TAB && e.getKeyCode() !=
                KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_DOWN) {
            handleKeyPressed(e.getKeyCode(), e.isShiftDown(), e.isControlDown());
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Key event
     */
    @Override
    public void keyReleased(final KeyEvent e) {
    //Ignore
    }
}
