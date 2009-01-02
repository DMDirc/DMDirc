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

package com.dmdirc.commandparser;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.ConfigManager;


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
     * Returns the popup menu that should be used for the specified type.
     * Configuration data is read from the specified config manager.
     * 
     * @param menuType The type of the menu that is needed
     * @param configManager The config manager to be used for the menu
     * @return The PopupMenu that should be displayed
     */
    public static PopupMenu getMenu(final PopupType menuType, final ConfigManager configManager) {
        final PopupMenu menu = getMenu(menuType.toString(), configManager);
        
        ActionManager.processEvent(CoreActionType.CLIENT_POPUP_GENERATED, 
                null, menuType, menu, configManager);
        
        return menu;
    }
    
    /**
     * Retrieves the menu with the specified name.
     * 
     * @param menuName The name of the menu to read
     * @param configManager The config manager to be used for the menu
     * @return The PopupMenu with the specified name
     */
    private static PopupMenu getMenu(final String menuName, final ConfigManager configManager) {
        final PopupMenu res = new PopupMenu();
        
        for (String item : configManager.getOptionList("popups", menuName)) {
            if (item.length() > 0 && item.charAt(0) == '<') {
                res.addAll(getMenu(item.substring(1), configManager).getItems());
            } else {
                res.add(getItem(item, configManager));
            }
        }
        
        return res;
    }
    
    /**
     * Creates a PopupMenuItem for the specified item.
     * 
     * @param item The item to be turned into a PopupMenuItem
     * @param configManager The config manager to beused for the menu
     * @return The corresponding PopupMenuItem
     */
    private static PopupMenuItem getItem(final String item, final ConfigManager configManager) {
        PopupMenuItem res;
        
        if ("-".equals(item)) {
            res = new PopupMenuItem();
        } else {
            final int colon = item.indexOf(':');
            
            if (colon == -1) {
                throw new IllegalArgumentException("Invalid popup menu item: "
                        + item);
            }
            
            final String name = item.substring(0, colon);
            final String command = item.substring(colon + 1);
            
            if (command.length() > 0 && command.charAt(0) == '<') {
                res = new PopupMenuItem(name, getMenu(command.substring(1), configManager));
            } else {
                res = new PopupMenuItem(name, command);
            }
        }
        
        return res;
    }
    
}