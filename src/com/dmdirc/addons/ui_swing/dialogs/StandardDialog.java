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

package com.dmdirc.addons.ui_swing.dialogs;

import com.dmdirc.ui.CoreUIUtils;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/**
 * Provides common methods for dialogs.
 */
public class StandardDialog extends JDialog {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /**
     * The OK button for this frame.
     */
    private JButton okButton;
    /**
     * The cancel button for this frame.
     */
    private JButton cancelButton;

    /**
     * Creates a new instance of StandardDialog.
     * @param owner The frame that owns this dialog
     * @param modal Whether to display modally or not
     */
    public StandardDialog(final Frame owner, final boolean modal) {
        super(owner, modal);
        
        if (owner != null) {
            setIconImages(owner.getIconImages());
        }
    }

    /**
     * Creates a new instance of StandardDialog.
     * @param owner The frame that owns this dialog
     * @param modal Whether to display modally or not
     */
    public StandardDialog(final Window owner, final ModalityType modal) {
        super(owner, modal);
        
        if (owner != null) {
            setIconImages(owner.getIconImages());
        }
    }

    /**
     * Creates a new instance of StandardDialog.
     * @param owner The frame that owns this dialog
     * @param modal Whether to display modally or not
     */
    public StandardDialog(final Dialog owner, final boolean modal) {
        super(owner, modal);
        
        if (owner != null) {
            setIconImages(owner.getIconImages());
        }
    }

    /**
     * Centers and displays this dialog.
     */
    public void display() {
        CoreUIUtils.centreWindow(this);
        setVisible(true);
    }

    /**
     * Sets the specified button up as the OK button.
     * @param button The target button
     */
    protected void setOkButton(final JButton button) {
        okButton = button;
        button.setText("OK");
        button.setDefaultCapable(false);
    }

    /**
     * Sets the specified button up as the Cancel button.
     * @param button The target button
     */
    protected void setCancelButton(final JButton button) {
        cancelButton = button;
        button.setText("Cancel");
        button.setDefaultCapable(false);
    }

    /**
     * Gets the left hand button for a dialog.
     * @return left JButton
     */
    protected final JButton getLeftButton() {
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            return getOkButton();
        } else {
            return getCancelButton();
        }
    }

    /**
     * Gets the right hand button for a dialog.
     * @return right JButton
     */
    protected final JButton getRightButton() {
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            return getCancelButton();
        } else {
            return getOkButton();
        }
    }

    /**
     * Orders the OK and Cancel buttons in an appropriate order for the current
     * operating system.
     * @param leftButton The left-most button
     * @param rightButton The right-most button
     */
    protected final void orderButtons(final JButton leftButton,
            final JButton rightButton) {
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            // Windows - put the OK button on the left
            setOkButton(leftButton);
            setCancelButton(rightButton);
        } else {
            // Everything else - adhere to usability guidelines and put it on
            // the right.
            setOkButton(rightButton);
            setCancelButton(leftButton);
        }
        leftButton.setPreferredSize(new Dimension(100, 25));
        rightButton.setPreferredSize(new Dimension(100, 25));
        leftButton.setMinimumSize(new Dimension(100, 25));
        rightButton.setMinimumSize(new Dimension(100, 25));
    }

    /**
     * Creates the root pane of this dialog. We hook in two keylisteners
     * to send enter/escape events to our buttons.
     * @return The new root pane
     */
    @Override
    protected final JRootPane createRootPane() {
        final ActionListener escapeListener = new ActionListener() {

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                executeAction(StandardDialog.this.cancelButton);
            }
        };

        final ActionListener enterListener = new ActionListener() {

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                if (StandardDialog.this.getFocusOwner() instanceof JButton) {
                    executeAction((JButton) StandardDialog.this.getFocusOwner());
                } else {
                    executeAction(StandardDialog.this.okButton);
                }
            }
        };

        final KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                InputEvent.CTRL_DOWN_MASK);

        final JRootPane customRootPane = new JRootPane();

        customRootPane.registerKeyboardAction(escapeListener, escape,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        customRootPane.registerKeyboardAction(enterListener, enter,
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        return customRootPane;
    }

    /**
     * Retrieves the OK button for this form.
     * @return The form's OK button
     */
    public final JButton getOkButton() {
        return okButton;
    }

    /**
     * Retrieves the Cancel button for this form.
     * @return The form's cancel button
     */
    public final JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Simulates the user clicking on the specified target button.
     * @param target The button to use
     */
    protected final void executeAction(final JButton target) {
        if (target != null) {
            target.doClick();
        }
    }
}
