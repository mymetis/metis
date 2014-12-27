package org.metis.pull;

import static org.junit.Assert.*;
import static javax.servlet.http.HttpServletResponse.*;

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
 * The tests in this class are meant to be executed against the 'spacesrdb' RDB.
 * 
 * DO NOT RUN THESE TESTS IN PARALLEL! THEY ARE MEANT TO BE EXECUTED IN THE
 * GIVEN SEQUENCE!
 * 
 * @author jfernandez
 * 
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpacesTest extends ClientTest {

	private static Integer rowID = null;

	@Test
	// This test will first insert a new row in the 'pspace' table. The table
	// represents a parking space at Gumpu. The test then expects to
	// get back the primary key for that newly inserted parking space. It will
	// then use that key to retrieve the newly inserted parking space.
	public void TestA() {
		// 1. insert the space
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			// NOTE: we must issue a POST and not a PUT, because a PUT is used
			// by clients when they know the primary key and thus furnish it.
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl() + "/spaces?lot=10",
					HttpMethod.POST, entity, String.class);
			// 2. confirm the insertion was successful
			// note that SC_CREATE is the expected http response code
			assertEquals(SC_CREATED, result.getStatusCode().value());
			// 3. confirm that metis returned a location header. The
			// location header will contain the primary key of the newly
			// inserted row
			assertEquals(true, result.getHeaders().getLocation() != null);
			// 4. extract the primary key from the location path and confirm it
			// is a numeric
			String[] tokens = result.getHeaders().getLocation().getPath()
					.trim().split("/");
			try {
				rowID = Integer.valueOf(tokens[tokens.length - 1]);
			} catch (NumberFormatException e) {
				fail("ERROR, did not get a numeric id, got this instead: "
						+ tokens[tokens.length - 1]);
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Fetch the just inserted row and validate its contents
	public void TestB() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/spaces?id=" + rowID.toString(),
					HttpMethod.GET, entity, List.class);

			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			// we should get back 1 row
			assertEquals(list.size(), 1);
			for (Map<String, Object> map : list) {
				// confirm that the row has 2 columns (keys).
				assertEquals(map.keySet().size(), 2);
				// confirm the names of the returned columns
				// @formatter:off
				for (String key : map.keySet()) {
					assertEquals(true, key.equalsIgnoreCase("id")
								 || key.equalsIgnoreCase("lot"));
				}
				// @formatter:on
				Object obj = map.get("ID");
				if (obj == null) {
					obj = map.get("id");
				}
				assertEquals(true, obj != null);
				assertEquals(true, obj instanceof Integer);
				Integer rID = (Integer) obj;
				obj = map.get("LOT");
				if (obj == null) {
					obj = map.get("lot");
				}
				assertEquals(true, obj != null);
				assertEquals(true, obj instanceof Integer);
				rID = (Integer) obj;
				assertEquals(rID.intValue(), 10);
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Now partially update the row that was just inserted
	public void TestC() {
		// the entity body
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		Map<String, Object> pMap = new HashMap<String, Object>();
		pMap.put("lot", Integer.valueOf(33));
		pMap.put("id", rowID);
		mapList.add(pMap);
		HttpHeaders headers = new HttpHeaders();
		// specify the content type for the entity body
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("User-Agent", "macosx");
		HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<List<Map<String, Object>>>(
				mapList, headers);
		try {
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl() + "/spaces", HttpMethod.POST,
					entity, String.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			// An update POST does not return a location header!
			assertEquals(true, result.getHeaders().getLocation() == null);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Fetch the just inserted row and validate its contents
	public void TestD() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/spaces?id=" + rowID.toString()
							+ "&lot=33", HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			// we should get back 1 row
			assertEquals(list.size(), 1);
			for (Map<String, Object> map : list) {
				// confirm that the row has 2 columns (keys).
				assertEquals(map.keySet().size(), 2);
				// confirm the names of the returned columns
				// @formatter:off
					for (String key : map.keySet()) {
						assertEquals(true, key.equalsIgnoreCase("id")
									 || key.equalsIgnoreCase("lot"));
					}
					// @formatter:on
				Object obj = map.get("ID");
				if (obj == null) {
					obj = map.get("id");
				}
				assertEquals(true, obj != null);
				assertEquals(true, obj instanceof Integer);
				Integer rID = (Integer) obj;
				assertEquals(rID, rowID);
				obj = map.get("LOT");
				if (obj == null) {
					obj = map.get("lot");
				}
				assertEquals(true, obj != null);
				assertEquals(true, obj instanceof Integer);
				rID = (Integer) obj;
				assertEquals(rID.intValue(), 33);
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

}
