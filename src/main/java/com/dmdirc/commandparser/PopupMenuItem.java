/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.commandparser;

import com.dmdirc.interfaces.CommandController;

/**
 * Represents an abstract, UI-independent popup menu item.
 */
public class PopupMenuItem {

    /** The command manager to use to retrieve command information. */
    private final CommandController commandManager;
    /** Whether this item is a divider. */
    private boolean divider;
    /** The submenu for this item, if any. */
    private PopupMenu submenu;
    /** The name of this item, if any. */
    private String name;
    /** The command for this item, if any. */
    private String command;
    /** The arity of the command. */
    private int arity;

    /**
     * Creates a new PopupMenuItem that is used as a divider.
     *
     * @param manager The command manager to use to retrieve command information
     */
    public PopupMenuItem(final CommandController manager) {
        divider = true;
        this.commandManager = manager;
    }

    /**
     * Creates a new PopupMenuItem that is used as a submenu.
     *
     * @param manager The command manager to use to retrieve command information
     * @param name    The name of the menu item
     * @param submenu The submenu of this item
     */
    public PopupMenuItem(final CommandController manager, final String name, final PopupMenu submenu) {
        this.name = name;
        this.submenu = submenu;
        this.commandManager = manager;
    }

    /**
     * Creates a new PopupMenuItem that executes a command.
     *
     * @param manager The command manager to use to retrieve command information
     * @param name    The name of the menu item
     * @param arity   The arity of the command this item will execute
     * @param command The command to be executed
     */
    public PopupMenuItem(final CommandController manager, final String name, final int arity,
            final String command) {
        this.name = name;
        this.arity = arity;
        this.command = command;
        this.commandManager = manager;
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
     * Retrieves the command for this menu item, with the specified arguments substituted in. Note
     * that the result may actually consist of multiple commands on separate lines.
     *
     * @param arguments A two dimensional array containing one array of arguments for each subject
     *                  of the command.
     *
     * @return The command to be passed to a command parser
     */
    public String getCommand(final Object[][] arguments) {
        final StringBuilder builder = new StringBuilder();

        final String actualCommand;
        final int expectedArgs;
        if (command.matches("^[0-9]+:.+")) {
            final int index = command.indexOf(':');
            expectedArgs = Integer.parseInt(command.substring(0, index));
            actualCommand = command.substring(index + 1);
        } else {
            expectedArgs = arity;
            actualCommand = command;
        }

        final Object[] args = new Object[expectedArgs];
        int offset = 0;

        for (Object[] singleArg : arguments) {
            System.arraycopy(singleArg, 0, args, offset, arity);

            offset += arity;

            if (offset >= expectedArgs) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }

                builder.append(commandManager.getCommandChar());
                builder.append(String.format(actualCommand, args));
                offset = 0;
            }
        }

        if (offset > 0) {
            for (int i = offset; i < expectedArgs; i++) {
                args[i] = "";
            }

            if (builder.length() > 0) {
                builder.append('\n');
            }

            builder.append(commandManager.getCommandChar());
            builder.append(String.format(actualCommand, args));
        }

        return builder.toString();
    }

}
