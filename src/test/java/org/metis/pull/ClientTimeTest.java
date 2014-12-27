package org.metis.pull;

import static org.junit.Assert.*;

import java.sql.Timestamp;
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

// Class to exercise PUT and POST tests using time, timestamp and date.
//
// create table timeDate(
//		id 			int not null auto_increment,
//		ATIME		time,
//		ATIMESTAMP	timestamp,
//		ADATE		date,
//	 primary key(id));
//
//	 time:	(hh:mm:ss)
//	 timestamp:	(YYYYMMDDhhmmss)
//	 date:  (YYYY-MM-DD)
//

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientTimeTest extends ClientTest{

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
	    
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATIME", "11:22:33");

    		// Value returned from MySQL is in milliseconds.
    		Timestamp ts = Timestamp.valueOf("1999-01-02 03:04:05");
    		verifyMap.put("ATIMESTAMP", ts.getTime());
	
    		verifyMap.put("ADATE", "1999-08-20");
    		
    		String putUri= "/clienttime?id=1&ATIME=11:22:33&ATIMESTAMP=1999-01-02 03:04:05&ADATE=1999-08-20";

    		try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		fail("ERROR, ClientTimeTest#TestA: " + e.getMessage());
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clienttime?ADATE=1999-08-20";
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
	    
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATIME", "10/11/2014");
    		verifyMap.put("ATIMESTAMP", "09/13/2014");
    		verifyMap.put("ADATE", "08/12/2014");
    		
    		// String putUri= "/clienttime?id=1&ATIME=11:22:33&ATIMESTAMP=1999-01-02 03:04:05&ADATE=1999-08-20";
    		String putUri= "/clienttime?id=1&adate1=2014-10-11&aFormat1=yyyy/mm/dd&adate2=2014-09-13&aFormat2=yyyy/mm/dd&adate3=2014-08-12&aFormat3=yyyy/mm/dd";

    		try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		fail("ERROR, ClientTimeTest#TestA: " + e.getMessage());
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clienttime";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	// PUT negative testing.  Timestamp value is a bogus year.
	public void TestBM() {

	    if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
				return;
	    }
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 

    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATIME", "11:22:33");

    		// Value returned from MySQL is in milliseconds.
    		Timestamp ts = Timestamp.valueOf("1999-01-02 03:04:05");
    		verifyMap.put("ATIMESTAMP", ts.getTime());
	
    		verifyMap.put("ADATE", "1999-08-20");
    		
    		String putUri= "/clienttime?id=1&ATIME=25:22:33&ATIMESTAMP=2039-01-02 03:04:05&ADATE=1999-08-20";

    		try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		// 500 Internal Server Error because year 2039 is beyond the max legal value which is 2038
        		assertEquals(0, e.getMessage().compareToIgnoreCase("500 Internal Server Error"));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clienttime?ADATE=1999-08-20";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	// PUT negative testing.  Timestamp value is a bogus year.
	public void TestBO() {
	    if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
				return;
	    }
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 

    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATIME", "11:22:33");

    		// Value returned from MySQL is in milliseconds.
    		Timestamp ts = Timestamp.valueOf("1999-01-02 03:04:05");
    		verifyMap.put("ATIMESTAMP", ts.getTime());
	
    		verifyMap.put("ADATE", "1999-08-20");
    		
    		String putUri= "/clienttime?id=1&ATIME=25:22:33&ATIMESTAMP=2039-01-02 03:04:05&ADATE=1999-08-20";

    		try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		// 500 Internal Server Error because year 2039 is beyond the max legal value which is 2038
        		assertEquals(0, e.getMessage().compareToIgnoreCase("500 Internal Server Error"));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clienttime";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	// PUT negative testing.   Date value is a bogus year.
	public void TestCM() {
	    if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
				return;
	    }
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 

    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATIME", "11:22:33");

    		// Value returned from MySQL is in milliseconds.
    		Timestamp ts = Timestamp.valueOf("1999-01-02 03:04:05");
    		verifyMap.put("ATIMESTAMP", ts.getTime());
	
    		verifyMap.put("ADATE", "1999-08-20");
    		
    		String putUri= "/clienttime?id=1&ATIME=25:22:33&ATIMESTAMP=1999-01-02 03:04:05&ADATE=39-08-20";

    		try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		// 500 Internal Server Error because year 2039 is beyond the max legal value which is 2038
        		assertEquals(0, e.getMessage().compareToIgnoreCase("500 Internal Server Error"));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clienttime?ADATE=1999-08-20";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	// PUT negative testing.   Date value is a bogus year.
	public void TestCO() {
	    if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
				return;
	    }
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 

    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATIME", "11:22:33");

    		// Value returned from MySQL is in milliseconds.
    		Timestamp ts = Timestamp.valueOf("1999-01-02 03:04:05");
    		verifyMap.put("ATIMESTAMP", ts.getTime());
	
    		verifyMap.put("ADATE", "1999-08-20");
    		
    		String putUri= "/clienttime?id=1&ATIME=25:22:33&ATIMESTAMP=1999-01-02 03:04:05&ADATE=39-08-20";

    		try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		// 500 Internal Server Error because year 2039 is beyond the max legal value which is 2038
        		assertEquals(0, e.getMessage().compareToIgnoreCase("500 Internal Server Error"));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clienttime";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	// PUT negative testing.  Time value is a bogus.
	public void TestDM() {
	    if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
				return;
	    }
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 

    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATIME", "11:22:33");

    		// Value returned from MySQL is in milliseconds.
    		Timestamp ts = Timestamp.valueOf("1999-01-02 03:04:05");
    		verifyMap.put("ATIMESTAMP", ts.getTime());
	
    		verifyMap.put("ADATE", "1999-08-20");
    		
    		String putUri= "/clienttime?id=1&ATIME=x:22:33&ATIMESTAMP=2039-01-02 03:04:05&ADATE=1999-08-20";

    		try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		// 500 Internal Server Error because year 2039 is beyond the max legal value which is 2038
        		assertEquals(0, e.getMessage().compareToIgnoreCase("500 Internal Server Error"));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clienttime?ADATE=1999-08-20";
        		assertEquals(true, deleteVerify(delUri, null));
        }
	}

	@Test
	// PUT negative testing.  Time value is a bogus.
	public void TestDO() {
	    if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
				return;
	    }
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", "macosx");
	    HttpEntity<String> entity= new HttpEntity<String>(headers ); 

    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATIME", "11:22:33");

    		// Value returned from MySQL is in milliseconds.
    		Timestamp ts = Timestamp.valueOf("1999-01-02 03:04:05");
    		verifyMap.put("ATIMESTAMP", ts.getTime());
	
    		verifyMap.put("ADATE", "1999-08-20");
    		
    		String putUri= "/clienttime?id=1&ATIME=x:22:33&ATIMESTAMP=2039-01-02 03:04:05&ADATE=1999-08-20";

    		try{
        		assertEquals(true, putPostVerify(entity, putUri , HttpMethod.PUT, verifyMap));
        } catch (Exception e) {
        		// 500 Internal Server Error because year 2039 is beyond the max legal value which is 2038
        		assertEquals(0, e.getMessage().compareToIgnoreCase("500 Internal Server Error"));
        } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clienttime";
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
	    
    		String params = "ATIME=11:22:33&ATIMESTAMP=1999-01-02 03:04:05&ADATE=1999-08-20";
	    
	    entity = new HttpEntity<String>(params, hdr);
	    
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATIME", "11:22:33");

    		// Value returned from MySQL is in milliseconds.
    		Timestamp ts = Timestamp.valueOf("1999-01-02 03:04:05");
    		verifyMap.put("ATIMESTAMP", ts.getTime());
	
    		verifyMap.put("ADATE", "1999-08-20");
	        
	    try{
	    		assertEquals(true, putPostVerify(entity, "/clienttime", HttpMethod.POST, verifyMap));
	    } catch (Exception e) {
        		fail("ERROR, ClientTimeTest#TestE: " + e.getMessage());
	    } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clienttime?ADATE=1999-08-20";
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
	    
    		String params= "adate1=2014-10-11&aFormat1=yyyy/mm/dd&adate2=2014-09-13&aFormat2=yyyy/mm/dd&adate3=2014-08-12&aFormat3=yyyy/mm/dd";
	    
	    entity = new HttpEntity<String>(params, hdr);
	    
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATIME", "10/11/2014");
    		verifyMap.put("ATIMESTAMP", "09/13/2014");
    		verifyMap.put("ADATE", "08/12/2014");
	        
	    try{
	    		assertEquals(true, putPostVerify(entity, "/clienttime", HttpMethod.POST, verifyMap));
	    } catch (Exception e) {
        		fail("ERROR, ClientTimeTest#TestE: " + e.getMessage());
	    } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clienttime";
        		assertEquals(true, deleteVerify(delUri, null));
	    }
	  }
	  
	  @Test
	  // POST test with values specified in a MultiValueMap and content type set to URLENCODED.
	  public void TestFM(){

	    if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
				return;
	    }
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	    headers.set("User-Agent", "macosx");

	    MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
    		form.add("ATIME", "11:22:33");
    		form.add("ATIMESTAMP", "1999-01-02 03:04:05");
    		form.add("ADATE", "1999-08-20");

	    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(form, headers);
	    
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATIME", "11:22:33");

    		// Value returned from MySQL is in milliseconds.
    		Timestamp ts = Timestamp.valueOf("1999-01-02 03:04:05");
    		verifyMap.put("ATIMESTAMP", ts.getTime());
	
    		verifyMap.put("ADATE", "1999-08-20");

	    try{
	    		assertEquals(true, putPostVerify(entity, "/clienttime",HttpMethod.POST, verifyMap));
	    } catch (Exception e) {
        		fail("ERROR, ClientTimeTest#TestF: " + e.getMessage());
	    } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clienttime?ADATE=1999-08-20";
        		assertEquals(true, deleteVerify(delUri, null));
	    }
	  }

	  @Test
	  // POST test with values specified in a MultiValueMap and content type set to URLENCODED.
	  public void TestFO(){
	    if (getDbmsname().equalsIgnoreCase("SKIP") == false){
				return;
	    }
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	    headers.set("User-Agent", "macosx");

	    MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
    		form.add("ATIME", "TO_DATE('2014-10-11', 'yyyy/mm/dd')");
    		form.add("ATIMESTAMP", "TO_DATE('2014-09-13', 'yyyy/mm/dd')");
    		form.add("ADATE", "TO_DATE('2014-08-12', 'yyyy/mm/dd')");

	    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(form, headers);
	    
    		Map<String,Object> verifyMap=new HashMap<String,Object>();
    		verifyMap.put("ATIME", "10/11/2014");
    		verifyMap.put("ATIMESTAMP", "09/13/2014");
    		verifyMap.put("ADATE", "08/12/2014");

    		// Value returned from MySQL is in milliseconds.
    		Timestamp ts = Timestamp.valueOf("1999-01-02 03:04:05");
    		verifyMap.put("ATIMESTAMP", ts.getTime());
	
    		verifyMap.put("ADATE", "1999-08-20");

	    try{
	    		assertEquals(true, putPostVerify(entity, "/clienttime",HttpMethod.POST, verifyMap));
	    } catch (Exception e) {
        		fail("ERROR, ClientTimeTest#TestF: " + e.getMessage());
	    } finally {
        		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
        		String delUri= "/clienttime";
        		assertEquals(true, deleteVerify(delUri, null));
	    }
	  }
}
