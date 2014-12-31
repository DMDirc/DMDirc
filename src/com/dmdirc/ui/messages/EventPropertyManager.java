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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.logger.ErrorLevel;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EventPropertyManager {

    private final DMDircMBassador eventBus;
    private final Map<String, Function<String, String>> functions = new HashMap<>();

    @Inject
    public EventPropertyManager(final DMDircMBassador eventBus) {
        this.eventBus = eventBus;

        functions.put("uppercase", String::toUpperCase);
        functions.put("lowercase", String::toLowerCase);
        functions.put("trim", String::trim);
    }

    public <S> Optional<Object> getProperty(final S object, final Class<? extends S> type,
            final String property) {
        final String methodName = "get" + property.substring(0, 1).toUpperCase()
                + property.substring(1);
        try {
            final Method method = type.getMethod(methodName);
            return Optional.ofNullable(method.invoke(object));
        } catch (ReflectiveOperationException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex,
                    "Unable to format event: could not retrieve property " + property,
                    ex.getMessage()));
        }
        return Optional.empty();
    }

    public String applyFunction(final String input, final String function) {
        if (functions.containsKey(function)) {
            return functions.get(function).apply(input);
        }
        eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                "Unable to format event: no such function " + function, null));
        return input;
    }

}
