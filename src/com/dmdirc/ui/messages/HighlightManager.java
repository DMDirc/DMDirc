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

import com.dmdirc.events.BaseChannelTextEvent;
import com.dmdirc.events.BaseQueryTextEvent;
import com.dmdirc.events.ChannelHighlightEvent;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.QueryHighlightEvent;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.events.ServerNickchangeEvent;
import com.dmdirc.parser.interfaces.LocalClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.util.colours.Colour;

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

    private Optional<Pattern> nicknamePattern = Optional.empty();

    @Handler
    public void handleChannelMessage(final BaseChannelTextEvent event) {
        if (patterns.stream().anyMatch(p -> p.matcher(event.getMessage()).matches())) {
            event.setDisplayProperty(DisplayProperty.BACKGROUND_COLOUR, Colour.RED);
            event.getChannel().getEventBus().publishAsync(new ChannelHighlightEvent(event));
        }
    }

    @Handler
    public void handleQueryMessage(final BaseQueryTextEvent event) {
        if (patterns.stream().anyMatch(p -> p.matcher(event.getMessage()).matches())) {
            event.setDisplayProperty(DisplayProperty.BACKGROUND_COLOUR, Colour.RED);
            event.getQuery().getEventBus().publishAsync(new QueryHighlightEvent(event));
        }
    }

    @Handler
    public void handleNickChange(final ServerNickchangeEvent event) {
        setNickname(event.getNewNick());
    }

    @Handler
    public void handleConnected(final ServerConnectedEvent event) {
        event.getConnection().getParser()
                .map(Parser::getLocalClient)
                .map(LocalClientInfo::getNickname)
                .ifPresent(this::setNickname);
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
