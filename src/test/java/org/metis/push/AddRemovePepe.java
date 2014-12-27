package org.metis.push;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import java.sql.Date;
import java.util.concurrent.CountDownLatch;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.http.*;
import org.metis.ClientTest;

/**
 * This class add a student called Pepe to and from the student table. It is
 * used for testing the push module's processing of null result sets.
 * 
 * @author jfernandez
 * 
 */
public class AddRemovePepe extends ClientTest implements Runnable {

	private long waitTime;
	private CountDownLatch latch;

	public AddRemovePepe(CountDownLatch latch, long waitTime) throws Exception {
		// first init the base class
		super();
		initialize();
		setWaitTime(waitTime);
		setLatch(latch);
	}

	public void run() {

		// wait specified amount of time before proceeding
		try {
			TimeUnit.SECONDS.sleep(waitTime);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// add Pepe to the database
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		Map<String, Object> pMap = new HashMap<String, Object>();
		ResponseEntity<String> result = null;

		pMap.put("stno", Integer.valueOf(888));
		pMap.put("sname", "Pepe");
		pMap.put("major", "MATH");
		pMap.put("class", Integer.valueOf(4));
		pMap.put("bdate", Date.valueOf("1980-08-08"));
		mapList.add(pMap);
		HttpHeaders headers = new HttpHeaders();
		// specify the content type for the entity body
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("User-Agent", "macosx");
		HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<List<Map<String, Object>>>(
				mapList, headers);
		try {
			result = getRestTemplate().exchange(getServerurl() + "/student",
					HttpMethod.PUT, entity, String.class);
			if (result.getStatusCode().value() != SC_CREATED) {
				System.out.println("UpdateStudents: this error was returned "
						+ "when adding student: "
						+ result.getStatusCode().value());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// wait 3 seconds and remove the student that we just added
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (Exception ignore) {
		}

		headers.clear();
		headers.set("User-Agent", "macosx");
		HttpEntity<String> entity2 = new HttpEntity<String>(headers);
		entity2 = new HttpEntity<String>(headers);
		try {
			result = getRestTemplate().exchange(
					getServerurl() + "/student?stno="
							+ Integer.valueOf(888).toString(),
					HttpMethod.DELETE, entity2, String.class);
			if (result.getStatusCode().value() != SC_NO_CONTENT) {
				System.out
						.println("UpdateStudents: got this unexpected return "
								+ "status when deleting student: "
								+ result.getStatusCode().value());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// tell parent we're done
		getLatch().countDown();

	}

	public long getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(long waitTime) {
		this.waitTime = waitTime;
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}

}
