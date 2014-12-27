package org.metis.push;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.runners.MethodSorters;
import org.metis.ClientTest;
import org.metis.utils.Statics;

import static org.metis.utils.Statics.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PingTest extends ClientTest {

	private static WsClient wsClient = null;

	private static List<Map<String, String>> jsonObjects = new ArrayList<Map<String, String>>();
	private static Map<String, String> jsonCommand = new HashMap<String, String>();

	static CountDownLatch latch = null;

	@AfterClass
	/**
	 * Disconnect the socket client when all tests have completed.
	 */
	public static void disconnect() {
		if (wsClient != null && wsClient.getSession() != null
				&& wsClient.getSession().isOpen()) {
			wsClient.disconnect();
		}
	}

	@Test
	/**
	 * Create the base client and ensure it connects with the web socket 
	 * server. 
	 * @throws Exception
	 */
	public void TestA() throws Exception {
		try {
			wsClient = WsClient.connect(getPushUrl() + "/studentnotify");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to connect to server");
		}
	}

	@Test
	/**
	 * Client has not yet subscribed. Send a ping and what should return is 
	 * ws_status = "ok"
	 * 
	 * @throws Exception
	 */
	public void TestB() throws Exception {
		jsonObjects.clear();
		jsonCommand.clear();
		jsonCommand.put(Statics.WS_COMMAND, Statics.WS_PING);
		jsonObjects.add(jsonCommand);
		latch = new CountDownLatch(1);
		wsClient.setLatch(latch);
		try {
			wsClient.sendMessage(jsonObjects);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to send to server");
		}
		boolean result = latch.await(3, TimeUnit.SECONDS);
		assertTrue(result == true);
		// grab the json objects returned from server.
		jsonObjects = wsClient.getJsonObjects();
		assertTrue(jsonObjects != null);
		assertTrue(jsonObjects.size() >= 1);
		jsonCommand = jsonObjects.get(0);
		assertTrue(jsonCommand != null);
		assertTrue(jsonCommand.size() >= 1);
		assertTrue(jsonCommand.get(WS_STATUS) != null);
		assertTrue(jsonCommand.get(WS_STATUS).equalsIgnoreCase(WS_OK));
	}

	@Test
	/*
	 * Subscribe the client
	 */
	public void TestC() throws Exception {
		jsonObjects.clear();
		jsonCommand.clear();
		jsonCommand.put(Statics.WS_COMMAND, Statics.WS_SUBSCRIBE);
		jsonCommand.put("stno", "0");
		jsonObjects.add(jsonCommand);
		try {
			wsClient.sendMessage(jsonObjects);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to send to server");
		}
		// a response is not immediately expected for a subscribe
	}

	@Test
	/**
	 * Send another ping and what should come back is ws_status="subscribed".
	 * Followed by stno = "0"
	 * 
	 * @throws Exception
	 */
	public void TestD() throws Exception {
		jsonObjects.clear();
		jsonCommand.clear();
		jsonCommand.put(Statics.WS_COMMAND, Statics.WS_PING);
		jsonObjects.add(jsonCommand);
		latch = new CountDownLatch(1);
		wsClient.setLatch(latch);
		try {
			wsClient.sendMessage(jsonObjects);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to connect to server");
		}
		boolean result = latch.await(3, TimeUnit.SECONDS);
		assertTrue(result == true);
		// grab the json objects returned from server.
		jsonObjects = wsClient.getJsonObjects();
		assertTrue(jsonObjects != null);
		assertTrue(jsonObjects.size() >= 1);
		jsonCommand = jsonObjects.get(0);
		assertTrue(jsonCommand != null);
		assertTrue(jsonCommand.size() >= 2);
		assertTrue(jsonCommand.get(WS_STATUS) != null);
		assertTrue(jsonCommand.get(WS_STATUS).equalsIgnoreCase(WS_SUBSCRIBED));
		assertTrue(jsonCommand.get("stno") != null);
		assertTrue(jsonCommand.get("stno").equalsIgnoreCase("0"));
	}

	@Test
	/**
	 * Disconnect the client, then reconnect with a new URI 
	 * server. 
	 * @throws Exception
	 */
	public void TestE() throws Exception {
		try {
			wsClient.disconnect();
			wsClient = WsClient.connect(getPushUrl() + "/studentnotify/major/MATH");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to connect to server");
		}
	}

	@Test
	/**
	 * Send another ping and what should come back is ws_status="subscribed".
	 * Followed by major = "MATH"
	 * 
	 * @throws Exception
	 */
	public void TestF() throws Exception {
		jsonObjects.clear();
		jsonCommand.clear();
		jsonCommand.put(Statics.WS_COMMAND, Statics.WS_PING);
		jsonObjects.add(jsonCommand);
		latch = new CountDownLatch(1);
		wsClient.setLatch(latch);
		try {
			wsClient.sendMessage(jsonObjects);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to connect to server");
		}
		boolean result = latch.await(3, TimeUnit.SECONDS);
		assertTrue(result == true);
		// grab the json objects returned from server.
		jsonObjects = wsClient.getJsonObjects();
		assertTrue(jsonObjects != null);
		assertTrue(jsonObjects.size() >= 1);
		jsonCommand = jsonObjects.get(0);
		assertTrue(jsonCommand != null);
		assertTrue(jsonCommand.size() >= 2);
		assertTrue(jsonCommand.get(WS_STATUS) != null);
		assertTrue(jsonCommand.get(WS_STATUS).equalsIgnoreCase(WS_SUBSCRIBED));
		assertTrue(jsonCommand.get("major") != null);
		assertTrue(jsonCommand.get("major").equalsIgnoreCase("MATH"));
	}

	@Test
	/**
	 * Disconnect the client, then reconnect with a new URI 
	 * server. 
	 * @throws Exception
	 */
	public void TestG() throws Exception {
		try {
			wsClient.disconnect();
			wsClient = WsClient.connect(getPushUrl() + "/studentnotify?name=Jake");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to connect to server");
		}
	}

	@Test
	/**
	 * Send another ping and what should come back is ws_status="subscribed".
	 * Followed by name = "Jake"
	 * 
	 * @throws Exception
	 */
	public void TestH() throws Exception {
		jsonObjects.clear();
		jsonCommand.clear();
		jsonCommand.put(Statics.WS_COMMAND, Statics.WS_PING);
		jsonObjects.add(jsonCommand);
		latch = new CountDownLatch(1);
		wsClient.setLatch(latch);
		try {
			wsClient.sendMessage(jsonObjects);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to connect to server");
		}
		boolean result = latch.await(3, TimeUnit.SECONDS);
		assertTrue(result == true);
		// grab the json objects returned from server.
		jsonObjects = wsClient.getJsonObjects();
		assertTrue(jsonObjects != null);
		assertTrue(jsonObjects.size() >= 1);
		jsonCommand = jsonObjects.get(0);
		assertTrue(jsonCommand != null);
		assertTrue(jsonCommand.size() >= 2);
		assertTrue(jsonCommand.get(WS_STATUS) != null);
		assertTrue(jsonCommand.get(WS_STATUS).equalsIgnoreCase(WS_SUBSCRIBED));
		assertTrue(jsonCommand.get("name") != null);
		assertTrue(jsonCommand.get("name").equalsIgnoreCase("Jake"));
	}

	@Test
	/*
	 * Resubscribe the client
	 */
	public void TestI() throws Exception {
		jsonObjects.clear();
		jsonCommand.clear();
		jsonCommand.put(Statics.WS_COMMAND, Statics.WS_SUBSCRIBE);
		jsonCommand.put("stno", "0");
		jsonObjects.add(jsonCommand);
		try {
			wsClient.sendMessage(jsonObjects);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to send to server");
		}
		// a response is not immediately expected for a subscribe
	}

	@Test
	/**
	 * Send another ping and what should come back is ws_status="subscribed".
	 * Followed by stno = "0"
	 * 
	 * @throws Exception
	 */
	public void TestJ() throws Exception {
		jsonObjects.clear();
		jsonCommand.clear();
		jsonCommand.put(Statics.WS_COMMAND, Statics.WS_PING);
		jsonObjects.add(jsonCommand);
		latch = new CountDownLatch(1);
		wsClient.setLatch(latch);
		try {
			wsClient.sendMessage(jsonObjects);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to connect to server");
		}
		boolean result = latch.await(3, TimeUnit.SECONDS);
		assertTrue(result == true);
		// grab the json objects returned from server.
		jsonObjects = wsClient.getJsonObjects();
		assertTrue(jsonObjects != null);
		assertTrue(jsonObjects.size() >= 1);
		jsonCommand = jsonObjects.get(0);
		assertTrue(jsonCommand != null);
		assertTrue(jsonCommand.size() >= 2);
		assertTrue(jsonCommand.get(WS_STATUS) != null);
		assertTrue(jsonCommand.get(WS_STATUS).equalsIgnoreCase(WS_SUBSCRIBED));
		assertTrue(jsonCommand.get("stno") != null);
		assertTrue(jsonCommand.get("stno").equalsIgnoreCase("0"));
	}

	@Test
	/**
	 * Disconnect the client, then reconnect with a new bogus URI 
	 *  
	 * @throws Exception
	 */
	public void TestK() {
		try {
			wsClient.disconnect();
			wsClient = WsClient.connect(getPushUrl() + "/studentnotify?crap=Table");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to connect to server");
		}
	}

	@Test
	/**
	 * Send another ping, which should fail 
	 * 
	 * @throws Exception
	 */
	public void TestL() throws Exception {
		jsonObjects.clear();
		jsonCommand.clear();
		jsonCommand.put(Statics.WS_COMMAND, Statics.WS_PING);
		jsonObjects.add(jsonCommand);
		latch = new CountDownLatch(1);
		wsClient.setLatch(latch);
		try {
			wsClient.sendMessage(jsonObjects);
			//fail("Did not receive exception for bogus connection");
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean result = latch.await(3, TimeUnit.SECONDS);
		assertTrue(result == false);
	}

}
