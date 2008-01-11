/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import java.net.Socket;
import java.io.File;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
/**
 * This class handles a DCC Send
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: DCCSend.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public class DCCSend extends DCC {
	/** File Transfer Types */
	public enum TransferType { SEND, RECIEVE; }
	/** The File transfer type for this file */
	private TransferType transferType = TransferType.RECIEVE;
	/** The handler for this DCCSend */
	private DCCSendInterface handler = null;
	/** Used to send data out the socket */
	private DataOutputStream out;
	/** Used to read data from the socket */
	private DataInputStream in;
	/** File we are using */
	private File transferFile;
	/** Used to write data to the file */
	private DataOutputStream fileOut;
	/** Used to read data from the file */
	private DataInputStream fileIn;
	/** Where are we starting from? */
	private long startpos = 0;
	/** How big is this file? */
	private long size = -1;
	/** How much of this file have we read so far? */
	private long readSize = 0;
	/** What is the name of the file? */
	private String filename = "";
	/** Block Size */
	private int blockSize = 1024;
	/** Is this a turbo dcc? */
	private boolean turbo = false;
	
	/**
	 * Set the filename of this file
	 *
	 * @param filename Filename
	 */
	public void setFileName(final String filename) {
		this.filename = filename;
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
	public boolean getTurbo() {
		return turbo;
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
	 */
	public void setFileStart(final long startpos) {
		this.startpos = startpos;
	}
	
	/**
	 * Creates a new instance of DCCSend.
	 */
	public DCCSend() {
		super();
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
	protected void socketOpened() {
		try {
			transferFile = new File(filename);
			if (transferType == TransferType.RECIEVE) {
				fileOut = new DataOutputStream(new FileOutputStream(transferFile.getAbsolutePath()));
			} else if (transferType == TransferType.SEND) {
				fileIn = new DataInputStream(new FileInputStream(transferFile.getAbsolutePath()));
			}
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
			if (handler != null) {
				handler.socketOpened(this);
			}
		} catch (IOException ioe) { }
	}
	
	/**
	 * Called when the socket is closed, before the thread terminates.
	 */
	protected void socketClosed() {
		// Try to close both, even if one fails.
		try { out.close(); } catch (Exception e) { }
		try { in.close(); } catch (Exception e) { }
		out = null;
		in = null;
		if (handler != null) {
			handler.socketClosed(this);
		}
	}
	
	/**
	 * Handle the socket.
	 *
	 * @return false when socket is closed, true will cause the method to be
	 *         called again.
	 */
	protected boolean handleSocket() {
		if (out == null || in == null) { return false; }	
		if (transferType == TransferType.RECIEVE) {
			return handleRecieve();
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
	protected boolean handleRecieve() {
		try {
			byte[] data = new byte[blockSize];
			int bytesRead = in.read(data);
			readSize = readSize + bytesRead;
			
			if (bytesRead > 0) {
				fileOut.write(data, 0, bytesRead);
				out.writeInt(bytesRead);
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
			byte[] data = new byte[blockSize];
			int bytesRead = fileIn.read(data);
			readSize = readSize + bytesRead;
			
			if (bytesRead > 0) {
				out.write(data, 0, bytesRead);
				out.flush();
				
				if (!turbo) {
					while (bytesRead > 0) {
						bytesRead = bytesRead - in.readInt();
					}
				}
				
				if (readSize == size) {
					fileIn.close();
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
}
