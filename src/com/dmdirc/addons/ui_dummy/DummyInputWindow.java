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

package com.dmdirc.addons.ui_dummy;

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.UIController;

/**
 * Dummy input window, used for testing.
 */
public class DummyInputWindow implements InputWindow {

    /** Our container. */
    private final WritableFrameContainer container;

    /**
     * Instantiates a new DummyInputWindow.
     *
     * @param owner Parent window
     * @param commandParser Parent command parser
     */
    public DummyInputWindow(final WritableFrameContainer owner,
            final CommandParser commandParser) {
        this.container = owner;
    }

    /** {@inheritDoc} */
    @Override
    public InputHandler getInputHandler() {
        return new DummyInputHandler(new DummyInputField(), null, this);
    }

    /** {@inheritDoc} */
    @Override
    public WritableFrameContainer getContainer() {
        return container;
    }

    /** {@inheritDoc} */
    @Override
    public UIController getController() {
        return new DummyController();
    }

}
