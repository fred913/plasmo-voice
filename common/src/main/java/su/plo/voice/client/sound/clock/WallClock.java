/*
 * This file is licensed under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * Because it contains modified code from:
 * https://github.com/RestComm/media-core/blob/master/scheduler/src/main/java/org/restcomm/media/core/scheduler/WallClock.java
 */

package su.plo.voice.client.sound.clock;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

public class WallClock {
    private TimeUnit unit = TimeUnit.NANOSECONDS;
    @Getter
    private long time = System.nanoTime();
    @Getter
    private long currentTime = System.currentTimeMillis();

    public long getTime(TimeUnit timeUnit) {
        return timeUnit.convert(time, unit);
    }

    public TimeUnit getTimeUnit() {
        return unit;
    }

    public void tick(long amount) {
        time += amount;
        currentTime += TimeUnit.NANOSECONDS.toMillis(amount);
    }
}
