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

package com.dmdirc.logger;

import com.dmdirc.interfaces.Connection;
import com.dmdirc.util.ClientInfo;

import java.util.Date;

import javax.annotation.Nullable;

import net.kencochrane.raven.Raven;
import net.kencochrane.raven.RavenFactory;
import net.kencochrane.raven.dsn.Dsn;
import net.kencochrane.raven.event.Event;
import net.kencochrane.raven.event.EventBuilder;
import net.kencochrane.raven.event.interfaces.ExceptionInterface;
import net.kencochrane.raven.event.interfaces.MessageInterface;

/**
 * Facilitates reporting errors to the DMDirc developers.
 */
public class ErrorReporter {

    /** DSN used to connect to Sentry. */
    private static final String SENTRY_DSN = "https://d53a31a3c53c4a4f91c5ff503e612677:"
            + "e0a8aa1ecca14568a9f52d052ecf6a30@sentry.dmdirc.com/2?raven.async=false";
    /** Template to use when sending mode alias reports. */
    private static final String MODE_ALIAS_TEMPLATE = "%s\n\nConnection headers:\n%s";

    /**
     * Sends an error report caused by some kind of exception.
     *
     * @param message   The message generated when the exception occurred.
     * @param level     The severity level of the error.
     * @param timestamp The timestamp that the error first occurred at.
     * @param exception The actual exception, if available.
     * @param details   The details of the exception, if any.
     */
    public void sendException(
            final String message,
            final ErrorLevel level,
            final Date timestamp,
            @Nullable final Throwable exception,
            @Nullable final String details) {
        final EventBuilder eventBuilder = newEventBuilder()
                .setMessage(message)
                .setLevel(getSentryLevel(level))
                .setTimestamp(timestamp);

        if (exception != null) {
            eventBuilder.addSentryInterface(new ExceptionInterface(exception));
        }

        if (details != null) {
            eventBuilder.addExtra("details", details);
        }

        send(eventBuilder.build());
    }

    /**
     * Sends a report of missing mode aliases.
     *
     * @param connection       The connection that encountered the missing aliases.
     * @param channelModes     The missing channel modes (may be empty).
     * @param userModes        The missing user modes (may be empty).
     * @param modeAliasVersion The version of the mode aliases used.
     */
    public void sendModeAliasesReport(
            final Connection connection,
            final String channelModes,
            final String userModes,
            final String modeAliasVersion) {
        final String title = getModeAliasReportTitle(channelModes, userModes, connection.getIrcd());
        final EventBuilder eventBuilder = newEventBuilder()
                .setMessage(title)
                .setCulprit(title)
                .setLevel(Event.Level.INFO)
                .addTag("ircd.type", connection.getIrcd())
                .addTag("ircd.version", connection.getParser().getServerSoftware())
                .addTag("network", connection.getNetwork())
                .addTag("server", connection.getAddress())
                .addTag("modealiases", modeAliasVersion)
                .addSentryInterface(new MessageInterface(MODE_ALIAS_TEMPLATE, title,
                                connection.getParser().getServerInformationLines().toString()));

        send(eventBuilder.build());
    }

    /**
     * Constructs a title for a mode alias report.
     *
     * @param channelModes The missing channel modes (may be empty).
     * @param userModes    The missing user modes (may be empty).
     * @param ircd         The name of the IRCd that has the modes.
     *
     * @return A title string to use for mode alias reports.
     */
    private String getModeAliasReportTitle(final String channelModes, final String userModes,
            final String ircd) {
        final StringBuilder title = new StringBuilder("Missing mode aliases:");

        if (!channelModes.isEmpty()) {
            title.append(" channel: +").append(channelModes);
        }

        if (!userModes.isEmpty()) {
            title.append(" user: ").append(userModes);
        }

        title.append(" [").append(ircd).append(']');
        return title.toString();
    }

    /**
     * Creates a new event builder with DMDirc and platform information pre-populated.
     *
     * @return A new event builder.
     */
    private EventBuilder newEventBuilder() {
        return new EventBuilder()
                .setServerName("")
                .addTag("version", ClientInfo.getVersion())
                .addTag("version.major", ClientInfo.getMajorVersion())
                .addTag("os.name", ClientInfo.getOperatingSystemName())
                .addTag("os.version", ClientInfo.getOperatingSystemVersion())
                .addTag("os.arch", ClientInfo.getOperatingSystemArchitecture())
                .addTag("encoding", ClientInfo.getSystemFileEncoding())
                .addTag("locale", ClientInfo.getSystemDefaultLocale())
                .addTag("jvm.name", ClientInfo.getJavaName())
                .addTag("jvm.vendor", ClientInfo.getJavaVendor())
                .addTag("jvm.version", ClientInfo.getJavaVersion())
                .addTag("jvm.version.major", ClientInfo.getJavaMajorVersion());
    }

    /**
     * Connects to Sentry and sends the specified event.
     *
     * @param event The event to be sent.
     */
    private void send(final Event event) {
        final Raven raven = RavenFactory.ravenInstance(new Dsn(SENTRY_DSN));
        raven.sendEvent(event);
    }

    /**
     * Converts a DMDirc error level into a Sentry event level.
     *
     * @param level The DMDirc error level to convert.
     *
     * @return The corresponding sentry error level.
     */
    private static Event.Level getSentryLevel(final ErrorLevel level) {
        switch (level) {
            case FATAL:
                return Event.Level.FATAL;
            case HIGH:
                return Event.Level.ERROR;
            case MEDIUM:
                return Event.Level.WARNING;
            case LOW:
                return Event.Level.INFO;
            default:
                return Event.Level.INFO;
        }
    }

}
