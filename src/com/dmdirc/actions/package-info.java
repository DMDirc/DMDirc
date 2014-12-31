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

/**
 * 'Actions' provide a way for users to execute commands in response to certain events.
 *
 * <h2>Action types and meta-types</h2>
 *
 * Actions are executed in response to events in the client. Each event has a corresponding
 * {@link com.dmdirc.interfaces.actions.ActionType}, which has both a user-friendly name and an
 * internal name, as well as a meta-type which describes the arguments it takes.
 *
 * <p>
 * For example, when a message on a channel is received, the client looks for actions that respond
 * to the {@link com.dmdirc.actions.CoreActionType#CHANNEL_MESSAGE} action type. The channel message
 * type has a meta-type of
 * {@link com.dmdirc.actions.metatypes.ChannelEvents#CHANNEL_SOURCED_EVENT_WITH_ARG} which says that
 * the event will come with three arguments: a channel, a user, and a message. It also defines the
 * types of those arguments ({@link com.dmdirc.Channel},
 * {@link com.dmdirc.parser.interfaces.ChannelClientInfo}, and {@link java.lang.String}
 * respectively).
 *
 * <h2>Conditions</h2>
 *
 * Before an action is executed, its 'conditions' are checked. These are a simple collection of
 * rules concerning the state of the client, or the event's arguments. There are two types of
 * condition: component-based and string-based.
 *
 * <p>
 * Component-based conditions start off with one of the action's arguments, and then apply one or
 * more components to it to get some useful property. Components all implement
 * {@link com.dmdirc.interfaces.actions.ActionComponent} and essentially transform one object into
 * another, somehow. A component may take a {@link com.dmdirc.Channel} object and return that
 * channel's name as a string, or could take a string and return the length of it as an integer
 * (these components are implemented in {@link com.dmdirc.actions.CoreActionComponent#CHANNEL_NAME}
 * and {@link com.dmdirc.actions.CoreActionComponent#STRING_LENGTH}).
 *
 * <p>
 * A component based action could be as simple as "the message's content", or as complicated as "the
 * channel's server's network name's length". These chains of components are handled by an
 * {@link ActionComponentChain}.
 *
 * <p>
 * String-based conditions simply start off with a plain string, which is subject to substitution as
 * described below.
 *
 * <p>
 * Action conditions also specify a comparison. These define various methods of comparing objects,
 * such as checking two strings are equal
 * ({@link com.dmdirc.actions.CoreActionComparison#STRING_EQUALS}) or that an integer is greater
 * than a pre-defined value ({@link com.dmdirc.actions.CoreActionComparison#INT_GREATER}). All
 * comparisons implement {@link com.dmdirc.interfaces.actions.ActionComparison}. The second argument
 * is always a string provided by the user.
 *
 * <p>
 * Finally, if more than one condition is present the user can decide how they are matched. The two
 * most common and straight-forward options are a conjunction (where all the conditions must be
 * true) and a disjunction (where one of the conditions must be true). Users can also specify their
 * own, more complicated, rules such as "condition 1 AND (condition 2 OR condition 3)". These are
 * all expressed as a {@link ConditionTree}.
 *
 * <h2>Commands and substitutions</h2>
 *
 * When an action is triggered and its conditions are satisfied, it will execute a set of user
 * specified commands. These are executed on the relevant
 * {@link com.dmdirc.commandparser.parsers.CommandParser}, so are interpreted as though the user
 * typed them in the window where the event occurred. For example if in response to a channel
 * message the user had entered the command {@code /part}, then the client would part the channel
 * where the message was received.
 *
 * <p>
 * Commands and condition arguments are subject to action substitutions. This allows users to
 * include various dynamic properties in their responses or conditions. Substitutions are prefixed
 * with a {@code $} character, and come in several varieties:
 *
 * <ul>
 * <li>Configuration keys. Any setting in the 'actions' domain with the key 'key' can be substituted
 * using {@code $key}.</li>
 * <li>Words. Events that include a message allow substitution of individual words in the form
 * {@code $n} or ranges in the form {@code $n-} or {@code $n-m}, for some indices n and m.</li>
 * <li>Argument components. Any argument that is part of the action may be substituted in using the
 * format {@code ${n.component}}, where 'n' is the index of the argument and 'component' is the name
 * of the component to apply to it. Multiple components may be chained together:
 * {@code ${n.component1.component2}}.</li>
 * <li>Connection components. For convenience, any event that is triggered within the context of a
 * connection allows a shorthand form for connection-based substitutions. This is the same as an
 * argument-based substitution, but without the argument number. The substituter locates an
 * appropriate argument it can obtain a {@link com.dmdirc.interfaces.Connection} from and applies
 * the component to the relevant connection directly.</li>
 * </ul>
 */
package com.dmdirc.actions;
