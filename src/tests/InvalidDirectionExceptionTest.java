package tests;

import floor.InvalidDirectionException;
import org.junit.Test;

public class InvalidDirectionExceptionTest {
    String message = "message";
    InvalidDirectionException dir = new InvalidDirectionException(message);

    @Test
    public void DirectionTest(){
        assert (dir != null);
    }
}
