package org.metis.pull;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.metis.ClientTest;

// POST tests based on not all field information being sent to Metis
//    TestA() - POST partial row data within the URI.
//    TestB() - POST partial row data with content-type specified as URLENCODED and data
//                    specified in the HTTP entity as string.
//    TestC() - POST partial row data with content-type specified as URLENCODED and data
//             specified as MultiValueMap.
//    TestD() - POST *all* row data within the URI.  This test is not the POST specification
//             but is performed anyways.
//    TestE() - POST *all* row data within a URLENCODED HTTP entity body.  This test is not
//             the POST specification but is performed anyways.
//
//    test-servlet.xml POST SQL contents:
//      <property name="sqls4Post">
//        <list>
//          <value>insert into car (MAKE, MODEL, MPG) values (`varchar:MAKE`,`varchar:MODEL`,`integer:MPG`)</value>
//          <value>insert into car values (`integer:id`,`varchar:MAKE`,`varchar:MODEL`,`integer:MPG`)</value>
//        </list>
//      </property>
public class ClientPostTest extends ClientTest{
  
  @Test
  // POST partial table row data specified in the URI
  public void TestA(){
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", "macosx");
    HttpEntity<String> entity= new HttpEntity<String>(headers );  

    Map<String,Object> verifyMap=new HashMap<String,Object>();
        
    verifyMap.put("MAKE", "toyota");
    verifyMap.put("MODEL",  "tercel");
    verifyMap.put("MPG", 30);
        
    try{
    		assertEquals(true, putPostVerify(entity, "/clientpost?MAKE=toyota&MODEL=tercel&MPG=30", HttpMethod.POST, verifyMap));
        
    		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
    		assertEquals(true, deleteVerify("/clientpost?MAKE=toyota",null));
    } catch (Exception e){
    		fail("ERROR, ClientPostTest#TestA(): " + e.getMessage());
    }
  }
  
  @Test
  // POST partial table row data specified as string in the HTTP entity and content-type set to URLENCODED
  public void TestB(){
    HttpEntity<String> entity;
    HttpHeaders hdr = new HttpHeaders();

    hdr.add("User-Agent", "macosx");
    hdr.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    
    String params = "MAKE=gmc&MODEL=sierra&MPG=15";
    
    entity = new HttpEntity<String>(params, hdr);
    
    Map<String,Object> verifyMap=new HashMap<String,Object>();
        
    verifyMap.put("MAKE", "gmc");
    verifyMap.put("MODEL",  "sierra");
    verifyMap.put("MPG", 15);
        
    try{
    		assertEquals(true, putPostVerify(entity, "/clientpost", HttpMethod.POST, verifyMap));
    		assertEquals(true, deleteVerify("/clientpost?MAKE=gmc",null));
    } catch (Exception e){
    		fail("ERROR, ClientPostTest#TestB(): " + e.getMessage());
    }
  }
  
  @Test
  // POST partial table row data specified as a MultiValueMap in the HTTP entity and content-type set to URLENCODED
  public void TestC(){
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.set("User-Agent", "macosx");

    MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
    form.add("MAKE", "dodge");
    form.add("MODEL", "viper");
    form.add("MPG", "5");

    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(form, headers);
    
    Map<String,Object> verifyMap=new HashMap<String,Object>();
          
    verifyMap.put("MAKE", "dodge");
    verifyMap.put("MODEL",  "viper");
    verifyMap.put("MPG", 5);

    try{
    		assertEquals(true, putPostVerify(entity, "/clientpost",HttpMethod.POST, verifyMap));
    		assertEquals(true, deleteVerify("/clientpost?MAKE=dodge",null));
    } catch (Exception e){
    		fail("ERROR, ClientPostTest#TestC(): " + e.getMessage());
    }
  }

  @Test
  // Insert data into the car table by specifying all column values including the primary key
  // (via a String HTTP entity)and verifying that the data got inserted.
  public void TestDM(){
	if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
		return;
	}
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", "macosx");
    HttpEntity<String> entity= new HttpEntity<String>(headers );  

