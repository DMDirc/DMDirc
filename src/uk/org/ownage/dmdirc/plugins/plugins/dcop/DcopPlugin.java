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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import uk.org.ownage.dmdirc.plugins.Plugin;

/**
 * Allows the user to execute dcop commands (and read the results).
 * @author chris
 */
public class DcopPlugin implements Plugin {
    
    /** Is this plugin active? */
    private boolean isActive = false;
    
    /** Creates a new instance of DcopPlugin. */
    public DcopPlugin() {
        
    }
    
    /**
     * Retrieves the result from executing the specified command.
     * @param command The command to be executed
     * @return The output of the specified command
     */
    public static List<String> getDcopResult(final String command) throws IOException {
        final ArrayList<String> result = new ArrayList<String>();
        
        final Process process = Runtime.getRuntime().exec(command);
        final BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        String line = "";
        
        while ((line = input.readLine()) != null) {
            result.add(line);
        }
        
        return result;
    }
    
    /** {@inheritDoc}. */
    public boolean onLoad() {
        new DcopCommand();
        new NowPlayingCommand();
        
        return true;
    }
    
    /** {@inheritDoc}. */
    public void onUnload() {
    }
    
    /** {@inheritDoc}. */
    public void onActivate() {
        isActive = true;
    }
    
    /** {@inheritDoc}. */
    public boolean isActive() {
        return isActive;
    }
    
    /** {@inheritDoc}. */
    public void onDeactivate() {
        isActive = false;
    }
    
    /** {@inheritDoc}. */
    public int getVersion() {
        return 0;
    }
    
    /** {@inheritDoc}. */
    public String getAuthor() {
        return "Chris 'MD87' Smith - chris@dmdirc.com";
    }
    
    /** {@inheritDoc}. */
    public String getDescription() {
        return "Provides commands to interface with DCOP applications";
    }
    
    /** {@inheritDoc}. */
    public boolean isConfigurable() {
        return false;
    }
    
    /** {@inheritDoc}. */
    public void showConfig() {
    }
    
}
