package tests;

import common.SimulationClock;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;

public class SimulationClockTest {
    Instant startInstant;
    long compressionFactor;
    SimulationClock sim;

    @Before
    public void Init() {
        sim = new SimulationClock(startInstant, compressionFactor);
    }

    @Test
    public void SimTest(){
        assert (sim.getCompressionFactor() == compressionFactor);
        assert (sim.getZone() == Clock.systemUTC().getZone());
        assert (sim.withZone(sim.getZone()) == null);
        assert (sim.instant() == startInstant);
    }
}
