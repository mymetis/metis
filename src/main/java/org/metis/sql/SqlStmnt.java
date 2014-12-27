/*
 * Copyright 2014 Joe Fernandez 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.metis.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import static org.metis.utils.Statics.*;

import org.metis.jdbc.WdsStoredProcedure;
import org.metis.jdbc.PreparedStmntCreator;
import org.metis.MetisController;
import org.metis.push.WdsSocketSession;

/**
 * Object that encapsulates or represents a SQL statement. Also serves as the
 * main integration point with Spring JDBC. This object is created by either the
 * WdsResourceBean or PusherBean during its lifecycle's initialization phase.
 * 
 * There are two types of SqlStmnts, those used by RDBs and those used by PDBs.
 * The latter of the two spawns SqlJobs used for notifying web socket clients
 * when a database change occurs. In other words, web socket clients subscribe
 * to one or more SqlJobs, and the SqlJobs notify their socket clients. It is
 * more or less an implementation of the observer design pattern.
 * 
 */
public class SqlStmnt implements RowMapper<Map<String, Object>> {

	public static final Log LOG = LogFactory.getLog(SqlStmnt.class);

	// this is the string representation of this SQL statement as provided by
	// the Spring application context.
	private String originalStr;

	// this is the string version of the prepared statement for this SQL
	// statement; i.e., assuming this is a prepared statement.
	private String preparedStr = "";

	// specifies whether this statement is call'able; i.e., whether this
	// represents a call to a stored function or procedure.
	private boolean isCallable;

	// specifies whether this statement is for invoking a stored function
	private boolean isFunction;

	// if this is a call'able statement, the name of the stored procedure
	private String storedProcName;

	// the Spring JdbcTemplate used by this SqlStmnt
	private JdbcTemplate jdbcTemplate;

	// Used for executing functions and stored procedures.
	private WdsStoredProcedure storedProcedure;

	// this list contains 'all' the tokens that comprise this SQL statement;
	// in other words, it contains both regular and key tokens
	private ArrayList<SqlToken> tokens = new ArrayList<SqlToken>();

	// this map contains only the key tokens; e.g., `integer:id` is a
	// key token.
	private Map<String, SqlToken> keyTokens = new HashMap<String, SqlToken>();
	// same as above, but is used to maintain the key tokens in sorted
	// order by position. This can be done during runtime, but for performance
	// reasons, it would be better to have them already in a sorted list.
	// Space usage is not an issue as there won't be many of these key:value
	// tokens
	private List<SqlToken> sortedKeyTokens = new ArrayList<SqlToken>();

	// this list contains 'all' the tokens for a call'able that have an IN
	// mode
	private ArrayList<SqlToken> inTokens = new ArrayList<SqlToken>();

	// contains the currently running SqlJobs (if any)
	private Hashtable<String, SqlJob> sqlJobs = new Hashtable<String, SqlJob>();

	// used for assigning ids to SqlJobs spawned by this SqlStmnt
	private static AtomicLong sqlJobId = new AtomicLong(1L);

	// this var is used for keeping track of those key fields that are used more
	// than once in a SqlStmnt
	private int numDupKeys = 0;

	// the optional primary key field assigned to an INSERT SqlStmnt
	private String primaryKey;

	private MetisController metisController;

	// the three frequency fields, which are used only for SqlJobs spawned by
	// this SqlStmnt
	private long intervalTime = 0L;
	private long intervalMax = 0L;
	private long intervalStep = 0L;

	/**
	 * Enumeration used for identifying the type of SQL statement; whether it is
	 * a function, stored procedure, query or update
	 */
	public enum SqlStmntType {
		SELECT, UPDATE, DELETE, INSERT, FUNCTION, PROCEDURE;
	}

	// the type of SQL statement that this statement represents; e.g., select,
	// insert, update, etc.
	private SqlStmntType sqlStmntType;

	/**
	 * Create a SqlStmnt object from the given SQL string and input tokens. The
	 * tokens were previously created by the static getSQLStmnt method.
	 * 
	 * @param originalStr
	 * @param intokens
	 * @throws IllegalArgumentException
	 */
	public SqlStmnt(MetisController wdch, String orig,
			ArrayList<SqlToken> intokens, JdbcTemplate template)
			throws IllegalArgumentException, Exception {
		jdbcTemplate = template;
		originalStr = orig;
		metisController = wdch;
		// perform all the initialization and validation tasks for this object
		init(intokens);
	}

	/**
	 * Get the SqlStmntType for this statement.
	 * 
	 * @return
	 */
	public SqlStmntType getSqlStmntType() {
		return sqlStmntType;
	}

	/**
	 * Destroys all the currently running SqlJobs (if any) pertaining to this
	 * SqlStmnt
	 */
	public void destroy() {
		for (SqlJob job : sqlJobs.values()) {
			job.doStop();
		}
	}

	/**
	 * Spawns a background SqlJob, for this statment, with the given input
	 * params and web socket session.
	 * 
	 * @param params
	 * @return
	 */
	public SqlJob createSqlJob(Map<String, String> params,
			WdsSocketSession wdsSession) throws Exception {
		// note: the job will add itself to this statement
		SqlJob job = (params == null) ? new SqlJob(this, Long.toString(sqlJobId
				.incrementAndGet())) : new SqlJob(this, params,
				Long.toString(sqlJobId.incrementAndGet()));
		// add the session to the newly created job
		job.addSession(wdsSession);
		// start the job
		job.doStart();
		return job;
	}

	/**
	 * Adds the given SqlJob to this statement's collection of SqlJobs.
	 * 
	 * @param job
	 */
	public void addSqlJob(SqlJob job) {
		sqlJobs.put(job.getId(), job);
	}

	/**
	 * Removes the given SqlJob from this statement's collection of jobs.
	 * 
	 * @param job
	 * @return
	 */
	public SqlJob removeSqlJob(SqlJob job) {
		job.doStop();
		return sqlJobs.remove(job.getId());
	}

