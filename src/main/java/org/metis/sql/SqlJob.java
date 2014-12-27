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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.util.HashMap;
import org.springframework.web.socket.TextMessage;
import org.metis.sql.SqlStmnt;
import org.metis.push.WdsSocketSession;
import org.metis.utils.Statics;
import org.metis.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static org.metis.utils.Statics.*;
import static org.metis.utils.Utils.dumpStackTrace;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

/**
 * The SqlJob is a runnable that is used for polling the DB at a specified
 * frequency. One or more web socket clients subscribe to a SqlJob in order to
 * be informed of a corresponding database change. Nothing precludes the web
 * socket client from subscribing to more than one SqlJob.
 * 
 * The signature of a SqlJob is defined by a parameterized SQL statement and the
 * parameter values that are bound to the SQL statement. Thus you can have many
 * SqlJobs spawned from this one parameterized SQL statement.
 * 
 * A SqlJob is spawned upon its first subscription and terminates when it has no
 * subscribers. Instead of terminating, a SqlJob could go dormant; however, the
 * number of possible distinct SqlJobs can, in theory, be infinite.
 * Subscriptions should be rather long in duration, so there shouldn't exist the
 * case where SqlJobs are constantly being spawned and terminated. If that is
 * the case, then perhpas the clients' design should be reviewed.
 * 
 * Each instance of a SqlJob is identified by the parameterized SQL statement
 * and the value of its bound parameters.
 * 
 * select * from student where lastname like concat(`char:name`,'%')
 * 
 * The frequency at which the SqlJob polls the DB is either static or grows from
 * a minimum to a maximum based on a step ratio. If the frequency is one that
 * grows, a change in the DB reverts it back to the starting frequency.
 * 
 * An instance of a SqlJob, which is part of a web application, can be
 * distributed across many servlet containers. If all of these distibuted
 * replicas all point to the same DB instance, the result can be redundant
 * polling. To counter this, the SqlJobs can be clustered using a distributed
 * framework such as Hazelcast. In such a case, the cluster master is the only
 * member of a cluster that performs the polling. If the master terminates,
 * another member of the cluster will take its place.
 * 
 * 
 */
public class SqlJob implements Runnable, MessageListener<String> {

	private static final Log LOG = LogFactory.getLog(SqlJob.class);

	/**
	 * A list of all the web socket clients that are subscribed to this job.
	 */
	private Hashtable<String, WdsSocketSession> socketSessions = new Hashtable<String, WdsSocketSession>();

	/**
	 * This job's thread.
	 */
	private Thread runner;

	/**
	 * Used for indicating whether the thread has started and/or is currently
	 * running
	 */
	private boolean started;

	/**
	 * The job's id
	 */
	private String id;

	/**
	 * The SHA (Secure Hash Algorithm) used for digitally signing result sets.
	 * It is also used for identifying the cluster that this SqlJob pertains to.
	 */
	private MessageDigest sha;

	/**
	 * The digital signature of the result set
	 */
	private String digitalSignature;

	/**
	 * Cluster hash used for identifying a cluster of identical SqlJobs.
	 */
	private String clusterHash;

	/**
	 * Unique name assigned to this job. It does not have to be globally unique.
	 * Formatted as follows: <pusher bean name>.sqljob.<sqlJobThreadCount>. For
	 * example, "oracle.sqljob.23".
	 */
	private String threadName;

	/**
	 * The param set assigned to this job.
	 */
	private Map<String, String> params;
	private List<Map<String, String>> lParams;

	/**
	 * Used for sending back responses to web socket clients
	 */
	private List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
	private Map<String, Object> responseMap = new HashMap<String, Object>();

	/**
	 * The SqlStmnt object that spawned this job.
	 */
	private SqlStmnt sqlStmnt;

	/**
	 * Used to indicate that a database change has been detected.
	 */
	private boolean changeDetected;

	/**
	 * Lock used for synchronizing access to the socketSessions.
	 */
	private Lock sessionsLock = new ReentrantLock();

	/**
	 * The optional Spring-loaded HazelCast instance.
	 */
	private HazelcastInstance hazelcastInstance;

	/**
	 * Hazelcast Lock used to define the cluster master. The SqlJob that
	 * acquires the lock becomes the cluster master and is responsible for
	 * polling the DB.
	 */
	private ILock masterLock;

