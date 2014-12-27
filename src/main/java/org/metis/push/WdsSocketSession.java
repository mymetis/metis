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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.WebSocketSession;
import org.metis.sql.SqlJob;

/**
 * Decorator for a SocketSession
 * 
 * @author jfernandez
 *
 */
public class WdsSocketSession {

	private static final Log LOG = LogFactory.getLog(WdsSocketSession.class);

	private long hash = -1L;

	private WebSocketSession session;
	
	private SqlJob myJob = null;

	public WdsSocketSession(WebSocketSession session) {
		setSession(session);
	}

	public String getId() {
		return getSession().getId();
	}

	public boolean isOpen() {
		return getSession().isOpen();
	}

	public WebSocketSession getSession() {
		return session;
	}

	public void setSession(WebSocketSession session) {
		this.session = session;
	}

	public long getHash() {
		return hash;
	}

	public void setHash(long hash) {
		this.hash = hash;
	}

	public SqlJob getMyJob() {
		return myJob;
	}

	public void setMyJob(SqlJob myJob) {
		this.myJob = myJob;
	}

}
