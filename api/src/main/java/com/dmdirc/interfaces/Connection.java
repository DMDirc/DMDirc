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

import com.dmdirc.ServerState;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.parser.common.IgnoreList;
import com.dmdirc.parser.interfaces.Parser;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Represents an abstract connection to a remote chat system.
 */
public interface Connection {

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
     * Deletes a query from this server.
     *
     * @param query The query that should be removed.
     */
    void delQuery(final PrivateChat query);

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
     * Retrieves this server's ignore list.
     *
     * @return This server's ignore list
     */
    IgnoreList getIgnoreList();

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
     * <ol>
     *  <li> If the server includes its network name in the 005 information, we use that
     *  <li> If the domain ends in a public suffix, the top private domain is used.
     *  <li> In all other cases, we use the full server name
     * </ol>
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
    Collection<PrivateChat> getQueries();

    /**
     * Retrieves the specified query belonging to this server. If the query does not yet exist, it
     * is created automatically.
     *
     * @param host The host of the query to look for
     *
     * @return The appropriate query object
     */
    PrivateChat getQuery(final String host);

    /**
     * Retrieves the specified query belonging to this server. If the query does not yet exist, it
     * is created automatically.
     *
     * @param host  The host of the query to look for
     * @param focus Should we focus the window on open?
     *
     * @return The appropriate query object
     */
    PrivateChat getQuery(final String host, final boolean focus);

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
    Optional<User> getLocalUser();

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
     * Sends a message to the specified target.
     *
     * @param target  target to send message to
     * @param message Message to send
     */
    void sendMessage(String target, String message);

    /**
     * Gets the core model for the input/output window for this connection.
     *
     * @return A model for windows based on this connection.
     */
    WindowModel getWindowModel();

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

    /**
     * Gets the manager that handles this connection's group chats.
     *
     * @return The group chat manager for this connection.
     */
    GroupChatManager getGroupChatManager();

    /**
     * Gets the manager that handles this connection's invites.
     *
     * @return The invite manager for this connection.
     */
    InviteManager getInviteManager();

    /**
     * Sets the local user's current nickname on this connection.
     *
     * @param nickname New nickname
     */
    void setNickname(final String nickname);

    /**
     * Returns the current nickname for this connection.
     *
     * @return Current nickname, or an empty if not present
     */
    Optional<String> getNickname();

    /**
     * Requests information about another user on the server.
     *
     * @param user User to request information about
     */
    void requestUserInfo(User user);

}