	/**
	 * Hazelcast Topic used for subscribing to messages sent by the publishing
	 * cluster master.
	 */
	private ITopic<String> topic;

	/**
	 * Registration id for topic - only used if this SqlJob is not a cluster
	 * master
	 */
	private String topicRegistrationId;

	/**
	 * The Message that is published to a Topic by the cluster master
	 */
	private Message<String> topicMessage;

	/**
	 * Create a SqlJob
	 * 
	 * @param sqlStmnt
	 *            the SqlStmnt that spawns this job
	 * @param params
	 *            the params that are bound to the SqlStmnt
	 * @param id
	 *            used for defining this job's id
	 * @throws Exception
	 */
	public SqlJob(SqlStmnt sqlStmnt, Map<String, String> params, String id)
			throws Exception {
		try {
			// create the hash function for this job
			setSha(MessageDigest.getInstance("SHA-256"));
		} catch (Exception e) {
			throw new Exception(
					"ERROR, sha-256 hash function was not acquired. Cause = "
							+ e.getMessage());
		}
		setSqlStmnt(sqlStmnt);
		if (params != null && params.size() > 0) {
			setParams(params);
			setlParams(new ArrayList<Map<String, String>>());
			getlParams().add(params);
		}
		setId(id);
		sqlStmnt.addSqlJob(this);
		setThreadName(getPusherBeanName() + ".sqljob." + id);
		// preload the response
		getResponse().add(getResponseMap());

		// create the string that will be used to identify the cluster of
		// SqlJobs that this SqlJob belongs to. The cluster is identified with
		// the following fields: 1. serlvet name 2. DB URL 3. sql statment 4.
		// sql statement's parameter values
		setClusterHash(sqlStmnt.getMetisController().getServletName()
				+ sqlStmnt.getMetisController().getDbUrl());

		for (SqlToken token : sqlStmnt.getTokens()) {
			setClusterHash(getClusterHash() + token.getValue());
			if (token.isKey()) {
				setClusterHash(getClusterHash() + token.getJdbcType());
			}
		}
		if (params != null) {
			for (String value : params.values()) {
				setClusterHash(getClusterHash() + value);
			}
		}
		// use SHA-256 to create the final cluster hash
		setClusterHash(getHashOf(getClusterHash()));

		// set the hazelcast instance if one has been wired to this SqlJob's
		// controller bean
		setHazelcastInstance(getSqlStmnt().getMetisController()
				.getHazelcastInstance());

		// if operating within a hazelcast instance, then get a reference to the
		// master cluster lock and the cluster topic
		if (inCluster()) {
			masterLock = getHazelcastInstance().getLock(getClusterHash());
			topic = getHazelcastInstance().getTopic(getClusterHash());
		}

	}

	public SqlJob(SqlStmnt sqlStmnt, String id) throws Exception {
		this(sqlStmnt, null, id);
	}

	/**
	 * Starts the internal thread.
	 * 
	 * @throws Exception
	 */
	public synchronized void doStart() {
		if (getRunner() != null && isStarted()) {
			return;
		}
		setRunner(new Thread(this, getThreadName()));
		getRunner().setDaemon(true);
		getRunner().setPriority(Thread.MIN_PRIORITY);
		getRunner().start();
	}

	/**
	 * Interrupts the thread
	 */
	public void doInterrupt() {
		if (getRunner() != null) {
			getRunner().interrupt();
		}
	}

	/**
	 * Stops the internal thread.
	 * 
	 */
	public synchronized void doStop() {
		if (!isStarted()) {
			return;
		}
		setStarted(false);
		if (runner != null) {
			runner.interrupt();
		}
		// divorce this job from its parent
		// sql statement
		sqlStmnt.removeSqlJob(this);
	}

