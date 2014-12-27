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

//  create table charTest(
//		ACHAR 			char not null,
//		ANCHAR			char character set UTF8,
//		AVARCHAR		varchar(5),
//		ANVARCHAR		varchar(15) character set UTF8,
//		ALONGVARCHAR	longtext,
//		ALONGNVARCHAR	longtext character set UTF8,
//		primary key(ACHAR));  
// 
// test-servlet.xml
//    sqls4Put:
//       insert into charTest values (`char:ACHAR`, `nchar:ANCHAR`, `varchar:AVARCHAR`, `nvarchar:ANVARCHAR`, `longvarchar:ALONGVARCHAR`, `longnvarchar:ALONGNVARCHAR`)
//
//    sqls4Post:
//		  insert into charTest(ACHAR, ANCHAR, AVARCHAR, ALONGNVARCHAR) values (`char:ACHAR`,  `nchar:ANCHAR`, `varchar:AVARCHAR`, `longnvarchar:ALONGNVARCHAR`)

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientCharTest extends ClientTest{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	// PUT test via parameters specified in the URI.
	public void TestA() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ACHAR", "a");
    		verifyMap.put("ANCHAR", "b");
    		verifyMap.put("AVARCHAR", "cd");
    		verifyMap.put("ANVARCHAR", "ef");
    		verifyMap.put("ALONGVARCHAR", "gh"); 
    		verifyMap.put("ALONGNVARCHAR", "ij");
    		
    	    try{
        		assertEquals(true, putPostVerify(entity, "/clientchar?ACHAR=a&ANCHAR=b&AVARCHAR=cd&ANVARCHAR=ef&ALONGVARCHAR=gh&ALONGNVARCHAR=ij", HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		fail("ERROR, ClientCharTest#TestA: " + e.getMessage());
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		assertEquals(true, deleteVerify("/clientchar?ACHAR=a",null));
        }
	}
	
	@Test
	// POST test.  If a primary key is specified for an update and it is auto-increment, then a URI location will be returned.  However,
	// with a primary key that is not auto-increment, but it still an update, not URI location is returned.  ClientTest.java::putPostVerify()
	// throws an exception if POST is used with a primary key that is not auto-increment because a URI location is not returned.
	public void TestBM() {
		if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
			return;
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ACHAR", "a");
    		verifyMap.put("ANCHAR", "b");
    		verifyMap.put("AVARCHAR", "cd");
    		verifyMap.put("ALONGNVARCHAR", "ij");
			
    	    try{
        		assertEquals(true, putPostVerify(entity, "/clientchar?ACHAR=a&ANCHAR=b&AVARCHAR=cd&ALONGNVARCHAR=ij", HttpMethod.POST, verifyMap));
        } catch (Exception e) {
        		// Primary key of charTest table is not auto-increment and as such, POST does not return a URI location
        		assertEquals(true, (e.getMessage().compareTo("No URI location returned.")==0));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		assertEquals(true, deleteVerify("/clientchar?ACHAR=a",null));
        }
	}

	@Test
	// POST test.  If a primary key is specified for an update and it is auto-increment, then a URI location will be returned.  However,
	// with a primary key that is not auto-increment, but it still an update, not URI location is returned.  ClientTest.java::putPostVerify()
	// throws an exception if POST is used with a primary key that is not auto-increment because a URI location is not returned.
	public void TestBO() {
		if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
			return;
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ACHAR", "a");
    		verifyMap.put("ANCHAR", "b");
    		verifyMap.put("AVARCHAR", "cd");
    		verifyMap.put("ALONGNVARCHAR", "ij");
			
    	    try{
        		assertEquals(true, putPostVerify(entity, "/clientchar?ACHAR=a&ANCHAR=b&AVARCHAR=cd&ALONGNVARCHAR=ij", HttpMethod.POST, verifyMap));
        } catch (Exception e) {
        		// Primary key of charTest table is not auto-increment and as such, POST does not return a URI location
        		assertEquals(true, (e.getMessage().compareTo("500 Internal Server Error")==0));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		assertEquals(true, deleteVerify("/clientchar?ACHAR=a",null));
        }
	}

	@Test
	  // Perform a PUT  via a URLENCODED string.
	  public void TestC(){
	    HttpEntity<String> entity;
	    HttpHeaders hdr = new HttpHeaders();

	    hdr.add("User-Agent", "macosx");
	    hdr.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	    
	    String params = "ACHAR=a&ANCHAR=b&AVARCHAR=cd&ANVARCHAR=ef&ALONGVARCHAR=gh&ALONGNVARCHAR=ij";
	    
	    entity = new HttpEntity<String>(params, hdr);
	    
		Map<String,Object> verifyMap=new HashMap<String,Object>();
		verifyMap.put("ACHAR", "a");
		verifyMap.put("ANCHAR", "b");
		verifyMap.put("AVARCHAR", "cd");
		verifyMap.put("ANVARCHAR", "ef");
		verifyMap.put("ALONGVARCHAR", "gh"); 
		verifyMap.put("ALONGNVARCHAR", "ij");
	        
	    try{
	    		assertEquals(true, putPostVerify(entity, "/clientchar", HttpMethod.PUT, verifyMap));
	    } catch (Exception e) {
        		fail("ERROR, ClientCharTest#TestC: " + e.getMessage());
	    } finally {
        		assertEquals(true, deleteVerify("/clientchar?ACHAR=a",null));
	    }
	  }
	  
	  @Test
	  // Perform a PUT via a MultiValueMap.
	  public void TestD(){
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	    headers.set("User-Agent", "macosx");

	    MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.add("ACHAR", "a");
		form.add("ANCHAR", "b");
		form.add("AVARCHAR", "cd");
		form.add("ANVARCHAR", "ef");
		form.add("ALONGVARCHAR", "gh"); 
		form.add("ALONGNVARCHAR", "ij");

	    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(form, headers);
	    
		Map<String,Object> verifyMap=new HashMap<String,Object>();
		verifyMap.put("ACHAR", "a");
		verifyMap.put("ANCHAR", "b");
		verifyMap.put("AVARCHAR", "cd");
		verifyMap.put("ANVARCHAR", "ef");
		verifyMap.put("ALONGVARCHAR", "gh"); 
		verifyMap.put("ALONGNVARCHAR", "ij");

	    try{
	    		assertEquals(true, putPostVerify(entity, "/clientchar",HttpMethod.PUT, verifyMap));
	    } catch (Exception e) {
	    		assertEquals(true, e.getMessage().equalsIgnoreCase("500 Internal Server Error"));
	    } finally {
        		assertEquals(true, deleteVerify("/clientchar?ACHAR=a",null));
	    }
	  }
}
