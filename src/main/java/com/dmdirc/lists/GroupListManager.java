/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.lists;

import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.parser.events.GroupListEndEvent;
import com.dmdirc.parser.events.GroupListEntryEvent;
import com.dmdirc.parser.events.GroupListStartEvent;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.collections.ObservableList;
import com.dmdirc.util.collections.ObservableListDecorator;

import java.util.LinkedList;

import net.engio.mbassy.listener.Handler;

/**
 * Manages a group list request.
 */
public class GroupListManager {

    /** List of registered listeners. */
    private final ListenerList listenerList = new ListenerList();
    /** The connection to request group information from. */
    private final Connection connection;
    /** The cached groups. */
    private final ObservableList<GroupListEntry> groups = new ObservableListDecorator<>(
            new LinkedList<>());

    public GroupListManager(final Connection connection) {
        this.connection = connection;
    }

    public ObservableList<GroupListEntry> getGroups() {
        return groups;
    }

    /**
     * Starts a search with the given search terms.
     *
     * @param searchTerm The term to search for
     *
     * @see Parser#requestGroupList(String)
     */
    public void startSearch(final String searchTerm) {
        groups.clear();

        connection.getParser().ifPresent(p -> {
            p.getCallbackManager().subscribe(this);
            p.requestGroupList(searchTerm);
        });
    }

    @Handler
    public void onGroupListStart(final GroupListStartEvent event) {
        listenerList.getCallable(GroupListObserver.class).onGroupListStarted();
    }

    @Handler
    public void onGroupListEntry(final GroupListEntryEvent event) {
        groups.add(new GroupListEntry(event.getName(), event.getUsers(), event.getTopic()));
    }

    @Handler
    public void onGroupListEnd(final GroupListEndEvent event) {
        event.getParser().getCallbackManager().unsubscribe(this);
        listenerList.getCallable(GroupListObserver.class).onGroupListFinished();
    }

    /**
     * Joins a group list entry.
     *
     * @param entry Group list entry to join
     */
    public void joinGroupListEntry(final GroupListEntry entry) {
        connection.getGroupChatManager().join(new ChannelJoinRequest(entry.getName()));
    }

    /**
     * Adds an observer to this manager.
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
