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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.plugins.ServiceProvider;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.updater.UpdateChecker;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * Provides various handy ways to test or debug the client.
 *
 * @author Chris
 */
public class Debug extends GlobalCommand implements IntelligentCommand {
    
    /**
     * Creates a new instance of Debug.
     */
    public Debug() {
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent,
            final CommandArguments args) {
        if (args.getArguments().length == 0) {
            showUsage(origin, isSilent, "debug", "<debug command> [options]");
        } else if ("error".equals(args.getArguments()[0])) {
            doError(args.getArguments());
        } else if ("showraw".equals(args.getArguments()[0])) {
            doShowRaw(origin, isSilent);
        } else if ("configstats".equals(args.getArguments()[0])) {
            doConfigStats(origin, isSilent);
        } else if ("configinfo".equals(args.getArguments()[0])) {
            doConfigInfo(origin, isSilent);
        } else if ("globalconfiginfo".equals(args.getArguments()[0])) {
            doGlobalConfigInfo(origin, isSilent);
        } else if ("colourspam".equals(args.getArguments()[0])) {
            doColourSpam(origin, isSilent);
        } else if ("meminfo".equals(args.getArguments()[0])) {
            doMemInfo(origin, isSilent);
        } else if ("rungc".equals(args.getArguments()[0])) {
            doGarbage(origin, isSilent);
        } else if ("threads".equals(args.getArguments()[0])) {
            doThreads(origin, isSilent);
        } else if ("forceupdate".equals(args.getArguments()[0])) {
            doForceUpdate();
        } else if ("serverinfo".equals(args.getArguments()[0])) {
            doServerInfo(origin, isSilent);
        } else if ("serverstate".equals(args.getArguments()[0])) {
            doServerState(origin, isSilent);
        } else if ("benchmark".equals(args.getArguments()[0])) {
            doBenchmark(origin);
        } else if ("services".equals(args.getArguments()[0])) {
            doServices(origin, isSilent, args.getArguments());
        } else if ("firstrun".equals(args.getArguments()[0])) {
            Main.getUI().showFirstRunWizard();
        } else if ("migration".equals(args.getArguments()[0])) {
            Main.getUI().showMigrationWizard();
        } else if ("notify".equals(args.getArguments()[0])) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Current notification colour is: "
                    + origin.getContainer().getNotification());
        } else {
            sendLine(origin, isSilent, FORMAT_ERROR, "Unknown debug action.");
        }
    }
    
    /**
     * Generates a fake error.
     *
     * @param args The arguments that were passed to the command
     */
    private void doError(final String ... args) {
        ErrorLevel el = ErrorLevel.HIGH;
        if (args.length > 2) {
            final String level = args[2];
            
            if (level.equals("low")) {
                el = ErrorLevel.LOW;
            } else if (level.equals("medium")) {
                el = ErrorLevel.MEDIUM;
            } else if (level.equals("fatal")) {
                el = ErrorLevel.FATAL;
            } else if (level.equals("unknown")) {
                el = ErrorLevel.UNKNOWN;
            }
        }
        
        if (args.length > 1 && args[1].equals("user")) {
            Logger.userError(el, "Debug error message");
        } else {
            Logger.appError(el, "Debug error message", new Exception());
        }
    }
    
    /**
     * Attempts to show the server's raw window.
     *
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     */
    private void doShowRaw(final InputWindow origin, final boolean isSilent) {
        if (origin == null || origin.getContainer() == null
                || origin.getContainer().getServer() == null) {
            sendLine(origin, isSilent, FORMAT_ERROR, "Cannot show raw window here.");
        } else {
            origin.getContainer().getServer().addRaw();
        }
    }
    
    /**
     * Shows stats related to the config system.
     *
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     */
    private void doConfigStats(final InputWindow origin, final boolean isSilent) {
        final TreeSet<Entry<String, Integer>> sortedStats =
                new TreeSet<Entry<String, Integer>>(new ValueComparator());
        sortedStats.addAll(ConfigManager.getStats().entrySet());
        for (Map.Entry<String, Integer> entry : sortedStats) {
            sendLine(origin, isSilent, FORMAT_OUTPUT,
                    entry.getKey() + " - " + entry.getValue());
        }
    }
    
    /**
     * Shows memory usage information.
     *
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     */
    private void doMemInfo(final InputWindow origin, final boolean isSilent) {
        sendLine(origin, isSilent, FORMAT_OUTPUT, "Total Memory: "
                + Runtime.getRuntime().totalMemory());
        sendLine(origin, isSilent, FORMAT_OUTPUT, "Free Memory: "
                + Runtime.getRuntime().freeMemory());
        sendLine(origin, isSilent, FORMAT_OUTPUT, "Used Memory: "
                + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
    }
    
    /**
     * Outputs 100 lines containing various colours.
     *
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     */
    private void doColourSpam(final InputWindow origin, final boolean isSilent) {
        for (int i = 0; i < 100; i++) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, ((char) 3) + "5Colour! "
                    + ((char) 3) + "6Colour! " + ((char) 3) + "7Colour! "
                    + ((char) 3) + "6Colour! " + ((char) 3) + "7Colour! "
                    + ((char) 3) + "6Colour! " + ((char) 3) + "7Colour! "
                    + ((char) 3) + "6Colour! " + ((char) 3) + "7Colour! ");
        }
    }
    
    /**
     * Manually runs the garbage collector.
     *
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     */
    private void doGarbage(final InputWindow origin, final boolean isSilent) {
        System.gc();
        sendLine(origin, isSilent, FORMAT_OUTPUT, "Invoked garbage collector.");
    }
    
    /**
     * Shows information about the config manager.
     *
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     */
    private void doConfigInfo(final InputWindow origin, final boolean isSilent) {
        for (Identity source : origin.getConfigManager().getSources()) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, source.getTarget() + " - "
                    + source + "(" + source.getTarget().getOrder() + ")");
        }
    }
    
    /**
     * Shows information about the global config manager.
     *
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     */
    private void doGlobalConfigInfo(final InputWindow origin, final boolean isSilent) {
        for (Identity source : IdentityManager.getGlobalConfig().getSources()) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, source.getTarget() + " - "
                    + source + "(" + source.getTarget().getOrder() + ")");
        }
    }
    
    /**
     * Forces the update checker to check for updates.
     */
    private void doForceUpdate() {
        new Thread(new UpdateChecker(), "Forced update checker").start();
    }

    /**
     * Shows information about active threads.
     *
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     */
    private void doThreads(final InputWindow origin, final boolean isSilent) {
        for (Entry<Thread, StackTraceElement[]> thread: Thread.getAllStackTraces().entrySet()) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_BOLD
                    + thread.getKey().getName());

            for (StackTraceElement element : thread.getValue()) {
                sendLine(origin, isSilent, FORMAT_OUTPUT, Styliser.CODE_FIXED
                        + "    " + element.toString());
            }
        }
    }

    /**
     * Shows information about the current server's state.
     *
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     */
    private void doServerState(final InputWindow origin, final boolean isSilent) {
        if (origin.getContainer().getServer() == null) {
            sendLine(origin, isSilent, FORMAT_ERROR, "This window isn't connected to a server");
        } else {
            final Server server = origin.getContainer().getServer();
            sendLine(origin, isSilent, FORMAT_OUTPUT, server.getStatus().getTransitionHistory());
        }
    }
    
    /**
     * Shows information about the current server.
     * 
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     */
    private void doServerInfo(final InputWindow origin, final boolean isSilent) {
        if (origin.getContainer().getServer() == null) {
            sendLine(origin, isSilent, FORMAT_ERROR, "This window isn't connected to a server");
        } else {
            final Server server = origin.getContainer().getServer();
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Server name: " + server.getName());
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Actual name: "
                    + server.getParser().getServerName());
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Network: " + server.getNetwork());
            sendLine(origin, isSilent, FORMAT_OUTPUT, "IRCd: "
                    + server.getParser().getIRCD(false) + " - "
                    + server.getParser().getIRCD(true));
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Modes: "
                    + server.getParser().getBoolChanModes() + " "
                    + server.getParser().getListChanModes() + " "
                    + server.getParser().getSetOnlyChanModes() + " "
                    + server.getParser().getSetUnsetChanModes());
        }
    }
    
    /**
     * Benchmarks the textpane.
     * 
     * @param origin The window this command was executed in
     */
    private void doBenchmark(final InputWindow origin) {
        long[] results = new long[10];
        
        for (int i = 0; i < results.length; i++) {
            final long start = System.nanoTime();
            
            for (int j = 0; j < 5000; j++) {
                origin.addLine(FORMAT_OUTPUT, "This is a benchmark. Lorem ipsum doler...");
            }
            
            final long end = System.nanoTime();
            
            results[i] = end - start;
        }
        
        for (int i = 0; i < results.length; i++) {
            origin.addLine(FORMAT_OUTPUT, "Iteration " + i + ": " + results[i]
                    + " nanoseconds.");
        }
    }
    
    /**
     * Shows information about all the current services available to plugins.
     * 
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     * @param args The arguments that were passed to the command
     */
    private void doServices(final InputWindow origin, final boolean isSilent,
            final String[] args) {
        sendLine(origin, isSilent, FORMAT_OUTPUT, "Available Services:");
        for (Service service : PluginManager.getPluginManager().getAllServices()) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, "    " + service.toString());
            if (args.length > 1 && args[1].equals("full")) {
                for (ServiceProvider provider : service.getProviders()) {
                    sendLine(origin, isSilent, FORMAT_OUTPUT, "            "
                            + provider.getProviderName() + " [Active: "
                            + provider.isActive() + "]");
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "debug";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        res.excludeAll();
        
        if (arg == 0) {
            res.add("error");
            res.add("showraw");
            res.add("colourspam");
            res.add("configstats");
            res.add("meminfo");
            res.add("rungc");
            res.add("configinfo");
            res.add("globalconfiginfo");
            res.add("forceupdate");
            res.add("serverinfo");
            res.add("serverstate");
            res.add("threads");
            res.add("benchmark");
            res.add("firstrun");
            res.add("migration");
            res.add("notify");
            res.add("services");
        } else if (arg == 1 && "error".equals(previousArgs.get(0))) {
            res.add("user");
            res.add("app");
        } else if (arg == 1 && "services".equals(previousArgs.get(0))) {
            res.add("full");
        } else if (arg == 2 && "error".equals(previousArgs.get(0))) {
            res.add("low");
            res.add("medium");
            res.add("high");
            res.add("fatal");
            res.add("unknown");
        }
        
        return res;
    }
    
    /** Reverse value comparator for a map entry. */
    private static class ValueComparator implements
            Comparator<Entry<String, Integer>>, Serializable {
        
        /**
         * A version number for this class. It should be changed whenever the
         * class structure is changed (or anything else that would prevent
         * serialized objects being unserialized with the new class).
         */
        private static final long serialVersionUID = 1;
        
        /** Instantiates a new ValueComparator. */
        public ValueComparator() {
            super();
        }
        
        /** {@inheritDoc} */
        @Override
        public int compare(final Entry<String, Integer> o1,
                final Entry<String, Integer> o2) {
            int returnValue = o1.getValue().compareTo(o2.getValue()) * -1;
            
            if (returnValue == 0) {
                returnValue = o1.getKey().compareToIgnoreCase(o2.getKey());
            }
            
            return returnValue;
        }
    }
    
}
