/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.ui.messages;

import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.BaseChannelTextEvent;
import com.dmdirc.events.BaseQueryTextEvent;
import com.dmdirc.events.ChannelHighlightEvent;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.events.QueryHighlightEvent;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.events.ServerNickChangeEvent;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.util.colours.Colour;

import com.google.common.base.Strings;

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
    private final AggregateConfigProvider configProvider;
    private final ColourManager colourManager;

    private Optional<Pattern> nicknamePattern = Optional.empty();

    private Optional<Colour> backgroundColour = Optional.empty();
    private Optional<Colour> foregroundColour = Optional.empty();

    public HighlightManager(
            final WindowModel serverWindow,
            final AggregateConfigProvider configProvider,
            final ColourManager colourManager) {
        this.serverWindow = serverWindow;
        this.configProvider = configProvider;
        this.colourManager = colourManager;
    }

    public void init() {
        configProvider.getBinder().bind(this, HighlightManager.class);
    }

    public void stop() {
        configProvider.getBinder().unbind(this);
    }

    @Handler
    void handleChannelMessage(final BaseChannelTextEvent event) {
        if (event.getChannel().getConnection().get().getWindowModel().equals(serverWindow)
                && patterns.stream().anyMatch(p -> p.matcher(event.getMessage()).matches())) {
            setColours(event);
            event.getChannel().getEventBus().publishAsync(new ChannelHighlightEvent(event));
        }
    }

    @Handler
    void handleQueryMessage(final BaseQueryTextEvent event) {
        if (event.getUser().getConnection().getWindowModel().equals(serverWindow)
                && patterns.stream().anyMatch(p -> p.matcher(event.getMessage()).matches())) {
            setColours(event);
            event.getQuery().getEventBus().publishAsync(new QueryHighlightEvent(event));
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

    @ConfigBinding(domain = "ui", key = "highlightLineForegroundColour", required = false)
    void handleForegroundColour(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            foregroundColour = Optional.empty();
        } else {
            foregroundColour = Optional.ofNullable(colourManager.getColourFromString(value, null));
        }
    }

    @ConfigBinding(domain = "ui", key = "highlightLineBackgroundColour", required = false)
    void handleBackgroundColour(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            backgroundColour = Optional.empty();
        } else {
            backgroundColour = Optional.ofNullable(colourManager.getColourFromString(value, null));
        }
    }

    private void setColours(final DisplayableEvent event) {
        backgroundColour.ifPresent(
                c -> event.setDisplayProperty(DisplayProperty.BACKGROUND_COLOUR, c));
        foregroundColour.ifPresent(
                c -> event.setDisplayProperty(DisplayProperty.FOREGROUND_COLOUR, c));
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
