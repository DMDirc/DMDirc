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

package com.dmdirc;

import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.InputModel;
import com.dmdirc.ui.input.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Basic implementation of {@link InputModel}.
 */
public class DefaultInputModel implements InputModel {

    private final Consumer<String> lineConsumer;
    private final CommandParser commandParser;
    private final TabCompleter tabCompleter;
    private final Supplier<Integer> lineLengthSupplier;

    public DefaultInputModel(final Consumer<String> lineConsumer, final CommandParser commandParser,
            final TabCompleter tabCompleter, final Supplier<Integer> lineLengthSupplier) {
        this.lineConsumer = lineConsumer;
        this.commandParser = commandParser;
        this.tabCompleter = tabCompleter;
        this.lineLengthSupplier = lineLengthSupplier;
    }

    @Override
    public void sendLine(final String line) {
        lineConsumer.accept(line);
    }

    @Override
    public CommandParser getCommandParser() {
        return commandParser;
    }

    @Override
    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    @Override
    public int getMaxLineLength() {
        return lineLengthSupplier.get();
    }

    @Override
    public List<String> splitLine(final String line) {
        final List<String> result = new ArrayList<>();

        if (line.indexOf('\n') > -1) {
            for (String part : line.split("\n")) {
                result.addAll(splitLine(part));
            }
        } else {
            final StringBuilder remaining = new StringBuilder(line);

            while (getMaxLineLength() > -1 && remaining.toString().getBytes().length
                    > getMaxLineLength()) {
                int number = Math.min(remaining.length(), getMaxLineLength());

                while (remaining.substring(0, number).getBytes().length > getMaxLineLength()) {
                    number--;
                }

                result.add(remaining.substring(0, number));
                remaining.delete(0, number);
            }

            result.add(remaining.toString());
        }

        return result;
    }

    @Override
    public final int getNumLines(final String line) {
        final String[] splitLines = line.split("(\n|\r\n|\r)", Integer.MAX_VALUE);
        int lines = 0;

        for (String splitLine : splitLines) {
            if (getMaxLineLength() <= 0) {
                lines++;
            } else {
                lines += (int) Math.ceil(splitLine.getBytes().length
                        / (double) getMaxLineLength());
            }
        }

        return lines;
    }

}
