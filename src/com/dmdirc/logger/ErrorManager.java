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

package com.dmdirc.logger;

import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.FatalErrorDialog;
import com.dmdirc.util.Downloader;
import com.dmdirc.util.ListenerList;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Error manager.
 */
public final class ErrorManager implements Serializable, Runnable {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 4;

    /** Time to wait between error submissions. */
    private static final int SLEEP_TIME = 5000;

    /** Previously instantiated instance of ErrorManager. */
    private static final ErrorManager me = new ErrorManager();

    /** ProgramError folder. */
    private static File errorDir;

    /** Queue of errors to be reported. */
    private final List<ProgramError> reportQueue = new ArrayList<ProgramError>();

    /** Thread used for sending errors. */
    private volatile Thread reportThread;

    /** Error list. */
    private final Map<Long, ProgramError> errors;

    /** Listener list. */
    private final ListenerList errorListeners = new ListenerList();

    /** Next error ID. */
    private long nextErrorID;

    /** Creates a new instance of ErrorListDialog. */
    private ErrorManager() {
        errors = new HashMap<Long, ProgramError>();
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
     * @param sendable True if the error is reportable, false otherwise
     */
    public void addError(final ProgramError error, final boolean sendable) {
        boolean report = IdentityManager.getGlobalConfig().getOptionBool(
                "general", "submitErrors")
                && !IdentityManager.getGlobalConfig().getOptionBool("temp",
                "noerrorreporting");

        synchronized (errors) {
            if (errors.containsValue(error)) {
                // It's a duplicate

                error.setReportStatus(ErrorReportStatus.NOT_APPLICABLE);
                error.setFixedStatus(ErrorFixedStatus.UNREPORTED);
                report = false;
            }

            errors.put(error.getID(), error);
        }

        for (String line : error.getTrace()) {
            if (line.startsWith("com.dmdirc.ui.swing.DMDircEventQueue")) {
                error.setReportStatus(ErrorReportStatus.NOT_APPLICABLE);
                error.setFixedStatus(ErrorFixedStatus.INVALID);
                report = false;
                break;
            } else if (line.startsWith("com.dmdirc")) {
                break;
            }
        }

        if (error.getMessage().startsWith("java.lang.NoSuchMethodError")
                || error.getMessage().startsWith("java.lang.NoClassDefFoundError")
                || error.getMessage().startsWith("java.lang.UnsatisfiedLinkError")) {
            error.setReportStatus(ErrorReportStatus.NOT_APPLICABLE);
            error.setFixedStatus(ErrorFixedStatus.INVALID);
            report = false;
        }

        if (!sendable) {
            error.setReportStatus(ErrorReportStatus.NOT_APPLICABLE);
            error.setFixedStatus(ErrorFixedStatus.UNREPORTED);
        } else if (report) {
            sendError(error);
        }

        if (IdentityManager.getGlobalConfig().getOptionBool("general", "logerrors")) {
            writeError(error);
        }

        if (error.getLevel() == ErrorLevel.FATAL && !report) {
            error.setReportStatus(ErrorReportStatus.FINISHED);
        }

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
        synchronized (errors) {
            errors.remove(error.getID());
        }

        fireErrorDeleted(error);
    }

    /**
     * Returns a list of errors.
     *
     * @return Error list
     */
    public Map<Long, ProgramError> getErrorList() {
        synchronized (errors) {
            return new HashMap<Long, ProgramError>(errors);
        }
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
    public long getNextErrorID() {
        return nextErrorID++;
    }

    /**
     * Returns specified program error.
     *
     * @param id ID of the error to fetch
     *
     * @return ProgramError with specified ID
     */
    public ProgramError getError(final long id) {
        return errors.get(id);
    }

    /**
     * Returns the list of program errors.
     *
     * @return Program error list
     */
    public List<ProgramError> getErrors() {
        synchronized (errors) {
            return new ArrayList<ProgramError>(errors.values());
        }
    }

    /**
     * Sends an error to the developers.
     *
     * @param error error to be sent
     */
    public void sendError(final ProgramError error) {
        if (error.getLevel().equals(ErrorLevel.FATAL)) {
            sendErrorInternal(error);
        } else {
            reportQueue.add(error);

            if (reportThread == null || !reportThread.isAlive()) {
                reportThread = new Thread(this, "Error reporting thread");
                reportThread.start();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        while (!reportQueue.isEmpty()) {
            sendErrorInternal(reportQueue.remove(0));

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException ex) {
                // Do nothing
            }
        }
    }

    /**
     * Sends an error to the developers.
     *
     * @param error ProgramError to be sent
     */
    private void sendErrorInternal(final ProgramError error) {
        final Map<String, String> postData = new HashMap<String, String>();
        List<String> response = new ArrayList<String>();
        int tries = 0;

        postData.put("message", error.getMessage());
        postData.put("trace", Arrays.toString(error.getTrace()));
        postData.put("version", Main.VERSION + "(" + Main.SVN_REVISION + ")");

        error.setReportStatus(ErrorReportStatus.SENDING);

        do {
            if (tries != 0) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    //Ignore
                }
            }
            try {
                response = Downloader.getPage("http://www.dmdirc.com/error.php", postData);
            } catch (MalformedURLException ex) {
                //Ignore, wont happen
            } catch (IOException ex) {
                //Ignore being handled
            }

            tries++;
        } while ((response.isEmpty() || !response.get(response.size() - 1).
                equalsIgnoreCase("Error report submitted. Thank you."))
                && tries <= 5);

        checkResponses(error, response);
    }

    /**
     * Checks the responses and sets status accordingly.
     *
     * @param error Error to check response
     * @param response Response to check
     */
    private static void checkResponses(final ProgramError error,
            final List<String> response) {
        if (!response.isEmpty() && response.get(response.size() - 1).
                equalsIgnoreCase("Error report submitted. Thank you.")) {
            error.setReportStatus(ErrorReportStatus.FINISHED);
        } else {
            error.setReportStatus(ErrorReportStatus.ERROR);
            return;
        }

        if (response.size() == 1) {
            error.setFixedStatus(ErrorFixedStatus.NEW);
            return;
        }

        final String responseToCheck = response.get(0);
        if (responseToCheck.matches(".*fixed.*")) {
            error.setFixedStatus(ErrorFixedStatus.FIXED);
        } else if (responseToCheck.matches(".*more recent version.*")) {
            error.setFixedStatus(ErrorFixedStatus.TOOOLD);
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
        if (listener == null) {
            return;
        }

        errorListeners.add(ErrorListener.class, listener);
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
        int firedListeners = 0;

        for (ErrorListener listener : errorListeners.get(ErrorListener.class)) {
            if (listener.isReady()) {
                listener.errorAdded(error);
                firedListeners++;
            }
        }

        if (firedListeners == 0) {
            System.err.println("An error has occurred: " + error.getLevel()
                    + ": " + error.getMessage());

            for (String line : error.getTrace()) {
                System.err.println("\t" + line);
            }
        }
    }

    /**
     * Fired when the program encounters a fatal error.
     *
     * @param error Error that occurred
     */
    protected void fireFatalError(final ProgramError error) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("A fatal error has occurred: " + error.getMessage());
            System.out.println(Arrays.toString(error.getTrace()));
        } else {
            FatalErrorDialog.displayBlocking(error);
        }
        
        new Timer("Fatal Error Timer").schedule(new TimerTask() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                while (error.getReportStatus() != ErrorReportStatus.FINISHED) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        //Ignore
                    }
                }
            }
        }, 0);
        System.exit(-1);
    }

    /**
     * Fired when an error is deleted.
     *
     * @param error Error that has been deleted
     */
    protected void fireErrorDeleted(final ProgramError error) {
        for (ErrorListener listener : errorListeners.get(ErrorListener.class)) {
            listener.errorDeleted(error);
        }
    }

    /**
     * Fired when an error's status is changed.
     *
     * @param error Error that has been altered
     */
    protected void fireErrorStatusChanged(final ProgramError error) {
        for (ErrorListener listener : errorListeners.get(ErrorListener.class)) {
            listener.errorStatusChanged(error);
        }
    }

    /**
     * Writes the specified error to a file.
     *
     * @param error ProgramError to write to a file.
     */
    private static void writeError(final ProgramError error) {
        final PrintWriter out = new PrintWriter(createNewErrorFile(error), true);
        out.println("Date:" + error.getDate());
        out.println("Level: " + error.getLevel());
        out.println("Description: " + error.getMessage());
        out.println("Details:");
        final String[] trace = error.getTrace();
        for (String traceLine : trace) {
            out.println('\t' + traceLine);
        }
        out.close();
    }

    /**
     * Creates a new file for an error and returns the output stream.
     *
     * @param error Error to create file for
     *
     * @return BufferedOutputStream to write to the error file
     */
    @SuppressWarnings("PMD.SystemPrintln")
    private static synchronized OutputStream createNewErrorFile(final ProgramError error) {
        if (errorDir == null || !errorDir.exists()) {
            errorDir = new File(Main.getConfigDir() + "errors");
            if (!errorDir.exists()) {
                errorDir.mkdirs();
            }
        }
        final String logName = error.getDate().getTime() + "-" + error.getLevel();


        final File errorFile = new File(errorDir, logName + ".log");

        if (errorFile.exists()) {
            boolean rename = false;
            int i = 0;
            while (!rename) {
                i++;
                rename = errorFile.renameTo(new File(errorDir, logName + "-" + i + ".log"));
            }
        }
        try {
            errorFile.createNewFile();
        } catch (IOException ex) {
            System.err.println("Error creating new file: ");
            ex.printStackTrace();
            return new NullOutputStream();
        }

        try {
            return new FileOutputStream(errorFile);
        } catch (FileNotFoundException ex) {
            System.err.println("Error creating new stream: ");
            ex.printStackTrace();
            return new NullOutputStream();
        }
    }

}
