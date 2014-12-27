package org.metis;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.PrintWriter;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * A Dummy DataSource used only for testing purposes.
 * 
 * @author jfernandez
 */
public class DummyDataSource implements DataSource {
	
	public DummyDataSource(){
		
	}

	public Connection getConnection() throws SQLException {
		return null;
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		return null;
	}

	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	public void setLogWriter(PrintWriter out) throws SQLException {

	}

	public void setLoginTimeout(int seconds) throws SQLException {
	}

	public int getLoginTimeout() throws SQLException {
		return 1;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return true;
	}
	
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@SuppressWarnings("unused")
	// Has to match signature in DataSource
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

}