	/**
	 * Given the map of key-value piars, find a job in this statment's
	 * collection of jobs that has an identical map.
	 * 
	 * @param map
	 * @return
	 */
	public SqlJob findSqlJob(Map<String, String> map,
			WdsSocketSession wdsSession) {
		for (String key : sqlJobs.keySet()) {
			SqlJob job = sqlJobs.get(key);
			if (job.isParamMatch(map)) {
				// if we can't get the lock on the job, then the job is on its
				// way out.
				if (job.tryLock()) {
					try {
						job.addSession(wdsSession);
						return job;
					} finally {
						job.unLock();
					}
				} else {
					// a new job will be created to replace the one on its way
					// out
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Returns true if the given session exists in one of the jobs
	 * 
	 * @param sessionId
	 * @return
	 */
	public boolean sessionExists(String sessionId) {
		for (String key : sqlJobs.keySet()) {
			SqlJob job = sqlJobs.get(key);
			if (job.sessionExists(sessionId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the sql job that contains the given session, else returns null
	 * 
	 * @param sessionId
	 * @return
	 */
	public SqlJob getSqlJob(String sessionId) {
		for (String key : sqlJobs.keySet()) {
			SqlJob job = sqlJobs.get(key);
			if (job.sessionExists(sessionId)) {
				return job;
			}
		}
		return null;
	}

	/**
	 * Set the SqlStmntType for this statement
	 * 
	 * @param sqlStmntType
	 */
	public void setSqlStmntType(SqlStmntType sqlStmntType) {
		this.sqlStmntType = sqlStmntType;
	}

	/**
	 * Get the String representation as originally injected.
	 * 
	 * @return
	 */
	public String getOriginal() {
		return originalStr;
	}

	/**
	 * Get the prepared string version of this statement.
	 * 
	 * @return
	 */
	public String getPrepared() {
		return preparedStr;
	}

	/**
	 * Returns true if this statement is prepared.
	 * 
	 * @return
	 */
	public boolean isPrepared() {
		return !getPrepared().isEmpty();
	}

	/**
	 * Returns true if this statement is call'able.
	 * 
	 * @return
	 */
	public boolean isCallable() {
		return isCallable;
	}

	/**
	 * Is this is a stored function
	 * 
	 * @return
	 */
	public boolean isFunction() {
		return isFunction;
	}

	/**
	 * Return the list of all tokens that comprise this SQL statement object.
	 * 
	 * @return
	 */
	public ArrayList<SqlToken> getTokens() {
		return tokens;
	}

	/**
	 * Return the key fields/tokens (if any) for this statement
	 * 
	 * @return
	 */
	public Map<String, SqlToken> getKeyTokens() {
		return keyTokens;
	}

	/**
	 * Return this statement's Spring JdbcTemplate
	 * 
	 * @return
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	/**
	 * Returns the number of tokens that are of type 'key'
	 * 
	 * @return
	 */
	public int getNumKeyTokens() {
		return keyTokens.size();
	}

	/**
	 * If this is a call'able, return the name of the stored procedure, else
	 * null is returned.
	 * 
	 * @return
	 */
	public String getStoredProcName() {
		return storedProcName;
	}

	public WdsStoredProcedure getStoredProcedure() {
		return storedProcedure;
	}

	public void setStoredProcedure(WdsStoredProcedure storedProcedure) {
		this.storedProcedure = storedProcedure;
	}

	public ArrayList<SqlToken> getInTokens() {
		return inTokens;
	}

	public void setInTokens(ArrayList<SqlToken> inTokens) {
		this.inTokens = inTokens;
	}

	public List<SqlToken> getSortedKeyTokens() {
		return sortedKeyTokens;
	}

	public void setSortedKeyTokens(List<SqlToken> sortedKeyTokens) {
		this.sortedKeyTokens = sortedKeyTokens;
	}

	public boolean isSelect() {
		return this.getSqlStmntType() == SqlStmntType.SELECT;
	}

	public boolean isDelete() {
		return this.getSqlStmntType() == SqlStmntType.DELETE;
	}

	public boolean isInsert() {
		return this.getSqlStmntType() == SqlStmntType.INSERT;
	}

	public boolean isUpdate() {
		return this.getSqlStmntType() == SqlStmntType.UPDATE;
	}

	public boolean isProcedure() {
		return this.getSqlStmntType() == SqlStmntType.PROCEDURE;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * Called to determine if the given set of keys (input param names) matches
	 * those in this statement. If there are no keys given and this statement is
	 * not a prepared statement, then return true because this statement has no
	 * keys. Also, if the are no given keys and this is a call'able with no IN
	 * params then also return true.
	 * 
	 * @param keys
	 * @return
	 */
	public boolean isMatch(Set<String> keys) {

		if (keys == null || keys.isEmpty()) {
			LOG.trace("isMatch: given key set is null or empty");
			if (!isPrepared()) {
				LOG.trace("isMatch: returning true because this stmt is not "
						+ "prepared");
				return true;
			} else if (isCallable() && getInTokens().isEmpty()) {
				LOG.trace("isMatch: returning true because this stmt is "
						+ "call'able and has no in params");
				return true;
			}
			return false;
		} else if (!isCallable()) {
			// if this statement is not call'able, then every key in this
			// statement must match every given key
			if (keys.size() != getNumKeyTokens()) {
				LOG.trace("isMatch: returning false because key set is "
						+ "null or set is not of matching size");
				return false;
			}
			for (String key : keys) {
				if (keyTokens.get(key) == null) {
					LOG.trace("isMatch: returning false because this given "
							+ "key does not match: " + key);
					return false;
				}
			}
		} else {
			// its call'able so every IN key in this statement must have a
			// match in the given key set
			if (keys.size() != getInTokens().size()) {
				LOG.trace("isMatch: key set size does not equal "
						+ "number of IN SqlTokens");
				return false;
			}
			for (String key : keys) {
				boolean match = false;
				for (SqlToken token : getInTokens()) {
					if (token.getKey().equals(key)) {
						match = true;
						break;
					}
				}
				if (!match) {
					LOG.trace("isMatch: could not find IN token for "
							+ "this key: " + key);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns true if this statement is equal to the given one. Equality is
	 * strictly based on those key:value pairs that are for 'input' params. This
	 * method is indirectly called, via isEqual(List), by afterProperties in the
	 * RDB to ensure there will be no mapping conflicts between the
	 * Spring-injected sql statements. That is, two SQL statements with the same
	 * set of input params.
	 * 
	 * @param stmt
	 * @return
	 */
	public boolean isEqual(SqlStmnt stmt) {

		// get a count of the number of input fields for this statement
		// statements that are not prepared do not have input fields
		int it1Cnt = 0;
		if (isPrepared()) {
			// if its not call'able then they're all input fields
			it1Cnt = isCallable() ? getInTokens().size() : getNumKeyTokens();
		}

		// System.out.println("it1Cnt = " + it1Cnt);

		// do the same for the given statement
		int it2Cnt = 0;
		if (stmt.isPrepared()) {
			it2Cnt = stmt.isCallable() ? stmt.getInTokens().size() : stmt
					.getNumKeyTokens();
		}

		// note that it is possible for a prepared statement to
		// not have any input params, as would be the case for
		// a stored function with only an out param!

		// System.out.println("it2Cnt = " + it2Cnt);

		// if the number of input fields don't match up
		// return false. if they're both equal to zero,
		// return true
		if (it1Cnt != it2Cnt) {
			return false;
		} else if (it1Cnt + it2Cnt == 0) {
			// if neither have any input params, then they're
			// considered equal; even if one is prepared and
			// the other one is not. if that were the case,
			// the prepared, which most likely is a function,
			// would never be used.
			return true;
		}

		// both statements have the same number of input params,
		// which is greater than 0.

		// iterate through this statements key fields and compare them
		// against the given stmnt
		for (SqlToken token1 : getSortedKeyTokens()) {
			// skip out fields, which may reside in call'ables
			if (token1.isOut()) {
				continue;
			}
			for (SqlToken token2 : stmt.getSortedKeyTokens()) {
				// skip out fields, which may reside in call'ables
				if (token2.isOut()) {
					continue;
				}
				// ok, we have two input params, are they
				// called the same?
				if (!token1.getKey().equals(token2.getKey())) {
					return false;
				}
			}
		}
		// System.out.println("EVERYTHING IS EQUAL");
		return true;
	}

	/**
	 * Returns true if this statement is equal to one in the given list.
	 * Equality is based on those key:value pairs that are for 'input' params
	 * 
	 * @param stmts
	 * @return
	 */
	public boolean isEqual(List<SqlStmnt> stmts) {
		for (SqlStmnt stmt : stmts) {
			if (isEqual(stmt)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * From the given list of SQL statements, return the one that matches the
	 * given set of parameter keys, which represents input params.
	 * 
	 * <code>
	 * 1. If there are no keys (i.e., no input params) to work with, then the 
	 * first non-prepared SQL statement will be returned. Thus, only one 
	 * non-prepared SQL statement can or should be used  within a given list. 
	 * This takes highest precedence when no input params are given.
	 * 
	 * 
	 * 2. If there are no keys to work with and #1 fails to find a statement, 
	 * then search for a call'able statement that has only one parameter that 
	 * is an OUT parameter. So we're looking for the following:
	 * 
	 *       `cursor:students` = call foo()  --> ? = call foo()
	 *       call foo(`cursor:students:out`) --> call foo(?)
	 *       
	 * Note that a cursor type is the most logical choice, but is not 
	 * required in this use case. I guess it is possible for a function 
	 * or stored procedure with only one OUT param to return a scalar. 
	 * 
	 * 
	 * 3. If there are keys, then the match will be performed off those keys. 
	 * 
	 * </code>
	 * 
	 * @param stmnts
	 * @param keys
	 * @return
	 */
	public static SqlStmnt getMatch(List<SqlStmnt> stmnts, Set<String> keys) {

		if (stmnts == null || stmnts.size() == 0) {
			LOG.trace("getMatch: null or empty list of statements "
					+ "was provided");
			return null;
		}

		// if there are no keys, then follow steps 1 and 2 above
		if (keys == null || keys.isEmpty()) {
			LOG.trace("getMatch: input params were not provided");
			// first search for the first non-prepared statement
			for (SqlStmnt stmnt : stmnts) {
				if (!stmnt.isPrepared()) {
					LOG.trace("getMatch: returning this non-prepared statement: "
							+ stmnt.getOriginal());
					return stmnt;
				}
			}

			// there was no non-prepared statement, so
			// follow step 2
			for (SqlStmnt stmnt : stmnts) {
				if (stmnt.isCallable) {
					if (stmnt.getKeyTokens().size() == 1) {
						SqlToken token = stmnt.getKeyTokens().values()
								.iterator().next();
						if (token.isOut()) {
							LOG.trace("getMatch: returning this prepared statement: "
									+ stmnt.getOriginal());
							return stmnt;
						}
					}
				}
			}
			LOG.trace("getMatch: returning null");
			return null;
		} // if (keys == null || keys.isEmpty())

		// We have keys to work with so we'll work off the given keys
		LOG.trace("getMatch: input params were provided");
		for (SqlStmnt stmnt : stmnts) {
			if (stmnt.isMatch(keys)) {
				LOG.trace("getMatch: returning this statement: "
						+ stmnt.getOriginal());
				return stmnt;
			}
		}
		LOG.warn("getMatch: keys were provided, but no parameterized sql "
				+ "statement could be found");
		return null;
	}

	/**
	 * SqlStmnt factory used for creating a SqlStmnt object from the given SQL
	 * string.
	 * 
	 * @param wdch
	 * @param sql
	 * @param jdbcTemplate
	 * @return
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	public static SqlStmnt getSQLStmnt(MetisController wdch, String sql,
			JdbcTemplate jdbcTemplate) throws IllegalArgumentException,
			Exception {

		if (sql == null || sql.isEmpty()) {
			throw new IllegalArgumentException(
					"getSQLTokens: sql string is null or empty");
		}

		// remove spaces that may be embedded in the field tokens. for example:
		// select * from users where first like ` string : first `
		// will get transformed to
		// select * from users where first like `string:first`
		//
		// this is done in order to ensure that the entire field token is
		// treated as one token
		//
		// remove spaces will also ensure that any ` ... ` strings are
		// surrounded by spaces, thus also ensuring the entire string is
		// treated as one token
		String sql2 = removeSpaces(sql);

		// split the sql string into tokens where the delimiter is any number of
		// white spaces. a sql statement must have at least 2 tokens
		String[] tokens = sql2.trim().split(DELIM);
		if (tokens.length < 2) {
			throw new IllegalArgumentException(
					"Invalid SQL statement - insufficent number of tokens");
		}

		// the list that will hold the resulting SqlTokens
		ArrayList<SqlToken> tList = new ArrayList<SqlToken>();

		int pos = 1;
		Boolean isCallable = null;
		boolean isFunction = false;
		boolean pkeySet = false;
		boolean pkeyFound = false;
		// convert the string tokens to SqlTokens and tuck them into the tList.
		for (int i = 0; i < tokens.length; i++) {

			// see if its a field-token; i.e., ` ... `
			if (tokens[i].startsWith(BACK_QUOTE_STR)
					&& tokens[i].endsWith(BACK_QUOTE_STR)) {

				// reset pkeyFound key
				pkeyFound = false;

				// extract the type:key-name:mode from the field token
				// the mode is optional and only used for callable statements

				// strip out the leading and trailing '`' chars
				String k = tokens[i].substring(1, tokens[i].length() - 1);

				// tokenize based on the ':' delimiter
				String[] tks = k.split(COLON_STR);
				if (tks.length != 2 && tks.length != 3) {
					throw new IllegalArgumentException(
							"Invalid SQL statement - paramter [" + k
									+ "] is not properly formatted");
				}

				// trim the tokens
				for (int j = 0; j < tks.length; j++) {
					tks[j] = tks[j].trim();
				}

				// is this a special "pkey" token?
				if (tks[0].equalsIgnoreCase(PKEY_STR)) {
					// pkey is not allowed as first token
					if (i == 0) {
						throw new IllegalArgumentException(
								"Invalid SQL statement - 'pkey' type field "
										+ "cannot be out param for a function");
					}
					// only one pkey is allowed
					else if (pkeySet) {
						throw new IllegalArgumentException(
								"Invalid SQL statement - only one 'pkey' type field "
										+ "is allowed per statement");
					}
					pkeySet = true;
					// if the previous token is a comma and the next token
					// is also a comma or right paren, then remove the previous
					// token
					if (tList.size() > 0) {
						// grab the previous token
						SqlToken prevToken = tList.get(tList.size() - 1);
						String nextTokenStr = (i != tokens.length - 1) ? tokens[i + 1]
								: SPACE_CHR_STR;
						if (!prevToken.isKey()
								&& prevToken.getValue().equals(COMMA_STR)
								&& (nextTokenStr.equals(COMMA_STR) || nextTokenStr
										.startsWith(RIGHT_PAREN_STR))) {
							tList.remove(tList.size() - 1);
						} else {
							// if the next token is a comma, then it
							// will be skipped
							pkeyFound = true;
						}
					}

					tList.add(new SqlToken(tks[0], tks[1], null, -1));

					// go on to the next token, so as not to bump the
					// pos counter. pkey field is not a bound parameter
					continue;
				}

				// if the very first token is a key:value field, then this
				// statement is a function
				// e.g., `integer:id` = call foo()
				if (i == 0) {
					isCallable = true;
					isFunction = true;
				}

				// a mode was not provided
				if (tks.length == 2) {
					// if this is the first key token, then it represents a
					// function
					if (i == 0) {
						// this token represents the return value for a stored
						// function, so give it an out mode since user has not
						// provided one
						tList.add(new SqlToken(tks[0], tks[1], "out", pos));
					}
					// if this is a call'able statement that is not a function,
					// then give it an appropriate default mode
					else if (isCallable != null && isCallable.booleanValue()
							&& !isFunction) {
						if (tks[0].equalsIgnoreCase(CURSOR_STR)
								|| tks[0].equalsIgnoreCase(RSET_STR)) {
							tList.add(new SqlToken(tks[0], tks[1], "out", pos));
						} else {
							tList.add(new SqlToken(tks[0], tks[1], "in", pos));
						}
					}
					// functions cannot be given rset and cursor params
					else if (isCallable != null && isCallable.booleanValue()
							&& isFunction) {
						if (tks[0].equalsIgnoreCase(CURSOR_STR)
								|| tks[0].equalsIgnoreCase(RSET_STR)) {
							throw new IllegalArgumentException(
									"Invalid SQL statement - rset and cursor "
											+ "params cannot be given to a function: "
											+ sql);
						} else {
							tList.add(new SqlToken(tks[0], tks[1], "in", pos));
						}
					} else {
						tList.add(new SqlToken(tks[0], tks[1], null, pos));
					}
				}

				// a mode was provided, do some validation
				else {
					// non-callables cannot be assigned modes
					if (isCallable != null && !isCallable.booleanValue()) {
						throw new IllegalArgumentException(
								"Invalid SQL statement - 'mode' provided for "
										+ "this non-callable statement: " + sql);
					}
					// function params cannot have an OUT mode
					else if (isFunction
							&& (tks[2].equalsIgnoreCase(OUT_STR) || tks[2]
									.equalsIgnoreCase(INOUT_STR))) {
						throw new IllegalArgumentException(
								"Invalid SQL statement - OUT 'mode' cannot be assigned to "
										+ "function: " + sql);
					}
					// create a parameterized SqlToken
					tList.add(new SqlToken(tks[0], tks[1], tks[2], pos));
				}
				// update the param field pointer
				pos++;
			} else {

				// if the previous token was a pkey and this token is a ','
				// then skip this token
				if (pkeyFound) {
					pkeyFound = false;
					if (tokens[i].equals(COMMA_STR)) {
						continue;
					} else if (tokens[i].startsWith(COMMA_STR)) {
						// it may have been something like ",statfield"
						// in which case we don't skip "statfield"
						tokens[i] = tokens[i].substring(1);
					}
				}
				tList.add(new SqlToken(tokens[i]));
				// see if this is a stored procedure
				if (isCallable == null) {
					isCallable = new Boolean(CALL_STR.equals(tokens[i]));
				}
			}
		}
		return new SqlStmnt(wdch, sql, tList, jdbcTemplate);
	}

	/**
	 * Called by the Controller bean (RDB or PDB) to execute this SQL statement
	 * with the given params.
	 * 
	 * @param params
	 * @throws SQLException
	 */
	public SqlResult execute(List<Map<String, String>> params) {

		if (params == null) {
			params = new ArrayList<Map<String, String>>();
		}

		LOG.debug("execute: executing this statement: " + getOriginal());
		LOG.debug("execute: ... with this number of param maps  = "
				+ params.size());

		// first, do some light validation work
		if (params.size() == 0 && (isPrepared() || isCallable())) {
			// if it is callable and it requires an IN param
			if (isCallable() && getInTokens().size() > 0) {
				LOG.error("execute: ERROR, IN params were not provided "
						+ "for this callable statement that requires IN params: "
						+ getPrepared());
				return null;
			}
			// all prepared statements that are not callable require an
			// input param
			else {
				LOG.error("execute: ERROR, params were not provided "
						+ "for this prepared statement: " + getPrepared());
				return null;

			}
		} else if (params.size() > 0 && !isPrepared()) {
			LOG.error("execute: ERROR, params were provided "
					+ "for this static or non-prepared statement that does not "
					+ "require params: " + getOriginal());
			return null;
		}

		// make sure given params match
		if (params.size() > 0) {
			for (Map<String, String> pMap : params) {
				if (!isMatch(pMap.keySet())) {
					LOG.error("execute: ERROR, given key:value set does not match "
							+ "this statement's key:value set\n"
							+ getKeyTokens().toString()
							+ "  vs.  "
							+ params.toString());
					return null;
				}
			}
		}

		// if trace is on, dump params if any
		if (params.size() > 0 && LOG.isTraceEnabled()) {
			for (Map<String, String> pMap : params) {
				LOG.trace("execute: valid param set = " + pMap.toString());
			}
		}

		// A list that essentially represents the result set returned by the
		// DB for queries.
		List<Map<String, Object>> listOfMaps = new ArrayList<Map<String, Object>>();

		// dequeue a sqlResult object from the SqlResult cache
		SqlResult sqlResult = SqlResult.dequeue();

		try {
			// if this statement is call'able, then execute its stored procedure
			// object. Note that we don't support batching calls to stored
			// procedures and functions. Maybe that can be a future
			// enhancement...
			if (isCallable()) {
				LOG.debug("execute: invoking this stored procedure or function: "
						+ getStoredProcName());
				Map<String, Object> kvMap = new HashMap<String, Object>();
				// first prepare the IN params (if any)
				if (params.size() > 0) {
					for (KeyValueObject kvObj : getPreparedObjects(params
							.get(0))) {
						kvMap.put(kvObj.getKey(), kvObj.getObj());
					}
				}
				// now execute the function or stored proc
				// Note from Spring docs: The execute() method returns a
				// map with an entry for each declared output parameter,
				// using the parameter name as the key.
				kvMap = getStoredProcedure().execute(kvMap);
				// now that the execute has completed, fetch the OUT params
				// from the kvMap. i suppose it is possible for a stored proc
				// not to have any OUT params.

				// need to transfer each key:value that is associated with
				// the OUT param as a map to listOfMaps. However, those
				// keys that pertain to cursors or sets, point
				// to a List of Maps!!
				for (SqlToken sqlToken : getSortedKeyTokens()) {
					// skip IN only params; we're only looking for OUT params
					if (sqlToken.isIn()) {
						continue;
					}
					Object outObj = kvMap.remove(sqlToken.getKey());
					if (outObj == null) {
						LOG.error("execute: object was not returned for this "
								+ "out param: " + sqlToken.getKey());
						continue;
					}
					if (sqlToken.isCursor() || sqlToken.isRset()) {
						if (outObj instanceof List) {
							List<Map<String, Object>> mList = (List<Map<String, Object>>) outObj;
							for (Map<String, Object> map : mList) {
								listOfMaps.add(map);
							}
						} else {
							LOG.error("execute: this OUT result set param did not return a type of List: "
									+ sqlToken.getKey());
							LOG.error("execute: got this type/class instead: "
									+ outObj.getClass().getName());
						}
					} else {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(sqlToken.getKey(), outObj);
						listOfMaps.add(map);
					}
				}
				/*
				 * Any undeclared results returned are added to the output map
				 * with generated names like "#result-set-1" "#result-set-2"
				 * etc. You can change this by setting 'skipUndeclaredResults'
				 * to true, and then these undeclared resultsets will be
				 * skipped. TODO: look into the update count
				 */
				if (!kvMap.isEmpty()) {
					LOG.debug("execute: looking for result sets");
					for (Object kvObj : kvMap.values()) {
						if (kvObj instanceof List) {
							for (Map<String, Object> map : (List<Map<String, Object>>) kvObj) {
								listOfMaps.add(map);
							}
						} else {
							LOG.debug("execute: unknown object returned from execute: "
									+ kvObj.getClass().getName());
							LOG.debug("execute: unknown object's toString value: "
									+ kvObj.toString());
						}
					}
				}
				sqlResult.setResultSet(listOfMaps);
				return sqlResult;

			} // if (isCallable()...

			// key:value type objects used for binding the input params to
			// prepared statements
			List<KeyValueObject> kvObjs = null;
			Object bindObjs[] = null;

			// is this a query; i.e., select statement?
			if (getSqlStmntType() == SqlStmntType.SELECT) {
				if (isPrepared()) {
					LOG.debug("execute: executing this prepared SELECT statement: "
							+ getPrepared());
					kvObjs = getPreparedObjects(params.get(0));
					bindObjs = new Object[kvObjs.size()];
					for (int i = 0; i < bindObjs.length; i++) {
						bindObjs[i] = kvObjs.get(i).getObj();
					}
					listOfMaps = getJdbcTemplate().query(getPrepared(),
							bindObjs, this);
				} else {
					LOG.trace("execute: executing this SELECT statement: "
							+ getOriginal());
					listOfMaps = getJdbcTemplate().query(getOriginal(), this);
				}
				if (listOfMaps != null && listOfMaps.size() > 0) {
					LOG.trace("execute: dumping first map - "
							+ listOfMaps.get(0).toString());
				}
				sqlResult.setResultSet(listOfMaps);
				return sqlResult;
			}

			// ok, this statement is neither call'able nor a query so it
			// must be either an update of some kind; i.e., insert, update or
			// delete

			// note that keyHolders are only used for INSERT statements!

			if (!isPrepared()) {
				PreparedStmntCreator creatorSetter = new PreparedStmntCreator(
						this, bindObjs);
				// i guess it is possible to have a non prepared update of some
				// sort
				if (getSqlStmntType() == SqlStmntType.INSERT) {
					GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
					sqlResult.setNumRows(getJdbcTemplate().update(
							creatorSetter, keyHolder));
					sqlResult.setKeyHolder(keyHolder);
				} else {
					sqlResult.setNumRows(getJdbcTemplate().update(
							getOriginal(), creatorSetter));
				}
			}

			// we have a prepared update; is the client requesting a batch
			// update?
			else if (params.size() > 1) {
				LOG.debug("execute: invoking batch update for this statement: "
						+ getPrepared());
				// create the list of objects for the batch update
				List<Object[]> batchArgs = new ArrayList<Object[]>();
				for (Map<String, String> map : params) {
					// prepare the bind objects for the prepared
					// statement
					kvObjs = getPreparedObjects(map);
					bindObjs = new Object[kvObjs.size()];
					for (int i = 0; i < bindObjs.length; i++) {
						bindObjs[i] = kvObjs.get(i).getObj();
					}
					batchArgs.add(bindObjs);
				}
				sqlResult.setBatchNumRows(getJdbcTemplate().batchUpdate(
						getPrepared(), batchArgs));
				// note that a key holder is not possible with a batch
				// update
			}

			// we have a prepared update, but it is not a batch update
			else if (params.size() == 1) {

				LOG.debug("execute: invoking prepared update for this statement: "
						+ getPrepared());
				kvObjs = getPreparedObjects(params.get(0));
				bindObjs = new Object[kvObjs.size()];
				for (int i = 0; i < bindObjs.length; i++) {
					bindObjs[i] = kvObjs.get(i).getObj();
				}
				// note that PreparedStmntCreator is both a creator and setter
				PreparedStmntCreator creatorSetter = new PreparedStmntCreator(
						this, bindObjs);

				if (getSqlStmntType() == SqlStmntType.INSERT) {
					LOG.trace("execute: executing prepared INSERT statement");
					GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
					int numRows = getJdbcTemplate().update(creatorSetter,
							keyHolder);
					sqlResult.setNumRows(numRows);
					sqlResult.setKeyHolder(keyHolder);
				} else {
					LOG.trace("execute: executing UPDATE statement");
					int numRows = getJdbcTemplate().update(getPrepared(),
							creatorSetter);
					sqlResult.setNumRows(numRows);
				}
			}

		} catch (IllegalArgumentException exc) {
			LOG.error("execute: ERROR, caught this "
					+ "IllegalArgumentException while executing sql: "
					+ exc.toString());
			LOG.error("execute: exception stack trace follows:");
			dumpStackTrace(exc.getStackTrace());
			if (exc.getCause() != null) {
				LOG.error("execute: Caused by " + exc.getCause().toString());
				LOG.error("execute: causing exception stack trace follows:");
				dumpStackTrace(exc.getCause().getStackTrace());
			}
			if (sqlResult != null) {
				SqlResult.enqueue(sqlResult);
			}
			sqlResult = null;
		} catch (DataAccessException exc) {
			LOG.error("execute:ERROR, caught this "
					+ "DataAccessException while executing sql: "
					+ exc.toString());
			LOG.error("execute: exception stack trace follows:");
			dumpStackTrace(exc.getStackTrace());
			LOG.error("execute: Most Specific Cause = "
					+ exc.getMostSpecificCause().toString());
			LOG.error("execute: MSC exception stack trace follows:");
			dumpStackTrace(exc.getMostSpecificCause().getStackTrace());
			if (sqlResult != null) {
				SqlResult.enqueue(sqlResult);
			}
			sqlResult = null;
		}
		return sqlResult;
	}

	/**
	 * This method is a call-back method for the Spring JdbcTemplate's query
	 * call. It is responsible for mapping a row in the result set to a map. The
	 * returned map is placed into a list or array that is eventually
	 * transformed into a json array or object.
	 */
	public Map<String, Object> mapRow(ResultSet rs, int rowNum)
			throws SQLException {

		Map<String, Object> map = new HashMap<String, Object>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		for (int index = 1; index <= columnCount; index++) {
			String column = JdbcUtils.lookupColumnName(rsmd, index);
			Object value = rs.getObject(column);
			map.put(column, value);
		}
		return map;
	}

	/**
	 * Returns a String representation of this statement.
	 */
	public String toString() {

		return (isPrepared()) ? getPrepared() : getOriginal();

	}

	/**
	 * 
	 * @return the MetisController that owns this SqlStmnt
	 */
	public MetisController getMetisController() {
		return metisController;
	}

	/**
	 * Sets the MetisController that owns this SqlStmnt
	 * 
	 * @param metisController
	 */
	public void setMetisController(MetisController metisController) {
		this.metisController = metisController;
	}

	/**
	 * Remove spaces from preparedStr fields. Also ensure preparedStr field is
	 * surrounded by spaces. Care is taken not to modify anything within single
	 * quotes.
	 * 
	 * @param sql
	 * @return
	 */
	private static String removeSpaces(String sql) {
		char[] sqlChar = sql.toCharArray();
		char[] sqlChar2 = new char[sqlChar.length * 2];
		char cstate = SPACE_CHR;
		int index = 0;
		for (char c : sqlChar) {
			if (cstate == BACK_QUOTE_CHR) {
				if (c != SPACE_CHR && c != TAB_CHR && c != CARRIAGE_RETURN_CHR
						&& c != NEWLINE_CHR) {
					sqlChar2[index++] = c;
				}
				if (c == BACK_QUOTE_CHR) {
					sqlChar2[index++] = SPACE_CHR;
					cstate = SPACE_CHR;
				}

			} else if (cstate == SINGLE_QUOTE_CHR) {
				sqlChar2[index++] = c;
				if (c == SINGLE_QUOTE_CHR) {
					cstate = SPACE_CHR;
				}
			} else {
				cstate = c;
				if (cstate == BACK_QUOTE_CHR || cstate == COMMA_CHR) {
					sqlChar2[index++] = SPACE_CHR;
				}
				sqlChar2[index++] = cstate;
				// surround commas with spaces
				if (cstate == COMMA_CHR) {
					sqlChar2[index++] = SPACE_CHR;
				}
			}
		}
		return new String(sqlChar2).trim();
	}

	/**
	 * This method is responsible for returning a set of objects that are
	 * eventually bound to this statement's PreparedStatement. The binding of
	 * the objects to the PreparedStatement is performed by this statement's
	 * execute method.
	 * 
	 * NOTE: This statement 'assumes' that this is a prepared statement that
	 * does require input params; therefore, some sort of validation should have
	 * been done prior to calling this statement.
	 */
	private List<KeyValueObject> getPreparedObjects(Map<String, String> params)
			throws IllegalArgumentException {

		List<KeyValueObject> objects = new ArrayList<KeyValueObject>();

		if (!isPrepared() || (isCallable() && getInTokens().isEmpty())) {
			// this statement does not require params
			LOG.debug("getPreparedObjects: this statement does not "
					+ "require params: " + getOriginal());
			return objects;
		}

		int nParams = (params == null) ? 0 : params.size();

		if (params == null || params.size() == 0) {
			throw new IllegalArgumentException("statement requires in params, "
					+ "but params map was null or empty");
		}

		if (isCallable()) {
			if (nParams != getInTokens().size()) {
				String eStr = "getPreparedObjects: number of given params does "
						+ "not match number of params required by this "
						+ "call'able statement: " + getOriginal();
				LOG.error(eStr);
				throw new IllegalArgumentException(eStr);

			}
		} else if (nParams != getSortedKeyTokens().size()) {
			String eStr = "getPreparedObjects: number of given params does "
					+ "not match number of params required by this "
					+ "statement: " + getOriginal();
			LOG.error(eStr);
			throw new IllegalArgumentException(eStr);
		}

		LOG.trace("getPreparedObjects: numDupKeys = " + getNumDupKeys());
		if (getNumDupKeys() > 0) {
			int numElements = getNumDupKeys() + getSortedKeyTokens().size();
			for (int i = 0; i < numElements; i++) {
				objects.add(null);
			}
		}
		String paramValue = null;
		for (SqlToken token : getSortedKeyTokens()) {
			// skip those tokens that are strictly OUT params for call'ables
			// these are skipped because they're not mapped to input
			// parameters passed in by the client
			if (token.isOut()) {
				continue;
			}
			// get this key's param value from the given param map
			if ((paramValue = params.get(token.getKey())) == null) {
				LOG.error("getPreparedObjects: this key has no corresponding "
						+ "param in the given param map: " + token.getKey());
				throw new IllegalArgumentException(
						"this key has no corresponding param in the given "
								+ "param map: " + token.getKey());
			}
			LOG.trace("getPreparedObjects: getPosition returns "
					+ token.getPosition());
			try {
				// if the object list has already been pre-allocated, then
				// use set instead of add, and look for additional positions (if
				// any) where the key field is used
				if (getNumDupKeys() > 0) {
					objects.set(token.getPosition() - 1, new KeyValueObject(
							token.getKey(), token.getObjectValue(paramValue)));
					for (Integer pos : token.getPositions()) {
						LOG.trace("getPreparedObjects: xtra pos = " + pos);
						objects.set(
								pos.intValue() - 1,
								new KeyValueObject(token.getKey(), token
										.getObjectValue(paramValue)));
					}
				} else {
					objects.add(new KeyValueObject(token.getKey(), token
							.getObjectValue(paramValue)));
				}
			} catch (NumberFormatException e) {
				LOG.error("getPreparedObjects: this param value results in a  "
						+ "NumberFormatException: " + paramValue);
				throw new IllegalArgumentException("getPreparedObjects: this "
						+ "param value results in a  NumberFormatException: "
						+ paramValue);
			} catch (MalformedURLException e) {
				LOG.error("getPreparedObjects: this param value results in a  "
						+ "MalformedURLException: " + paramValue);
				throw new IllegalArgumentException("getPreparedObjects: this "
						+ "param value results in a  MalformedURLException: "
						+ paramValue);
			} catch (IllegalArgumentException e) {
				LOG.error("getPreparedObjects: this param value results in a  "
						+ "IllegalArgumentException: " + paramValue);
				throw e;
			}
		}
		return objects;
	}

	/**
	 * Return the number of times key are duplicated across this statement.
	 * 
	 * @return
	 */
	private int getNumDupKeys() {
		return numDupKeys;
	}

	/**
	 * Called by constructor to prep or initialize the object with the given
	 * SqlTokens. It will also perform validation.
	 * 
	 * @param orig
	 * @param intokens
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	private void init(ArrayList<SqlToken> intokens)
			throws IllegalArgumentException, Exception {

		SqlToken tmpToken = null;
		SqlToken[] keyTokenArray = new SqlToken[intokens.size()];
		int keyTokensIndex = 0;

		// start by iterating through all the tokens, placing them in
		// different buckets; depending on their type. There are 2 types
		// non-key and key. A non-key is a SQL keyword, while a key is
		// a token for a prepared statement; e.g., `integer:id`
		for (SqlToken token : intokens) {
			if (token.isKey()) {
				// save key tokens in one bucket. first check to see if this is
				// a primary key token.
				if (token.isPkey()) {
					primaryKey = token.getKey();
					continue;
				}
				keyTokenArray[keyTokensIndex++] = token;
				// and construct the prepared statement
				preparedStr += "? ";
			} else {
				preparedStr += token.getValue() + " ";
			}
			// save 'all' tokens, regardless of type, in the main
			// tokens bucket
			tokens.add(token);
		}

		// mark the statement as being prepared if there were key
		// type tokens
		if (keyTokensIndex > 0) {
			preparedStr = preparedStr.trim();
		} else {
			preparedStr = "";
		}

		// now look for a call'able statement. if the first token is a key, then
		// it is a function call e.g., `integer:id` = call foo(...). if the
		// first token is a 'call' keyword then it is a stored procedure
		String tmpStr = null;
		tmpToken = tokens.get(0);
		if (tmpToken.isKey()) {
			// check for `integer:id` =call foo(...) Or
			// `integer:id` = call foo(...)
			tmpStr = tokens.get(1).getValue();
			if (!tmpStr.equalsIgnoreCase(FUNC_EQUAL_CALL_STR)
					&& !tmpStr.equalsIgnoreCase(EQUALS_STR)) {
				throw new IllegalArgumentException(
						"SqlStmnt: function statment is not "
								+ "properly formatted: " + getOriginal());
			}
			int index = 0;
			if (tmpStr.equalsIgnoreCase(FUNC_EQUAL_CALL_STR)) {
				index = 2;
			} else {
				index = 3;
			}
			tmpToken = tokens.get(index);
			if (tmpToken.isKey()) {
				throw new IllegalArgumentException(
						"SqlStmnt: function statment is not properly "
								+ "formatted:" + getOriginal());
			}
			storedProcName = tmpToken.getValue().split(ESCAPED_LEFT_PAREN)[0]
					.trim();
			isFunction = true;
			isCallable = true;
			setSqlStmntType(SqlStmntType.FUNCTION);
		} else if (tmpToken.getValue().equalsIgnoreCase(CALL_STR)) {
			// else if first token is 'call', then it is for a stored procedure
			//
			// look for someone creating a stored procedure that has no params.
			// For example, "call foo()"
			if (!isPrepared()) {
				throw new IllegalArgumentException(
						"SqlStmnt: this call'able statement needs params: "
								+ getOriginal());
			}
			tmpToken = tokens.get(1);
			if (tmpToken.isKey()) {
				throw new IllegalArgumentException(
						"SqlStmnt: second token in statment cannot be "
								+ "parameterized");
			}
			// get the name of the stored procedure or function
			storedProcName = tmpToken.getValue().split(ESCAPED_LEFT_PAREN)[0]
					.trim();
			isCallable = true;
			setSqlStmntType(SqlStmntType.PROCEDURE);

		} else if (tmpToken.getValue().equalsIgnoreCase(SELECT_STR)) {
			setSqlStmntType(SqlStmntType.SELECT);

		} else if (tmpToken.getValue().equalsIgnoreCase(DELETE_STR)) {
			setSqlStmntType(SqlStmntType.DELETE);

		} else if (tmpToken.getValue().equalsIgnoreCase(UPDATE_STR)) {
			setSqlStmntType(SqlStmntType.UPDATE);

		} else if (tmpToken.getValue().equalsIgnoreCase(INSERT_STR)) {
			setSqlStmntType(SqlStmntType.INSERT);

		} else {
			throw new IllegalArgumentException("unknown SQL statement: "
					+ tmpToken.getValue());
		}

		// only insert statements are allowed the 'pkyey' marker field
		if (getSqlStmntType() != SqlStmntType.INSERT && getPrimaryKey() != null) {
			throw new IllegalArgumentException(
					"only INSERT statements are allowed a 'pkey' type field");
		}

		// validate the statement using different rules based on whether the
		// newly formed statement is non-prepared, prepared, or call'able.
		if (isPrepared() && isCallable) {
			for (int i = 0; i < keyTokensIndex; i++) {
				SqlToken token1 = keyTokenArray[i];
				for (int j = i + 1; j < keyTokensIndex; j++) {
					SqlToken token2 = keyTokenArray[j];
					// in a call'able you can't have more than one
					// key field with the same name
					if (token1.getKey().equals(token2.getKey())) {
						LOG.error("this call'able has duplicate key names: "
								+ getOriginal());
						throw new IllegalArgumentException(
								"SqlStmnt: duplicate key names in a "
										+ "call'able is not allowed: "
										+ getOriginal());
					}
				}
			}
		}

		// if it wasn't call'able, then is it prepared?
		else if (isPrepared()) {
			// it is a prepared statement, but it is not call'able
			// look for redundant or duplicate keys and keys having
			// invalid cursor types
			for (int i = 0; i < keyTokensIndex; i++) {
				SqlToken token1 = keyTokenArray[i];
				// skip those tokens that were found to be redundant and
				// removed
				if (token1 == null) {
					continue;
				}
				// this is not a call'able, so check that rset and
				// cursor types are not being used for this non
				// call'able
				if (token1.isRset() || token1.isCursor()) {
					LOG.error("this non call'able statement cannot "
							+ "have param types of RSET or CURSOR: "
							+ getOriginal());
					throw new IllegalArgumentException(
							"SqlStmnt: this non call'able statement cannot "
									+ "have param types of RSET or CURSOR: "
									+ getOriginal());

				}
				// now look duplicate key fields. for example,
				// select `char:field` from student order by `char:field` asc
				// not in the above how the key 'field' is used twice in the
				// select
				for (int j = i + 1; j < keyTokensIndex; j++) {
					SqlToken token2 = keyTokenArray[j];
					// skip those tokens that were found to be redundant and
					// removed
					if (token2 == null) {
						continue;
					}
					// if two tokens have the same key, then
					// they should also have the same type!
					if (token1.getKey().equals(token2.getKey())) {
						if (token1.getJdbcType() != token2.getJdbcType()) {
							LOG.error("this non call'able has duplicate "
									+ "key names, but with different "
									+ "jdbc types: " + getOriginal());
							throw new IllegalArgumentException(
									"SqlStmnt: duplicate key names in a "
											+ "non call'able must have "
											+ "same jdbc types");
						} else {
							// record the fact that token1 is found
							// in more than one position in this sql statement
							token1.getPositions().add(
									Integer.valueOf(token2.getPosition()));
							++numDupKeys;
							// token2 is now redundant
							keyTokenArray[j] = null;
						}
					}
				}
			}
		} // else if prepared

		// now place the resulting key tokens into the main key map,
		// sorted key map and any IN type key tokens in the inTokens list
		// also do some validation for IN types
		for (int i = 0; i < keyTokensIndex; i++) {
			if (keyTokenArray[i] != null) {
				keyTokens.put(keyTokenArray[i].getKey(), keyTokenArray[i]);
				sortedKeyTokens.add(keyTokenArray[i]);
				if (isCallable
						&& (keyTokenArray[i].isIn() || keyTokenArray[i]
								.isInOut())) {
					// IN or INOUTs cannot be of type RSET or CURSOR!
					if (keyTokenArray[i].isRset()
							|| keyTokenArray[i].isCursor()) {
						LOG.error("this call'able has IN or INOUT param "
								+ "that is declared as RSET or CURSOR: "
								+ getOriginal());
						throw new IllegalArgumentException("SqlStmnt: this "
								+ "call'able has an IN or INOUT param ["
								+ keyTokenArray[i].toString() + "] that is "
								+ "declared as RSET or CURSOR: "
								+ getOriginal());
					}
					inTokens.add(keyTokenArray[i]);
				}
			}
		}
		// sort the key:values fields in ascending order
		Collections.sort(sortedKeyTokens);

		// if this statement is call'able, then create a stored procedure object
		// for it
		setStoredProcedure((isCallable) ? new WdsStoredProcedure(this) : null);

	}

	private static void dumpStackTrace(StackTraceElement[] elements) {
		if (elements == null) {
			return;
		}
		int i = 0;
		for (; i < 10; i++) {
			LOG.error("at " + elements[i].toString());
		}
		if (elements.length > i) {
			LOG.error("... " + (elements.length - i) + " more");
		}
	}

	public long getIntervalTime() {
		return intervalTime;
	}

	public void setIntervalTime(long intervalTime) {
		this.intervalTime = intervalTime;
	}

	public long getIntervalMax() {
		return intervalMax;
	}

	public void setIntervalMax(long intervalMax) {
		this.intervalMax = intervalMax;
	}

	public long getIntervalStep() {
		return intervalStep;
	}

	public void setIntervalStep(long intervalStep) {
		this.intervalStep = intervalStep;
	}

	private class KeyValueObject {
		String key;
		Object obj;

		KeyValueObject(String key, Object obj) {
			this.key = key;
			this.obj = obj;
		}

		String getKey() {
			return key;
		}

		Object getObj() {
			return obj;
		}

	}

}
