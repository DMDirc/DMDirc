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

package com.dmdirc.commandparser.commands.chat;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.ValidatingCommand;
import com.dmdirc.commandparser.commands.context.ChatCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.Chat;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.util.validators.ValidationResponse;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The me command sends a CTCP action to the current channel.
 */
public class Me extends Command implements ValidatingCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("me",
            "me <action> - sends the specified action",
            CommandType.TYPE_CHAT);

    /**
     * Creates a new instance of {@link Me} using the given command controller.
     *
     * @param controller The controller to use for command information.
     */
    @Inject
    public Me(final CommandController controller) {
        super(controller);
    }

    @Override
    public void execute(@Nonnull final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final Chat target = ((ChatCommandContext) context).getChat();
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "me", "<action>");
        } else {
            target.sendAction(args.getArgumentsAsString());
        }
    }

    @Override
    public ValidationResponse validateArguments(
            final FrameContainer origin,
            final CommandArguments arguments) {
        final int length = 2 + arguments.getArgumentsAsString().length();

        return origin.getConnection()
                .flatMap(Connection::getParser)
                .map(p -> p.getMaxLength("PRIVMSG", origin.getName()))
                .map(l -> l <= length ? new ValidationResponse("Too long") : new ValidationResponse())
                .orElse(new ValidationResponse());
    }

}
