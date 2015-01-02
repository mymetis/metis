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
package org.metis.pull;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.*;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.OutputStream;
import javax.sql.DataSource;
import java.sql.Connection;
import org.metis.MetisController;
import org.metis.utils.Utils;
import static org.metis.sql.SqlStmnt.getSQLStmnt;
import org.metis.sql.SqlStmnt;
import org.metis.sql.SqlResult;
import static org.metis.utils.Statics.*;
import static org.metis.utils.Utils.dumpStackTrace;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.core.HazelcastInstance;

/**
 * This bean represents a statically pre-configured resource bean (service
 * object). It is mapped to one or more URIs and invoked by a combination of
 * WdsDispatcherServlet and WdsRdbMapper to service an HTTP request.
 * 
 * The bean extends a 'Controller' that is found in Spring's
 * model-view-controller (MVC) package.
 * 
 * The bean is or may be assigned one or more parameterized SQL statements, and
 * it is the responsibility of the bean to map HTTP query parameters, if any, to
 * their respective SQL statements.
 * 
 * The bean must also be assigned a JDBC connection pool, through which it
 * interacts with a DBMS. The application context can have many such beans and
 * JDBC connections pools defined.
 * 
 */
public class WdsResourceBean extends AbstractController implements
		InitializingBean, BeanNameAware, DisposableBean, MetisController {

	public static final Log LOG = LogFactory.getLog(WdsResourceBean.class);

	/**
	 * The supported character sets.
	 */
	private static final String[] validCharSets = { "utf-8", "us-ascii",
			"utf-16" };
	/**
	 * The default character set is utf-8.
	 */
	private String charSet = validCharSets[0];
	/**
	 * This property specifies the value that is to be assigned to the HTTP
	 * response object's content-type header. Default is set to json, but it can
	 * be overridden via spring file.
	 */
	private static final String jsonContentType = "application/json";
	private static final String rspJsonContentType = jsonContentType
			+ ";charset=UTF-8";
	private static final String anyContentType = "*/*";
	private static final String urlEncodedContentType = "application/x-www-form-urlencoded";
	private String contentType = jsonContentType;

	// the string representations of the SQL statements assigned to this bean
	private List<String> sqls4Get;
	private List<String> sqls4Put;
	private List<String> sqls4Post;
	private List<String> sqls4Delete;

	// the SQL statements assigned to this bean
	private List<SqlStmnt> sqlStmnts4Get;
	private List<SqlStmnt> sqlStmnts4Put;
	private List<SqlStmnt> sqlStmnts4Post;
	private List<SqlStmnt> sqlStmnts4Delete;

	private static final String TRANSFER_ENCODING_HDR = "transfer-encoding";
	private static final String CHUNKED = "chunked";

	/**
	 * The name of this bean per the Spring application context
	 */
	private String beanName = "";

	/**
	 * Used to specify whether the request must be made via a secure channel
	 * (i.e., https).
	 */
	private Boolean secure;

	/**
	 * Used to specify whether the request must be made by an authenticated
	 * user.
	 */
	private Boolean authenticated;

	/**
	 * A comma-separated list of those agent types that are either allowed or
	 * not allowed. Those types that are not allowed are prefixed with a '!';
	 * e.g., '!Windows'. The list cannot contain both allowed and not allowed
	 * types; it must be one or the other. By default, all agents are allowed.
	 * The User-Agent http header field is used for picking out the agent type
	 * making the request.
	 */
	private String agentNames;
	private List<String> allowedAgents;
	private List<String> notAllowedAgents;

	/**
	 * Represents the list of methods assigned to the 'Allowed' header field
	 * when sending back a 405
	 */
	private String allowedMethodsRsp = "";

	private GeneratedKeyHolder keyHolder;

	/**
	 * Enumeration used for keeping track of the valid method types
	 */
	enum Method {

		POST, GET, PUT, DELETE;

		public boolean isPost() {
			return this == POST;
		}

		public boolean isPut() {
			return this == PUT;
		}

		public boolean isGet() {
			return this == GET;
		}

		public boolean isDelete() {
			return this == DELETE;
		}
	}

	/**
	 * A list of characters that are not permitted as part of field values. This
	 * is to help prevent SQL injection attacks.
	 */
	private String blackList = "";

	/**
	 * The Spring JDBC Template for this RDB
	 */
	private JdbcTemplate jdbcTemplate;

	/**
	 * The DataSource (connection cache) used for creating the jdbcTemplate.
	 */
	private DataSource dataSource;

	/**
	 * The URL being used to connect to the DB.
	 */
	private String dbUrl;

	/**
	 * The name of the servlet that this bean pertains to.
	 */
	private String servletName;

	/**
	 * Used to let the dispatcher know whether this bean was able to successully
	 * acquire a jdbc connection during startup
	 */
	private boolean dbConnectionAcquired;

	/**
	 * Used for setting the Expires response header. For example, the DBA may
	 * know that a certain entity doesn't change very often; maybe once a day,
	 * at most. So setting this property to 10, tells the client that the
	 * data retrieved from a GET is unlikley to change in the next 10 seconds.
	 */
	private long expires;

	/**
	 * This property allows the admin to set the cache-control string.
	 */
	private String cacheControl;

	/**
	 * The name of the JDBC Driver being used.
	 */
	private String driverName;

	/**
	 * If true, indicates that this RDB is using an Oracle driver.
	 */
	private boolean isOracle;

	/**
	 * This is initialized by the dispatcher servlet. It identifies the servlet
	 * container being used.
	 */
	public static String serverInfo;

	/**
	 * The Hazelcast instance to use (optional).
	 */
	private HazelcastInstance hazelcastInstance;

	public WdsResourceBean() {
		super();
	}

	// -- begin getter and setter methods for this bean's properties; to allow
	// maximum flexibility, all should be Spring inject'able

	/**
	 * Spring will inject the data source, from which we also create the Spring
	 * JDBC template for this RDB.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public GeneratedKeyHolder getKeyHolder() {
		return keyHolder;
	}

	public void setKeyHolder(GeneratedKeyHolder keyHolder) {
		this.keyHolder = keyHolder;
	}

	public void setSecure(boolean secure) {
		this.secure = Boolean.valueOf(secure);
	}

	public Boolean getSecure() {
		return secure;
	}

	public boolean isSecure() {
		return (secure == null) ? false : secure.booleanValue();
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = Boolean.valueOf(authenticated);
	}

	public Boolean getAuthenticated() {
		return authenticated;
	}

	public boolean isAuthenticated() {
		return (authenticated == null) ? false : authenticated.booleanValue();
	}

	/**
	 * Called by Spring to set the name of this bean
	 */
	public void setBeanName(String name) {
		beanName = name;
	}

	public String getBeanName() {
		return beanName;
	}

	public boolean isDbConnectionAcquired() {
		return dbConnectionAcquired;
	}

	public void setDbConnectionAcquired(boolean dbConnectionAcquired) {
		this.dbConnectionAcquired = dbConnectionAcquired;
	}

	public long getExpires() {
		return expires;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}

	public static String getServerInfo() {
		return serverInfo;
	}

	public String getCacheControl() {
		return cacheControl;
	}

	public void setCacheControl(String cacheControl) {
		this.cacheControl = cacheControl;
	}

	public boolean isOracle() {
		return isOracle;
	}

	/**
	 * Set the list of allowed or not allowed agents.
	 * 
	 * @param agentTypes
	 */
	public void setAgentNames(String agentsNames)
			throws IllegalArgumentException {
		if (agentsNames != null && agentsNames.length() > 0) {

			// Convert to lower case as http USER-AGENT header
			// value has been converted to lower case as well
			agentsNames = agentsNames.toLowerCase();

			this.agentNames = agentsNames;
			setAllowedAgents(Utils.getAgentNames(agentsNames, true));
			setNotAllowedAgents(Utils.getAgentNames(agentsNames, false));
			if (!getAllowedAgents().isEmpty()
					&& !getNotAllowedAgents().isEmpty()) {
				throw new IllegalArgumentException(
						"ERROR, both allowed and not allowed device types cannot "
								+ "be in the agentTypes list; it must be one or "
								+ "the other.");
			}
		}
	}

	public String getAgentNames() {
		return agentNames;
	}

	/**
	 * The list of illegal characters for input field values. Default value is
	 * an empty list.
	 * 
	 * @param list
	 */
	public void setBlackList(String list) {
		if (list != null && list.length() > 0) {
			blackList = list;
		}
	}

	public String getBlackList() {
		return blackList;
	}

	public void setCharSet(String charSet) throws IllegalArgumentException {
		if (charSet != null && charSet.length() > 0) {
			for (int i = 0; i < validCharSets.length; i++) {
				if (validCharSets[i].equalsIgnoreCase(charSet)) {
					this.charSet = charSet;
					return;
				}
			}
		}
		throw new IllegalArgumentException("invalid char set [" + charSet + "]");
	}

	public String getCharSet() {
		return charSet;
	}

	/**
	 * Set the content type for this bean. The default being "application/json".
	 * 
	 * @param contentType
	 */
	public void setContentType(String contentType)
			throws IllegalArgumentException {
		if (contentType != null && contentType.length() > 0) {
			this.contentType = contentType;
		} else {
			throw new IllegalArgumentException(
					"invalid contentType - null or empty");
		}
	}

	/**
	 * Get the content type currently set for this bean
	 * 
	 * @return
	 */
	public String getContentType() {
		return contentType;
	}

	public List<String> getAllowedAgents() {
		return allowedAgents;
	}

	public void setAllowedAgents(List<String> allowedAgents) {
		this.allowedAgents = allowedAgents;
	}

	public List<String> getNotAllowedAgents() {
		return notAllowedAgents;
	}

	public void setNotAllowedAgents(List<String> notAllowedAgents) {
		this.notAllowedAgents = notAllowedAgents;
	}

	/*
	 * These are the getter and setter methods for the bean's properties that
	 * represent the SQL statements mapped to the HTTP methods. The bean must
	 * have at least one such property defined.
	 */

	/**
	 * Used for validating SQL statement for GET method
	 * 
	 * @param sql
	 * @return
	 * @throws IllegalArgumentException
	 */
	private String valSql4Get(String sql) throws IllegalArgumentException {
		if (sql != null && sql.length() > 0) {
			sql = Utils.stripCall(sql);
			String[] tokens = sql.split("\\s+");
			if (tokens.length < 2) {
				throw new IllegalArgumentException(
						"valSql4Get: invalid SQL statement - insufficent "
								+ "number of tokens");
			} else if (!tokens[0].equalsIgnoreCase(SELECT_STR)
					&& !tokens[0].equalsIgnoreCase(CALL_STR)
					&& !tokens[0].startsWith(BACK_QUOTE_STR)) {
				throw new IllegalArgumentException(
						"valSql4Get: invalid SQL statement - must start with "
								+ "either 'select' or 'call'");
			}
		} else {
			throw new IllegalArgumentException(
					"valSql4Get: invalid SQL statement - empty or null statement");
		}
		return sql.trim();
	}

	/**
	 * This method is called by Spring to inject the SQL statements for the GET
	 * method. You can assign more than one SQL statement to an HTTP method.
	 * Based on the incoming parameters, WDS will match the service request to
	 * the SQL statement.
	 * 
	 * @param sqls
	 * @throws IllegalArgumentException
	 */
	public void setSqls4Get(List<String> sqls) throws IllegalArgumentException {
		if (sqls == null || sqls.isEmpty()) {
			throw new IllegalArgumentException(
					"setSqls4Get: invalid list of SQL statements - empty or "
							+ "null statement");
		}
		sqls4Get = new ArrayList<String>();
		for (String sql : sqls) {
			sqls4Get.add(valSql4Get(sql));
		}
	}

	public List<String> getSqls4Get() {
		return sqls4Get;
	}

	/**
	 * Used for validating SQL statement for POST method
	 * 
	 * @param sql
	 * @return
	 * @throws IllegalArgumentException
	 */
	private String valSql4Post(String sql) throws IllegalArgumentException {
		if (sql != null && sql.length() > 0) {
			sql = Utils.stripCall(sql);
			String[] tokens = sql.split("\\s+");
			if (tokens.length < 2) {
				throw new IllegalArgumentException(
						"valSql4Post: invalid SQL statement - insufficent "
								+ "number of tokens");
			} else if (!tokens[0].equalsIgnoreCase(UPDATE_STR)
					&& !tokens[0].equalsIgnoreCase(INSERT_STR)
					&& !tokens[0].equalsIgnoreCase(CALL_STR)
					&& !tokens[0].startsWith(BACK_QUOTE_STR)) {
				throw new IllegalArgumentException(
						"valSql4Post: invalid SQL statement - must start with "
								+ "'insert', 'update' or 'call'");
			}

		} else {
			throw new IllegalArgumentException(
					"valSql4Post: invalid SQL statement - empty or null "
							+ "statement");
		}
		return sql.trim();
	}

	/**
	 * This method is called by Spring to inject the SQL statements for the POST
	 * method.
	 * 
	 * @param sqls
	 * @throws IllegalArgumentException
	 */
	public void setSqls4Post(List<String> sqls) throws IllegalArgumentException {
		if (sqls == null || sqls.isEmpty()) {
			throw new IllegalArgumentException(
					"setSqls4Post: invalid list of SQL statements - "
							+ "empty or null statement");
		}
		sqls4Post = new ArrayList<String>();
		for (String sql : sqls) {
			sqls4Post.add(valSql4Post(sql));
		}
	}

	/**
	 * Get the list of SQLs for the POST method
	 * 
	 * @return
	 */
	public List<String> getSqls4Post() {
		return sqls4Post;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	/**
	 * Used for validating SQL statement for PUT method
	 * 
	 * @param sql
	 * @return
	 * @throws IllegalArgumentException
	 */
	private String valSql4Put(String sql) {
		if (sql != null && sql.length() > 0) {
			sql = Utils.stripCall(sql);
			String[] tokens = sql.split("\\s+");
			if (tokens.length < 2) {
				throw new IllegalArgumentException(
						"setSql4Put: invalid SQL statement - insufficent "
								+ "number of tokens");
			} else if (!tokens[0].equalsIgnoreCase(UPDATE_STR)
					&& !tokens[0].equalsIgnoreCase(INSERT_STR)
					&& !tokens[0].equalsIgnoreCase(CALL_STR)
					&& !tokens[0].startsWith(BACK_QUOTE_STR)) {
				throw new IllegalArgumentException(
						"setSql4Put: invalid SQL statement - must start "
								+ "with 'insert', 'update' or 'call'");
			}
		} else {
			throw new IllegalArgumentException(
					"setSql4Put: invalid SQL statement - empty or null "
							+ "statement");
		}
		return sql.trim();
	}

	/**
	 * This method is called by Spring to inject the SQL statements for the PUT
	 * method.
	 * 
	 * @param sqls
	 * @throws IllegalArgumentException
	 */
	public void setSqls4Put(List<String> sqls) throws IllegalArgumentException {
		if (sqls == null || sqls.isEmpty()) {
			throw new IllegalArgumentException(
					"setSqls4Put: invalid list of SQL statements - empty or "
							+ "null statement");
		}
		sqls4Put = new ArrayList<String>();
		for (String sql : sqls) {
			sqls4Put.add(valSql4Put(sql));
		}
	}

	/**
	 * Get the list of SQLs for the PUT method
	 * 
	 * @return
	 */
	public List<String> getSqls4Put() {
		return sqls4Put;
	}

	/**
	 * Used for validating SQL statement for DELETE method
	 * 
	 * @param sql
	 * @return
	 * @throws IllegalArgumentException
	 */
	private String valSql4Delete(String sql) throws IllegalArgumentException {
		if (sql != null && sql.length() > 0) {
			sql = Utils.stripCall(sql);
			String[] tokens = sql.split("\\s+");
			if (tokens.length < 2) {
				throw new IllegalArgumentException(
						"valSql4Delete: invalid SQL statement - insufficent "
								+ "number of tokens");
			} else if (!tokens[0].equalsIgnoreCase(DELETE_STR)
					&& !tokens[0].equalsIgnoreCase(CALL_STR)
					&& !tokens[0].startsWith(BACK_QUOTE_STR)) {
				throw new IllegalArgumentException(
						"valSql4Delete: invalid SQL statement - must start "
								+ "with 'delete' or 'call' ");
			}
		} else {
			throw new IllegalArgumentException(
					"valSql4Delete: invalid SQL statement - empty or null "
							+ "statement");
		}
		return sql.trim();
	}

	/**
	 * This method is called by Spring to inject the SQL statements for the
	 * DELETE method.
	 * 
	 * @param sqls
	 * @throws IllegalArgumentException
	 */
	public void setSqls4Delete(List<String> sqls)
			throws IllegalArgumentException {
		if (sqls == null || sqls.isEmpty()) {
			throw new IllegalArgumentException(
					"setSqls4Delete: invalid list of SQL statements - empty "
							+ "or null statement");
		}
		sqls4Delete = new ArrayList<String>();
		for (String sql : sqls) {
			sqls4Delete.add(valSql4Delete(sql));
		}
	}

	// these are the accessor or getter methods for the SQL statements, which
	// are assigned values by the afterProperties method.

	/**
	 * Get the list of SQLs for the DELETE method
	 * 
	 * @return
	 */
	public List<String> getSqls4Delete() {
		return sqls4Delete;
	}

	public List<SqlStmnt> getSqlStmnts4Get() {
		return sqlStmnts4Get;
	}

	public List<SqlStmnt> getSqlStmnts4Post() {
		return sqlStmnts4Post;
	}

	public List<SqlStmnt> getSqlStmnts4Put() {
		return sqlStmnts4Put;
	}

	public List<SqlStmnt> getSqlStmnts4Delete() {
		return sqlStmnts4Delete;
	}

	// -----------------------

	/**
	 * Called by Spring after all of this bean's properties have been set.
	 */
	public void afterPropertiesSet() throws Exception {

		// log info for the jdbc driver being used
		// this will also attempt to open connection
		// with jdbc driver
		try {
			Connection con = getDataSource().getConnection();
			if (con != null) {
				DatabaseMetaData dbmd = con.getMetaData();
				setDbConnectionAcquired(true);
				if (dbmd != null) {
					setDriverName(dbmd.getDriverName().trim().toLowerCase());
					isOracle = (getDriverName() != null && getDriverName()
							.indexOf(ORACLE_STR) >= 0) ? true : false;
					LOG.info(getBeanName() + ":Is Oracle JDBC Driver = "
							+ isOracle);
					LOG.info(getBeanName() + ":JDBC Driver name = "
							+ getDriverName());
					LOG.info(getBeanName() + ":JDBC Driver version = "
							+ dbmd.getDriverVersion().trim());
					LOG.info(getBeanName() + ":JDBC Driver product name = "
							+ dbmd.getDatabaseProductName().trim());
					LOG.info(getBeanName()
							+ ":JDBC Driver database product version = "
							+ dbmd.getDatabaseProductVersion().trim());
					con.close();
				} else {
					LOG.info(getBeanName()
							+ ": Unable to get JDBC driver meta data");
				}
			} else {
				LOG.info(getBeanName() + ": Unable to get JDBC connection");
			}
		} catch (SQLException exc) {
			LOG.error(getBeanName() + ": got this exception when trying to "
					+ "get driver meta data: " + exc.toString());
			LOG.error(getBeanName() + ": exception stack trace follows:");
			dumpStackTrace(exc.getStackTrace());
			LOG.error(getBeanName() + ": Caused by "
					+ exc.getCause().toString());
			LOG.error(getBeanName()
					+ ": causing exception stack trace follows:");
			dumpStackTrace(exc.getCause().getStackTrace());
		}

		// bean must be assigned a JDBC DataSource
		if (getDataSource() == null) {
			throw new Exception(getBeanName()
					+ ".afterPropertiesSet: this bean has not been "
					+ "assigned a JDBC DataSource");
		}

		// do some validation
		if (getSqls4Get() == null && getSqls4Put() == null
				&& getSqls4Post() == null && getSqls4Delete() == null) {
			throw new Exception(
					"At least one of the WdsResourceBean's http methods has "
							+ "not been assigned a SQL statement");
		}

		// create and validate the different SQL statements
		if (getSqls4Get() != null) {
			sqlStmnts4Get = new ArrayList<SqlStmnt>();
			for (String sql : getSqls4Get()) {
				SqlStmnt stmt = getSQLStmnt(this, sql, getJdbcTemplate());
				if (stmt.isEqual(sqlStmnts4Get)) {
					throw new Exception(
							"Injected SQL statements for GET are not distinct");
				}
				sqlStmnts4Get.add(stmt);
			}
			if (LOG.isDebugEnabled()) {
				for (SqlStmnt sqlstmnt : sqlStmnts4Get) {
					LOG.debug(getBeanName() + ": SQL for GET = "
							+ sqlstmnt.getOriginal());
					LOG.debug(getBeanName() + ": Parameterized SQL for GET = "
							+ sqlstmnt.getPrepared());
				}
			}
			allowedMethodsRsp += "GET ";
		}

		if (getSqls4Put() != null) {
			sqlStmnts4Put = new ArrayList<SqlStmnt>();
			for (String sql : getSqls4Put()) {
				SqlStmnt stmt = getSQLStmnt(this, sql, getJdbcTemplate());
				if (stmt.isEqual(sqlStmnts4Put)) {
					throw new Exception(
							"Injected SQL statements for PUT are not distinct");
				}
				sqlStmnts4Put.add(stmt);
			}
			if (LOG.isDebugEnabled()) {
				for (SqlStmnt sqlstmnt : sqlStmnts4Put) {
					LOG.debug(getBeanName() + ": SQL for PUT = "
							+ sqlstmnt.getOriginal());
					LOG.debug(getBeanName() + ": Parameterized SQL for PUT = "
							+ sqlstmnt.getPrepared());
				}
			}
			allowedMethodsRsp += "PUT ";
		}

		if (getSqls4Post() != null) {
			sqlStmnts4Post = new ArrayList<SqlStmnt>();
			for (String sql : getSqls4Post()) {
				SqlStmnt stmt = getSQLStmnt(this, sql, getJdbcTemplate());
				if (stmt.isEqual(sqlStmnts4Post)) {
					throw new Exception(
							"Injected SQL statements for POST are not distinct");
				}
				sqlStmnts4Post.add(stmt);
			}
			if (LOG.isDebugEnabled()) {
				for (SqlStmnt sqlstmnt : sqlStmnts4Post) {
					LOG.debug(getBeanName() + ": SQL for POST = "
							+ sqlstmnt.getOriginal());
					LOG.debug(getBeanName() + ": Parameterized SQL for POST = "
							+ sqlstmnt.getPrepared());
				}
			}
			allowedMethodsRsp += "POST ";
		}

		if (getSqls4Delete() != null) {
			sqlStmnts4Delete = new ArrayList<SqlStmnt>();
			for (String sql : getSqls4Delete()) {
				SqlStmnt stmt = getSQLStmnt(this, sql, getJdbcTemplate());
				if (stmt.isEqual(sqlStmnts4Delete)) {
					throw new Exception(
							"Injected SQL statements for DELETE are not distinct");
				}
				sqlStmnts4Delete.add(stmt);
			}
			if (LOG.isDebugEnabled()) {
				for (SqlStmnt sqlstmnt : sqlStmnts4Delete) {
					LOG.debug(getBeanName() + ": SQL for DELETE = "
							+ sqlstmnt.getOriginal());
					LOG.debug(getBeanName()
							+ ": Parameterized SQL for DELETE = "
							+ sqlstmnt.getPrepared());
				}
			}
			allowedMethodsRsp += "DELETE";
		}

		LOG.debug(getBeanName() + ": allowedMethodsRsp string = "
				+ allowedMethodsRsp);

		// tell our parent what methods this RDB will support
		setSupportedMethods(allowedMethodsRsp.split(SPACE_CHR_STR));

		if (LOG.isDebugEnabled() && getAllowedAgents() != null) {
			if (!getAllowedAgents().isEmpty()) {
				LOG.debug(getBeanName() + ": agents allowed =  "
						+ getAllowedAgents());
			} else {
				LOG.debug(getBeanName() + ": agents not allowed =  "
						+ getNotAllowedAgents());
			}
		}

	}

	/**
	 * Invoked by the BeanFactory on destruction of this singleton.
	 */
	public void destroy() {
	}

	/**
	 * This method gets called by the WdsRdbMapper bean to handle a HTTP
	 * request. This method must be multi-thread capable. Note that since we're
	 * not using Views, this method must return null.
	 * 
	 * @param request
	 *            the http request that is being serviced
	 * @param response
	 *            the response that will be sent back to the service consumer
	 * @return must return null since we're not using a view
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		LOG.debug(getBeanName()
				+ ": handleRequestInternal - **** new request ****");

		// dump the request if trace is on
		if (LOG.isTraceEnabled()) {
			LOG.trace(getBeanName() + ":handleRequestInternal - method = "
					+ request.getMethod());
			LOG.trace(getBeanName() + ":handleRequestInternal - uri  = "
					+ request.getRequestURI());
			LOG.trace(getBeanName() + ":handleRequestInternal - protocol  = "
					+ request.getProtocol());
			LOG.trace(getBeanName() + ":handleRequestInternal - secure  = "
					+ request.isSecure());

			// dump all the http headers and their values
			Enumeration<String> headerNames = request.getHeaderNames();
			if (headerNames != null) {
				while (headerNames.hasMoreElements()) {
					String headerName = headerNames.nextElement();
					LOG.trace(getBeanName() + ":handleRequestInternal - "
							+ headerName + " = "
							+ request.getHeader(headerName));
				}
			}

			if (request.getQueryString() != null) {
				LOG.trace(getBeanName()
						+ ":handleRequestInternal - queryString  = "
						+ request.getQueryString());
			}
		}

		long currentTime = System.currentTimeMillis();

		// give the response a Date header with the current time
		response.setDateHeader(DATE_HDR, currentTime);

		// assign the Server header this container's info
		response.setHeader(SERVER_HDR, getServerInfo());

		// determine the HTTP protocol version being used by the client
		// default version will be 0
		int protocolVersion = 0;
		try {
			protocolVersion = Integer.parseInt(request.getProtocol().split(
					FORWARD_SLASH_STR)[1].split(ESC_DOT_STR)[1]);
		} catch (Exception exc) {
			LOG.warn(getBeanName()
					+ ": handleRequestInternal - unable to get http protocol "
					+ "version, stack trace follows: ");
			LOG.error(getBeanName() + ": exception stack trace follows:");
			dumpStackTrace(exc.getStackTrace());
		}

		LOG.trace(getBeanName() + ":handleRequestInternal - using this "
				+ "protocol version: " + protocolVersion);

		/*
		 * Ok, the request first needs to run the security gauntlet
		 * 
		 * We do not want to send any error messages back to the client that
		 * would give it a hint that we're invoking SQL statements. This is a
		 * countermeasure for SQL injection probes.
		 */

		// see if this RDB is restricting user agents and if so, validate user
		// agent
		if ((getAllowedAgents() != null && !getAllowedAgents().isEmpty())
				|| (getNotAllowedAgents() != null && !getNotAllowedAgents()
						.isEmpty())) {

			String userAgent = request.getHeader(USER_AGENT_HDR);

			if (userAgent != null && userAgent.length() > 0) {
				LOG.debug(getBeanName()
						+ ": handleRequestInternal - validating this "
						+ "user agent: " + userAgent);

				// Convert to lower case as allowed agents have been
				// converted to lower case as well
				userAgent = userAgent.toLowerCase();

				boolean allow = false;
				if (getAllowedAgents() != null && !getAllowedAgents().isEmpty()) {
					for (String agent : getAllowedAgents()) {
						LOG.trace(getBeanName()
								+ ": handleRequestInternal - comparing to this "
								+ "allowed agent : " + agent);
						if (userAgent.indexOf(agent) >= 0) {
							LOG.trace(getBeanName()
									+ ": handleRequestInternal - this allowed agent "
									+ "was found: " + agent);
							allow = true;
							break;
						}
					}
				} else {
					allow = true;
					for (String agent : getNotAllowedAgents()) {
						LOG.trace(getBeanName()
								+ ": handleRequestInternal - comparing to this "
								+ "non-allowed agent : " + agent);
						if (userAgent.indexOf(agent) >= 0) {
							LOG.trace(getBeanName()
									+ ": handleRequestInternal - this non-allowed "
									+ "agent was found: " + agent);
							allow = false;
							break;
						}
					}
				}
				if (!allow) {
					response.sendError(SC_UNAUTHORIZED, "ERROR, user agent "
							+ "is not authorized");
					LOG.error(getBeanName()
							+ ": handleRequestInternal - ERROR, user agent is "
							+ "not authorized");
					return null;
				}
			} else {
				response.sendError(SC_UNAUTHORIZED, "ERROR, user agent info "
						+ "was not received and is required!");
				LOG.error(getBeanName()
						+ ": handleRequestInternal - ERROR, user agent header "
						+ "is required but was not provided by the client");
				return null;
			}
		}

		// we do not support chunked transfer encoding, which is a http
		// 1.1 feature.
		if (request.getHeader(TRANSFER_ENCODING_HDR) != null
				&& request.getHeader(TRANSFER_ENCODING_HDR).equalsIgnoreCase(
						CHUNKED)) {
			response.sendError(SC_BAD_REQUEST,
					"Chunked tranfer encoding is not " + "supported");
			return null;
		}

		/*
		 * isSecure returns a boolean indicating whether this request was made
		 * using a secure channel, such as HTTPS. so, if the channel must be
		 * secure, but it is not, then throw an exception and return an error.
		 */
		if (isSecure() && !request.isSecure()) {
			response.sendError(SC_UNAUTHORIZED, "ERROR, channel is not secure");
			LOG.error(getBeanName()
					+ ": handleRequestInternal - ERROR, channel is not secure");
			return null;
		}

		/*
		 * getUserPrincipal() returns a java.security.Principal containing the
		 * name of the user making this request, else it returns null if the
		 * user has not been authenticated. so, if it is mandated that the user
		 * be authenticated, but has not been authenticated, then throw an
		 * exception and return an error
		 */
		if (isAuthenticated() && request.getUserPrincipal() == null) {
			response.sendError(SC_UNAUTHORIZED,
					"ERROR, user is not authenticated");
			LOG.error(getBeanName()
					+ ": handleRequestInternal - ERROR, user is not authenticated");
			return null;
		}

		/*
		 * Check for valid method - the only supported http methods are GET,
		 * POST, PUT, and DELETE. Here are some good descriptions regarding the
		 * methods and their use with respect to this servlet.
		 * 
		 * The GET method is used for projecting data from the DB. So it maps to
		 * a select statement.
		 * 
		 * The PUT and POST methods are used for inserting or updating an entity
		 * in the DB. So they map to either an update or insert.
		 * 
		 * The DELETE is used for removing one or more entities from the DB. So
		 * it maps to a delete.
		 * 
		 * The bean must be assigned at least one of the methods to service
		 */
		Method method = null;
		try {
			method = Enum.valueOf(Method.class, request.getMethod()
					.toUpperCase());
			LOG.debug(getBeanName()
					+ ": handleRequestInternal - processing this method: "
					+ method.toString());
		} catch (IllegalArgumentException e) {
			LOG.error(getBeanName()
					+ ":handleRequestInternal - This method is not allowed ["
					+ request.getMethod() + "]");
			response.setHeader("Allow", allowedMethodsRsp);
			response.sendError(SC_METHOD_NOT_ALLOWED,
					"This method is not allowed [" + request.getMethod() + "]");
			return null;
		}

		// do some more method validation; i.e., make sure requested method has
		// been assigned a SQL statement
		//
		// TODO: we may be able to remove this block of code
		String s1 = null;
		if (method.isGet() && sqlStmnts4Get == null || method.isPost()
				&& sqlStmnts4Post == null || method.isPut()
				&& sqlStmnts4Put == null || method.isDelete()
				&& sqlStmnts4Delete == null) {
			response.setHeader("Allow", allowedMethodsRsp);
			s1 = "HTTP method [" + method + "] is not supported";
			response.sendError(SC_METHOD_NOT_ALLOWED, s1);
			LOG.error(getBeanName() + ":handleRequestInternal - " + s1);
			return null;
		}

		// If the client has specified an 'Accept' header field, then determine
		// if it is willing or capable of accepting JSON or anything (*/*)
		//
		// TODO: what about the client accepting urlencoded strings??
		s1 = request.getHeader(ACCEPT_HDR);
		if (s1 != null && s1.length() > 0) {
			LOG.debug(getBeanName()
					+ ":handleRequestInternal - client-specified media "
					+ "type in accept header = " + s1);
			// parse the accept header's content
			String[] mediaTypes = s1.trim().split(COMMA_STR);
			boolean match = false;
			for (String mediaType : mediaTypes) {
				mediaType = mediaType.trim().toLowerCase();
				if (mediaType.startsWith(anyContentType)
						|| mediaType.startsWith(jsonContentType)) {
					match = true;
					break;
				}
			}
			if (!match) {
				LOG.error(getBeanName()
						+ ":handleRequestInternal - client-specified media type of '"
						+ s1 + "' does not include '" + "'" + jsonContentType);
				response.sendError(SC_NOT_ACCEPTABLE, "client-specified media "
						+ "type of '" + s1 + "' does not include '" + "'"
						+ jsonContentType);
				return null;
			}
		}

		// pick up the corresponding list of SQL statements for this request
		List<SqlStmnt> sqlStmnts = null;
		switch (method) {
		case GET:
			sqlStmnts = getSqlStmnts4Get();
			break;
		case DELETE:
			sqlStmnts = getSqlStmnts4Delete();
			break;
		case PUT:
			sqlStmnts = getSqlStmnts4Put();
			break;
		case POST:
			sqlStmnts = getSqlStmnts4Post();
			break;
		default:
			response.sendError(SC_METHOD_NOT_ALLOWED,
					"ERROR, unsupported method type: " + method);
			LOG.error(getBeanName()
					+ ": handleRequestInternal - ERROR, encountered unknown "
					+ "method type: " + method);
			return null;
		}

		// ~~~~~~ EXTRACT PARAMERTERS, IF ANY ~~~~~~~~~~~

		// GETs with entity bodies are illegal
		if (method.isGet() && request.getContentLength() > 0) {
			response.sendError(SC_BAD_REQUEST,
					"Client has issued a malformed or illegal request; "
							+ "GET cannot include entity body");
			return null;
		}

		// the DELETE method also cannot include an entity body; however, the
		// servlet containers already ignore them. so no need to check for that

		// see if json object arrived
		boolean jsonObjectPresent = (method.isPost() || method.isPut())
				&& (request.getContentLength() > 0 && request.getContentType()
						.equalsIgnoreCase(jsonContentType));

		LOG.debug(getBeanName() + ": jsonObjectPresent = " + jsonObjectPresent);

		// see if this is a PUT with entity. we've learned that for PUTs,
		// getParameterMap does not work the same across all servlet containers.
		// so we need take care of this ourselves
		boolean putWithBodyPresent = (method.isPut())
				&& (request.getContentLength() > 0 && request.getContentType()
						.equalsIgnoreCase(urlEncodedContentType));

		LOG.debug(getBeanName() + ": putWithBodyPresent = "
				+ putWithBodyPresent);

		// collect incoming parameters and place them in a common bucket
		//
		// ~~~~ ALL PARAMETER KEY NAMES MUST BE FORCED TO LOWER CASE ~~~
		//
		List<Map<String, String>> cParams = new ArrayList<Map<String, String>>();

		// first, get the incoming query or form parameters (if any); we will
		// assume that each key has only one parameter. in other words,
		// we're not dealing with drop-down boxes or things similar
		if (!putWithBodyPresent && !jsonObjectPresent) {
			Map<String, String[]> qParams = request.getParameterMap();
			if (qParams != null && !qParams.isEmpty()) {
				Map<String, String> qMap = new HashMap<String, String>();
				for (String key : qParams.keySet()) {
					qMap.put(key.toLowerCase(), qParams.get(key)[0]);
				}
				if (!qMap.isEmpty()) {
					cParams.add(qMap);
					LOG.debug(getBeanName() + ": query params = "
							+ qMap.toString());
				}
			}
		}

		// a put with entity body arrived, so get the parameters from the
		// body and place them in the common bucket
		else if (putWithBodyPresent) {

			try {
				Map<String, String> putParams = null;
				// parseUrlEncoded will force keys to lower case
				putParams = Utils.parseUrlEncoded(request.getInputStream());
				if (putParams != null && !putParams.isEmpty()) {
					cParams.add(putParams);
				}
			} catch (Exception exc) {
				LOG.error(getBeanName() + ": ERROR, caught this "
						+ "exception while parsing urlencoded string: "
						+ exc.toString());
				LOG.error(getBeanName() + ": exception stack trace follows:");
				dumpStackTrace(exc.getStackTrace());
				if (exc.getCause() != null) {
					LOG.error(getBeanName() + ": Caused by "
							+ exc.getCause().toString());
					LOG.error(getBeanName()
							+ ": causing exception stack trace follows:");
					dumpStackTrace(exc.getCause().getStackTrace());
				}
				response.sendError(SC_BAD_REQUEST,
						"urlencoded string parsing error: " + exc.getMessage());
				return null;
			}
		}

		// ok, a json object arrived, so get parameters defined in that object
		// and place them in the common bucket
		else {
			// its a json object, so parse it to extract params from it
			try {
				List<Map<String, String>> jParams = null;
				// parseJson will ensure that all passed-in JSON objects have
				// the same set of identical keys
				jParams = Utils.parseJson(request.getInputStream());
				if (jParams != null && !jParams.isEmpty()) {
					// if we also got query params then ensure they have the
					// same set of keys as the json params. why anyone would
					// ever do this is beyond me, but I'll leave it in for now
					if (!cParams.isEmpty()) {
						Map<String, String> cMap = cParams.get(0);
						Map<String, String> jMap = jParams.get(0);
						for (String key : cMap.keySet()) {
							if (jMap.get(key) == null) {
								String eStr = getBeanName()
										+ ": ERROR, json "
										+ "object key set does not match query "
										+ "param key set";
								LOG.error(eStr);
								response.sendError(SC_BAD_REQUEST, eStr);
								return null;
							}
						}
						// place the passed in query params in the jParams
						// bucket
						jParams.add(cMap);
					}
					// assign the jParams bucket to the common bucket
					cParams = jParams;
				}
			} catch (Exception exc) {
				LOG.error(getBeanName() + ": ERROR, caught this "
						+ "exception while parsing json object: "
						+ exc.toString());
				LOG.error(getBeanName() + ": exception stack trace follows:");
				dumpStackTrace(exc.getStackTrace());
				if (exc.getCause() != null) {
					LOG.error(getBeanName() + ": Caused by "
							+ exc.getCause().toString());
					LOG.error(getBeanName()
							+ ": causing exception stack trace follows:");
					dumpStackTrace(exc.getCause().getStackTrace());
				}
				response.sendError(SC_BAD_REQUEST,
						"json parsing error: " + exc.getMessage());
				return null;
			}
		}

		// if trace is on, dump the params (if any) to the log
		if (LOG.isDebugEnabled()) {
			if (!cParams.isEmpty()) {
				for (int i = 0; i < cParams.size(); i++) {
					LOG.debug(getBeanName()
							+ ": handleRequestInternal - received these params: "
							+ cParams.get(i).toString());
				}
			} else {
				LOG.debug(getBeanName()
						+ ": handleRequestInternal - did not receive any params");
			}
		}

		// ensure none of the params' values have been black listed
		if (!cParams.isEmpty() && getBlackList().length() > 0) {
			char[] bl = getBlackList().toCharArray();
			for (int i = 0; i < cParams.size(); i++) {
				for (String value : cParams.get(i).values()) {
					if (Utils.isOnBlackList(value, bl)) {
						response.sendError(SC_BAD_REQUEST,
								"encountered black listed character in this param "
										+ "value: " + value);
						LOG.error(getBeanName()
								+ "handleRequestInternal - encountered black listed "
								+ "character in this param value: " + value);
						return null;
					}

				}
			}
		}

		// find the proper SQL statement based on the incoming parameters' (if
		// any) keys
		SqlStmnt sqlStmnt = null;
		try {
			// getMatch will try and find a match, even if no params were
			// provided.
			// @formatter:off
			sqlStmnt = (cParams.isEmpty()) 
					? SqlStmnt.getMatch(sqlStmnts, null) 
					: SqlStmnt.getMatch(sqlStmnts, cParams.get(0).keySet());
			// @formatter:on

			if (sqlStmnt == null && !cParams.isEmpty()) {
				LOG.error(getBeanName() + ":ERROR, unable to find sql "
						+ "statement with this incoming param set: "
						+ cParams.toString());
				response.sendError(SC_INTERNAL_SERVER_ERROR,
						"internal server error: mapping error");
				return null;
			} else if (sqlStmnt == null) {
				LOG.warn(getBeanName() + ": warning, unable to find sql "
						+ "statement on first pass, will use extra path info");
			} else {
				LOG.debug(getBeanName()
						+ ": handleRequestInternal - matching sql stmt = "
						+ sqlStmnt.toString());
			}
		} catch (Exception exc) {
			LOG.error(getBeanName() + ":ERROR, caught this exception "
					+ "while mapping sql to params: " + exc.toString());
			LOG.error(getBeanName() + ": exception stack trace follows:");
			dumpStackTrace(exc.getStackTrace());
			if (exc.getCause() != null) {
				LOG.error(getBeanName() + ": Caused by "
						+ exc.getCause().toString());
				LOG.error(getBeanName()
						+ ": causing exception stack trace follows:");
				dumpStackTrace(exc.getCause().getStackTrace());
			}
			response.sendError(SC_INTERNAL_SERVER_ERROR, "mapping error");
			return null;
		}

		// if getMatch could not find a match - perhaps input params were not
		// provided - then use the URI's 'extended path' information as an input
		// param
		if (sqlStmnt == null) {
			LOG.debug(getBeanName() + ": invoking getExtraPathInfo");
			String[] xtraPathInfo = Utils.getExtraPathInfo(request
					.getPathInfo());
			if (xtraPathInfo != null && xtraPathInfo.length >= 2) {
				LOG.debug(getBeanName() + ": extra path key:value = "
						+ xtraPathInfo[0] + ":" + xtraPathInfo[1]);
			} else {
				LOG.error(getBeanName()
						+ ":ERROR, getExtraPathInfo failed to find info");
				response.sendError(SC_INTERNAL_SERVER_ERROR,
						"internal server error: mapping error");
				return null;
			}
			// put the xtra path info in the common param bucket and try again
			cParams.clear();
			Map<String, String> xMap = new HashMap<String, String>();
			xMap.put(xtraPathInfo[0], xtraPathInfo[1]);
			cParams.add(xMap);
			// try again with the extra path info
			sqlStmnt = SqlStmnt.getMatch(sqlStmnts, xMap.keySet());
			if (sqlStmnt == null) {
				LOG.error(getBeanName() + ":ERROR, unable to find sql "
						+ "statement with this xtra path info: "
						+ cParams.toString());
				response.sendError(SC_NOT_FOUND,
						"internal server error: mapping error");
				return null;
			}
		}

		// if we've gotten this far, we've gotten past the security gauntlet and
		// we have a SQL statement to work with.
		SqlResult sqlResult = null;
		try {
			// get the output stream
			OutputStream os = response.getOutputStream();

			// FIRE IN THE DB HOLE :)
			if ((sqlResult = sqlStmnt.execute(cParams)) == null) {
				// execute will have logged the necessary debug/error info
				response.sendError(SC_INTERNAL_SERVER_ERROR);
				return null;
			}

			// execute went through ok, lets see how to respond
			switch (method) {
			case GET:
				// if a resultset was returned, then set the content type,
				// convert it to json, and write it out
				List<Map<String, Object>> listMap = sqlResult.getResultSet();
				if (listMap != null) {
					// tell the client the content type
					response.setContentType(rspJsonContentType);
					String jsonOutput = Utils.generateJson(sqlResult
							.getResultSet());
					LOG.trace(getBeanName() + ": returning this payload - "
							+ jsonOutput);
					os.write(jsonOutput.getBytes());

					// ensure that only the client can cache the data and tell
					// the client how long the data can remain active
					response.setHeader(CACHE_CNTRL_HDR,
							(getCacheControl() != null) ? getCacheControl()
									: DFLT_CACHE_CNTRL_STR);
					response.setHeader(PRAGMA_HDR, PRAGMA_NO_CACHE_STR);					
					response.setDateHeader(EXPIRES_HDR, currentTime
							+ (getExpires() * 1000));
				} else {
					LOG.debug(getBeanName() + ": NOT returning json message");
				}
				response.setStatus(SC_OK);
				break;
			case DELETE:
				// a DELETE should not send back an entity body
				response.setStatus(SC_NO_CONTENT);
				break;
			case PUT:
				/*
				 * PUTs are idempotent; therefore, they must provide ALL the
				 * properties that pertain to the resource/entity that they are
				 * creating or updating. Updates cannot be partial updates; they
				 * must be full updates. A PUT is issued by a client that knows
				 * the identifier (in our case, primary key) of the
				 * resource/entity. Therefore, we do not have to send back a
				 * Location header in response to a PUT that has created a
				 * resource.
				 */
				if (sqlStmnt.isInsert()) {
					response.setStatus(SC_CREATED);
				} else {
					response.setStatus(SC_OK);
				}
				break;
			case POST:
				/*
				 * A POST is not idempotent; therefore, it can be used to
				 * perform a 'partial' update, as well as a full create. When
				 * creating a resource via POST, the client does not know the
				 * primary key, and it assumes it will be auto-generated;
				 * therefore, a Location header with auto-generated key must be
				 * returned to client.
				 */
				if (sqlStmnt.isInsert()) {
					response.setStatus(SC_CREATED);
					// we need to return the new key, but only if it was not a
					// batch insert. the new key should be returned via the
					// location header

					// check if a key holder exists; if not, then table was not
					// configured with auto-generated key.
					String locationPath = request.getRequestURL().toString();
					if (sqlResult.getKeyHolder() != null) {
						// key holder exists, check and see if a key is
						// present
						if (sqlResult.getKeyHolder().getKey() != null) {
							String id = sqlResult.getKeyHolder().getKey()
									.toString();
							LOG.debug(getBeanName() + ": getKey() returns "
									+ id);
							locationPath += ("/" + id);
							LOG.debug(getBeanName() + ": locationPath = "
									+ locationPath);
							response.setHeader(LOCATION_HDR, locationPath);
						}
						// no key, check for multiple keys
						// TODO: should we send back all keys?
						else if (sqlResult.getKeyHolder().getKeys() != null) {
							Map<String, Object> keyMap = sqlResult
									.getKeyHolder().getKeys();
							LOG.debug(getBeanName() + ": getKeys() returns "
									+ keyMap);
						}
						// maybe map of keys?
						// TODO: should we send back all keys?
						else if (sqlResult.getKeyHolder().getKeyList() != null) {
							for (Map<String, Object> map : sqlResult
									.getKeyHolder().getKeyList()) {
								LOG.debug(getBeanName()
										+ ": Map from getKeyList(): " + map);
							}
						}
					} else {
						// if it was not an insert, then it was an update.
						LOG.debug(getBeanName()
								+ ": key holder was not returned for the insert");
					}
				} else {
					// it was not an insert, so just send back an OK for the
					// update
					response.setStatus(SC_OK);
				}
				break;
			default:
				response.setStatus(SC_OK);
				break;
			}
		} catch (JsonProcessingException exc) {
			LOG.error(getBeanName() + ":ERROR, caught this "
					+ "JsonProcessingException while trying to gen json "
					+ "message: " + exc.toString());
			LOG.error(getBeanName() + ": exception stack trace follows:");
			dumpStackTrace(exc.getStackTrace());
			if (exc.getCause() != null) {
				LOG.error(getBeanName() + ": Caused by "
						+ exc.getCause().toString());
				LOG.error(getBeanName()
						+ ": causing exception stack trace follows:");
				dumpStackTrace(exc.getCause().getStackTrace());
			}
			response.sendError(SC_INTERNAL_SERVER_ERROR, "parsing error");
			return null;
		} catch (Exception exc) {
			LOG.error(getBeanName() + ":ERROR, caught this "
					+ "Exception while trying to gen json " + "message: "
					+ exc.toString());
			LOG.error(getBeanName() + ": exception stack trace follows:");
			dumpStackTrace(exc.getStackTrace());
			if (exc.getCause() != null) {
				LOG.error(getBeanName() + ": Caused by "
						+ exc.getCause().toString());
				LOG.error(getBeanName()
						+ ": causing exception stack trace follows:");
				dumpStackTrace(exc.getCause().getStackTrace());
			}
			response.sendError(SC_INTERNAL_SERVER_ERROR, "parsing error");
			return null;

		} finally {
			if (sqlResult != null) {
				SqlResult.enqueue(sqlResult);
			}
		}

		// must return null, because we're not using views!
		return null;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getServletName() {
		return servletName;
	}

	public void setServletName(String servletName) {
		this.servletName = servletName;
	}

	public HazelcastInstance getHazelcastInstance() {
		return hazelcastInstance;
	}

	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}
}
