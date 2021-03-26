package common;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class SimulationClock extends Clock {

    private Instant startInstant;
    private Instant actualStartInstant;
    private long compressionFactor;

    public SimulationClock(Instant startInstant, long compressionFactor) {
        this.startInstant = startInstant;
        this.compressionFactor = compressionFactor;
    }

    public long getCompressionFactor() {
        return compressionFactor;
    }

    public void start() {
        actualStartInstant = Instant.now();
    }

    @Override
    public Instant instant() {
        if (actualStartInstant == null) return startInstant;
        Instant now = Instant.now();
        long seconds = now.getEpochSecond() - actualStartInstant.getEpochSecond();
        int nanos = now.getNano() - actualStartInstant.getNano();
        if (nanos < 0) {
            seconds--;
            nanos = now.getNano() + 1000000000 - actualStartInstant.getNano();
        }
        Duration duration = Duration.ofSeconds(seconds, nanos).multipliedBy(compressionFactor);
        return startInstant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return Clock.systemUTC().getZone();
    }

    @Override
    public Clock withZone(ZoneId zoneId) {
        return null;
    }
}
