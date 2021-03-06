/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes,
 * Simon Mott
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
package com.dmdirc.addons.ui_swing.framemanager.buttonbar;

import com.dmdirc.FrameContainer;
import com.dmdirc.ui.interfaces.Window;
import javax.swing.Icon;
import javax.swing.JToggleButton;

/**
 * Custom toggle button that contains Window information for this button.
 *
 * @author Simon Mott
 * @since 0.6.4
 */
public class FrameToggleButton extends JToggleButton {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Contains the window associated with this button. */
    private final Window window;

    /** Create a new instance of FrameToggleButton. */
    public FrameToggleButton(final String text, final Icon icon, final Window window) {
        super(text, icon);
        this.window = window;
    }

    /**
     * Returns the window associated with this button.
     *
     * @return Window associated with this button
     */
    public Window getWindow() {
        return window;
    }

    /**
     * Returns the FrameContainer associated with this button.
     *
     * @return FrameContainer associated with this button
     */
    public FrameContainer getFrameContainer() {
        return window.getContainer();
    }

}
