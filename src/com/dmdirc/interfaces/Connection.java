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

package com.dmdirc.interfaces;

import com.dmdirc.FrameContainer;
import com.dmdirc.Invite;
import com.dmdirc.Query;
import com.dmdirc.ServerState;
import com.dmdirc.ServerStatus;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.parser.common.IgnoreList;
import com.dmdirc.parser.interfaces.Parser;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Represents an abstract connection to a remote chat system.
 */
public interface Connection {

    /**
     * Attempts to accept the specified invites, and join the corresponding channels.
     *
     * @param invites The invites to process
     *
     * @since 0.6.4
     */
    void acceptInvites(final Invite... invites);

    /**
     * Attempts to accept all active invites for this server, and join the corresponding channels.
     *
     * @since 0.6.4
     */
    void acceptInvites();

    /**
     * Adds an invite to this server, and fires the appropriate listeners.
     *
     * @param invite The invite to be added
     */
    void addInvite(final Invite invite);

    /**
     * Passes the arguments to all frames for this server.
     *
     * @param messageType The type of message to send
     * @param date        The date at which the event occurred
     * @param args        The arguments of the message
     */
    void addLineToAll(final String messageType, final Date date, final Object... args);

    /**
     * Compare the given URI to the URI we are currently using to see if they would both result in
     * the server connecting to the same place, even if the URIs do not match exactly.
     *
     * @param uri URI to compare with the Servers own URI.
     *
     * @return True if the Given URI is the "same" as the one we are using.
     *
     * @since 0.6.3
     */
    boolean compareURI(final URI uri);

    /**
     * Connects to a new server with the previously supplied address and profile.
     *
     * @since 0.6.3m2
     */
    void connect();

    /**
     * Connects to a new server with the specified details.
     *
     * @param address The address of the server to connect to
     * @param profile The profile to use
     *
     * @since 0.6.3
     */
    void connect(final URI address, final Profile profile);

    /**
     * Removes a specific channel and window from this server.
     *
     * @param chan channel to remove
     */
    void delChannel(final String chan);

    /**
     * Deletes a query from this server.
     *
     * @param query The query that should be removed.
     */
    void delQuery(final Query query);

    /**
     * Disconnects from the server with the default quit message.
     */
    void disconnect();

    /**
     * Disconnects from the server.
     *
     * @param reason disconnect reason
     */
    void disconnect(final String reason);

    /**
     * Retrieves the address of this server.
     *
     * @return This sever's address
     */
    String getAddress();

    /**
     * Gets the current away message.
     *
     * @return Null if the client isn't away, or a textual away message if it is
     */
    String getAwayMessage();

    /**
     * Retrieves the specified channel belonging to this server.
     *
     * @param channel The channel to be retrieved
     *
     * @return The appropriate channel object
     */
    Optional<GroupChat> getChannel(final String channel);

    /**
     * Retrieves the possible channel prefixes in use on this server.
     *
     * @return This server's possible channel prefixes
     */
    String getChannelPrefixes();

    /**
     * Gets a collection of all channels on this connection.
     *
     * @return collection of channels belonging to this connection
     */
    Collection<GroupChat> getChannels();

    /**
     * Retrieves this server's ignore list.
     *
     * @return This server's ignore list
     */
    IgnoreList getIgnoreList();

    /**
     * Returns the list of invites for this server.
     *
     * @return Invite list
     */
    List<Invite> getInvites();

    /**
     * Retrieves the name of this server's IRCd.
     *
     * @return The name of this server's IRCd
     */
    String getIrcd();

    /**
     * Retrieves the name of this server's network. The network name is determined using the
     * following rules:
     *
     * 1. If the server includes its network name in the 005 information, we use that 2. If the
     * server's name ends in biz, com, info, net or org, we use the second level domain (e.g.,
     * foo.com) 3. If the server's name contains more than two dots, we drop everything up to and
     * including the first part, and use the remainder 4. In all other cases, we use the full server
     * name
     *
     * @return The name of this server's network
     */
    String getNetwork();

    /**
     * Retrieves the identity for this server's network.
     *
     * @return This server's network identity
     */
    ConfigProvider getNetworkIdentity();

    /**
     * Retrieves the parser used for this connection.
     *
     * @return this connection's parser
     */
    @Nonnull
    Optional<Parser> getParser();

    /**
     * Retrieves the profile that's in use for this server.
     *
     * @return The profile in use by this server
     */
    Profile getProfile();

    /**
     * Retrieves the protocol used by this server.
     *
     * @return This server's protocol
     *
     * @since 0.6.3
     */
    String getProtocol();

    /**
     * Retrieves a list of queries belonging to this server.
     *
     * @return list of queries belonging to this server
     */
    Collection<Query> getQueries();

    /**
     * Retrieves the specified query belonging to this server. If the query does not yet exist, it
     * is created automatically.
     *
     * @param host The host of the query to look for
     *
     * @return The appropriate query object
     */
    Query getQuery(final String host);

    /**
     * Retrieves the specified query belonging to this server. If the query does not yet exist, it
     * is created automatically.
     *
     * @param host  The host of the query to look for
     * @param focus Should we focus the window on open?
     *
     * @return The appropriate query object
     */
    Query getQuery(final String host, final boolean focus);

