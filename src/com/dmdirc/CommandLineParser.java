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
    private static final Object[] ARGUMENTS = new Object[]{
        //'c', "connect", "Connect to the specified server", Boolean.TRUE,
        "h", "help", "Show command line options and exit", Boolean.FALSE,
        "v", "version", "Display client version and exit", Boolean.FALSE,
    };
    
    /**
     * Creates a new instance of CommandLineParser.
     *
     * @param arguments The arguments to be parsed
     */
    public CommandLineParser(String ... arguments) {
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
    
    private char processLongArg(final String arg) {
        return 'h';
    }
    
    private char processShortArg(final String arg) {
        for (int i = 0; i < ARGUMENTS.length; i += 4) {
            if (arg.equals(ARGUMENTS[i])) {
                return arg.charAt(0);
            }
        }
        
        doUnknownArg("Unknown argument: " + arg);
        exit();
        
        return '.';
    }
    
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
    
    private void doUnknownArg(final String message) {
        System.out.println(message);
        processArgument('h', null);
        exit();
    }
    
    /**
     * Exits DMDirc.
     */
    private void exit() {
        System.exit(0);
    }
    
    private void doVersion() {
        System.out.println("DMDirc - a cross-platform, open-source IRC client.");
        System.out.println();
        System.out.println("      Version: " + Main.VERSION);
        System.out.println(" Release date: " + Main.RELEASE_DATE);
        System.out.println("      Channel: " + Main.UPDATE_CHANNEL);
        exit();
    }
    
    private void doHelp() {
        exit();
    }
    
}
