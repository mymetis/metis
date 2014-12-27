package org.metis.pull;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.metis.Concurrent;
import org.metis.ConcurrentJunitRunner;
import org.springframework.web.client.*;

import org.metis.ClientTest;


/**
 * The test case pulls in different testable items from other test cases and
 * runs them in parallel
 * 
 * @author jfernandez
 * 
 */

// Run all test methods concurrently using the ConcurrentJunitRunner class
@RunWith(ConcurrentJunitRunner.class)
// Specify the thread pool size
@Concurrent(threads = 15)
public class MiscConcurrentTest extends ClientTest {

	@Test
	// The not allowed list in the rooms RDB, does not include 'macosx';
	// therefore, the http request should get back a SC_OK. The RDB
	// should also send back a list of json objects; each object
	// represents a row in the rooms table. The rooms table is found
	// in the gumpu database.
	public void TestA() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/rooms", HttpMethod.GET, entity,
					List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			// we should get back 10 rows (json objects)
			assertEquals(list.size(), 10);
			for (Map<String, Object> map : list) {
				// confirm that each row has 4 columns (keys).
				assertEquals(map.keySet().size(), 4);
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// This test is like TestA; however, it is requesting only those rooms in
	// building 36. So it should only get back 4 rows.
	public void TestB() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/rooms?bldg=36", HttpMethod.GET,
					entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			// we should get back 4 rows (json objects)
			assertEquals(list.size(), 4);
			for (Map<String, Object> map : list) {
				// confirm that each row has 4 columns (keys).
				assertEquals(true, map.keySet().size() == 4);
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// This test is requesting those rooms that are in building 36 or 58. So it
	// should only get back 7 rows.
	public void TestC() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/rooms?bldg1=36&bldg2=58",
					HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			// we should get back 4 rows (json objects)
			assertEquals(list.size(), 7);
			for (Map<String, Object> map : list) {
				// confirm that each row has 4 columns (keys).
				assertEquals(map.keySet().size(), 4);
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// This test will fetch all the student numbers in acending order.
	public void TestD() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/students?stno=0",
					HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			// we should get back 48 rows (json objects)
			assertEquals(list.size(), 48);
			for (Map<String, Object> map : list) {
				// confirm that each row returned has 1 column.
				assertEquals(true, map.keySet().size() == 1);
			}
			// fetch the highest valued student number or id
			Map<String, Object> map = list.get(list.size() - 1);
			Object stno = map.get("stno");
			if (stno == null) {
				stno = map.get("STNO");
			}
			assertEquals(true, stno != null);
			assertEquals(true, stno instanceof Integer);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Even though this test uses a URI that is different than the one used in
	// the previous test, it should get the same result set as the previous test
	public void TestE() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/students/stno/0",
					HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			// we should get back 48 rows (json objects)
			assertEquals(list.size(), 48);
			for (Map<String, Object> map : list) {
				// confirm that each row returned has 1 column.
				assertEquals(true, map.keySet().size() == 1);
			}
			// fetch the highest valued student number or id
			Map<String, Object> map = list.get(list.size() - 1);
			Object stno = map.get("stno");
			if (stno == null) {
				stno = map.get("STNO");
			}
			assertEquals(true, stno != null);
			assertEquals(true, stno instanceof Integer);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// This test requests a list of student numbers for all students majoring in
	// math. We're testing the use of strings as key fields in the target
	// sql.
	public void TestF() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/students/major/MATH",
					HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			// we should get back 7 rows (json objects)
			assertEquals(list.size(), 7);
			for (Map<String, Object> map : list) {
				// confirm that each row returned has 1 column,
				// and that contents of the column is an integer
				assertEquals(true, map.keySet().size() == 1);
				Object stno = map.get("stno");
				if (stno == null) {
					stno = map.get("STNO");
				}
				assertEquals(true, stno != null);
				assertEquals(true, stno instanceof Integer);
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// This test requests a list of students whose names start with 'ma'. Only
	// two students, 'Mario' and 'Mary', should be returned. We're
	// testing the use of functions within a sql statement. In this case,
	// concat() is being used. We're also testing NULL values having been
	// assigned to column values
	public void TestG() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/students/name/Ma",
					HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			// we should get back 2 rows (json objects)
			assertEquals(list.size(), 2);
			for (Map<String, Object> map : list) {
				// confirm that each row returned has 5 columns,
				assertEquals(true, map.keySet().size() == 5);
				Object obj = map.get("SNAME");
				if (obj == null) {
					obj = map.get("sname");
				}
				assertEquals(true, obj != null);
				assertEquals(true, obj instanceof String);
				Object obj2 = map.get("CLASS");
				if (obj2 == null) {
					obj2 = map.get("class");
				}
				// mario does not belong to a class
				if (((String) obj).equalsIgnoreCase("mario")) {
					assertEquals(true, obj2 == null);
				} else {
					assertEquals(true, obj2 instanceof Integer);
					assertEquals(true, ((Integer) obj2).intValue() == 4);
				}
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Unlike the previous tests, this test should get back a SC_UNAUTHORIZED
	// because the 'fooAgent1' agent is not allowed
	public void TestX() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "fooagent1");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl() + "/rooms", HttpMethod.GET, entity,
					String.class);
			fail("Did not get expected exception; return status = "
					+ result.getStatusCode().value());
		} catch (HttpClientErrorException e) {
			assertEquals(true, SC_UNAUTHORIZED == e.getStatusCode().value());
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Like TestC, but set User-Agent to 'fooagent2'
	public void TestY() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "fooagent2");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl() + "/rooms", HttpMethod.GET, entity,
					String.class);
			fail("Did not get expected exception; return status = "
					+ result.getStatusCode().value());
		} catch (HttpClientErrorException e) {
			assertEquals(true, SC_UNAUTHORIZED == e.getStatusCode().value());
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

}
