package org.metis;

import org.junit.BeforeClass;
import org.junit.Test;
import org.metis.pull.WdsResourceBean;
import org.metis.utils.Utils;
import static org.metis.utils.Utils.*;
import static org.metis.utils.Statics.*;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import static java.net.URLEncoder.encode;

/**
 * Runs some misc/general tests against the WdsResourceBean class.
 * 
 * @author jfernandez
 * 
 */

// Test methods will be executed in ascending order by name
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UtilsTest {

	// this will be the RDB that we test against.
	static WdsResourceBean rdb;

	@BeforeClass
	public static void initialize() throws Exception {
		// create our test rdb
		rdb = new WdsResourceBean();
	}

	// Test extra path util
	@Test
	public void TestA() {
		// make sure we have a rdb and run some simple tests against it
		assertEquals(true, (rdb != null));
		String pathInfo = "";
		assertEquals(true, getExtraPathInfo(pathInfo) == null);
		pathInfo = "foo";
		assertEquals(true, getExtraPathInfo(pathInfo) == null);
		pathInfo = "foo/bar";
		assertEquals(true, getExtraPathInfo(pathInfo).length == 2);
		pathInfo = "/foo/bar";
		assertEquals(true, getExtraPathInfo(pathInfo).length == 2);
		pathInfo = "/foo/bar/";
		assertEquals(true, getExtraPathInfo(pathInfo).length == 2);
		pathInfo = "/my/foo/bar/";
		assertEquals(true, getExtraPathInfo(pathInfo).length == 2);
		String[] tokens = getExtraPathInfo(pathInfo);
		assertEquals(true, tokens[0].equals("foo"));
		assertEquals(true, tokens[1].equals("bar"));
		pathInfo = "/my/fOo/BAR/";
		tokens = getExtraPathInfo(pathInfo);
		assertEquals(true, tokens[0].equals("foo"));
		assertEquals(true, tokens[1].equals("BAR"));
		pathInfo = "/my/fOo/BAR";
		tokens = getExtraPathInfo(pathInfo);
		assertEquals(true, tokens[0].equals("foo"));
		assertEquals(true, tokens[1].equals("BAR"));
		pathInfo = "my/FOO/BAR";
		tokens = getExtraPathInfo(pathInfo);
		assertEquals(true, tokens[0].equals("foo"));
		assertEquals(true, tokens[1].equals("BAR"));
		pathInfo = "my/FOOS/BARS";
		tokens = getExtraPathInfo(pathInfo);
		assertEquals(true, tokens[0].equals("foo"));
		assertEquals(true, tokens[1].equals("BARS"));
		pathInfo = "my/little/FOOS/BARS";
		tokens = getExtraPathInfo(pathInfo);
		assertEquals(true, tokens[0].equals("foo"));
		assertEquals(true, tokens[1].equals("BARS"));
	}

	// Test json parsing util
	@Test
	public void TestB() {

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

		// now an array of json objects
		jsonStr = "[{\"name\": \"Joe Fernandez\", \"gender\":\"MALE\"}, "
				+ " {\"name\": \"Rey Berin\",     \"gender\":\"MALE\"}]";

		is = new ByteArrayInputStream(jsonStr.getBytes());
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

		// embed an array within an array
		jsonStr = "[{\"name\":\"Joe Fernandez\",\"gender\":\"MALE\"}, "
				+ "[{\"name\":\"Rey Berin\",\"gender\":\"MALE\"},"
				+ " {\"name\":\"Shelby\", \"gender\":\"FEMALE\"}]]";

		is = new ByteArrayInputStream(jsonStr.getBytes());
		try {
			rows = parseJson(is);
		} catch (Exception e) {
			fail("Got this exception when parsing json packet: "
					+ e.getLocalizedMessage());
		}

		assertEquals(true, rows != null);
		assertEquals(false, rows.isEmpty());
		assertEquals(true, rows.size() == 3);
		assertEquals(true, rows.get(0).get("name").equals("Joe Fernandez"));
		assertEquals(true, rows.get(1).get("name").equals("Rey Berin"));
		assertEquals(true, rows.get(2).get("name").equals("Shelby"));
		assertEquals(true, rows.get(0).get("gender").equals("MALE"));
		assertEquals(true, rows.get(1).get("gender").equals("MALE"));
		assertEquals(true, rows.get(2).get("gender").equals("FEMALE"));

		// force an exception - FUBAR is not a key:value pair
		jsonStr = "[{\"name\":\"Joe Fernandez\", \"gender\":\"MALE\"}, "
				+ "[{\"email\": \"joe.fernandez@ttmsolutions.com\"}, "
				+ " \"FUBAR\"]]";
		is = new ByteArrayInputStream(jsonStr.getBytes());
		try {
			rows = parseJson(is);
			fail("Did not get expected exception from parseJson(is)");
		} catch (Exception ignore) {

		}

		// force an exception - the key sets are not identical
		jsonStr = "[{\"name\":\"Joe Fernandez\",\"gender\":\"MALE\"}, "
				+ "[{\"name\":\"Rey Berin\",\"gender\":\"MALE\"},"
				+ " {\"name\":\"Shelby\", \"age\":\"22\"}]]";
		is = new ByteArrayInputStream(jsonStr.getBytes());
		try {
			rows = parseJson(is);
			fail("Did not get expected exception from parseJson(is)");
		} catch (Exception ignore) {

		}
	}

	// Test json parsing util
	@Test
	public void TestC() {
		Map<String, Object> map1 = new HashMap<String, Object>();
		Map<String, Object> map2 = new HashMap<String, Object>();
		Map<String, Object> map3 = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		map1.put("name", "Joe Fernandez");
		map1.put("age", Integer.valueOf("22"));
		map2.put("name", "Rey Berin");
		map2.put("age", Integer.valueOf("21"));
		map3.put("name", "Wilma Flintstone");
		map3.put("age", Integer.valueOf("220"));
		list.add(map1);
		list.add(map2);
		list.add(map3);
		try {
			String jsonStr = Utils.generateJson(list);
			// System.out.println(jsonStr);
			assertEquals(
					true,
					"[{\"age\":22,\"name\":\"Joe Fernandez\"},{\"age\":21,\"name\":\"Rey Berin\"},{\"age\":220,\"name\":\"Wilma Flintstone\"}]"
							.equals(jsonStr));
		} catch (Exception exc) {
			System.out.println("Caught this exception: "
					+ exc.getLocalizedMessage());
			fail("Caught this exception: " + exc.getLocalizedMessage());
		}
	}

	// test Utils.getDeviceNames
	@Test
	public void TestD() {
		String devicesStr = ", dev1,,dev2,  dev3 ,,,";
		List<String> devices = Utils.getAgentNames(devicesStr, true);
		assertEquals(true, devices.size() == 3);
		assertEquals(true, devices.get(0).equals("dev1"));
		devices = Utils.getAgentNames(devicesStr, false);
		assertEquals(true, devices.size() == 0);

		devicesStr = ", !, !  dev1, !dev2,  !  dev3, ";
		devices = Utils.getAgentNames(devicesStr, false);
		assertEquals(true, devices.size() == 3);
		assertEquals(true, devices.get(0).equals("dev1"));
		assertEquals(true, devices.get(1).equals("dev2"));
		assertEquals(true, devices.get(2).equals("dev3"));
		devices = Utils.getAgentNames(devicesStr, true);
		assertEquals(true, devices.size() == 0);

		devicesStr = ", !, !  dev1, dev2,  !  dev3, ";
		devices = Utils.getAgentNames(devicesStr, true);
		assertEquals(true, devices.size() == 1);
		assertEquals(true, devices.get(0).equals("dev2"));
		devices = Utils.getAgentNames(devicesStr, false);
		assertEquals(true, devices.size() == 2);
		assertEquals(true, devices.get(0).equals("dev1"));
		assertEquals(true, devices.get(1).equals("dev3"));

	}

	@Test
	public void TestE() {

		Map<String, String> map = null;
		String[] keys = new String[8];
		String[] values = new String[8];

		keys[0] = "k0";
		values[0] = "value0";
		keys[1] = "k1";
		values[1] = "value 1";
		keys[2] = "k2";
		values[2] = "value 2 ";
		keys[3] = "k3";
		values[3] = " value 3 ";
		keys[4] = "k4";
		values[4] = "my $value #4";
		keys[5] = " k 5 ";
		values[5] = "& my $value #5 @ !";
		keys[6] = "user@example.com";
		values[6] = "1234567 !@#$%^&*";
		keys[7] = "http://www.example.com";
		values[7] = "charlie : hello world";

		try {
			String queryString = "";
			for (int i = 0; i < keys.length; i++) {
				queryString += (encode(keys[i], UTF8_STR) + "=" + encode(
						values[i], UTF8_STR));
				if (i != keys.length - 1) {
					queryString += "&";
				}
			}
			map = Utils.parseUrlEncoded(queryString);
		} catch (Exception exc) {
			fail("Caught this exception: " + exc.getLocalizedMessage());
		}

		assertEquals(keys.length, map.size());

		for (int i = 0; i < keys.length; i++) {
			assertEquals(true, map.get(keys[i]) != null);
			assertEquals(true, map.get(keys[i]).equals(values[i]));
		}
	}

	@Test
	public void TestF() {
		Map<String, Long> tMap = null;
		String s = "select * from table [180]";
		String st = "select * from table";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 180);
			assert (tMap.get(TIME_INTERVAL_MAX) == 0);
			assert (tMap.get(TIME_INTERVAL_STEP) == 0);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = " select * from table[ 180  ]";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 180);
			assert (tMap.get(TIME_INTERVAL_MAX) == 0);
			assert (tMap.get(TIME_INTERVAL_STEP) == 0);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = " select * from table[  180]";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 180);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = " select * from table[180  ]";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 180);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = " select * from table[\n180  ]";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 180);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = " select * from table[\t180  ]";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 180);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = " select * from table \n[\t180  ]";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 180);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = " select * from table \n[\t180  \n]\n";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 180);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = "select * from table where id=`integer:id`[1]";
		st = "select * from table where id=`integer:id`";
		try {
			parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 1);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = " {call foo(`integer:id`)\n}[\t180  \n]\n";
		st = "{call foo(`integer:id`)}";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 180);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = " select * from table[a180  ]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		s = " select * from table[0  ]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		s = " select * from table[-180]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		s = " select * from table[1 80  ]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		s = " select * from table[  ]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		s = " select * from table  ]";
		try {
			tMap = parseTimeInterval(s);
			s = stripTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		s = " select * from table[";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		s = " select * from table ";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}
	}

	@Test
	public void TestG() {

		Map<String, Long> tMap = null;

		String s = "select * from table [80:90:60]";
		String st = "select * from table";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 80);
			assert (tMap.get(TIME_INTERVAL_MAX) == 90);
			assert (tMap.get(TIME_INTERVAL_STEP) == 60);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = "select * from table [ 80 : 90:  60     ]";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 80);
			assert (tMap.get(TIME_INTERVAL_MAX) == 90);
			assert (tMap.get(TIME_INTERVAL_STEP) == 60);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = " {call foo(`integer:id`)\n}[\t8: 10: 10  \n]\n";
		st = "{call foo(`integer:id`)}";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 80);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = "select * from table[ 80 : 90:  60     ]   \n";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 80);
			assert (tMap.get(TIME_INTERVAL_MAX) == 90);
			assert (tMap.get(TIME_INTERVAL_STEP) == 60);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = "select * from table[ 80 : 0:  0     ]   \n";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 80);
			assert (tMap.get(TIME_INTERVAL_MAX) == 0);
			assert (tMap.get(TIME_INTERVAL_STEP) == 0);
			assert (stripTimeInterval(s).equals(st));
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		// Exception expected due to 4 values specified
		// Must specify 1 or 3 values only.
		s = "select * from table [180:90:60:30]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		// exception expected due to negative value
		s = "select * from table [180:-20:0]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		// exception expected due to interval being greater than max
		s = "select * from table [180:20:1]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		// exception expected due to interval step being 0
		s = "select * from table [180:20:0]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		// exception expected due to interval step being > 99
		s = "select * from table [8:20:100]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		// exception expected due to interval max being 0
		s = "select * from table [180:0:10]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		// exception expected due to negative value
		s = "select * from table [180:20:-10]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		// exception expected due to negative value
		s = "select * from table [180:-20:-10]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		// Exception expected due to invalid characters.
		s = "select * from table [8z0:90:60]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		// Exception expected due to only 2 values specified.
		s = "select * from table [180:90]";
		try {
			tMap = parseTimeInterval(s);
			fail("Did not get expected exception from: " + s);
		} catch (Exception ignore) {
		}

		// Verify that non-specified values have 0 values;
		s = "select * from table [123]";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 123);
			assert (tMap.get(TIME_INTERVAL_MAX) == 0);
			assert (tMap.get(TIME_INTERVAL_STEP) == 0);
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = "select * from table [123:0:0]";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 123);
			assert (tMap.get(TIME_INTERVAL_MAX) == 0);
			assert (tMap.get(TIME_INTERVAL_STEP) == 0);
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		s = "select * from table [123:  \t0:   \n0]";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 123);
			assert (tMap.get(TIME_INTERVAL_MAX) == 0);
			assert (tMap.get(TIME_INTERVAL_STEP) == 0);
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}

		// Verify that non-specified values have 0 values;
		s = "select * from table \n[ \t 123 \n]";
		try {
			tMap = parseTimeInterval(s);
			assert (tMap.get(TIME_INTERVAL) == 123);
			assert (tMap.get(TIME_INTERVAL_MAX) == 0);
			assert (tMap.get(TIME_INTERVAL_STEP) == 0);
		} catch (Exception e) {
			fail("Caught this exception: " + e.getLocalizedMessage());
		}
	}

}
