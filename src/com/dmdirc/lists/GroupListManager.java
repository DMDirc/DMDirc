/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.GroupListEndListener;
import com.dmdirc.parser.interfaces.callbacks.GroupListEntryListener;
import com.dmdirc.parser.interfaces.callbacks.GroupListStartListener;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.collections.ObservableList;
import com.dmdirc.util.collections.ObservableListDecorator;

import java.util.Date;
import java.util.LinkedList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Manages a group list request.
 */
@RequiredArgsConstructor
public class GroupListManager implements GroupListStartListener,
        GroupListEntryListener, GroupListEndListener {

    /** List of registered listeners. */
    private final ListenerList listenerList = new ListenerList();

    /** The connection to request group information from. */
    private final Connection connection;

    /** The cached groups. */
    @Getter
    private final ObservableList<GroupListEntry> groups
            = new ObservableListDecorator<>(new LinkedList<GroupListEntry>());

    /**
     * Starts a search with the given search terms.
     *
     * @param searchTerm The term to search for
     * @see Parser#requestGroupList(java.lang.String)
     */
    public void startSearch(final String searchTerm) {
        groups.clear();

        connection.getParser().getCallbackManager().addCallback(GroupListStartListener.class, this);
        connection.getParser().getCallbackManager().addCallback(GroupListEntryListener.class, this);
        connection.getParser().getCallbackManager().addCallback(GroupListEndListener.class, this);
        connection.getParser().requestGroupList(searchTerm);
    }

    /** {@inheritDoc} */
    @Override
    public void onGroupListStart(final Parser parser, final Date date) {
        listenerList.getCallable(GroupListObserver.class).onGroupListStarted();
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
        listenerList.getCallable(GroupListObserver.class).onGroupListFinished();
    }

    /**
     * Joins a group list entry.
     *
     * @param entry Group list entry to join
     */
    public void joinGroupListEntry(final GroupListEntry entry) {
        connection.join(new ChannelJoinRequest(entry.getName()));
    }

    /**
     * Adds an observer to this mananger.
     *
     * @param observer The observer to be notified of group start/stop events.
     */
    public void addGroupListObserver(final GroupListObserver observer) {
        listenerList.add(GroupListObserver.class, observer);
    }

    /**
     * Removes an observer from this manager.
     *
     * @param observer The observer to be removed.
     */
    public void removeGroupListObserver(final GroupListObserver observer) {
        listenerList.remove(GroupListObserver.class, observer);
    }

}
