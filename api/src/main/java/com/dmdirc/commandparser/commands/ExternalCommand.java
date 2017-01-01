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

package com.dmdirc.commandparser.commands;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.WindowModel;

/**
 * An external command is a channel command that can be executed from outside of the channel.
 */
public interface ExternalCommand {

    /**
     * Executes the command externally.
     *
     * @param origin   The window in which the command was typed
     * @param connection The connection the command is being executed on.
     * @param channel  The name of the channel the command is being executed for
     * @param isSilent Whether this command is silenced or not
     * @param args     Arguments passed to this command
     */
    void execute(WindowModel origin, Connection connection, String channel,
            boolean isSilent, CommandArguments args);

}
