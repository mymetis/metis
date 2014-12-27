package org.metis.pull;

import static org.junit.Assert.*;
import static javax.servlet.http.HttpServletResponse.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.*;
import org.springframework.web.client.*;
import org.springframework.http.MediaType;

import org.metis.ClientTest;

/**
 * The tests in this class are meant to be executed against the 'roomsrdb' RDB.
 * 
 * DO NOT RUN THESE TESTS IN PARALLEL! THEY ARE MEANT TO EXECUTED IN THE GIVEN
 * SEQUENCE!
 * 
 * @author jfernandez
 * 
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RoomsTest extends ClientTest {

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
					getServerurl() + "/rooms?bldg=36", HttpMethod.GET, entity,
					List.class);
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
	// This test will insert a room into the room table. Note how we're havng to
	// use a PUT and not a POST because the rooms table does not auto-generate
	// primary keys.
	public void TestD() {
		// the entity body
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		Map<String, Object> pMap = new HashMap<String, Object>();
		pMap.put("bldg", Integer.valueOf(99));
		pMap.put("room", Integer.valueOf(98));
		pMap.put("capacity", Integer.valueOf(97));
		pMap.put("ohead", "Y");
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
					getServerurl() + "/rooms", HttpMethod.PUT, entity,
					String.class);
			assertEquals(SC_CREATED, result.getStatusCode().value());
			// A PUT does not return a location header!
			assertEquals(true, result.getHeaders().getLocation() == null);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Confirm that there are now 11 rows in the table
	public void TestE() {
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
			// we should get back 11 rows (json objects)
			assertEquals(list.size(), 11);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// This test will delete the room that was just inserted.
	public void TestF() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl() + "/rooms?bldg=99", HttpMethod.DELETE,
					entity, String.class);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			// A DELETE does not return a location header!
			assertEquals(true, result.getHeaders().getLocation() == null);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Confirm that there are now 10 rows in the table
	public void TestG() {
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
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// This test will insert a room into the room table. We're prep'ing for a
	// different type of delete test
	public void TestH() {
		// the entity body
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		Map<String, Object> pMap = new HashMap<String, Object>();
		pMap.put("bldg", Integer.valueOf(97));
		pMap.put("room", Integer.valueOf(96));
		pMap.put("capacity", Integer.valueOf(95));
		pMap.put("ohead", "Y");
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
					getServerurl() + "/rooms", HttpMethod.PUT, entity,
					String.class);
			assertEquals(SC_CREATED, result.getStatusCode().value());
			// A PUT does not return a location header!
			assertEquals(true, result.getHeaders().getLocation() == null);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Confirm that we are now back to 11 rows in the table
	public void TestI() {
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
			// we should get back 11 rows (json objects)
			assertEquals(list.size(), 11);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// This test will delete the room that was just inserted. Note that this
	// delete is not using URI params and is testing Metis' use of the
	// "extra path" info
	public void TestJ() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl() + "/rooms/bldg/97", HttpMethod.DELETE,
					entity, String.class);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Confirm that we're now back to 10 rows in the table
	public void TestK() {
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
