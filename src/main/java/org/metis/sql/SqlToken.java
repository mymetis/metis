
package org.metis.sql;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Types;
import java.net.URL;
import java.net.MalformedURLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a string token of a SQL statement. There are two types: regular
 * and key. A regular token is a SQL word, while a key-token represents a
 * parameterized field of the SQL statement. A parameterized field is comprised
 * of three parts (sql-type, key, mode) and is delimited by '`'. The type and
 * key-name are required, while mode is optional and reserved for call'able
 * statements. For example, `string:first` represents a field of type string
 * whose key name is 'first'. For example: <code>
 * select first from users where first like `string:first` || '%'
 * </code>
 * 
 */
public class SqlToken implements Comparable<SqlToken> {

	public static final Log LOG = LogFactory.getLog(SqlToken.class);

	public static final int ORACLE_CURSOR = -10;

	/**
	 * Enumeration used for identifying the parameter mode for this token; used
	 * for call'able statements
	 */
	public enum Mode {
		IN, OUT, INOUT, NOMODE;
	}

	/**
	 * Enumeration used for the valid or accepted SQL types
	 */
	public enum JdbcType {
		// @formatter:off
		CHAR, 
		VARCHAR, 
		LONGVARCHAR,	
		NUMERIC, 
		DECIMAL,		
		BIT,
		BOOLEAN,
		TINYINT,
		SMALLINT,
		INTEGER,
		BIGINT,
		REAL,
		FLOAT,
		DOUBLE,
		DATE,
		TIME,
		TIMESTAMP,
		DATALINK,
		NCHAR,
		NVARCHAR,
		LONGNVARCHAR,
		CURSOR,
		// RSET is our own type used for defining 
		// ResultSet OUT params
		RSET,
		// PKEY is our type used for defining
		// a primary key for an insert
		PKEY;
		// @formatter:on

		// return the numeric value of this JDBC type
		public int getType() {
			switch (this) {
			case CHAR:
				return Types.CHAR;
			case NCHAR:
				return Types.NCHAR;
			case VARCHAR:
				return Types.VARCHAR;
			case NVARCHAR:
				return Types.NVARCHAR;
			case LONGVARCHAR:
				return Types.LONGVARCHAR;
			case LONGNVARCHAR:
				return Types.LONGNVARCHAR;
			case NUMERIC:
				return Types.NUMERIC;
			case DECIMAL:
				return Types.DECIMAL;
			case BIT:
				return Types.BIT;
			case BOOLEAN:
				return Types.BOOLEAN;
			case TINYINT:
				return Types.TINYINT;
			case SMALLINT:
				return Types.SMALLINT;
			case INTEGER:
				return Types.INTEGER;
			case BIGINT:
				return Types.BIGINT;
			case REAL:
				return Types.REAL;
			case FLOAT:
				return Types.FLOAT;
			case DOUBLE:
				return Types.DOUBLE;
			case DATE:
				return Types.DATE;
			case TIMESTAMP:
				return Types.TIMESTAMP;
			case TIME:
				return Types.TIME;
			case DATALINK:
				return Types.DATALINK;
			case CURSOR:
				return ORACLE_CURSOR;
			default:
				return 0;
			}
		}
	}

	// the key is used only for key fields such as `<sql type>:<key>:<mode>`
	// the key is left null for non key fields like SQL keywords
	private String key;
	private String value;
	private JdbcType jdbcType;
	private Mode mode = Mode.NOMODE;
	// the token's first, and most probably only, position in the sql statment
	private int position = -1;
	// additional positions that this token is found in
	private List<Integer> positions = new ArrayList<Integer>();

