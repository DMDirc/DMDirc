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

package com.dmdirc.commandparser;

/**
 * A basic {@link CommandInfo} implementation whose parameters can be configured via the
 * constructor.
 */
public class BaseCommandInfo implements CommandInfo {

    /** The name of the command. */
    private final String name;
    /** The help text for this command. */
    private final String help;
    /** The type of the command. */
    private final CommandType type;

    public BaseCommandInfo(final String name, final String help, final CommandType type) {
        this.name = name;
        this.help = help;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHelp() {
        return help;
    }

    @Override
    public CommandType getType() {
        return type;
    }

}
