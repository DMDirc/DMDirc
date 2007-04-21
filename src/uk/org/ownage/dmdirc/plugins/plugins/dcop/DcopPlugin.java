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

package uk.org.ownage.dmdirc.plugins.plugins.dcop;

import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.commandparser.CommandManager;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.commandparser.ServerCommand;
import uk.org.ownage.dmdirc.plugins.Plugin;

/**
 * Allows the user to execute dcop commands (and read the results).
 * @author chris
 */
public class DcopPlugin implements Plugin {
    
    /** Creates a new instance of DcopPlugin */
    public DcopPlugin() {
        
    }
    
    public void onLoad() {
        CommandManager.registerCommand(new ServerCommand() {
            public void execute(CommandWindow origin, Server server, String ... args) {
                origin.addLine("dcop test");
            }
            public int getArity() {
                return 0;
            }
            public String getHelp() {
                return null;
            }
            public String getName() {
                return "dcoptest";
            }
            public boolean isPolyadic() {
                return false;
            }
            public boolean showInHelp() {
                return false;
            }
        });
    }
    
    public void onUnload() {
    }
    
    public void onActivate() {
    }
    
    public void onDeactivate() {
    }
    
    public int getVersion() {
        return 0;
    }
    
    public String getAuthor() {
        return "Chris 'MD87' Smith - chris@dmdirc.com";
    }
    
    public String getDescription() {
        return "Provides commands to interface with DCOP applications";
    }
    
}
