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

import com.dmdirc.interfaces.Connection;
import com.dmdirc.util.ClientInfo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;
import com.getsentry.raven.dsn.Dsn;
import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.interfaces.ExceptionInterface;
import com.getsentry.raven.event.interfaces.MessageInterface;

/**
 * Facilitates reporting errors to the DMDirc developers.
 */
@Singleton
public class SentryErrorReporter {

    /** DSN used to connect to Sentry. */
    private static final String SENTRY_DSN = "https://d53a31a3c53c4a4f91c5ff503e612677:"
            + "e0a8aa1ecca14568a9f52d052ecf6a30@sentry.dmdirc.com/2?raven.async=false";
    /** Template to use when sending mode alias reports. */
    private static final String MODE_ALIAS_TEMPLATE = "%s\n\nConnection headers:\n%s";
    private final ClientInfo clientInfo;

    @Inject
    public SentryErrorReporter(final ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    /**
     * Sends an error report caused by some kind of exception.
     *
     * @param message   The message generated when the exception occurred.
     * @param level     The severity level of the error.
     * @param timestamp The timestamp that the error first occurred at.
     * @param exception The actual exception, if available.
     */
    public void sendException(
            final String message,
            final ErrorLevel level,
            final LocalDateTime timestamp,
            final Optional<Throwable> exception) {
        final EventBuilder eventBuilder = newEventBuilder()
                .withMessage(message)
                .withLevel(getSentryLevel(level))
                .withTimestamp(Date.from(timestamp.toInstant(ZoneOffset.UTC)));

        exception.ifPresent(e -> eventBuilder.withSentryInterface(new ExceptionInterface(e)));

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
                .withMessage(title)
                .withCulprit(title)
                .withLevel(Event.Level.INFO)
                .withTag("ircd.type", connection.getIrcd())
                .withTag("ircd.version", connection.getParser().get().getServerSoftware())
                .withTag("network", connection.getNetwork())
                .withTag("server", connection.getAddress())
                .withTag("modealiases", modeAliasVersion)
                .withSentryInterface(new MessageInterface(MODE_ALIAS_TEMPLATE, title,
                        connection.getParser().get().getServerInformationLines().toString()));

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
                .withServerName("")
                .withRelease(clientInfo.getVersion())
                .withTag("version", clientInfo.getVersion())
                .withTag("version.major", clientInfo.getMajorVersion())
                .withTag("os.name", clientInfo.getOperatingSystemName())
                .withTag("os.version", clientInfo.getOperatingSystemVersion())
                .withTag("os.arch", clientInfo.getOperatingSystemArchitecture())
                .withTag("encoding", clientInfo.getSystemFileEncoding())
                .withTag("locale", clientInfo.getSystemDefaultLocale())
                .withTag("jvm.name", clientInfo.getJavaName())
                .withTag("jvm.vendor", clientInfo.getJavaVendor())
                .withTag("jvm.version", clientInfo.getJavaVersion())
                .withTag("jvm.version.major", clientInfo.getJavaMajorVersion());
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
