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

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;

import javax.annotation.Nonnull;

/**
 * {@link Command} implementation that handles an alias.
 */
public class AliasCommandHandler extends Command {

    private final Alias alias;

    public AliasCommandHandler(final CommandController controller, final Alias alias) {
        super(controller);
        this.alias = alias;
    }

    @Override
    public void execute(@Nonnull final WindowModel origin, final CommandArguments args,
            final CommandContext context) {
        if (args.getArguments().length >= alias.getMinArguments()) {
            for (String line : alias.getSubstitution().split("\n")) {
                origin.getCommandParser().parseCommand((FrameContainer) origin,
                        getSubstituteCommand(line, args));
            }
        } else {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, alias.getName() + " requires at least "
                    + alias.getMinArguments() + " argument"
                    + (alias.getMinArguments() == 1 ? "" : "s") + ".");
        }
    }

    /**
     * Gets the command that should be executed, with the appropriate substitutions made.
     * <p>
     * The returned command will have arguments substituted (replacing "$1-", "$2", etc), and will
     * be silenced if the given args are silent.
     *
     * @param line The line to substitute.
     * @param args The arguments entered by the user.
     *
     * @return The substituted command to execute.
     */
    private String getSubstituteCommand(final String line, final CommandArguments args) {
        final StringBuilder builder = new StringBuilder(line.trim());

        final String[] arguments = args.getArguments();
        for (int i = 0; i < arguments.length; i++) {
            replaceAll(builder, "$" + (i + 1) + "-", args.getArgumentsAsString(i));
            replaceAll(builder, "$" + (i + 1), arguments[i]);
        }

        if (args.isSilent()) {
            builder.insert(0, getController().getSilenceChar());
        }
        builder.insert(0, getController().getCommandChar());

        return builder.toString();
    }

    /**
     * Replaces all instances of the specified substring in the builder.
     *
     * @param builder     The builder to modify.
     * @param substr      The substring to replace.
     * @param replacement The string to use as a replacement.
     */
    private static void replaceAll(final StringBuilder builder,
            final String substr, final String replacement) {
        int index = builder.indexOf(substr);
        while (index > -1) {
            builder.replace(index, index + substr.length(), replacement);
            index = builder.indexOf(substr);
        }
    }

}
