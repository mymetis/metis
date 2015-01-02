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
package org.metis;

import java.util.Map;
import java.util.Iterator;

import javax.servlet.ServletException;

import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.context.ApplicationContext;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.metis.pull.WdsResourceBean;
import org.metis.push.PusherBean;

/**
 * This is the main dispatcher Servlet for Metis. It will dispatch http requests
 * on to the mapper, which maps the requests to resource or pusher beans.
 * 
 */
public class MetisServlet extends DispatcherServlet {

	private static final long serialVersionUID = -2808801234972854107L;

	private static final Log LOG = LogFactory.getLog(MetisServlet.class);

	private Map<String, WdsResourceBean> rdbs = null;
	private Map<String, PusherBean> pdbs = null;

	public MetisServlet() {
		super();
	}

	/**
	 * This method will be invoked after all bean properties have been set and
	 * the WebApplicationContext has been loaded.
	 * 
	 * @throws ServletException
	 *             in case of an initialization exception
	 */
	@Override
	protected void initFrameworkServlet() throws ServletException {
		super.initFrameworkServlet();

		if ((rdbs == null || rdbs.isEmpty())
				&& (pdbs == null || pdbs.isEmpty())) {
			throw new ServletException(getServletConfig().getServletName()
					+ ": There are neither resource nor pusher"
					+ "beans defined for this web application context");
		}

		for (WdsResourceBean rdb : rdbs.values()) {
			if (!rdb.isDbConnectionAcquired()) {
				LOG.error(getServletConfig().getServletName()
						+ ": this RDB was not able to get a "
						+ "connection to its data source: " + rdb.getBeanName());
				throw new ServletException(getServletConfig().getServletName()
						+ ": this RDB was not able to get a "
						+ "connection to its data source: " + rdb.getBeanName());
			}
		}
		for (PusherBean pdb : pdbs.values()) {
			if (!pdb.isDbConnectionAcquired()) {
				LOG.error(getServletConfig().getServletName()
						+ ": this PUSHER was not able to get a "
						+ "connection to its data source: " + pdb.getBeanName());
				throw new ServletException(getServletConfig().getServletName()
						+ ": this PUSHER was not able to get a "
						+ "connection to its data source: " + pdb.getBeanName());
			}
			// assign this servelt's name to the PDB
			pdb.setServletName(getServletName());
		}

	}

	/**
	 * This is where we initialize the web application's strategy objects (rdbs,
	 * pools, etc.) This method is called prior to initFrameworkServlet() being
	 * called.
	 */
	@Override
	protected void initStrategies(ApplicationContext context) {
		super.initStrategies(context);

		// Assign the resource bean this container's info
		WdsResourceBean.serverInfo = getServletContext().getServerInfo();

		// get list of all RDBs and PUSHERs for this application context
		rdbs = context.getBeansOfType(WdsResourceBean.class);
		pdbs = context.getBeansOfType(PusherBean.class);
		if (rdbs.isEmpty() && pdbs.isEmpty()) {
			LOG.error(getServletConfig().getServletName()
					+ ": Neither RDBs nor PUSHERs have been defined for this web application");
			return;
		}

		Boolean secure = null;
		String prop = getInitParameter("secure");
		if (prop != null) {
			secure = Boolean.valueOf(prop);
			LOG.debug(getServletConfig().getServletName() + ": secure = "
					+ secure.booleanValue());
		}

		Boolean authenticated = null;
		prop = getInitParameter("authenticated");
		if (prop != null) {
			authenticated = Boolean.valueOf(prop);
			LOG.debug(getServletConfig().getServletName()
					+ ": authenticated = " + authenticated.booleanValue());
		}

		String agentNames = getInitParameter("agentNames");
		if (agentNames != null) {
			LOG.debug(getServletConfig().getServletName() + ": agentNames = "
					+ agentNames);
		}

		String cacheControl = getInitParameter("cacheControl");
		if (cacheControl != null) {
			LOG.debug(getServletConfig().getServletName() + ": cacheControl = "
					+ cacheControl);
		}

		try {
			// assign global init properties to the RDBs
			for (Iterator<WdsResourceBean> it = rdbs.values().iterator(); it
					.hasNext();) {
				WdsResourceBean rdb = it.next();

				if (rdb.getAuthenticated() == null && authenticated != null) {
					rdb.setAuthenticated(authenticated.booleanValue());
				}

				if (rdb.getSecure() == null && secure != null) {
					rdb.setSecure(secure.booleanValue());
				}

				if (rdb.getAgentNames() == null && agentNames != null) {
					rdb.setAgentNames(agentNames);
				}

				if (rdb.getCacheControl() == null && cacheControl != null) {
					rdb.setCacheControl(cacheControl);
				}
			}

		} catch (Exception e) {
			LOG.error(getServletConfig().getServletName()
					+ "initStrategies: ERROR, this exception was caught - "
					+ e.getLocalizedMessage());
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		LOG.trace("destroy - enter");
	}
}
