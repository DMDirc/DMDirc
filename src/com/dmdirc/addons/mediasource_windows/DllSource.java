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

package com.dmdirc.addons.mediasource_windows;

import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.addons.nowplaying.MediaSourceState;

/**
 * Uses WindowsMediaSourcePlugin to retrieve now playing info.
 *
 * @author Shane
 */
public class DllSource implements MediaSource {

    /** Player name */
    private final String playerName;

    /** Use getArtistTitle */
    private final boolean useArtistTitle;

    /**
     * Instantiates the media source.
     *
     * @param playerName Name of Player and DLL
     */
    public DllSource(final String playerName) {
        this(playerName, false);
    }

    /**
     * Instantiates the media source.
     *
     * @param playerName Name of Player and DLL
     * @param useArtistTitle True if getArtistTitle should be parsed rather than
     *                       using getArtist() and getTitle()
     */
    public DllSource(final String playerName, final boolean useArtistTitle) {
        this.playerName = playerName;
        this.useArtistTitle = useArtistTitle;
    }

    /** {@inheritDoc} */
    @Override
    public String getAppName() {
        return playerName;
    }

    /**
     * Get the "goodoutput" from GetMediaInfo for the given command
     *
     * @param command Command to run
     * @return "Good" Output
     */
    private String getOutput(final String command) {
        return WindowsMediaSourcePlugin.getOutput(playerName, command).getGoodOutput();
    }

    /** {@inheritDoc} */
    @Override
    public MediaSourceState getState() {
        final MediaInfoOutput result = WindowsMediaSourcePlugin.getOutput(playerName, "getPlayState");

        if (result.getExitCode() == 0) {
            final String output = result.getGoodOutput();
            if (output.equalsIgnoreCase("stopped")) {
                return MediaSourceState.STOPPED;
            } else if (output.equalsIgnoreCase("playing")) {
                return MediaSourceState.PLAYING;
            } else if (output.equalsIgnoreCase("paused")) {
                return MediaSourceState.PAUSED;
            } else {
                return MediaSourceState.NOTKNOWN;
            }
        } else {
            return MediaSourceState.CLOSED;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getArtist() {
        if (useArtistTitle) {
            return getOutput("getArtistTitle").split("\\s-\\s", 2)[0];
        } else {
            return getOutput("getArtist");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        if (useArtistTitle) {
            String bits[] = getOutput("getArtistTitle").split("\\s-\\s", 2);
            return (bits.length > 1) ? bits[1] : "";
        } else {
            return getOutput("getTitle");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getAlbum() {
        return getOutput("getAlbum");
    }

    /**
     * Get the duration in seconds as a string.
     *
     * @param secondsInput to get duration for
     * @return Duration as a string
     */
    private String duration(final long secondsInput) {
        final StringBuilder result = new StringBuilder();
        final long hours = (secondsInput / 3600);
        final long minutes = (secondsInput / 60 % 60);
        final long seconds = (secondsInput % 60);

        if (hours > 0) {
            result.append(hours + ":");
        }
        result.append(String.format("%0,2d:%0,2d", minutes, seconds));

        return result.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String getLength() {
        try {
            final int seconds = Integer.parseInt(getOutput("getLength"));
            return duration(seconds);
        } catch (NumberFormatException nfe) {
        }
        return "Unknown";
    }

    /** {@inheritDoc} */
    @Override
    public String getTime() {
        try {
            final int seconds = Integer.parseInt(getOutput("getTime"));
            return duration(seconds);
        } catch (NumberFormatException nfe) {
        }
        return "Unknown";
    }

    /** {@inheritDoc} */
    @Override
    public String getFormat() {
        return getOutput("getFormat");
    }

    /** {@inheritDoc} */
    @Override
    public String getBitrate() {
        return getOutput("getBitrate");
    }

}
