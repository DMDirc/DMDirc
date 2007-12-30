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
import com.dmdirc.interfaces.UpdateCheckerListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ErrorListener;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.ui.interfaces.StatusBar;
import com.dmdirc.ui.interfaces.StatusMessageNotifier;
import com.dmdirc.ui.swing.dialogs.SwingUpdaterDialog;
import com.dmdirc.ui.swing.dialogs.error.ErrorListDialog;
import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.UpdateChecker.STATE;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;

/** Status bar, shows message and info on the gui. */
public final class SwingStatusBar extends JPanel implements MouseListener,
        ErrorListener, StatusBar, UpdateCheckerListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    /** Default status bar message. */
    private static final String DEFAULT_MESSAGE = "Ready.";
    /** non error state image icon. */
    private static final Icon DEFAULT_ICON =
            IconManager.getIconManager().getIcon("normal");
    /** message label. */
    private final JLabel messageLabel;
    /** error label. */
    private final JLabel errorLabel;
    /** update label. */
    private final JLabel updateLabel;
    /** current status bar message notifier. */
    private transient StatusMessageNotifier messageNotifier;
    /** Timer to clear the message. */
    private transient TimerTask messageTimer;
    /** Currently showing error level. */
    private ErrorLevel errorLevel;
    /** Invite label. */
    private InviteLabel inviteLabel;

    /**
     * Creates a new instance of SwingStatusBar.
     */
    public SwingStatusBar() {
        super();

        messageLabel = new JLabel(DEFAULT_MESSAGE);
        errorLabel = new JLabel(DEFAULT_ICON);
        updateLabel = new JLabel();
        inviteLabel = new InviteLabel();

        messageLabel.setBorder(BorderFactory.createEtchedBorder());
        errorLabel.setBorder(BorderFactory.createEtchedBorder());
        updateLabel.setBorder(BorderFactory.createEtchedBorder());

        messageLabel.addMouseListener(this);
        errorLabel.addMouseListener(this);
        updateLabel.addMouseListener(this);
        ErrorManager.getErrorManager().addErrorListener(this);
        UpdateChecker.addListener(this);

        setLayout(new MigLayout("ins rel, hidemode 3"));

        add(messageLabel, "growx, pushx, sgy components, hmax 20, hmin 20");
        add(inviteLabel, "sgy components, hmax 20, hmin 20");
        add(errorLabel, "sgy components, hmax 20, hmin 20");

        checkErrors();
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(final String newMessage) {
        setMessage(newMessage, null);
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier) {
        final int timeout =
                IdentityManager.getGlobalConfig().
                getOptionInt("statusBar", "messageDisplayLength", 5);
        setMessage(newMessage, newNotifier, timeout);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier, final int timeout) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                messageLabel.setText(newMessage);
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

    /** {@inheritDoc} */
    @Override
    public void clearMessage() {
        setMessage(DEFAULT_MESSAGE);
    }

    /** Clears the error. */
    public void clearError() {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                errorLabel.setIcon(DEFAULT_ICON);
                errorLevel = null;
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void errorAdded(final ProgramError error) {
        checkErrors();
    }

    /** {@inheritDoc} */
    @Override
    public void fatalError(final ProgramError error) {
    //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void errorDeleted(final ProgramError error) {
        checkErrors();
    }

    /** {@inheritDoc} */
    @Override
    public void errorStatusChanged(final ProgramError error) {
    //Ignore
    }

    /** Checks all the errors for the most significant error. */
    private void checkErrors() {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                clearError();
                final List<ProgramError> errors =
                        ErrorManager.getErrorManager().getErrors();
                if (errors.size() > 0) {
                    for (ProgramError error : errors) {
                        if (errorLevel == null ||
                                error.getLevel().moreImportant(errorLevel)) {
                            errorLevel = error.getLevel();
                            errorLabel.setIcon(errorLevel.getIcon());
                        }
                    }
                    errorLabel.setVisible(true);
                } else {
                    errorLabel.setVisible(false);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void mousePressed(final MouseEvent mouseEvent) {
    //Ignore.
    }

    /** {@inheritDoc} */
    @Override
    public void mouseReleased(final MouseEvent mouseEvent) {
    //Ignore.
    }

    /** {@inheritDoc} */
    @Override
    public void mouseEntered(final MouseEvent mouseEvent) {
    //Ignore.
    }

    /** {@inheritDoc} */
    @Override
    public void mouseExited(final MouseEvent mouseEvent) {
    //Ignore.
    }

    /** {@inheritDoc} */
    @Override
    public void mouseClicked(final MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            if (mouseEvent.getSource() == errorLabel) {
                ErrorListDialog.showErrorListDialog();
            } else if (mouseEvent.getSource() == updateLabel) {
                SwingUpdaterDialog.showSwingUpdaterDialog(UpdateChecker.getAvailableUpdates());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addComponent(final Component component) {
        if (!Arrays.asList(getComponents()).contains(component)) {
            remove(errorLabel);
            remove(inviteLabel);
            add(component, "sgy components, hmax 20, hmin 20");
            add(inviteLabel, "sgy components, hmax 20, hmin 20");
            add(errorLabel, "sgy components, hmax 20, hmin 20");
            validate();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeComponent(final Component component) {
        remove(component);
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReady() {
        return isValid();
    }

    /** {@inheritDoc} */
    @Override
    public void statusChanged(final STATE newStatus) {
        if (newStatus.equals(STATE.IDLE)) {
            removeComponent(updateLabel);
        } else {
            addComponent(updateLabel);
        }
        if (newStatus.equals(STATE.CHECKING)) {
            updateLabel.setToolTipText("Checking for updates...");
            updateLabel.setIcon(IconManager.getIconManager().
                    getIcon("hourglass"));
        } else if (newStatus.equals(STATE.UPDATES_AVAILABLE)) {
            updateLabel.setToolTipText("Updates available");
            updateLabel.setIcon(IconManager.getIconManager().getIcon("update"));
        }
    }
}
