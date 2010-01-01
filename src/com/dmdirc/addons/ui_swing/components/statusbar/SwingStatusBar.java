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

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.StatusBar;
import com.dmdirc.ui.interfaces.StatusBarComponent;
import com.dmdirc.ui.interfaces.StatusMessageNotifier;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/** Status bar, shows message and info on the gui. */
public final class SwingStatusBar extends JPanel implements StatusBar {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    /** message label. */
    private final MessageLabel messageLabel;
    /** error panel. */
    private final ErrorPanel errorPanel;
    /** update label. */
    private final UpdaterLabel updateLabel;
    /** Invite label. */
    private final InviteLabel inviteLabel;
    /** Swing controller. */
    private SwingController controller;
    /** Main frame. */
    private MainFrame mainFrame;

    /**
     * Creates a new instance of SwingStatusBar.
     * 
     * @param controller Swing controller
     * @param mainFrame Main frame
     */
    public SwingStatusBar(final SwingController controller, final MainFrame mainFrame) {
        super();
        
        this.controller = controller;
        this.mainFrame = mainFrame;

        messageLabel = new MessageLabel();
        errorPanel = new ErrorPanel(controller, mainFrame, this);
        updateLabel = new UpdaterLabel(mainFrame);
        inviteLabel = new InviteLabel(mainFrame);

        setLayout(new MigLayout("fill, ins 0, hidemode 3"));

        add(messageLabel, "growx, pushx, sgy components, hmax 20, hmin 20");
        add(updateLabel, "sgy components, hmax 20, hmin 20, wmin 20, shrink 0");
        add(errorPanel, "sgy components, hmax 20, hmin 20, wmin 20, shrink 0");
        add(inviteLabel, "sgy components, hmax 20, hmin 20, wmin 20, shrink 0");
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(final String newMessage) {
        messageLabel.setMessage(newMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier) {
        messageLabel.setMessage(newMessage, newNotifier);
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(final String iconType, final String newMessage) {
        messageLabel.setMessage(iconType, newMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(final String iconType, final String newMessage,
            final StatusMessageNotifier newNotifier) {
        messageLabel.setMessage(iconType, newMessage, newNotifier);
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier, final int timeout) {
        messageLabel.setMessage(newMessage, newNotifier, timeout);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setMessage(final String iconType, 
            final String newMessage,
            final StatusMessageNotifier newNotifier, final int timeout) {
        messageLabel.setMessage(iconType, newMessage, newNotifier, timeout);
    }

    /** {@inheritDoc} */
    @Override
    public void clearMessage() {
        messageLabel.clearMessage();
    }

    /** {@inheritDoc} */
    @Override
    public void addComponent(final StatusBarComponent component) {
        if (!(component instanceof Component)) {
            Logger.appError(ErrorLevel.HIGH, "Error adding status bar component", 
                    new IllegalArgumentException("Component must be an " +
                    "instance of java.awt.component"));
            return;
        }
        if (!Arrays.asList(getComponents()).contains(component)) {
            SwingUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    remove(updateLabel);
                    remove(errorPanel);
                    remove(inviteLabel);
                    add((Component) component,
                            "sgy components, hmax 20, hmin 20, wmin 20, shrink 0");
                    add(updateLabel,
                            "sgy components, hmax 20, hmin 20, wmin 20, shrink 0");
                    add(inviteLabel,
                            "sgy components, hmax 20, hmin 20, wmin 20, shrink 0");
                    add(errorPanel,
                            "sgy components, hmax 20, hmin 20, wmin 20, shrink 0");
                    validate();
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeComponent(final StatusBarComponent component) {
        if (!(component instanceof Component)) {
            Logger.appError(ErrorLevel.HIGH, "Error adding status bar component", 
                    new IllegalArgumentException("Component must be an " +
                    "instance of java.awt.component"));
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                remove((Component) component);
                validate();
            }
            });
    }

    /**
     * Returns the message label for this status bar. This is intended to be used
     * for advanced plugins that wish to do compliated things with messages.
     *
     * @return Message label component
     */
    public MessageLabel getMessageComponent() {
        return messageLabel;
    }
}
