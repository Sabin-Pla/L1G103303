package mainClassTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import elevatorSubSystem.Elevator;
import floorSubSystem.Floor;
import scheduler.Scheduler;

/**
 * This class tests the Main.java class
 * 
 * @version Iteration 1
 */
public class MainTest {
	private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	private PrintStream realOut = System.out;
	private Scheduler scheduler;
	private Thread floorThread;
	private Thread schedulerThread;
	private Thread elevatorThread;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {
		scheduler = new Scheduler();
		floorThread = new Thread(new Floor(scheduler), "Floor");
		schedulerThread = new Thread(scheduler, "Scheduler");
		elevatorThread = new Thread(new Elevator(scheduler), "Elevator");
	}

	/**
	 * test set up streams
	 */
	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(outStream));
	}

	/**
	 * test set stream out
	 */
	@After
	public void setStreamsOut() {
		System.setOut(realOut);
	}

	/**
	 * main test
	 */
	@Test
	public void test() {
		assertNotNull(outStream.toString());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	public void tearDown() throws Exception {
		scheduler = null;
		floorThread = null;
		schedulerThread = null;
		elevatorThread = null;
		assertNull(scheduler);
		assertNull(floorThread);
		assertNull(schedulerThread);
		assertNull(elevatorThread);
	}
}
