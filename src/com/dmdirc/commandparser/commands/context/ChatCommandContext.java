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

package com.dmdirc.commandparser.commands.context;

import com.dmdirc.FrameContainer;
import com.dmdirc.MessageTarget;
import com.dmdirc.commandparser.CommandInfo;

/**
 * A command context specific to chat windows.
 *
 * @since 0.6.4
 */
public class ChatCommandContext extends ServerCommandContext {

    /** The chat container associated with this context. */
    private final MessageTarget chat;

    /**
     * Creates a new chat command context.
     *
     * @param source      The source of the command
     * @param commandInfo The command info object which associated the command with the input
     * @param chat        The chat container associated with the command
     */
    public ChatCommandContext(final FrameContainer source,
            final CommandInfo commandInfo, final MessageTarget chat) {
        super(source, commandInfo, chat.getConnection());
        this.chat = chat;
    }

    /**
     * Retrieves the chat container's associated with the command.
     *
     * @return This context's associated container
     */
    public MessageTarget getChat() {
        return chat;
    }

}
