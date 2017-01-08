/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.commandline;

import com.dmdirc.config.GlobalConfig;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.util.InvalidURIException;
import com.dmdirc.util.system.SystemInfo;
import com.dmdirc.util.URIParser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Parses command line arguments for the client.
 */
@Singleton
public class CommandLineParser {

    /**
     * The arguments that the client supports, in groups of four, in the following order: short
     * option, long option, description, whether or not the option takes an argument.
     */
    private static final Object[][] ARGUMENTS = {
        {'c', "connect", "Connect to the specified server", Boolean.TRUE},
        {'d', "directory", "Use the specified configuration directory", Boolean.TRUE},
        {'e', "existing", "Try to use an existing instance of DMDirc (use with -c)", Boolean.FALSE},
        {'h', "help", "Show command line options and exit", Boolean.FALSE},
        {'l', "launcher", "Specifies the version of DMDirc's launcher", Boolean.TRUE},
        {'p', "portable", "Enable portable mode", Boolean.FALSE},
        {'r', "disable-reporting", "Disable automatic error reporting", Boolean.FALSE},
        {'v', "version", "Display client version and exit", Boolean.FALSE},
        {'k', "check", "Check if an existing instance of DMDirc exists.", Boolean.FALSE}
    };
    /** A list of addresses to autoconnect to. */
    private final List<URI> addresses = new ArrayList<>();
    /** Provider to use to get server managers. */
    @Nullable private final Provider<ConnectionManager> serverManagerProvider;
    /** Provider to use to get the global config. */
    @Nullable private final Provider<AggregateConfigProvider> globalConfigProvider;
    /** The parser to use for URIs. */
    @Nullable private final URIParser uriParser;
    /** Used to retrieve informationa about the running system. */
    private final SystemInfo systemInfo;
    /** Whether to disable error reporting or not. */
    private boolean disablereporting;
    /** The version string passed for the launcher. */
    private Optional<String> launcherVersion;
    /** The configuration directory. */
    private String configDirectory;
    /** The RMI server we're using. */
    private RemoteInterface server;

    /**
     * Creates a new instance of CommandLineParser.
     *
     * @param serverManagerProvider Provider to use to get server managers.
     * @param globalConfigProvider  Provider to use to get the global config.
     * @param uriParser             The parser to use for URIs.
     */
    @Inject
    public CommandLineParser(
            @Nullable final Provider<ConnectionManager> serverManagerProvider,
            @Nullable @GlobalConfig final Provider<AggregateConfigProvider> globalConfigProvider,
            @Nullable final URIParser uriParser,
            final SystemInfo systemInfo) {
        this.serverManagerProvider = serverManagerProvider;
        this.globalConfigProvider = globalConfigProvider;
        this.uriParser = uriParser;
        this.systemInfo = systemInfo;
        launcherVersion = Optional.empty();
    }

