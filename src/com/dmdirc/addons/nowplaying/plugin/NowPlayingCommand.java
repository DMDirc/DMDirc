/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.nowplaying.plugin;

import com.dmdirc.MessageTarget;
import com.dmdirc.Server;
import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.commandparser.ChatCommand;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.IntelligentCommand;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.List;

/**
 * The now playing command retrieves the currently playing song from a
 * variety of media players.
 * @author chris
 */
public final class NowPlayingCommand extends ChatCommand implements IntelligentCommand {
    
    /** The plugin that's using this command. */
    final NowPlayingPlugin parent;
    
    /**
     * Creates a new instance of NowPlayingCommand.
     *
     * @param parent The plugin that's instansiating this command
     */
    public NowPlayingCommand(final NowPlayingPlugin parent) {
        super();
        
        this.parent = parent;
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    public void execute(final InputWindow origin, final Server server,
            final MessageTarget target, final boolean isSilent, final String ... args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--sources")) {
            doSourceList(origin, isSilent);
        } else if (args.length > 0 && args[0].equalsIgnoreCase("--source")) {
            if (args.length > 1) {
                final String sourceName = args[1];
                final MediaSource source = parent.getSource(sourceName);
                
                if (source == null) {
                    sendLine(origin, isSilent, "commandError", "Source not found.");
                } else {
                    if (source.isRunning()) {
                        target.sendAction("is playing " + source.getInformation());
                    } else {
                        sendLine(origin, isSilent, "commandError", "Source is not running.");
                    }
                }
            } else {
                sendLine(origin, isSilent, "commandError", "You must specify a source when using --source.");
            }
        } else {
            if (parent.hasRunningSource()) {
                target.sendAction("is playing " + parent.getBestSource().getInformation());
            } else {
                sendLine(origin, isSilent, "commandError", "No running media sources available.");
            }
        }
    }
    
    /**
     * Outputs a list of sources for the nowplaying command.
     *
     * @param origin The input window where the command was entered
     * @param isSilent Whether this command is being silenced
     */
    private void doSourceList(final InputWindow origin, final boolean isSilent) {
        final List<MediaSource> sources = parent.getSources();
        
        if (sources.isEmpty()) {
            sendLine(origin, isSilent, "commandError", "No media sources available.");
        } else {
            String status;
            
            for (MediaSource source : sources) {
                if (source.isRunning()) {
                    if (source.isPlaying()) {
                        status = "playing: " + source.getInformation();
                    } else {
                        status = "paused: " + source.getInformation();
                    }
                } else {
                    status = "not running";
                }
                sendLine(origin, isSilent, "commandOutput", source.getName() + ": " + status);
            }
        }
    }
    
    /** {@inheritDoc}. */
    public String getName() {
        return "nowplaying";
    }
    
    /** {@inheritDoc}. */
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public boolean isPolyadic() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public int getArity() {
        return 0;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "nowplaying [--sources|--source <source>] - tells the channel the song you're currently playing";
    }
    
    /** {@inheritDoc} */
    public AdditionalTabTargets getSuggestions(int arg, List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        res.setIncludeNormal(false);
        
        if (arg == 0) {
            res.add("--sources");
            res.add("--source");
        } else if (arg == 1 && previousArgs.get(0).equalsIgnoreCase("--source")) {
            for (MediaSource source : parent.getSources()) {
                if (source.isRunning()) {
                    res.add(source.getName());
                }
            }
        }
        
        return res;
    }
    
}
