/*
* Copyright 2014-2019 Logo Business Solutions
* (a.k.a. LOGO YAZILIM SAN. VE TIC. A.S)
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/

package com.lbs.tedam.agent;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbs.tedam.agent.AgentGUIController;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AgentGUIControllerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentGUIControllerTest.class);

	/** AgentGUIControllerGUIController AgentGUIControllerGUIController */
	@Parameter
	private static AgentGUIController agentGUIController;

	/**
	 * this method test00GetAgentGUIControllerServiceImpl <br>
	 *
	 * @author Canberk.Erkmen <br>
	 */
	@BeforeClass
	public static void test00GetAgentGUIController() {
		agentGUIController = new AgentGUIController();
	}

	/**
	 * @author Tarik.Mikyas
	 * @param sourcePath
	 * @return <br>
	 *         this method getFilePath
	 */
	private File getFilePath(String sourcePath) {
		URL resource = getClass().getResource(sourcePath);
		File file = null;
		try {
			file = (Paths.get(resource.toURI()).toFile()).getAbsoluteFile();
		} catch (URISyntaxException e) {
			LOGGER.error("" + e);
		}
		return file;
	}

	/**
	 * this method test05Calculate <br>
	 * @author Canberk.Erkmen
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 *             <br>
	 */
	@Test
	public void test03Calculate() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method method = agentGUIController.getClass().getDeclaredMethod("calculate", File.class);
		method.setAccessible(true);
		Object object = method.invoke(agentGUIController, getFilePath("/SampleTestTEDAMCaution.xls"));
		assertNotNull(object);
	}

}
