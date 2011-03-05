/*
 * Copyright (c) 2006-2011 DMDirc Developers
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
 * A basic {@link CommandInfo} implementation whose parameters can be
 * configured via the constructor.
 */
public class BaseCommandInfo implements CommandInfo {

    /** The name of the command. */
    private final String name;

    /** The help text for this command. */
    private final String helpText;

    /** The type of the command. */
    private final CommandType type;

    /**
     * Creates a new BaseCommandInfo with the specified information.
     *
     * @param name The name of the command
     * @param helpText The help text for this command
     * @param type The type of the command
     */
    public BaseCommandInfo(final String name, final String helpText,
            final CommandType type) {
        this.name = name;
        this.helpText = helpText;
        this.type = type;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return helpText;
    }

    /** {@inheritDoc} */
    @Override
    public CommandType getType() {
        return type;
    }

}
