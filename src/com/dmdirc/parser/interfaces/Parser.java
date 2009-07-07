/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.parser.interfaces;

import com.dmdirc.parser.irc.IRCStringConverter;
import com.dmdirc.parser.irc.RegexStringList;
import com.dmdirc.parser.common.CallbackManager;

import java.util.Collection;

/**
 * A parser connects to a back-end chat system and handles all communication
 * with it.
 *
 * @since 0.6.3m2
 * @author chris
 */
public interface Parser extends Runnable {

    /**
     * Disconnect from server. This method will quit and automatically close the
     * socket without waiting for the server.
     *
     * @param message Reason for quitting.
     */
    void disconnect(String message);

    /**
     * Join a channel with no key.
     *
     * @param channel Name of channel to join
     */
    void joinChannel(String channel);

    /**
     * Joins a channel with the specified key.
     *
     * @param channel Name of channel to join
     * @param key The key required to join the channel
     */
    void joinChannel(String channel, String key);

    /**
     * Retrieves a channel information object for the specified channel.
     *
     * @param channel Name of the channel to retrieve an information object for
     * @return A corresponding channel info object
     */
    ChannelInfo getChannel(String channel);

    /**
     * Retrieves a collection of all known channels.
     *
     * @return A collection of known channels
     */
    Collection<? extends ChannelInfo> getChannels();

    /**
     * Set the IP address that this parser will bind to
     *
     * @param ip IP to bind to
     */
    void setBindIP(String ip);

    /**
     * Determines the maximimum length a message of the specified type may be.
     *
     * @param type Type of message (eg PRIVMSG)
     * @param target Target of message (eg channel name)
     * @return The maximum length of the message
     */
    int getMaxLength(String type, String target);

    /**
     * Returns a {@link ClientInfo} object which represents the locally
     * connected client.
     *
     * @return An info object for the local client
     */
    LocalClientInfo getLocalClient();

    /**
     * Retrieves a {@link ClientInfo} object which corresponds to the specified
     * details. If the client wasn't previously known, it will be created.
     *
     * @param details The details of the client to look up
     * @return A corresponding client info object
     */
    ClientInfo getClient(String details);

    /**
     * Sends a raw message directly to the backend system. The message will
     * need to be of the appropriate format for whatever system is in use.
     * 
     * @param message The message to be sent
     */
    void sendRawMessage(String message);

    /**
     * Retrieves an object that can be used to convert between upper- and lower-
     * case strings in the relevant charset for the backend system.
     *
     * @return A string convertor for this parser
     */
    IRCStringConverter getStringConverter();

    /**
     * Determines whether the specified channel name is valid or not for this
     * parser.
     *
     * @param name The name of the channel to be tested
     * @return True if the channel name is valid, false otherwise
     */
    boolean isValidChannelName(String name);

    /**
     * Retrieves the name of the server that this parser is connected to.
     *
     * @return This parser's server's name
     */
    String getServerName();

    /**
     * Retrieves the name of the network that this parser is connected to.
     *
     * @return This parser's network's name
     */
    String getNetworkName();

    /**
     * Retrieves a textual description of the software running on the server.
     *
     * @return This parser's server's software name
     */
    String getServerSoftware();

    /**
     * Retrieves the detected type of the software running on the server
     *
     * @return This parser's server's software type
     */
    String getServerSoftwareType();

    /**
     * Retrieves the maximum length for a topic that can be set by this parser.
     *
     * @return The maximum length (in bytes) of a topic
     */
    int getMaxTopicLength();

    /**
     * Retrieves an alphabetically-sorted list of boolean channel modes.
     * Boolean channel modes may only be set or unset, and do not take any
     * arguments.
     *
     * @return A string containing a list of channel mode characters
     */
    String getBooleanChannelModes();

    /**
     * Retrieves an alphabetically-sorted list of channel list modes.
     * List channel modes may be set multiple times with different arguments,
     * building up a "list" of values.
     *
     * @return A string containing a list of channel mode characters
     */
    String getListChannelModes();

    /**
     * Retrieves the maximum number of list modes of the specified type which
     * may be set on a channel. Returns 0 or -1 if the limit wasn't specified,
     * or couldn't be discovered, respectively.
     *
     * @param mode The list mode being requested
     * @return The maximimum number of that mode which can be set
     */
    int getMaxListModes(char mode);

    /**
     * Determines if the specified channel mode is settable by users.
     *
     * @param mode The mode to be tested
     * @return True if users may set the mode, false otherwise
     */
    boolean isUserSettable(final char mode);

