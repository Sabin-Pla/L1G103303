package tests;

import common.TimeException;
import org.junit.Test;

public class TimeExceptionTest {
    String message = "message";
    TimeException time = new TimeException(message);

    @Test
    public void TimeExceptionTest(){
        assert (message != null);
    }
}
