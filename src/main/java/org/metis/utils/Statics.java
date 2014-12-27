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
package org.metis.utils;

public class Statics {
	public static final String QUESTION_STR = "?";
	public static final String AMPERSAND_STR = "&";
	public static final String COMMA_STR = ",";
	public static final char   COMMA_CHR = ',';
	public static final String BANG_STR = "!";
	public static final String EQUALS_STR = "=";
	public static final String COLON_STR = ":";
	public static final char FORWARD_SLASH_CHR = '/';
	public static final String FORWARD_SLASH_STR = "/";
	public static final String ESC_DOT_STR = "\\.";
	public static final String DELIM = "\\s+";
	public static final char SPACE_CHR = ' ';
	public static final String SPACE_CHR_STR = " ";
	public static final String S_CHR_STR = "s";
	public static final char TAB_CHR = '\t';
	public static final char NEWLINE_CHR = '\n';
	public static final char CARRIAGE_RETURN_CHR = '\r';
	public static final char BACK_QUOTE_CHR = '`'; // also called a 'grave
													// accent'
	public static final String BACK_QUOTE_STR = "`";
	public static final char SINGLE_QUOTE_CHR = '\'';
	
	public static final String LEFT_BRACE_STR = "{";
	public static final String RIGHT_BRACE_STR = "}";
	public static final char LEFT_BRACE_CHR = '{';
	public static final char RIGHT_BRACE_CHR = '}';
	
	public static final String LEFT_BRACKET_STR = "[";
	public static final String RIGHT_BRACKET_STR = "]";
	public static final char LEFT_BRACKET_CHR = '[';
	public static final char RIGHT_BRACKET_CHR = ']';
	
	
	public static final String NO_NAME_PARAM = "_none";
	public static final String EMPTY_STR = "";
	public static final String ESCAPED_LEFT_PAREN = "\\(";
	public static final String RIGHT_PAREN_STR = ")";
	public static final String UPDATE_STR = "update";
	public static final String DELETE_STR = "delete";
	public static final String INSERT_STR = "insert";
	public static final String SELECT_STR = "select";
	public static final String CALL_STR = "call";
	public static final String RSET_STR = "rset";
	public static final String CURSOR_STR = "cursor";
	public static final String INOUT_STR = "inout";
	public static final String OUT_STR = "out";
	public static final String FUNC_EQUAL_CALL_STR = EQUALS_STR + CALL_STR;
	public static final String USER_AGENT_HDR = "User-Agent";
	public static final String ACCEPT_HDR = "Accept";
	public static final String LOCATION_HDR = "Location";
	public static final String DATE_HDR = "Date";
	public static final String EXPIRES_HDR = "Expires";
	public static final String SERVER_HDR = "Server";
	public static final String PRAGMA_HDR = "Pragma";
	public static final String CACHE_CNTRL_HDR = "Cache-control";
	public static final String DFLT_CACHE_CNTRL_STR = "no-store, no-transform";
	public static final String PRAGMA_NO_CACHE_STR = "no-cache";
	public static final String productNameLabel= "productname";
	public static final String productName= "Metis";
	public static final String productVersionLabel = "productversion";
	public static final String productVersion = "1";
	public static final String WS_COMMAND = "ws_command";
	public static final String WS_STATUS = "ws_status";
	//public static final String WS_STATUS_MSG = "ws_status_msg";
	public static final String WS_SUBSCRIBE = "subscribe";
	public static final String WS_SUBSCRIBED = "subscribed";
	public static final String WS_NOTIFY = "notify";
	public static final String WS_PING = "ping";	
	public static final String WS_OK = "ok";	
	public static final String WS_NOT_FOUND = "not_found";	
	public static final String WS_ERROR = "internal_error";
	public static final String WS_DFLT_SIGNATURE = "ws_default_signature";
	public static final String WS_MSG = "ws_message";
	public static final String TIME_INTERVAL = "time_interval";	
	public static final String TIME_INTERVAL_MAX = "time_interval_max";	
	public static final String TIME_INTERVAL_STEP = "time_interval_step";	
	public static final String HZ_GROUP = "group";
	public static final String HZ_PROTO = "protocol"; 
	public static final String HZ_TCP = "tcp";
	public static final String HZ_MCAST = "mcast"; 
	
	
	/**
	 * Design note: The 'private' directive states that no shared intermediary
	 * (proxy or CDN) is allowed to cache the response. This is how we ensure
	 * that the client, and only the client, caches the data.
	 */
	public static final String PRIVATE_STR = "private, no-transform";
	public static final String LAST_MOD_HDR = "Last-Modified";
	public static final String UTF8_STR = "utf-8";
	public static final String ORACLE_STR = "oracle";
	public static final String PKEY_STR = "pkey";
}
