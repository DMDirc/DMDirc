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

import com.dmdirc.IconManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ErrorListener;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.ui.interfaces.StatusBar;
import com.dmdirc.ui.interfaces.StatusMessageNotifier;
import com.dmdirc.ui.swing.dialogs.error.ErrorListDialog;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

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
public final class SwingStatusBar extends JPanel implements MouseListener,
        ErrorListener, StatusBar {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 4;
    
    /** Default status bar message. */
    private static final String DEFAULT_MESSAGE = "Ready.";
    
    /** non error state image icon. */
    private static final Icon DEFAULT_ICON = IconManager.getIconManager().getIcon("normal");
    
    /** message label. */
    private final JLabel messageLabel;
    
    /** icon label. */
    private final JLabel iconLabel;
    
    /** current status bar message notifier. */
    private transient StatusMessageNotifier messageNotifier;
    
    /** Timer to clear the message. */
    private transient TimerTask messageTimer;
    
    /** Currently showing error level. */
    private ErrorLevel errorLevel;
    
    /**
     * Creates a new instance of SwingStatusBar.
     */
    public SwingStatusBar() {
        super();
        
        setBorder(BorderFactory.createEmptyBorder(0, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER));
        
        messageLabel = new JLabel(DEFAULT_MESSAGE);
        iconLabel = new JLabel(DEFAULT_ICON);
        
        messageLabel.setBorder(BorderFactory.createEtchedBorder());
        iconLabel.setBorder(BorderFactory.createEtchedBorder());
        
        messageLabel.addMouseListener(this);
        iconLabel.addMouseListener(this);
        ErrorManager.getErrorManager().addErrorListener(this);
        clearError();
        
        setLayout(new SpringLayout());
        
        add(messageLabel);
        add(iconLabel);
        
        setPreferredSize(new Dimension(Short.MAX_VALUE, 25));
        setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
        
        layoutBar();
    }
    
    /** {@inheritDoc} */
    public void setMessage(final String newMessage) {
        setMessage(newMessage, null);
    }
    
    /** {@inheritDoc} */
    public void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier) {
        final int timeout =  IdentityManager.getGlobalConfig().getOptionInt("statusBar", "messageDisplayLength", 5);
        setMessage(newMessage, newNotifier, timeout);
    }
    
    /** {@inheritDoc} */
    public synchronized void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier, final int timeout) {
        messageLabel.setText(newMessage);
        messageNotifier = newNotifier;
        
        if (messageTimer != null && (System.currentTimeMillis()
        - messageTimer.scheduledExecutionTime()) <= 0) {
            messageTimer.cancel();
        }
        
        if (!DEFAULT_MESSAGE.equals(newMessage)) {
            messageTimer = new TimerTask() {
                public void run() {
                    clearMessage();
                }
            };
            new Timer("SwingStatusBar messageTimer").schedule(messageTimer,
                    new Date(System.currentTimeMillis() + 250 + timeout * 1000L));
        }
    }
    
    /** {@inheritDoc} */
    public void clearMessage() {
        setMessage(DEFAULT_MESSAGE);
    }
    
    /** {@inheritDoc} */
    public void clearError() {
        iconLabel.setIcon(DEFAULT_ICON);
        errorLevel = ErrorLevel.UNKNOWN;
    }
    
    /** {@inheritDoc} */
    public void errorAdded(final ProgramError error) {
        if (!ErrorListDialog.getErrorListDialog().isVisible()
        && error.getLevel().moreImportant(errorLevel)) {
            errorLevel = error.getLevel();
            iconLabel.setIcon(getIcon(error));
        }
    }
    
    private Icon getIcon(final ProgramError error) {
        switch (error.getLevel()) {
            case HIGH:
                return IconManager.getIconManager().getIcon("error");
            case MEDIUM:
                return IconManager.getIconManager().getIcon("warning");
            case LOW:
                return IconManager.getIconManager().getIcon("info");
            default:
                return IconManager.getIconManager().getIcon("info");
        }
    }
    
    /** {@inheritDoc} */
    public void fatalError(final ProgramError error) {
        //Ignore
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
        if (mouseEvent.getButton() == MouseEvent.BUTTON1
                && mouseEvent.getSource() == iconLabel) {
            ErrorListDialog.getErrorListDialog().setVisible(true);
            clearError();
        }
    }
    
    /** {@inheritDoc} */
    public void addComponent(final Component component) {
        remove(iconLabel);
        add(component);
        add(iconLabel);
        layoutBar();
    }
    
    /** {@inheritDoc} */
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
