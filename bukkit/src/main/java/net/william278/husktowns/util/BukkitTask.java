/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.util;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.HuskTowns;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.scheduling.GracefulScheduling;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public interface BukkitTask extends Task {

    class Sync extends Task.Sync implements BukkitTask {

        private ScheduledTask task;

        protected Sync(@NotNull HuskTowns plugin, @NotNull Runnable runnable, long delayTicks) {
            super(plugin, runnable, delayTicks);
        }

        @Override
        public void cancel() {
            if (task != null && !cancelled) {
                task.cancel();
            }
            super.cancel();
        }

        @Override
        public void run() {
            if (isPluginDisabled()) {
                runnable.run();
                return;
            }

            if (delayTicks > 0) {
                this.task = getScheduler().globalRegionalScheduler().runDelayed(runnable, delayTicks);
            } else {
                this.task = getScheduler().globalRegionalScheduler().run(runnable);
            }
        }
    }

    class Async extends Task.Async implements BukkitTask {

        private ScheduledTask task;

        protected Async(@NotNull HuskTowns plugin, @NotNull Runnable runnable, long delayTicks) {
            super(plugin, runnable, delayTicks);
        }

        @Override
        public void cancel() {
            if (task != null && !cancelled) {
                task.cancel();
            }
            super.cancel();
        }

        @Override
        public void run() {
            if (isPluginDisabled()) {
                runnable.run();
                return;
            }

            if (delayTicks > 0) {
                this.task = getScheduler().globalRegionalScheduler().runDelayed(runnable, delayTicks);
            } else {
                this.task = getScheduler().globalRegionalScheduler().run(runnable);
            }

            if (!cancelled) {
                if (delayTicks > 0) {
                    this.task = getScheduler().asyncScheduler().runDelayed(runnable, Duration.of(delayTicks / 20 * 1000, ChronoUnit.MILLIS));
                } else {
                    this.task = getScheduler().asyncScheduler().run(runnable);
                }
            }
        }
    }

    class Repeating extends Task.Repeating implements BukkitTask {

        private ScheduledTask task;

        protected Repeating(@NotNull HuskTowns plugin, @NotNull Runnable runnable, long repeatingTicks) {
            super(plugin, runnable, repeatingTicks);
        }

        @Override
        public void cancel() {
            if (task != null && !cancelled) {
                task.cancel();
            }
            super.cancel();
        }

        @Override
        public void run() {
            if (isPluginDisabled()) {
                return;
            }

            if (!cancelled) {
                this.task = getScheduler().asyncScheduler().runAtFixedRate(runnable, Duration.ZERO, Duration
                        .of(repeatingTicks * 50L, ChronoUnit.MILLIS)
                );
            }
        }
    }

    // Returns if the Bukkit HuskTowns plugin is disabled
    default boolean isPluginDisabled() {
        return !((BukkitHuskTowns) getPlugin()).isEnabled();
    }

    interface Supplier extends Task.Supplier {

        @NotNull
        @Override
        default Task.Sync getSyncTask(@NotNull Runnable runnable, long delayTicks) {
            return new BukkitTask.Sync(getPlugin(), runnable, delayTicks);
        }

        @NotNull
        @Override
        default Task.Async getAsyncTask(@NotNull Runnable runnable, long delayTicks) {
            return new BukkitTask.Async(getPlugin(), runnable, delayTicks);
        }

        @NotNull
        @Override
        default Task.Repeating getRepeatingTask(@NotNull Runnable runnable, long repeatingTicks) {
            return new BukkitTask.Repeating(getPlugin(), runnable, repeatingTicks);
        }

        @Override
        default void cancelTasks() {
            ((BukkitHuskTowns) getPlugin()).getScheduler().cancelGlobalTasks();
        }

    }

    @NotNull
    default GracefulScheduling getScheduler() {
        return ((BukkitHuskTowns) getPlugin()).getScheduler();
    }

}