    /**
     * Parses the given arguments.
     *
     * @param arguments The arguments to be parsed
     */
    public void parse(final String... arguments) {
        boolean inArg = false;
        char previousArg = '.';

        for (String arg : arguments) {
            if (inArg) {
                processArgument(previousArg, arg);
                inArg = false;
            } else {
                if (arg.startsWith("--")) {
                    previousArg = processLongArg(arg.substring(2));
                    inArg = checkArgument(previousArg);
                } else if (arg.charAt(0) == '-') {
                    previousArg = processShortArg(arg.substring(1));
                    inArg = checkArgument(previousArg);
                } else {
                    doUnknownArg("Unknown argument: " + arg);
                }
            }
        }

        if (inArg) {
            doUnknownArg("Missing parameter for argument: " + previousArg);
        }

        if (server != null) {
            try {
                server.connect(addresses);
                System.exit(0);
            } catch (RemoteException ex) {
                System.err.println("Unable to execute remote connection: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        if (serverManagerProvider != null) {
            new RemoteServer(serverManagerProvider).bind();
        }
    }

    /**
     * Checks whether the specified arg type takes an argument. If it does, this method returns
     * true. If it doesn't, the method processes the argument and returns false.
     *
     * @param argument The short code of the argument
     *
     * @return True if the arg requires an argument, false otherwise
     */
    private boolean checkArgument(final char argument) {
        boolean needsArg = false;

        for (Object[] target : ARGUMENTS) {
            if (target[0].equals(argument)) {
                needsArg = (Boolean) target[3];
                break;
            }
        }

        if (needsArg) {
            return true;
        } else {
            processArgument(argument, null);

            return false;
        }
    }

    /**
     * Processes the specified string as a single long argument.
     *
     * @param arg The string entered
     *
     * @return The short form of the corresponding argument
     */
    private char processLongArg(final String arg) {
        for (Object[] target : ARGUMENTS) {
            if (arg.equalsIgnoreCase((String) target[1])) {
                return (Character) target[0];
            }
        }

        doUnknownArg("Unknown argument: " + arg);
        exit();

        return '.';
    }

    /**
     * Processes the specified string as a single short argument.
     *
     * @param arg The string entered
     *
     * @return The short form of the corresponding argument
     */
    private char processShortArg(final String arg) {
        for (Object[] target : ARGUMENTS) {
            if (arg.equals(String.valueOf(target[0]))) {
                return (Character) target[0];
            }
        }

        doUnknownArg("Unknown argument: " + arg);
        exit();

        return '.';
    }

    /**
     * Processes the specified command-line argument.
     *
     * @param arg   The short form of the argument used
     * @param param The (optional) string parameter for the option
     */
    private void processArgument(final char arg, final String param) {
        switch (arg) {
            case 'c':
                doConnect(param);
                break;
            case 'd':
                doDirectory(Paths.get(param));
                break;
            case 'e':
                doExisting();
                break;
            case 'k':
                doExistingCheck();
                break;
            case 'h':
                doHelp();
                break;
            case 'l':
                launcherVersion = Optional.ofNullable(param);
                break;
            case 'p':
                doDirectory(Paths.get(systemInfo.getProperty("user.dir")));
                break;
            case 'r':
                disablereporting = true;
                break;
            case 'v':
                doVersion();
                break;
            default:
                // This really shouldn't ever happen, but we'll handle it nicely
                // anyway.

                doUnknownArg("Unknown argument: " + arg);
                break;
        }
    }

    /**
     * Informs the user that they entered an unknown argument, prints the client help, and exits.
     *
     * @param message The message about the unknown argument to be displayed
     */
    private void doUnknownArg(final String message) {
        System.out.println(message);
        System.out.println();
        doHelp();
    }

    /**
     * Exits DMDirc.
     */
    private void exit() {
        System.exit(0);
    }

    /**
     * Handles the --connect argument.
     *
     * @param address The address the user told us to connect to
     */
    private void doConnect(final String address) {
        if (uriParser != null) {
            try {
                addresses.add(uriParser.parseFromText(address));
            } catch (InvalidURIException ex) {
                doUnknownArg("Invalid address specified: " + ex.getMessage());
            }
        } else {
            System.out.println("Unable to connect to address.");
            exit();
        }
    }

    /**
     * Handles the --existing argument.
     */
    private void doExisting() {
        server = RemoteServer.getServer();

        if (server == null) {
            System.err.println("Unable to connect to existing instance");
        }
    }

    /**
     * Handles the --check argument.
     */
    private void doExistingCheck() {
        if (RemoteServer.getServer() == null) {
            System.out.println("Existing instance not found.");
            System.exit(1);
        } else {
            System.out.println("Existing instance found.");
            System.exit(0);
        }
    }

    /**
     * Sets the config directory to the one specified.
     *
     * @param dir The new config directory
     */
    private void doDirectory(final Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException ex) {
                System.err.println("Unable to create directory " + dir);
                System.exit(1);
            }
        }
        configDirectory = dir.toAbsolutePath().toString() + File.separator;
        if (!Files.exists(Paths.get(configDirectory))) {
            System.err.println("Unable to resolve directory " + dir);
            System.exit(1);
        }
    }

    /**
     * Prints out the client version and exits.
     */
    private void doVersion() {
        System.out.println("DMDirc - a cross-platform, open-source IRC client.");
        System.out.println();
        if (globalConfigProvider == null) {
            System.out.println("Version: Unknown");
            exit();
        }
        final AggregateConfigProvider globalConfig = globalConfigProvider.get();
        System.out.println("        Version: " + globalConfig.getOption("version", "version"));
        System.out.println(" Update channel: " + globalConfig.getOption("updater", "channel"));
        exit();
    }

    /**
     * Prints out client help and exits.
     */
    private void doHelp() {
        System.out.println("Usage: java -jar DMDirc.jar [options]");
        System.out.println();
        System.out.println("Valid options:");
        System.out.println();

        int maxLength = 0;

        for (Object[] arg : ARGUMENTS) {
            final String needsArg = ((Boolean) arg[3]) ? " <argument>" : "";

            if ((arg[1] + needsArg).length() > maxLength) {
                maxLength = (arg[1] + needsArg).length();
            }
        }

        for (Object[] arg : ARGUMENTS) {
            final String needsArg = ((Boolean) arg[3]) ? " <argument>" : "";
            final StringBuilder desc = new StringBuilder(maxLength + 1);

            desc.append(arg[1]);

            while (desc.length() < maxLength + 1) {
                desc.append(' ');
            }

            System.out.print("   -" + arg[0] + needsArg);
            System.out.println(" --" + desc + needsArg + ' ' + arg[2]);
            System.out.println();
        }

        exit();
    }

    /**
     * Returns the user-supplied configuration directory.
     *
     * @return The user-supplied config directory, or {@code null} if none was supplied.
     */
    public String getConfigDirectory() {
        return configDirectory;
    }

    /**
     * Indicates whether the user has requested error reporting be disabled.
     *
     * @return True if the user has disabled reporting, false otherwise.
     */
    public boolean getDisableReporting() {
        return disablereporting;
    }

    /**
     * Returns the provided launcher version, if any.
     *
     * @return The version supplied by the launcher, or {@code null} if no launcher is identified.
     */
    public Optional<String> getLauncherVersion() {
        return launcherVersion;
    }

    /**
     * Processes arguments once the client has been loaded properly. This allows us to auto-connect
     * to servers, etc.
     *
     * @param connectionManager The server manager to use to connect servers.
     */
    public void processArguments(final ConnectionManager connectionManager) {
        addresses.forEach(connectionManager::connectToAddress);
    }

}
