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
