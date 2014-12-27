package org.metis;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.After;

/**
 * Example of how to run concurrent tests. The test methods in this class are
 * invoked simultaneously; each within their own thread of execution. All you
 * need to make sure of is that the thread pool size is equal to or greater
 * then the number of methods.
 * 
 * @author jfernandez
 * 
 */

// Run all test methods concurrently using the ConcurrentJunitRunner class
@RunWith(ConcurrentJunitRunner.class)
// Specify the thread pool size
@Concurrent(threads = 10)
public class ConcurrentTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testA() {
		System.out.println("testA Thread name = "
				+ Thread.currentThread().getName());
	}

	@Test
	public final void testB() {
		System.out.println("testB Thread name = "
				+ Thread.currentThread().getName());
	}

	@Test
	public final void testC() {
		System.out.println("testC Thread name = "
				+ Thread.currentThread().getName());
	}

	@Test
	public final void testD() {
		System.out.println("testD Thread name = "
				+ Thread.currentThread().getName());
	}

}
