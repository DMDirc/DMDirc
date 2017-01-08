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

package com.dmdirc.commandparser.aliases;

import com.dmdirc.commandparser.CommandType;
import com.dmdirc.interfaces.CommandController;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles the creation of new aliases.
 */
@Singleton
public class AliasFactory {

    /** The controller to use to trim command chars. */
    private final CommandController commandController;

    @Inject
    public AliasFactory(final CommandController commandController) {
        this.commandController = commandController;
    }

    /**
     * Creates a new alias.
     *
     * @param name         The name of the alias to be created.
     * @param minArguments The minimum number of arguments the alias requires.
     * @param substitution The command to substitute when the alias is executed.
     *
     * @return A new alias with the given attributes.
     */
    public Alias createAlias(final String name, final int minArguments, final String substitution) {
        checkNotNull(name);
        checkArgument(!Strings.isNullOrEmpty(name));
        checkArgument(minArguments >= 0);
        checkNotNull(substitution);

        // TODO: Infer command type from the substituted command
        return new Alias(CommandType.TYPE_GLOBAL, removeCommandChar(name), minArguments,
                removeCommandChar(substitution));
    }

    private String removeCommandChar(final String input) {
        // TODO: This could be moved into the command controller, or a utility class.
        // CommandArguments does something similar.
        final List<String> lines = Splitter.on(CharMatcher.anyOf("\r\n"))
                .omitEmptyStrings()
                .splitToList(input);
        final List<String> trimmedLines = new ArrayList<>(lines.size());
        trimmedLines.addAll(lines.stream()
                .map(line -> line.charAt(0) == commandController.getCommandChar() ?
                        line.length() > 1 && line.charAt(1) == commandController.getSilenceChar() ?
                                line.substring(2) : line.substring(1) : line)
                .collect(Collectors.toList()));
        return Joiner.on("\r\n").join(trimmedLines);
    }

}
