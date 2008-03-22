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

package com.dmdirc.ui.swing.components;

import com.dmdirc.FrameContainer;
import com.dmdirc.ui.IconManager;
import com.dmdirc.Invite;
import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.InviteListener;
import com.dmdirc.util.MapList;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 * Invite label.
 */
public class InviteLabel extends JLabel implements InviteListener,
        ActionListener, MouseListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Active server. */
    private Server activeServer;
    /** Invite map list. */
    private final MapList<Server, Invite> inviteList;
    /** Invite popup menu. */
    private JPopupMenu menu;
    /** Dismiss invites menu item. */
    private JMenuItem dismiss;

    /**
     * Instantiates a new invite label.
     */
    public InviteLabel() {
        super();

        setBorder(BorderFactory.createEtchedBorder());
        setIcon(IconManager.getIconManager().getIcon("invite"));

        inviteList = new MapList<Server, Invite>();
        menu = new JPopupMenu();
        dismiss = new JMenuItem("Dismiss all invites");
        dismiss.addActionListener(new java.awt.event.ActionListener() {

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e) {
                for (Server server : inviteList.keySet()) {
                    final List<Invite> invites = new ArrayList<Invite>(inviteList.values(server));
                    for (Invite invite : invites) {
                        invite.getServer().removeInvite(invite);
                    }
                }
            }
        });

        for (Server server : ServerManager.getServerManager().getServers()) {
            inviteList.add(server, server.getInvites());
            server.addInviteListener(this);
        }

        ActionManager.addListener(this, CoreActionType.CLIENT_FRAME_CHANGED);
        addMouseListener(this);

        update();
    }

    /**
     * Popuplates the menu.
     */
    private void popuplateMenu() {
        menu.removeAll();

        final List<Invite> invites = activeServer.getInvites();
        for (Invite invite : invites) {
            menu.add(new JMenuItem(new InviteAction(invite)));
        }
        menu.add(new JSeparator());
        menu.add(dismiss);
    }

    /**
     * Updates the invite label for the currently active server.
     */
    private void update() {
        update(Main.getUI().getActiveServer());
    }

    /**
     * Updates the invite label for the server.
     * 
     * @param server Server to update
     */
    private void update(final Server server) {
        activeServer = server;
        if (server != null && !inviteList.containsKey(server)) {
            inviteList.add(server, server.getInvites());
            server.addInviteListener(this);
        }

        if (activeServer == null || inviteList.get(activeServer).isEmpty()) {
            setVisible(false);
        } else {
            setVisible(true);
            setToolTipText(inviteList.get(activeServer).size() +
                    " pending invites.");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void inviteReceived(final Server server, final Invite invite) {
        if (!inviteList.containsKey(server)) {
            inviteList.add(server);
        }
        inviteList.add(server, invite);
        update();
    }

    /** {@inheritDoc} */
    @Override
    public void inviteExpired(final Server server, final Invite invite) {
        if (!inviteList.containsKey(server)) {
            inviteList.add(server);
        }
        inviteList.remove(server, invite);
        update();
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (arguments[0] == null) {
            update((Server) null);
        } else {
            update(((FrameContainer) arguments[0]).getServer());
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        popuplateMenu();
        if (menu.getComponentCount() > 0) {
            menu.show(this, e.getX(), e.getY());
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }
}

/**
 * Invite action.
 */
class InviteAction extends AbstractAction {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Invite. */
    private final Invite invite;

    /**
     * Instantiates a new invite action.
     * 
     * @param invite Invite for the action
     */
    public InviteAction(final Invite invite) {
        super(invite.getChannel() + " (" + invite.getSource()[0] + ")");
        
        this.invite = invite;
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        invite.accept();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return invite.getChannel() + "(" + Arrays.toString(invite.getSource()) + ")";
    }
}
