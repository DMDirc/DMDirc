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

package com.dmdirc.addons.ui_swing.components.statusbar;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.interfaces.StatusBarComponent;
import com.dmdirc.ui.interfaces.StatusMessageNotifier;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * Message label handles showing messages in the status bar.
 */
public class MessageLabel extends JLabel implements StatusBarComponent, 
        MouseListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Default status bar message. */
    private static final String DEFAULT_MESSAGE = "Ready.";
    /** current status bar message notifier. */
    private transient StatusMessageNotifier messageNotifier;
    /** Timer to clear the message. */
    private transient TimerTask messageTimer;

    /**
     * Instantiates a new message label.
     */
    public MessageLabel() {
        super();
        setText(DEFAULT_MESSAGE);
        setBorder(BorderFactory.createEtchedBorder());
        addMouseListener(this);
    }

    public void setMessage(final String newMessage) {
        setMessage(newMessage, (StatusMessageNotifier) null);
    }

    public void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier) {
        setMessage(null, newMessage, newNotifier);
    }

    public void setMessage(final String iconType, final String newMessage) {
        setMessage(iconType, newMessage, null);
    }

    public void setMessage(final String iconType, final String newMessage,
            final StatusMessageNotifier newNotifier) {
        final int timeout =
                IdentityManager.getGlobalConfig().
                getOptionInt("statusBar", "messageDisplayLength");
        setMessage(iconType, newMessage, newNotifier, timeout);
    }

    public void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier, final int timeout) {
        setMessage(null, newMessage, newNotifier, timeout);
    }

    public synchronized void setMessage(final String iconType, final String newMessage,
            final StatusMessageNotifier newNotifier, final int timeout) {
        final Icon icon;
        if (iconType == null) {
            icon = null;
        } else {
            icon = IconManager.getIconManager().getIcon(iconType);
        }
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setIcon(icon);
                    setText(UIUtilities.clipStringifNeeded(MessageLabel.this,
                            newMessage, getWidth()));
                messageNotifier = newNotifier;

                if (messageTimer != null &&
                        (System.currentTimeMillis() -
                        messageTimer.scheduledExecutionTime()) <= 0) {
                    messageTimer.cancel();
                }

                if (!DEFAULT_MESSAGE.equals(newMessage)) {
                    messageTimer = new TimerTask() {

                        /** {@inheritDoc} */
                        @Override
                        public void run() {
                            clearMessage();
                        }
                    };
                    new Timer("SwingStatusBar messageTimer").schedule(messageTimer,
                            new Date(System.currentTimeMillis() + 250 +
                            timeout * 1000L));
                }
            }
        });

    }

    /**
     * Removes the message from the status bar.
     */
    public void clearMessage() {
        setMessage(DEFAULT_MESSAGE);
        messageNotifier = null;
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (messageNotifier != null) {
            messageNotifier.clickReceived(e.getButton(), e.getClickCount());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }
}