	/**
	 * SqlJob's main execution block
	 */
	public void run() {

		long lastExecTime = 0L;
		long origIntervalTime = getSqlStmnt().getIntervalTime() * 1000;
		long intervalTime = origIntervalTime;
		long intervalMax = getSqlStmnt().getIntervalMax() * 1000;
		double intervalStep = 1.0 + getSqlStmnt().getIntervalStep() / 100.0;
		boolean clusterMaster = false;

		setStarted(true);

		if (LOG.isTraceEnabled()) {
			if (getParams() != null) {
				LOG.trace(getThreadName() + ": started with these params: "
						+ params.toString());
			}
			LOG.trace(getThreadName()
					+ ":started with intervalTime, intervalMax, intervalStep = "
					+ intervalTime + ", " + intervalMax + ", " + intervalStep);

			LOG.trace(getThreadName() + ": cluster hash = " + getClusterHash());

			LOG.trace(getThreadName() + ": using masterLock = "
					+ ((getMasterLock() != null) ? true : false));
		}

		try {

			while (true) {
				// If operating within a cluster and this thread is not a
				// cluster master, then attempt to acquire the master cluster
				// lock. If not acquired, then subscribe to the topic - if not
				// already dones so - that the master will publish to.
				if (inCluster() && !clusterMaster) {
					// attempt to become the clusterMaster
					clusterMaster = getMasterLock().tryLock();
					LOG.trace(getThreadName() + ": cluster master = "
							+ clusterMaster);
					if (!clusterMaster) {
						// attempt to become cluster master failed, so
						// subscibe to topic if not already subscribed
						if (getTopicRegistrationId() == null) {
							setTopicRegistrationId(getTopic()
									.addMessageListener(this));
						}
					}
					// we've become the cluster master, remove
					// subscription if any
					else if (getTopicRegistrationId() != null) {
						getTopic().removeMessageListener(
								getTopicRegistrationId());
						setTopicRegistrationId(null);
					}
				}

				// if this job has not been wired to a Hazelcast cluster-group
				// or it is the cluster master, then poll the DB
				if (!inCluster() || clusterMaster) {
					if (LOG.isTraceEnabled()) {
						LOG.trace(getThreadName() + ": polling the DB");
						if (lastExecTime > 0L) {
							LOG.trace(getThreadName()
									+ ": elapsed time (msec) since last execute = "
									+ (System.currentTimeMillis() - lastExecTime));
						}
						// record the execution time
						lastExecTime = System.currentTimeMillis();
					}
					// poll the database. if the polling routine throws an
					// exception, then stop the job and terminate; the polling
					// routine will have notified the clients of the fatal
					// error. if the polling polling routine returns non-null, a
					// database change has occurred and thus notify the clients
					// by publishing to the cluster topic
					try {
						String dSign = pollDB();
						if (dSign != null) {
							setChangeDetected(true);
							if (clusterMaster) {
								// notify subordinate threads
								getTopic().publish(dSign);
							}
						}
					} catch (Exception exc) {
						doStop();
						return;
					}
				}

				// after polling, and possibly sending change notification,
				// suspend the thread for the current interval time if a change
				// was detected, reset interval time to original interval time.
				intervalTime = (isChangeDetected()) ? origIntervalTime
						: intervalTime;
				setChangeDetected(false);
				LOG.trace(getThreadName() + ": sleeping with interval time = "
						+ intervalTime);
				try {
					Thread.sleep(intervalTime);
				} catch (InterruptedException ignore) {
				}

				// while sleeping, this thread may have been stopped
				if (!isStarted()) {
					LOG.trace(getThreadName() + ": SqlJob stopped");
					setStarted(false);
					getSqlStmnt().removeSqlJob(this);
					return;
				}

				// sweep out any zombie sessions
				sweepSessions();

				// if this job has no sessions, then terminate
				if (socketSessions.isEmpty()) {
					sessionsLock.lock();
					try {
						// make sure session did not sneak in
						if (socketSessions.isEmpty()) {
							LOG.trace(getThreadName()
									+ ": SqlJob stopping because it has no "
									+ "more sessions");
							doStop();
							return;
						}
					} finally {
						sessionsLock.unlock();
					}
				}

				// update the interval time, but only if this job is working
				// with a step
				if (intervalMax > 0L && intervalTime < intervalMax) {
					// step up the interval
					long step = (long) (intervalTime * intervalStep);
					// if max breached, then set to max
					intervalTime = (step < intervalMax) ? step : intervalMax;
					LOG.trace(getThreadName()
							+ ": SqlJob stepped up interval time = "
							+ intervalTime);
				}
			}
		} finally {
			// if this was the cluster master, then release the lock
			if (clusterMaster) {
				getMasterLock().unlock();
				clusterMaster = false;
			}

		}
	}

