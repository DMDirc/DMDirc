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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.actions.CopyAction;
import com.dmdirc.addons.ui_swing.actions.CutAction;
import com.dmdirc.addons.ui_swing.actions.PasteAction;

import com.dmdirc.interfaces.ConfigChangeListener;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.awt.event.WindowEvent;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Custom event queue to add common functionality to certain components and 
 * monitor the EDT for long running tasks.  The monitoring code was taken from
 * TracingEventQueue by Kirill Grouchnikov (http://today.java.net/lpt/a/433).
 */
public final class DMDircEventQueue extends EventQueue implements
        ConfigChangeListener {

    /** Swing Controller. */
    private SwingController controller;
    /** Tracing thread. */
    private TracingEventQueueThread tracingThread;

    /** 
     * Instantiates the DMDircEventQueue. 
     * 
     * @param controller Swing controller
     */
    public DMDircEventQueue(final SwingController controller) {
        super();

        this.controller = controller;
        checkTracing();
        //TODO add config listener
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatchEvent(final AWTEvent event) {
        if (tracingThread == null) {
            super.dispatchEvent(event);
        } else {
            this.tracingThread.eventDispatched(event);
            super.dispatchEvent(event);
            this.tracingThread.eventProcessed(event);
        }

        if (event instanceof MouseEvent) {
            handleMouseEvent((MouseEvent) event);
        } else if (event instanceof KeyEvent) {
            handleKeyEvent((KeyEvent) event);
        } else if (event instanceof WindowEvent) {
            handleWindowEvent((WindowEvent) event);
        }
    }

    private void checkTracing() {
        //TODO check value
        final boolean tracing = false;
        if (tracing) {
            this.tracingThread = new TracingEventQueueThread(500);
            this.tracingThread.start();
        } else {
            tracingThread = null;
        }
    }

    /**
     * Handles key events.
     * 
     * @param ke Key event
     */
    private void handleKeyEvent(final KeyEvent ke) {
        switch (ke.getKeyChar()) {
            case KeyEvent.VK_F1:
            //Fallthrough
            case KeyEvent.VK_F2:
            //Fallthrough
            case KeyEvent.VK_F3:
            //Fallthrough
            case KeyEvent.VK_F4:
            //Fallthrough
            case KeyEvent.VK_F5:
            //Fallthrough
            case KeyEvent.VK_F6:
            //Fallthrough
            case KeyEvent.VK_F7:
            //Fallthrough
            case KeyEvent.VK_F8:
            //Fallthrough
            case KeyEvent.VK_F9:
            //Fallthrough
            case KeyEvent.VK_F10:
            //Fallthrough
            case KeyEvent.VK_F11:
            //Fallthrough
            case KeyEvent.VK_F12:
            //Fallthrough
            case KeyEvent.VK_F13:
            //Fallthrough
            case KeyEvent.VK_F14:
            //Fallthrough
            case KeyEvent.VK_F15:
            //Fallthrough
            case KeyEvent.VK_F16:
            //Fallthrough
            case KeyEvent.VK_F17:
            //Fallthrough
            case KeyEvent.VK_F18:
            //Fallthrough
            case KeyEvent.VK_F19:
            //Fallthrough
            case KeyEvent.VK_F20:
            //Fallthrough
            case KeyEvent.VK_F21:
            //Fallthrough
            case KeyEvent.VK_F22:
            //Fallthrough
            case KeyEvent.VK_F23:
            //Fallthrough
            case KeyEvent.VK_F24:
                ActionManager.processEvent(CoreActionType.CLIENT_KEY_PRESSED,
                        null, KeyStroke.getKeyStroke(ke.getKeyChar(),
                        ke.getModifiers()));
                break;
            default:
                if (ke.getModifiers() != 0) {
                    ActionManager.processEvent(CoreActionType.CLIENT_KEY_PRESSED,
                            null, KeyStroke.getKeyStroke(ke.getKeyChar(),
                            ke.getModifiers()));
                }
                break;
        }
    }

    /**
     * Handles mouse events.
     * 
     * @param me Mouse event
     */
    private void handleMouseEvent(final MouseEvent me) {
        if (!me.isPopupTrigger()) {
            return;
        }

        if (me.getComponent() == null) {
            return;
        }

        final Component comp = SwingUtilities.getDeepestComponentAt(
                me.getComponent(), me.getX(), me.getY());

        if (!(comp instanceof JTextComponent)) {
            return;
        }

        if (MenuSelectionManager.defaultManager().getSelectedPath().length > 0) {
            return;
        }

        final JTextComponent tc = (JTextComponent) comp;
        final JPopupMenu menu = new JPopupMenu();
        menu.add(new CutAction(tc));
        menu.add(new CopyAction(tc));
        menu.add(new PasteAction(tc));

        final Point pt = SwingUtilities.convertPoint(me.getComponent(),
                me.getPoint(), tc);
        menu.show(tc, pt.x, pt.y);
    }

    /**
     * Handles window events
     * 
     * @param windowEvent Window event
     */
    private void handleWindowEvent(final WindowEvent we) {
        if (we.getSource() instanceof Window) {
            if (controller.hasMainFrame()) {
                if (we.getID() == WindowEvent.WINDOW_OPENED) {
                    controller.addTopLevelWindow((Window) we.getSource());
                } else if (we.getID() == WindowEvent.WINDOW_CLOSED) {
                    controller.delTopLevelWindow((Window) we.getSource());
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        checkTracing();
    }
}