    /**
     * Retrieves the identity for this server.
     *
     * @return This server's identity
     */
    ConfigProvider getServerIdentity();

    /**
     * Retrieves the current state for this server.
     *
     * @return This server's state
     */
    ServerState getState();

    /**
     * Retrieves the status object for this server. Effecting state transitions on the object
     * returned by this method will almost certainly cause problems.
     *
     * @since 0.6.3m1
     * @return This server's status object.
     */
    ServerStatus getStatus();

    /**
     * Determines whether the server knows of the specified query.
     *
     * @param host The host of the query to look for
     *
     * @return True iff the query is known, false otherwise
     */
    boolean hasQuery(final String host);

    /**
     * Returns a {@link User} object representing the local client.
     *
     * @return Local user, or empty if there is no local client
     */
    User getLocalUser();

    /**
     * Returns a {@link User} object representing the specified details.
     *
     * @return Retrieved user, or empty if there was no match
     */
    User getUser(final String details);

    /**
     * Returns the current away status.
     *
     * @return True if the client is marked as away, false otherwise
     */
    boolean isAway();

    /**
     * Determines whether this server is currently connected to the specified network.
     *
     * @param target The network to check for
     *
     * @return True if this server is connected to the network, false otherwise
     *
     * @since 0.6.3m1rc3
     */
    boolean isNetwork(final String target);

    /**
     * Determines if the specified channel name is valid. A channel name is valid if we already have
     * an existing Channel with the same name, or we have a valid parser instance and the parser
     * says it's valid.
     *
     * @param channelName The name of the channel to test
     *
     * @return True if the channel name is valid, false otherwise
     */
    boolean isValidChannelName(final String channelName);

    /**
     * Attempts to join the specified channels. If channels with the same name already exist, they
     * are (re)joined and their windows activated.
     *
     * @param requests The channel join requests to process
     *
     * @since 0.6.4
     */
    void join(final ChannelJoinRequest... requests);

    /**
     * Attempts to join the specified channels. If channels with the same name already exist, they
     * are (re)joined.
     *
     * @param focus    Whether or not to focus any new channels
     * @param requests The channel join requests to process
     *
     * @since 0.6.4
     */
    void join(final boolean focus, final ChannelJoinRequest... requests);

    /**
     * Reconnects to the server with a specified reason.
     *
     * @param reason The quit reason to send
     */
    void reconnect(final String reason);

    /**
     * Reconnects to the server.
     */
    void reconnect();

    /**
     * Removes an invite from this server, and fires the appropriate listeners.
     *
     * @param invite The invite to be removed
     */
    void removeInvite(final Invite invite);

    /**
     * Removes all invites for the specified channel.
     *
     * @param channel The channel to remove invites for
     */
    void removeInvites(final String channel);

    /**
     * Removes all invites for all channels.
     */
    void removeInvites();

    /**
     * Saves the contents of our ignore list to the network identity.
     */
    void saveIgnoreList();

    /**
     * Replies to an incoming CTCP message.
     *
     * @param source The source of the message
     * @param type   The CTCP type
     * @param args   The CTCP arguments
     */
    void sendCTCPReply(final String source, final String type, final String args);

    /**
     * Updates our away state and fires the relevant listeners.
     *
     * @param message The away message to use, empty is not away.
     */
    void updateAwayState(final Optional<String> message);

    /**
     * Updates this server's ignore list to use the entries stored in the config manager.
     */
    void updateIgnoreList();

    /**
     * Updates the state of this server following a nick change of someone that the user has a query
     * open with. Namely, this updates the tabcompleter with the new name, and ensures that the
     * <code>queries</code> map uses the correct nickname.
     *
     * @param query   The query object being updated
     * @param oldNick The old nickname of the user
     * @param newNick The new nickname of the user
     *
     * @since 0.6.4
     */
    void updateQuery(final Query query, final String oldNick, final String newNick);

    /**
     * Updates the name and title of this window.
     */
    void updateTitle();

    /**
     * Sends a raw line to the underlying connection.
     *
     * @param line The line to be sent
     */
    void sendLine(String line);

    /**
     * Gets the core model for the input/output window for this connection.
     *
     * @return A model for windows based on this connection.
     */
    FrameContainer getWindowModel();

    /**
     * Returns the available channel modes applicable to users.
     *
     * @return User modes in ascending order, or an empty string if they're not known
     */
    String getUserModes();

    /**
     * Returns the available boolean modes.
     *
     * @return Boolean modes or an empty string if they're not known
     */
    String getBooleanModes();

    /**
     * Returns the available list modes.
     *
     * @return List modes or an empty string if they're not known
     */
    String getListModes();

    /**
     * Returns the available parameter modes.  Parameter modes need a parameter to set, but not to
     * unset.
     *
     * @return Parameter modes or an empty string if they're not known
     */
    String getParameterModes();

    /**
     * Returns the available double parameter modes.  Double parameter modes need a parameter to
     * both set and unset.
     *
     * @return Double parameter modes or an empty string if they're not known
     */
    String getDoubleParameterModes();

    /**
     * Returns the maximum number list modes of a certain type that can be set.
     *
     * @param mode Mode to query
     *
     * @return Maximum modes that can be set, or -1 if they're not known
     */
    int getMaxListModes(final char mode);

}
