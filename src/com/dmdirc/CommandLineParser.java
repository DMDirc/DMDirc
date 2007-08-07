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

package com.dmdirc;

/**
 * Parses command line arguments for the client.
 *
 * @author Chris
 */
public class CommandLineParser {
    
    /**
     * The arguments that the client supports, in groups of four, in the
     * following order: short option, long option, description, whether or not
     * the option takes an argument.
     */
    private static final Object[][] ARGUMENTS = new Object[][]{
        //{'c', "connect", "Connect to the specified server", Boolean.TRUE},
        {'h', "help", "Show command line options and exit", Boolean.FALSE},
        {'v', "version", "Display client version and exit", Boolean.FALSE},
    };
    
    /**
     * Creates a new instance of CommandLineParser.
     *
     * @param arguments The arguments to be parsed
     */
    public CommandLineParser(final String ... arguments) {
        boolean inArg = false;
        char previousArg;
        
        for (String arg : arguments) {
            if (!inArg) {
                if (arg.startsWith("--")) {
                    previousArg = processLongArg(arg.substring(2));
                    processArgument(previousArg, null);
                } else if (arg.startsWith("-")) {
                    previousArg = processShortArg(arg.substring(1));
                    processArgument(previousArg, null);
                } else {
                    doUnknownArg("Unknown argument: " + arg);
                }
            } else {
                
            }
        }
    }

    /**
     * Processes the specified string as a single long argument.
     * 
     * @param arg The string entered
     * @return The short form of the corresponding argument
     */    
    private char processLongArg(final String arg) {
        for (int i = 0; i < ARGUMENTS.length; i++) {
            if (arg.equalsIgnoreCase((String) ARGUMENTS[i][1])) {
                return (Character) ARGUMENTS[i][0];
            }
        }
        
        doUnknownArg("Unknown argument: " + arg);
        exit();
        
        return '.';        
    }
    
    /**
     * Processes the specified string as a single short argument.
     * 
     * @param arg The string entered
     * @return The short form of the corresponding argument
     */
    private char processShortArg(final String arg) {
        for (int i = 0; i < ARGUMENTS.length; i++) {
            if (arg.charAt(0) == ARGUMENTS[i][0]) {
                return arg.charAt(0);
            }
        }
        
        doUnknownArg("Unknown argument: " + arg);
        exit();
        
        return '.';
    }
    
    /**
     * Processes the sepcified command-line argument.
     * 
     * @param arg The short form of the argument used
     * @param param The (optional) string parameter for the option
     */
    private void processArgument(final char arg, final String param) {
        switch (arg) {
        case 'h':
            doHelp();
            break;
        case 'v':
            doVersion();
            break;
        default:
            doUnknownArg("Unknown argument: " + arg);
            break;
        }
    }
    
    /**
     * Informs the user that they entered an unknown argument, prints the
     * client help, and exits.
     * 
     * @param message The message about the unknown argument to be displayed
     */
    private void doUnknownArg(final String message) {
        System.out.println(message);
        doHelp();
    }
    
    /**
     * Exits DMDirc.
     */
    private void exit() {
        System.exit(0);
    }
    
    /**
     * Prints out the client version and exits.
     */
    private void doVersion() {
        System.out.println("DMDirc - a cross-platform, open-source IRC client.");
        System.out.println();
        System.out.println("      Version: " + Main.VERSION);
        System.out.println(" Release date: " + Main.RELEASE_DATE);
        System.out.println("      Channel: " + Main.UPDATE_CHANNEL);
        exit();
    }
    
    /**
     * Prints out client help and exits.
     */
    private void doHelp() {
        exit();
    }
    
}
