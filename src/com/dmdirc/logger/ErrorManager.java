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
import java.io.Serializable;
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

import javax.swing.event.EventListenerList;

/**
 * Error manager.
 */
public final class ErrorManager implements Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Previously instantiated instance of ErrorManager. */
    private static ErrorManager me;
    
    /** Error list. */
    private final List<ProgramError> errors;
    
    /** Listener list. */
    private final EventListenerList errorListeners;
    
    /** Creates a new instance of ErrorListDialog. */
    private ErrorManager() {
        errors = new LinkedList<ProgramError>();
        errorListeners = new EventListenerList();
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
        if (error.getLevel() == ErrorLevel.FATAL) {
            fireFatalError(error);
        } else {
            fireErrorAdded(error);
        }
    }
    
    
    /**
     * Called when an error needs to be deleted from the list.
     *
     * @param error ProgramError that changed
     */
    public void deleteError(final ProgramError error) {
        errors.remove(error);
        fireErrorDeleted(error);
    }
    
    /**
     * Returns a list of errors.
     *
     * @return Error list
     */
    public List<ProgramError> getErrorList() {
        return new LinkedList<ProgramError>(errors);
    }
    
    /**
     * Returns the number of errors.
     *
     * @return Number of ProgramErrors
     */
    public int getErrorCount() {
        return errors.size();
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
     * Returns the ID of an error.
     *
     * @param id Error ID to get
     *
     * @return ProgramError with specified ID
     */
    public ProgramError getError(final int id) {
        return errors.get(id);
    }
    
    /**
     * Sends an error to the developers.
     *
     * @param error error to be sent
     */
    @SuppressWarnings("PMD.SystemPrintln")
    public static void sendError(final ProgramError error) {
        new Timer("ErrorManager Timer").schedule(new TimerTask() {
            public void run() {
                sendErrorInternal(error);
            }
        }, 0);
    }
    
    /**
     * Sends an error to the developers.
     *
     * @param error ProgramError to be sent
     */
    @SuppressWarnings("PMD.SystemPrintln")
    private static void sendErrorInternal(final ProgramError error) {
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
    
    /**
     * Adds an ErrorListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addErrorListener(final ErrorListener listener) {
        synchronized (errorListeners) {
            if (listener == null) {
                return;
            }
            errorListeners.add(ErrorListener.class, listener);
        }
    }
    
    /**
     * Removes an ErrorListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeErrorListener(final ErrorListener listener) {
        errorListeners.remove(ErrorListener.class, listener);
    }
    
    /**
     * Fired when the program encounters an error.
     *
     * @param error Error that occurred
     */
    protected void fireErrorAdded(final ProgramError error) {
        final Object[] listeners = errorListeners.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ErrorListener.class) {
                ((ErrorListener) listeners[i + 1]).errorAdded(error);
            }
        }
    }
    
    /**
     * Fired when the program encounters a fatal error.
     *
     * @param error Error that occurred
     */
    protected void fireFatalError(final ProgramError error) {
        final Object[] listeners = errorListeners.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ErrorListener.class) {
                ((ErrorListener) listeners[i + 1]).fatalError(error);
            }
        }
    }
    
    /**
     * Fired when an error is deleted.
     *
     * @param error Error that has been deleted
     */
    protected void fireErrorDeleted(final ProgramError error) {
        final Object[] listeners = errorListeners.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ErrorListener.class) {
                ((ErrorListener) listeners[i + 1]).errorDeleted(error);
            }
        }
    }
    
    /**
     * Fired when an error's status is changed.
     *
     * @param error Error that has been altered
     */
    protected void fireErrorStatusChanged(final ProgramError error) {
        final Object[] listeners = errorListeners.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ErrorListener.class) {
                ((ErrorListener) listeners[i + 1]).errorStatusChanged(error);
            }
        }
    }
    
}
