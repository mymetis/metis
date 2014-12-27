package org.metis.push;

import java.util.List;
import java.util.Map;

import org.metis.utils.Utils;

import static org.metis.utils.Statics.WS_STATUS;

import javax.websocket.Decoder;
import javax.websocket.DecodeException;
import javax.websocket.EndpointConfig;

/**
 * This decoder is used to decode one or more Json objects into a list of Maps.
 * 
 * @author jfernandez
 * 
 */
public class JsonDecoder implements Decoder.Text<List<Map<String, String>>> {

	public void init(EndpointConfig config) {
	}

	public void destroy() {
	}

	public List<Map<String, String>> decode(String json) throws DecodeException {
		//System.out.println("decode = " + json);
		List<Map<String, String>> list = null;
		try {
			list = Utils.parseJson(json);
		} catch (Exception e) {
			throw new DecodeException(json, e.getMessage());
		}
		return list;
	}

	public boolean willDecode(String s) {
		//System.out.println("willDecode received = " + s);
		return (s == null || s.isEmpty()) ? false : s.indexOf(WS_STATUS) >= 0;
	}

}
