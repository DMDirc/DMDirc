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
    private final EventTemplateProvider templateProvider;

    @Inject
    public EventFormatter(final EventPropertyManager propertyManager,
            final EventTemplateProvider templateProvider) {
        this.propertyManager = propertyManager;
        this.templateProvider = templateProvider;
    }

    public Optional<String> format(final DisplayableEvent event) {
        final Optional<String> template = templateProvider.getTemplate(event.getClass());

        if (template.isPresent()) {
            final StringBuilder builder = new StringBuilder(template.get());
            int tagStart = builder.indexOf("{{");
            while (tagStart > -1) {
                final int tagEnd = builder.indexOf("}}", tagStart);
                final String tag = builder.substring(tagStart + 2, tagEnd);
                builder.replace(tagStart, tagEnd + 2, getReplacement(event, tag));
                tagStart = builder.indexOf("{{");
            }
            return Optional.ofNullable(builder.toString());
        }

        return Optional.empty();
    }

    private String getReplacement(final DisplayableEvent event, final String tag) {
        final String[] functionParts = tag.split("\\|");
        final String[] dataParts = functionParts[0].split("\\.");

        Object target = event;
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
