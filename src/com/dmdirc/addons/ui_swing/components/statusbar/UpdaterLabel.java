/*
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

package com.dmdirc.addons.ui_swing.components.statusbar;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.dialogs.updater.SwingRestartDialog;
import com.dmdirc.addons.ui_swing.dialogs.updater.SwingUpdaterDialog;
import com.dmdirc.updater.UpdateCheckerListener;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.interfaces.StatusBarComponent;
import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.UpdateChecker.STATE;

import java.awt.Dialog.ModalityType;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;

/**
 * Updater label is responsible for handling the display of updates in the
 * status bar.
 */
public class UpdaterLabel extends StatusbarPopupPanel implements StatusBarComponent,
        UpdateCheckerListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Swing controller. */
    private MainFrame mainFrame;

    /**
     * Instantiates a new updater label, handles showing updates on the status bar.
     * 
     * @param mainFrame Main frame
     */
    public UpdaterLabel(final MainFrame mainFrame) {
        super();
        
        this.mainFrame = mainFrame;
        setBorder(BorderFactory.createEtchedBorder());
        UpdateChecker.addListener(this);
        setVisible(false);
        label.setText(null);
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
            if (UpdateChecker.getStatus().equals(UpdateChecker.STATE.RESTART_REQUIRED)) {
                SwingRestartDialog restartDialog = new SwingRestartDialog(
                        mainFrame, ModalityType.MODELESS);
                restartDialog.setVisible(true);
            } else if (!UpdateChecker.getStatus().equals(UpdateChecker.STATE.CHECKING)) {
                SwingUpdaterDialog.showSwingUpdaterDialog(
                        UpdateChecker.getAvailableUpdates(), mainFrame);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void statusChanged(final STATE newStatus) {
        if (newStatus.equals(STATE.IDLE)) {
            setVisible(false);
        } else {
            setVisible(true);
        }

        if (newStatus.equals(STATE.CHECKING)) {
            label.setIcon(IconManager.getIconManager().
                    getIcon("hourglass"));
        } else if (newStatus.equals(STATE.UPDATES_AVAILABLE)) {
            label.setIcon(IconManager.getIconManager().getIcon("update"));
        } else if (newStatus.equals(STATE.RESTART_REQUIRED)) {
            label.setIcon(IconManager.getIconManager().getIcon("restart-needed"));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected StatusbarPopupWindow getWindow() {
        return new UpdaterPopup(this, mainFrame);
    }
}
