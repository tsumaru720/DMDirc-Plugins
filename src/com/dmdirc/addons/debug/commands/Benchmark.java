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

package com.dmdirc.addons.debug.commands;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;

/**
 * Textpane speed benchmark, outputs a large volume of text and times the amount
 * of time it takes the textpane to draw this.
 */
public class Benchmark extends DebugCommand {

    /**
     * Creates a new instance of the command.
     *
     * @param command Parent command
     */
    public Benchmark(final Debug command) {
        super(command);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "benchmark";
    }

    /** {@inheritDoc} */
    @Override
    public String getUsage() {
        return " - Runs a textpane benchmark";
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        long[] results = new long[10];

        for (int i = 0; i < results.length; i++) {
            final long start = System.nanoTime();

            for (int j = 0; j < 5000; j++) {
                origin.addLine(FORMAT_OUTPUT,
                        "This is a benchmark. Lorem ipsum doler...");
            }

            final long end = System.nanoTime();

            results[i] = end - start;
        }

        for (int i = 0; i < results.length; i++) {
            origin.addLine(FORMAT_OUTPUT, "Iteration " + i + ": " + results[i]
                    + " nanoseconds.");
        }
    }
}
