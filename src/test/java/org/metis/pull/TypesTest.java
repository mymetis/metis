package org.metis.pull;

import static org.junit.Assert.*;
import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.math.BigDecimal;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.*;

import org.metis.ClientTest;

/**
 * DO NOT RUN THESE TESTS IN PARALLEL! THEY ARE MEANT TO BE EXECUTED IN THE
 * GIVEN SEQUENCE!
 * 
 * @author jfernandez
 * 
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TypesTest extends ClientTest {

	private static Integer primaryKey = null;
	private static Random random = new Random();
	private static Integer aTinyInt = null;
	private static Integer aSmallInt = null;
	private static Integer aRegularInt = null;
	private static Long aBigInt = null;
	private static Double aDouble = null;
	private static Double aFloat = null;
	private static Float aReal = null;
	private static Boolean aBit = null;
	private static Boolean aBoolean = null;
	private static BigDecimal aNumeric = null;
	private static BigDecimal aDecimal = null;
	private static Time aTime = null;
	private static Date aDate = null;
	private static String aTimeStamp = null;

	@Test
	// Insert a row into the signedInt table
	public void TestA() {
		aTinyInt = Integer.valueOf(random.nextInt(100));
		aSmallInt = Integer.valueOf(random.nextInt(1000));
		aRegularInt = Integer.valueOf(random.nextInt(1000));
		aBigInt = Long.valueOf(random.nextLong());
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		Map<String, Object> pMap = new HashMap<String, Object>();
		pMap.put("aTinyInt", aTinyInt);
		pMap.put("aSmallInt", aSmallInt);
		pMap.put("aInteger", aRegularInt);
		pMap.put("aBigInt", aBigInt);
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
					getServerurl() + "/clientsignedint",
					HttpMethod.PUT, entity, String.class);
			assertEquals(SC_CREATED, result.getStatusCode().value());
			// A PUT does not return a location header!
			assertEquals(true, result.getHeaders().getLocation() == null);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Fetch the row that was just inserted and confirm all the columns and
	// their expected values
	public void TestB() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/clientsignedint?aTinyInt="
							+ aTinyInt.toString(), HttpMethod.GET, entity,
					List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getDate() > 0);
			assertEquals(true, result.getHeaders().containsKey("Server"));
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			// we should get back 48 rows (json objects)
			assertEquals(list.size(), 1);
			for (Map<String, Object> map : list) {
				// confirm that each row returned has 4 columns.
				assertEquals(true, map.keySet().size() == 4);
			}
			// fetch the one and only row
			Map<String, Object> map = list.get(0);
			Object obj = map.get("atinyint");
			if (obj == null) {
				obj = map.get("ATINYINT");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof Integer);
			assertEquals(aTinyInt.intValue(), ((Integer) obj).intValue());
			obj = map.get("asmallint");
			if (obj == null) {
				obj = map.get("ASMALLINT");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof Integer);
			assertEquals(aSmallInt.intValue(), ((Integer) obj).intValue());
			obj = map.get("ainteger");
			if (obj == null) {
				obj = map.get("AINTEGER");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof Integer);
			assertEquals(aRegularInt.intValue(), ((Integer) obj).intValue());
			obj = map.get("abigint");
			if (obj == null) {
				obj = map.get("ABIGINT");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof Long);
			assertEquals(aBigInt.intValue(), ((Long) obj).intValue());

		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Delete the row that was just inserted
	public void TestC() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl() + "/clientsignedint?aTinyInt="
							+ aTinyInt.toString(), HttpMethod.DELETE, entity,
					String.class);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			// A DELETE does not return a location header!
			assertEquals(true, result.getHeaders().getLocation() == null);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getLocation() == null);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Ensure row is no longer there
	public void TestD() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/clientsignedint?aTinyInt="
							+ aTinyInt.toString(), HttpMethod.GET, entity,
					List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getDate() > 0);
			assertEquals(true, result.getHeaders().containsKey("Server"));
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			if(list == null || list.isEmpty()) {
				return;
		    } else {
		  
		    	fail("ERROR, signedint table is not empty: " + list.get(0).toString()); 	
		    }
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Insert a row into the floatTest table. The table has an auto-
	// incrementing primary key so we need to fetch the returned key.
	public void TestE() {
		aDouble = Double.valueOf(random.nextDouble());
		if (isPostgreSQL()) {
			aFloat = Double.valueOf(random.nextFloat());
		} else {
			aFloat = Double.valueOf("0.5991");
		}
		aReal = Float.valueOf(random.nextFloat());
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		Map<String, Object> pMap = new HashMap<String, Object>();
		pMap.put("aDouble", aDouble);
		pMap.put("aFloat", aFloat);
		pMap.put("aReal", aReal);
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
					getServerurl() + "/clientfloat", HttpMethod.POST,
					entity, String.class);
			// 1. confirm the insertion was successful
			// note that SC_CREATE is the expected http response code
			assertEquals(SC_CREATED, result.getStatusCode().value());
			// 2. confirm that metis returned a location header. The
			// location header will contain the primary key of the newly
			// inserted row
			assertEquals(true, result.getHeaders().getLocation() != null);
			// 3. extract the primary key from the location path and confirm it
			// is a numeric
			String[] tokens = result.getHeaders().getLocation().getPath()
					.trim().split("/");
			try {
				primaryKey = Integer.valueOf(tokens[tokens.length - 1]);
			} catch (NumberFormatException e) {
				fail("ERROR, did not get a numeric id, got this instead: "
						+ tokens[tokens.length - 1]);
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@SuppressWarnings({ "deprecation", "null" })
	@Test
	// Fetch the row that was just inserted and confirm all the columns and
	// their expected values
	public void TestF() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/clientfloat?id="
							+ primaryKey.toString(), HttpMethod.GET, entity,
					List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getDate() > 0);
			assertEquals(true, result.getHeaders().containsKey("Server"));
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			assertEquals(list.size(), 1);
			for (Map<String, Object> map : list) {
				// confirm that each row returned has 4 columns.
				assertEquals(true, map.keySet().size() == 4);
			}
			// fetch the one and only row
			Map<String, Object> map = list.get(0);
			Object obj = map.get("adouble");
			if (obj == null) {
				obj = map.get("ADOUBLE");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof Double);
			assertEquals(true,
					aDouble.doubleValue() == ((Double) obj).doubleValue());
			obj = map.get("afloat");
			if (obj == null) {
				obj = map.get("AFLOAT");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof Double);
			assertEquals(true,
					aFloat.doubleValue() == ((Double) obj).doubleValue());
			obj = map.get("areal");
			if (obj == null) {
				obj = map.get("AREAL");
			}
			assertEquals(true, obj != null);
			// System.out.println(obj.getClass().getName());
			assertEquals(true, obj instanceof Double);
			assertEquals(true,
					aReal.floatValue() == ((Double) obj).floatValue());
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Delete the row that was just inserted
	public void TestG() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl() + "/clientfloat?id="
							+ primaryKey.toString(), HttpMethod.DELETE, entity,
					String.class);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			// A DELETE does not return a location header!
			assertEquals(true, result.getHeaders().getLocation() == null);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getLocation() == null);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Ensure row is no longer there
	public void TestH() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/clientfloat?id="
							+ primaryKey.toString(), HttpMethod.GET, entity,
					List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getDate() > 0);
			assertEquals(true, result.getHeaders().containsKey("Server"));
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list == null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Insert a row into the bitBoolean table. The bitBoolean table has an auto-
	// incrementing primary key so we need to fetch the returned key.
	public void TestI() {
		// Oracle and SQL Server do not support Boolean types
		if(isOracle() || isSQLServer()){
			return;
		}
		aBit = Boolean.FALSE;
		aBoolean = Boolean.TRUE;
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		Map<String, Object> pMap = new HashMap<String, Object>();
		pMap.put("aBit", aBit);
		pMap.put("aBoolean", aBoolean);
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
					getServerurl() + "/clientbit", HttpMethod.POST,
					entity, String.class);
			// 1. confirm the insertion was successful
			// note that SC_CREATE is the expected http response code
			assertEquals(SC_CREATED, result.getStatusCode().value());
			// 2. confirm that metis returned a location header. The
			// location header will contain the primary key of the newly
			// inserted row
			assertEquals(true, result.getHeaders().getLocation() != null);
			// 3. extract the primary key from the location path and confirm it
			// is a numeric
			String[] tokens = result.getHeaders().getLocation().getPath()
					.trim().split("/");
			try {
				primaryKey = Integer.valueOf(tokens[tokens.length - 1]);
			} catch (NumberFormatException e) {
				fail("ERROR, did not get a numeric id, got this instead: "
						+ tokens[tokens.length - 1]);
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Fetch the row that was just inserted and confirm all the columns and
	// their expected values
	public void TestJ() {
		// Oracle and SQL Server do not support Boolean types
		if(isOracle() || isSQLServer()){
			return;
		}
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/clientbit?id="
							+ primaryKey.toString(), HttpMethod.GET, entity,
					List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getDate() > 0);
			assertEquals(true, result.getHeaders().containsKey("Server"));
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			assertEquals(list.size(), 1);
			for (Map<String, Object> map : list) {
				// confirm that each row returned has 4 columns.
				assertEquals(true, map.keySet().size() == 3);
			}
			// fetch the one and only row
			Map<String, Object> map = list.get(0);
			Object obj = map.get("abit");
			if (obj == null) {
				obj = map.get("ABIT");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof Boolean);
			assertEquals(aBit.booleanValue(), ((Boolean) obj).booleanValue());
			obj = map.get("aboolean");
			if (obj == null) {
				obj = map.get("ABOOLEAN");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof Boolean);
			assertEquals(aBoolean.booleanValue(),
					((Boolean) obj).booleanValue());
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Delete the row that was just inserted
	public void TestK() {
		// Oracle and SQL Server do not support Boolean types
		if(isOracle() || isSQLServer()){
			return;
		}
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl() + "/clientbit?id="
							+ primaryKey.toString(), HttpMethod.DELETE, entity,
					String.class);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			// A DELETE does not return a location header!
			assertEquals(true, result.getHeaders().getLocation() == null);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getLocation() == null);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Ensure row is no longer there
	public void TestL() {
		// Oracle and SQL Server do not support Boolean types
		if(isOracle() || isSQLServer()){
			return;
		}
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/clientbit?id="
							+ primaryKey.toString(), HttpMethod.GET, entity,
					List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getDate() > 0);
			assertEquals(true, result.getHeaders().containsKey("Server"));
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list == null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Insert a row into the numericDecimal table. The table has an auto-
	// incrementing primary key so we need to fetch the returned key.
	public void TestM() {
		aNumeric = new BigDecimal("12345E-2");
		aDecimal = new BigDecimal("678934E-2");
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		Map<String, Object> pMap = new HashMap<String, Object>();
		pMap.put("aNumeric", aNumeric);
		pMap.put("aDecimal", aDecimal);
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
					getServerurl() + "/clientnum", HttpMethod.POST,
					entity, String.class);
			// 1. confirm the insertion was successful
			// note that SC_CREATE is the expected http response code
			assertEquals(SC_CREATED, result.getStatusCode().value());
			// 2. confirm that metis returned a location header. The
			// location header will contain the primary key of the newly
			// inserted row
			assertEquals(true, result.getHeaders().getLocation() != null);
			// 3. extract the primary key from the location path and confirm it
			// is a numeric
			String[] tokens = result.getHeaders().getLocation().getPath()
					.trim().split("/");
			try {
				primaryKey = Integer.valueOf(tokens[tokens.length - 1]);
			} catch (NumberFormatException e) {
				fail("ERROR, did not get a numeric id, got this instead: "
						+ tokens[tokens.length - 1]);
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Fetch the row that was just inserted and confirm all the columns and
	// their expected values
	public void TestN() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/clientnum?id="
							+ primaryKey.toString(), HttpMethod.GET, entity,
					List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getDate() > 0);
			assertEquals(true, result.getHeaders().containsKey("Server"));
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			assertEquals(list.size(), 1);
			for (Map<String, Object> map : list) {
				// confirm that each row returned has 3 columns.
				assertEquals(true, map.keySet().size() == 3);
			}
			// fetch the one and only row
			Map<String, Object> map = list.get(0);
			Object obj = map.get("adecimal");
			if (obj == null) {
				obj = map.get("ADECIMAL");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof Double);
			assertEquals(true,
					aDecimal.doubleValue() == ((Double) obj).doubleValue());
			obj = map.get("anumeric");
			if (obj == null) {
				obj = map.get("ANUMERIC");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof Double);
			assertEquals(true,
					aNumeric.doubleValue() == ((Double) obj).doubleValue());
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Delete the row that was just inserted
	public void TestO() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl() + "/clientnum?id="
							+ primaryKey.toString(), HttpMethod.DELETE, entity,
					String.class);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			// A DELETE does not return a location header!
			assertEquals(true, result.getHeaders().getLocation() == null);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getLocation() == null);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Ensure row is no longer there
	public void TestP() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/clientnum?id="
							+ primaryKey.toString(), HttpMethod.GET, entity,
					List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getDate() > 0);
			assertEquals(true, result.getHeaders().containsKey("Server"));
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list == null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Insert a row into the timeDate table. The table has an auto-
	// incrementing primary key so we need to fetch the returned key.
	public void TestQ() {
		aTime = Time.valueOf("11:22:33");
		aDate = Date.valueOf("1999-08-20");
		aTimeStamp = "2014-03-04 15:34:07";
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		Map<String, Object> pMap = new HashMap<String, Object>();
		pMap.put("aTime", aTime);
		pMap.put("aDate", aDate);
		pMap.put("aTimeStamp", aTimeStamp);
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
					getServerurl() + "/clienttime", HttpMethod.POST,
					entity, String.class);
			// 1. confirm the insertion was successful
			// note that SC_CREATE is the expected http response code
			assertEquals(SC_CREATED, result.getStatusCode().value());
			// 2. confirm that metis returned a location header. The
			// location header will contain the primary key of the newly
			// inserted row
			assertEquals(true, result.getHeaders().getLocation() != null);
			// 3. extract the primary key from the location path and confirm it
			// is a numeric
			String[] tokens = result.getHeaders().getLocation().getPath()
					.trim().split("/");
			try {
				primaryKey = Integer.valueOf(tokens[tokens.length - 1]);
			} catch (NumberFormatException e) {
				fail("ERROR, did not get a numeric id, got this instead: "
						+ tokens[tokens.length - 1]);
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Fetch the row that was just inserted and confirm all the columns and
	// their expected values
	public void TestR() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/clienttime?id="
							+ primaryKey.toString(), HttpMethod.GET, entity,
					List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getDate() > 0);
			assertEquals(true, result.getHeaders().containsKey("Server"));
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list != null);
			assertEquals(list.size(), 1);
			for (Map<String, Object> map : list) {
				// confirm that each row returned has 4 columns.
				assertEquals(true, map.keySet().size() == 4);
			}
			// fetch the one and only row
			Map<String, Object> map = list.get(0);
			Object obj = map.get("atime");
			if (obj == null) {
				obj = map.get("ATIME");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof String);
			assertEquals(true, aTime.equals(Time.valueOf((String) obj)));
			obj = map.get("adate");
			if (obj == null) {
				obj = map.get("ADATE");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof String);
			assertEquals(true, aDate.equals(Date.valueOf((String) obj)));
			obj = map.get("atimestamp");
			if (obj == null) {
				obj = map.get("ATIMESTAMP");
			}
			assertEquals(true, obj != null);
			assertEquals(true, obj instanceof Long);
			String rTime = (new Timestamp(((Long) obj).longValue())).toString();
			if (rTime.indexOf('.') > 0) {
				rTime = rTime.substring(0, rTime.indexOf('.'));
			}
			System.out.println(rTime);
			System.out.println(aTimeStamp);
			assertEquals(true, aTimeStamp.equals(rTime));
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Delete the row that was just inserted
	public void TestS() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<String> result = getRestTemplate().exchange(
					getServerurl() + "/clienttime?id="
							+ primaryKey.toString(), HttpMethod.DELETE, entity,
					String.class);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			// A DELETE does not return a location header!
			assertEquals(true, result.getHeaders().getLocation() == null);
			assertEquals(SC_NO_CONTENT, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getLocation() == null);
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

	@Test
	// Ensure row is no longer there
	public void TestT() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + "/clienttime?id="
							+ primaryKey.toString(), HttpMethod.GET, entity,
					List.class);
			assertEquals(SC_OK, result.getStatusCode().value());
			assertEquals(true, result.getHeaders().getDate() > 0);
			assertEquals(true, result.getHeaders().containsKey("Server"));
			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();
			assertEquals(true, list == null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

}
