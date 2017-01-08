/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.events.eventbus;

/**
 * An event bus offers facilities for publishing events to decoupled handlers.
 */
public interface EventBus {

    /**
     * Subscribe all handlers of the given listener. Any listener is only subscribed once; subsequent subscriptions of
     * an already subscribed listener will be silently ignored.
     */
    void subscribe(Object listener);

    /**
     * Immediately remove all registered handlers (if any) of the given listener. When this call returns all handlers
     * have effectively been removed and will not receive any messages.
     *
     * <p>A call to this method passing any object that is not subscribed will not have any effect and is silently
     * ignored.
     */
    void unsubscribe(Object listener);

    /**
     * Synchronously publish a message to all registered listeners. This includes listeners defined for super types of
     * the given message type, provided they are not configured to reject valid subtype. The call returns when all
     * matching handlers of all registered listeners have been notified (invoked) of the message.
     */
    void publish(BaseEvent message);

    /**
     * Asynchronously publish a message to all registered listeners. This includes listeners defined for super types of
     * the given message type, provided they are not configured to reject valid subtype. The call returns immediately.
     */
    void publishAsync(BaseEvent message);

}
