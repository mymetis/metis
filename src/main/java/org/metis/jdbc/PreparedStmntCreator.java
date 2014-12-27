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
package org.metis.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.metis.sql.SqlStmnt;

/**
 * This class is used, by the SqlStmnt object, as a callback for the Spring JDBC
 * template. It is used for INSERT, UPDATE and DELETE SQL statements. Note that
 * this class implements PreparedStatementSetter, as well as
 * PreparedStatementCreator
 * 
 */
public class PreparedStmntCreator implements PreparedStatementCreator,
		PreparedStatementSetter {

	public static final Log LOG = LogFactory.getLog(PreparedStmntCreator.class);

	private SqlStmnt stmnt;
	private Object[] bindObs;

	public PreparedStmntCreator() {
	}

	public PreparedStmntCreator(SqlStmnt stmnt, Object[] bindObs) {
		this.stmnt = stmnt;
		this.bindObs = bindObs;
	}

	public SqlStmnt getStmnt() {
		return stmnt;
	}

	public void setStmnt(SqlStmnt stmnt) {
		this.stmnt = stmnt;
	}

	public Object[] getBindObs() {
		return bindObs;
	}

	public void setBindObs(Object[] bindObs) {
		this.bindObs = bindObs;
	}

	/**
	 * This is the method that is called by the template.
	 * 
	 */
	public PreparedStatement createPreparedStatement(Connection con)
			throws SQLException {

		PreparedStatement ps = null;
		// @formatter:off
		String sqlString = (stmnt.isPrepared()) 
				? stmnt.getPrepared() 
				: stmnt.getOriginal();		
		// @formatter:on

		// if the statement is not an INSERT, then don't bother with a
		// key holder
		if (stmnt.getSqlStmntType() == SqlStmnt.SqlStmntType.INSERT) {
			// Oracle does not return auto-generated key, you must tell it which
			// key to return. If a key is not given, then just ask for the value
			// of the row's first column
			if (stmnt.getPrimaryKey() != null) {
				ps = con.prepareStatement(sqlString,
						new String[] { stmnt.getPrimaryKey() });
			} else {
				ps = con.prepareStatement(sqlString,
						PreparedStatement.RETURN_GENERATED_KEYS);
			}
		} else {
			// this was not an insert, so there is no generated key
			// associated with this call
			ps = con.prepareStatement(sqlString);
		}

		// bind objects if any
		bindObjects(ps);
		return ps;
	}

	public void setValues(PreparedStatement ps) throws SQLException {
		bindObjects(ps);
	}

	private void bindObjects(PreparedStatement ps) throws SQLException {
		// bind the corresponding objects (if any)
		if (bindObs != null) {
			LOG.debug("setValues: binding this many objects " + bindObs.length);
			for (int i = 0; i < bindObs.length; i++) {
				try {
					ps.setObject(i + 1, bindObs[i]);
				} catch (Exception e) {
					String eStr = "setValues: caught this exception ["
							+ e.getLocalizedMessage()
							+ "], when calling setObject for ["
							+ bindObs[i].toString() + "]";
					LOG.error(eStr);
					throw new SQLException(eStr);
				}
			}
		}
		return;
	}

}
