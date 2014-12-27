package org.metis.push;

import java.util.List;
import java.util.Map;

import org.metis.utils.Utils;

import javax.websocket.Encoder;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;

/**
 * This encoder is used to encode a List of Maps to a corresponding number of
 * json objects.
 * 
 * @author jfernandez
 * 
 */
public class JsonEncoder implements Encoder.Text<List<Map<String, Object>>> {

	public void init(EndpointConfig config) {

	}

	public void destroy() {

	}

	public String encode(List<Map<String, Object>> list) throws EncodeException {
		String json = null;
		try {
			json = Utils.generateJson(list);
		} catch (Exception e) {
			throw new EncodeException(list, e.getMessage());
		}
		return json;
	}

}
