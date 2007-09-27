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

import com.dmdirc.Main;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final long serialVersionUID = 3;
    
    /** Previously instantiated instance of ErrorManager. */
    private static ErrorManager me = new ErrorManager();
    
    /** Error list. */
    private final Map<Integer, ProgramError> errors;
    
    /** Listener list. */
    private final EventListenerList errorListeners;
    
    /** Next error ID. */
    private int nextErrorID;
    
    /** Creates a new instance of ErrorListDialog. */
    private ErrorManager() {
        errors = new HashMap<Integer, ProgramError>();
        errorListeners = new EventListenerList();
        nextErrorID = 0;
    }
    
    /**
     * Returns the instance of ErrorManager.
     *
     * @return Instance of ErrorManager
     */
    public static ErrorManager getErrorManager() {
        synchronized (me) {
            return me;
        }
    }
    
    /**
     * Called when an error occurs in the program.
     *
     * @param error ProgramError that occurred
     */
    public void addError(final ProgramError error) {
        errors.put(error.getID(), error);
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
        errors.remove(error.getID());
        fireErrorDeleted(error);
    }
    
    /**
     * Returns a list of errors.
     *
     * @return Error list
     */
    public Map<Integer, ProgramError> getErrorList() {
        return new HashMap<Integer, ProgramError>(errors);
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
     * Returns the next error ID.
     *
     * @return Next error ID
     */
    public int getNextErrorID() {
        return nextErrorID++;
    }
    
    /**
     * Returns specified program error
     *
     * @param id ID of the error to fetch
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
        final List<String> responseList = new ArrayList<String>();
        
        error.setReportStatus(ErrorReportStatus.SENDING);
        
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
                        error.getTrace()), "UTF-8") + "&version="
                        + URLEncoder.encode(Main.VERSION + "(" + Main.RELEASE_DATE + ")", "UTF-8");
                printout.writeBytes(content);
                printout.flush();
                printout.close();
                printin = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                
                String line = null;
                do {
                    if (line != null) {
                        response = line;
                        responseList.add(line);
                    }
                    
                    line = printin.readLine();
                } while (line != null);
                printin.close();
            } catch (MalformedURLException ex) {
                //Unable to send error report
            } catch (UnsupportedEncodingException ex) {
                //Unable to send error report
            } catch (IOException ex) {
                //Unable to send error report
            }
            
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                //Ignore
            }
            
            tries++;
        }
        
        checkResponses(error, response, responseList);
    }
    
    /** Checks the responses and sets status accordingly. */
    private static void checkResponses(final ProgramError error, 
            final String response, final List<String> responseList) {
        if ("Error report submitted. Thank you.".equalsIgnoreCase(response)) {
            error.setReportStatus(ErrorReportStatus.FINISHED);
        } else {
            error.setReportStatus(ErrorReportStatus.ERROR);
        }
        
        if (responseList.size() == 1) {
            error.setFixedStatus(ErrorFixedStatus.NEW);
            return;
        }
        
        final String responseToCheck = responseList.get(0);
        if (responseToCheck.matches(".*fixed.*")) {
            error.setFixedStatus(ErrorFixedStatus.FIXED);
        } else if (responseToCheck.matches(".*invalid.*")) {
            error.setFixedStatus(ErrorFixedStatus.INVALID);
        } else if (responseToCheck.matches(".*previously.*")) {
            error.setFixedStatus(ErrorFixedStatus.KNOWN);
        } else {
            error.setFixedStatus(ErrorFixedStatus.NEW);
        }
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
