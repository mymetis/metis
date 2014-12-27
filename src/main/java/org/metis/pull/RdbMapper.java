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
package org.metis.pull;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * This is just a facade for the Spring handler mapper
 * @author jfernandez
 *
 */
public class RdbMapper extends SimpleUrlHandlerMapping {

    private static final Log LOG = LogFactory.getLog(RdbMapper.class);

}
