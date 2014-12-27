package org.metis;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.metis.pull.WdsResourceBean;
import org.metis.sql.SqlStmnt;
import org.metis.sql.SqlToken;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

/**
 * Runs a lot of tests against the creation and validation of the SQL
 * statements.
 * 
 * 
 * @author jfernandez
 * 
 */

// Test methods will be executed in ascending order by name
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SqlStmntTest {

	// this will be the RDB that we test against.
	private static WdsResourceBean rdb;
	private static ArrayList<String> list = new ArrayList<String>();

	@Test
	public void TestA() {
		
		List<SqlStmnt> sqlList = null;

		// this sql should throw an exception because of the empty field
		// definition
		String sql = "select first, lastju	jmih from users where first=``";
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid "
					+ "sql statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException "
						+ "for this invalid sql statement: " + sql);
			}
		}

		// this sql should throw an exception because the field's
		// definition is not fully defined
		sql = "select first from users where first=`integer`";
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for "
						+ "this invalid sql statement: " + sql);
			}
		}

		// this sql should throw an exception because the field's
		// definition is not fully defined
		sql = "select first from users where first=`:id`";
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for "
						+ "this invalid sql statement: " + sql);
			}
		}

		// this sql should throw an exception because the field's
		// definition is not fully defined
		sql = "select first from users where first=`:`";
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid "
					+ "sql statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for "
						+ "this invalid sql statement: " + sql);
			}
		}

		// this sql should throw an exception because the field's
		// definition is not fully defined
		sql = "select first from users where first=`::`";
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid "
					+ "sql statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException "
						+ "for this invalid sql statement: " + sql);
			}
		}

		// this sql should throw an exception because 'caca' is not a valid type
		sql = "select first from users where first=`caca:first`";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for "
						+ "this invalid sql statement: " + sql);
			}
		}

		// this should result in an exception because there are two sql
		// statements with the same 'signature' being assigned to the
		// GET method
		list.clear();
		list.add("select first from users where first=`integer:first`");
		list.add("select last from users where first=`integer:first`");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for duplicate sql test");
		} catch (Exception ignore) {
		}

		// this should result in an exception because there are two sql
		// statements with the same 'signature' being assigned to the
		// GET method
		list.clear();
		list.add("select first from users where first=`integer:first`");
		list.add("select last from users where first=`integer:FIRSt`");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for duplicate sql test");
		} catch (Exception ignore) {
		}

		// ditto
		list.clear();
		list.add("select first from users where first=`integer:first`");
		list.add("call foo(`integer:first:in`");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for duplicate sql test");
		} catch (Exception ignore) {
		}

		// ditto
		list.clear();
		list.add("select first from users where first=`integer:first`");
		list.add("call foo(`integer:FiRst:in`");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for duplicate sql test");
		} catch (Exception ignore) {
		}

		// this should throw an exception because the two sql statements
		// have the same signature. note that the field for the second
		// statement defaults to an OUT, and not an IN, param; therefore it does
		// not have any input params, just like the first one
		list.clear();
		list.add("select first from foo");
		list.add("`nvarchar:name` = call foo()");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for duplicate sql test");
		} catch (Exception ignore) {
		}

		// this should throw an exception because a select statement cannot have
		// a param field with a mode
		sql = "select first from users where first=`char:first:out`";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// same as above, but for insert
		sql = "insert into foo values(`char:first:out`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// same as above, but for delete
		sql = "delete from foo where first like `char:first:out` || '%' ";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Delete(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}
		
	
		// test that anything within single quotes is not touched
		sql = "delete from foo where first like `integer:first` || '%' ";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Delete(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
				fail("ERROR: got this exception on single quote test: " + e.getMessage());
		}
		sqlList = rdb.getSqlStmnts4Delete();
		//System.out.println(sqlList.get(0).getPrepared());
		assertEquals(
				true,
				sqlList.get(0)
						.getPrepared()
						.equals("delete from foo where first like ? || '%'"));
		
		// test that anything within single quotes is not touched
		sql = "delete from foo where first like `integer:first` || '`integer:last`' ";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Delete(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
				fail("ERROR: got this exception on single quote test: " + e.getMessage());
		}
		sqlList = rdb.getSqlStmnts4Delete();
		//System.out.println(sqlList.get(0).getPrepared());
		assertEquals(
				true,
				sqlList.get(0)
						.getPrepared()
						.equals("delete from foo where first like ? || '`integer:last`'"));
		
	
		// this should throw an exception because a call'able cannot have
		// an IN param of type rset
		list.clear();
		list.add("call foo(`rset:FiRst:in`)");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// this should throw an exception because a call'able cannot have
		// an IN param of type rset; note the use of a different mode than in
		// the prior test
		list.clear();
		list.add("call foo(`rset:FiRst:inout`)");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// this should throw an exception because a call'able cannot have
		// an IN param of type cursor
		list.clear();
		list.add("call foo(`cursor:FiRst:in`)");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// this should throw an exception because a non call'able cannot have
		// an IN param of type cursor
		list.clear();
		list.add("select * from users where id = `cursor:FiRst`");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// this should throw an exception because a non call'able cannot have
		// an IN param of type rset
		list.clear();
		list.add("select * from users where id = `rset:FiRst`");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// same as above but for insert
		list.clear();
		list.add("insert into foo values(`rset:first`)");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Post(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// same as above but for put
		list.clear();
		list.add("insert into foo values(`cursor:first`)");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// this should fail because 'function' params cannot have an OUT mode
		sql = "`rset:second` = call foo(`cursor:FiRst:out`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// should not throw an exception because procedures allow OUT params
		sql = "call foo(`cursor:FiRst:out`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}

		// should not throw an exception, for the same reason as above
		sql = "call foo(`cursor:FiRst`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		// confirm that the one param has defaulted to an OUT param
		sqlList = rdb.getSqlStmnts4Get();
		List<SqlToken> tokenList = sqlList.get(0).getSortedKeyTokens();
		assertEquals(true, tokenList.get(0).getMode() == SqlToken.Mode.OUT);

		// same as above, but this time we're using a rset instead of a cursor
		sql = "call foo(`rset:FiRst`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		// confirm that the one param has defaulted to an OUT param
		sqlList = rdb.getSqlStmnts4Get();
		tokenList = sqlList.get(0).getSortedKeyTokens();
		assertEquals(true, tokenList.get(0).getMode() == SqlToken.Mode.OUT);

		// should not throw an exception; the rset params default to OUT mode,
		// while the integer param defaults to IN mode
		sql = "call foo(`rset:FiRst`,`integer:sec`,`rset:third`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		// confirm that the proper default values have been assigned
		sqlList = rdb.getSqlStmnts4Get();
		tokenList = sqlList.get(0).getSortedKeyTokens();
		assertEquals(true, tokenList.get(0).getMode() == SqlToken.Mode.OUT);
		assertEquals(true, tokenList.get(1).getMode() == SqlToken.Mode.IN);
		assertEquals(true, tokenList.get(2).getMode() == SqlToken.Mode.OUT);

		// same as above except that we're using a cursor
		sql = "call foo(`cursor:FiRst`,`integer:sec`,`rset:third`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		// confirm that the proper default values have been assigned
		sqlList = rdb.getSqlStmnts4Get();
		tokenList = sqlList.get(0).getSortedKeyTokens();
		assertEquals(true, tokenList.get(0).getMode() == SqlToken.Mode.OUT);
		assertEquals(true, tokenList.get(1).getMode() == SqlToken.Mode.IN);
		assertEquals(true, tokenList.get(2).getMode() == SqlToken.Mode.OUT);

		// combination of defaults and non defaults
		sql = "call foo(`rset:FiRst`,`integer:sec:inout`,`rset:third:out`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		// confirm that the proper default values have been assigned
		sqlList = rdb.getSqlStmnts4Get();
		tokenList = sqlList.get(0).getSortedKeyTokens();
		assertEquals(true, tokenList.get(0).getMode() == SqlToken.Mode.OUT);
		assertEquals(true, tokenList.get(1).getMode() == SqlToken.Mode.INOUT);
		assertEquals(true, tokenList.get(2).getMode() == SqlToken.Mode.OUT);

		// should not throw an exception, rset types should default to OUT
		sql = "call foo(`rset:FiRst`,`rset:second`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}

		// should throw an exception; rset and cursor are not allowed as
		// function params because they default to OUT mode which is not allowed
		// for functions
		sql = "`rset:second` = call foo(`rset:FiRst`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// should throw an exception; rset and cursor are not allowed as
		// function params
		sql = "`rset:second` = call foo(`cursor:FiRst`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// should not throw an exception; the statement is valid
		sql = "select first, last from users where first = 'wile' and last = "
				+ "'coyote'";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Get();
		assertEquals(true, sqlList != null);
		// it is not a parameterized stmt
		assertEquals(false, sqlList.get(0).isPrepared());
		// ensure proper method(s) are supported
		assertEquals(true, rdb.getSupportedMethods().length == 1);
		assertEquals(true, rdb.getSupportedMethods()[0].equalsIgnoreCase("GET"));

		// should not throw an exception
		sql = "select first from users where first =`char:first`";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Get();
		assertEquals(true, sqlList != null);
		// it is a parameterized stmt
		assertEquals(true, sqlList.get(0).isPrepared());
		assertEquals(true,
				"select first from users where first = ?".equals(sqlList.get(0)
						.getPrepared()));

		// an exception should be thrown because 'caca' is not a valid mode
		sql = "call foo(`char:last:caca`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql statement: "
					+ sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// an exception should be thrown because 'caca' is not a valid mode
		sql = "call foo(`char:last:in`,`char:first:caca`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql statement: "
					+ sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// an exception should be thrown because there are duplicate keys with
		// different modes
		sql = "call foo(`char:last:in`,`char:last:out`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql statement: "
					+ sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// an exception should be thrown because there are duplicate keys with
		// different valid modes
		sql = "call foo(`char:last:in`,`char:last:inout`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for "
						+ "this invalid sql statement: " + sql);
			}
		}

		// should not get an exception for this one
		sql = "call foo(`char:last:in`,`char:first:out`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean(); // create a new RDB for this test case
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Put();
		assertEquals(true, sqlList != null);
		// it is a parameterized stmt
		assertEquals(true, sqlList.get(0).isPrepared());
		assertEquals(true, sqlList.get(0).isCallable());
		assertEquals(true, sqlList.get(0).getStoredProcName().equals("foo"));
		assertEquals(true,
				"call foo( ? , ? )".equals(sqlList.get(0).getPrepared()));
		assertEquals(true, sqlList.get(0).getInTokens().size() == 1);
		// ensure proper method(s) are supported
		assertEquals(true, rdb.getSupportedMethods().length == 1);
		assertEquals(true, rdb.getSupportedMethods()[0].equalsIgnoreCase("PUT"));

		// should not get an exception for this one
		list.clear();
		list.add("call Foo(`char:last:inout`)");
		rdb = new WdsResourceBean(); // create a new RDB for this test case
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Put();
		assertEquals(true, sqlList != null);
		// it is a parameterized stmt
		assertEquals(true, sqlList.get(0).isPrepared());
		assertEquals(true, sqlList.get(0).isCallable());
		assertEquals(true, sqlList.get(0).getStoredProcName().equals("Foo"));
		assertEquals(true, "call Foo( ? )".equals(sqlList.get(0).getPrepared()));
		assertEquals(true, sqlList.get(0).getInTokens().size() == 1);
		// ensure proper method(s) are supported
		assertEquals(true, rdb.getSupportedMethods().length == 1);
		assertEquals(true, rdb.getSupportedMethods()[0].equalsIgnoreCase("PUT"));

		// leading and trailing escape characters are ok for call'ables.
		// add a space between 'foo' and '('
		sql = "{   call foo (`char:last:in`,`char:first:out`) }  ";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Put();
		assertEquals(true, sqlList != null);
		assertEquals(true, sqlList.get(0).isPrepared());
		assertEquals(true, sqlList.get(0).isCallable());
		assertEquals(true, sqlList.get(0).getStoredProcName().equals("foo"));
		assertEquals(true, sqlList.get(0).getInTokens().size() == 1);
		assertEquals(true,
				"call foo ( ? , ? )".equals(sqlList.get(0).getPrepared()));

		// it doesn't make sense to have more than one IN param with the same
		// name for a callable
		sql = "call foo(`char:last:in`,`char:last:in`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql "
					+ "statement: " + sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// this is valid, same input param for a prepared statement is being
		// used in multiple locations
		sql = "select * from users where foo =`char:first` or bar =`char:first`";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Get();
		assertEquals(true, sqlList != null);
		assertEquals(true, sqlList.get(0).getKeyTokens().size() == 1);
		assertEquals(false, sqlList.get(0).isCallable());
		assertEquals(true, sqlList.get(0).isPrepared());
		assertEquals(true,
				"select * from users where foo = ? or bar = ?".equals(sqlList
						.get(0).getPrepared()));
		assertEquals(true, sqlList.get(0).getNumKeyTokens() == 1);

		// exception should not be thrown; this is valid
		list.add("select * from users where foo =`char:first`");
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		list.clear();
		list.add("update foo set attr1='bar'");
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Get();
		assertEquals(true, sqlList != null);
		sqlList = rdb.getSqlStmnts4Put();
		assertEquals(true, sqlList != null);
		sqlList = rdb.getSqlStmnts4Post();
		assertEquals(true, sqlList == null);
		sqlList = rdb.getSqlStmnts4Delete();
		assertEquals(true, sqlList == null);
		// ensure proper method(s) are supported
		assertEquals(true, rdb.getSupportedMethods().length == 2);
		assertEquals(true, rdb.getSupportedMethods()[0].equalsIgnoreCase("GET"));
		assertEquals(true, rdb.getSupportedMethods()[1].equalsIgnoreCase("PUT"));

		// no exception should be thrown
		list.clear();
		list.add("select * from users where foo =`char:first` or bar =`char:first`");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		list.clear();
		list.add("update foo set attr1='bar'");
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Get();
		assertEquals(true, sqlList != null);
		sqlList = rdb.getSqlStmnts4Put();
		assertEquals(true, sqlList != null);
		sqlList = rdb.getSqlStmnts4Post();
		assertEquals(true, sqlList == null);
		sqlList = rdb.getSqlStmnts4Delete();
		assertEquals(true, sqlList == null);
		// ensure proper method(s) are supported
		assertEquals(true, rdb.getSupportedMethods().length == 2);
		assertEquals(true, rdb.getSupportedMethods()[0].equalsIgnoreCase("GET"));
		assertEquals(true, rdb.getSupportedMethods()[1].equalsIgnoreCase("PUT"));

		// an exception will be thrown because the function statement
		// will never be used. So there's no point putting it on
		// the list.
		list.clear();
		list.add("`integer:id` = call foo()");
		list.add("select * from foo");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for redundant function "
					+ "test");
		} catch (Exception e) {
		}

		// this test should throw an exception because you have two non-prepared
		// statements with neither of them having any input params
		list.clear();
		list.add("select first from foo where first like '%smith' ");
		list.add("select * from foo");
		rdb = new WdsResourceBean(); // create a new RDB for this test case
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get exception for two non-prepared "
					+ "statements test");
		} catch (Exception e) {
		}

		// test the use of the 'pkey' field
		sql = "insert into car (id, mpg) values (`pkey:id`,s_car.nextval,`integer:mpg`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Post(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		// confirm that the pkey calue is "id"
		sqlList = rdb.getSqlStmnts4Post();
		assertEquals(true, sqlList.get(0).getPrimaryKey() != null);
		assertEquals(true, sqlList.get(0).getPrimaryKey().equals("id"));
		// System.out.println(sqlList.get(0).getPrepared());
		assertEquals(
				true,
				sqlList.get(0)
						.getPrepared()
						.equals("insert into car (id , mpg) values ( s_car.nextval , ? )"));
		
		// test the use of the 'pkey' field
		sql = "insert into car ( id , mpg ) values (`pkey:id` ,s_car.nextval,`integer:mpg`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Post(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		// confirm that the pkey calue is "id"
		sqlList = rdb.getSqlStmnts4Post();
		assertEquals(true, sqlList.get(0).getPrimaryKey() != null);
		assertEquals(true, sqlList.get(0).getPrimaryKey().equals("id"));
		// System.out.println(sqlList.get(0).getPrepared());
		assertEquals(
				true,
				sqlList.get(0)
						.getPrepared()
						.equals("insert into car ( id , mpg ) values ( s_car.nextval , ? )"));

		// test the use of the 'pkey' field in other parts of the statement
		sql = "insert into car (id, mpg) values `pkey:id` (s_car.nextval,`integer:mpg`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Post(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Post();
		// System.out.println(sqlList.get(0).getPrepared());
		assertEquals(
				true,
				sqlList.get(0)
						.getPrepared()
						.equals("insert into car (id , mpg) values (s_car.nextval , ? )"));
		assertEquals(true, sqlList.get(0).getPrimaryKey() != null);
		assertEquals(true, sqlList.get(0).getPrimaryKey().equals("id"));

		// test the use of the 'pkey' field in other parts of the statement
		sql = "insert into car (id, mpg) values (`pkey:id`,s_car.nextval,`integer:mpg`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Post(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Post();
		assertEquals(
				true,
				sqlList.get(0)
						.getPrepared()
						.equals("insert into car (id , mpg) values ( s_car.nextval , ? )"));
		assertEquals(true, sqlList.get(0).getPrimaryKey() != null);
		assertEquals(true, sqlList.get(0).getPrimaryKey().equals("id"));

		// test the use of the 'pkey' field in other parts of the statement
		sql = "insert into car (id, mpg) values (s_car.nextval,`pkey:id`,`integer:mpg`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Post(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Post();
		// System.out.println(sqlList.get(0).getPrepared());
		assertEquals(
				true,
				sqlList.get(0)
						.getPrepared()
						.equals("insert into car (id , mpg) values (s_car.nextval , ? )"));
		assertEquals(true, sqlList.get(0).getPrimaryKey() != null);
		assertEquals(true, sqlList.get(0).getPrimaryKey().equals("id"));

		// test the use of the 'pkey' field in other parts of the statement
		sql = "insert into car (id,mpg) values (s_car.nextval,`integer:mpg`,`pkey:id`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Post(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Post();
		//System.out.println(sqlList.get(0).getPrepared());
		assertEquals(
				true,
				sqlList.get(0)
						.getPrepared()
						.equals("insert into car (id , mpg) values (s_car.nextval , ? )"));
		assertEquals(true, sqlList.get(0).getPrimaryKey() != null);
		assertEquals(true, sqlList.get(0).getPrimaryKey().equals("id"));

		// test the use of the 'pkey' field in other parts of the statement
		sql = "insert into car (id, mpg) values (s_car.nextval,`integer:mpg`) `pkey:id`";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Post(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Post();
		assertEquals(
				true,
				sqlList.get(0)
						.getPrepared()
						.equals("insert into car (id , mpg) values (s_car.nextval , ? )"));
		assertEquals(true, sqlList.get(0).getPrimaryKey() != null);
		assertEquals(true, sqlList.get(0).getPrimaryKey().equals("id"));

		// we should get an exception because a statement cannot start with pkey
		sql = "`pkey:id` insert into car (id, mpg) values (s_car.nextval,`integer:mpg`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Post(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get exception for pkey function test");
		} catch (Exception e) {
		}

		// we should get an exception because two pkeys are not allowed
		sql = "insert into car (id, mpg) values (`pkey:id`, s_car.nextval,`integer:mpg`) `pkey:id`";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Post(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get exception for pkey function test");
		} catch (Exception e) {
		}

		// we should get an exception because pkey cannot be used as out param
		// for a function
		sql = "`pkey:id` = call foo(`char:last:in`,`char:first:in`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Post(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get exception for pkey function test");
		} catch (Exception e) {
		}

		// we should get an exception because pkey can only be used in insert
		// statements
		sql = "`integer:id` = call foo(`pkey:id`,`char:last:in`,`char:first:in`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Post(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get exception for pkey function test");
		} catch (Exception e) {
		}

		// same as above, but with a select
		list.add("select first from foo where `pkey:id` first like '%smith' ");
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get exception for pkey function test");
		} catch (Exception e) {
		}

		// same as above, but with a update
		list.add("update car set mpg=28 where id=123 `pkey:id`");
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get exception for pkey function test");
		} catch (Exception e) {
		}

	}

	// test stored functions
	@Test
	public void TestB() {
		// make sure we have a rdb
		assertEquals(true, (rdb != null));

		List<SqlStmnt> sqlList = null;

		String sql = "`integer:id` = call foo(`char:last:in`,`char:first:in`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean(); // create a new RDB for this test case
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Put(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Put();
		assertEquals(true, sqlList != null);
		assertEquals(true, sqlList.get(0).isPrepared());
		assertEquals(true, sqlList.get(0).isCallable());
		assertEquals(true, sqlList.get(0).isFunction());
		assertEquals(true, sqlList.get(0).getStoredProcName().equals("foo"));
		assertEquals(true,
				"? = call foo( ? , ? )".equals(sqlList.get(0).getPrepared()));
		// ensure proper method(s) are supported
		assertEquals(true, rdb.getSupportedMethods().length == 1);
		assertEquals(true, rdb.getSupportedMethods()[0].equalsIgnoreCase("PUT"));

		// test '=call' being one string
		sql = "`integer:id` =call foo(`char:last:in`,`char:first`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Get();
		assertEquals(true, sqlList != null);
		assertEquals(true, sqlList.get(0).isPrepared());
		assertEquals(true, sqlList.get(0).isCallable());
		assertEquals(true, sqlList.get(0).isFunction());
		assertEquals(true, sqlList.get(0).getStoredProcName().equals("foo"));
		assertEquals(true,
				"? =call foo( ? , ? )".equals(sqlList.get(0).getPrepared()));

		// test '=call' and first in field being one string
		sql = "`integer:id`=call foo(`char:last`,`char:first`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Get();
		assertEquals(true, sqlList != null);
		assertEquals(true, sqlList.get(0).isPrepared());
		assertEquals(true, sqlList.get(0).isCallable());
		assertEquals(true, sqlList.get(0).isFunction());
		assertEquals(true, sqlList.get(0).getStoredProcName().equals("foo"));
		assertEquals(true,
				"? =call foo( ? , ? )".equals(sqlList.get(0).getPrepared()));

		sql = "{`integer:id` =call FOO(`char:last:in`,`char:first:in`)}";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception for this valid sql statement: "
					+ sql);
		}
		sqlList = rdb.getSqlStmnts4Get();
		assertEquals(true, sqlList != null);
		assertEquals(true, sqlList.get(0).isPrepared());
		assertEquals(true, sqlList.get(0).isCallable());
		assertEquals(true, sqlList.get(0).isFunction());
		assertEquals(true, sqlList.get(0).getStoredProcName().equals("FOO"));
		assertEquals(true,
				"? =call FOO( ? , ? )".equals(sqlList.get(0).getPrepared()));
		List<SqlToken> tokens = sqlList.get(0).getSortedKeyTokens();
		assertEquals(true, tokens.get(0).getKey().equals("id"));
		assertEquals(true, tokens.get(1).getKey().equals("last"));
		assertEquals(true, tokens.get(2).getKey().equals("first"));

		// exception should be thrown because of in-balanced braces
		sql = "{`integer:id` =call FOO(`char:last`,`char:first`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		try {
			rdb.setSqls4Get(list);
			fail("ERROR: did not get Exception for this invalid sql statement: "
					+ sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		sql = "`integer:id` = call foo(`char:name:in`,`integer:id`)";
		list.clear();
		list.add(sql);
		rdb = new WdsResourceBean(); // create a new RDB for this test case
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get Exception for this invalid sql statement: "
					+ sql);
		} catch (Exception e) {
			if (!(e instanceof IllegalArgumentException)) {
				fail("ERROR: did not get IllegalArgumentException for this "
						+ "invalid sql statement: " + sql);
			}
		}

		// this should throw an exception because there are two params with
		// the same name
		list.clear();
		sql = "`integer:id` = call foo(`char:name`,`integer:id`)";
		list.add(sql);
		sql = "select * from foo";
		list.add(sql);
		rdb = new WdsResourceBean(); // create a new RDB for this test case
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
			fail("ERROR: did not get exception when one was expected");
		} catch (Exception e) {
		}

	}

}
