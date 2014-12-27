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

import org.metis.sql.SqlStmnt;
import org.metis.sql.SqlToken;
import java.sql.Types;
import static org.metis.sql.SqlToken.ORACLE_CURSOR;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.object.StoredProcedure;

/**
 * Class used for executing functions and stored procedures.
 * 
 * A quote regarding Oracle's REF CURSOR type: "In general, I believe Oracle's
 * implementation here breaks JDBC, and is not used elsewhere (MySQL, MSSQL,
 * etc). You should be returning your results as a select statement and
 * iterating over the JDBC ResultSet, as is standard (and intended) practice
 * when using JDBC."
 * 
 * @author jfernandez
 * 
 */
public class WdsStoredProcedure extends StoredProcedure {

	public static final Log LOG = LogFactory.getLog(WdsStoredProcedure.class);

	private SqlStmnt myStmt;

	public WdsStoredProcedure(SqlStmnt stmt) throws Exception {

		super(stmt.getJdbcTemplate(), stmt.getStoredProcName());

		setFunction(stmt.isFunction());
		myStmt = stmt;

		// Parameters should be declared in the same order that
		// they are declared in the stored procedure. The one exception
		// are result sets, which must be defined first!!
		//
		// Here's something I found as to why - When you make any private
		// static class of StoreProcedure, then in its constructor you must
		// declare SqlReturnResultSet before you declare SqlParameter.
		// Otherwise you will not be able to find return data from
		// StoredProcedure execution. Still not sure what this means
		// and why its so.
		//
		for (SqlToken sqlToken : myStmt.getSortedKeyTokens()) {
			if (sqlToken.isRset()) {
				declareParameter(new SqlReturnResultSet(sqlToken.getKey(),
						myStmt));
			}
		}

		// now do the other parameters
		// iterate through tokens in proper sequence; parameters must be
		// declared according to the sequence in which they appear in the
		// statement
		for (SqlToken sqlToken : myStmt.getSortedKeyTokens()) {

			// skip result sets
			if (sqlToken.isRset()) {
				continue;
			}

			switch (sqlToken.getMode()) {
			case IN:
				declareParameter(new SqlParameter(sqlToken.getKey(), sqlToken
						.getJdbcType().getType()));
				break;
			case OUT:
				// look for CURSOR types
				if (sqlToken.isCursor()) {
					// if it is a cursor then check to see if it is Oracle or
					// some other DBMS and set the type accrodingly
					int type = (myStmt.getMetisController().isOracle()) ? ORACLE_CURSOR
							: Types.OTHER;
					declareParameter(new SqlOutParameter(sqlToken.getKey(),
							type, myStmt));
				} else {
					declareParameter(new SqlOutParameter(sqlToken.getKey(),
							sqlToken.getJdbcType().getType()));
				}
				break;
			case INOUT:
				// note: you can't have cursors as IN params - doesn't
				// make sense, so don't check for them when its an INOUT
				declareParameter(new SqlInOutParameter(sqlToken.getKey(),
						sqlToken.getJdbcType().getType()));
				break;
			default:
				throw new Exception(
						"WdsStoredProcedure: this invalid mode was provided: "
								+ sqlToken.getMode());
			}
		}

		// specify whether this is a function
		super.setFunction(myStmt.isFunction());

		// compile the statement
		compile();
	}

}
