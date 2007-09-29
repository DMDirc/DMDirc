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

package com.dmdirc.addons.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine;
import java.io.File;
import java.io.IOException;

/**
 * The AudioPlayer handles the playing of the audio
 *
 * @author Shane "Dataforce" Mc Cormack
 * @version $Id: AudioPlayer.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public final class AudioPlayer extends Thread {
	/** The AudioType enum */
	private enum AudioType { WAV, OTHER; }
	
	/** The file object of the file to play */
	final File myFile;
	
	/**
	 * Create the AudioPlayer
	 *
	 * @param file The file to play
	 */
	public AudioPlayer(final File file) {
		myFile = file;
	}
	
	/**
	 * Run this AudioPlayer
	 */
	public void run() {
		final AudioType type = getAudioType(myFile);
		switch (type) {
			case WAV:
				playWav();
				break;
			default:
				break;
		}
	}
	
	/**
	 * Check if this File is a supported file type
	 *
	 * @param file the File to check
	 * @return true if playable, else false.
	 */
	public static boolean isValid(final File file) {
		final AudioType type = getAudioType(file);
		return (type != AudioType.OTHER);
	}
	
	/**
	 * Get the AudioType of a given file
	 *
	 * @param file the File to check
	 * @return AudioType for this file.
	 */
	public static AudioType getAudioType(final File file) {
		AudioType type;
		try {
			AudioSystem.getAudioInputStream(file);
			type = AudioType.WAV;
		} catch (Exception e) {
			type = AudioType.OTHER;
		}
		return type;
	}
	
	/**
	 * Play the file as a wav file.
	 * Based on http://www.anyexample.com/programming/java/java_play_wav_sound_file.xml
	 *
	 */
	private void playWav() {
		final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(myFile);
		} catch (Exception e) { return; }
		
		AudioFormat format = audioInputStream.getFormat();
		SourceDataLine auline = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		
		try {
			auline = (SourceDataLine) AudioSystem.getLine(info);
			auline.open(format);
		} catch (Exception e) { return; }
		
		auline.start();
		int nBytesRead = 0;
		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
 
		try {
			while (nBytesRead != -1) {
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
				if (nBytesRead >= 0) {
					auline.write(abData, 0, nBytesRead);
				}
			}
		} catch (Exception e) {
			/** Do Nothing */
		} finally {
			auline.drain();
			auline.close();
		}
		try {
			audioInputStream.close();
		} catch (Exception e) { }
	}
}

