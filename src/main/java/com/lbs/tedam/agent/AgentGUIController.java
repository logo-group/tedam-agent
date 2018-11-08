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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;

import com.lbs.tedam.agent.websocket.JobRunnerSocketClientListener;
import com.lbs.tedam.exception.CreateNewFileException;
import com.lbs.tedam.util.Constants;
import com.lbs.tedam.util.Enums.FileName;
import com.lbs.tedam.util.HasLogger;
import com.lbs.tedam.util.PropUtils;
import com.lbs.tedam.util.TedamFileUtils;
import com.lbs.tedam.websocket.WebSocketException;
import com.lbs.tedam.websocket.client.WebSocketClient;

/**
 * @author Canberk.Erkmen <br>
 * 
 */
public class AgentGUIController implements HasLogger {

	private static String clientName = setClientName();
	/** AgentGUI agentGUI */
	private static AgentGUI agentGUI;
	/** String clientName */
	/** WebSocketClient jobRunnerSocketClient */
	private WebSocketClient jobRunnerSocketClient = null;

	/**
	 * this method main procedure used to start the agent. <br>
	 * 
	 * @author Canberk.Erkmen <br>
	 * @author Tarik.Mikyas <br>
	 * @param args
	 *            <br>
	 */
	public static void main(String[] args) {
		AgentGUIController agentGUIController = new AgentGUIController();
		agentGUIController.shutDownHook();
		agentGUIController.checkLogFilePath();

		if (args.length > 0) {
			PropUtils.loadPropFile(args[0] + Constants.FILE_SEPARATOR + FileName.CONFIG_PROPERTIES.getName());
			System.out.println(Constants.CONFIG_FILE_PATH + args[0]);
		}

		agentGUIController.setVisibleGUI();
		agentGUIController.createFilePath();
		agentGUIController.initJobRunnerSocketClient(agentGUIController);
	}

	private void checkLogFilePath() {
		try {
			String logFilePath = System.getProperty(Constants.LOG_FILE_PATH);
			if (logFilePath == null) {
				throw new InvalidParameterException("The agent will not be opened because logFilePath is not defined! ");
			} else {
				getLogger().info(Constants.LOG_FILE_PATH + " : " + logFilePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * this method createFilePaths <br>
	 * @author Canberk.Erkmen <br>
	 */
	private void createFilePath() {
		try {
			TedamFileUtils.createNewFilePath(PropUtils.getProperty(Constants.TEMP_FILE_PATH));
		} catch (CreateNewFileException e) {
			getLogger().error("" + e);
		}
	}

	/**
	 * this method initJobRunnerSocketClient <br>
	 * @author Canberk.Erkmen
	 * @param agentGUIController
	 *            <br>
	 */
	public void initJobRunnerSocketClient(AgentGUIController agentGUIController) {
		String jobRunnerServerURI = PropUtils.getProperty("jobRunnerWebSocketServer");
		getLogger().info("clientName : " + getClientName());
		agentGUI.appendTextAreaContent("clientName : " + getClientName());
		jobRunnerSocketClient = new WebSocketClient(jobRunnerServerURI);
		jobRunnerSocketClient.addWebSocketClientListener(new JobRunnerSocketClientListener(agentGUIController, jobRunnerSocketClient));
		connectJobRunnerSocketServer(jobRunnerServerURI);
	}

	private void connectJobRunnerSocketServer(String jobRunnerServerURI) {
		getLogger().info("Trying to connect to web socket server : " + jobRunnerServerURI);
		agentGUI.appendTextAreaContent("Trying to connect to web socket server : " + jobRunnerServerURI);
		jobRunnerSocketClient.reconnect();
		agentGUI.appendTextAreaContent("Connected to web socket server : " + jobRunnerServerURI);
		getLogger().info("Connected to web socket server : " + jobRunnerServerURI);
	}

	/**
	 * this method setVisibleGUI <br>
	 * @author Canberk.Erkmen <br>
	 */
	public void setVisibleGUI() {
		agentGUI = new AgentGUI();
		agentGUI.setVisible(true);
	}

	/**
	 * this method getAgentGUI <br>
	 * @author Canberk.Erkmen
	 * @return <br>
	 */
	public static AgentGUI getAgentGUI() {
		return agentGUI;
	}

	/**
	 * this method getClientName <br>
	 * @author Canberk.Erkmen
	 * @return <br>
	 */
	public String getClientName() {
		return clientName;
	}

	public String getProjectName() {
		return PropUtils.getProperty(Constants.CLIENT_PROJECT_NAME);
	}

	/**
	 * this method setClientName <br>
	 * @author Canberk.Erkmen
	 * @param clientName
	 *            <br>
	 * @return
	 */
	public static String setClientName() {
		String clientName = Constants.EMPTY_STRING;
		try {
			clientName = InetAddress.getLocalHost().getHostName();
			System.setProperty("clientName", clientName);
		} catch (UnknownHostException e) {
			System.out.println("" + e);
		}
		return clientName;
	}

	public void shutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					jobRunnerSocketClient.disconnect();
				} catch (WebSocketException e) {
					getLogger().error(e.getMessage());
				}
			}
		});
	}

}
