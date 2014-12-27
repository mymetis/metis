package org.metis;

import static javax.servlet.http.HttpServletResponse.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;
import java.io.InputStream;

// Base class for HTTP client tests
public abstract class ClientTest {

	static private HttpHeaders httpHdr = new HttpHeaders();;
	static private RestTemplate restTemplate = new BaseRestTemplate();
	static private String serverurl;
	static private String pushUrl;
	static private String dbmsname;
	static private String resourceurl;

	public static final String ORACLE = "oracle";
	public static final String MYSQL = "mysql";
	public static final String SQLSERVER = "sqlserver";
	public static final String POSTGRESQL = "postgresql";

	@BeforeClass
	public static void initialize() throws Exception {
		// Note that resourceurl has '/' pre-appended
		setResourceurl(System.getProperty("resourceurl"));

		// Note that resourceurl has '/' pre-appended
		setDbmsname(System.getProperty("dbms"));

		setPushUrl(System.getProperty("pushurl"));

		// check for a properties file, which takes precedence over -D
		// properties
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = ClientTest.class.getClassLoader().getResourceAsStream(
					"test.properties");
			if (input != null) {
				// load a properties file from class path
				prop.load(input);
				// get the property values
				setDbmsname(prop.getProperty("dbms"));
				setServerurl(prop.getProperty("serverurl"));
				setPushUrl(prop.getProperty("pushurl"));
				setResourceurl(prop.getProperty("resourceurl"));
			}
		} catch (Exception ignore) {
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception ignore) {

				}
			}
		}
		// Maven command to override property:
		// "mvn -Dtest=<test class> -Dserverurl=<url value> test"
		// E.g.
		// "mvn -Dtest=HttpClientTest -Dserverurl=http://localhost:8080 -Dresourceurl=/wds/test/rooms test"
		setServerurl(System.getProperty("serverurl"));


		// Note that serverurl does not have a '/' as the last character
		if (serverurl == null) {
			serverurl = "http://localhost:8080";
		}

		// Note that resourceurl has '/' pre-appended
		if (resourceurl == null) {
			resourceurl = "/wds/test/rooms";
		}

		if (pushUrl == null) {
			pushUrl = "ws://localhost:8080";
		}

	}

	// Method to perform HTTP Delete based on URI passed in and "make" which is
	// used
	// to verify that the data does not exist in the car table.
	public static boolean deleteVerify(String uri, Map<String, Object> verifyMap) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		try {
			ResponseEntity<String> result = restTemplate.exchange(
					getServerurl() + uri, HttpMethod.DELETE, entity,
					String.class);

			if (SC_NO_CONTENT != result.getStatusCode().value())
				return (false);

		} catch (Exception e) {
			fail("ERROR, deleteVerify() failed exchange: " + e.getMessage());
			return (false);
		}

		if (verifyMap != null) {
			if (!mapInResults(uri, verifyMap))
				return (false);
		}
		return (true);
	}

	// Method intended to execute SQL insert statement as defined in the
	// test-servlet.xml and as matched based
	// on the uri passed in. The insert is verified via verifyMap.
	// Returns: False if verifying verifyMap fails. True if the verification
	// succeeds.
	// Exceptions: Exception thrown forward for exchange() fails on a PUT and
	// parameters are specified in the
	// HTTP entity body.
	@SuppressWarnings("unused")
	public static boolean putPostVerify(HttpEntity<?> entity, String uri,
			HttpMethod verb, Map<String, Object> verifyMap) throws Exception {
		URI uriLoc = null;
		Integer rowID;
		ResponseEntity<String> result = null;

		try {
			result = getRestTemplate().exchange(getServerurl() + uri, verb,
					entity, String.class);
			if (SC_CREATED != result.getStatusCode().value())
				return (false);
		} catch (Exception e) {
			throw (e);
		}

		try {
			// For POST, the location is return regardless if primary key is
			// auto-generated or supplied
			// For PUT, no location should be returned.
			uriLoc = result.getHeaders().getLocation();
		} catch (Exception e) {
			fail("ERROR, putPostVerify() failed getLocation() "
					+ e.getMessage());
		}

		if (verb == HttpMethod.POST) {
			if (uriLoc == null) {
				Exception e = new Exception("No URI location returned.");
				throw (e);
			} else {
				String[] tokens = result.getHeaders().getLocation().getPath()
						.trim().split("/");

				try {
					rowID = Integer.valueOf(tokens[tokens.length - 1]);
				} catch (NumberFormatException e) {
					fail("ERROR, putPostVerify(): NumberFormatException with getLocation(): "
							+ result.getHeaders().getLocation().getPath());
				}
			}
		} else {
			assertEquals(true, uriLoc == null);
		}

		if (verifyMap != null) {
			if (!mapInResults(uri, verifyMap))
				return (false);
		}
		return (true);
	}

	// Method to check if the map passed in is in the list of results returned
	// from the uri REST API.
	// If the map is in the results, then true is returned, otherwise, false is
	// returned. Resource data
	// bean needs to have a generic SQLGet statement defined. It is this
	// statement that is used to
	// retrieve data to compare
	//
	// Note: It is possible that the verification map is only specified partial
	// contents of a row of data.
	// Code is written this way to accommodate unknown auto-increment primary
	// key values.
	//
	// Returns: False if the verifyMap values are not found in the returned
	// results. True if all values
	// in verifyMap were found.
	public static boolean mapInResults(String uri, Map<String, Object> verifyMap) {
		Integer equalCnt;
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		// Remove any trailing parameters specified in the URI because we just
		// want to perform a GET on the basename of the URI.
		Integer index = uri.indexOf('?');

		if (index != -1) {
			uri = uri.substring(0, index);
		}

		// System.out.println("dumping verifyMap: " + verifyMap.toString());

		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + uri, HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());

			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();

			if (list == null || list.size() == 0) {
				return (false);
			}

			for (Map<String, Object> resultMap : list) {
				// resultMap can't have less key/value pairs as
				// the verification map.

				// I commented this out because the result map is coming back
				// with one more column than the verify map. That extra column
				// is the ID
				// if (resultMap.size() < verifyMap.size()) {
				// return (false);
				// }

				// System.out.println("dumping resultMap: " +
				// resultMap.toString());

				// Now we need to ensure that every
				// key in the verifyMap has a corresponding
				// key in the resultMap

				// iterate through all the keys in the verifyMap
				boolean match = false;
				for (String key : verifyMap.keySet()) {
					// get the object from the verify map, which we know has to
					// exist
					Object vObj = verifyMap.get(key);
					// now using the same key, attempt to the get
					// an object from the resultMap
					Object rObj = resultMap.get(key);

					if (rObj == null) {
						rObj = resultMap.get(key.toUpperCase());
					}

					if (rObj != null) {
						/*
						 * System.out.println("*** key: " + key +
						 * ", rObj class name: " + rObj.getClass().getName());
						 * System.out.println("\t\t\tvObj class name: " +
						 * vObj.getClass().getName());
						 * 
						 * if (vObj instanceof Integer){
						 * System.out.println("\t\tvObj instanceof == Integer: "
						 * + (Integer)vObj); } else if (vObj instanceof Float){
						 * System.out.println("\t\tvObj instanceof == Float: " +
						 * (Float)vObj); } else if (vObj instanceof Double){
						 * System.out.println("\t\tvObj instanceof == Double: "
						 * + (Double)vObj); } else if (vObj instanceof Long){
						 * System.out.println("\t\tvObj instanceof == Long: " +
						 * (Long)vObj); } else if (vObj instanceof BigInteger){
						 * System
						 * .out.println("\t\tvObj instanceof == BigInteger: " +
						 * (BigInteger)vObj); } else if (vObj instanceof
						 * String){
						 * System.out.println("\t\tvObj instanceof == String: "
						 * + (String)vObj); } else if (vObj instanceof Boolean){
						 * Boolean y=(Boolean)vObj;
						 * System.out.println("\t\tvObj instanceof == Boolean: "
						 * + y); }
						 * 
						 * if (rObj instanceof Integer){
						 * System.out.println("\t\trObj instanceof == Integer: "
						 * + (Integer)rObj); } else if (rObj instanceof Float){
						 * System.out.println("\t\trObj instanceof == Float: " +
						 * (Float)rObj); } else if (rObj instanceof Double){
						 * System.out.println("\t\trObj instanceof == Double: "
						 * + (Double)rObj); } else if (rObj instanceof Long){
						 * System.out.println("\t\trObj instanceof == Long: " +
						 * (Long)rObj); } else if (rObj instanceof BigInteger){
						 * System
						 * .out.println("\t\trObj instanceof == BigInteger: " +
						 * (BigInteger)rObj); } else if (rObj instanceof
						 * String){
						 * System.out.println("\t\trObj instanceof == String: "
						 * + (String)rObj); } else if (rObj instanceof Boolean){
						 * System.out.println("\t\trObj instanceof == Boolean: "
						 * + (Boolean)rObj); }
						 */

						try {
							if (rObj.equals(vObj)) {
								match = true;
							} else {
								match = false;
							}
						} catch (Exception e) {
							System.out.println("Exception: mapInResults(): "
									+ e.getMessage());
							System.out.println("*** key: " + key
									+ ", rObj class name: "
									+ rObj.getClass().getName());
							System.out.println("\t\t\tvObj class name: "
									+ vObj.getClass().getName());
						}
					}

				}

				// System.out.println("*** match = " + match);

				if (match) {
					return true;
				}
				// there was no match, so maybe the next item (if any) in the
				// list will produce a match
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR, mapInResults():  " + e.toString());
		}
		return false;
	}

	public static void dumpGetResults(String uri) {
		Integer equalCnt;
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		try {
			ResponseEntity<List> result = getRestTemplate().exchange(
					getServerurl() + uri, HttpMethod.GET, entity, List.class);
			assertEquals(SC_OK, result.getStatusCode().value());

			List<Map<String, Object>> list = (List<Map<String, Object>>) result
					.getBody();

			if (list == null || list.size() == 0) {
				System.out
						.println("*** dumpGetResults(): no result set returned.");
			}

			for (Map<String, Object> resultMap : list) {
				Iterator it = resultMap.entrySet().iterator();

				System.out.println("\nresult map:");

				while (it.hasNext()) {
					Map.Entry pairs = (Map.Entry) it.next();
					Object obj = pairs.getValue();

					System.out.println("\t" + pairs.getKey() + ", "
							+ pairs.getValue() + ", "
							+ obj.getClass().getName());
					it.remove();
				}
				System.out.println("");
			}
		} catch (Exception e) {
			System.out.println("*** Exception - dumpGetResults(): "
					+ e.getMessage());
		}
	}

	public static HttpHeaders getHdr() {
		return httpHdr;
	}

	public static void setHdr(HttpHeaders hdr) {
		ClientTest.httpHdr = hdr;
	}

	public static RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public static void setRestTemplate(RestTemplate restTemplate) {
		ClientTest.restTemplate = restTemplate;
	}

	public static String getServerurl() {
		return serverurl;
	}

	public static void setServerurl(String serverurl) {
		if (serverurl != null) {
			ClientTest.serverurl = serverurl;
		}
	}

	public static String getPushUrl() {
		return pushUrl;
	}

	public static void setPushUrl(String url) {
		if (url != null) {
			pushUrl = url;
		}
	}

	public static String getResourceurl() {
		return resourceurl;
	}

	public static void setResourceurl(String resourceurl) {
		if (resourceurl != null) {
			ClientTest.resourceurl = resourceurl;
		}
	}

	public static void main(String[] args) throws Exception {
		initialize();
		System.out.println(getDbmsname().toLowerCase().indexOf(ORACLE));
		System.out.println("oracle     = " + isOracle());
		System.out.println("mysql      = " + isMySql());
		System.out.println("sql server = " + isSQLServer());
		System.out.println("postgresql = " + isPostgreSQL());
		System.out.println("pushUrl    = " + getPushUrl());
	}

	public static String getDbmsname() {
		return dbmsname;
	}

	public static void setDbmsname(String dbmsname) {
		if (dbmsname != null) {
			ClientTest.dbmsname = dbmsname;
		}
	}

	public static final boolean isOracle() {
		return (getDbmsname() == null) ? false : getDbmsname().toLowerCase()
				.indexOf(ORACLE) >= 0;
	}

	public static final boolean isMySql() {
		return (getDbmsname() == null) ? false : getDbmsname().toLowerCase()
				.indexOf(MYSQL) >= 0;
	}

	public static final boolean isSQLServer() {
		return (getDbmsname() == null) ? false : getDbmsname().toLowerCase()
				.indexOf(SQLSERVER) >= 0;
	}

	public static final boolean isPostgreSQL() {
		return (getDbmsname() == null) ? false : getDbmsname().toLowerCase()
				.indexOf(POSTGRESQL) >= 0;

	}
}
