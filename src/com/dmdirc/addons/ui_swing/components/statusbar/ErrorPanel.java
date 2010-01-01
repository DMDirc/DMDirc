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
import com.dmdirc.addons.ui_swing.dialogs.error.ErrorListDialog;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ErrorListener;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.ui.IconManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * Shows error status in the status bar.
 *
 * @since 0.6.3m1
 * @author chris
 */
public class ErrorPanel extends StatusbarPopupPanel implements ErrorListener, ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;

    /** non error state image icon. */
    private static final Icon DEFAULT_ICON = IconManager.getIconManager().
            getIcon("normal");
    /** Currently showing error level. */
    private ErrorLevel errorLevel;
    /** Status controller. */
    private final MainFrame mainFrame;
    /** Swing status bar. */
    private SwingStatusBar statusBar;
    /** Error manager. */
    private final ErrorManager errorManager = ErrorManager.getErrorManager();
    /** Dismiss menu. */
    private final JPopupMenu menu;
    /** Dismiss menu item. */
    private final JMenuItem dismiss;
    /** Show menu item. */
    private final JMenuItem show;
    /** Swing controller. */
    private SwingController controller;

    /**
     * Creates a new ErrorPanel for the speicified status bar.
     *
     * @param controller Swing controller
     * @param mainFrame Main frame
     * @param statusBar Status bar  
     */
    public ErrorPanel(final SwingController controller,
            final MainFrame mainFrame, final SwingStatusBar statusBar) {
        super(new JLabel());

        this.controller = controller;
        this.mainFrame = mainFrame;
        this.statusBar = statusBar;
        
        menu = new JPopupMenu();
        dismiss = new JMenuItem("Clear All");
        show = new JMenuItem("Open");
        label.setIcon(DEFAULT_ICON);
        setVisible(errorManager.getErrorCount() > 0);
        menu.add(show);
        menu.add(dismiss);
        errorManager.addErrorListener(this);
        dismiss.addActionListener(this);
        show.addActionListener(this);
        checkErrors();
    }

    /** {@inheritDoc} */
    @Override
    protected StatusbarPopupWindow getWindow() {
        return new ErrorPopup(this, mainFrame);
    }

    /** Clears the error. */
    public void clearError() {
        label.setIcon(DEFAULT_ICON);
        errorLevel = null;
    }

    /** {@inheritDoc} */
    @Override
    public void errorAdded(final ProgramError error) {
        checkErrors();
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
                final List<ProgramError> errors = errorManager.getErrors();

                if (errors.isEmpty()) {
                    setVisible(false);
                } else {
                    for (ProgramError error : errors) {
                        if (errorLevel == null ||
                                !error.getLevel().moreImportant(errorLevel)) {
                            errorLevel = error.getLevel();
                            label.setIcon(errorLevel.getIcon());
                        }
                    }
                    setVisible(true);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReady() {
        return statusBar.isValid();
    }

    /**
     * {@inheritDoc}
     *
     * @param mouseEvent Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent mouseEvent) {
        super.mousePressed(mouseEvent);
        checkMouseEvent(mouseEvent);
    }

    /**
     * {@inheritDoc}
     *
     * @param mouseEvent Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent mouseEvent) {
        super.mouseReleased(mouseEvent);
        checkMouseEvent(mouseEvent);
    }

    /**
     * {@inheritDoc}
     *
     * @param mouseEvent Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent mouseEvent) {
        super.mouseEntered(mouseEvent);
        checkMouseEvent(mouseEvent);
    }

    /**
     * {@inheritDoc}
     *
     * @param mouseEvent Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent mouseEvent) {
        super.mouseExited(mouseEvent);
        checkMouseEvent(mouseEvent);
    }

    /**
     * {@inheritDoc}
     *
     * @param mouseEvent Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent mouseEvent) {
        super.mouseClicked(mouseEvent);
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            controller.showErrorDialog();
        }
        checkMouseEvent(mouseEvent);
    }

    /**
     * Checks a mouse event for a popup trigger.
     *
     * @param e Mouse event
     */
    private void checkMouseEvent(MouseEvent e) {
        if (e.isPopupTrigger()) {
            menu.show(this, e.getX(), e.getY());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == show) {
            controller.showErrorDialog();
        } else {
            final Collection<ProgramError> errors =
                    ErrorManager.getErrorManager().getErrors();
            for (ProgramError error : errors) {
                ErrorManager.getErrorManager().deleteError(error);
            }
        }
    }

}