    Map<String,Object> verifyMap=new HashMap<String,Object>();
    verifyMap.put("ID", 11);
    verifyMap.put("MAKE", "bmw");
    verifyMap.put("MODEL",  "R1150R");
    verifyMap.put("MPG", 45);
        
    try{
    		assertEquals(true, putPostVerify(entity, "/clientpost?ID=11&MAKE=bmw&MODEL=R1150R&MPG=45", HttpMethod.POST, verifyMap));
    		assertEquals(true, deleteVerify("/clientpost?MAKE=bmw",null));
    } catch (Exception e){
    		fail("ERROR, ClientPostTest#TestD(): " + e.getMessage());
    }
  }

  @Test
  // Insert data into the car table by specifying all column values including the primary key
  // (via a String HTTP entity)and verifying that the data got inserted.
  public void TestDO(){
	if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
		return;
	}
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", "macosx");
    HttpEntity<String> entity= new HttpEntity<String>(headers );  

    Map<String,Object> verifyMap=new HashMap<String,Object>();
    verifyMap.put("MAKE", "bmw");
    verifyMap.put("MODEL",  "R1150R");
    verifyMap.put("MPG", 45);
        
    try{
    		assertEquals(true, putPostVerify(entity, "/clientpost?MAKE=bmw&MODEL=R1150R&MPG=45", HttpMethod.POST, verifyMap));
    		assertEquals(true, deleteVerify("/clientpost?MAKE=bmw",null));
    } catch (Exception e){
    		fail("ERROR, ClientPostTest#TestD(): " + e.getMessage());
    }
  }

  @Test
  // Insert data into the car table by specifying all column values including the primary key
  // (via a URLENCODED HTTP entity)and verifying that the data got inserted.
  public void TestEM(){
	if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
		return;
	}
    HttpEntity<String> entity;
    HttpHeaders hdr = new HttpHeaders();

    hdr.add("User-Agent", "macosx");
    hdr.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    
    String params = "id=123&MAKE=gmc&MODEL=sierra&MPG=15";
    
    entity = new HttpEntity<String>(params, hdr);
    
    Map<String,Object> verifyMap=new HashMap<String,Object>();
        
    verifyMap.put("ID", 123);
    verifyMap.put("MAKE", "gmc");
    verifyMap.put("MODEL",  "sierra");
    verifyMap.put("MPG", 15);
        
    try{
    		assertEquals(true, putPostVerify(entity, "/clientpost", HttpMethod.POST, verifyMap));
    		assertEquals(true, deleteVerify("/clientpost?MAKE=gmc",null));
    } catch (Exception e){
    		fail("ERROR, ClientPostTest#TestC(): " + e.getMessage());
    }
  }

  @Test
  // Insert data into the car table by specifying all column values including the primary key
  // (via a URLENCODED HTTP entity)and verifying that the data got inserted.
  public void TestEO(){
	if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
		return;
	}
    HttpEntity<String> entity;
    HttpHeaders hdr = new HttpHeaders();

    hdr.add("User-Agent", "macosx");
    hdr.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    
    String params = "MAKE=gmc&MODEL=sierra&MPG=15";
    
    entity = new HttpEntity<String>(params, hdr);
    
    Map<String,Object> verifyMap=new HashMap<String,Object>();
        
    verifyMap.put("MAKE", "gmc");
    verifyMap.put("MODEL",  "sierra");
    verifyMap.put("MPG", 15);
        
    try{
    		assertEquals(true, putPostVerify(entity, "/clientpost", HttpMethod.POST, verifyMap));
    		assertEquals(true, deleteVerify("/clientpost?MAKE=gmc",null));
    } catch (Exception e){
    		fail("ERROR, ClientPostTest#TestC(): " + e.getMessage());
    }
  }
}
