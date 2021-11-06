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
