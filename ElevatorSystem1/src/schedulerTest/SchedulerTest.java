package schedulerTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import elevatorSubSystem.Elevator;
import floorSubSystem.DataStorage;
import floorSubSystem.Floor;
import scheduler.Scheduler;

/**
 * This class tests the Scheduler.java class
 * 
 * @author Harshil Verma
 * @version Iteration 1
 */
public class SchedulerTest {
	private Scheduler scheduler;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		scheduler = new Scheduler();
	}

	/**
	 * @throws InterruptedException
	 */
	@Test
	public void test() throws InterruptedException {
		Thread floor = new Thread(new Floor(scheduler));
		Thread elevator = new Thread(new Elevator(scheduler));
		floor.start();
		elevator.start();
		assertNotNull(scheduler.getNewRequest());
		DataStorage data = new DataStorage();
		scheduler.setRequest(data);
		assertSame(data, scheduler.getNewRequest());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		scheduler = null;
		assertNull(scheduler);
	}
}
