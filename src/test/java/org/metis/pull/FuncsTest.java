package org.metis.pull;

import static org.junit.Assert.*;
import static javax.servlet.http.HttpServletResponse.*;
import java.util.List;
import java.util.Map;
import org.springframework.http.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.metis.ClientTest;

/**
 * This test class covers stored functions and procedures. It requires the
 * 'func' RDB found in test-servlet.xml.
 * 
 * @author jfernandez
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FuncsTest extends ClientTest {

	/**
	 * This test will invoke the following SQL, which maps to the 'hello'
	 * function
	 * 
	 * `char:ostring` = call hello(`char:name:in`)
	 * 
	 */
	@Test
	public void TestA() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/funcs?name=Joe",
					HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			// we should get back 1 rows (json objects)
			assertEquals(list.size(), 1);
			Map<String, Object> map = list.get(0);
			assertEquals(true, map.get("ostring") != null);
			assertEquals(true, map.get("ostring") instanceof String);
			assertEquals(true,
					map.get("ostring").toString().equals("Hello, Joe!"));
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	/**
	 * This test invokes the following SQL, which maps to the 'getmajors' stored
	 * procedure.
	 * 
	 * call getmajors(`char:major`)
	 * 
	 * In this case, the major is set to 'math', which returns all the students
	 * majoring in math.
	 */
	@Test
	public void TestB() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/funcs?major=MATH",
					HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			// we should get back 1 rows (json objects)
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

	/**
	 * This test invokes the following SQL, which maps to the 'getmajors2'
	 * stored procedure.
	 * 
	 * call getmajors2(`char:major`,`integer:class`)
	 * 
	 * In this case, the major is set to 'math' and class is set to '4', which
	 * returns all the students in their fourth year who are majoring in math.
	 */
	@Test
	public void TestC() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/funcs?major=MATH&class=4",
					HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			if (isPostgreSQL()) {
				assertEquals(list.size(), 1);
			} else {
				assertEquals(list.size(), 17);
			}
			Object obj = null;
			for (Map<String, Object> map : list) {
				// confirm that each row returned has 1, 2 or 3 columns,
				// and the types of the columns
				assertEquals(true, map.keySet().size() >= 1
						&& map.keySet().size() <= 3);
				if (map.keySet().size() == 1) {
					obj = map.get("stno");
					if (obj == null) {
						obj = map.get("STNO");
					}
					assertEquals(true, obj != null);
					assertEquals(true, obj instanceof Integer);
				} else if (map.keySet().size() == 2) {
					obj = map.get("sname");
					if (obj == null) {
						obj = map.get("SNAME");
					}
					assertEquals(true, obj != null);
					assertEquals(true, obj instanceof String);
					obj = map.get("class");
					if (obj == null) {
						obj = map.get("CLASS");
					}
					assertEquals(true, obj != null);
					assertEquals(true, obj instanceof Integer);
				} else {
					obj = map.get("stno");
					if (obj == null) {
						obj = map.get("STNO");
					}
					assertEquals(true, obj != null);
					assertEquals(true, obj instanceof Integer);
					obj = map.get("sname");
					if (obj == null) {
						obj = map.get("SNAME");
					}
					assertEquals(true, obj != null);
					assertEquals(true, obj instanceof String);
					obj = map.get("class");
					if (obj == null) {
						obj = map.get("CLASS");
					}
					assertEquals(true, obj != null);
					assertEquals(true, obj instanceof Integer);
				}
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	/**
	 * This test covers an OUT param and a result set. It should only get back
	 * one row with one param called count and two additional rows whose params
	 * reflect all the columns of the student table. The count param should be
	 * set to 2 and reflects the number of rows returned from the student table.
	 */
	@Test
	public void TestD() {
		if (isPostgreSQL()) {
			return;
		}
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/funcs?major=MATH&name=M",
					HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			assertEquals(list.size(), 3);
			Object obj = null;
			for (Map<String, Object> map : list) {
				if (map.size() == 1) {
					obj = map.get("count");
					if (obj == null) {
						obj = map.get("COUNT");
					}
					assertEquals(true, obj != null);
					assertEquals(true, obj instanceof Integer);
					assertEquals(2, Integer.parseInt(obj.toString()));
				} else if (map.size() == 5) {
					// System.out.println(map.toString());
					assertEquals(true, map.get("STNO") != null);
					assertEquals(true, map.get("SNAME") != null);
					assertEquals(true, map.get("MAJOR") != null);
					// assertEquals(true, map.get("CLASS") != null);
					assertEquals(true, map.get("BDATE") != null);
				} else {
					fail("ERROR, Invalid number of columns received, got this: "
							+ map.size());
				}
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

}
