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

package com.dmdirc.addons.dcop;

import com.dmdirc.plugins.Plugin;
import com.dmdirc.commandparser.CommandManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows the user to execute dcop commands (and read the results).
 * 
 * @author chris
 */
public final class DcopPlugin extends Plugin {
    /** The DcopCommand we created */
    private DcopCommand command = null;
    
    /** Creates a new instance of DcopPlugin. */
    public DcopPlugin() {
        super();
    }
    
    /**
     * Retrieves the result from executing the specified command.
     *
     * @param command The command to be executed
     * @return The output of the specified command
     */
    public static List<String> getDcopResult(final String command) {
        final ArrayList<String> result = new ArrayList<String>();

        InputStreamReader reader;
        BufferedReader input;
        Process process;
        
        try {
            process = Runtime.getRuntime().exec(command);
            
            reader = new InputStreamReader(process.getInputStream());
            input = new BufferedReader(reader);
            
            String line = "";
            
            while ((line = input.readLine()) != null) {
                result.add(line);
            }
            
            reader.close();
            input.close();
            process.destroy();
        } catch (IOException ex) {
            // Do nothing
        }
        
        return result;
    }
    
    /** {@inheritDoc}. */
    public void onLoad() {
        command = new DcopCommand();
    }
    
    /** {@inheritDoc}. */
    public void onUnload() {
        CommandManager.unregisterCommand(command);
    }

}
