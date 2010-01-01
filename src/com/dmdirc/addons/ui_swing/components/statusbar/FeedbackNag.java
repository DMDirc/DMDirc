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

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.dialogs.FeedbackDialog;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.interfaces.StatusBarComponent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Feedback nag icon.
 */
public class FeedbackNag extends JLabel implements StatusBarComponent, 
        MouseListener, ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Nag icon. */
    private final ImageIcon icon;
    /** Dismiss menu. */
    private final JPopupMenu menu;
    /** Dismiss menu item. */
    private final JMenuItem dismiss;
    /** Show menu item. */
    private final JMenuItem show;
    /** Swing Controller. */
    private SwingController controller;

    /**
     * Creates a new feedback nag.
     * 
     * @param controller Swing controller
     */
    public FeedbackNag(final SwingController controller) {
        super();
        this.controller = controller;
        icon = new ImageIcon(IconManager.getIconManager().getImage("feedback"));
        menu = new JPopupMenu();
        dismiss = new JMenuItem("Dismiss");
        show = new JMenuItem("Open");

        setIcon(icon);
        setBorder(BorderFactory.createEtchedBorder());
        setToolTipText("We would appreciate any feedback you may have about DMDirc.");

        menu.add(show);
        menu.add(dismiss);

        show.addActionListener(this);
        dismiss.addActionListener(this);
        addMouseListener(this);

        controller.getStatusBar().addComponent(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == 1) {
            FeedbackDialog.showFeedbackDialog(controller.getMainFrame());
            controller.getStatusBar().removeComponent(this);
        }
        checkMouseEvent(e);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mousePressed(MouseEvent e) {
        checkMouseEvent(e);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        checkMouseEvent(e);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        checkMouseEvent(e);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseExited(MouseEvent e) {
        checkMouseEvent(e);
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
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == show) {
            FeedbackDialog.showFeedbackDialog(controller.getMainFrame());
        }
        controller.getStatusBar().removeComponent(this);
    }
}
