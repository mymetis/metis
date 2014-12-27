package org.metis.pull;

import static org.junit.Assert.*;
import static javax.servlet.http.HttpServletResponse.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.*;

import org.metis.ClientTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HttpClientTest extends ClientTest {
		
	// Method to send a particular User-Agent string to a particular resource URL
	private static int sendUserAgent(String resourceUrl, String agent) {
		HttpEntity<String> entity;
		HttpHeaders hdr = getHdr();
		
		int rtnVal;
		
		hdr.set("User-Agent", agent);
		
		entity = new HttpEntity<String>(hdr);
		
		try {
			ResponseEntity<String> result=getRestTemplate().exchange(getServerurl()+resourceUrl, HttpMethod.GET, entity, String.class);
		  	
		  	rtnVal = result.getStatusCode().value();
		} catch (HttpClientErrorException e) {
			rtnVal = e.getStatusCode().value();
		}
		return( rtnVal );
	}
	
	// Method to send a particular HTTP Method to a particular resource URL
	private static int sendHttpMethod(String resourceUrl, HttpMethod method)
	{
		HttpEntity<String> entity;
		HttpHeaders hdr = getHdr();
		int rtnVal;
		
		hdr.set("User-Agent", "foobarDevice");

		entity = new HttpEntity<String>(hdr);
		
		try {
			ResponseEntity<String> result=getRestTemplate().exchange(getServerurl()+resourceUrl, method, entity, String.class);
		  	
		  	rtnVal = result.getStatusCode().value();
		} catch (HttpClientErrorException e) {
			rtnVal = e.getStatusCode().value();
		} catch (HttpServerErrorException exc) {
			rtnVal = exc.getStatusCode().value();
		} catch (ResourceAccessException exc) {
			System.out.println("*** Resource Access Exception: " + exc.getMessage());
			rtnVal=SC_METHOD_NOT_ALLOWED;
		} catch (Exception exc) {
			System.out.println("*** Unanticipated Exception: " + exc.getMessage());
			rtnVal=-1;
		}
		return( rtnVal );
	}

	@Test
	// Test different values for "User-Agent" http header.
	public void TestA() {
		String rdb;

		// hctA1rdb does not have an AgentName list declared
		rdb="/wds/test/hctA1";
		assertEquals(true, SC_OK == sendUserAgent(rdb, ""));
		assertEquals(true, SC_OK == sendUserAgent(rdb, "crap"));
		assertEquals(true, SC_OK == sendUserAgent(rdb, "safari"));

		// hctA2rdb has an allowed AgentName list declared
		rdb="/wds/test/hctA2";
		assertEquals(true, SC_UNAUTHORIZED == sendUserAgent(rdb, ""));
		assertEquals(true, SC_UNAUTHORIZED == sendUserAgent(rdb, "crap"));
		assertEquals(true, SC_OK == sendUserAgent(rdb, "allowedAgent1"));
		assertEquals(true, SC_OK == sendUserAgent(rdb, "ALLowedAgent1"));
		assertEquals(true, SC_OK == sendUserAgent(rdb, "allowedagent2"));

		// hctA3rdb has an not-allowed AgentName list declared
		rdb="/wds/test/hctA3";
		assertEquals(true, SC_UNAUTHORIZED == sendUserAgent(rdb, ""));
		assertEquals(true, SC_OK == sendUserAgent(rdb, "crap"));
		assertEquals(true, SC_OK == sendUserAgent(rdb, "safari"));
		assertEquals(true, SC_UNAUTHORIZED == sendUserAgent(rdb, "notallowedAgent1"));
		assertEquals(true, SC_UNAUTHORIZED == sendUserAgent(rdb, "NOTALLowedAgent1"));
		assertEquals(true, SC_UNAUTHORIZED == sendUserAgent(rdb, "NoTallowedagent2"));
	}

	@Test
	// Test setting different values for http verb.
	public void TestB(){		
		String rdb;
		rdb="/wds/test/hctA3";
		assertEquals(true, SC_OK == sendHttpMethod(rdb, HttpMethod.GET));
		assertEquals(true, SC_METHOD_NOT_ALLOWED == sendHttpMethod(rdb, HttpMethod.PUT));
		assertEquals(true, SC_METHOD_NOT_ALLOWED == sendHttpMethod(rdb, HttpMethod.POST));
		assertEquals(true, SC_METHOD_NOT_ALLOWED == sendHttpMethod(rdb, HttpMethod.DELETE));
		
		// NOTE:  PATCH method results with a ResourceAccessException which does not have a status code.
		// For now, sendHttpMethod() manually sets the status code to SC_METHOD_NOT_ALLOWED
		assertEquals(true, SC_METHOD_NOT_ALLOWED == sendHttpMethod(rdb, HttpMethod.PATCH));
		assertEquals(true, SC_METHOD_NOT_ALLOWED == sendHttpMethod(rdb, HttpMethod.HEAD));
		assertEquals(true, SC_OK == sendHttpMethod(rdb, HttpMethod.OPTIONS)); // SC_OK????
		assertEquals(true, SC_METHOD_NOT_ALLOWED == sendHttpMethod(rdb, HttpMethod.TRACE));
	}
	
	@Test
	// Test invalid resource
	public void TestC(){
		String rdb;
		
		rdb="/wds/test/hctA3_bogus";
		assertEquals(true, SC_NOT_FOUND == sendHttpMethod(rdb, HttpMethod.GET));

		rdb="/wds/test/hctA3?bogus=-1";
		// test unmatched parameters to SQL statement.  NOTE: exception caught is HttpServerErrorException
		// and not HttpClientErrorException
		assertEquals(true, SC_INTERNAL_SERVER_ERROR == sendHttpMethod(rdb, HttpMethod.GET));

	}

}
