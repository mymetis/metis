package org.metis;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.metis.pull.WdsResourceBean;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.BeansException;
import org.metis.sql.SqlStmnt;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Reads in a test Spring context file and runs some test based on the file.
 * 
 */

// Test methods will be executed in ascending order by name
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SimpleContextTest {

	static private ApplicationContext context = null;

	private Map<String, WdsResourceBean> rdbs = null;

	@BeforeClass
	public static void initialize() throws Exception {
		// create our context file
		try {
			context = new ClassPathXmlApplicationContext("mysql-servlet.xml");
		} catch (BeansException be) {
			System.out
					.println("ERROR: unable to load spring context, got this exception: \n"
							+ be.getLocalizedMessage());
			be.printStackTrace();
		}
	}

	@Test
	public void TestA() {

		if (context == null) {
			fail("ERROR: unable to load context");
		}

		// get list of all RDBs for this application context
		rdbs = context.getBeansOfType(WdsResourceBean.class);
		if (rdbs.isEmpty()) {
			fail("ERROR: no rdbs defined for this context");
		}

		WdsResourceBean rdb = null;
		try {
			rdb = context.getBean("usersrdb", WdsResourceBean.class);
		} catch (BeansException be) {
			fail("ERROR: unable to get usersrdb: " + be.getLocalizedMessage());
		}
		assertEquals(true, rdb.isSecure());
		// there should be more than one SQL statement for GET
		assertEquals(true, rdb.getSqls4Get().size() > 1);
		assertEquals(true, rdb.getAllowedAgents().get(0).equals("foobardevice"));
		boolean passed = false;
		for (String sql : rdb.getSqls4Get()) {
			// System.out.println("sql = [" + sql + "]");
			if (sql.startsWith("select") && sql.endsWith("'%'")
					&& sql.indexOf("last") > 0) {
				passed = true;
				break;
			}
		}
		assertEquals(true, passed);

		try {
			rdb = context.getBean("ticketsrdb", WdsResourceBean.class);
		} catch (BeansException be) {
			fail("ERROR: unable to get ticketsrdb: " + be.getLocalizedMessage());
		}
		assertEquals(false, rdb.isSecure());
		// there should be more than one SQL statement for GET
		assertEquals(true, rdb.getSqls4Get().size() > 1);
		assertEquals(true, rdb.getNotAllowedAgents().get(0).equals("fooagent1"));
		assertEquals(true, rdb.getNotAllowedAgents().get(1).equals("fooagent2"));
	}

	@Test
	public void TestB() {

		WdsResourceBean rdb = null;
		try {
			rdb = context.getBean("usersrdb", WdsResourceBean.class);
		} catch (BeansException be) {
			fail("ERROR: unable to get usersrdb: " + be.getLocalizedMessage());
		}

		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this exception for usersrdb afterPropertiesSet: "
					+ e.getLocalizedMessage());
		}

		// Get the list of SQL statements assigned to this RDB
		List<SqlStmnt> list = rdb.getSqlStmnts4Get();
		// Ensure that a list is returned and that it has more than one
		// statement
		assertEquals(false, list == null);
		assertEquals(true, list.size() > 1);

		// Create a set of keys and test the getMatch method, which returns the
		// statement that contains a statement whose key fields match the given
		// keys. A null is returned if there is no match
		Set<String> keys = new HashSet<String>();
		keys.add("caca");
		keys.add("id");
		// this first test should return null
		assertEquals(true, SqlStmnt.getMatch(list, keys) == null);
		keys.remove("caca");
		// now we should get back a statement for the rest of the
		// tests
		assertEquals(true, SqlStmnt.getMatch(list, keys) != null);
		keys.remove("id");
		keys.add("first");
		assertEquals(true, SqlStmnt.getMatch(list, keys) != null);
		keys.add("last");
		assertEquals(true, SqlStmnt.getMatch(list, keys) != null);

	}

	@Test
	public void TestC() {

		if (context == null) {
			fail("ERROR: unable to load context");
		}

		// get list of all RDBs for this application context
		rdbs = context.getBeansOfType(WdsResourceBean.class);
		if (rdbs.isEmpty()) {
			fail("ERROR: no rdbs defined for this context");
		}

		WdsResourceBean rdb = null;
		try {
			rdb = context.getBean("usersrdb", WdsResourceBean.class);
		} catch (BeansException be) {
			fail("ERROR: unable to get usersrdb: " + be.getLocalizedMessage());
		}

		DataSource ds = rdb.getDataSource();
		assertEquals(true, ds != null);
		Connection con = null;
		// test to see if the RDB can establish a connection to the DB
		try {
			con = ds.getConnection();
			//System.out.println(Utils.getDriverMetaData(con));
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
			fail("ERROR: got this exception when try to get DB connection via usersrdb: "
					+ e.getLocalizedMessage());
		}

	}

}
