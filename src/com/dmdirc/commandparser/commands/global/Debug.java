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
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.updater.UpdateChecker;

import java.util.List;
import java.util.Map;

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
        
        if (command.equals("error")) {
            ErrorLevel el = ErrorLevel.HIGH;
            if (args.length > 2) {
                String level = args[2];
                
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
        } else if (command.equals("fakeupdate")) {
            new UpdateChecker().doUpdateAvailable("outofdate dummy 1337 0 http://www.example.com/");
        } else if (command.equals("showraw") && origin != null && origin.getContainer() != null
                && origin.getContainer().getServer() != null) {
            origin.getContainer().getServer().addRaw();
        } else if (command.equals("colourspam") && origin != null) {
            for (int i = 0; i < 100; i++) {
                sendLine(origin, isSilent, "commandOutput", ((char) 3) + "5Colour! "
                        + ((char) 3) + "6Colour! " + ((char) 3) + "7Colour! "
                        + ((char) 3) + "6Colour! " + ((char) 3) + "7Colour! "
                        + ((char) 3) + "6Colour! " + ((char) 3) + "7Colour! "
                        + ((char) 3) + "6Colour! " + ((char) 3) + "7Colour! ");
            }
        } else if (command.equals("configstats")) {
            for (Map.Entry<String, Integer> entry : ConfigManager.getStats().entrySet()) {
                sendLine(origin, isSilent, "commandOutput",
                        entry.getKey() + " - " + entry.getValue());
            }
        } else if (command.equals("meminfo")) {
            sendLine(origin, isSilent, "commandOutput", "Total Memory: " + Runtime.getRuntime().totalMemory());
            sendLine(origin, isSilent, "commandOutput", "Free Memory: " + Runtime.getRuntime().freeMemory());
            sendLine(origin, isSilent, "commandOutput", "Used Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        } else if (command.equals("rungc")) {
            System.gc();
            sendLine(origin, isSilent, "commandOutput", "Invoked garbage collector.");
        } else if (command.equals("configinfo") && origin != null) {
            for (Identity source : origin.getConfigManager().getSources()) {
                sendLine(origin, isSilent, "commandOutput", source.getTarget() + " - " + source);
            }
        } else if (command.equals("forceupdate")) {
            new Thread(new UpdateChecker(), "Forced update checker").start();
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
        return true;
    }
    
    public int getArity() {
        return 0;
    }
    
    public String getHelp() {
        return null;
    }
    
    public AdditionalTabTargets getSuggestions(int arg, List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        res.setIncludeNormal(false);
        
        if (arg == 0) {
            res.add("error");
            res.add("fakeupdate");
            res.add("showraw");
            res.add("colourspam");
            res.add("configstats");
            res.add("meminfo");
            res.add("rungc");
            res.add("configinfo");
            res.add("forceupdate");
        } else if (arg == 1) {
            res.add("user");
            res.add("app");
        } else if (arg == 2) {
            res.add("low");
            res.add("medium");
            res.add("high");
            res.add("fatal");
            res.add("unknown");
        }
        
        return res;
    }
    
}
