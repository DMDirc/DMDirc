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

package com.dmdirc.logger;

import com.dmdirc.MissingModeAliasException;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.parser.interfaces.Parser;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import static com.dmdirc.util.LogUtils.APP_ERROR;

/**
 * Watches for newly connected servers and raises errors about their mode aliases.
 */
@Singleton
@Listener(references = References.Strong)
public class ModeAliasReporter {

    private static final Logger LOG = LoggerFactory.getLogger(ModeAliasReporter.class);

    /** The name of the server domain. */
    private static final String DOMAIN_SERVER = "server";

    @Inject
    public ModeAliasReporter() {
    }

    @Handler
    public void handleServerConnected(final ServerConnectedEvent event) {
        final Parser parser = event.getConnection().getParser().get();
        final AggregateConfigProvider configManager = event.getConnection().getWindowModel()
                .getConfigManager();

        // Check we have mode aliases
        final String modes = parser.getBooleanChannelModes()
                + parser.getListChannelModes()
                + parser.getParameterChannelModes()
                + parser.getDoubleParameterChannelModes();
        final String umodes = parser.getUserModes();

        final StringBuilder missingModes = new StringBuilder();
        final StringBuilder missingUmodes = new StringBuilder();

        for (char mode : modes.toCharArray()) {
            if (!configManager.hasOptionString(DOMAIN_SERVER, "mode" + mode)) {
                missingModes.append(mode);
            }
        }

        for (char mode : umodes.toCharArray()) {
            if (!configManager.hasOptionString(DOMAIN_SERVER, "umode" + mode)) {
                missingUmodes.append(mode);
            }
        }

        if (missingModes.length() + missingUmodes.length() > 0) {
            final StringBuilder missing = new StringBuilder("Missing mode aliases: ");

            if (missingModes.length() > 0) {
                missing.append("channel: +");
                missing.append(missingModes);
            }

            if (missingUmodes.length() > 0) {
                if (missingModes.length() > 0) {
                    missing.append(' ');
                }

                missing.append("user: +");
                missing.append(missingUmodes);
            }


            LOG.info(APP_ERROR, "Missing mode aliases", new MissingModeAliasException(
                            event.getConnection().getNetwork(),
                            parser,
                            configManager.getOption("identity", "modealiasversion"),
                            missing.toString()),
                    missing + " [" + parser.getServerSoftwareType() + ']', "");
        }
    }
}
