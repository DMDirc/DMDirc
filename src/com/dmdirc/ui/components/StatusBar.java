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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import com.dmdirc.Config;
import com.dmdirc.Error;
import com.dmdirc.ui.interfaces.StatusErrorNotifier;
import com.dmdirc.ui.interfaces.StatusMessageNotifier;

import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;

/**
 * Status bar, shows message and info on the gui.
 */
public final class StatusBar extends JPanel implements MouseListener,
        ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** message label. */
    private final JLabel messageLabel;
    
    /** icon label. */
    private final JLabel iconLabel;
    
    /** current status bar message notifier. */
    private StatusMessageNotifier messageNotifier;
    
    /** current status bar error notifier. */
    private StatusErrorNotifier errorNotifier;
    
    /** non error state image icon. */
    private final ImageIcon normalIcon;
    
    /** Error history storage. */
    private final List<Error> errors;
    
    /** Timer to clear the error. */
    private TimerTask errorTimer;
    
    /** Timer to clear the message. */
    private TimerTask messageTimer;
    
    /** Popupmenu for this frame. */
    private final JPopupMenu popup;
    
    /** Clear error menu menu item. */
    private final JMenuItem clearErrors;
    
    /** No errors menu menu item. */
    private final JMenuItem noErrors;
    
    /** Creates a new instance of StatusBar. */
    public StatusBar() {
        super();
        final SpringLayout layout = new SpringLayout();
        errors = new ArrayList<Error>();
        
        popup = new JPopupMenu();
        
        noErrors = new JMenuItem("No errors");
        noErrors.addActionListener(this);
        noErrors.setActionCommand("None");
        popup.add(noErrors);
        
        clearErrors = new JMenuItem("Clear errors");
        clearErrors.addActionListener(this);
        clearErrors.setActionCommand("Clear");
        
        setBorder(BorderFactory.createEmptyBorder(0, SMALL_BORDER, SMALL_BORDER, 
                SMALL_BORDER));
        
        messageLabel = new JLabel("Ready");
        iconLabel = new JLabel();
        
        messageLabel.setBorder(BorderFactory.createEtchedBorder());
        iconLabel.setBorder(BorderFactory.createEtchedBorder());
        
        messageLabel.addMouseListener(this);
        iconLabel.addMouseListener(this);
        
        this.setLayout(layout);
        
        add(messageLabel);
        add(iconLabel);
        
        setPreferredSize(new Dimension(Short.MAX_VALUE, 25));
        setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
        
        normalIcon = new ImageIcon(this.getClass()
        .getClassLoader().getResource("com/dmdirc/res/normal.png"));
        
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
    public synchronized void setError(final ImageIcon newIcon,
            final StatusErrorNotifier newNotifier) {
        addToHistory(newIcon, newNotifier);
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
        processMouseEvent(mouseEvent);
    }
    
    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param mouseEvent mouse event
     */
    public void mouseReleased(final MouseEvent mouseEvent) {
        processMouseEvent(mouseEvent);
    }
    
    /**
     * Invoked when the mouse enters a component.
     *
     * @param mouseEvent mouse event
     */
    public void mouseEntered(final MouseEvent mouseEvent) {
        //Ignore.
    }
    
    /**
     * Invoked when the mouse exits a component.
     *
     * @param mouseEvent mouse event
     */
    public void mouseExited(final MouseEvent mouseEvent) {
        //Ignore.
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
            errorNotifier.clickReceived();
        }
        processMouseEvent(mouseEvent);
    }
    
    /**
     * Processes every mouse button event to check for a popup trigger.
     * @param e mouse event
     */
    public void processMouseEvent(final MouseEvent e) {
        if (e.isPopupTrigger() && e.getSource() == iconLabel) {
            final Point point = this.getMousePosition();
            popup.show(this, (int) point.getX(), (int) point.getY());
        } else {
            super.processMouseEvent(e);
        }
    }
    
    /**
     * Adds the error to the history.
     * @param icon error icon
     * @param notifier error notifier
     */
    private void addToHistory(final Icon icon, final StatusErrorNotifier notifier) {
        if (icon != null && notifier != null) {
            JMenuItem mi;
            final Error error = new Error(icon, notifier);
            final int errorHistory = Config.getOptionInt("statusBar", "errorHistory", 10);
            errors.add(error);
            popup.removeAll();
            while (errors.size() >= errorHistory) {
                errors.remove(0);
            }
            for (Error thisError : errors) {
                mi = new JMenuItem(thisError.getDate().toString(), icon);
                mi.addActionListener(this);
                mi.setActionCommand(String.valueOf(errors.indexOf(thisError)));
                popup.add(mi);
            }
            popup.addSeparator();
            popup.add(clearErrors);
        }
    }
    
    /**
     * Shows the error from the history. {@inheritDoc}
     */
    public void actionPerformed(final ActionEvent e) {
        if ("Clear".equals(e.getActionCommand())) {
            errors.clear();
            popup.removeAll();
            popup.add(noErrors);
            clearError();
        } else if ("None".equals(e.getActionCommand())) {
            //Ignore
        } else {
            errors.get(Integer.valueOf(e.getActionCommand())).getNotifier().clickReceived();
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
        
        if (numComponents > 1) {
            for (int i = 1; i < numComponents; i++) {
                layout.putConstraint(SpringLayout.EAST, getComponent(i),
                        Spring.constant(-SMALL_BORDER), SpringLayout.WEST,
                        getComponent(i + 1));
                constraints = layout.getConstraints(getComponent(i));
                constraints.setHeight(Spring.constant(20));
                constraints.setWidth(constraints.getWidth());
            }
        }
        
        layout.putConstraint(SpringLayout.EAST, getComponent(numComponents),
                Spring.constant(0), SpringLayout.EAST, this);
        constraints = layout.getConstraints(getComponent(numComponents));
        constraints.setHeight(Spring.constant(20));
        constraints.setWidth(constraints.getWidth());
        this.setVisible(true);
    }
}
