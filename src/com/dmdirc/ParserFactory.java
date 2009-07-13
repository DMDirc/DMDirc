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

package com.dmdirc;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.irc.ServerInfo;
import com.dmdirc.plugins.ExportedService;
import com.dmdirc.plugins.NoSuchProviderException;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.plugins.ServiceProvider;
import com.dmdirc.util.IrcAddress;

/**
 * Provides a method to retrieve a parser.
 *
 * @since 0.6
 * @author chris
 */
public class ParserFactory {
    
    /**
     * Retrieves a parser instance.
     * 
     * @param myInfo The client information to use
     * @param address The address of the server to connect to
     * @return An appropriately configured parser
     * @since 0.6.3m2
     */
    public Parser getParser(final MyInfo myInfo, final IrcAddress address) {
        // TODO: Hacky Hack McHack
        final ServerInfo info = new ServerInfo(address.getServer(), address.getPort(6667),
                address.getPassword());
        info.setSSL(address.isSSL());

        if ("irc-test".equals(address.getProtocol())) {
            try {
                return (Parser) Class.forName("com.dmdirc.harness.parser.TestParser")
                        .getConstructor(MyInfo.class, ServerInfo.class)
                        .newInstance(myInfo, info);
            } catch (Exception ex) {
                Logger.userError(ErrorLevel.UNKNOWN, "Unable to create parser", ex);
            }
        }

        try {
            if (address.getProtocol() != null) {
                final Service service = PluginManager.getPluginManager().getService("parser", address.getProtocol());
                final ServiceProvider provider = service.getProviders().get(0);
                provider.activateServices();
                if (provider != null) {
                    final ExportedService exportService = provider.getExportedService("getParser");
                    final Object obj = exportService.execute(myInfo, address);
                    if (obj != null && obj instanceof Parser) {
                        return (Parser)obj;
                    } else {
                        Logger.userError(ErrorLevel.UNKNOWN, "Unable to create parser for: "+address.getProtocol());
                    }
                }
            }
        } catch (NoSuchProviderException nspe) {
            Logger.userError(ErrorLevel.UNKNOWN, "No parser found for: "+address.getProtocol());
        }

        return new IRCParser(myInfo, info);
    }

}
