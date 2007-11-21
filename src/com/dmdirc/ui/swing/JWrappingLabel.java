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

import javax.swing.JLabel;
import javax.swing.Icon;

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
		
		// Now loop through the words
		for (String bit : bits) {
			// Get the sizes of this word
			Rectangle2D bounds = g.getFont().getStringBounds(bit, g2.getFontRenderContext());
			LineMetrics metrics = g.getFont().getLineMetrics(bit, g2.getFontRenderContext());
			
			// Approx height of this word (total letter height minus overhang).
			int y = top + (int)Math.round(bounds.getHeight() - metrics.getDescent());
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
				// It doesn't so we need to move down a line.
				// This stops us leaving a blank line at the very top if the first
				// word is too big.
				if (!isFirst) {
					top = top + height;
					height = 0;
				}
				// Change of plans, draw in this location instead!
				y = top + (int)Math.round(bounds.getHeight() - metrics.getDescent());
				left = 0;
			}
			// Draw the word
			g.drawString(bit, left, y);
			left = left+thisWidth+spaceWidth;
			// And thats that!
			isFirst = false;
		}
		
		// Update component height.
		setSize(getWidth(), top+height);
	}
}