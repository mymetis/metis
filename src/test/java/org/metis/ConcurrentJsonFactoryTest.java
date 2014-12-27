package org.metis;

import static org.junit.Assert.*;
import static org.metis.utils.Utils.parseJson;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

/**
 * Run concurrent tests against Utils.parseJson().
 * 
 * @author jfernandez
 * 
 */

// Run all test methods concurrently using the ConcurrentJunitRunner class
@RunWith(ConcurrentJunitRunner.class)
// Specify the thread pool size
@Concurrent(threads = 10)
public class ConcurrentJsonFactoryTest {

	// Test json parsing util
	@Test
	public void TestA() {

		// start with a single json object
		String jsonStr = "{\"name\":\"Joe Fernandez\", \"gender\":\"MALE\",  "
				+ "\"email\": \"joe.fernandez@ttmsolutions.com\", \"verified\":false, "
				+ "\"age\":22 }";

		InputStream is = new ByteArrayInputStream(jsonStr.getBytes());
		List<Map<String, String>> rows = null;
		try {
			rows = parseJson(is);
		} catch (Exception e) {
			fail("Got this exception when parsing json packet: "
					+ e.getLocalizedMessage());
		}
		assertEquals(true, rows != null);
		assertEquals(false, rows.isEmpty());
		assertEquals(true, rows.size() == 1);
		assertEquals(true, rows.get(0).get("name").equals("Joe Fernandez"));
		assertEquals(true, rows.get(0).get("gender").equals("MALE"));
		assertEquals(
				true,
				rows.get(0).get("email")
						.equals("joe.fernandez@ttmsolutions.com"));
		assertEquals(true, rows.get(0).get("verified").equals("false"));
		assertEquals(true, rows.get(0).get("age").equals("22"));

	}

	@Test
	public void TestB() {

		// now an array of json objects
		String jsonStr = "[{\"name\": \"Joe Fernandez\", \"gender\":\"MALE\"}, "
				+ " {\"name\": \"Rey Berin\",     \"gender\":\"MALE\"}]";

		InputStream is = new ByteArrayInputStream(jsonStr.getBytes());
		List<Map<String, String>> rows = null;
		try {
			rows = parseJson(is);
		} catch (Exception e) {
			fail("Got this exception when parsing json packet: "
					+ e.getLocalizedMessage());
		}

		assertEquals(true, rows != null);
		assertEquals(false, rows.isEmpty());
		assertEquals(true, rows.size() == 2);
		assertEquals(true, rows.get(0).get("name").equals("Joe Fernandez"));
		assertEquals(true, rows.get(1).get("name").equals("Rey Berin"));
		assertEquals(true, rows.get(0).get("gender").equals("MALE"));
		assertEquals(true, rows.get(1).get("gender").equals("MALE"));

	}

	@Test
	public void TestC() {

		// now an array of json objects
		String jsonStr = "[{\"name\": \"Wilma Fernandez\", \"gender\":\"FEMALE\"}, "
				+ " {\"name\": \"Fred Berin\",     \"gender\":\"MALE\"}]";

		InputStream is = new ByteArrayInputStream(jsonStr.getBytes());
		List<Map<String, String>> rows = null;
		try {
			rows = parseJson(is);
		} catch (Exception e) {
			fail("Got this exception when parsing json packet: "
					+ e.getLocalizedMessage());
		}

		assertEquals(true, rows != null);
		assertEquals(false, rows.isEmpty());
		assertEquals(true, rows.size() == 2);
		assertEquals(true, rows.get(0).get("name").equals("Wilma Fernandez"));
		assertEquals(true, rows.get(1).get("name").equals("Fred Berin"));
		assertEquals(true, rows.get(0).get("gender").equals("FEMALE"));
		assertEquals(true, rows.get(1).get("gender").equals("MALE"));

	}

	@Test
	public void TestD() {

		// now an array of json objects
		String jsonStr = "[{\"name\": \"Dino\", \"gender\":\"NOIDEA\"}, "
				+ " {\"name\": \"Fred\",     \"gender\":\"MALE\"}]";

		InputStream is = new ByteArrayInputStream(jsonStr.getBytes());
		List<Map<String, String>> rows = null;
		try {
			rows = parseJson(is);
		} catch (Exception e) {
			fail("Got this exception when parsing json packet: "
					+ e.getLocalizedMessage());
		}

		assertEquals(true, rows != null);
		assertEquals(false, rows.isEmpty());
		assertEquals(true, rows.size() == 2);
		assertEquals(true, rows.get(0).get("name").equals("Dino"));
		assertEquals(true, rows.get(1).get("name").equals("Fred"));
		assertEquals(true, rows.get(0).get("gender").equals("NOIDEA"));
		assertEquals(true, rows.get(1).get("gender").equals("MALE"));

	}

	@Test
	public void TestE() {

		// now an array of json objects
		String jsonStr = "[{\"name\": \"Dino\", \"age\":\"10\"}, "
				+ " {\"name\": \"Fred\",     \"age\":\"55\"}]";

		InputStream is = new ByteArrayInputStream(jsonStr.getBytes());
		List<Map<String, String>> rows = null;
		try {
			rows = parseJson(is);
		} catch (Exception e) {
			fail("Got this exception when parsing json packet: "
					+ e.getLocalizedMessage());
		}

		assertEquals(true, rows != null);
		assertEquals(false, rows.isEmpty());
		assertEquals(true, rows.size() == 2);
		assertEquals(true, rows.get(0).get("name").equals("Dino"));
		assertEquals(true, rows.get(0).get("age").equals("10"));
		assertEquals(true, rows.get(1).get("name").equals("Fred"));
		assertEquals(true, rows.get(1).get("age").equals("55"));
	}

}
