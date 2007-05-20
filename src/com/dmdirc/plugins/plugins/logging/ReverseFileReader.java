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

package com.dmdirc.plugins.plugins.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.EOFException;
import java.util.Stack;

/**
 * Reads a file in reverse.
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: ReverseFileReader.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public class ReverseFileReader {
	/** File to manipulate. */
	private RandomAccessFile file;
	/** \r Character */
	private static final byte r = 0X0D;
	/** \n Character */
	private static final byte n = 0X0A;
	/** Number of bytes to skip backwards at a time. */
	private byte seekLength = 50;

	/**
	 * Create a new ReverseFileReader.
	 *
	 * @param filename File to open.
	 * @throws FileNotFoundException If the file is not a regular file.
	 * @throws SecurityException If a security manager exists and its checkRead method denies read access to the file.
	 * @throws IOException If there is an error seeking to the end of the file.
	 */
	public ReverseFileReader(String filename) throws FileNotFoundException, SecurityException, IOException {
		file = new RandomAccessFile(filename, "r");
		reset();
	}
	
	/**
	 * Create a new ReverseFileReader.
	 *
	 * @param myFile Existing file to use.
	 * @throws FileNotFoundException If the file is not a regular file.
	 * @throws SecurityException If a security manager exists and its checkRead method denies read access to the file.
	 * @throws IOException If there is an error seeking to the end of the file.
	 */
	public ReverseFileReader(File myFile) throws FileNotFoundException, SecurityException, IOException {
		file = new RandomAccessFile(myFile, "r");
		reset();
	}
	
	/**
	 * Reset the file pointer to the end of the file.
	 *
	 * @throws IOException If there is an error seeking, or the file is closed.
	 */
	public void reset() throws IOException {
		if (file == null) { throw new IOException("File has been closed."); }
		file.seek(file.length());
	}
	
	/**
	 * Get the current seekLength.
	 *
	 * @return current seekLength
	 */
	public byte getSeekLength() {
		return seekLength;
	}
	
	/**
	 * Set the seekLength.
	 *
	 * @param newValue New value for seekLength
	 */
	public void setSeekLength(byte newValue) {
		seekLength = newValue;
	}
	
	/**
	 * Close the file pointer.
	 * This should be called before removing the reference to this object
	 *
	 * @throws IOException If there is an error closing the file, or if it has been closed already.
	 */
	public void close() throws IOException {
		if (file == null) { throw new IOException("File has been closed."); }
		file.close();
		file = null;
	}

	/**
	 * Get the next full line.
	 *
	 * @return The next full line.
	 * @throws EOFException If there is no more lines.
	 * @throws IOException If an error reading or seeking occured, or if the fiel is closed.
	 */
	public String getNextLine() throws EOFException, IOException {
		if (file == null) { throw new IOException("File has been closed."); }
		// Used to store result to output.
		StringBuilder line = new StringBuilder();
		// Used to store position in file pre-read
		long fp = 0;
		// Used to store position in file when this is called
		long startfp = 0;
		// Used to store read bytes
		byte[] bytes = new byte[seekLength];
		// Distance seeked
		int seekDistance = 0;
		
		// Check current position, if 0 we are at the start of the file
		// and should throw an exception.
		startfp = file.getFilePointer();
		if (startfp == 0) {
			throw new EOFException("Reached Start of file");
		}
		
		// Keep looping untill we get a full line, or the end of the file
		while (true) {
			// Get Current Position
			fp = file.getFilePointer();
			
			// Check how far to seek backwards (seekLength or to the start of the file)
			if (fp < seekLength) {
				// Seek to the start of the file;
				seekDistance = (int)fp;
				fp = 0;
			} else {
				// Seek to position current-seekLength
				seekDistance = seekLength;
				fp = fp - seekDistance;
			}
			// Seek!
			file.seek(fp);
			
			// Read into the bytes array (This will read seekLength bytes)
			file.read(bytes);
			
			// And loop looking for data
			// This uses seekDistance so that only wanted data is checked.
			for (int i = seekDistance-1; i >= 0; --i) {
				// Check for New line Character, or a non carraige-return char
				if (bytes[i] == n) {
					// Seek to the location of this character and exit this loop.
					file.seek(fp+i);
					break;
				} else if (bytes[i] != r) {
					// Add to the result, the loop will continue going.
					line.append((char)bytes[i]);
				}
			}
			// We have now processed the data we read (Either added it all to the
			// buffer, or found a newline character.
			
			// "fp" is where we started reading from, "file.getFilePointer()"
			// is where we are at now. If they are not the same then we found
			// a new line somewhere, else we read all "seekDistance" bytes and
			// need to read another amount of bytes.
			if (fp != (file.getFilePointer()-seekDistance)) {
				// We have found a new line somewhere, thus we don't need
				// to read any more bytes, so exit the while loop!
				break;
			} else {
				// We have not found a new line anywhere,
				// Seek to the pre-read position, and repeat.
				file.seek(fp);
			}
		}
		
		// If the current position is past the position we started at, then
		// set the file position back to 0, this will cause the next attempt
		// tp read a line to throw an EOFException.
		if (startfp < file.getFilePointer()) {
			file.seek(0);
		}
		
		// Return the data obtained. (we reverse the string buffer because we
		// add to it in reverse, so this fixes it to go in the correct order)
		return line.reverse().toString();
	}

	/**
	 * Try and get x number of lines.
	 * If the file is closed, an empty stack will be returned.
	 *
	 * @param numLines Number of lines to try and get.
	 */
	public Stack<String> getLines(int numLines) {
		Stack<String> result = new Stack<String>();
		for (int i = 0; i < numLines; ++i) {
			try {
				result.push(getNextLine());
			} catch (IOException e) {
				break;
			}
		}
		return result;
	}
}