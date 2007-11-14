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

package com.dmdirc.addons.nowplaying;

import com.dmdirc.MessageTarget;
import com.dmdirc.Server;
import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.commandparser.commands.ChatCommand;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.IntelligentCommand;
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
                    sendLine(origin, isSilent, FORMAT_ERROR, "Source not found.");
                } else {
                    if (source.isRunning()) {
                        target.sendAction("is playing " + getInformation(source));
                    } else {
                        sendLine(origin, isSilent, FORMAT_ERROR, "Source is not running.");
                    }
                }
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "You must specify a source when using --source.");
            }
        } else {
            if (parent.hasRunningSource()) {
                target.sendAction("is playing " + getInformation(parent.getBestSource()));
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "No running media sources available.");
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
            sendLine(origin, isSilent, FORMAT_ERROR, "No media sources available.");
        } else {
            String status;
            
            for (MediaSource source : sources) {
                if (source.isRunning()) {
                    if (source.isPlaying()) {
                        status = "playing: " + getInformation(source);
                    } else {
                        status = "paused: " + getInformation(source);
                    }
                } else {
                    status = "not running";
                }
                sendLine(origin, isSilent, FORMAT_OUTPUT, source.getAppName() + ": " + status);
            }
        }
    }
    
    /**
     * Returns a formatted information string from the requested soruce.
     *
     * @param source MediaSource to query
     *
     * @return Formatted information string
     */ 
    private String getInformation(final MediaSource source) {
        //TODO grab format for specified source
        String artist = source.getArtist();
        String title = source.getTitle();
        
        if (artist == null) {
            artist = "n/a";
        }
        if (title == null) {
            title = "n/a";
        }
        
        return artist + " - " + title;
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
    public String getHelp() {
        return "nowplaying [--sources|--source <source>] - tells the channel the song you're currently playing";
    }
    
    /** {@inheritDoc} */
    public AdditionalTabTargets getSuggestions(final int arg, 
            final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        res.setIncludeNormal(false);
        
        if (arg == 0) {
            res.add("--sources");
            res.add("--source");
        } else if (arg == 1 && previousArgs.get(0).equalsIgnoreCase("--source")) {
            for (MediaSource source : parent.getSources()) {
                if (source.isRunning()) {
                    res.add(source.getAppName());
                }
            }
        }
        
        return res;
    }
    
}
