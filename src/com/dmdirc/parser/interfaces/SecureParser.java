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

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

/**
 * A {@link SecureParser} is a specialised {@link Parser} that can connect
 * to secure (SSL-enabled) backends.
 *
 * @since 0.6.3m2
 * @author chris
 */
public interface SecureParser extends Parser {

    /**
     * Sets the trust managers which should be used to determine if a server
     * certificate is trusted by the client.
     *
     * @param managers The new trust managers to use
     */
    void setTrustManagers(TrustManager[] managers);

    /**
     * Sets the key managers which should be used to supply client certificates.
     *
     * @param managers The new key managers to use
     */
    void setKeyManagers(KeyManager[] managers);

}
