package tests;

import org.junit.Test;
import scheduler.ElevatorPositionException;

import java.lang.reflect.Type;

import static scheduler.ElevatorPositionException.Type.NOT_STOPPED;

public class ElevatorPositionExceptionTest {

    public static final int elevatorNumber = 5;

    @Test
    public void ElevatorPositionExceptionTest() {

        ElevatorPositionException e = new ElevatorPositionException("message", NOT_STOPPED, elevatorNumber);
        assert (e.getType() == NOT_STOPPED);

        assert (e.getElevator() == elevatorNumber);

    }
}
