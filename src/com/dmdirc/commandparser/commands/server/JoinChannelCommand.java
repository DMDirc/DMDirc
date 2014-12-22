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

package com.dmdirc.commandparser.commands.server;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.events.ClientLineAddedEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.collections.MapList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;

/**
 * Allows the user to join channels.
 *
 * @since 0.6.3m1
 */
public class JoinChannelCommand extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("join",
            "join <channel [key]>[,channel [key]...] - joins the specified channel(s)",
            CommandType.TYPE_SERVER);
    /** A map of channel name mentions. */
    private final MapList<FrameContainer, String> mentions = new MapList<>();

    /**
     * Creates a new instance of the join channel command.
     *
     * @param controller       The controller to use to retrieve command information.
     * @param eventBus   The bus to listen on for events.
     */
    @Inject
    public JoinChannelCommand(
            final CommandController controller,
            final DMDircMBassador eventBus) {
        super(controller);
        eventBus.subscribe(this);
    }

    @Override
    public void execute(@Nonnull final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final Connection connection = ((ServerCommandContext) context).getConnection();
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "join", "join <channel [key]>[,channel [key]...]");
            return;
        }

        final List<ChannelJoinRequest> channels = new ArrayList<>();

        for (String pair : args.getArgumentsAsString().split(",")) {
            final int index = pair.trim().indexOf(' ');

            if (index == -1) {
                channels.add(new ChannelJoinRequest(pair));
            } else {
                channels.add(new ChannelJoinRequest(pair.substring(0, index),
                        pair.substring(index + 1)));
            }
        }

        connection.join(!args.isSilent(), channels.toArray(new ChannelJoinRequest[channels.size()]));
    }

    @Handler
    public void handleClientLineAdded(final ClientLineAddedEvent event) {
        final String[] parts = event.getFrameContainer().getBackBuffer().getStyliser()
                .doLinks(event.getLine())
                .split(Character.toString(Styliser.CODE_CHANNEL));

        for (int i = 1; i < parts.length; i += 2) {
            // All of the odd parts of the array are channel names
            mentions.add(event.getFrameContainer(), parts[i]);
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final FrameContainer source = context.getWindow();
        final Connection connection = source.getOptionalConnection().get();
        final List<String> results = checkSource(source, true, true);

        final AdditionalTabTargets targets = new AdditionalTabTargets().excludeAll();

        final String prefix;
        final int index;
        if ((index = context.getPartial().lastIndexOf(',')) > -1) {
            // If they are tab completing something containing a comma, we
            // add our results after the comma instead of returning them as-is.
            prefix = context.getPartial().substring(0, index + 1);
        } else {
            prefix = "";
        }

        final boolean showExisting = source.getConfigManager()
                .getOptionBool("commands", "join-tabexistingchannels");

        if (!showExisting) {
            // Only tab complete channels we're not already on
            targets.addAll(results.stream().filter(result -> !connection.hasChannel(result))
                    .map(result -> prefix + result).collect(Collectors.toList()));
        }

        for (char chPrefix : connection.getChannelPrefixes().toCharArray()) {
            // Let them tab complete the prefixes as well
            targets.add(prefix + chPrefix);
        }

        return targets;
    }

    /**
     * Checks a hierarchy of frame containers for channels which have been mentioned.
     *
     * @param source        The base frame container to check
     * @param checkParents  Whether or not to check that frame's parents
     * @param checkChildren Whether or not to check that frame's children
     *
     * @return A list of channel names which have been mentioned in the hierarchy
     *
     * @since 0.6.4
     */
    protected List<String> checkSource(final FrameContainer source,
            final boolean checkParents, final boolean checkChildren) {
        final List<String> results = new ArrayList<>();

        // Check the window itself
        if (mentions.containsKey(source)) {
            results.addAll(mentions.get(source));
        }

        // Check the parent window
        final Optional<FrameContainer> parent = source.getParent();
        if (checkParents && parent.isPresent()) {
            results.addAll(checkSource(parent.get(), true, false));
        }

        // Check the children window
        if (checkChildren) {
            for (FrameContainer child : source.getChildren()) {
                results.addAll(checkSource(child, false, true));
            }
        }

        return results;
    }

}
