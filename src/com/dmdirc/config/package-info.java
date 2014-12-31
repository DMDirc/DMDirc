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
 * Deals with client configuration and preferences.
 *
 * DMDirc's configuration system is based upon a set of 'identities'. Each identity has a specific
 * target which controls which elements of the client it is applied to. Identities can be targeted
 * to the client as a whole, and individual networks, servers or channels. Identities may also
 * specify an order, which controls in which order they are checked when looking up a configuration
 * setting.
 *
 * Most of the time configuration settings need to be sourced from multiple identities. For example,
 * channel settings will come from the channel's identities if any exist and if any contain the
 * setting in question. Failing that, they will come from the server's identities, then the
 * network's, and finally the global identities. This ordering and lookup behaviour is captured in
 * the {@link ConfigManager} class, which can determine which identities should be used in a
 * specific context and then use those to look up settings appropriately.
 *
 * Identities are all stored on disk under the 'identities' directory in the user's DMDirc profile.
 * Default identities in this package are extracted automatically by the {@link IdentityManager} if
 * they do not exist. Identities use the DMDirc {@link com.dmdirc.util.io.ConfigFile} file format.
 */
package com.dmdirc.config;
