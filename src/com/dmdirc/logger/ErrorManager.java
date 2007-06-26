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

package com.dmdirc.logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Error manager.
 */
public final class ErrorManager {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Previously instantiated instance of ErrorManager. */
    private static ErrorManager me;
    
    /** Error list. */
    private final List<ProgramError> errors;
    
    /** Creates a new instance of ErrorListDialog. */
    private ErrorManager() {
        errors = new LinkedList<ProgramError>();
    }
    
    /**
     * Returns the instance of ErrorManager.
     *
     * @return Instance of ErrorManager
     */
    public static synchronized ErrorManager getErrorManager() {
        if (me == null) {
            me = new ErrorManager();
        }
        return me;
    }
    
    /**
     * Called when an error occurs in the program.
     *
     * @param error ProgramError that occurred
     */
    public void addError(final ProgramError error) {
        errors.add(error);
    }
    
    
    /**
     * Called when an error needs to be deleted from the list.
     *
     * @param error ProgramError that changed
     */
    public void deleteError(final ProgramError error) {
        errors.remove(error);
    }
    
    /**
     * Returns a list of errors
     *
     * @return Error list
     */
    public List<ProgramError> getErrorList() {
        return errors;
    }

    /**
     * Returns the ID of an error.
     *
     * @param error Error to get ID for
     *
     * @return Error ID
     */
    public int getErrorID(final ProgramError error) {
        return errors.indexOf(error);
    }
    
        /**
     * Sends an error to the developers.
     *
     * @param ProgramError error to be sent
     */
    @SuppressWarnings("PMD.SystemPrintln")
    public static void sendError(final ProgramError error) {
        new Timer().schedule(new TimerTask() {
            public void run() {
                URL url;
                URLConnection urlConn;
                DataOutputStream printout;
                BufferedReader printin;
                String response = "";
                int tries = 0;
                
                error.setStatus(ErrorStatus.SENDING);
                
                System.err.println("Sending error report...");
                
                while (!"Error report submitted. Thank you.".equalsIgnoreCase(response)
                || tries >= 5) {
                    try {
                        url = new URL("http://www.dmdirc.com/error.php");
                        urlConn = url.openConnection();
                        urlConn.setDoInput(true);
                        urlConn.setDoOutput(true);
                        urlConn.setUseCaches(false);
                        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        printout = new DataOutputStream(urlConn.getOutputStream());
                        final String content =
                                "message=" + URLEncoder.encode(error.getMessage(), "UTF-8")
                                + "&trace=" + URLEncoder.encode(Arrays.toString(
                                error.getTrace()), "UTF-8");
                        printout.writeBytes(content);
                        printout.flush();
                        printout.close();
                        printin = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                        
                        String line = null;
                        do {
                            if (line != null) {
                                response = line;
                                System.err.println(line);
                            }
                            
                            line = printin.readLine();
                        } while (line != null);
                        printin.close();
                    } catch (MalformedURLException ex) {
                        System.err.println("Malformed URL, unable to send error report.");
                    } catch (UnsupportedEncodingException ex) {
                        System.err.println("Unsupported exception,  unable to send error report.");
                    } catch (IOException ex) {
                        System.err.println("IO Error, unable to send error report.");
                    }
                    
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        //Ignore
                    }
                    
                    tries++;
                }
                
                error.setStatus(ErrorStatus.FINISHED);
            }
        }, 0);
    }
    
}
