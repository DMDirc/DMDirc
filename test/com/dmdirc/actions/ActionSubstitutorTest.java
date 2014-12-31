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

package com.dmdirc.actions;

import com.dmdirc.Channel;
import com.dmdirc.Server;
import com.dmdirc.ServerState;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.parser.interfaces.ChannelClientInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class ActionSubstitutorTest {

    private static final Map<String, String> SETTINGS = new HashMap<>();

    private final String input, expected;
    private final Channel channel;
    private final ActionSubstitutor substitutor;
    private final Object[] args;

    @BeforeClass
    public static void setup() throws InvalidIdentityFileException {
        SETTINGS.put("alpha", "A");
        SETTINGS.put("bravo", "$alpha");
        SETTINGS.put("charlie", "${bravo}");
        SETTINGS.put("delta", "${${bravo}${bravo}}");
        SETTINGS.put("AA", "win!");
    }

    public ActionSubstitutorTest(final String input, final String expected) {
        this.input = input;
        this.expected = expected;

        this.channel = mock(Channel.class);

        final AggregateConfigProvider manager = mock(AggregateConfigProvider.class);
        final Server server = mock(Server.class);

        final ChannelClientInfo clientInfo = mock(ChannelClientInfo.class);

        when(channel.getConnection()).thenReturn(Optional.of(server));
        when(channel.getConfigManager()).thenReturn(manager);
        when(server.getState()).thenReturn(ServerState.DISCONNECTED);
        when(server.getAwayMessage()).thenReturn("foo");
        when(server.getProtocol()).thenReturn("$alpha");

        when(manager.getOptions(eq("actions"))).thenReturn(SETTINGS);
        for (Map.Entry<String, String> entry : SETTINGS.entrySet()) {
            when(manager.hasOptionString("actions", entry.getKey())).thenReturn(true);
            when(manager.getOption("actions", entry.getKey())).thenReturn(entry.getValue());
        }

        final ActionController controller = mock(ActionController.class);
        when(controller.getComponent("STRING_STRING")).thenReturn(CoreActionComponent.STRING_STRING);
        when(controller.getComponent("STRING_LENGTH")).thenReturn(CoreActionComponent.STRING_LENGTH);
        when(controller.getComponent("SERVER_NETWORK")).thenReturn(CoreActionComponent.SERVER_NETWORK);
        when(controller.getComponent("SERVER_PROTOCOL")).thenReturn(CoreActionComponent.SERVER_PROTOCOL);
        when(controller.getComponent("SERVER_MYAWAYREASON")).thenReturn(CoreActionComponent.SERVER_MYAWAYREASON);

        substitutor = new ActionSubstitutor(controller, mock(CommandController.class),
                mock(AggregateConfigProvider.class), CoreActionType.CHANNEL_MESSAGE);

        args = new Object[]{
            channel,
            clientInfo,
            "1 2 3 fourth_word_here 5 6 7"
        };
    }

    @Test
    public void testSubstitution() {
        assertEquals(expected, substitutor.doSubstitution(input, args));
    }

    @Parameterized.Parameters
    public static List<String[]> data() {
        final String[][] tests = {
            // -- Existing behaviour -------------------------------------------
            // ---- No subs at all ---------------------------------------------
            {"no subs here!", "no subs here!"},
            // ---- Config subs ------------------------------------------------
            {"$alpha", "A"},
            {"--$alpha--", "--A--"},
            // ---- Word subs --------------------------------------------------
            {"$1", "1"},
            {"$4", "fourth_word_here"},
            {"$5-", "5 6 7"},
            // ---- Component subs ---------------------------------------------
            {"${2.STRING_LENGTH}", "28"},
            {"${2.STRING_STRING}", "1 2 3 fourth_word_here 5 6 7"},
            // ---- Server subs ------------------------------------------------
            {"${SERVER_MYAWAYREASON}", "foo"},
            // ---- Combinations -----------------------------------------------
            {"${1}${2.STRING_LENGTH}$alpha", "128A"},
            {"$alpha$4${SERVER_MYAWAYREASON}", "Afourth_word_herefoo"},

            // -- New behaviour ------------------------------------------------
            // ---- Config subs ------------------------------------------------
            {"${alpha}", "A"},
            {"\\$alpha", "$alpha"},
            {"$bravo", "A"},
            {"$charlie", "A"},
            {"$delta", "win!"},
            {"$sigma", "not_defined"},
            // ---- Word subs --------------------------------------------------
            {"$5-6", "5 6"},
            {"${5-6}", "5 6"},
            {"${5-$6}", "5 6"},
            {"${5-${${6}}}", "5 6"},
            // ---- Component subs ---------------------------------------------
            {"${2.STRING_STRING.STRING_LENGTH}", "28"},
            {"${2.STRING_FLUB.STRING_LENGTH}", "illegal_component"},
            {"${SERVER_NETWORKFOO}", "illegal_component"},
            {"${SERVER_NETWORK}", "not_connected"},
            {"${SERVER_PROTOCOL}", "$alpha"},
            // ---- Escaping ---------------------------------------------------
            {"\\$1", "$1"},
            {"\\$alpha $alpha", "$alpha A"},
            {"\\$$1", "$1"},
            {"\\\\$4", "\\fourth_word_here"},
            {"\\\\${4}", "\\fourth_word_here"},
            {"\\\\\\$4", "\\$4"},
        };

        return Arrays.asList(tests);
    }

}
