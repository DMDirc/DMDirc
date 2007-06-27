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

package com.dmdirc.ui.components;

import com.dmdirc.Config;
import com.dmdirc.IconManager;
import com.dmdirc.logger.ErrorListener;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.ui.interfaces.StatusErrorNotifier;
import com.dmdirc.ui.interfaces.StatusMessageNotifier;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;
import com.dmdirc.ui.dialogs.error.ErrorListDialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

/**
 * Status bar, shows message and info on the gui.
 */
public final class StatusBar extends JPanel implements MouseListener,
        ErrorListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
    /** message label. */
    private final JLabel messageLabel;
    
    /** icon label. */
    private final JLabel iconLabel;
    
    /** current status bar message notifier. */
    private transient StatusMessageNotifier messageNotifier;
    
    /** current status bar error notifier. */
    private transient StatusErrorNotifier errorNotifier;
    
    /** non error state image icon. */
    private final Icon normalIcon;
    
    /** Timer to clear the error. */
    private transient TimerTask errorTimer;
    
    /** Timer to clear the message. */
    private transient TimerTask messageTimer;
    
    /** Creates a new instance of StatusBar. */
    public StatusBar() {
        super();
        final SpringLayout layout = new SpringLayout();
        
        setBorder(BorderFactory.createEmptyBorder(0, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER));
        
        messageLabel = new JLabel("Ready");
        iconLabel = new JLabel();
        
        messageLabel.setBorder(BorderFactory.createEtchedBorder());
        iconLabel.setBorder(BorderFactory.createEtchedBorder());
        
        messageLabel.addMouseListener(this);
        iconLabel.addMouseListener(this);
        ErrorManager.getErrorManager().addErrorListener(this);
        
        this.setLayout(layout);
        
        add(messageLabel);
        add(iconLabel);
        
        setPreferredSize(new Dimension(Short.MAX_VALUE, 25));
        setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
        
        normalIcon = IconManager.getIcon("normal");
        
        clearMessage();
        clearError();
        
        layoutBar();
    }
    
    /**
     * Sets the message in the status bar with a specified callback event
     * using the default timeout.
     *
     * @param newMessage Message to display
     * @param newNotifier status message notifier to be notified for events on
     * this message
     */
    public void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier) {
        final int timeout = Config.getOptionInt("statusBar", "messageDisplayLength", 5);
        setMessage(newMessage, newNotifier, timeout);
    }
    
    /**
     * Sets the message in the status bar with a specified callback event for
     * a specified time.
     *
     * @param newMessage Message to display
     * @param newNotifier status message notifier to be notified for events on
     * this message
     * @param timeout message timeout in seconds
     */
    public synchronized void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier, final int timeout) {
        messageLabel.setText(newMessage);
        messageNotifier = newNotifier;
        
        if (messageTimer != null && (System.currentTimeMillis()
        - messageTimer.scheduledExecutionTime()) <= 0) {
            messageTimer.cancel();
        }
        messageTimer = new TimerTask() {
            public void run() {
                clearMessage();
            }
        };
        new Timer().schedule(messageTimer,
                new Date(System.currentTimeMillis() + 250 + timeout * 1000L));
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
    public synchronized void setError(final Icon newIcon,
            final StatusErrorNotifier newNotifier) {
        //Add to list
        iconLabel.setIcon(newIcon);
        errorNotifier = newNotifier;
        if (errorTimer != null && (System.currentTimeMillis()
        - errorTimer.scheduledExecutionTime()) <= 0) {
            errorTimer.cancel();
        }
        
        final int displayLength = Config.getOptionInt("statusBar", "errorDisplayLength", 10000);
        
        errorTimer = new TimerTask() {
            public void run() {
                clearError();
            }
        };
        new Timer().schedule(errorTimer,
                new Date(System.currentTimeMillis() + 250  + displayLength));
    }
    
    /**
     * sets the icon in the status bar.
     *
     * @param newIcon Icon to display
     */
    public void setError(final Icon newIcon) {
        setError(newIcon, null);
    }
    
    /**
     * Removes the error state from the status bar.
     */
    public void clearError() {
        setError(normalIcon);
    }
    
    /** {@inheritDoc} */
    public void errorAdded(final ProgramError error) {
        final Icon icon;
        switch (error.getLevel()) {
            case HIGH:
                icon = IconManager.getIcon("error");
                break;
            case MEDIUM:
                icon = IconManager.getIcon("warning");
                break;
            case LOW:
                icon = IconManager.getIcon("info");
                break;
            default:
                icon = IconManager.getIcon("info");
                break;
        }
        setError(icon);
    }
    
    /** {@inheritDoc} */
    public void errorDeleted(final ProgramError error) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void errorStatusChanged(final ProgramError error) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void mousePressed(final MouseEvent mouseEvent) {
        //Ignore.
    }
    
    /** {@inheritDoc} */
    public void mouseReleased(final MouseEvent mouseEvent) {
        //Ignore.
    }
    
    /** {@inheritDoc} */
    public void mouseEntered(final MouseEvent mouseEvent) {
        //Ignore.
    }
    
    /** {@inheritDoc} */
    public void mouseExited(final MouseEvent mouseEvent) {
        //Ignore.
    }
    
    /** {@inheritDoc} */
    public void mouseClicked(final MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            ErrorListDialog.getErrorListDialog().setVisible(true);
        }
    }
    
    /**
     * Adds a component to the status bar.
     *
     * @param component component to add
     */
    public void addComponent(final Component component) {
        remove(iconLabel);
        add(component);
        add(iconLabel);
        layoutBar();
    }
    
    /**
     * Removes a component to the status bar.
     *
     * @param component component to add
     */
    public void removeComponent(final Component component) {
        remove(component);
        layoutBar();
    }
    
    /** Layout the status bar. */
    private void layoutBar() {
        this.setVisible(false);
        final SpringLayout layout = (SpringLayout) this.getLayout();
        final int numComponents = this.getComponentCount() - 1;
        
        SpringLayout.Constraints constraints;
        
        layout.putConstraint(SpringLayout.WEST, getComponent(0),
                Spring.constant(0), SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.EAST, getComponent(0),
                Spring.constant(-SMALL_BORDER), SpringLayout.WEST, getComponent(1));
        constraints = layout.getConstraints(getComponent(0));
        constraints.setHeight(Spring.constant(20));
        
        for (int i = 1; i < numComponents; i++) {
            layout.putConstraint(SpringLayout.EAST, getComponent(i),
                    Spring.constant(-SMALL_BORDER), SpringLayout.WEST,
                    getComponent(i + 1));
            constraints = layout.getConstraints(getComponent(i));
            constraints.setHeight(Spring.constant(20));
            constraints.setWidth(constraints.getWidth());
        }
        
        layout.putConstraint(SpringLayout.EAST, getComponent(numComponents),
                Spring.constant(0), SpringLayout.EAST, this);
        constraints = layout.getConstraints(getComponent(numComponents));
        constraints.setHeight(Spring.constant(20));
        constraints.setWidth(constraints.getWidth());
        this.setVisible(true);
    }
}
