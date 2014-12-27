/*
 * Copyright 2014 Joe Fernandez 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.metis.utils;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import static com.fasterxml.jackson.core.JsonToken.*;
import static org.metis.utils.Statics.*;

public class Utils {

	public static final Log LOG = LogFactory.getLog(Utils.class);

	private static ObjectMapper jsonObjectMapper = new ObjectMapper();
	private static JsonFactory jsonFactory = new JsonFactory();

	/**
	 * Parse the given JSON object (stream). Returns a List of Maps, where each
	 * Map pertains to a JSON object.
	 * 
	 * A JSON object is an unordered set of key:value pairs. An object begins
	 * with { (left brace) and ends with } (right brace). Each key is followed
	 * by : (colon) and the key:value pairs are separated by , (comma). For
	 * example: <code>
	 * <p>
	 * {  
	 *    "first":   "Joe",
	 *    "last":    "Fernandez", 
	 *    "email":   "joe.fernandez@ttmsolutions.com",
	 *    "age":	  22,
	 *    "gender":   "male",
	 *    "verified": false 
	 * }
	 * </p>
	 * </code>
	 * 
	 * Metis only accepts JSON objects or JSON arrays comprised only of objects
	 * or nested arrays. For updating, each JSON object is viewed as a table row
	 * or entity.
	 * 
	 * @param jsonStream
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, String>> parseJson(InputStream jsonStream)
			throws Exception {
		return parseJson(getStringFromInputStream(jsonStream));
	}

	public static List<Map<String, String>> parseJson(String json)
			throws Exception {

		if (jsonFactory == null) {
			LOG.error("parseJson: ERROR, jsonFactory is null");
			throw new Exception("parseJson: ERROR, jsonFactory is null");
		}

		LOG.trace("parseJson: jsonString = " + json);
		JsonParser jp = jsonFactory.createParser(json);

		List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
		try {
			parseJson(jp, rows);
		} finally {
			jp.close();
		}
		return rows;
	}

	/**
	 * Recursively steps through JSON object and arrays of objects. We support
	 * only a single object or an array of objects, where each object represents
	 * an entity (e.g., a student, a customer, an account, etc.).
	 * 
	 * All objects must have the same identical set of keys.
	 * 
	 * @param jp
	 * @param params
	 * @throws Exception
	 */
	private static void parseJson(JsonParser jp, List<Map<String, String>> rows)
			throws Exception {

		// get the next json token
		JsonToken current = jp.nextToken();

		// base case: return if we've reached end of json stream
		if (current == null) {
			return;
		}

		// all rows must have the identical set of keys!
		Map<String, String> firstRow = null;
		if (!rows.isEmpty()) {
			firstRow = rows.get(0);
		}

		// we only accept objects or arrays of objects
		switch (current) {
		case START_OBJECT:
			HashMap<String, String> row = new HashMap<String, String>();
			while (jp.nextToken() != END_OBJECT) {
				// parser should be on 'key' token
				String key = jp.getCurrentName().toLowerCase();
				// ensure all rows have the identical set of keys!
				if (firstRow != null && firstRow.get(key) == null) {
					String eStr = "parseJson: given list of json objects do "
							+ "not have identical set of keys";
					LOG.error(eStr);
					throw new Exception(eStr);
				}
				// now advance to 'value' token
				jp.nextToken();
				String value = jp.getText();
				row.put(key, value);
			}
			// if row is not null, add it to the rows list
			if (!row.isEmpty()) {
				// ensure all rows have the identical set of keys!
				if (firstRow != null && firstRow.size() != row.size()) {
					String eStr = "parseJson: given list of json objects do "
							+ "not have identical set of keys; number of "
							+ "keys vary";
					LOG.error(eStr);
					throw new Exception(eStr);
				}
				rows.add(row);
			}
			break;
		case START_ARRAY:
		case END_ARRAY:
			break;
		default:
			LOG.error("parseJson: ERROR, json token is neither object nor array");
			throw new Exception(
					"parseJson: ERROR, json token is neither object nor array");
		}

		// go on to the next start-of-array, end-of-array, start-of-object, or
		// end of stream
		parseJson(jp, rows);
	}

	/**
	 * Given a list of Maps<String, Object>, returns the corresponding JSON
	 * objects(s) as a JSON String
	 * 
	 * @param list
	 * @return
	 * @throws JsonProcessingException
	 */
	public static String generateJson(List<Map<String, Object>> list)
			throws JsonProcessingException, Exception {

		if (jsonObjectMapper == null) {
			LOG.error("generateJson: ERROR, jsonObjectMapper is null");
			throw new Exception("generateJson: ERROR, jsonObjectMapper is null");
		}
		return jsonObjectMapper.writeValueAsString(list);
	}

	/**
	 * Given a JSON object and Class, returns an instance of the Class that is
	 * represented by the JSON object
	 * 
	 * @param json
	 * @param c
	 * @return
	 * @throws JsonProcessingException
	 * @throws Exception
	 */
	public static Object parseJsonObject(String json, Class c)
			throws JsonProcessingException, Exception {
		if (jsonObjectMapper == null) {
			LOG.error("generateJson: ERROR, jsonObjectMapper is null");
			throw new Exception("generateJson: ERROR, jsonObjectMapper is null");
		}
		return jsonObjectMapper.readValue(json, c);
	}

	/**
	 * Returns a String representation of the contents of the input stream
	 * 
	 * @param is
	 * @return
	 */
	public static String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Returns URI extra path info in the form of a 2 char array where first
	 * element of the array is a key and the second is the key's value (i.e., a
	 * key:value pair). For example, given '/user/123', returns 'user' as the
	 * key and '123' as the value. Given '/users/123', returns 'user' as the key
	 * and '123' as the value. Note how 'users' is transformed to 'user'.
	 * 
	 * @param pathInfo
	 * @return
	 */
	public static String[] getExtraPathInfo(String pathInfo) {
		if (pathInfo == null || pathInfo.length() == 0) {
			return null;
		}

		String[] tokens = pathInfo.replace(FORWARD_SLASH_CHR, SPACE_CHR).trim()
				.split(SPACE_CHR_STR);

		if (tokens.length < 2) {
			return null;
		}

		String[] rTokens = new String[2];
		rTokens[0] = tokens[tokens.length - 2].toLowerCase();
		rTokens[1] = tokens[tokens.length - 1];

		// transform plural to singular
		if (rTokens[0].endsWith(S_CHR_STR)) {
			rTokens[0] = rTokens[0].substring(0, rTokens[0].length() - 1);
		}

		return rTokens;
	}

	/**
	 * Throws an exception if any character in the given field contains a black
	 * listed character.
	 * 
	 * @param field
	 * @param blackList
	 * @throws IllegalArgumentException
	 */
	public static boolean isOnBlackList(String field, char[] blackList) {
		if (blackList == null || blackList.length == 0 || field == null
				|| field.length() == 0) {
			return false;
		}
		char[] strChar = field.toCharArray();
		for (char bc : blackList) {
			for (char fc : strChar) {
				if (fc == bc) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This method strips out leading '{' and trailing '}' characters. It also
	 * trims the string of leading and trailing spaces.
	 * 
	 * @param callStr
	 * @return
	 */
	public static String stripCall(String callStr)
			throws IllegalArgumentException {

		if (callStr == null || callStr.length() == 0) {
			throw new IllegalArgumentException(
					"invalid SQL statement - callable statement "
							+ "is null or 0 length");
		}

		callStr = callStr.trim();
		if ((callStr.startsWith(LEFT_BRACE_STR) && !callStr
				.endsWith(RIGHT_BRACE_STR))
				|| (!callStr.startsWith(LEFT_BRACE_STR) && callStr
						.endsWith(RIGHT_BRACE_STR))) {
			throw new IllegalArgumentException(
					"invalid SQL statement - a callable statement must have "
							+ "'{' and '}' as leading and trailing chars");
		}
		if (callStr.startsWith(LEFT_BRACE_STR)) {
			callStr = callStr.substring(1);
		}
		if (callStr.endsWith(RIGHT_BRACE_STR)) {
			callStr = callStr.substring(0, callStr.length() - 1);
		}
		return callStr.trim();
	}

	/**
	 * Used for dumping the stack trace
	 * 
	 * @param elements
	 */
	public static void dumpStackTrace(StackTraceElement[] elements) {
		if (elements == null) {
			return;
		}
		int i = 0;
		for (; i < 10; i++) {
			LOG.error("at " + elements[i].toString());
		}
		if (elements.length > i) {
			LOG.error("... " + (elements.length - i) + " more");
		}
	}

	/**
	 * This method is used for extracting the time intervals from a SQL job
	 * statement and returning the values as Long objects in a map.
	 * 
	 * @param inStr
	 * @return
	 * @throws IllegalArgumentException
	 * @throws NumberFormatException
	 */
	public static Map<String, Long> parseTimeInterval(String inStr)
			throws IllegalArgumentException, NumberFormatException {

		if (inStr == null || inStr.length() == 0) {
			throw new IllegalArgumentException("SQL statement is null or empty");
		}
		inStr = inStr.trim();

		if (!inStr.endsWith(RIGHT_BRACKET_STR)) {
			throw new IllegalArgumentException(
					"invalid SQL statement, statement must end with bracketed "
							+ "time interval; e.g., select * from table [180]");
		}

		int leftBrIndex = inStr.lastIndexOf(LEFT_BRACKET_CHR);

		if (leftBrIndex < 0) {
			throw new IllegalArgumentException(
					"invalid SQL statement, statement must end with bracketed "
							+ "time interval; e.g., select * from table [180]");
		}

		// grab the string within the brackets
		String interval = inStr.substring(leftBrIndex + 1, inStr.length() - 1)
				.trim();

		if (interval.isEmpty()) {
			throw new IllegalArgumentException(
					"invalid SQL statement, statement must end with bracketed "
							+ "time interval; e.g., select * from table [180]");
		}

		// parse the remaining string using ":"
		String[] fields = interval.split(COLON_STR);

		// there has to be one or three subfields
		if (fields.length != 1 && fields.length != 3) {
			throw new IllegalArgumentException(
					"invalid SQL statement, incorrect format for "
							+ "time interval( [ interval : max interval : "
							+ "step factor ] )");
		}

		// create the map to return
		Map<String, Long> map = new HashMap<String, Long>();

		Long intervalTime = Long.valueOf(fields[0].trim());

		if (intervalTime <= 0) {
			throw new IllegalArgumentException(
					"invalid SQL statement, incorrect format for "
							+ "time interval. Interval must be > 0");
		}
		map.put(TIME_INTERVAL, intervalTime);

		if (fields.length == 3) {
			Long intervalMax = Long.valueOf(fields[1].trim());
			if (intervalMax < 0) {
				throw new IllegalArgumentException(
						"invalid SQL statement, incorrect format for "
								+ "time interval. Interval max must be >= 0");
			} else if (intervalMax > 0 && intervalMax <= intervalTime) {
				throw new IllegalArgumentException(
						"invalid SQL statement, incorrect format for "
								+ "time interval. Interval max must be > interval");
			}
			map.put(TIME_INTERVAL_MAX, intervalMax);
			Long intervalStep = Long.valueOf(fields[2].trim());
			if (intervalStep < 0) {
				throw new IllegalArgumentException(
						"invalid SQL statement, incorrect format for "
								+ "time interval. Interval step must be >= 0");
			} else if (intervalStep > 99) {
				throw new IllegalArgumentException(
						"invalid SQL statement, incorrect format for "
								+ "time interval. Interval step must be <= 99");
			} else if (intervalStep == 0 && intervalMax != 0
					|| intervalStep != 0 && intervalMax == 0) {
				throw new IllegalArgumentException(
						"invalid SQL statement, incorrect format for "
								+ "time interval. Interval max and Interval step must "
								+ "either both be non zero or both be zero");
			}
			map.put(TIME_INTERVAL_STEP, intervalStep);
		}
		return map;
	}

	/**
	 * This method is used for stripping the time interval field from the given
	 * sql statement. So if this comes in, "select * from student [180]", this
	 * is returned, "select * from student".
	 * 
	 * @param inStr
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static String stripTimeInterval(String inStr)
			throws IllegalArgumentException {

		if (inStr == null || inStr.length() == 0) {
			throw new IllegalArgumentException("SQL statement is null or empty");
		}
		inStr = inStr.trim();

		if (!inStr.endsWith(RIGHT_BRACKET_STR)) {
			throw new IllegalArgumentException(
					"invalid SQL statement, statement must end with bracketed "
							+ "time interval; e.g., select * from table [180]");
		}
		int leftBrIndex = inStr.lastIndexOf(LEFT_BRACKET_CHR);

		if (leftBrIndex < 0) {
			throw new IllegalArgumentException(
					"invalid SQL statement, statement must end with bracketed "
							+ "time interval; e.g., select * from table [180]");
		}
		return inStr.substring(0, leftBrIndex).trim();
	}

	/**
	 * Depending on given boolean 'allowed', returns a list of agents that are
	 * all marked either allowed or not allowed.
	 * 
	 * @param agentNames
	 * @param allowed
	 * @return
	 */
	public static List<String> getAgentNames(String agentNames, boolean allowed) {
		List<String> list = null;
		if (agentNames != null && agentNames.length() > 0) {
			list = new ArrayList<String>();
			String[] tokens = agentNames.trim().split(COMMA_STR);
			for (String token : tokens) {
				token = token.trim();
				if (allowed) {
					// method is being asked to return list of devices that are
					// allowed; i.e., are not preceded with a !
					if (token.length() > 0 && !token.startsWith(BANG_STR)) {
						list.add(token);
					}
				} else {
					// method is being asked to return list of devices that are
					// not allowed; i.e., are preceded with a !
					if (token.startsWith(BANG_STR)) {
						token = token.substring(1).trim();
						if (token.length() > 0) {
							list.add(token);
						}
					}
				}
			}
		}
		return list;
	}

	/**
	 * Given a query string, places the name value pairs in a HashMap
	 * 
	 * @param query
	 * @return
	 */
	public static Map<String, String> getQueryMap(String query) {
		LOG.trace("getQueryMap: entered with this query string = " + query);
		if (query == null || query.isEmpty()) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		String[] params = query.split(Statics.AMPERSAND_STR);
		for (String param : params) {
			String nv[] = param.split("=");
			if (nv.length == 2) {
				map.put(nv[0].trim(), nv[1].trim());
			}
		}
		LOG.trace("getQueryMap: returning this map = " + map.toString());
		return map;
	}

	public static Map<String, String> parseUrlEncoded(InputStream encodedStream)
			throws UnsupportedEncodingException {
		return parseUrlEncoded(getStringFromInputStream(encodedStream));
	}

	public static Map<String, String> parseUrlEncoded(String queryString)
			throws UnsupportedEncodingException {

		if (queryString == null || queryString.length() == 0) {
			return null;
		}

		Map<String, String> map = new HashMap<String, String>();
		for (String pair : queryString.split(AMPERSAND_STR)) {
			int eq = pair.indexOf(EQUALS_STR);
			if (eq < 0) {
				// key with no value
				map.put(URLDecoder.decode(pair, UTF8_STR), "");
			} else {
				// key=value
				String key = URLDecoder.decode(pair.substring(0, eq), UTF8_STR);
				String value = URLDecoder.decode(pair.substring(eq + 1),
						UTF8_STR);
				map.put(key.toLowerCase(), value);
			}
		}
		return map;
	}

	public static String byteArrayToHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}

	public static String getHashOf(String s) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		s += "TTM";
		md.update(s.getBytes());
		return byteArrayToHexString(md.digest());
	}


	
}
