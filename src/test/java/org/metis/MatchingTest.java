package org.metis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.metis.sql.SqlStmnt;
import org.metis.pull.WdsResourceBean;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MatchingTest {

	private static WdsResourceBean rdb;
	private static ArrayList<String> list = new ArrayList<String>();

	private static List<SqlStmnt> sqlList = null;
	private static Map<String, String> map = new HashMap<String, String>();

	@Test
	public void TestA() {
		list.clear();
		map.clear();
		list.add("select * from foo");
		list.add("select first from foo where id = `integer:id`");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception: " + e.getLocalizedMessage());
		}
		sqlList = rdb.getSqlStmnts4Get();
		assertEquals(true, sqlList != null);
		assertEquals(true, sqlList.get(0).getNumKeyTokens() == 0);
		assertEquals(true, sqlList.get(1).getNumKeyTokens() == 1);
		assertEquals(false, sqlList.get(0).isPrepared());
		assertEquals(false, sqlList.get(0).isCallable());
		assertEquals(false, sqlList.get(0).isFunction());
		assertEquals(true, sqlList.get(1).isPrepared());
		assertEquals(false, sqlList.get(1).isCallable());
		assertEquals(false, sqlList.get(1).isFunction());
		SqlStmnt stmnt = SqlStmnt.getMatch(sqlList, map.keySet());
		assertEquals(true, stmnt != null);
		assertEquals(true, "select * from foo".equals(stmnt.getOriginal()));
		map.put("id", "1234");
		stmnt = SqlStmnt.getMatch(sqlList, map.keySet());
		assertEquals(true, stmnt != null);
		assertEquals(true, "select first from foo where id = ?".equals(stmnt
				.getPrepared()));

	}

	@Test
	public void TestB() {
		// an exception is not thrown because the function has no
		// input params, while the prepared statement has one
		// input param. if a URI arrives that has no params,
		// the function will be chosen
		list.clear();
		map.clear();
		list.add("`integer:id` = call foo()");
		list.add("select first from foo where id = `integer:id`");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception: " + e.getLocalizedMessage());
		}
		sqlList = rdb.getSqlStmnts4Get();
		assertEquals(true, sqlList != null);
		SqlStmnt stmnt = SqlStmnt.getMatch(sqlList, map.keySet());
		assertEquals(true, stmnt != null);
		assertEquals(true, "? = call foo()".equals(stmnt.getPrepared()));
	}

	@Test
	public void TestC() {

		list.clear();
		map.clear();
		list.add("select * from foo");
		list.add("select first from foo where id = `integer:id`");
		list.add("`integer:id` = call foo(`nvarchar:name`)");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception: " + e);
		}
		sqlList = rdb.getSqlStmnts4Get();
		assertEquals(true, sqlList != null);
		assertEquals(true, sqlList.get(0).getNumKeyTokens() == 0);
		assertEquals(true, sqlList.get(1).getNumKeyTokens() == 1);
		assertEquals(true, sqlList.get(2).getNumKeyTokens() == 2);
		assertEquals(false, sqlList.get(0).isPrepared());
		assertEquals(false, sqlList.get(0).isCallable());
		assertEquals(false, sqlList.get(0).isFunction());
		assertEquals(true, sqlList.get(1).isPrepared());
		assertEquals(false, sqlList.get(1).isCallable());
		assertEquals(false, sqlList.get(1).isFunction());
		assertEquals(true, sqlList.get(2).isPrepared());
		assertEquals(true, sqlList.get(2).isCallable());
		assertEquals(true, sqlList.get(2).isFunction());
		SqlStmnt stmnt = SqlStmnt.getMatch(sqlList, map.keySet());
		assertEquals(true, stmnt != null);
		assertEquals(true, "select * from foo".equals(stmnt.getOriginal()));
		map.put("name", "fred flintstone");
		stmnt = SqlStmnt.getMatch(sqlList, map.keySet());
		assertEquals(true, stmnt != null);
		assertEquals(true, "? = call foo( ? )".equals(stmnt.getPrepared()));
		map.clear();
		map.put("id", "123");
		stmnt = SqlStmnt.getMatch(sqlList, map.keySet());
		assertEquals(true, stmnt != null);
		assertEquals(true, "select first from foo where id = ?".equals(stmnt
				.getPrepared()));
	}

	@Test
	public void TestD() {
		list.clear();
		map.clear();
		list.add("select * from foo");
		list.add("select first from foo where id = `integer:id`");
		list.add("select first from foo where id = `integer:id` and name like `varchar:name`");
		list.add("`integer:id` = call foo(`nvarchar:name`)");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception: " + e);
		}
		sqlList = rdb.getSqlStmnts4Get();
		assertEquals(true, sqlList.size() == 4);
		assertEquals(true, sqlList.get(0).getNumKeyTokens() == 0);
		assertEquals(true, sqlList.get(1).getNumKeyTokens() == 1);
		assertEquals(true, sqlList.get(2).getNumKeyTokens() == 2);
		assertEquals(true, sqlList.get(3).getNumKeyTokens() == 2);
		assertEquals(true, sqlList != null);
		assertEquals(false, sqlList.get(0).isPrepared());
		assertEquals(false, sqlList.get(0).isCallable());
		assertEquals(false, sqlList.get(0).isFunction());
		assertEquals(true, sqlList.get(1).isPrepared());
		assertEquals(false, sqlList.get(1).isCallable());
		assertEquals(false, sqlList.get(1).isFunction());
		assertEquals(true, sqlList.get(2).isPrepared());
		assertEquals(false, sqlList.get(2).isCallable());
		assertEquals(false, sqlList.get(2).isFunction());
		assertEquals(true, sqlList.get(3).isPrepared());
		assertEquals(true, sqlList.get(3).isCallable());
		assertEquals(true, sqlList.get(3).isFunction());
		SqlStmnt stmnt = SqlStmnt.getMatch(sqlList, map.keySet());
		assertEquals(true, stmnt != null);
		assertEquals(true, "select * from foo".equals(stmnt.getOriginal()));
		map.put("name", "fred flintstone");
		map.put("id", "123");
		stmnt = SqlStmnt.getMatch(sqlList, map.keySet());
		assertEquals(true, stmnt != null);
		assertEquals(true,
				"select first from foo where id = ? and name like ?"
						.equals(stmnt.getPrepared()));
		map.clear();
		map.put("name", "fred flintstone");
		stmnt = SqlStmnt.getMatch(sqlList, map.keySet());
		assertEquals(true, stmnt != null);
		assertEquals(true, "? = call foo( ? )".equals(stmnt.getPrepared()));
		map.clear();
		map.put("foo", "fred flintstone");
		stmnt = SqlStmnt.getMatch(sqlList, map.keySet());
		assertEquals(true, stmnt == null);
	}

	@Test
	public void TestE() {

		// an exception is not thrown because the function has no
		// input params, while the prepared statement has one
		// input param. if a URI arrives that has no params,
		// the function will be chosen
		list.clear();
		map.clear();
		list.add("`integer:id` = call foo()");
		list.add("select first from foo where id = `integer:iD`");
		rdb = new WdsResourceBean();
		rdb.setDataSource(new DummyDataSource());
		rdb.setSqls4Get(list);
		try {
			rdb.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR: got this Exception: " + e.getLocalizedMessage());
		}
		sqlList = rdb.getSqlStmnts4Get();
		SqlStmnt stmnt = SqlStmnt.getMatch(sqlList, map.keySet());
		assertEquals(true, stmnt != null);
		assertEquals(true, "? = call foo()".equals(stmnt.getPrepared()));
		map.put("id", "123");
		stmnt = SqlStmnt.getMatch(sqlList, map.keySet());
		assertEquals(true, stmnt != null);
		assertEquals(true, "select first from foo where id = ?".equals(stmnt
				.getPrepared()));
	}
}
