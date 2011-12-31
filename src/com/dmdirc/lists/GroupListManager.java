/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

package com.dmdirc.lists;

import com.dmdirc.Server;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.GroupListEndListener;
import com.dmdirc.parser.interfaces.callbacks.GroupListEntryListener;
import com.dmdirc.parser.interfaces.callbacks.GroupListStartListener;
import com.dmdirc.util.collections.ObservableList;
import com.dmdirc.util.collections.ObservableListDecorator;

import java.util.Date;
import java.util.LinkedList;

import lombok.Getter;
import lombok.ListenerSupport;
import lombok.RequiredArgsConstructor;

/**
 * Manages a group list request.
 */
@RequiredArgsConstructor
@ListenerSupport(GroupListObserver.class)
public class GroupListManager implements GroupListStartListener,
        GroupListEntryListener, GroupListEndListener {

    /** The server to request group information from. */
    private final Server server;

    /** The cached groups. */
    @Getter
    private final ObservableList<GroupListEntry> groups
            = new ObservableListDecorator<GroupListEntry>(new LinkedList<GroupListEntry>());

    /**
     * Starts a search with the given search terms.
     *
     * @param searchTerm The term to search for
     * @see Parser#requestGroupList(java.lang.String)
     */
    public void startSearch(final String searchTerm) {
        groups.clear();

        server.getParser().getCallbackManager().addCallback(GroupListStartListener.class, this);
        server.getParser().getCallbackManager().addCallback(GroupListEntryListener.class, this);
        server.getParser().getCallbackManager().addCallback(GroupListEndListener.class, this);
        server.getParser().requestGroupList(searchTerm);
    }

    /** {@inheritDoc} */
    @Override
    public void onGroupListStart(final Parser parser, final Date date) {
        fireOnGroupListStarted();
    }

    /** {@inheritDoc} */
    @Override
    public void onGroupListEntry(final Parser parser, final Date date,
            final String name, final int users, final String topic) {
        groups.add(new GroupListEntry(name, users, topic));
    }

    /** {@inheritDoc} */
    @Override
    public void onGroupListEnd(final Parser parser, final Date date) {
        parser.getCallbackManager().delAllCallback(this);
        fireOnGroupListFinished();
    }

    /**
     * Joins a group list entry.
     *
     * @param entry Group list entry to join
     */
    public void joinGroupListEntry(final GroupListEntry entry) {
        server.join(new ChannelJoinRequest(entry.getName()));
    }

}
