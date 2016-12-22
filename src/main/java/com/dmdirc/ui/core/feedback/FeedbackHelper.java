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

package com.dmdirc.ui.core.feedback;

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.util.ClientInfo;

import javax.inject.Inject;
import java.nio.file.Path;

public class FeedbackHelper {

    private final Path configDirectory;
    private final ClientInfo clientInfo;
    private final ConnectionManager connectionManager;
    private final AggregateConfigProvider config;

    @Inject
    public FeedbackHelper(@Directory(DirectoryType.BASE) final Path configDirectory,
            final ClientInfo clientInfo, final ConnectionManager connectionManager,
            @GlobalConfig final AggregateConfigProvider config) {
        this.configDirectory = configDirectory;
        this.clientInfo = clientInfo;
        this.connectionManager = connectionManager;
        this.config = config;
    }

    public String getDMDircInfo() {
        return "DMDirc version: " + clientInfo.getVersionInformation() + '\n'
                + "Profile directory: " + configDirectory + '\n' + "Java version: "
                + clientInfo.getJavaInformation() + '\n' + "OS Version: "
                + clientInfo.getOperatingSystemInformation();
    }

    public String getServerInfo() {
        final StringBuilder serverInfo = new StringBuilder(255);
        connectionManager.getConnections()
                .forEach(c -> serverInfo.append(getServerInfo(c)).append('\n'));
        return serverInfo.toString();
    }

    public String getServerInfo(final Connection connection) {
        if (connection.getState().isDisconnected()) {
            return "";
        }
        final Parser parser = connection.getParser().get();
        return "Actual name: " + parser.getServerName() + '\n'
                + "Network: " + connection.getNetwork() + '\n'
                + "IRCd: " + parser.getServerSoftware() + " - "
                + parser.getServerSoftwareType() + '\n'
                + "Modes: " + parser.getBooleanChannelModes() + ' '
                + parser.getListChannelModes() + ' '
                + parser.getParameterChannelModes() + ' '
                + parser.getDoubleParameterChannelModes();
    }

    public String getVersion() {
        return config.getOption("version", "version");
    }
}
