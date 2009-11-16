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

import com.dmdirc.updater.Update;
import com.dmdirc.updater.UpdateChecker;
import java.awt.Window;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Information popup for the updater label.
 *
 * @since 0.6.4
 * @author chris
 */
public class UpdaterPopup extends StatusbarPopupWindow {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /**
     * Creates a new popup window for the specified panel and window.
     *
     * @param parent The panel that owns this popup
     * @param parentWindow The Window that owns this popup
     */
    public UpdaterPopup(final JPanel parent, final Window parentWindow) {
        super(parent, parentWindow);
    }

    /** {@inheritDoc} */
    @Override
    protected void initContent(final JPanel panel) {
        final UpdateChecker.STATE state = UpdateChecker.getStatus();

        if (state.equals(UpdateChecker.STATE.CHECKING)) {
            panel.add(new JLabel("Checking for updates..."));
        } else {
            final List<Update> updates = UpdateChecker.getAvailableUpdates();

            if (state.equals(UpdateChecker.STATE.RESTART_REQUIRED)) {
                panel.add(new JLabel("A restart is required to install updates."), "span,wrap");
            } else if (state.equals(UpdateChecker.STATE.UPDATING)) {
                panel.add(new JLabel("Update in progress..."), "span,wrap");
            } else {
                panel.add(new JLabel(updates.size() == 1 ? "There is one update available"
                        : "There are " + updates.size() + " updates available"), "span,wrap");
            }

            panel.add(new JSeparator(), "span, growx, pushx, wrap");

            for (Update update : updates) {
                panel.add(new JLabel(update.getComponent().getFriendlyName()),
                        "growx, pushx");
                panel.add(new JLabel(update.getRemoteVersion(), JLabel.CENTER),
                        "growx, pushx, al center");
                panel.add(new JLabel(update.getStatus().toString(), JLabel.RIGHT),
                        "growx, pushx, al right, wrap");
            }
        }
    }

}
