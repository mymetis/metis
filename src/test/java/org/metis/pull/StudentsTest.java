package org.metis.pull;

import static org.junit.Assert.*;
import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.*;

import org.metis.ClientTest;

/**
 * The tests in this class are meant to be executed against the 'studentrdb'
 * RDB.
 * 
 * DO NOT RUN THESE TESTS IN PARALLEL! THEY ARE MEANT TO BE EXECUTED IN THE
 * GIVEN SEQUENCE!
 * 
 * @author jfernandez
 * 
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StudentsTest extends ClientTest {

	private static Integer studentID = null;

	@Test
	// This test will fetch all the student numbers in acending order.
	public void TestA() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/students?stno=0",
					HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getDate() > 0);
			assertEquals(true,result.getHeaders().containsKey("Server"));
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
			if(stno == null) {
				stno = map.get("STNO");
			}
			assertEquals(true, stno != null);
			assertEquals(true, stno instanceof Integer);
			studentID = Integer.valueOf(stno.toString());
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Even though this test uses a URI that is different than the one used in
	// TestA, it should get the same result set as TestA
	public void TestB() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/students/stno/0",
					HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getDate() > 0);
			assertEquals(true,result.getHeaders().containsKey("Server"));
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
			if(stno == null) {
				stno = map.get("STNO");
			}
			assertEquals(true, stno != null);
			assertEquals(true, stno instanceof Integer);
			Integer myStudentID = Integer.valueOf(stno.toString());
			assertEquals(studentID, myStudentID);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// This test inserts 2 students (batch insert) into the student table.
	// Note how we're having to use a PUT and not a POST because the student
	// table does not auto-generate primary keys.
	public void TestD() {
		// the entity body
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		Map<String, Object> pMap = new HashMap<String, Object>();
		pMap.put("stno", Integer.valueOf(studentID.intValue() + 1));
		pMap.put("sname", "Fred");
		pMap.put("major", "MATH");
		pMap.put("class", Integer.valueOf(4));
		pMap.put("bdate", Date.valueOf("1980-08-08"));
		mapList.add(pMap);
		pMap = new HashMap<String, Object>();
		pMap.put("stno", Integer.valueOf(studentID.intValue() + 2));
		pMap.put("sname", "Wilma");
		pMap.put("major", "POLY");
		pMap.put("class", Integer.valueOf(3));
		pMap.put("bdate", Date.valueOf("1981-07-07"));
		mapList.add(pMap);
		HttpHeaders headers = new HttpHeaders();
		// specify the content type for the entity body
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("User-Agent", "macosx");
		// tell metis that we're expecting to get back json
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<List<Map<String, Object>>>(
				mapList, headers);
		try {
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl() + "/students", HttpMethod.PUT,
					entity, String.class);
			assertEquals(SC_CREATED, result.getStatusCode().value());
			// A PUT does not return a location header!
			assertEquals(true, result.getHeaders().getLocation() == null);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Confirm that the previous test worked
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
			// we should now get back 2 additional rows (json objects)
			assertEquals(list.size(), 50);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// This test will delete the 2 students just entered. Note that a DELETE
	// cannot accomodate an entity body; therefore, you cannot execute a batch
	// delete.
	public void TestF() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl()
							+ "/students?stno="
							+ Integer.valueOf(studentID.intValue() + 1)
									.toString(), HttpMethod.DELETE, entity,
					String.class);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			// A DELETE does not return a location header!
			assertEquals(true, result.getHeaders().getLocation() == null);
			result = getRestTemplate().exchange(
					getServerurl()
							+ "/students?stno="
							+ Integer.valueOf(studentID.intValue() + 2)
									.toString(), HttpMethod.DELETE, entity,
					String.class);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getLocation() == null);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Confirm that the previous test worked
	public void TestG() {
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
			// we should now get back 2 additional rows (json objects)
			assertEquals(list.size(), 48);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// This test requests a list of student numbers for all students majoring in
	// math. We're testing the use of strings as key fields in the target
	// sql.
	public void TestH() {
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
				if(stno == null) {
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
	public void TestI() {
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
				Object sname = map.get("SNAME");
				if(sname == null) {
					sname = map.get("sname");
				}
				assertEquals(true, sname != null);
				assertEquals(true, sname instanceof String);
				Object obj = map.get("CLASS");
				if(obj == null) {
					obj = map.get("class");
				}
				if (sname.toString().equalsIgnoreCase("mario")) {
					assertEquals(true, obj == null);
				} else {
					assertEquals(true, obj instanceof Integer);
					assertEquals(true, ((Integer) obj).intValue() == 4);
				}
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

}
