package org.metis.pull;

import static org.junit.Assert.*;

import java.math.BigInteger;
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

// Data type ranges           signed
// -------------------------------------------------------------
//    		TINYINT:			-128 to 127
//
//    		SMALLINT:			-32768 to 32767
//
//    		INTEGER:			-2147483648 to 2147483647
//
//    		BIGINT:				-9223372036854775808 to 9223372036854775807

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientIntegerTest extends ClientTest{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	// Positive testing of LOWER SIGNED integer range.
	public void TestA() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATINYINT", -128);
    		verifyMap.put("ASMALLINT",-32768);
    		verifyMap.put("AINTEGER", -2147483648);
    		BigInteger bi = new BigInteger("-9223372036854775808");
    		verifyMap.put("ABIGINT", bi.longValue());
    		
    	    try{
        		assertEquals(true, putPostVerify(entity, "/clientsignedint?ATINYINT=-128&ASMALLINT=-32768&AINTEGER=-2147483648&ABIGINT=-9223372036854775808", HttpMethod.PUT, verifyMap));
            
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		assertEquals(true, deleteVerify("/clientsignedint?ATINYINT=-128",null));
        } catch (Exception e) {
        		assertEquals(true, deleteVerify("/clientsignedint?ATINYINT=-128",null));
        		fail("ERROR, ClientPutTest#TestA: " + e.getMessage());
        }
	}

	@Test
	// Positive testing of UPPER SIGNED integer range.
	public void TestB() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATINYINT", 127);
    		verifyMap.put("ASMALLINT", 32767);
    		verifyMap.put("AINTEGER", 2147483647);
    		BigInteger bi = new BigInteger("9223372036854775807");
    		verifyMap.put("ABIGINT", bi.longValue());
    		
    	    try{
        		assertEquals(true, putPostVerify(entity, "/clientsignedint?ATINYINT=127&ASMALLINT=32767&AINTEGER=2147483647&ABIGINT=9223372036854775807", HttpMethod.PUT, verifyMap));
            
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		assertEquals(true, deleteVerify("/clientsignedint?ATINYINT=127",null));
        } catch (Exception e) {
        		assertEquals(true, deleteVerify("/clientsignedint?ATINYINT=127",null));
        		fail("ERROR, ClientPutTest#TestA: " + e.getMessage());
        }
	}

	@Test
	// Negative testing of UPPER SIGNED integer range.  ASMALLINT is an out of range value for
	// the corresponding database field.
	public void TestC() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATINYINT", 127);
    		verifyMap.put("ASMALLINT", 32768);
    		verifyMap.put("AINTEGER", 2147483647);
    		BigInteger bi = new BigInteger("9223372036854775807");
    		verifyMap.put("ABIGINT", bi.longValue());
    		
    	    try{
        		assertEquals(true, putPostVerify(entity, "/clientsignedint?ATINYINT=127&ASMALLINT=32768&AINTEGER=2147483647&ABIGINT=9223372036854775807", HttpMethod.PUT, verifyMap));
            
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		assertEquals(true, deleteVerify("/clientsignedint?ATINYINT=127",null));
        } catch (Exception e) {
        		assertEquals(true, e.getMessage().equalsIgnoreCase("500 Internal Server Error"));
        		assertEquals(true, deleteVerify("/clientsignedint?ATINYINT=127",null));
        }
	}
	
	@Test
	  // Perform a put of integer values via a URLENCODED string.
	  public void TestD(){
	    HttpEntity<String> entity;
	    HttpHeaders hdr = new HttpHeaders();

	    hdr.add("User-Agent", "macosx");
	    hdr.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	    
	    String params = "ATINYINT=127&ASMALLINT=32767&AINTEGER=2147483647&ABIGINT=9223372036854775807";
	    
	    entity = new HttpEntity<String>(params, hdr);
	    
	    Map<String, Object> verifyMap=new HashMap<String, Object>();
	        
	    verifyMap.put("ATINYINT", 127);
	    verifyMap.put("ASMALLINT",32767);
	    verifyMap.put("AINTEGER",  2147483647);
        BigInteger bi = new BigInteger("9223372036854775807");
	    verifyMap.put("ABIGINT", bi.longValue());
	        
	    try{
	    		assertEquals(true, putPostVerify(entity, "/clientsignedint", HttpMethod.PUT, verifyMap));
        		assertEquals(true, deleteVerify("/clientsignedint?ATINYINT=127",null));
	    } catch (Exception e) {
        		assertEquals(true, deleteVerify("/clientsignedint?ATINYINT=127",null));
	    }
	  }
	  
	  @Test
	  // Perform a put of integer values via a MultiValueMap.
	  public void TestE(){
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	    headers.set("User-Agent", "macosx");

	    MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
	    form.add("ATINYINT", "127");
	    form.add("ASMALLINT", "32767");
	    form.add("AINTEGER",  "2147483647");
	    form.add("ABIGINT", "9223372036854775807");

	    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(form, headers);
	    
	    Map<String,Object> verifyMap=new HashMap<String,Object>();
	          
	    verifyMap.put("ATINYINT", 127);
	    verifyMap.put("ASMALLINT", 32767);
	    verifyMap.put("AINTEGER",  2147483647);
        BigInteger bi = new BigInteger("9223372036854775807");
	    verifyMap.put("ABIGINT", bi.longValue());

	    try{
	    		assertEquals(true, putPostVerify(entity, "/clientsignedint",HttpMethod.PUT, verifyMap));
	    		assertEquals(true, deleteVerify("/clientsignedint?ATINYINT=127",null));
	    } catch (Exception e) {
	    		assertEquals(true, deleteVerify("/clientsignedint?ATINYINT=127",null));
	    		assertEquals(true, e.getMessage().equalsIgnoreCase("500 Internal Server Error"));
	    }
	  }
}
