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

/**
 * The objective of this test is to susbribe to a SQL job that, at first, gets
 * back a null result set, then detects a change.
 * 
 * @author jfernandez
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PepeTest extends ClientTest {

	// this test's web socket clients
	private static WsClient wsStudentClient = null;

	private static List<Map<String, String>> jsonObjects = new ArrayList<Map<String, String>>();
	private static Map<String, String> jsonCommand = new HashMap<String, String>();

	// latches used for inter-thread communication
	CountDownLatch wsClientLatch = null;
	CountDownLatch updateLatch = null;

	/**
	 * Disconnect the socket clients when all tests have completed.
	 */
	@AfterClass
	public static void disconnect() {
		if (wsStudentClient != null && wsStudentClient.getSession() != null
				&& wsStudentClient.getSession().isOpen()) {
			wsStudentClient.disconnect();
		}
	}

	/**
	 * Create the base client and ensure it connects to the web socket server.
	 * 
	 * @throws Exception
	 */
	@Test
	public void TestA() throws Exception {
		try {
			wsStudentClient = WsClient.connect(getPushUrl() + "/studentnotify");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to connect student client to server");
		}

	}

	/**
	 * The student client has not yet subscribed. Have it send a ping and what
	 * should return is ws_status = "ok"
	 * 
	 * @throws Exception
	 */
	@Test
	public void TestB() throws Exception {
		jsonObjects.clear();
		jsonCommand.clear();
		jsonCommand.put(Statics.WS_COMMAND, Statics.WS_PING);
		jsonObjects.add(jsonCommand);
		wsClientLatch = new CountDownLatch(1);
		wsStudentClient.setLatch(wsClientLatch);
		try {
			wsStudentClient.sendMessage(jsonObjects);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to send to server");
		}
		boolean result = wsClientLatch.await(3, TimeUnit.SECONDS);
		assertTrue(result == true);
		// grab the json objects returned from server.
		jsonObjects = wsStudentClient.getJsonObjects();
		assertTrue(jsonObjects != null);
		assertTrue(jsonObjects.size() >= 1);
		jsonCommand = jsonObjects.get(0);
		assertTrue(jsonCommand != null);
		assertTrue(jsonCommand.size() >= 1);
		assertTrue(jsonCommand.get(WS_STATUS) != null);
		assertTrue(jsonCommand.get(WS_STATUS).equalsIgnoreCase(WS_OK));
	}

	/*
	 * Subscribe to the student client, and ask it to monitor a student that
	 * does not initially exist
	 */
	@Test
	public void TestC() throws Exception {
		jsonObjects.clear();
		jsonCommand.clear();
		jsonCommand.put(Statics.WS_COMMAND, Statics.WS_SUBSCRIBE);
		jsonCommand.put("name", "Pepe");
		jsonObjects.add(jsonCommand);
		try {
			wsStudentClient.sendMessage(jsonObjects);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to send to server");
		}
		// a response is not immediately expected for a subscribe
	}

	/**
	 * Send another ping and what should come back is ws_status="subscribed".
	 * Followed by name = "Pepe"
	 * 
	 * @throws Exception
	 */
	@Test
	public void TestD() throws Exception {
		jsonObjects.clear();
		jsonCommand.clear();
		jsonCommand.put(Statics.WS_COMMAND, Statics.WS_PING);
		jsonObjects.add(jsonCommand);
		wsClientLatch = new CountDownLatch(1);
		wsStudentClient.setLatch(wsClientLatch);
		try {
			wsStudentClient.sendMessage(jsonObjects);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to send from student client");
		}
		boolean result = wsClientLatch.await(3, TimeUnit.SECONDS);
		assertTrue(result == true);
		// grab the json objects returned from server.
		jsonObjects = wsStudentClient.getJsonObjects();
		assertTrue(jsonObjects != null);
		assertTrue(jsonObjects.size() >= 1);
		jsonCommand = jsonObjects.get(0);
		assertTrue(jsonCommand != null);
		assertTrue(jsonCommand.size() >= 2);
		assertTrue(jsonCommand.get(WS_STATUS) != null);
		assertTrue(jsonCommand.get(WS_STATUS).equalsIgnoreCase(WS_SUBSCRIBED));
		assertTrue(jsonCommand.get("name") != null);
		assertTrue(jsonCommand.get("name").equalsIgnoreCase("Pepe"));
	}

	/**
	 * Now that the client has subscribed, start background thread that adds
	 * Pepe to the DB and wait for the client to get a corresponding
	 * notification.
	 * 
	 * @throws Exception
	 */
	@Test
	public void TestF() throws Exception {
		// give the ws client a new latch
		wsClientLatch = new CountDownLatch(1);
		wsStudentClient.setLatch(wsClientLatch);
		// create a latch for the update thread
		updateLatch = new CountDownLatch(1);
		// create and start threads that update the DB.
		// the threads will wait 5 seconds before they
		// update the DB. the threads will first insert a row, wait a bit, then
		// delete the row
		Thread updateStudent = new Thread(new AddRemovePepe(updateLatch, 5));
		updateStudent.setDaemon(true);
		updateStudent.start();
		// now that the update thread has started, wait for the web socket
		// client to be notified. the notifications should be received in less
		// than 15 seconds
		boolean result = wsClientLatch.await(15, TimeUnit.SECONDS);
		assertTrue(result == true);
		// wait for threads to finish
		updateLatch.await();
		// confirm that we got a 'notify' from the pusher
		jsonObjects = wsStudentClient.getJsonObjects();
		assertTrue(jsonObjects != null);
		assertTrue(jsonObjects.size() >= 1);
		jsonCommand = jsonObjects.get(0);
		assertTrue(jsonCommand != null);
		assertTrue(jsonCommand.size() >= 2);
		assertTrue(jsonCommand.get(WS_STATUS) != null);
		assertTrue(jsonCommand.get(WS_STATUS).equalsIgnoreCase(WS_NOTIFY));
		assertTrue(jsonCommand.get(WS_MSG) != null);
		assertTrue(jsonCommand.get(WS_MSG).length() == 64);
	}

}
