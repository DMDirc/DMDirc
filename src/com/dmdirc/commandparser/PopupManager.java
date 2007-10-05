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

package com.dmdirc.commandparser;

import com.dmdirc.config.ConfigManager;
import java.util.HashMap;
import java.util.Map;

/**
 * The popup manager manages which commands should be present in popup menus.
 *
 * @author Chris
 */
public class PopupManager {
    
    /**
     * Creates a new instance of PopupManager.
     */
    private PopupManager() {
        // Shouldn't be instansiated.
    }
    
    /**
     * Retrieves a map of the menu items that should be in a certain menu type.
     *
     * @param menuType The type of menu that is being built
     * @param configManager The config manager for the current context
     * @return A map of "friendly names" to commands (without command characters)
     */
    public static Map<String, String> getMenuItems(final PopupType menuType,
            final ConfigManager configManager) {
        final Map<String, String> res = new HashMap<String, String>();
        
        for (String domain : menuType.getDomains()) {
            for (String option : configManager.getOptions(domain)) {
                final String command = configManager.getOption(domain, option);
                if (!command.isEmpty()) {
                    res.put(option, command);
                }
            }
        }
        
        return res;
    }
    
}