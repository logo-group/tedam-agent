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

package com.lbs.tedam.service.impl;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import com.lbs.tedam.agent.AgentGUIController;
import com.lbs.tedam.agent.websocket.JobRunnerSocketClientListener;
import com.lbs.tedam.model.JobRunnerCommand;
import com.lbs.tedam.model.JobRunnerDetailCommand;
import com.lbs.tedam.model.TedamSocketMessage;
import com.lbs.tedam.model.DTO.LogoTestResult;
import com.lbs.tedam.service.AgentGUIControllerService;
import com.lbs.tedam.util.EnumsV2.CommandStatus;
import com.lbs.tedam.util.EnumsV2.ExecutionStatus;
import com.lbs.tedam.util.EnumsV2.RunOrder;
import com.lbs.tedam.util.EnumsV2.TedamSocketMessageType;
import com.lbs.tedam.util.HasLogger;
import com.lbs.tedam.util.TedamFileUtils;
import com.lbs.tedam.util.TedamJsonFactory;
import com.lbs.tedam.util.TedamProcessUtils;
import com.lbs.tedam.util.TedamStringUtils;

public class AgentGUIControllerServiceImpl implements AgentGUIControllerService, HasLogger {

	@Override
	public void startJobCommandListOperations(JobRunnerCommand jobRunnerCommand,
			JobRunnerSocketClientListener jobRunnerSocketClientListener) {
		appendTextAreaContent("TestSetId : " + jobRunnerCommand.getTestSetId() + " started.");

		for (JobRunnerDetailCommand jobRunnerDetailCommand : jobRunnerCommand.getJobRunnerDetailCommandList()) {

			String command = getCommand(jobRunnerDetailCommand);
			getLogger().info("command : " + command);

			appendTextAreaContent("TestCaseId : " + jobRunnerDetailCommand.getTestCaseId() + " for  "
					+ jobRunnerDetailCommand.getDraftCommandName() + " command status : "
					+ CommandStatus.IN_PROGRESS.getValue());

			jobRunnerDetailCommand.setCommandStatus(CommandStatus.IN_PROGRESS);
			sendJobRunnerDetailCommand(jobRunnerSocketClientListener, jobRunnerDetailCommand);

			String testResultReportPath = TedamStringUtils.getTestResultReportPath(jobRunnerCommand.getTestSetId(),
					jobRunnerDetailCommand.getTestCaseId(),
					jobRunnerDetailCommand.getRunOrder() == RunOrder.CREATE_SCRIPT ? true : false);
			getLogger().info("testResultReportPath : " + testResultReportPath);

			File excelFile = new File(testResultReportPath);
			TedamFileUtils.deleteFile(excelFile.getPath());

			LocalDateTime startDate = LocalDateTime.now();
			getLogger().info("startDate : " + startDate);

			boolean isCommandSucces = TedamProcessUtils.launchCommand(command,
					TedamProcessUtils.getClassPath(AgentGUIController.class),
					jobRunnerDetailCommand.getLastExpectedResult());

			getLogger().info("jobCommand calistirildi. isCommandSucces  : " + isCommandSucces);

			LocalDateTime endDate = LocalDateTime.now();
			getLogger().info("endDate : " + endDate);

			if (!isCommandSucces) {// it will enter here if it will be false if the command does not arrive at the
									// desired line.
									// According to the situation, the next testCase should be considered
									// TODO:mikyas
				appendTextAreaContent("TestCaseId : " + jobRunnerDetailCommand.getTestCaseId()
						+ " the command could not be completed as expected");
				jobRunnerDetailCommand.setExecutionStatus(ExecutionStatus.BLOCKED);
				jobRunnerDetailCommand.setCommandStatus(CommandStatus.BLOCKED);
				prepareTestRunStartAndEndDate(jobRunnerDetailCommand, startDate, endDate);
				sendJobRunnerDetailCommand(jobRunnerSocketClientListener, jobRunnerDetailCommand);
				continue;
			}
			boolean isTestResultFileExist = TedamFileUtils.isFileExist(testResultReportPath);
			if (isTestResultFileExist) {
				getLogger().info("TestCaseId : " + jobRunnerDetailCommand.getTestCaseId()
						+ " command finished as requested and the file was created.");
				List<LogoTestResult> results = TedamFileUtils.readFromExcelFileTEDAM(excelFile.getPath());
				jobRunnerDetailCommand.setTestResultList(results);
				jobRunnerDetailCommand.setCommandStatus(CommandStatus.COMPLETED);
			} else {
				getLogger().info("TestCaseId : " + jobRunnerDetailCommand.getTestCaseId()
						+ " command finished as desired, but the result control file was not found.");
				jobRunnerDetailCommand.setExecutionStatus(ExecutionStatus.BLOCKED);
				jobRunnerDetailCommand.setCommandStatus(CommandStatus.BLOCKED);
			}
			prepareTestRunStartAndEndDate(jobRunnerDetailCommand, startDate, endDate);
			appendTextAreaContent("TestCaseId : " + jobRunnerDetailCommand.getTestCaseId() + " for commandStatus : "
					+ CommandStatus.COMPLETED.getValue());
			sendJobRunnerDetailCommand(jobRunnerSocketClientListener, jobRunnerDetailCommand);

		}
		appendTextAreaContent("TestSetId : " + jobRunnerCommand.getTestSetId() + " it's over.");
	}

	private void prepareTestRunStartAndEndDate(JobRunnerDetailCommand jobRunnerDetailCommand, LocalDateTime startDate,
			LocalDateTime endDate) {
		jobRunnerDetailCommand.setStartDate(startDate);
		jobRunnerDetailCommand.setEndDate(endDate);
	}

	private String getCommand(JobRunnerDetailCommand jobRunnerDetailCommand) {
		String osName = System.getProperty("os.name");
		String osNameLowerCase = osName.toLowerCase();
		boolean isWindows = osNameLowerCase.contains("windows");
		if (isWindows) {
			return jobRunnerDetailCommand.getWindowsCommand();
		} else {
			return jobRunnerDetailCommand.getUnixCommand();
		}
	}

	/**
	 * appendTextAreaContent CommandTextArea with this method is added to the system
	 * clock or content.<br>
	 * 
	 * @author Canberk.Erkmen
	 * @param content <br>
	 */
	private void appendTextAreaContent(String content) {
		AgentGUIController.getAgentGUI().appendTextAreaContent(content);
		getLogger().info(content);
	}

	private void sendJobRunnerDetailCommand(JobRunnerSocketClientListener jobRunnerSocketClientListener,
			JobRunnerDetailCommand jobRunnerDetailCommand) {
		TedamSocketMessage tedamSocketMessage = new TedamSocketMessage();
		tedamSocketMessage.setTedamSocketMessageType(TedamSocketMessageType.JOB);
		tedamSocketMessage.setDetail(TedamJsonFactory.toJson(jobRunnerDetailCommand));
		jobRunnerSocketClientListener.sendMessage(TedamJsonFactory.toJson(tedamSocketMessage));
	}

}
