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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.GlobalCommand;
import com.dmdirc.commandparser.IntelligentCommand;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.updater.UpdateChecker;
import java.util.List;

/**
 * Provides various handy ways to test or debug the client.
 *
 * @author Chris
 */
public class Debug extends GlobalCommand implements IntelligentCommand {
    
    public Debug() {
        CommandManager.registerCommand(this);
    }
    
    public void execute(InputWindow origin, boolean isSilent, String... args) {
        final String command = args[0].toLowerCase();
        
        if (command.equals("fakeusererror")) {
            Logger.userError(ErrorLevel.HIGH, "Debug (user) error message");
        } else if (command.equals("fakeapperror")) {
            Logger.appError(ErrorLevel.HIGH, "Debug (app) error message", new Exception());
        } else if (command.equals("fakeupdate")) {
            new UpdateChecker().doUpdateAvailable("outofdate dummy 1337 0 http://www.example.com/");
        } else if (command.equals("showraw") && origin != null) {
            
        } else {
            sendLine(origin, isSilent, "commandError", "Unknown debug action.");
        }
    }
    
    public String getName() {
        return "debug";
    }
    
    public boolean showInHelp() {
        return false;
    }
    
    public boolean isPolyadic() {
        return false;
    }
    
    public int getArity() {
        return 1;
    }
    
    public String getHelp() {
        return null;
    }
    
    public AdditionalTabTargets getSuggestions(int arg, List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        res.setIncludeNormal(false);
        res.add("fakeusererror");
        res.add("fakeapperror");
        res.add("fakeupdate");
        res.add("showraw");
        
        return res;
    }
    
}
