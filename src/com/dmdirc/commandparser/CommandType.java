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

package com.dmdirc.commandparser;

/**
 * Defines the possible targets for commands.
 */
public enum CommandType {

    /** A global command, which may be executed anywhere. */
    TYPE_GLOBAL,
    /** A server command, which only makes sense in the context of a
     * connection. */
    TYPE_SERVER,
    /** A chat command, which needs a MessageTarget to make sense. */
    TYPE_CHAT,
    /** A channel command. */
    TYPE_CHANNEL,
    /** A query command. */
    TYPE_QUERY;

    /**
     * Retrieves an array of component types that make up this command type. Generally this will
     * only contain the type itself, but some commands may be registered in multiple queues (such as
     * CHANNEL commands going into both CHAT and CHANNEL queues). Note that for obvious reasons
     * there is no recursion done on the values returned here.
     *
     * @since 0.6.3m1
     * @return An array of types which this type should be registered as.
     */
    public CommandType[] getComponentTypes() {
        if (this == TYPE_CHANNEL || this == TYPE_QUERY) {
            return new CommandType[]{this, TYPE_CHAT};
        } else {
            return new CommandType[]{this};
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case TYPE_CHANNEL:
                return "Channel";
            case TYPE_CHAT:
                return "Chat";
            case TYPE_GLOBAL:
                return "Global";
            case TYPE_QUERY:
                return "Query";
            case TYPE_SERVER:
                return "Server";
            default:
                return "Unknown";
        }
    }

}
