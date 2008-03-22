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

package com.dmdirc.ui.swing.textpane;

import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Searches the textpane for specified phrases.
 */
public class IRCDocumentSearcher implements IRCDocumentListener {

    /** Occurances of the phrase. */
    private Map<Integer, LinePosition> occurrences;
    /** Document to search. */
    private IRCDocument document;
    /** Phrase to search for. */
    private String phrase;
    /** Textpane position. */
    private LinePosition position;

    /**
     * Constructs a new IRC Document searcher.
     * 
     * @param phrase Phrase to search for
     * @param document Document to search
     */
    public IRCDocumentSearcher(final String phrase,
            final IRCDocument document) {
        this.phrase = phrase;
        this.document = document;
        this.occurrences = new TreeMap<Integer, LinePosition>();

        this.position = getEndPosition();

        document.addIRCDocumentListener(this);
    }

    /**
     * Returns the end position in the document.
     * 
     * @return End position
     */
    private LinePosition getEndPosition() {
        final int documentSize = document.getNumLines() - 1;
        final int lineLength = document.getLineLength(documentSize);

        return new LinePosition(documentSize, lineLength, documentSize,
                lineLength);
    }
    
    /**
     * Sets the position of the current match
     * 
     * @param position New match position
     */
    public void setPosition(final LinePosition position) {
        this.position = position;
    }

    /**
     * Searches up in the document.
     * 
     * @return Line position of the next match
     */
    public LinePosition searchUp() {
        if (position == null) {
            position = getEndPosition();
        }
        int tempLine = position.getStartLine();
        List<Match> matches = new ArrayList<Match>();
        String line = "";
        while (matches.size() == 0 && tempLine > -1) {
            line = document.getLineText(tempLine);
            System.out.println("Searching: " + line);
            matches = searchLine(line);
            tempLine--;
        }
        if (matches.size() == 0) {
            return null;
        }
        
        final Match current = new Match(position.getStartPos(), position.getEndPos());
        ListIterator<Match> i = matches.listIterator(matches.size());
        while (i.hasPrevious()) {
            Match match = i.previous();
            if (match.compareTo(current) == -1) {
                return new LinePosition(tempLine, match.start, tempLine, match.end);
            }
        }

        return null;
    }

    /**
     * Searches a line and returns all matches on a line.
     * 
     * @param line Line to search
     * 
     * @return List of matches
     */
    private List<Match> searchLine(final String line) {
        final List<Match> matches = new ArrayList<Match>();
        Matcher matcher = Pattern.compile(phrase).matcher(line);

        while (matcher.find()) {
            matches.add(new Match(matcher.start(), matcher.end()));
        }

        return matches;
    }

    /**
     * Random test method.
     * 
     * @param args CLI params
     */
    public static void main(final String[] args) {
        final String search = "to";
        IRCDocument document = new IRCDocument();
        document.addText(new AttributedString("rar! this is a text sentence to search"));
        document.addText(new AttributedString("we need to have several of these"));
        document.addText(new AttributedString("then we can pretend to search through them if we want"));

        IRCDocumentSearcher searcher = new IRCDocumentSearcher(search, document);

        searcher.setPosition(new LinePosition(1, 0, 1, 0));
        
        System.out.println("Searching for: \"" + search + "\"");
        LinePosition position = searcher.searchUp();
        System.out.println(position);
        
        searcher.setPosition(position);
        
        System.out.println("Searching for: \"" + search + "\"");
        position = searcher.searchUp();
        System.out.println(position);
    }

    /** {@inheritDoc} */
    @Override
    public void lineAdded(final int line, final int size) {
    //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void trimmed(final int numLines) {
    //check for invalid lineposition pointer
    }

    /** {@inheritDoc} */
    @Override
    public void cleared() {
        occurrences.clear();
    }
}

/**
 * Represents the boundaries of a match.
 */
class Match implements Comparable<Match> {

    /**  Start of a match. */
    public final int start;
    /**  End of a match. */
    public final int end;

    /**
     * Instantiates a new match.
     * 
     * @param start Match start
     * @param end Match end
     */
    public Match(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "[" + start + "-" + end + "]";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Match other = (Match) obj;
        return this.start == other.start && this.end == other.end;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.start;
        hash = 89 * hash + this.end;
        return hash;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Match o) {
        if (this.equals(o) || end == o.end) {
            return 0;
        }
        if (end > o.end) {
            return 1;
        } else {
            return -1;
        }
    }
}
