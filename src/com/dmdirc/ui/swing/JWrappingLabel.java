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

package com.dmdirc.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.font.LineMetrics;
import java.util.ArrayList;

import java.util.Map;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

/**
 * JLabel that wraps automatically.
 * Wrapping occurs on newlines and spaces.
 *
 * @author Shane Mc Cormack
 */
public class JWrappingLabel extends JComponent {
	/**
	 * A version number for this class.
	 * It should be changed whenever the class structure is changed (or anything
	 * else that would prevent serialized objects being unserialized with the
	 * new class).
	 */
	private static final long serialVersionUID = 2007112201;

	/** My prefered height. */
	private int myPreferredHeight = 0;
	/** My prefered width. */
	private int myPreferredWidth = 0;

	/** My Text */
	private String text;
	/** My Horizontal Alignment */
	private int horizontalAlignment;

	/**
	 * Create a new Default JWrappingLabel
	 */
	public JWrappingLabel() {
		this("", SwingConstants.LEFT);
	}

	/**
	 * Create a new JWrappingLabel with some initial text
	 *
	 * @param text Text to use on the label
	 */
	public JWrappingLabel(final String text) {
		this(text, SwingConstants.LEFT);
	}

	/**
	 * Create a new JWrappingLabel with some initial text and a Horizontal Alignment
	 *
	 * @param text Text to use on the label
	 * @param horizontalAlignment Horizontal Alignment to use. (SwingConstants CENTER LEFT or RIGHT)
	 */
	public JWrappingLabel(final String text, final int horizontalAlignment) {
		super();
		this.text = text;
		this.horizontalAlignment = horizontalAlignment;
	}


	/**
	 * Get the Horizontal Alignment of this label
	 *
	 * @return the Text of this label
	 */
	public int getHorizontalAlignment() {
		return horizontalAlignment;
	}


	/**
	 * Set the Horizontal Alignment of this label
	 *
	 * @param horizontalAlignment The new Horizontal Alignment of this label
	 */
	public void setHorizontalAlignment(final int horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
		repaint();
	}

	/**
	 * Get the text of this label
	 *
	 * @return the Text of this label
	 */
	public String getText() {
		return text;
	}

	/**
	 * Set the text of this label
	 *
	 * @param text the new Text of this label
	 */
	public void setText(final String text) {
		this.text = text;
		repaint();
	}

	/** {@inheritDoc} */
	public Dimension getMinimumSize() {
		if (isMinimumSizeSet()) {
			return super.getMinimumSize();
		} else {
			doPaint(getGraphics(), true);
			return new Dimension(0, myPreferredHeight);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Dimension getPreferredSize() {
		if (isPreferredSizeSet()) {
			return super.getPreferredSize();
		} else {
			doPaint(getGraphics(), true);
			return new Dimension(myPreferredWidth, myPreferredHeight);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void paint(final Graphics g) {
		doPaint(g, false);
	}

	/**
	 * This method handles painting.
	 *
	 * @param g Graphics object to paint into
	 * @param getPreferred If true, assume unlimited width and height, and don't
	 *                     actually paint, only set the values for
	 *                     myPreferredHeight and myPreferredWidth
	 */
	private void doPaint(final Graphics g, final boolean getPreferred) {
		if (g == null || getText() == null) { return; }

		// Get a decent graphics object
		final Graphics2D g2 = (Graphics2D)g;

		if (!getPreferred && isOpaque()) {
			Color oldColor = g.getColor();
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(oldColor);
		}

		// Split the lines
		final String[] allLines = getText().split("\n");

		// Current drawing positions
		int top = 0;
		int height = 0;
		int maxLeft = 0;
		// The width of a space
		int spaceWidth = (int)g.getFont().getStringBounds(" ", g2.getFontRenderContext()).getWidth();

		for (String thisLine : allLines) {
			int left = 0;
			// Top of this block of lines
			int startTop = top;
			// Split the text
			final String[] bits = thisLine.split(" ");

			// This is used to make sure we don't draw a blank line at the top if the
			// first word is too long.
			boolean isFirst = true;

			// Store each line to draw afterwards (allows for alignment)
			ArrayList<String> lines = new ArrayList<String>();
			// The line we have atm
			StringBuilder line = new StringBuilder();

			// Now loop through the words
			for (String bit : bits) {
				// Get the sizes of this word
				Rectangle2D bounds = g.getFont().getStringBounds(bit, g2.getFontRenderContext());
				LineMetrics metrics = g.getFont().getLineMetrics(bit, g2.getFontRenderContext());

				// Width of this word
				int thisWidth = (int)bounds.getWidth();

				// Update line height so we know how far to drop down when drawing the
				// next line
				if (height < bounds.getHeight()) {
					height = (int)bounds.getHeight();
				}


				boolean needNewLine;
				// Check if this word fits on the current line or not.
				// If there are no other words on this line, we assume the word fits
				// (Thus long words don't get split up)
				int totalWidth = thisWidth+left;
				if ((isPreferredSizeSet() || myPreferredWidth > 0) && getWidth() > 0) {
					needNewLine = ((totalWidth > getWidth() || (totalWidth > getMaximumSize().getWidth() && isMaximumSizeSet())) && thisWidth != 0);
				} else {
					// If no preferred width is set, we assume we can draw as wide as we
					// want, unless we have a maximum width.
					needNewLine = (totalWidth > getMaximumSize().getWidth());
				}

				if (needNewLine) {
					// It doesn't fit so we need to move down a line.

					// This stops us leaving a blank line at the very top if the first
					// word is too big.
					if (!isFirst) {
						top = top + height;
						height = 0;
						lines.add(line.toString());
						line = new StringBuilder();
					}
					left = 0;
				}
				left = left+thisWidth+spaceWidth;
				if (maxLeft < left) { maxLeft = left; }
				line.append(bit+" ");
				// And thats that!
				isFirst = false;
			}
			if (line.length() > 0) { lines.add(line.toString()); }

			// Now draw
			top = startTop;
			for (String drawLine : lines) {
				Rectangle2D bounds = g.getFont().getStringBounds(drawLine, g2.getFontRenderContext());

				// Only draw if we are not just trying to get the preferred size.
				// otherwise we just update top
				if (!getPreferred) {
					LineMetrics metrics = g.getFont().getLineMetrics(drawLine, g2.getFontRenderContext());

					final Map desktopHints = (Map) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
					if (desktopHints != null) {
						g2.addRenderingHints(desktopHints);
					}
					// However, we need to take into account the overhang in characters like y and g
					int y = top + (int)Math.round(bounds.getHeight() - metrics.getDescent());

					// Now to get where the left should go.
					int x;
					int alignment = getHorizontalAlignment();
					if (alignment == SwingConstants.CENTER) {
						x = (int)((getWidth()/2) - (bounds.getWidth()/2));
					} else if (alignment == SwingConstants.RIGHT) {
						x = (int)(getWidth() - bounds.getWidth());
					} else {
						x = 0;
					}

					// And finally actually draw the word
					g.drawString(drawLine, x, y);
				}
				top = (int)(top + bounds.getHeight());
				// If we are going to start trying to draw higher than allowed, stop.
				if (top >= getMaximumSize().getHeight() && getMaximumSize().getHeight() != 0 && isMaximumSizeSet()) { break; }
			}
		}

		if (getPreferred) {
			// Update component height.
			setSize(getWidth(), top);
			// Update prefered sizes
			myPreferredHeight = top;
			myPreferredWidth = maxLeft;
		}
	}
}
