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

// Class to exercise PUT and POST tests using numeric and decimal.
// create table numericDecimal(
// 		id 			int not null auto_increment,
// 		ANUMERIC	numeric(5,2),         // 5 = max digits of the number.  2=max digits to right of decimal point.
// 		ADECIMAL	decimal(7,3),         //  Total of 5 digits is enforced, otherwise, it's an error.  So, there must be
// 		primary key(id) );                   // 3 digits to the left of the decimal point and 2 to the right.  Total is 5.

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientNumTest extends ClientTest{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	// PUT positive testing.  Parameters are specified in the URI.
	public void TestA() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    	// Primary key "ID" has an unknown value, so it is not added to the verification map.
    	Map<String,Object> verifyMap=new HashMap<String,Object>();
    	verifyMap.put("ANUMERIC", 123.45);
    	verifyMap.put("ADECIMAL", 6789.34);
    		
    	String putUri= "/clientnum?id=1&ANUMERIC=123.45&ADECIMAL=6789.34";

    	try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		fail("ERROR, ClientNumTest#TestA: " + e.getMessage());
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientnum?id=1";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	// PUT negative testing of setting ANUMERIC  value larger that data field can store.
	public void TestB() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 

    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ANUMERIC", 123.456);
    		verifyMap.put("ADECIMAL", 6789.34);
    		
    		// ANUMERIC = numeric(5,2).  Here, specify 6 digits for ANUMERIC.
    		String putUri= "/clientnum?id=1&ANUMERIC=1234.56&ADECIMAL=6789.34";

    		try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		// 500 Internal Server Error because "crap" was the passed value and not true/false
        		assertEquals(0, e.getMessage().compareToIgnoreCase("500 Internal Server Error"));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientnum?id=1";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	// PUT negative testing of setting ADECIMAL  value larger that data field can store.
	public void TestC() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 

    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ANUMERIC", 123.456);
    		verifyMap.put("aBoolean", 67890.34);
    		
    		// ANUMERIC = numeric(7,3).  Here, specify 8 digits for ANUMERIC.  Actually, 7, but
    		// number of digits to the right of decimal point will be enforced by MySQL to be
    		// 3 digits.  So, 7 digits are specified but with the enforcement, there will be 8.
    		String putUri= "/clientnum?id=1&ANUMERIC=123.56&ADECIMAL=67890.34";

    		try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		// 500 Internal Server Error because "crap" was the passed value and not true/false
        		assertEquals(0, e.getMessage().compareToIgnoreCase("500 Internal Server Error"));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientnum?id=1";
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
	    
    		String params = "ANUMERIC=123.45&ADECIMAL=1122.334";
	    
	    entity = new HttpEntity<String>(params, hdr);
	    
	    Map<String, Object> verifyMap=new HashMap<String, Object>();
    		verifyMap.put("ANUMERIC", 123.45);
    		verifyMap.put("ADECIMAL", 1122.334);
	        
	    try{
	    		assertEquals(true, putPostVerify(entity, "/clientnum", HttpMethod.POST, verifyMap));
	    } catch (Exception e) {
        		fail("ERROR, ClientNumTest#TestE: " + e.getMessage());
	    } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientnum?ANUMERIC=123.45";
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
	    
    		String params = "ANUMERIC=123.45&ADECIMAL=1122.334";
	    
	    entity = new HttpEntity<String>(params, hdr);
	    
	    Map<String, Object> verifyMap=new HashMap<String, Object>();
    		verifyMap.put("ANUMERIC", 123.45);
    		verifyMap.put("ADECIMAL", 1122.334);
	        
	    try{
	    		assertEquals(true, putPostVerify(entity, "/clientnum", HttpMethod.POST, verifyMap));
	    } catch (Exception e) {
        		fail("ERROR, ClientNumTest#TestE: " + e.getMessage());
	    } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientnum?ANUMERIC=123.45";
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
    		form.add("ANUMERIC", "543.21");
    		form.add("ADECIMAL", "9876.543");

	    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(form, headers);
	    
	    Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ANUMERIC",543.21);
    		verifyMap.put("ADECIMAL", 9876.543);

	    try{
	    		assertEquals(true, putPostVerify(entity, "/clientnum",HttpMethod.POST, verifyMap));
	    } catch (Exception e) {
        		fail("ERROR, ClientNumTest#TestF: " + e.getMessage());
	    } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientnum?ANUMERIC=543.21";
        		assertEquals(true, deleteVerify(delUri, null));
	    }
	  }
}
