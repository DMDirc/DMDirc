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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.font.LineMetrics;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * JLabel that wraps automatically.
 *
 * @author Shane Mc Cormack
 */
public class JWrappingLabel extends JLabel {
	/** {@inheritDoc} */
	public JWrappingLabel() {
		super();
	}
	
	/** {@inheritDoc} */
	public JWrappingLabel(final String text) {
		super(text);
	}
	
	/** {@inheritDoc} */
	public JWrappingLabel(final String text, final int horizontalAlignment) {
		super(text, horizontalAlignment);
	}
	
	/** {@inheritDoc} */
	@Override
	public void paint(final Graphics g) {
		// Get a decent graphics object
		final Graphics2D g2 = (Graphics2D)g;
		
		// Split the text
		final String[] bits = getText().split(" ");
		
		// This is used to make sure we don't draw a blank line at the top if the
		// first word is too long.
		boolean isFirst = true;
		
		// Current drawing positions
		int top = 0;
		int height = 0;
		int left = 0;
		
		// The width of a space
		int spaceWidth = (int)g.getFont().getStringBounds(" ", g2.getFontRenderContext()).getWidth();
		
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
			
			// Check if this word fits on the current line or not.
			// If there are no other words on this line, we assume the word fits
			// (Thus long words don't get split up)
			if (thisWidth+left > getWidth() && thisWidth != 0) {
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
			line.append(bit+" ");
			// And thats that!
			isFirst = false;
		}
		if (line.length() > 0) { lines.add(line.toString()); }
		
		// Now draw
		top = 0;
		for (String drawLine : lines) {
			Rectangle2D bounds = g.getFont().getStringBounds(drawLine, g2.getFontRenderContext());
			LineMetrics metrics = g.getFont().getLineMetrics(drawLine, g2.getFontRenderContext());
			
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
			top = (int)(top + bounds.getHeight());
		}
		
		// Update component height.
		setSize(getWidth(), top+height);
	}
}