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

package uk.org.ownage.dmdirc.ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;

import uk.org.ownage.dmdirc.ui.interfaces.StatusErrorNotifier;
import uk.org.ownage.dmdirc.ui.interfaces.StatusMessageNotifier;

/**
 * Status bar, shows message and info on the gui.
 */
public final class StatusBar extends JPanel implements MouseListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** message label. */
    private JLabel messageLabel;
    
    /** icon label. */
    private JLabel iconLabel;
    
    /** current status bar message notifier. */
    private transient StatusMessageNotifier messageNotifier;
    
    /** current status bar error notifier. */
    private transient StatusErrorNotifier errorNotifier;
    
    /** non error state image icon. */
    private ImageIcon normalIcon;
    
    /** Creates a new instance of StatusBar. */
    public StatusBar() {
        super();
        final BorderLayout layout = new BorderLayout(5, 5);
        
        setBorder(new EmptyBorder(0, 5, 5, 5));
        
        messageLabel = new JLabel();
        iconLabel = new JLabel();
        
        messageLabel.setBorder(new EtchedBorder());
        iconLabel.setBorder(new EtchedBorder());
        
        messageLabel.addMouseListener(this);
        iconLabel.addMouseListener(this);
        
        this.setLayout(layout);
        
        add(messageLabel, BorderLayout.CENTER);
        add(iconLabel, BorderLayout.LINE_END);
        
        setPreferredSize(new Dimension(Short.MAX_VALUE, 26));
        iconLabel.setPreferredSize(new Dimension(21, 26));
        
        normalIcon = new ImageIcon(this.getClass()
        .getClassLoader().getResource("uk/org/ownage/dmdirc/res/normal.png"));
        
        clearMessage();
        clearError();
    }
    
    /**
     * sets the message in the status bar with a specified callback event.
     *
     * @param newMessage Message to display
     * @param newNotifier status message notifier to be notified for events on
     * this message
     */
    public void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier) {
        messageLabel.setText(newMessage);
        messageNotifier = newNotifier;
        int displayLength = 5000;
        if (Config.hasOption("statusBar", "messageDisplayLength")) {
            try {
                displayLength = Integer.parseInt(Config.getOption("statusBar", "messageDisplayLength"));
            } catch (NumberFormatException e) {
                Logger.error(ErrorLevel.WARNING, e);
            }
        }
        new Timer().schedule(new TimerTask() {
            public void run() {
                clearError();
            }
        }, new Date(System.currentTimeMillis() + displayLength));
    }
    
    /**
     * sets the message in the status bar.
     *
     * @param newMessage Message to display
     */
    public void setMessage(final String newMessage) {
        setMessage(newMessage, null);
    }
    
    /**
     * Removes the message from the status bar.
     */
    public void clearMessage() {
        setMessage("Ready.");
    }
    
    /**
     * sets the icon in the status bar with a specified callback event.
     *
     * @param newIcon Icon to display
     * @param newNotifier status error notifier to be notified for events on
     * this error
     */
    public void setError(final ImageIcon newIcon,
            final StatusErrorNotifier newNotifier) {
        iconLabel.setIcon(newIcon);
        errorNotifier = newNotifier;
        int displayLength = 10000;
        if (Config.hasOption("statusBar", "errorDisplayLength")) {
            try {
                displayLength = Integer.parseInt(Config.getOption("statusBar", "errorDisplayLength"));
            } catch (NumberFormatException e) {
                Logger.error(ErrorLevel.WARNING, e);
            }
        }
        new Timer().schedule(new TimerTask() {
            public void run() {
                setError(normalIcon);
            }
        }, new Date(System.currentTimeMillis() + displayLength));
    }
    
    /**
     * sets the icon in the status bar.
     *
     * @param newIcon Icon to display
     */
    public void setError(final ImageIcon newIcon) {
        setError(newIcon, null);
    }
    
    /**
     * Removes the error state from the status bar.
     */
    public void clearError() {
        setError(normalIcon);
    }
    
    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param mouseEvent mouse event
     */
    public void mousePressed(final MouseEvent mouseEvent) {
    }
    
    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param mouseEvent mouse event
     */
    public void mouseReleased(final MouseEvent mouseEvent) {
    }
    
    /**
     * Invoked when the mouse enters a component.
     *
     * @param mouseEvent mouse event
     */
    public void mouseEntered(final MouseEvent mouseEvent) {
    }
    
    /**
     * Invoked when the mouse exits a component.
     *
     * @param mouseEvent mouse event
     */
    public void mouseExited(final MouseEvent mouseEvent) {
    }
    
    /**
     * Invoked when the mouse button has been clicked (pressed and released)
     * on a component.
     *
     * @param mouseEvent mouse event
     */
    public void mouseClicked(final MouseEvent mouseEvent) {
        if (mouseEvent.getSource() == messageLabel && messageNotifier != null) {
            messageNotifier.clickReceived(mouseEvent);
        } else if (mouseEvent.getSource() == iconLabel && errorNotifier != null) {
            errorNotifier.clickReceived(mouseEvent);
        }
    }
}
