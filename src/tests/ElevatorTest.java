package tests;

import scheduler.Scheduler;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import elevator.Elevator;

/**
 * This class tests the Elevator.java class
 * 
 * @author Mmedara Josiah
 * @version Iteration 1
 */
public class ElevatorTest {
	private Elevator realElevator;;
	private Elevator fakeElevator;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	public void setUp() throws Exception{
		fakeElevator = new Elevator(null);
		realElevator = new Elevator(new Scheduler());
	}
	
	/**
	 * This method tests creating a new elevator
	 */
    @Test
    public void NewElevatorTest() {
        assertNotNull(fakeElevator);
        assertNotNull(realElevator);
    }

    /**
     * This method tests exceptions
     */
    @Test
    public void exceptionTest() {
        assertThrows(NullPointerException.class, () -> fakeElevator.toString(),
                "Expected toString() to throw since scheduler is null");
        assertThrows(NullPointerException.class, () -> fakeElevator.run(),
                "Expected run() to throw since scheduler is null");
    }
}
