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

package com.lbs.tedam.agent.websocket;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCode;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;

import com.lbs.tedam.agent.AgentGUIController;
import com.lbs.tedam.model.ClientMessage;
import com.lbs.tedam.model.JobRunnerCommand;
import com.lbs.tedam.model.TedamSocketMessage;
import com.lbs.tedam.service.AgentGUIControllerService;
import com.lbs.tedam.service.impl.AgentGUIControllerServiceImpl;
import com.lbs.tedam.util.EnumsV2.ClientStatus;
import com.lbs.tedam.util.EnumsV2.TedamSocketMessageType;
import com.lbs.tedam.util.HasLogger;
import com.lbs.tedam.util.TedamJsonFactory;
import com.lbs.tedam.websocket.client.WebSocketClient;
import com.lbs.tedam.websocket.client.WebSocketClientListener;

public class JobRunnerSocketClientListener implements WebSocketClientListener, HasLogger {

	/** long serialVersionUID */
	private static final long serialVersionUID = 1L;

	private AgentGUIController controller = null;
	private WebSocketClient websocketClient = null;
	private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	private final AgentGUIControllerService agentGUIControllerServiceImpl = new AgentGUIControllerServiceImpl();

	public JobRunnerSocketClientListener(AgentGUIController controller, WebSocketClient websocketClient) {
		this.controller = controller;
		this.websocketClient = websocketClient;
	}

	@Override
	public void onOpen(Session session) {
		TedamSocketMessage tedamSocketMessage = buildClientMessage();
		sendMessage(TedamJsonFactory.toJson(tedamSocketMessage));
	}

	private TedamSocketMessage buildClientMessage() {
		ClientStatus clientStatus = ClientStatus.FREE;
		if (executor.getActiveCount() > 0) {
			clientStatus = ClientStatus.BUSY;
		}
		ClientMessage clientMessage = new ClientMessage(controller.getClientName(), controller.getProjectName(),
				clientStatus);
		TedamSocketMessage tedamSocketMessage = new TedamSocketMessage(TedamJsonFactory.toJson(clientMessage), TedamSocketMessageType.CLIENT);
		return tedamSocketMessage;
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		getLogger().warn("Close reason: " + closeReason.getCloseCode().getCode());
		reconnect(closeReason);
	}

	private void reconnect(CloseReason closeReason) {
		CloseCode closeCode = closeReason.getCloseCode();
		if (closeCode == CloseCodes.CLOSED_ABNORMALLY || closeCode == CloseCodes.GOING_AWAY || closeCode == CloseCodes.PROTOCOL_ERROR) {
			getLogger().warn("Web socket server closed abnormally! Trying to reconnect...");
			AgentGUIController.getAgentGUI().appendTextAreaContent("Web socket server closed abnormally! Trying to reconnect...");
			websocketClient.reconnect();
			getLogger().warn("Connected to web socket server.");
			AgentGUIController.getAgentGUI().appendTextAreaContent("Connected to web socket server.");
		}
	}

	@Override
	public void onMessage(String message) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				JobRunnerCommand jobRunnerCommand = TedamJsonFactory.fromJson(message, JobRunnerCommand.class);
				getLogger().warn(message);
				executeCommands(jobRunnerCommand);
			}
		});
	}

	public void sendMessage(String message) {
		try {
			if (!websocketClient.isSessionValid()) {
				getLogger().warn("Reconnecting while sending message!");
				websocketClient.reconnect();
			}
			getLogger().info("Outgoing message : " + message);
			websocketClient.getSession().getBasicRemote().sendText(message);
			getLogger().info("After send message");
		} catch (IOException e) {
			getLogger().error("" + e);
		}
	}

	private void executeCommands(JobRunnerCommand jobRunnerCommand) {
		getLogger().info("Incoming message clientId : " + jobRunnerCommand.getClient().getId() + " testSetId : " + jobRunnerCommand.getTestSetId());
		agentGUIControllerServiceImpl.startJobCommandListOperations(jobRunnerCommand, this);
	}

	@Override
	public void onError(Throwable error) {
		getLogger().error("" + error);
	}

}