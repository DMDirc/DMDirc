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
 * Describes a command.
 */
public interface CommandInfo {

    /**
     * Returns this command's name.
     *
     * @return The name of this command
     */
    String getName();

    /**
     * Returns a string representing the help message for this command.
     * <p>
     * The help text should generally be one line, and must start with the name of the command. It
     * should then summarise the arguments of that command, using <code>&lt;angled&gt;</code>
     * brackets for required arguments, and <code>[square]</code> brackets for optional arguments.
     * Where multiple possibilities exist, they are typically separated by a pipe ( <code>|</code>),
     * for example: <code>command [--arg1|--arg2]</code>. The usage summary should then be followed
     * by a dash and a brief summary of the purpose of the command.
     * <p>
     * A typical help message would look similar to:
     * <p>
     * <code>command [--arg &lt;param_for_arg&gt;] [someparam] - does x, y and z</code>
     *
     * @return the help message for this command
     */
    String getHelp();

    /**
     * Retrieves the type of this command.
     *
     * @return This command's type
     */
    CommandType getType();

}