    /**
     * Retrieves an alphabetically-sorted list of 'parameter' channel modes.
     * Parameter channel modes may only be set or unset, and require a
     * parameter to be specified when they are set (but not when unset).
     *
     * @return A string containing a list of channel mode characters
     */
    String getParameterChannelModes();

    /**
     * Retrieves an alphabetically-sorted list of 'double parameter' channel
     * modes. Double parameter channel modes may only be set or unset, and
     * require a parameter to be specified both when they are set and when
     * they are unset.
     *
     * @return A string containing a list of channel mode characters
     */
    String getDoubleParameterChannelModes();

    /**
     * Retrieves a list of user modes in no particular order.
     *
     * @return A string containing a list of user mode characters
     */
    String getUserModes();

    /**
     * Retrieves a list of channel user modes, in descending priority order.
     * 
     * @return A string containing a list of channel user mode characters
     */
    String getChannelUserModes();
    
    /**
     * Retrieves the object which is responsible for managing callbacks for
     * this parser.
     * 
     * @return This parser's callback manager
     */
    CallbackManager<? extends Parser> getCallbackManager();

    /**
     * Retrieves the latency between the parser and the server in milliseconds.
     *
     * @return The current latency, in milliseconds
     */
    long getServerLatency();

    /**
     * Sends a CTCP of the specified type to the specified target.
     *
     * @param target The destination of the CTCP message
     * @param type The type of CTCP to send
     * @param message The content of the CTCP message
     */
    void sendCTCP(String target, String type, String message);

    /**
     * Sends a CTCP reply of the specified type to the specified target.
     *
     * @param target The destination of the CTCP reply
     * @param type The type of CTCP to reply to
     * @param message The content of the CTCP reply
     */
    void sendCTCPReply(String target, String type, String message);

    /**
     * Sends a message to the specified target.
     *
     * @param target The target to send the message to
     * @param message The message to be sent
     */
    void sendMessage(String target, String message);

    /**
     * Sends a notice to the specified target.
     *
     * @param target The target to send the notice to
     * @param message The message to be sent
     */
    void sendNotice(String target, String message);

    /**
     * Sends an action to the specified target.
     *
     * @param target The target to send the action to
     * @param message The message to be sent
     */
    void sendAction(String target, String message);

    /**
     * Retrieves the last line/communication received from the server, for use
     * in debugging purposes.
     *
     * @return The last line received
     */
    String getLastLine();

    /**
     * Sets the ignore list which should be used by this parser.
     *
     * @param ignoreList The new ignore list to be used by the parser
     */
    void setIgnoreList(RegexStringList ignoreList);

    /**
     * Retrieves the ignore list which is currently in use by this parser.
     *
     * @return This parser's ignore list
     */
    RegexStringList getIgnoreList();

    /**
     * Parses the specified hostmask into an array containing a nickname,
     * username and hostname, in that order.
     *
     * @param hostmask The hostmask to be parsed
     * @return An array containing the nickname, username and hostname
     */
    String[] parseHostmask(String hostmask);

    /**
     * Retrieves the local port number that this parser is using to communicate
     * with the service.
     *
     * @return This parser's local port number
     */
    int getLocalPort();

    /**
     * Retrieves the amount of time elapsed since the last ping request was
     * sent (or until the reply was received).
     *
     * @return The current ping time to the server
     */
    long getPingTime();

    /**
     * Sets the interval of the ping timer, in milliseconds. If the parser is
     * waiting for a ping, it should fire a PingFailed event every time this
     * interval is passed (or disconnect if no listeners are registered).
     *
     * @param newValue The new value for the ping timer interval
     */
    void setPingTimerInterval(long newValue);

    /**
     * Retrieves the length of the ping timer interval for this parser.
     *
     * @return This parser's ping timer interval
     */
    long getPingTimerInterval();

    /**
     * Sets how many ping timer intervals should pass before the parser sends
     * a ping. That is, the time between pings on an idle connection will be
     * <code>(ping timer interval) * (ping timer fraction)</code> millisecs.
     *
     * For example, setting the interval to 10,000 (10 seconds) and the fraction
     * to 6 means that pings will be sent once every minute and if a reply is
     * not received within 10 seconds, a ping failed event will be raised.
     *
     * @param newValue The new value of the ping timer fraction
     */
    void setPingTimerFraction(int newValue);

    /**
     * Retrieves the number of ping timer intervals that must pass before this
     * parser sends a ping request.
     *
     * @return This parser's ping timer fraction
     */
    int getPingTimerFraction();

}
