/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.dcc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles a DCC Send.
 *
 * @author Shane 'Dataforce' McCormack
 */
public class DCCSend extends DCC {

    /** List of active sends. */
    private static final List<DCCSend> SENDS = new ArrayList<DCCSend>();

    /** File Transfer Types. */
    public enum TransferType {

        SEND, RECEIVE;

    }

    /** The File transfer type for this file. */
    private TransferType transferType = TransferType.RECEIVE;

    /** The handler for this DCCSend. */
    private DCCSendInterface handler;

    /** Used to send data out the socket. */
    private DataOutputStream out;

    /** Used to read data from the socket. */
    private DataInputStream in;

    /** File we are using. */
    private File transferFile;

    /** Used to write data to the file. */
    private DataOutputStream fileOut;

    /** Used to read data from the file. */
    private DataInputStream fileIn;

    /** Where are we starting from? */
    private int startpos;

    /** How big is this file? */
    private long size = -1;

    /** How much of this file have we read so far? */
    private long readSize;

    /** What is the name of the file? */
    private String filename = "";

    /** What is the token for this send? */
    private String token = "";

    /** Block Size. */
    private final int blockSize;

    /** Is this a turbo dcc? */
    private boolean turbo = false;

    /** Creates a new instance of DCCSend with a default block size. */
    public DCCSend() {
        this(1024);
    }

    /**
     * Creates a new instance of DCCSend.
     *
     * @param blockSize Block size to use
     */
    public DCCSend(final int blockSize) {
        super();
        this.blockSize = blockSize;
        synchronized (SENDS) {
            SENDS.add(this);
        }
    }

    /**
     * Reset this send to be used again (eg a resend).
     */
    public void reset() {
        close();
        setFileName(filename);
        setFileStart(startpos);
    }

    /**
     * Get a copy of the list of active sends.
     *
     * @return A copy of the list of active sends.
     */
    public static List<DCCSend> getSends() {
        synchronized (SENDS) {
            return new ArrayList<DCCSend>(SENDS);
        }
    }

    /**
     * Called to remove this object from the sends list.
     */
    public void removeFromSends() {
        synchronized (SENDS) {
            SENDS.remove(this);
        }
    }

    /**
     * Set the filename of this file
     *
     * @param filename Filename
     */
    public void setFileName(final String filename) {
        this.filename = filename;
        if (transferType == TransferType.SEND) {
            transferFile = new File(filename);
            try {
                fileIn = new DataInputStream(new FileInputStream(transferFile.getAbsolutePath()));
            } catch (FileNotFoundException e) {
                fileIn = null;
            } catch (SecurityException e) {
                fileIn = null;
            }
        }
    }

    /**
     * Get the filename of this file
     *
     * @return Filename
     */
    public String getFileName() {
        return filename;
    }

    /**
     * Get the filename of this file, without the path
     *
     * @return Filename without path
     */
    public String getShortFileName() {
        return (new File(filename)).getName();
    }

    /**
     * Set dcc Type.
     *
     * @param type Type of DCC Send this is.
     */
    public void setType(final TransferType type) {
        this.transferType = type;
    }

    /**
     * Get dcc Type.
     *
     * @return Type of DCC Send this is.
     */
    public TransferType getType() {
        return transferType;
    }

    /**
     * Set turbo mode on/off.
     * Turbo mode doesn't wait for ack packets. Only relevent when sending.
     *
     * @param turbo True for turbo dcc, else false
     */
    public void setTurbo(final boolean turbo) {
        this.turbo = turbo;
    }

    /**
     * Is turbo mode on/off.
     * Turbo mode doesn't wait for ack packets. Only relevent when sending.
     *
     * @return True for turbo dcc, else false
     */
    public boolean isTurbo() {
        return turbo;
    }

    /**
     * Set the Token for this send
     *
     * @param token Token for this send
     */
    public void setToken(final String token) {
        this.token = token;
    }

    /**
     * Get the Token for this send
     *
     * @return Token for this send
     */
    public String getToken() {
        return token;
    }

    /**
     * Make a Token for this send.
     * This token will be unique compared to all the other known sends
     *
     * @return The Token for this send.
     */
    public String makeToken() {
        String myToken = "";
        boolean unique = true;
        do {
            myToken = Integer.toString(Math.abs((myToken + filename).hashCode()));
            unique = (findByToken(myToken) == null);
        } while (!unique);
        setToken(myToken);
        return myToken;
    }

    /**
     * Find a send based on a given token.
     *
     * @param token Token to look for. (case sensitive)
     * @return The first DCCSend that matches the given token.
     *         null if none match, or token is "" or null.
     */
    public static DCCSend findByToken(final String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        for (DCCSend send : getSends()) {
            if (send.getToken().equals(token)) {
                return send;
            }
        }
        return null;
    }