	/**
	 * This method gets called whenever the cluster master publishes a message
	 * to the cluster topic.
	 */
	@Override
	public void onMessage(Message<String> message) {
		setChangeDetected(true);
		setDigitalSignature(message.getMessageObject());
		sendChangeNotification(message.getMessageObject());
		doInterrupt();
	}

	/**
	 * Polls the DB and notifies clients of a change in the DB or a fatal error.
	 * 
	 * @return
	 * @throws Exception
	 */
	private String pollDB() throws Exception {

		SqlResult sqlResult = null;

		try {

			// execute the sql statement. if a sqlResult was not returned,
			// then an error occurred and this job must be considered
			// defunct.
			if ((sqlResult = sqlStmnt.execute(getlParams())) == null) {
				// execute will have logged the necessary debug/error info.
				// notify all subscribed clients, that an error has occurred
				// and that this job is being stopped
				LOG.error(getThreadName()
						+ ":ERROR, execute did not return a sqlResult object");
				sendInternalServerError("");
				throw new Exception("execute returns null sqlResult");
			}

			// sqlResult was returned, but it may not contain a result set
			List<Map<String, Object>> listMap = sqlResult.getResultSet();
			String jsonOutput = null;
			if (listMap == null || listMap.isEmpty()) {
				LOG.trace(getThreadName()
						+ ":sqlResult did not contain a result set");
			} else {
				// convert the result set to a json object
				jsonOutput = Utils.generateJson(listMap);
				if (LOG.isTraceEnabled()) {
					if (jsonOutput.length() > 100) {
						LOG.trace(getThreadName()
								+ ": first 100 bytes of acquired result set = "
								+ jsonOutput.substring(0, 100));
					} else {
						LOG.trace(getThreadName()
								+ ": acquired this result set - " + jsonOutput);
					}
				}
			}

			// get the digital signature of the json object (if any) that
			// represents the result set
			String dSign = (jsonOutput != null) ? getHashOf(jsonOutput)
					: WS_DFLT_SIGNATURE;
			LOG.trace(getThreadName() + ": acquired digital signature = "
					+ dSign);
			LOG.trace(getThreadName() + ": current  digital signature = "
					+ getDigitalSignature());

			// determine if a change has occurred
			if (getDigitalSignature() == null) {
				// first time, so just update the current digital signature
				setDigitalSignature(dSign);
			} else if (!dSign.equals(getDigitalSignature())) {
				// update the current digital signature
				setDigitalSignature(dSign);
				// ... and send the notification
				LOG.debug(getThreadName() + ": sending notification");
				sendChangeNotification(dSign);
				return getDigitalSignature();
			}

		} catch (JsonProcessingException exc) {
			LOG.error(getThreadName() + ":ERROR, caught this "
					+ "JsonProcessingException while trying to gen json "
					+ "message: " + exc.toString());
			LOG.error(getThreadName() + ": exception stack trace follows:");
			dumpStackTrace(exc.getStackTrace());
			if (exc.getCause() != null) {
				LOG.error(getThreadName() + ": Caused by "
						+ exc.getCause().toString());
				LOG.error(getThreadName()
						+ ": causing exception stack trace follows:");
				dumpStackTrace(exc.getCause().getStackTrace());
			}
			sendInternalServerError("");
			throw exc;

		} catch (Exception exc) {
			LOG.error(getThreadName() + ":ERROR, caught this "
					+ "Exception while trying to gen json " + "message: "
					+ exc.toString());
			LOG.error(getThreadName() + ": exception stack trace follows:");
			dumpStackTrace(exc.getStackTrace());
			if (exc.getCause() != null) {
				LOG.error(getThreadName() + ": Caused by "
						+ exc.getCause().toString());
				LOG.error(getThreadName()
						+ ": causing exception stack trace follows:");
				dumpStackTrace(exc.getCause().getStackTrace());
			}
			sendInternalServerError("");
			throw exc;
		} finally {
			if (sqlResult != null) {
				SqlResult.enqueue(sqlResult);
			}
		}

		return null;
	}

