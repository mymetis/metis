package org.metis.push;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.websocket.EndpointConfig;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ClientEndpointConfig.Builder;
import javax.websocket.Endpoint;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.MessageHandler;
import java.util.concurrent.CountDownLatch;
import static org.metis.utils.Statics.*;
import javax.websocket.*;

/**
 * WebSocket Endpoint class used by the push test cases. The test cases create
 * an instance of this Endpoint to interact with the web socket server; i.e.,
 * Metis.
 * 
 * @author jfernandez
 * 
 */
public class WsClient extends Endpoint implements
		MessageHandler.Whole<List<Map<String, String>>>,
		MessageHandler.Partial<String> {

	// the web socket session assigned to this client
	private Session session;

	// the wsClientLatch used to inform the test case that a message has
	// been received from the server
	private CountDownLatch latch = null;

	//
	private List<Map<String, String>> jsonObjects;
	private static List<Class<? extends Encoder>> encoders = new ArrayList<Class<? extends Encoder>>();
	private static List<Class<? extends Decoder>> decoders = new ArrayList<Class<? extends Decoder>>();
	static {
		decoders.add(JsonDecoder.class);
		encoders.add(JsonEncoder.class);
	}

	public WsClient() {
	}

	/**
	 * Called after connection to server has been established.
	 * 
	 * @param session
	 */
	public void onOpen(Session session, EndpointConfig ec) {
		//System.out.println("#### session opened");
		session.addMessageHandler(this);
		this.setSession(session);
	}

	/**
	 * Called after connection to server has been closed.
	 * 
	 * @param session
	 */
	public void onClose(Session session, EndpointConfig ec) {
		this.setSession(null);

	}

	public void onClose(Session session, CloseReason reason) {
		this.setSession(null);
	}

	/**
	 * Receives message from server. The incoming json string will have been
	 * encoded to a list of maps.
	 * 
	 * @param jsonObjects
	 * @throws Exception
	 */
	public void onMessage(List<Map<String, String>> jsonObjects) {
		System.out.println("jsonObjects received = " + jsonObjects.toString());
		setJsonObjects(jsonObjects);
		// after sending a message to the server, the test case waits/relies on
		// this wsClientLatch for notification that a message has been received from the
		// server.
		if (latch != null && latch.getCount() > 0) {
			latch.countDown();
		}
	}

	/**
	 * Used for receiving 'partial' messages; however, this should never get
	 * called because Metis should only send 'whole' messages.
	 */
	public void onMessage(String partialMessage, boolean last) {
		//System.out.println("partial jsonObjects received = " + jsonObjects.toString());
	}

	/**
	 * Sends message to server - the list of maps will be encoded to a json
	 * string.
	 * 
	 * @param jsonObjects
	 * @throws Exception
	 */
	public void sendMessage(List<Map<String, String>> jsonObjects)
			throws Exception {

		if (getSession() == null) {
			throw new Exception("session is null");
		}

		if (jsonObjects == null || jsonObjects.isEmpty()) {
			throw new Exception("jsonObjects passed in is either null or empty");
		}

		Map<String, String> map = jsonObjects.get(0);

		String command = map.get(WS_COMMAND);
		if (command == null) {
			throw new Exception("jsonObjects does not include expected "
					+ WS_COMMAND + " field");
		}

		if (!command.equals(WS_SUBSCRIBE) && !command.equals(WS_PING)) {
			throw new Exception("jsonObjects contains this unknown command: "
					+ command);
		}

		// send the message
		getSession().getBasicRemote().sendObject(jsonObjects);
	}

	/**
	 * Create a BaseClient and connect it to the server.
	 * 
	 * @param path
	 * @return an instance of a BaseClient
	 * @throws Exception
	 */
	public static WsClient connect(String path) throws Exception {
		ClientEndpointConfig cec = Builder.create().decoders(decoders)
				.encoders(encoders).build();
		WebSocketContainer wsc = ContainerProvider.getWebSocketContainer();
		WsClient wc = new WsClient();
		wsc.connectToServer(wc, cec, new URI(path));
		return wc;
	}

	/**
	 * Disconnect the BaseClient from the web socket server.
	 */
	public void disconnect() {
		try {
			if (getSession() != null) {
				getSession().close();
				setSession(null);
			}
		} catch (IOException ioe) {
			System.out.println("disconnect: caught this ioexception: "
					+ ioe.getMessage());
		}
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public List<Map<String, String>> getJsonObjects() {
		return jsonObjects;
	}

	public void setJsonObjects(List<Map<String, String>> jsonObjects) {
		this.jsonObjects = jsonObjects;
	}

	public CountDownLatch createLatch() {
		setLatch(new CountDownLatch(1));
		return getLatch();
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}

}
