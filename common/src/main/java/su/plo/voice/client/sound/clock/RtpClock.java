/*
 * This file is licensed under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * Because it contains modified code from:
 * https://github.com/RestComm/media-core/blob/master/rtp/src/main/java/org/restcomm/media/core/rtp/RtpClock.java
 */

package su.plo.voice.client.sound.clock;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

public class RtpClock {
    // absolute time clock
    @Getter
    private WallClock wallClock;

    //the clock rate measured in Hertz.
    @Getter
    private int clockRate;
    private int scale;

    //the difference between media time measured by local and remote clock
    protected long drift;

    //the flag indicating the state of relation between local and remote clocks
    //the flag value is true if relation established
    @Getter
    private boolean synced;

    public RtpClock(WallClock wallClock) {
        this.wallClock = wallClock;
    }

    public void setClockRate(int clockRate) {
        this.clockRate = clockRate;
        this.scale = clockRate / 1000;
    }

    public void synchronize(long remote) {
        this.drift = remote - getLocalRtpTime();
        this.synced = true;
    }

    public void reset() {
        this.drift = 0;
        this.clockRate = 0;
        this.synced = false;
    }

    public long getLocalRtpTime() {
        return scale * wallClock.getTime(TimeUnit.MILLISECONDS) + drift;
    }

    public long convertToAbsoluteTime(long timestamp) {
        return timestamp * 1000 / clockRate;
    }

    public long convertToRtpTime(long time) {
        return time * clockRate / 1000;
    }
}
