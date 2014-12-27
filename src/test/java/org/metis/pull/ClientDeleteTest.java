package org.metis.pull;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.metis.ClientTest;

public class ClientDeleteTest extends ClientTest{

	@Test
	// Method verifies that HTTP DELETE operation works with parameters specified in the URI
	public void TestA(){
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity= new HttpEntity<String>(headers );  

		try{
			// populate table with data that is to be deleted
			assertEquals(true, putPostVerify(entity,"/clientdel?MAKE=toyota&MODEL=tercel&MPG=30", HttpMethod.POST, null));
		} catch (Exception e) {
			fail("ERROR, ClientDeleteTest#TestA: " + e.getMessage());
		}

        Map<String,Object> verifyMap=new HashMap<String,Object>();
        
        verifyMap.put("MAKE", "toyota");
       
		assertEquals(false, deleteVerify( "/clientdel?MAKE=toyota",verifyMap));
	}
}
