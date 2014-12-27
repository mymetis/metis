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
package org.metis.push;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.DataSource;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.socket.CloseStatus;
import static org.springframework.web.socket.CloseStatus.*;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.metis.sql.SqlStmnt;
import org.metis.sql.SqlJob;
import org.metis.utils.Utils;
import org.metis.MetisController;
import static org.metis.utils.Statics.*;
import static org.metis.sql.SqlStmnt.getSQLStmnt;
import com.hazelcast.core.HazelcastInstance;

/**
 * A Spring MVC-loaded WebSocketHandler. The HandlerMapper invokes this bean to
 * service a web socket service request. This class represents a Spring MVC
 * Controller.
 * 
 */
public class PusherBean extends TextWebSocketHandler implements
		InitializingBean, BeanNameAware, DisposableBean, MetisController {

	public static final Log LOG = LogFactory.getLog(PusherBean.class);

	/**
	 * The string representations of the SQL statements assigned to this
	 * controller bean
	 */
	private List<String> sqls4Get;

	/**
	 * The SQL statements assigned to this controller bean
	 */
	private List<SqlStmnt> sqlStmnts4Get;

	/**
	 * The initial capacity for the session registry
	 */
	private int initCapacity = 50;

	/**
	 * The session registry
	 */
	private Hashtable<String, WdsSocketSession> wdsSessions;

	/**
	 * The Spring JDBC Template for this pusher bean
	 */
	private JdbcTemplate jdbcTemplate;

	/**
	 * The DataSource (connection cache) used for creating the jdbcTemplate.
	 */
	private DataSource dataSource;

	/**
	 * The name of this bean per the Spring application context
	 */
	private String beanName = "";

	/**
	 * The name of the JDBC Driver being used.
	 */
	private String driverName;

	/**
	 * The URL being used to connect to the DB.
	 */
	private String dbUrl;

	/**
	 * If true, indicates that this pusher is using an Oracle driver.
	 */
	private boolean isOracle;

	/**
	 * Used to let the MetisServlet know whether this controller was able to
	 * successully acquire a jdbc connection during startup
	 */
	private boolean dbConnectionAcquired;

	/**
	 * Lock used to synchronize access to SqlStmnts and their SqlJobs
	 */
	private static Lock mainLock = new ReentrantLock();

	/**
	 * The name of the MetisServlet that this PushBean belongs to.
	 */
	private String servletName;

	/**
	 * The optional Hazelcast instance used by this controller
	 */
	private HazelcastInstance hazelcastInstance;

	public PusherBean() {
		super();
	}

	@Override
	/**
	 * This method handles an incoming message from the web socket client. 
	 */
	public void handleTextMessage(WebSocketSession session, TextMessage message)
			throws Exception {

		if (session == null) {
			LOG.error(getBeanName() + ": null session");
			throw new Exception(getBeanName()
					+ ":handleTextMessage, null session was received");
		}

		// the session should be in the registry
		WdsSocketSession wdsSession = getWdsSessions().get(session.getId());
		if (wdsSession == null) {
			LOG.error(getBeanName()
					+ ":handleTextMessage, session with this id is not in registry: "
					+ session.getId());
			session.close(new CloseStatus(SERVER_ERROR.getCode(),
					"ERROR, session with this id not in registry: "
							+ session.getId()));
			return;
		}

		// some sort of message should have been received
		if (message == null) {
			LOG.error(getBeanName()
					+ ":handleTextMessage, null message parameter");
			session.close(new CloseStatus(POLICY_VIOLATION.getCode(),
					"ERROR, session with this id gave a null message "
							+ "parameter: " + session.getId()));
			return;
		}

		// we're supposed to receive a JSON object
		String jsonMsg = message.getPayload();

		if (jsonMsg == null) {
			LOG.error(getBeanName()
					+ ":handleTextMessage, getPayload returns null or empty string");
			session.close(new CloseStatus(POLICY_VIOLATION.getCode(),
					"ERROR, session with this id did not return a payload: "
							+ session.getId()));
			return;
		}

		if (jsonMsg.isEmpty()) {
			LOG.error(getBeanName()
					+ ":handleTextMessage, getPayload returns zero-length string");
			session.close(new CloseStatus(POLICY_VIOLATION.getCode(),
					"ERROR, session with this id returns zero-length payload: "
							+ session.getId()));
			return;
		}

		// dump the request if trace is on
		if (LOG.isTraceEnabled()) {
			LOG.trace(getBeanName() + ":***** processing new request *****");
			LOG.trace(getBeanName() + ":session id = " + session.getId());
			LOG.trace(getBeanName() + ":session remote address = "
					+ session.getRemoteAddress().toString());
			LOG.trace(getBeanName() + ":session uri  = "
					+ session.getUri().toString());
			LOG.trace(getBeanName() + ":session json object = " + jsonMsg);
		}

		// parse the json object
		List<Map<String, String>> jParams = null;
		try {
			jParams = Utils.parseJson(jsonMsg);
		} catch (Exception exc) {
			LOG.error(getBeanName() + ":caught this "
					+ "exception while parsing json object: " + exc.toString());
			LOG.error(getBeanName() + ": exception stack trace follows:");
			dumpStackTrace(exc.getStackTrace());
			if (exc.getCause() != null) {
				LOG.error(getBeanName() + ": Caused by "
						+ exc.getCause().toString());
				LOG.error(getBeanName()
						+ ": causing exception stack trace follows:");
				dumpStackTrace(exc.getCause().getStackTrace());
			}

			session.close(new CloseStatus(SERVER_ERROR.getCode(),
					"ERROR, got this json parsing exception: "
							+ exc.getMessage()));
			return;
		}

		if (jParams == null || jParams.isEmpty()) {
			LOG.error(getBeanName() + ":json parser returns null or "
					+ "empty json array");
			session.close(new CloseStatus(SERVER_ERROR.getCode(),
					"ERROR, json parser returns null or empty json array"));
			return;
		}

		// if trace is on, dump the params (if any) to the log
		if (LOG.isDebugEnabled()) {
			LOG.debug(getBeanName()
					+ ": handleRequestInternal, received these params: "
					+ jParams.get(0).toString());
		}

		// get the command portion of the json message
		Map<String, String> map = jParams.get(0);
		String command = map.remove(WS_COMMAND);
		if (command == null) {
			LOG.error(getBeanName() + ":command field not present");
			session.close(POLICY_VIOLATION);
			session.close(new CloseStatus(POLICY_VIOLATION.getCode(),
					"ERROR, command string not present or improperly set: "
							+ command));
			return;
		}

		if (!command.equals(WS_SUBSCRIBE) && !command.equals(WS_PING)) {
			LOG.error(getBeanName() + ":received this unknown command = "
					+ command);
			session.close(POLICY_VIOLATION);
			session.close(new CloseStatus(POLICY_VIOLATION.getCode(),
					"ERROR, received this unknown command =  " + command));
			return;
		}

		// Get the SQL Job, if any, that this session is currently subscribed to
		SqlJob job = wdsSession.getMyJob();

		// if this is a ping command, return session's current subscription
		if (command.equals(WS_PING)) {
			LOG.debug(getBeanName() + ":received ping command");
			List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
			Map<String, Object> map0 = new HashMap<String, Object>();
			if (job != null) {
				LOG.debug(getBeanName() + ": client is subscribed");
				map0.put(WS_STATUS, WS_SUBSCRIBED);
				map = job.getParams();
				if (map != null && !map.isEmpty()) {
					for (String key : map.keySet()) {
						map0.put(key, map.get(key));
					}
				}
			} else {
				LOG.debug(getBeanName() + ": client is not subscribed");
				map0.put(WS_STATUS, WS_OK);
			}
			response.add(map0);
			// send response back to client
			session.sendMessage(new TextMessage(Utils.generateJson(response)));
			return;
		}

		// find a sql statement that matches the incoming session request's
		// params
		SqlStmnt sqlStmnt = (map == null || map.isEmpty()) ? SqlStmnt.getMatch(
				getSqlStmnts4Get(), null) : SqlStmnt.getMatch(
				getSqlStmnts4Get(), map.keySet());

		// if getMatch could not find a match, then return error to client
		if (sqlStmnt == null) {
			LOG.error(getBeanName() + ":ERROR, unable to find sql "
					+ "statement with this map: " + map.toString());
			List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
			Map<String, Object> map0 = new HashMap<String, Object>();
			if (map != null && !map.isEmpty()) {
				for (String key : map.keySet()) {
					map0.put(key, map.get(key));
				}
			}
			map0.put(WS_STATUS, WS_NOT_FOUND);
			// send response back to client
			session.sendMessage(new TextMessage(Utils.generateJson(response)));
			return;
		}

		// other than a ping, the only other command from the client is a
		// subscription command

		// Does this session already exist in one of the sql jobs? Note that the
		// client can switch subscriptions.
		if (job != null) {
			// the session pertains to a job, but does that job's map match
			// that of this session's subscription request
			if (job.isParamMatch(map)) {
				// if so, we're done
				return;
			} else {
				// else remove this session from that job - the client is
				// switching subscriptions
				job.removeSession(wdsSession.getId());
			}
		}

		mainLock.lock();
		try {
			// if we've gotten this far, the session does not pertain to a job
			// or it is a subscription change. so we now need to find an
			// existing job whose params match that of the incoming session. if
			// no job was found, then create and start one
			if (sqlStmnt.findSqlJob(map, wdsSession) == null) {
				sqlStmnt.createSqlJob(map, wdsSession);
			}
		} finally {
			mainLock.unlock();
		}
	}

	public boolean supportsPartialMessages() {
		return false;
	}

	/**
	 * This method is called by the web socket container after an existing
	 * session has been closed.
	 */
	public void afterConnectionClosed(WebSocketSession session,
			CloseStatus status) throws Exception {

		super.afterConnectionClosed(session, status);

		LOG.debug(getBeanName() + ": afterConnectionClosed, session id = "
				+ session.getId() + ", status = " + status.toString());

		// remove the session from the session registry
		WdsSocketSession wdsSession = getWdsSessions().remove(session.getId());
		if (wdsSession == null) {
			LOG.warn(getBeanName()
					+ ":afterConnectionClosed - this session did not exist in registry: "
					+ session.getId());
			return;
		}

		// get the sql job that the session had been subsribed to and remove the
		// session from that job
		SqlJob job = wdsSession.getMyJob();
		if (job != null) {
			job.removeSession(session.getId());
		}

	}

	/**
	 * This method is called by the web socket container after a new
	 * connection/session has been established with this server endpoint. This
	 * method will attempt to subscribe the client based on any query params and
	 * if no params were provide, will use the URL's extra path info.
	 */
	public void afterConnectionEstablished(WebSocketSession session)
			throws Exception {

		super.afterConnectionEstablished(session);

		LOG.debug(getBeanName() + ":afterConnectionEstablished - session id = "
				+ session.getId());

		// place the session in the global session registry. it will be removed
		// from the registry when it is closed
		WdsSocketSession wds = new WdsSocketSession(session);
		getWdsSessions().put(session.getId(), wds);

		// based on the query string (if any), attempt to find a SqlStmnt for
		// this session
		Map<String, String> map = Utils
				.getQueryMap(session.getUri().getQuery());

		SqlStmnt sqlStmnt = (map == null || map.isEmpty()) ? SqlStmnt.getMatch(
				getSqlStmnts4Get(), null) : SqlStmnt.getMatch(
				getSqlStmnts4Get(), map.keySet());

		// if statement was not found and query params were provided, then
		// close connection because search for statement had to take place based
		// on query params provided. in other words, if client provides query
		// params then it is requesting a subscrption via those params.
		if (sqlStmnt == null && (map != null && !map.isEmpty())) {
			LOG.error(getBeanName()
					+ ":afterConnectionEstablished - unable to find sql "
					+ "statement with this incoming param set: "
					+ map.toString());
			session.close(POLICY_VIOLATION);
			return;
		}

		// if statement was not found and params were not provided, then attempt
		// to find statement based on the url's "extra path" info
		if (sqlStmnt == null) {
			LOG.trace(getBeanName()
					+ ":afterConnectionEstablished - unable to find sql "
					+ "statement on first pass, will attempt to use extra path info");
			String[] xtraPathInfo = Utils.getExtraPathInfo(session.getUri()
					.toString());
			if (xtraPathInfo != null && xtraPathInfo.length >= 2) {
				LOG.debug(getBeanName() + ": extra path key:value = "
						+ xtraPathInfo[0] + ":" + xtraPathInfo[1]);
				// put the xtra path info in the bucket and try again
				map = (map == null) ? new HashMap<String, String>() : map;
				map.clear();
				map.put(xtraPathInfo[0], xtraPathInfo[1]);
				// try again with the extra path info
				sqlStmnt = SqlStmnt.getMatch(getSqlStmnts4Get(), map.keySet());
				// if statement could not be found, then simply return - client
				// may later subscribe with valid params
				if (sqlStmnt == null) {
					LOG.debug(getBeanName()
							+ ":findStmnt - unable to find sql "
							+ "statement with this xtra path info: "
							+ map.toString());
					return;
				}
			} else {
				// there was no extra path info, so simply return
				return;
			}
		}

		mainLock.lock();
		try {
			// if we've gotten this far, a SqlStmnt was found based on query or
			// extra path info, now see if SqlStmnt already has a job with the
			// same param set (map). if no job was found, then create and start
			// one
			if (sqlStmnt.findSqlJob(map, wds) == null) {
				sqlStmnt.createSqlJob(map, wds);
			}
		} finally {
			mainLock.unlock();
		}
	}

	/**
	 * Invoked by the BeanFactory on destruction of this singleton.
	 */
	public void destroy() {
		for (SqlStmnt stmnt : getSqlStmnts4Get()) {
			stmnt.destroy();
		}
	}

	/**
	 * Called by Spring after all of this bean's properties have been set.
	 */
	public void afterPropertiesSet() throws Exception {

		// create the session registry
		setWdsSessions(new Hashtable<String, WdsSocketSession>(
				getInitCapacity()));

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
					// record the URL to the DB
					setDbUrl(dbmd.getURL().trim());
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
					LOG.info(getBeanName() + ":JDBC URL = " + getDbUrl());
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
		if (getSqls4Get() == null) {
			throw new Exception(
					"The PusherBean must be assigned at least one SQL statement");
		}

		// create and validate the injected SQL statements

		sqlStmnts4Get = new ArrayList<SqlStmnt>();
		for (String sql : getSqls4Get()) {
			// get the frequency settings
			Map<String, Long> map = Utils.parseTimeInterval(sql);
			sql = Utils.stripTimeInterval(sql);
			sql = Utils.stripCall(sql);
			SqlStmnt stmt = getSQLStmnt(this, sql, getJdbcTemplate());

			if (stmt.isEqual(sqlStmnts4Get)) {
				throw new Exception(
						"Injected SQL statements for GET are not distinct");
			}
			// set the frequency
			stmt.setIntervalTime(map.get(TIME_INTERVAL));
			if (map.get(TIME_INTERVAL_MAX) != null) {
				stmt.setIntervalMax(map.get(TIME_INTERVAL_MAX));
			}
			if (map.get(TIME_INTERVAL_STEP) != null) {
				stmt.setIntervalStep(map.get(TIME_INTERVAL_STEP));
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

		if (getHazelcastInstance() != null) {
			LOG.debug(getBeanName() + ": My Hazelcast Instance Name = "
					+ getHazelcastInstance().getName());
			;
		}
	}

	/**
	 * Used for validating SQL statement for GET method. The Pusher bean
	 * requires a statement as follows:
	 * 
	 * <sql statmenet>[time interval]
	 * 
	 * For example, select * from table [180]
	 * 
	 * @param sql
	 * @return
	 * @throws IllegalArgumentException
	 */
	private String valSql4Get(String sql) throws IllegalArgumentException,
			NumberFormatException {
		if (sql != null && sql.length() > 0) {
			// make sure statement ends with a proper frequency setting
			Utils.parseTimeInterval(sql);
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
	 * the SQL statement. The Pusher bean supports only the GET method.
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

	public List<SqlStmnt> getSqlStmnts4Get() {
		return sqlStmnts4Get;
	}

	// -----------------------

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

	/**
	 * Called by Spring to set the name of this bean
	 */
	public void setBeanName(String name) {
		beanName = name;
	}

	public String getBeanName() {
		return beanName;
	}

	public boolean isOracle() {
		return isOracle;
	}

	public boolean isDbConnectionAcquired() {
		return dbConnectionAcquired;
	}

	public void setDbConnectionAcquired(boolean dbConnectionAcquired) {
		this.dbConnectionAcquired = dbConnectionAcquired;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
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

	public Hashtable<String, WdsSocketSession> getWdsSessions() {
		return wdsSessions;
	}

	public void setWdsSessions(Hashtable<String, WdsSocketSession> wdsSessions) {
		this.wdsSessions = wdsSessions;
	}

	public int getInitCapacity() {
		return initCapacity;
	}

	public void setInitCapacity(int initCapacity) {
		this.initCapacity = initCapacity;
	}

	public String getServletName() {
		return servletName;
	}

	public void setServletName(String servletName) {
		this.servletName = servletName;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public HazelcastInstance getHazelcastInstance() {
		return hazelcastInstance;
	}

	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

}
