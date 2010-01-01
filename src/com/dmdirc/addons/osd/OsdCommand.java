/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.osd;

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.messages.Styliser;

import java.util.List;

/**
 * The osd command shows an on screen message.
 * @author chris
 */
public final class OsdCommand extends GlobalCommand implements IntelligentCommand {

    /** The plugin that owns this command. */
    private final OsdPlugin plugin;
    
    /**
     * Creates a new instance of OsdCommand.
     *
     * @param plugin The plugin that owns this command
     */
    public OsdCommand(final OsdPlugin plugin) {
        super();

        this.plugin = plugin;
        
        CommandManager.registerCommand(this);
    }
    

    /**
     * Used to show a notification using this plugin.
     *
     * @param title Title of dialog if applicable
     * @param message Message to show
     * @return True if the notification was shown.
     */
    public boolean showOSD(final String title, final String message) {
        new OsdWindow(Styliser.stipControlCodes(message), false, plugin);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent,
            final CommandArguments args) {
        if (args.getArguments().length > 0
                && "--close".equalsIgnoreCase(args.getArguments()[0])) {
            OsdWindow.closeAll();
        } else {
            showOSD("", args.getArgumentsAsString());
        }
    }
    
    /** {@inheritDoc}. */
    @Override
    public String getName() {
        return "osd";
    }
    
    /** {@inheritDoc}. */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc}. */
    @Override
    public String getHelp() {
        return "osd --close - closes all OSD windows\n" +
                "osd <message> - show the specified message in an OSD window";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        if (arg == 0) {
            res.add("--close");
        } else if (arg > 0 && previousArgs.get(0).equals("--close")) {
            res.excludeAll();
        }
        
        return res;
    } 
    
}
