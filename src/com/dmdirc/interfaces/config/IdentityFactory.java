/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.interfaces.config;

import com.dmdirc.Precondition;

/**
 * Defines methods implemented by a factory which can create useful identities.
 */
public interface IdentityFactory {

    /**
     * Retrieves the config for the specified channel@network. The config is
     * created if it doesn't exist.
     *
     * @param network The name of the network
     * @param channel The name of the channel
     * @return A config source for the channel
     */
    @Precondition(value = {"The specified network is non-null and not empty", "The specified channel is non-null and not empty"})
    ConfigProvider createChannelConfig(final String network, final String channel);

    /**
     * Retrieves the config for the specified network. The config is
     * created if it doesn't exist.
     *
     * @param network The name of the network
     * @return A config source for the network
     */
    @Precondition(value = "The specified network is non-null and not empty")
    ConfigProvider createNetworkConfig(final String network);

    /**
     * Retrieves the config for the specified server. The config is
     * created if it doesn't exist.
     *
     * @param server The name of the server
     * @return A config source for the server
     */
    @Precondition(value = "The specified server is non-null and not empty")
    ConfigProvider createServerConfig(final String server);

}
