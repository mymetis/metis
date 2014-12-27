package org.metis.sql;

import java.util.Map;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import java.util.LinkedList;

/**
 * This object is used by the SqlStmnt class to return the results of a SQL
 * statement invocation.
 */
public class SqlResult {

	public static final Log LOG = LogFactory.getLog(SqlResult.class);
	
	// keep a cache of these objects
	private static final int MAX_QUEUE_SIZE = 50;
	private static LinkedList<SqlResult> cache = new LinkedList<SqlResult>();
	
	// fill the cache
	static {
		for(int i = 0; i < MAX_QUEUE_SIZE; i++){
			enqueue(new SqlResult());
		}
	}
	
	// ~~~~~ IF YOU ADD NEW PROPERTIES, MAKE SURE TO INCLUDE THEM ~~~~~~~~~ 
	// ~~~~~ IN THE RESET METHOD                                  ~~~~~~~~~
	// the result set in the form of a list of maps
	private List<Map<String, Object>> resultSet;
	// the primary key returned as a result of an insert
	private GeneratedKeyHolder keyHolder;
	// the number of rows affected by the operation
	private int numRows;
	// the number of rows affected by a batch operation
	private int[] batchNumRows;
	

	public SqlResult() {
	}

	public SqlResult(List<Map<String, Object>> resultSet,
			GeneratedKeyHolder keyHolder, int numRows) {
		this.resultSet = resultSet;
		this.keyHolder = keyHolder;
		this.numRows = numRows;
	}

	public SqlResult(List<Map<String, Object>> resultSet) {
		this(resultSet, null, 0);
	}

	public SqlResult(int numRows) {
		this(null, null, numRows);
	}

	public SqlResult(GeneratedKeyHolder keyHolder) {
		this(null, keyHolder, 0);
	}

	public SqlResult(List<Map<String, Object>> resultSet, int numRows) {
		this(resultSet, null, numRows);
	}
	
	public int[] getBatchNumRows() {
		return batchNumRows;
	}

	public void setBatchNumRows(int[] batchNumRows) {
		this.batchNumRows = batchNumRows;
	}

	public List<Map<String, Object>> getResultSet() {
		return resultSet;
	}

	public void setResultSet(List<Map<String, Object>> resultSet) {
		if(resultSet == null || resultSet.isEmpty()){
			return;
		}
		this.resultSet = resultSet;
	}

	public GeneratedKeyHolder getKeyHolder() {
		return keyHolder;
	}

	public void setKeyHolder(GeneratedKeyHolder keyHolder) {
		this.keyHolder = keyHolder;
	}

	public int getNumRows() {
		return numRows;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	public void reset() {
		setResultSet(null);
		setKeyHolder(null);
		setNumRows(0);
		setBatchNumRows(null);
	}
	
	public void close(){
		enqueue(this);
	}

	public static void enqueue(SqlResult result) {
		synchronized (cache) {
			if (cache.size() >= MAX_QUEUE_SIZE) {
				return;
			}
			result.reset();
			cache.add(result);
		}
	}

	public static SqlResult dequeue() {
		synchronized (cache) {
			if (cache.isEmpty()) {
				return new SqlResult();
			}
			return cache.poll();
		}
	}

	
}