    /**
     * Set the size of the file
     *
     * @param size File size
     */
    public void setFileSize(final long size) {
        this.size = size;
    }

    /**
     * Get the expected size of the file
     *
     * @return The expected File size (-1 if unknown)
     */
    public long getFileSize() {
        return size;
    }

    /**
     * Set the starting position of the file
     *
     * @param startpos Starting position
     * @return -1 if fileIn is null or if dcc receive, else the result of fileIn.skipBytes()
     */
    public int setFileStart(final int startpos) {
        this.startpos = startpos;
        if (transferType == TransferType.SEND && fileIn != null) {
            try {
                this.startpos = fileIn.skipBytes(startpos);
                readSize = startpos;
                return this.startpos;
            } catch (IOException ioe) {
            }
        }
        return -1;
    }

    /**
     * Get the starting position of the file
     *
     * @return starting position of file.
     */
    public int getFileStart() {
        return this.startpos;
    }

    /**
     * Change the handler for this DCC Send
     *
     * @param handler A class implementing DCCSendInterface
     */
    public void setHandler(final DCCSendInterface handler) {
        this.handler = handler;
    }

    /**
     * Called when the socket is first opened, before any data is handled.
     */
    @Override
    protected void socketOpened() {
        try {
            transferFile = new File(filename);
            if (transferType == TransferType.RECEIVE) {
                fileOut = new DataOutputStream(new FileOutputStream(transferFile.getAbsolutePath(), (startpos > 0)));
            }
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            if (handler != null) {
                handler.socketOpened(this);
            }
        } catch (IOException ioe) {
            socketClosed();
        }
    }

    /**
     * Called when the socket is closed, before the thread terminates.
     */
    @Override
    protected void socketClosed() {
        // Try to close both, even if one fails.
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        out = null;
        in = null;
        if (handler != null) {
            handler.socketClosed(this);
        }
        // Try to delete empty files.
        if (transferType == TransferType.RECEIVE && transferFile.length() == 0) {
            transferFile.delete();
        }
        synchronized (SENDS) {
            SENDS.remove(this);
        }
    }

    /**
     * Handle the socket.
     *
     * @return false when socket is closed, true will cause the method to be
     *         called again.
     */
    @Override
    protected boolean handleSocket() {
        if (out == null || in == null) {
            return false;
        }
        if (transferType == TransferType.RECEIVE) {
            return handleReceive();
        } else {
            return handleSend();
        }
    }

    /**
     * Handle the socket as a RECEIVE.
     *
     * @return false when socket is closed (or should be closed), true will cause the method to be
     *         called again.
     */
    protected boolean handleReceive() {
        try {
            final byte[] data = new byte[blockSize];
            final int bytesRead = in.read(data);
            readSize = readSize + bytesRead;

            if (bytesRead > 0) {
                if (handler != null) {
                    handler.dataTransfered(this, bytesRead);
                }
                fileOut.write(data, 0, bytesRead);
                // Send ack
                out.writeInt((int) readSize);
                out.flush();
                if (readSize == size) {
                    fileOut.close();
                    return false;
                } else {
                    return true;
                }
            } else if (bytesRead < 0) {
                fileOut.close();
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * Handle the socket as a SEND.
     *
     * @return false when socket is closed (or should be closed), true will cause the method to be
     *         called again.
     */
    protected boolean handleSend() {
        try {
            final byte[] data = new byte[blockSize];
            int bytesRead = fileIn.read(data);
            readSize = readSize + bytesRead;

            if (bytesRead > 0) {
                if (handler != null) {
                    handler.dataTransfered(this, bytesRead);
                }
                out.write(data, 0, bytesRead);
                out.flush();

                // Wait for acknowlegement packet.
                if (!turbo) {
                    int bytesRecieved;
                    do {
                        bytesRecieved = in.readInt();
                    } while ((readSize - bytesRecieved) > 0);
                }

                if (readSize == size) {
                    fileIn.close();

                    // Process all the ack packets that may have been sent.
                    // In true turbo dcc mode, none will have been sent and the socket
                    // will just close, in fast-dcc mode all the acks will be here,
                    // So keep reading acks untill the socket closes (IOException) or we
                    // have recieved all the acks.
                    if (turbo) {
                        int ack = 0;
                        do {
                            try {
                                ack = in.readInt();
                            } catch (IOException e) {
                                break;
                            }
                        } while (ack > 0 && (readSize - ack) > 0);
                    }

                    return false;
                }

                return true;
            } else if (bytesRead < 0) {
                fileIn.close();
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

}
