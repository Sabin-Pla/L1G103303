package floorSubSystemTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import floorSubSystem.DataStorage;

/**
 * This class tests the DataStorage.java class
 * 
 * @author Aayush Mallya 
 * @version Iteration 1
 */
public class DataStorageTest {
	private DataStorage request;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		request = new DataStorage("05:34:27.0", 5, false, 2);
	}
	
	/**
	 * constructor test
	 */
	@Test
	public void newDataStorageTest() {
        assertNotNull(request);
    }
	
	/**
	 * Main test
	 */
	@Test
	public void test() {
		assertSame("05:34:27.0", request.getRequestTime());
		assertEquals(5, request.getCurrentFloor());
		assertFalse(request.getGoingUp());
		assertEquals(2, request.getDestinationFloor());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		request = null;
	}
}
