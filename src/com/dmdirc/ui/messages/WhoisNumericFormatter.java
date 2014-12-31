/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.ServerDisconnectedEvent;
import com.dmdirc.events.ServerNumericEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.ConfigProvider;

import java.util.HashMap;
import java.util.Map;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

/**
 * Listens for whois-like numeric events and automatically formats them.
 *
 * @since 0.6.3
 */
@Listener(references = References.Strong)
public class WhoisNumericFormatter {

    /** The name of the target of any current whois requests. */
    private final Map<Connection, String> targets = new HashMap<>();
    /** The identity to add formatters to. */
    private final ConfigProvider identity;
    /** Event bus to subscribe to events on. */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new whois numeric formatter that will add automatic formats to the specified
     * identity. This will normally be a temporary global identity.
     *
     * @param identity The identity to write formatters to
     * @param eventBus The event bus to subscribe to events on
     */
    public WhoisNumericFormatter(final ConfigProvider identity,
            final DMDircMBassador eventBus) {
        this.identity = identity;
        this.eventBus = eventBus;
    }

    /**
     * Registers this this whois numeric formatter with the global actions manager.
     */
    public void register() {
        eventBus.subscribe(this);
    }

    /**
     * Handles a server disconnected event. This clears any entry for that server in the
     * {@link #targets} map.
     *
     * @param event The server disconnected event to process
     */
    @Handler
    public void handleServerDisconnected(final ServerDisconnectedEvent event) {
        targets.remove(event.getConnection());
    }

    /**
     * Handles a received numeric event. This method has special handling for numerics 311 and 318,
     * used to signal the start and end of a WHOIS request. It then monitors any other numerics
     * without formatters for events which look like WHOIS information, and formats them
     * automatically.
     *
     * @param event The server numeric event to process
     */
    @Handler
    public void handleNumeric(final ServerNumericEvent event) {
        final Connection server = event.getConnection();
        final int numeric = event.getNumeric();
        final String[] arguments = event.getArgs();
        switch (numeric) {
            case 311: // RPL_WHOISUSER
                targets.put(server, arguments[3]);
                break;
            case 318: // RPL_ENDOFWHOIS
                targets.remove(server);
                break;
            default:
                if (arguments.length > 4
                        && targets.containsKey(server)
                        && arguments[3].equals(targets.get(server))) {
                    // This numeric should be automatically formatted.

                    if (!event.getDisplayFormat().isEmpty()) {
                        // There's a custom format. We'll see if we need to
                        // add a formatter or notification settings for it
                        // anyway.
                        ensureExists(event.getDisplayFormat(), arguments.length);
                    } else {
                        // No custom formatter, switch it to an auto whois
                        // format and target.
                        final String target = "numeric_autowhois_" + (arguments.length - 4);
                        ensureExists(target, arguments.length);
                        event.setDisplayFormat(target);
                    }
                }
                break;
        }
    }

    /**
     * Ensures that the specified formatter exists in our identity.
     *
     * @param target    The target to be checked and added if necessary
     * @param arguments The number of arguments for the numeric
     */
    private void ensureExists(final String target, final int arguments) {
        if (!identity.hasOptionString("formatter", target)) {
            final StringBuilder builder = new StringBuilder("%4$s %" + arguments + "$s");
            for (int i = 5; i < arguments; i++) {
                builder.append(" %");
                builder.append(i);
                builder.append("$s");
            }

            identity.setOption("formatter", target, builder.toString());
        }

        if (!identity.hasOptionString("notifications", target)) {
            identity.setOption("notifications", target, "group:whois");
        }
    }

}
