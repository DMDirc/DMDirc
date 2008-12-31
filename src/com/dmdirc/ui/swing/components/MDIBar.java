/*
 * 
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.FrameContainer;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.FrameManager;

import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.UIUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 *
 */
public class MDIBar extends JPanel implements FrameManager,
        PropertyChangeListener, ActionListener {

    private static final long serialVersionUID = -8028057596226636245L;
    private static final int ICON_SIZE = 12;
    private NoFocusButton closeButton;
    private NoFocusButton minimiseButton;
    private NoFocusButton restoreButton;
    private MainFrame mainFrame;

    /**
     *
     * @param mainFrame 
     */
    public MDIBar(final MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        closeButton = new NoFocusButton(IconManager.getIconManager().
                getScaledIcon("close-12", ICON_SIZE, ICON_SIZE));
        minimiseButton = new NoFocusButton(IconManager.getIconManager().
                getScaledIcon("minimise-12", ICON_SIZE, ICON_SIZE));
        restoreButton = new NoFocusButton(IconManager.getIconManager().
                getScaledIcon("maximise-12", ICON_SIZE, ICON_SIZE));

        setOpaque(false);
        setLayout(new MigLayout("hmax 16, ins 0, fill"));
        add(minimiseButton, "w 16!, h 16!, right");
        add(restoreButton, "w 16!, h 16!, right");
        add(closeButton, "w 16!, h 16!, right");


        WindowManager.addFrameManager(this);
        closeButton.addActionListener(this);
        minimiseButton.addActionListener(this);
        restoreButton.addActionListener(this);

        check();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        closeButton.setEnabled(enabled);
        minimiseButton.setEnabled(enabled);
        restoreButton.setEnabled(enabled);
    }

    private void check() {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setVisible(mainFrame.getDesktopPane().getAllFrames().length > 0);
                setEnabled(mainFrame.getDesktopPane().getAllFrames().length > 0);
            }
        });
    }

    @Override
    public void setParent(JComponent parent) {
        //Ignore
    }

    @Override
    public boolean canPositionVertically() {
        return true;
    }

    @Override
    public boolean canPositionHorizontally() {
        return true;
    }

    @Override
    public void addWindow(FrameContainer window) {
        if (window.getFrame() instanceof JInternalFrame) {
            ((JInternalFrame) window.getFrame()).addPropertyChangeListener(
                    "maximum-12", this);
        }
        check();
    }

    @Override
    public void delWindow(FrameContainer window) {
        if (window.getFrame() instanceof JInternalFrame) {
            ((JInternalFrame) window.getFrame()).removePropertyChangeListener(
                    this);
        }
        check();
    }

    @Override
    public void addWindow(FrameContainer parent, FrameContainer window) {
        addWindow(window);
    }

    @Override
    public void delWindow(FrameContainer parent, FrameContainer window) {
        delWindow(window);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ((Boolean) evt.getNewValue()) {
            restoreButton.setIcon(IconManager.getIconManager().getScaledIcon(
                    "restore-12", ICON_SIZE, ICON_SIZE));
        } else {
            restoreButton.setIcon(IconManager.getIconManager().getScaledIcon(
                    "maximise-12", ICON_SIZE, ICON_SIZE));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Window activeFrame = mainFrame.getActiveFrame();
        if (activeFrame == null) {
            return;
        }
        if (closeButton.equals(e.getSource())) {
            activeFrame.close();
        } else if (minimiseButton.equals(e.getSource())) {
            ((TextFrame) activeFrame).minimise();
        } else if (restoreButton.equals(e.getSource())) {
            if (activeFrame.isMaximum()) {
                ((TextFrame) activeFrame).restore();
            } else {
                ((TextFrame) activeFrame).maximise();
            }
        }
    }
}
