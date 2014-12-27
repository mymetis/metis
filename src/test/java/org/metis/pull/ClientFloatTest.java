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

// Class to exercise PUT and POST tests using doubles, floats, and real
// MySQL data types.

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientFloatTest extends ClientTest{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	// PUT positive testing with MIN_VALUEs.  Parameters are specified
	// in the URI.
	public void TestAM() {
		if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
			return;
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ADOUBLE", Double.MIN_VALUE);
    		verifyMap.put("AFLOAT", (Double)1.4E-45);
    		verifyMap.put("AREAL", (Double)1.4E-45);
    		
    		String putUri= "/clientfloat?id=1&ADOUBLE="+Double.MIN_VALUE + "&AFLOAT=1.4E-45&AREAL=1.4E-45";
    	    try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		fail("ERROR, ClientFloatTest#TestA: " + e.getMessage());
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientfloat?ADOUBLE="+Double.MIN_VALUE;
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	// PUT positive testing with MIN_VALUEs.  Parameters are specified
	// in the URI.
	public void TestAO() {
		if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
			return;
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("aDouble", 1000000000000000L);
    		verifyMap.put("aFloat", 1000000000000000L);
    		verifyMap.put("aReal", 999999990000000L);
    		
    		String putUri= "/clientfloat?id=1&aDouble=1.0E15&aFloat=1.0E15&aReal=1.0E15";
    	    try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		fail("ERROR, ClientFloatTest#TestA: " + e.getMessage());
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientfloat?ADOUBLE="+1000000000000000L;
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	// PUT positive testing MAX_VALUEs.  Parameters are specified in the URI.  The value for the float MySQL data types
	// loses precision, so the max float value is manually specified.  The loss of precision makes the verification of the
	// stored value fail.
	public void TestBM() {
		if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
			return;
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 

    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ADOUBLE", Double.MAX_VALUE);
    		verifyMap.put("AFLOAT", (Double)3.40282e38); 				// max value is actually 3.402823466E+38, but MySQL truncates the value.
    		verifyMap.put("AREAL", (Double)3.402825E35);
    		
    		// Manually set max float value as this is the max float value for MySQL
    		String putUri= "/clientfloat?id=2&ADOUBLE="+Double.MAX_VALUE + "&AFLOAT=3.40282e38&AREAL="+(Double)3.402825E35;
    	    try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		fail("ERROR, ClientFloatTest#TestA: " + e.getMessage());
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientfloat?ADOUBLE="+Double.MAX_VALUE;
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}
	
	@Test
	// PUT positive testing MAX_VALUEs.  Parameters are specified in the URI.  The value for the float MySQL data types
	// loses precision, so the max float value is manually specified.  The loss of precision makes the verification of the
	// stored value fail.
	public void TestBO() {
		if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
			return;
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 
	    
		// Primary key "ID" has an unknown value, so it is not added to the verification map.
		Map<String,Object> verifyMap=new HashMap<String,Object>();
		verifyMap.put("aDouble", (Double)1234.56789012345);
		verifyMap.put("aFloat",1.7976931348);
		verifyMap.put("aReal",123456.78);
		
		// Manually set max float value as this is the max float value for MySQL
		// String putUri= "/clientfloat?id=1&aDouble=15&aFloat=1125&aReal=125";
		String putUri= "/clientfloat?id=1&aDouble=1234.56789012345&aFloat=1.7976931348&aReal=123456.78";
		// String putUri= "/clientfloat?id=1&aDouble="+Double.MAX_VALUE + "&aFloat="+Float.MAX_VALUE + "&aReal="+Float.MAX_VALUE;
	    try{
    			assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
    		} catch (Exception e) {
    			fail("ERROR, ClientFloatTest#TestA: " + e.getMessage());
    		} finally {
    			// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientfloat?ADOUBLE="+(Double)1234.56789012345;
    			assertEquals(true, deleteVerify(delUri, null));
    		}
	}

	@Test
	// PUT testing with float value out of range.  This test generates an internal server error.
	public void TestCM() {
		if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
			return;
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 

    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ADOUBLE", Double.MAX_VALUE);
    		verifyMap.put("AFLOAT", (Double)3.40283e38);
    		verifyMap.put("AREAL", Float.MAX_VALUE);
    		
    		// Float value is out of range for MySql float.
    		String putUri= "/clientfloat?ADOUBLE="+Double.MAX_VALUE + "&AFLOAT=3.40283e38&AREAL="+Float.MAX_VALUE;
    	    try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		// 500 Internal Server Error thrown when float value is out-of-range
        		assertEquals(0, e.getMessage().compareToIgnoreCase("500 Internal Server Error"));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientfloat?ADOUBLE="+Double.MAX_VALUE;
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	// PUT testing with float value out of range.  This test generates an internal server error.
	public void TestCO() {
		if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
			return;
		}
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 

    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ADOUBLE", Double.MAX_VALUE);
    		verifyMap.put("AFLOAT", (Double)3.40283e38);
    		verifyMap.put("AREAL", Float.MAX_VALUE);
    		
    		// Float value is out of range for MySql float.
    		String putUri= "/clientfloat?ADOUBLE="+Double.MAX_VALUE + "&AFLOAT=3.40283e38&AREAL="+Float.MAX_VALUE;
    	    try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		// 500 Internal Server Error thrown when float value is out-of-range
        		assertEquals(0, e.getMessage().compareToIgnoreCase("500 Internal Server Error"));
        }
	}

	@Test
	// POST test with parameters specified in the URI.
	public void TestD() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 

    		// Primary key "ID" has an unknown value, so it is not added to the verification map.
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ADOUBLE",123.456);
    		verifyMap.put("AFLOAT", (Double)789.101);
    		verifyMap.put("AREAL", 1213.1415);
    		
    		// Float value is out of range for MySql float.
    		String putUri= "/clientfloat?ADOUBLE=123.456&AFLOAT=789.101&AREAL=1213.1415";
    	    try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.POST, verifyMap));
        } catch (Exception e) {
        		fail("ERROR, ClientFloatTest#TestD: " + e.getMessage());
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientfloat?ADOUBLE=123.456";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}
	
	@Test
	  // POST test with values specified in a string and content type set to URLENCODED.
	  public void TestE(){
	    HttpEntity<String> entity;
	    HttpHeaders hdr = new HttpHeaders();

	    hdr.add("User-Agent", "macosx");
	    hdr.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	    
    		String params = "ADOUBLE=123.456&AFLOAT=789.101&AREAL=1213.1415";
	    
	    entity = new HttpEntity<String>(params, hdr);
	    
	    Map<String, Object> verifyMap=new HashMap<String, Object>();
    		verifyMap.put("ADOUBLE",123.456);
    		verifyMap.put("AFLOAT", (Double)789.101);
    		verifyMap.put("AREAL", 1213.1415);
	        
	    try{
	    		assertEquals(true, putPostVerify(entity, "/clientfloat", HttpMethod.POST, verifyMap));
	    } catch (Exception e) {
        		fail("ERROR, ClientFloatTest#TestE: " + e.getMessage());
	    } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientfloat?ADOUBLE=123.456";
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
    		form.add("ADOUBLE","123.456");
    		form.add("AFLOAT", "789.101");
    		form.add("AREAL", "1213.1415");

	    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(form, headers);
	    
	    Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ADOUBLE",123.456);
    		verifyMap.put("AFLOAT", 789.101);
    		verifyMap.put("AREAL", 1213.1415);

	    try{
	    		assertEquals(true, putPostVerify(entity, "/clientfloat",HttpMethod.POST, verifyMap));
	    } catch (Exception e) {
	    		assertEquals(true, e.getMessage().equalsIgnoreCase("500 Internal Server Error"));
	    } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clientfloat?ADOUBLE=123.456";
        		assertEquals(true, deleteVerify(delUri, null));
	    }
	  }
}
