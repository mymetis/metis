package org.metis.pull;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.metis.ClientTest;

public class ClientGetTest extends ClientTest{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void TestA() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		try {
			ResponseEntity<List> result = getRestTemplate().exchange(getServerurl()
					+ "/rooms", HttpMethod.GET, entity, List.class);
			assertEquals(true, SC_OK == result.getStatusCode().value());
			List<Map<String, Object>> list = (List<Map<String, Object>>) result.getBody();
			assertEquals(true, list != null);
			// we should get back 10 rows (json objects)
			// reyb assertEquals(true, list.size() == 10);
			for (Map<String, Object> map : list) {
				// confirm that each row has 4 columns (keys).
				assertEquals(true, map.keySet().size() == 4);
				//for (Entry<String, Object> e : map.entrySet())  {
				//	  System.out.println(e.getKey()); //prints 2 and 1
				//	  System.out.println(e.getValue()); //prints two and one
				//	}
			}
		} catch (Exception e) {
			fail("ERROR, Got this exception: " + e.toString());
		}
	}

}
