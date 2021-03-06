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

package com.dmdirc.addons.time;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.plugins.BasePlugin;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Provides various time-related features.
 */
public final class TimePlugin  extends BasePlugin {

    /** Have we registered our types already? */
    private static boolean registered;
    /** The timer to use for scheduling. */
    private Timer timer;
    /** The TimerCommand we've registered. */
    private TimerCommand command;
    /** The Manager to use for managing timers. */
    private TimerManager manager;

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        if (!registered) {
            ActionManager.getActionManager().registerTypes(
                    TimeActionType.values());
            registered = true;
        }

        final int offset = 60 - Calendar.getInstance().get(Calendar.SECOND);

        timer = new Timer("Time plugin timer");

        timer.schedule(new TimerTask() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                runTimer();
            }
        }, 1000 * offset, 1000 * 60);

        manager = new TimerManager();
        command = new TimerCommand(manager);
        CommandManager.getCommandManager().registerCommand(command);
    }

    /** Handles a timer event that occurs every minute. */
    public void runTimer() {
        final Calendar cal = Calendar.getInstance();

        ActionManager.getActionManager().triggerEvent(
                TimeActionType.TIME_MINUTE, null, cal);

        if (cal.get(Calendar.MINUTE) == 0) {
            ActionManager.getActionManager().triggerEvent(
                    TimeActionType.TIME_HOUR, null, cal);

            if (cal.get(Calendar.HOUR_OF_DAY) == 0) {
                ActionManager.getActionManager().triggerEvent(
                        TimeActionType.TIME_DAY, null, cal);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        manager = null;
        CommandManager.getCommandManager().unregisterCommand(command);
    }

    /**
     * Returns the manager that is assigned to this Plugin.
     *
     * @return The Instance of TimeManager that is associated with this plugin
     */
    public TimerManager getTimerManager() {
        return manager;
    }
}
