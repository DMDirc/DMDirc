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

import com.dmdirc.Invite;
import com.dmdirc.Server;
import com.dmdirc.ui.messages.Formatter;
import java.awt.Window;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Shows information about received invites.
 *
 * @since 0.6.3m1
 * @author chris
 */
public class InvitePopup extends StatusbarPopupWindow {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** The server to show invites for. */
    private final Server server;

    /**
     * Creates a new InvitePopup for the specified panel and server.
     * 
     * @param parent The parent of this popup
     * @param server The server to show invites for
     * @param parentWindow Parent window
     */
    public InvitePopup(final JPanel parent, final Server server, final Window parentWindow) {
        super(parent, parentWindow);
        this.server = server;
    }

    /** {@inheritDoc} */
    @Override
    protected void initContent(final JPanel panel) {
        for (Invite invite : server.getInvites()) {
            panel.add(new JLabel(invite.getChannel()), "growx, pushx");
            panel.add(new JLabel(invite.getSource()[0], JLabel.CENTER), "growx, pushx, al center");
            panel.add(new JLabel(Formatter.formatDuration((int)
                    (System.currentTimeMillis() - invite.getTimestamp()) / 1000) + " ago",
                    JLabel.RIGHT), "growx, pushx, al right, wrap");
        }
    }

}
