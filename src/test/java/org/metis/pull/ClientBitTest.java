package org.metis.pull;


import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.metis.ClientTest;

// Class to exercise PUT and POST tests using bit and boolean
// MySQL data types.

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientBitTest extends ClientTest{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	// PUT positive testing.  Parameters are specified in the URI.
	public void TestAM() {
		if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
			return;
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    	// Primary key "ID" has an unknown value, so it is not added to the verification map.
    	Map<String,Object> verifyMap=new HashMap<String,Object>();
    	verifyMap.put("ABIT", true);
    	verifyMap.put("ABOOLEAN", false);
    		
    	String putUri= "/clientbit?id=1&ABIT=true&ABOOLEAN=false";

    	try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		fail("ERROR, ClientBitTest#TestA: " + e.getMessage());
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientbit?id=1";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}
	
	@Test
	// PUT positive testing.  Parameters are specified in the URI.
	public void TestAO() {
		if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
			return;
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ABIT", "1");
    		verifyMap.put("ABOOLEAN","false");
    		
    		String putUri= "/clientbit?id=1&aBit=1&aBoolean=false";

    		try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		fail("ERROR, ClientBitTest#TestAA: " + e.getMessage());
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientbit?id=1";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}


	@Test
	// PUT positive testing.  Same as TestA() but with values reversed.
	public void TestBM() {
		if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
			return;
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    	// Primary key "ID" has an unknown value, so it is not added to the verification map.
    	Map<String,Object> verifyMap=new HashMap<String,Object>();
    	verifyMap.put("ABIT", false);
    	verifyMap.put("ABOOLEAN", true);
    		
    	String putUri= "/clientbit?id=1&ABIT=false&ABOOLEAN=true";

    	try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		fail("ERROR, ClientBitTest#TestA: " + e.getMessage());
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientbit?id=1";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}
	
	@Test
	// PUT positive testing.  Same as TestA() but with values reversed.
	public void TestBO() {
		if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
			return;
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    	// Primary key "ID" has an unknown value, so it is not added to the verification map.
    	Map<String,Object> verifyMap=new HashMap<String,Object>();
    	verifyMap.put("ABIT", "0");
    	verifyMap.put("ABOOLEAN", "true");
    		
    	String putUri= "/clientbit?id=1&ABIT=0&ABOOLEAN=true";

    	try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		fail("ERROR, ClientBitTest#TestBB: " + e.getMessage());
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientbit?id=1";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	// PUT negative testing.  "crap" entered as value for Bit.
	public void TestC() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    	// Primary key "ID" has an unknown value, so it is not added to the verification map.
    	Map<String,Object> verifyMap=new HashMap<String,Object>();
    	verifyMap.put("ABIT", false);
    	verifyMap.put("ABOOLEAN", true);
    		
    	String putUri= "/clientbit?id=1&ABIT=crap&ABOOLEAN=true";

    	try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		// 500 Internal Server Error because "crap" was the passed value and not true/false
        		assertEquals(0, e.getMessage().compareToIgnoreCase("500 Internal Server Error"));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientbit?id=1";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}
	@Test
	// PUT negative testing.  "crap" entered as value for Boolean.
	public void TestD() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    	// Primary key "ID" has an unknown value, so it is not added to the verification map.
    	Map<String,Object> verifyMap=new HashMap<String,Object>();
    	verifyMap.put("ABIT", false);
    	verifyMap.put("ABOOLEAN", true);
    		
    	String putUri= "/clientbit?id=1&ABIT=true&ABOOLEAN=crap";

    	try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		// 500 Internal Server Error because "crap" was the passed value and not true/false
        		assertEquals(0, e.getMessage().compareToIgnoreCase("500 Internal Server Error"));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientbit?id=1";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	  // POST test with values specified in a string and content type set to URLENCODED.
	  public void TestEM(){
		if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
			return;
		}
	    HttpEntity<String> entity;
	    HttpHeaders hdr = new HttpHeaders();

	    hdr.add("User-Agent", "macosx");
	    hdr.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	    
    		String params = "ABIT=true&ABOOLEAN=false";
	    
	    entity = new HttpEntity<String>(params, hdr);
	    
	    Map<String, Object> verifyMap=new HashMap<String, Object>();
    		verifyMap.put("ABIT", true);
    		verifyMap.put("ABOOLEAN", false);
	        
	    try{
	    		assertEquals(true, putPostVerify(entity, "/clientbit", HttpMethod.POST, verifyMap));
	    } catch (Exception e) {
        		fail("ERROR, ClientBitTest#TestE: " + e.getMessage());
	    } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientbit?ABIT=true";
        		assertEquals(true, deleteVerify(delUri, null));
	    }
	  }

	@Test
	  // POST test with values specified in a string and content type set to URLENCODED.
	  public void TestEO(){
		if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
			return;
		}
	    HttpEntity<String> entity;
	    HttpHeaders hdr = new HttpHeaders();

	    hdr.add("User-Agent", "macosx");
	    hdr.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	    
    		String params = "ABIT=1&ABOOLEAN=false";
	    
	    entity = new HttpEntity<String>(params, hdr);
	    
	    Map<String, Object> verifyMap=new HashMap<String, Object>();
    		verifyMap.put("ABIT", "1");
    		verifyMap.put("ABOOLEAN", "false");
	        
	    try{
	    		assertEquals(true, putPostVerify(entity, "/clientbit", HttpMethod.POST, verifyMap));
	    } catch (Exception e) {
        		fail("ERROR, ClientBitTest#TestEE: " + e.getMessage());
	    } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientbit?ABIT=1";
        		assertEquals(true, deleteVerify(delUri, null));
	    }
	  }
	  
	  @Test
	  // POST test with values specified in a MultiValueMap and content type set to URLENCODED.
	  public void TestF(){
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	    headers.set("User-Agent", "macosx");

	    MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
    		form.add("ABIT", "true");
    		form.add("ABOOLEAN", "true");

	    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(form, headers);
	    
	    Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ABIT", true);
    		verifyMap.put("ABOOLEAN", true);

	    try{
	    		assertEquals(true, putPostVerify(entity, "/clientbit",HttpMethod.POST, verifyMap));
	    } catch (Exception e) {
	    		assertEquals(true, e.getMessage().equalsIgnoreCase("500 Internal Server Error"));
	    } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientbit?ABIT=true";
        		assertEquals(true, deleteVerify(delUri, null));
	    }
	  }
}
