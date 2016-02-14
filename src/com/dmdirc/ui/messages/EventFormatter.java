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

import com.dmdirc.events.DisplayableEvent;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Formats an event into a text string, based on a user-defined template.
 *
 * <p>Template tags start with <code>{{</code> and end with <code>}}</code>. The start of the
 * tag should be the property of the event that will be displayed. Properties can be chained using
 * a <code>.</code> character, e.g. <code>{{user.hostname}}</code>. One or more functions can
 * be applied to the result of this, to change the appearance of the output. Functions are
 * separated from their argument with a <code>|</code> character,
 * e.g. <code>{{user.hostname|uppercase}}</code>.
 *
 * <p>Properties and functions are case-insensitive.
 */
@Singleton
public class EventFormatter {

    private static final String ERROR_STRING = "<FormatError>";

    private final EventPropertyManager propertyManager;
    private final EventFormatProvider formatProvider;

    @Inject
    public EventFormatter(final EventPropertyManager propertyManager,
            final EventFormatProvider formatProvider) {
        this.propertyManager = propertyManager;
        this.formatProvider = formatProvider;
    }

    public Optional<String> format(final DisplayableEvent event) {
        final Optional<EventFormat> format = formatProvider.getFormat(event.getClass());
        format.map(EventFormat::getDisplayProperties)
                .ifPresent(event.getDisplayProperties()::putAll);
        return format.map(f -> format(f, event));
    }

    private String format(final EventFormat format, final DisplayableEvent event) {
        final StringBuilder builder = new StringBuilder();
        format.getBeforeTemplate().ifPresent(
                before -> builder.append(doSubstitutions(event, before)).append('\n'));
        builder.append(
                format.getIterateProperty()
                        .map(iterate -> formatIterable(event, iterate, format.getTemplate()))
                        .orElseGet(() -> doSubstitutions(event, format.getTemplate())));
        format.getAfterTemplate().ifPresent(
                after -> builder.append('\n').append(doSubstitutions(event, after)));
        return builder.toString();
    }

    private String doSubstitutions(final Object dataSource, final String line) {
        final StringBuilder builder = new StringBuilder(line);
        int tagStart = builder.indexOf("{{");
        while (tagStart > -1) {
            final int tagEnd = builder.indexOf("}}", tagStart);
            final String tag = builder.substring(tagStart + 2, tagEnd);
            final String replacement = getReplacement(dataSource, tag);
            builder.replace(tagStart, tagEnd + 2, replacement);
            tagStart = builder.indexOf("{{", tagStart + replacement.length());
        }
        return builder.toString();
    }

    private String formatIterable(final DisplayableEvent event, final String property,
            final String template) {
        final Optional<Object> value
                = propertyManager.getProperty(event, event.getClass(), property);
        if (!value.isPresent() || !(value.get() instanceof Iterable<?>)) {
            return ERROR_STRING;
        }
        @SuppressWarnings("unchecked")
        final Iterable<Object> collection = (Iterable<Object>) value.get();
        final StringBuilder res = new StringBuilder();
        for (Object line : collection) {
            if (res.length() > 0) {
                res.append('\n');
            }
            res.append(doSubstitutions(line, template));
        }
        return res.toString();
    }

    private String getReplacement(final Object dataSource, final String tag) {
        final String[] functionParts = tag.split("\\|");
        final String[] dataParts = functionParts[0].split("\\.");

        Object target = dataSource;
        for (String part : dataParts) {
            final Optional<Object> result =
                    propertyManager.getProperty(target, target.getClass(), part);
            if (result.isPresent()) {
                target = result.get();
            } else {
                return ERROR_STRING;
            }
        }

        String value = target.toString();
        for (int i = 1; i < functionParts.length; i++) {
            value = propertyManager.applyFunction(value, functionParts[i]);
        }

        return value;
    }

}
