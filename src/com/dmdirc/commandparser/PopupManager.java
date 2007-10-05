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
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import java.util.HashMap;
import java.util.List;
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
     * If the command is equal to "-", the entry should be treated as a divider.
     *
     * @param menuType The type of menu that is being built
     * @param configManager The config manager for the current context
     * @param arguments Any arguments appropriate for the menuType
     * 
     * @return A map of "friendly names" to commands (without command characters)
     */
    public static Map<String, String> getMenuItems(final PopupType menuType,
            final ConfigManager configManager, final Object ... arguments) {
        Logger.doAssertion(menuType != null, configManager != null);
        
        final Map<String, String> res = new HashMap<String, String>();
        
        int dividerCount = 0;
        
        for (String domain : menuType.getDomains()) {
            final List<String> commands = configManager.getOptionList("popups", domain);
            
            for (String command : commands) {
                if ("-".equals(command)) {
                    res.put("divider" + (++dividerCount), "-");
                } else if (command.indexOf(':') > 0) {
                    final String name = command.substring(0, command.indexOf(':'));
                    final String value = command.substring(command.indexOf(':'));
                    
                    res.put(name, String.format(value, arguments));
                } else if (!command.isEmpty()) {
                    Logger.userError(ErrorLevel.LOW, "Invalid command in "
                            + "popup menu configuration. Menu: " + domain
                            + ", Command: " + command);
                }
            }
        }
        
        return res;
    }
    
}