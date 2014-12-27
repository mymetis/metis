
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

// PUT tests based on not all field information being sent to Metis
//    TestA() - PUT partial row data within the URI.
//    TestB() - PUT partial row data with content-type specified as URLENCODED and data
//                   specified in the HTTP entity as string.
//    TestC()
//    TestD() - Both tests verify that passing parameters in the HTTP entity body is not
//             		  supported by Tomcat.
//
//    test-servlet.xml PUT SQL contents:
//      <property name="sqls4Put">
//        <list>
//          <value>insert into car (MAKE, MODEL, MPG) values (`varchar:MAKE`,`varchar:MODEL`,`integer:MPG`)</value>
//          <value>insert into car values (`integer:id`,`varchar:MAKE`,`varchar:MODEL`,`integer:MPG`)</value>
//        </list>
//      </property>
public class ClientPutTest extends ClientTest{
  
  @Test
  // PUT partial table row data specified in the URI.  Corresponding SQL statement is:
  //
  // insert into car (MAKE, MODEL, MPG) values (`varchar:MAKE`,`varchar:MODEL`,`integer:MPG`)
  //
  // The primary key is the column ID which is auto-increment.  The result of this insert is a 
  // row of data being inserted into the table.  Inserted data is verified to be inserted.
  public void TestA(){
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", "macosx");
    HttpEntity<String> entity= new HttpEntity<String>(headers );  

    // Primary key "ID" has an unknown value, so it is not added to the verification map.
    Map<String,Object> verifyMap=new HashMap<String,Object>();
    verifyMap.put("MAKE", "toyota");
    verifyMap.put("MODEL",  "tercel");
    verifyMap.put("MPG", 30);
        
    try{
    		assertEquals(true, putPostVerify(entity, "/clientput?ID=1&MAKE=toyota&MODEL=tercel&MPG=30", HttpMethod.PUT, verifyMap));
        
    		// Cleanup the inserted data table.  Not interested in verify the deletion hence the "null"
    		assertEquals(true, deleteVerify("/clientput?MAKE=toyota",null));
    } catch (Exception e) {
    		assertEquals(true, deleteVerify("/clientput?MAKE=toyota",null));
    		fail("ERROR, ClientPutTest#TestA: " + e.getMessage());
    }
  }

  @Test
  // Insert data into the car table by specifying all column values including the primary key.
  // Corresponding SQL statement is:
  //
  // insert into car values (`integer:id`,`varchar:MAKE`,`varchar:MODEL`,`integer:MPG`)
  //
  //  Inserted data is verified to be inserted.
  public void TestBM(){
	if (getDbmsname().equalsIgnoreCase(MYSQL) == false){
		return;
	}
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", "macosx");
    HttpEntity<String> entity= new HttpEntity<String>(headers );  

    // Primary key "ID" has a value of 11, so we add it to the map to verify
    Map<String,Object> verifyMap=new HashMap<String,Object>();
    verifyMap.put("ID", 11);
    verifyMap.put("MAKE", "bmw");
    verifyMap.put("MODEL",  "R1150R");
    verifyMap.put("MPG", 45);
        
    try{
    		assertEquals(true, putPostVerify(entity, "/clientput?ID=11&MAKE=bmw&MODEL=R1150R&MPG=45", HttpMethod.PUT, verifyMap));
    		assertEquals(true, deleteVerify("/clientput?MAKE=bmw",null));
    } catch(Exception e) {
    		assertEquals(true, deleteVerify("/clientput?MAKE=bmw",null));
    		fail("ERROR, ClientPutTest#TestB: " + e.getMessage());
    }
  }

  @Test
  // Insert data into the car table by specifying all column values including the primary key.
  // Corresponding SQL statement is:
  //
  // insert into car values (`integer:id`,`varchar:MAKE`,`varchar:MODEL`,`integer:MPG`)
  //
  //  Inserted data is verified to be inserted.
  public void TestBO(){
	if (getDbmsname().equalsIgnoreCase(ORACLE) == false){
		return;
	}
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", "macosx");
    HttpEntity<String> entity= new HttpEntity<String>(headers );  

    // Primary key "ID" has a value of 11, so we add it to the map to verify
    Map<String,Object> verifyMap=new HashMap<String,Object>();
    verifyMap.put("MAKE", "bmw");
    verifyMap.put("MODEL",  "R1150R");
    verifyMap.put("MPG", 45);
        
    try{
    		assertEquals(true, putPostVerify(entity, "/clientput?ID=1&MAKE=bmw&MODEL=R1150R&MPG=45", HttpMethod.PUT, verifyMap));
    		assertEquals(true, deleteVerify("/clientput?MAKE=bmw",null));
    } catch(Exception e) {
    		assertEquals(true, deleteVerify("/clientput?MAKE=bmw",null));
    		fail("ERROR, ClientPutTest#TestB: " + e.getMessage());
    }
  }
  
  @Test
  // Tomcat doesn't support URLENCODED string parameters passed in the HTTP entity 
  // body when using PUT.  Actually, no parameters passed in the HTTP entity body is
  // supported.  This test verifies that an exception is caught with the 
  // corresponding 500 error message.
  public void TestC(){
    HttpEntity<String> entity;
    HttpHeaders hdr = new HttpHeaders();

    hdr.add("User-Agent", "macosx");
    hdr.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    
    String params = "id=333&MAKE=gmc&MODEL=sierra&MPG=15";
    
    entity = new HttpEntity<String>(params, hdr);
    
    Map<String,Object> verifyMap=new HashMap<String,Object>();
        
    verifyMap.put("ID", 333);
    verifyMap.put("MAKE", "gmc");
    verifyMap.put("MODEL",  "sierra");
    verifyMap.put("MPG", 15);
        
    try{
    		assertEquals(true, putPostVerify(entity, "/clientput", HttpMethod.PUT, verifyMap));
    		assertEquals(true, deleteVerify("/clientput?MAKE=gmc",null));
    } catch (Exception e) {
    		assertEquals(true, deleteVerify("/clientput?MAKE=gmc",null));
    		assertEquals(true, e.getMessage().equalsIgnoreCase("500 Internal Server Error"));
    }
  }
  
  @Test
  // Tomcat doesn't support URLENCODED MultiValueMap parameters passed in the HTTP 
  // entity body when using PUT.  Actually, no parameters passed in the HTTP entity body
  // is supported.  This test verifies that an exception is caught with the corresponding 
  // 500 error message.
  public void TestD(){
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
    		assertEquals(true, putPostVerify(entity, "/clientput",HttpMethod.PUT, verifyMap));
    		assertEquals(true, deleteVerify("/clientput?MAKE=dodge",null));
    } catch (Exception e) {
    		assertEquals(true, deleteVerify("/clientput?MAKE=dodge",null));
    		assertEquals(true, e.getMessage().equalsIgnoreCase("500 Internal Server Error"));
    }
  }
}
