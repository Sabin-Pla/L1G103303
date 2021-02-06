package floorSubSystemTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import floorSubSystem.Floor;
import scheduler.Scheduler;

/**
 * This class tests the Floor.java class
 * 
 * @author 
 * @version Iteration 1
 */
public class FloorTest {
	private Floor realFloor;
	private Floor fakeFloor;
	private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	private final PrintStream realOut = System.out;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		fakeFloor = new Floor(null);
		realFloor = new Floor(new Scheduler());
	}
	
	/**
	 * test the print stream
	 */
	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(outStream));
	}

	/**
	 * test set out
	 */
	@After
	public void setStreamsOut() {
	    System.setOut(realOut);
	}
	
	/**
	 * This method tests exceptions
	 */
	@Test
	public void exceptionTest() {
		assertThrows(NullPointerException.class, () -> fakeFloor.run(), "Expected run() to throw since scheduler is null");
	}

	/**
	 * Main test
	 */
	@Test
	public void test() {
		assertNotSame(realFloor.getNumberOfRequests(),0);
		assertNotSame(fakeFloor.getNumberOfRequests(),0);
		realFloor.printRequests();
	    assertNotNull(outStream.toString());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		realFloor = null;
		fakeFloor = null;
		assertNull(fakeFloor);
		assertNull(realFloor);
	}
}
