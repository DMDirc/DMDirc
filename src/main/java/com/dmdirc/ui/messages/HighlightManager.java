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

package com.dmdirc.ui.messages;

import com.dmdirc.events.ChannelActionEvent;
import com.dmdirc.events.ChannelActionHighlightEvent;
import com.dmdirc.events.ChannelHighlightEvent;
import com.dmdirc.events.ChannelMessageEvent;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.QueryActionEvent;
import com.dmdirc.events.QueryActionHighlightEvent;
import com.dmdirc.events.QueryHighlightEvent;
import com.dmdirc.events.QueryMessageEvent;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.events.ServerNickChangeEvent;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.WindowModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

import net.engio.mbassy.listener.Handler;

/**
 * Looks for highlights in messages.
 */
public class HighlightManager {

    private static final String REGEX_TEPLATE = "(?i).*(\\p{Space}|^|\\p{Punct})\\Q%s\\E" +
            "(\\p{Space}|\\p{Punct}|$).*";

    private final Collection<Pattern> patterns = new ArrayList<>();
    private final WindowModel serverWindow;

    private Optional<Pattern> nicknamePattern = Optional.empty();

    public HighlightManager(final WindowModel serverWindow) {
        this.serverWindow = serverWindow;
    }

    @Handler(rejectSubtypes = true)
    void handleChannelMessage(final ChannelMessageEvent event) {
        if (event.getChannel().getConnection().get().getWindowModel().equals(serverWindow)
                && patterns.stream().anyMatch(p -> p.matcher(event.getMessage()).matches())) {
            event.setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
            event.getChannel().getEventBus().publish(
                    new ChannelHighlightEvent(
                            event.getTimestamp(), event.getChannel(), event.getClient(),
                            event.getMessage()));
        }
    }

    @Handler(rejectSubtypes = true)
    void handleChannelAction(final ChannelActionEvent event) {
        if (event.getChannel().getConnection().get().getWindowModel().equals(serverWindow)
                && patterns.stream().anyMatch(p -> p.matcher(event.getMessage()).matches())) {
            event.setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
            event.getChannel().getEventBus().publish(
                    new ChannelActionHighlightEvent(
                            event.getTimestamp(), event.getChannel(), event.getClient(),
                            event.getMessage()));
        }
    }

    @Handler(rejectSubtypes = true)
    void handleQueryMessage(final QueryMessageEvent event) {
        if (event.getUser().getConnection().getWindowModel().equals(serverWindow)
                && patterns.stream().anyMatch(p -> p.matcher(event.getMessage()).matches())) {
            event.setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
            event.getQuery().getWindowModel().getEventBus().publish(
                    new QueryHighlightEvent(
                            event.getTimestamp(), event.getQuery(), event.getUser(),
                            event.getMessage()));
        }
    }

    @Handler(rejectSubtypes = true)
    void handleQueryMessage(final QueryActionEvent event) {
        if (event.getUser().getConnection().getWindowModel().equals(serverWindow)
                && patterns.stream().anyMatch(p -> p.matcher(event.getMessage()).matches())) {
            event.setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
            event.getQuery().getWindowModel().getEventBus().publish(
                    new QueryActionHighlightEvent(
                            event.getTimestamp(), event.getQuery(), event.getUser(),
                            event.getMessage()));
        }
    }

    @Handler
    void handleNickChange(final ServerNickChangeEvent event) {
        if (event.getConnection().getWindowModel().equals(serverWindow)) {
            setNickname(event.getNewNick());
        }
    }

    @Handler
    void handleConnected(final ServerConnectedEvent event) {
        if (event.getConnection().getWindowModel().equals(serverWindow)) {
            patterns.clear();

            event.getConnection().getProfile().getHighlights()
                    .stream()
                    .map(this::compile)
                    .forEach(patterns::add);

            event.getConnection().getLocalUser()
                    .map(User::getNickname)
                    .ifPresent(this::setNickname);
        }
    }

    private void setNickname(final String newNick) {
        nicknamePattern.ifPresent(patterns::remove);
        nicknamePattern = Optional.of(compile(newNick));
        patterns.add(nicknamePattern.get());
    }

    private Pattern compile(final String word) {
        return Pattern.compile(String.format(REGEX_TEPLATE, word));
    }

}
