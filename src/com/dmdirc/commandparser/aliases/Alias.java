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

package com.dmdirc.commandparser.aliases;

import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Describes a user-defined command alias.
 */
public class Alias implements CommandInfo {

    /** The type of this alias. */
    private final CommandType type;
    /** The name of the alias. */
    private final String name;
    /** The minimum number of arguments this alias requires. */
    private final int minArguments;
    /** The command string to substitute arguments into. */
    private final String substitution;

    public Alias(
            final CommandType type,
            final String name,
            final int minArguments,
            final String substitution) {
        this.type = type;
        this.name = name;
        this.minArguments = minArguments;
        this.substitution = substitution;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHelp() {
        return name + " - alias for " + substitution.split(" ", 2)[0];
    }

    public int getMinArguments() {
        return minArguments;
    }

    public String getSubstitution() {
        return substitution;
    }

    @Override
    public CommandType getType() {
        return type;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof Alias)) {
            return false;
        }
        final Alias alias = (Alias) object;
        return Objects.equal(getName(), alias.getName())
                && Objects.equal(getMinArguments(), alias.getMinArguments())
                && Objects.equal(getSubstitution(), alias.getSubstitution())
                && Objects.equal(getType(), alias.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, minArguments, substitution, type);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("minargs", minArguments)
                .add("substitution", substitution)
                .add("type", type)
                .toString();
    }

}
