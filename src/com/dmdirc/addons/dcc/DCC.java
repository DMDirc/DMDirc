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

package com.dmdirc.addons.dcc;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * This class handles the main "grunt work" of DCC, subclasses process the data
 * received by this class.
 *
 * @author Shane 'Dataforce' McCormack
 */
public abstract class DCC implements Runnable {
	/** Address. */
	protected long address = 0;
	/** Port. */
	protected int port = 0;
	/** Socket used to communicate with. */
	protected Socket socket;
	/** The Thread in use for this. */
	private volatile Thread myThread;
	/** Are we already running? */
	protected boolean running = false;
	/** Are we a listen socket? */
	protected boolean listen = false;

	/**
	 * The current socket in use if this is a listen socket.
	 * This reference may be changed if and only if exactly one permit from the
	 * <code>serverSocketSem</code> and <code>serverListenignSem</code>
	 * semaphores is held by the thread doing the modification.
	 */
	private ServerSocket serverSocket;

	/**
	 * Semaphore to control write access to ServerSocket.
	 * If an object acquires a permit from the <code>serverSocketSem</code>, then
	 * <code>serverSocket</code> is <em>guaranteed</em> not to be externally
	 * modified until that permit is released, <em>unless</em> the object also
	 * acquires a permit from the <code>serverListeningSem</code>.
	 */
	private final Semaphore serverSocketSem = new Semaphore(1);

	/**
	 * Semaphore used when we're blocking waiting for connections.
	 * If an object acquires a permit from the <code>serverListeningSem</code>,
	 * then it is <em>guaranteed</em> that the {@link #run()} method is blocking
	 * waiting for incoming connections. In addition, it is <em>guaranteed</em>
	 * that the {@link #run()} method is holding the <code>serverSocketSem</code>
	 * permit, and it will continue holding that permit until it can reaquire
	 * the <code>serverListeningSem</code> permit.
	 */
	private final Semaphore serverListeningSem = new Semaphore(0);

	/**
	 * Creates a new instance of DCC.
	 */
	public DCC() {
		super();
	}

	/**
	 * Connect this dcc.
	 */
	public void connect() {
		try {
			if (listen) {
				address = 0;
				port = serverSocket.getLocalPort();
			} else {
				// socket = new Socket(longToIP(address), port, bindIP, 0);
				socket = new Socket(longToIP(address), port);
				socketOpened();
			}
		} catch (IOException ioe) {
            socketClosed();
			return;
		}

		myThread = new Thread(this);
		myThread.start();
	}

	/**
	 * Start a listen socket rather than a connect socket.
	 *
	 * @throws IOException If the listen socket can't be created
	 */
	public void listen() throws IOException {
		serverSocketSem.acquireUninterruptibly();
		serverSocket = new ServerSocket(0, 1);
		serverSocketSem.release();

		listen = true;
		connect();
	}

	/**
	 * Start a listen socket rather than a connect socket, use a port from the
	 * given range.
	 *
	 * @param startPort Port to try first
	 * @param endPort Last port to try.
	 * @throws IOException If no sockets were available in the given range
	 */
	public void listen(final int startPort, final int endPort) throws IOException {
		listen = true;

		for (int i = startPort; i <= endPort; ++i) {
			try {
				serverSocketSem.acquireUninterruptibly();
				serverSocket = new ServerSocket(i, 1);
				serverSocketSem.release();
				// Found a socket we can use!
				break;
			} catch (IOException ioe) {
				// Try next socket.
			} catch (SecurityException se) {
				// Try next socket.
			}
		}

		if (serverSocket == null) {
			throw new IOException("No available sockets in range "+startPort+":"+endPort);
		} else {
			connect();
		}
	}

	/**
	 * This handles the socket to keep it out of the main thread
	 */
	@Override
	public void run() {
		if (running) { return; }
		running = true;
		// handleSocket is implemented by sub classes, and should return false
		// when the socket is closed.
		Thread thisThread = Thread.currentThread();

		while (myThread == thisThread) {
			serverSocketSem.acquireUninterruptibly();

			if (serverSocket == null) {
				serverSocketSem.release();
				
				if (!handleSocket()) {
					close();
					break;
				}
			} else {
				try {
					serverListeningSem.release();
					socket = serverSocket.accept();
					serverSocket.close();
					socketOpened();
				} catch (IOException ioe) {
                    socketClosed();
					break;
				} finally {
					serverListeningSem.acquireUninterruptibly();
					serverSocket = null;
					serverSocketSem.release();
				}
			}
			
			// Sleep for a short period of time to reduce CPU usage.
			try { Thread.sleep(100); } catch (InterruptedException ie) { }
		}
		// Socket closed

		thisThread = null;
		running = false;
	}

	/**
	 * Called to close the socket
	 */
	protected void close() {
		boolean haveSLS = false;

		while (!serverSocketSem.tryAcquire() && !(haveSLS = serverListeningSem.tryAcquire())) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				// Do we care? I doubt we do! Should be unchecked damnit.
			}
		}

		if (serverSocket != null) {
			try {
				if (!serverSocket.isClosed()) {
					serverSocket.close();
				}
			} catch (IOException ioe) { }
			serverSocket = null;
		}

		if (haveSLS) {
			serverListeningSem.release();
		} else {
			serverSocketSem.release();
		}

		if (socket != null) {
			try {
				if (!socket.isClosed()) {
					socket.close();
				}
			} catch (IOException ioe) { }
			socketClosed();
			socket = null;
		}
	}

	/**
	 * Called when the socket is first opened, before any data is handled.
	 */
	protected void socketOpened() { }

	/**
	 * Called when the socket is closed, before the thread terminates.
	 */
	protected void socketClosed() { }

	/**
	 * Check if this socket can be written to.
	 *
	 * @return True if the socket is writable, false otehrwise
	 */
	public boolean isWriteable() {
		return false;
	}

	/**
	 * Handle the socket.
	 *
	 * @return false when socket is closed, true will cause the method to be
	 *         called again.
	 */
	protected abstract boolean handleSocket();

	/**
	 * Set the address to connect to for this DCC
	 *
	 * @param address Address as an int (Network Byte Order, as specified in the DCC CTCP)
	 * @param port Port to connect to
	 */
	public void setAddress(final long address, final int port) {
		this.address = address;
		this.port = port;
	}

	/**
	 * Is this a listening socket
	 *
	 * @return True if this is a listening socket
	 */
	public boolean isListenSocket() {
		return listen;
	}

	/**
	 * Get the host this socket is listening on/connecting to
	 *
	 * @return The IP that this socket is listening on/connecting to.
	 */
	public String getHost() {
		return longToIP(address);
	}

	/**
	 * Get the port this socket is listening on/connecting to
	 *
	 * @return The port that this socket is listening on/connecting to.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Convert the given IP Address to a long
	 *
	 * @param ip Input IP Address
	 * @return ip as a long
	 */
	public static long ipToLong(final String ip) {
		final String bits[] = ip.split("\\.");
		if (bits.length > 3) {
			return (Long.parseLong(bits[0]) << 24) + (Long.parseLong(bits[1]) << 16) + (Long.parseLong(bits[2]) << 8) + Long.parseLong(bits[3]);
		}
		return 0;
	}

	/**
	 * Convert the given long to an IP Address
	 *
	 * @param in Input long
	 * @return long as an IP
	 */
	public static String longToIP(final long in) {
		return ((in & 0xff000000) >> 24)+"."+((in & 0x00ff0000) >> 16)+"."+((in & 0x0000ff00) >> 8)+"."+(in & 0x000000ff);
	}
}
