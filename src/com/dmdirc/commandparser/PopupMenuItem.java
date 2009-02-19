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

/**
 * Represents an abstract, UI-independent popup menu item.
 * 
 * @author chris
 */
public class PopupMenuItem {
   
    /** Whether this item is a divider. */
    private boolean divider = false;
    /** The submenu for this item, if any. */
    private PopupMenu submenu = null;
    /** The name of this item, if any. */
    private String name;
    /** The command for this item, if any. */
    private String command;
    
    /**
     * Creates a new PopupMenuItem that is used as a divider.
     */
    public PopupMenuItem() {
        divider = true;
    }
    
    /**
     * Creates a new PopupMenuItem that is used as a submenu.
     * 
     * @param name The name of the menu item
     * @param submenu The submenu of this item
     */
    public PopupMenuItem(final String name, final PopupMenu submenu) {
        this.name = name;
        this.submenu = submenu;
    }
    
    /**
     * Creates a new PopupMenuItem that executes a command.
     * 
     * @param name The name of the menu item
     * @param command The command to be executed
     */
    public PopupMenuItem(final String name, final String command) {
        this.name = name;
        this.command = command;
    }
    
    /**
     * Determines if this menu item is a divider or not.
     * 
     * @return True if this item is a divider, false otherwise.
     */
    public boolean isDivider() {
        return divider;
    }

    /**
     * Determines if this menu item contains a submenu or not.
     * 
     * @return True if this item contains a submenu, false otherwise.
     */    
    public boolean isSubMenu() {
        return submenu != null;
    }
    
    /**
     * Retrieves the submenu associated with this item.
     * 
     * @return This menu item's submenu.
     */
    public PopupMenu getSubMenu() {
        return submenu;
    }
    
    /**
     * Retrieves the name of this menu item.
     * 
     * @return This menu item's name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Retrieves the command for this menu item, with the specified argumenwits
     * substituted in.
     * 
     * @param arguments The arguments needed for this command
     * @return The command to be passed to a command parser
     */
    public String getCommand(final Object... arguments) {
        return CommandManager.getCommandChar() + String.format(command, arguments);
    }

}
