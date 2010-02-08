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

package com.dmdirc.actions.internal;

import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Listens for whois-like numeric events and automatically formats them.
 *
 * @since 0.6.3
 * @author chris
 */
public class WhoisNumericFormatter implements ActionListener {

    /** The name of the target of any current whois requests. */
    private final Map<Server, String> targets = new HashMap<Server, String>();

    /** The identity to add formatters to. */
    private final Identity identity;

    /**
     * Creates a new whois numeric formatter that will add automatic formats
     * to the specified identity. This will normally be a temporary global
     * identity, such as the one returned by
     * {@link IdentityManager#getAddonIdentity()}.
     *
     * @param identity The identity to write formatters to
     */
    public WhoisNumericFormatter(final Identity identity) {
        this.identity = identity;
    }

    /**
     * Registers this this whois numeric formatter with the global actions
     * manager.
     */
    public void register() {
        ActionManager.addListener(this, CoreActionType.SERVER_NUMERIC,
                CoreActionType.SERVER_DISCONNECTED);
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (CoreActionType.SERVER_DISCONNECTED == type) {
            handleServerDisconnected((Server) arguments[0]);
        } else {
            handleNumeric((Server) arguments[0], (Integer) arguments[1],
                    (String[]) arguments[2], format);
        }
    }

    /**
     * Handles a server disconnected event. This clears any entry for that
     * server in the <code>target</code> map.
     *
     * @param server The server that was disconnected
     */
    private void handleServerDisconnected(final Server server) {
        targets.remove(server);
    }

    /**
     * Handles a received numeric event. This method has special handling for
     * numerics 311 and 318, used to signal the start and end of a WHOIS
     * request. It then monitors any other numerics without formatters for
     * events which look like WHOIS information, and formats them automatically.
     *
     * @param server The server on which the event was received
     * @param numeric The numeric code of the event
     * @param arguments The arguments to the numeric event
     * @param format The format that should be used to display the event
     */
    private void handleNumeric(final Server server, final int numeric,
            final String[] arguments, final StringBuffer format) {
        switch (numeric) {
            case 311: // RPL_WHOISUSER
                targets.put(server, arguments[3]);
                break;
            case 318: // RPL_ENDOFWHOIS
                targets.remove(server);
                break;
            default:
                if (format.length() == 0 && arguments.length > 4
                        && targets.containsKey(server)
                        && arguments[3].equals(targets.get(server))) {
                    // This numeric should be automatically formatted.

                    final String target = "numeric_auto_" + (arguments.length - 4);
                    ensureExists(target, arguments.length);
                    format.replace(0, format.length(), target);
                }
        }
    }

    /**
     * Ensures that the specified formatter exists in our identity.
     *
     * @param target The target to be checked and added if necessary
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
            identity.setOption("notifications", target, "group:whois");
        }
    }

}
