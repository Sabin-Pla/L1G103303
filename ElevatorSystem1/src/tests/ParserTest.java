package tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import floor.Parser;

/**
 * This class tests the Parser.java class
 * 
 * @author John Afolayan
 * @version Iteration 1
 */
public class ParserTest {
	private Parser p;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		p = new Parser();
	}

	/**
	 * Main test
	 */
	@Test
	public void test() {
		assertNotNull(p.getRequestFromFile());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		p = null;
		assertNull(p);
	}
}
