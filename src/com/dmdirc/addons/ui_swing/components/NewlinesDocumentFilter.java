/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Filters input to remove new lines.
 */
public class NewlinesDocumentFilter extends DocumentFilter {

    /** Creates a new instance of Newlines Documment filter. */
    public NewlinesDocumentFilter() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public void insertString(final DocumentFilter.FilterBypass fb,
            final int offset, final String string, final AttributeSet attr)
            throws BadLocationException {
        super.insertString(fb, offset, sanitise(string), attr);

    }

    /** {@inheritDoc} */
    @Override
    public void replace(final DocumentFilter.FilterBypass fb, final int offset,
            final int length, final String text, final AttributeSet attrs)
            throws BadLocationException {
        super.replace(fb, offset, length, sanitise(text), attrs);
    }

    /**
     * Removes new lines from the text.
     *
     * @param text Text to sanitise
     *
     * @return Sanitised string
     */
    private String sanitise(final String text) {
        if (text.contains("\n")) {
            return text.replace("\n", "");
        }
        return text;
    }
}
