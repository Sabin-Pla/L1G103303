package tests;

import org.junit.Test;
import scheduler.ElevatorPositionException;

import java.lang.reflect.Type;

import static scheduler.ElevatorPositionException.Type.NOT_STOPPED;

public class ElevatorPositionExceptionTest {

    public enum Type {NOT_STOPPED, PATH_MISMATCH, WRONG_ARRIVAL_FLOOR};
    public static final int elevatorNumber = 5;

    @Test
    public void ElevatorPositionExceptionTest() {
        Type type = Type.NOT_STOPPED;

        ElevatorPositionException e = new ElevatorPositionException("message", NOT_STOPPED, elevatorNumber);
        assert (e.getType() == NOT_STOPPED);

        assert (e.getElevator() == elevatorNumber);

    }
}