	/**
	 * Returns true if the given param set matches this job's param set.
	 * 
	 * Note: We could have just done a Map.equal, but we're ignoring case.
	 * 
	 */
	public boolean isParamMatch(Map<String, String> inParams) {
		// first ensure both maps are of the same size
		if (inParams == null) {
			return (getParams() == null) ? true : false;
		} else if (getParams() == null) {
			return false;
		} else if (inParams.size() != getParams().size()) {
			return false;
		}

		// both are of the same size, iterate through the key set ensuring all
		// key-values match
		for (String key : getParams().keySet()) {
			String value = getParams().get(key);
			String inValue = inParams.get(key);
			if (inValue == null || !value.equalsIgnoreCase(inValue)) {
				return false;
			}

		}
		return true;
	}

	/**
	 * Returns true if the given session's id exists in this job's collection of
	 * registered sessions
	 */
	public boolean sessionExists(WdsSocketSession inSession) {
		return getSocketSessions().get(inSession.getId()) != null;
	}

	public boolean sessionExists(String sessionId) {
		return getSocketSessions().get(sessionId) != null;
	}

	/**
	 * Removes the given session from this job's collection of registered
	 * sessions
	 * 
	 * @param inSession
	 * @return
	 */
	public WdsSocketSession removeSession(WdsSocketSession inSession) {
		return (inSession != null) ? removeSession(inSession.getId()) : null;
	}

	/**
	 * Remove the given session from this job and null out the job that the
	 * session pertains to.
	 * 
	 * @param sessionId
	 * @return
	 */
	public WdsSocketSession removeSession(String sessionId) {
		WdsSocketSession session = null;
		LOG.trace(getThreadName() + ": removing this session " + sessionId);
		if (sessionId != null) {
			session = getSocketSessions().remove(sessionId);
			if (session != null) {
				session.setMyJob(null);
			}
		}
		return session;
	}

	/**
	 * Add the given session to this job and assign this job to the session.
	 * 
	 * @param inSession
	 */
	public void addSession(WdsSocketSession inSession) {
		if (inSession != null) {
			LOG.trace(getThreadName() + ": adding this session "
					+ inSession.getId());
			getSocketSessions().put(inSession.getId(), inSession);
			inSession.setMyJob(this);
		}
	}

