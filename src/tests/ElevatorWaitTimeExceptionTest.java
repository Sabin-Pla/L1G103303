package tests;

import floor.ElevatorWaitTimeException;
import org.junit.Test;

public class ElevatorWaitTimeExceptionTest {
    String message = "message";
    ElevatorWaitTimeException dir = new ElevatorWaitTimeException(message);

    @Test
    public void DirectionTest(){
        assert (dir != null);
    }
}
