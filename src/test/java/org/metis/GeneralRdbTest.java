package org.metis;

import org.junit.BeforeClass;
import org.junit.Test;
import org.metis.pull.WdsResourceBean;
import org.metis.jdbc.*;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;
import java.util.*;

/**
 * Runs some misc/general tests against the WdsResourceBean class.
 * 
 * @author jfernandez
 * 
 */

// Test methods will be executed in ascending order by name
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GeneralRdbTest {

	// this will be the RDB that we test against.
	static WdsResourceBean rdb;

	@BeforeClass
	public static void initialize() throws Exception {
		// create our test rdb
		rdb = new WdsResourceBean();
	}

	@Test
	public void TestA() {
		// make sure we have a rdb and run some simple tests against it
		assertEquals(true, (rdb != null));

		try {
			rdb.setDataSource(new DummyDataSource());
		} catch (Exception e) {
			fail("This exception caught when setting dummy datasource: "
					+ e.getLocalizedMessage());

		}

		// test security mutator methods
		assertEquals(true, rdb.getAuthenticated() == null);
		assertEquals(true, rdb.getSecure() == null);
		rdb.setAuthenticated(true);
		rdb.setSecure(true);
		assertEquals(true, rdb.isAuthenticated());
		assertEquals(true, rdb.isSecure());

		// test misc mutator methods
		rdb.setBeanName("testBean");
		assertEquals(true, rdb.getBeanName().equals("testBean"));

		rdb.setAgentNames("fooDevice");
		// agent names are converted to lower case, so verify
		// that comparison of string with upper case character
		// returns false and and verify comparison with lower
		// case string returns true
		assertEquals(false, rdb.getAllowedAgents().get(0).equals("fooDevice"));
		assertEquals(true, rdb.getAllowedAgents().get(0).equals("foodevice"));

		rdb.setContentType("application/json");
		assertEquals(true, rdb.getContentType().equals("application/json"));

		try {
			rdb.setCharSet("utf-8");
			rdb.setCharSet("us-ascii");
			rdb.setCharSet("utf-16");
		} catch (IllegalArgumentException ignore) {
			fail("ERROR: got IllegalArgumentException on valid setCharSet test");
		}

		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception on invalid afterPropertiesSet test");
		} catch (Exception ignore) {
		}

		// misc exception tests
		try {
			rdb.setCharSet("utf-9");
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		ArrayList<String> list = new ArrayList<String>();
		list.add("insert * from fooTable");
		// get
		try {
			rdb.setSqls4Get(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("delete * from fooTable");
		try {
			rdb.setSqls4Get(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("alter * from fooTable");
		try {
			rdb.setSqls4Get(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("drop * from fooTable");
		try {
			rdb.setSqls4Get(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("select * from fooTable");
		try {
			rdb.setSqls4Get(list);
		} catch (IllegalArgumentException e) {
			fail("ERROR: got IllegalArgumentException on valid setCharSet test");
		}

		list.clear();
		list.add("SELECT * from fooTable");
		try {
			rdb.setSqls4Get(list);
		} catch (IllegalArgumentException e) {
			fail("ERROR: got IllegalArgumentException on valid setCharSet test");
		}

		list.clear();
		list.add("{call foo(`char:last:in`,`char:first:out`)}");
		try {
			rdb.setSqls4Get(list);
		} catch (IllegalArgumentException e) {
			fail("ERROR: got IllegalArgumentException on valid setCharSet test");
		}

		list.clear();
		list.add("{  call foo(`char:last:in`,`char:first:out`) }  ");
		try {
			rdb.setSqls4Get(list);
		} catch (IllegalArgumentException e) {
			fail("ERROR: got IllegalArgumentException on valid setCharSet test");
		}
		list.clear();
		list.add(" {call foo(`char:last:in`,`char:first:out`)}	");
		try {
			rdb.setSqls4Get(list);
		} catch (IllegalArgumentException e) {
			fail("ERROR: got IllegalArgumentException on valid setCharSet test");
		}

		// this should throw an exception because you must have both leading and
		// trailing escape characters for call'ables.
		list.clear();
		list.add("{  call foo(`char:last:in`,`char:first:out`)  ");
		try {
			rdb.setSqls4Get(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		// this should throw an exception because you must have both leading and
		// trailing escape characters for call'ables.
		list.clear();
		list.add("  call foo(`char:last:in`,`char:first:out`)}");
		try {
			rdb.setSqls4Get(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		// post
		list.clear();
		list.add("select * from fooTable");
		try {
			rdb.setSqls4Post(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("delete * from fooTable");
		try {
			rdb.setSqls4Post(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("alter * from fooTable");
		try {
			rdb.setSqls4Post(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("insert into fooTable values ('hello')");
		try {
			rdb.setSqls4Post(list);
		} catch (IllegalArgumentException e) {
			fail("ERROR: got IllegalArgumentException on valid setSql4Post test");
		}

		list.clear();
		list.add(" INSERT into     fooTable values ('hello')  ");
		try {
			rdb.setSqls4Post(list);
		} catch (IllegalArgumentException e) {
			fail("ERROR: got IllegalArgumentException on valid setSql4Post test");
		}

		// put
		list.clear();
		list.add("drop * from fooTable");
		try {
			rdb.setSqls4Put(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("delete * from fooTable");
		try {
			rdb.setSqls4Put(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("alter * from fooTable");
		try {
			rdb.setSqls4Put(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("update fooTable set attr='hello' where attr2=5");
		try {
			rdb.setSqls4Put(list);
		} catch (IllegalArgumentException e) {
			fail("ERROR: got IllegalArgumentException on valid setSql4Put test");
		}

		// delete
		list.clear();
		list.add("insert * from fooTable");
		try {
			rdb.setSqls4Delete(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("drop * from fooTable");
		try {
			rdb.setSqls4Delete(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("alter * from fooTable");
		try {
			rdb.setSqls4Delete(list);
			fail("ERROR: did not get IllegalArgumentException on invalid setCharSet test");
		} catch (IllegalArgumentException ignore) {
		}

		list.clear();
		list.add("delete from fooTable");
		try {
			rdb.setSqls4Delete(list);
		} catch (IllegalArgumentException e) {
			fail("ERROR: got IllegalArgumentException on valid setSql4Delete test");
		}
	}

}