	/**
	 * Returns the name of the pusher bean that owns this job.
	 * 
	 * @return
	 */
	public String getPusherBeanName() {
		return getSqlStmnt().getMetisController().getBeanName();
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public SqlStmnt getSqlStmnt() {
		return sqlStmnt;
	}

	public void setSqlStmnt(SqlStmnt sqlStmnt) {
		this.sqlStmnt = sqlStmnt;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public Hashtable<String, WdsSocketSession> getSocketSessions() {
		return socketSessions;
	}

	public void setSocketSessions(
			Hashtable<String, WdsSocketSession> socketSessions) {
		this.socketSessions = socketSessions;
	}

	/**
	 * Removes all closed sessions from the collection of registered sessions
	 */
	private void sweepSessions() {
		LOG.trace(getThreadName() + ": sweeping sessions");
		for (Enumeration<String> e = getSocketSessions().keys(); e
				.hasMoreElements();) {
			String key = e.nextElement();
			WdsSocketSession session = getSocketSessions().get(key);
			if (!session.isOpen()) {
				getSocketSessions().remove(key);
			}
		}
	}

	/**
	 * Broadcasts a notification message to all the clients that are currently
	 * subscribed to this SQL job
	 * 
	 * @param message
	 */
	private void sendChangeNotification(String notification) {
		// clear the response map
		getResponseMap().clear();
		// send back a notify status
		getResponseMap().put(Statics.WS_STATUS, Statics.WS_NOTIFY);
		// with notification message
		getResponseMap().put(WS_MSG, notification);
		LOG.trace(getThreadName() + ":sending this change notification - "
				+ notification);
		sendResponseMessage(getResponse());
	}

	/**
	 * Broadcasts an error message to all the clients that are currently
	 * subscribed to this SQL job
	 * 
	 * @param message
	 */
	private void sendInternalServerError(String message) {
		// clear the response map
		getResponseMap().clear();
		// send back an error status
		getResponseMap().put(Statics.WS_STATUS, Statics.WS_ERROR);
		// with message
		getResponseMap().put(WS_MSG, message);
		LOG.trace(getThreadName() + ":sending this internal server error - "
				+ message);
		sendResponseMessage(getResponse());
	}

	/**
	 * Broadcasts a message to all the clients that are currently subscribed to
	 * this SQL job
	 * 
	 * @param message
	 */
	private void sendResponseMessage(List<Map<String, Object>> response) {
		TextMessage textMessage = null;
		try {
			// create a TextMessage to send back based on json response
			// object
			textMessage = new TextMessage(Utils.generateJson(response));
		} catch (Exception e) {
			LOG.error(getThreadName() + ":ERROR, caught this "
					+ "Exception while trying to gen json message -  "
					+ e.toString());
			LOG.error(getThreadName() + ": exception stack trace follows:");
			dumpStackTrace(e.getStackTrace());
			if (e.getCause() != null) {
				LOG.error(getThreadName() + ": Caused by "
						+ e.getCause().toString());
				LOG.error(getThreadName()
						+ ": causing exception stack trace follows:");
				dumpStackTrace(e.getCause().getStackTrace());
			}
			return;
		}

		// broadcast the message
		for (String key : getSocketSessions().keySet()) {
			WdsSocketSession session = getSocketSessions().get(key);
			if (session.isOpen()) {
				try {
					session.getSession().sendMessage(textMessage);
				} catch (Exception exc) {
					LOG.error(getThreadName() + ":ERROR, caught this "
							+ "Exception while trying to broadcast message - "
							+ exc.toString());
					LOG.error(getThreadName()
							+ ": exception stack trace follows:");
					dumpStackTrace(exc.getStackTrace());
					if (exc.getCause() != null) {
						LOG.error(getThreadName() + ": Caused by "
								+ exc.getCause().toString());
						LOG.error(getThreadName()
								+ ": causing exception stack trace follows:");
						dumpStackTrace(exc.getCause().getStackTrace());
					}
					return;
				}
			}
		}
	}

	/**
	 * Returns the digital signature of the given string object
	 * 
	 * @param s
	 * @return
	 * @throws Exception
	 */
	private String getHashOf(String s) throws Exception {
		if (s == null || s.isEmpty()) {
			return null;
		}
		getSha().update(s.getBytes());
		return Utils.byteArrayToHexString(getSha().digest());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean tryLock() {
		return sessionsLock.tryLock();
	}

	public void unLock() {
		sessionsLock.unlock();
	}

	public List<Map<String, String>> getlParams() {
		return lParams;
	}

	public void setlParams(List<Map<String, String>> lParams) {
		this.lParams = lParams;
	}

	public MessageDigest getSha() {
		return sha;
	}

	public void setSha(MessageDigest sha) {
		this.sha = sha;
	}

	public String getDigitalSignature() {
		return digitalSignature;
	}

	public void setDigitalSignature(String digitalSignature) {
		this.digitalSignature = digitalSignature;
	}

	public List<Map<String, Object>> getResponse() {
		return response;
	}

	public void setResponse(List<Map<String, Object>> response) {
		this.response = response;
	}

	public Map<String, Object> getResponseMap() {
		return responseMap;
	}

	public void setResponseMap(Map<String, Object> responseMap) {
		this.responseMap = responseMap;
	}

	public String getClusterHash() {
		return clusterHash;
	}

	public void setClusterHash(String clusterHash) {
		this.clusterHash = clusterHash;
	}

	public HazelcastInstance getHazelcastInstance() {
		return hazelcastInstance;
	}

	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	public ILock getMasterLock() {
		return masterLock;
	}

	public void setMasterLock(ILock masterLock) {
		this.masterLock = masterLock;
	}

	public ITopic<String> getTopic() {
		return topic;
	}

	public void setTopic(ITopic<String> topic) {
		this.topic = topic;
	}

	public String getTopicRegistrationId() {
		return topicRegistrationId;
	}

	public void setTopicRegistrationId(String topicRegistrationId) {
		this.topicRegistrationId = topicRegistrationId;
	}

	public Message<String> getTopicMessage() {
		return topicMessage;
	}

	public void setTopicMessage(Message<String> topicMessage) {
		this.topicMessage = topicMessage;
	}

	public boolean isChangeDetected() {
		return changeDetected;
	}

	public void setChangeDetected(boolean changeDetected) {
		this.changeDetected = changeDetected;
	}

	public Thread getRunner() {
		return this.runner;
	}

	public void setRunner(Thread runner) {
		this.runner = runner;
	}

	public boolean inCluster() {
		return getHazelcastInstance() != null;
	}

}