	/**
	 * Create a parameterized token
	 * 
	 * @param sqlType
	 * @param key
	 * @param mode
	 * @param position
	 * @throws IllegalArgumentException
	 */
	public SqlToken(String sqlType, String key, String mode, int position)
			throws IllegalArgumentException {
		try {
			jdbcType = Enum.valueOf(JdbcType.class, sqlType.toUpperCase());
		} catch (IllegalArgumentException e) {
			LOG.error("This is an invalid jdbcType type ["
					+ sqlType.toUpperCase() + "]");
			throw e;
		}
		// the key is used to id this token as a parameter field; i.e., a
		// key-value field, where the value is the value of the input param that
		// is bound to the statement. The value in key-value is not be confused
		// with the value of this token, which is the token's name.
		// TODO: clear this up - too confusing!!!
		this.key = key.toLowerCase();
		this.value = this.key;
		this.position = position;
		// mode is optional
		if (mode != null) {
			try {
				this.mode = Enum.valueOf(Mode.class, mode.toUpperCase());
			} catch (IllegalArgumentException e) {
				LOG.error("This mode type is not valid/allowed ["
						+ mode.toUpperCase() + "]");
				throw e;
			}
		}
	}

	/**
	 * Create a non-parameterized token.
	 * 
	 * @param value
	 */
	public SqlToken(String value) {
		this.value = value;
	}

	/**
	 * Returns the position of this key-token in the SQL statement
	 * 
	 * @return
	 */
	public int getPosition() {
		return this.position;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isKey() {
		return key != null;
	}

	public Mode getMode() {
		return this.mode;
	}

	public JdbcType getJdbcType() {
		return jdbcType;
	}

	public boolean isPkey() {
		return getJdbcType() == JdbcType.PKEY;
	}

	public boolean isRset() {
		return getJdbcType() == JdbcType.RSET;
	}

	public boolean isCursor() {
		return getJdbcType() == JdbcType.CURSOR;
	}

	public boolean isIn() {
		return getMode() == Mode.IN;
	}

	public boolean isInOut() {
		return getMode() == Mode.INOUT;
	}

	public boolean isOut() {
		return getMode() == Mode.OUT;
	}

	public List<Integer> getPositions() {
		return positions;
	}

	/**
	 * Returns a Java object for this field token. Its value is taken from the
	 * given String.
	 * 
	 * @param value
	 * @return
	 */
	public Object getObjectValue(String value) throws NumberFormatException,
			IllegalArgumentException, MalformedURLException {

		if (!isKey()) {
			return null;
		}

		switch (getJdbcType()) {
		case NUMERIC:
		case DECIMAL:
			return new BigDecimal(value);
		case BIT:
		case BOOLEAN:
			if (value.trim().equalsIgnoreCase("true")
					|| value.trim().equalsIgnoreCase("false")) {
				return Boolean.valueOf(value);
			} else {
				LOG.error("this value is set to neither 'true' nor 'false' :"
						+ value);
				throw new NumberFormatException(
						"this value is set to neither 'true' nor 'false' :"
								+ value);
			}
		case TINYINT:
		case SMALLINT:
		case INTEGER:
			return Integer.valueOf(value);
		case BIGINT:
			return Long.valueOf(value);
		case REAL:
			return Float.valueOf(value);
		case FLOAT:
		case DOUBLE:
			return Double.valueOf(value);
		case DATE:
			return Date.valueOf(value);
		case TIME:
			return Time.valueOf(value);
		case TIMESTAMP:
			return Timestamp.valueOf(value);
		case DATALINK:
			return new URL(value);
		default:
			// if it is none of the above, then it is a String type
			return value;
		}
	}

	/**
	 * Used for sorting, in ascending order, a list of key tokens.
	 */
	public int compareTo(SqlToken token) {
		if (position == token.position) {
			return 0;
		} else if (position < token.position) {
			return -1;
		}
		return 1;
	}

	public String toString() {
		return "jdbcType=" + jdbcType + " key=" + key + " value=" + value
				+ " mode=" + mode + " pos=" + position;
	}

	public boolean isEqual(SqlToken token) {
		return (getKey().equals(token.getKey())
				&& getJdbcType() == token.getJdbcType() && getMode() == token
					.getMode()) ? true : false;
	}

}
