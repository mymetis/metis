package org.metis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metis.utils.Utils;

//Run all test methods concurrently using the ConcurrentJunitRunner class
@RunWith(ConcurrentJunitRunner.class)
// Specify the thread pool size
@Concurrent(threads = 10)
public class ConcurrentJsonMapperTest {

	@Test
	public final void TestA() {
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

	@Test
	public final void TestB() {
		Map<String, Object> map1 = new HashMap<String, Object>();
		Map<String, Object> map2 = new HashMap<String, Object>();
		Map<String, Object> map3 = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		map1.put("name", "Joe Fernandez");
		map1.put("age", Integer.valueOf("32"));
		map2.put("name", "Rey Berin");
		map2.put("age", Integer.valueOf("2100"));
		map3.put("name", "Wilma Flintstone");
		map3.put("age", Integer.valueOf("420"));
		list.add(map1);
		list.add(map2);
		list.add(map3);
		try {
			String jsonStr = Utils.generateJson(list);
			assertEquals(
					true,
					"[{\"age\":32,\"name\":\"Joe Fernandez\"},{\"age\":2100,\"name\":\"Rey Berin\"},{\"age\":420,\"name\":\"Wilma Flintstone\"}]"
							.equals(jsonStr));
		} catch (Exception exc) {
			System.out.println("Caught this exception: "
					+ exc.getLocalizedMessage());
			fail("Caught this exception: " + exc.getLocalizedMessage());
		}
	}

	@Test
	public final void TestC() {
		Map<String, Object> map1 = new HashMap<String, Object>();
		Map<String, Object> map2 = new HashMap<String, Object>();
		Map<String, Object> map3 = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		map1.put("name", "Joe Fernandez");
		map1.put("age", Integer.valueOf("42"));
		map2.put("name", "Rey Berin");
		map2.put("age", Integer.valueOf("11"));
		map3.put("name", "Wilma Flintstone");
		map3.put("age", Integer.valueOf("620"));
		list.add(map1);
		list.add(map2);
		list.add(map3);
		try {
			String jsonStr = Utils.generateJson(list);
			assertEquals(
					true,
					"[{\"age\":42,\"name\":\"Joe Fernandez\"},{\"age\":11,\"name\":\"Rey Berin\"},{\"age\":620,\"name\":\"Wilma Flintstone\"}]"
							.equals(jsonStr));
		} catch (Exception exc) {
			System.out.println("Caught this exception: "
					+ exc.getLocalizedMessage());
			fail("Caught this exception: " + exc.getLocalizedMessage());
		}
	}

	@Test
	public final void TestD() {
		Map<String, Object> map1 = new HashMap<String, Object>();
		Map<String, Object> map2 = new HashMap<String, Object>();
		Map<String, Object> map3 = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		map1.put("name", "Joe Fernandez");
		map1.put("age", Integer.valueOf("920"));
		map2.put("name", "Rey Berin");
		map2.put("age", Integer.valueOf("910"));
		map3.put("name", "Wilma Flintstone");
		map3.put("age", Integer.valueOf("922"));
		list.add(map1);
		list.add(map2);
		list.add(map3);
		try {
			String jsonStr = Utils.generateJson(list);
			assertEquals(
					true,
					"[{\"age\":920,\"name\":\"Joe Fernandez\"},{\"age\":910,\"name\":\"Rey Berin\"},{\"age\":922,\"name\":\"Wilma Flintstone\"}]"
							.equals(jsonStr));
		} catch (Exception exc) {
			System.out.println("Caught this exception: "
					+ exc.getLocalizedMessage());
			fail("Caught this exception: " + exc.getLocalizedMessage());
		}
	}

	@Test
	public final void TestE() {
		Map<String, Object> map1 = new HashMap<String, Object>();
		Map<String, Object> map2 = new HashMap<String, Object>();
		Map<String, Object> map3 = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		map1.put("name", "Joe Fernandez");
		map1.put("age", Integer.valueOf("1"));
		map2.put("name", "Rey Berin");
		map2.put("age", Integer.valueOf("2"));
		map3.put("name", "Wilma Flintstone");
		map3.put("age", Integer.valueOf("3"));
		list.add(map1);
		list.add(map2);
		list.add(map3);
		try {
			String jsonStr = Utils.generateJson(list);
			assertEquals(
					true,
					"[{\"age\":1,\"name\":\"Joe Fernandez\"},{\"age\":2,\"name\":\"Rey Berin\"},{\"age\":3,\"name\":\"Wilma Flintstone\"}]"
							.equals(jsonStr));
		} catch (Exception exc) {
			System.out.println("Caught this exception: "
					+ exc.getLocalizedMessage());
			fail("Caught this exception: " + exc.getLocalizedMessage());
		}
	}

	@Test
	public final void TestF() {
		Map<String, Object> map1 = new HashMap<String, Object>();
		Map<String, Object> map2 = new HashMap<String, Object>();
		Map<String, Object> map3 = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		map1.put("name", "Joe Fernandez");
		map1.put("age", Integer.valueOf("1000"));
		map2.put("name", "Rey Berin");
		map2.put("age", Integer.valueOf("2000"));
		map3.put("name", "Wilma Flintstone");
		map3.put("age", Integer.valueOf("3000"));
		list.add(map1);
		list.add(map2);
		list.add(map3);
		try {
			String jsonStr = Utils.generateJson(list);
			assertEquals(
					true,
					"[{\"age\":1000,\"name\":\"Joe Fernandez\"},{\"age\":2000,\"name\":\"Rey Berin\"},{\"age\":3000,\"name\":\"Wilma Flintstone\"}]"
							.equals(jsonStr));
		} catch (Exception exc) {
			System.out.println("Caught this exception: "
					+ exc.getLocalizedMessage());
			fail("Caught this exception: " + exc.getLocalizedMessage());
		}
	}

	@Test
	public final void TestG() {
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
		java.sql.Time time = new java.sql.Time(System.currentTimeMillis());
		java.sql.Timestamp timeStamp = new java.sql.Timestamp(
				System.currentTimeMillis());
		Map<String, Object> map1 = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		map1.put("name", "Joe Fernandez");
		map1.put("age", Integer.valueOf("1000"));
		map1.put("date", date);
		map1.put("time", time);
		map1.put("timestamp", timeStamp);
		list.add(map1);
		try {
			String jsonStr = Utils.generateJson(list);
			// System.out.println(jsonStr);
			assertEquals(true, jsonStr.indexOf(date.toString()) >= 0);
			assertEquals(true, jsonStr.indexOf(time.toString()) >= 0);
			assertEquals(true,
					jsonStr.indexOf(Long.toString(timeStamp.getTime())) >= 0);
		} catch (Exception exc) {
			System.out.println("Caught this exception: "
					+ exc.getLocalizedMessage());
			fail("Caught this exception: " + exc.getLocalizedMessage());
		}
	}

}
