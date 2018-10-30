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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.SystemColor;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lbs.tedam.util.Constants;
import com.lbs.tedam.util.DateTimeUtils;

/**
 * @author Canberk.Erkmen
 *
 */
public class AgentGUI extends JFrame {

	/** long serialVersionUID */
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentGUI.class);

	private JPanel contentPane;
	private TextAreaFIFO commandTextArea;

	/**
	 * Create the frame.
	 */
	public AgentGUI() {
		setTitle(Constants.AGENT_TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 750, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		commandTextArea = new TextAreaFIFO(Constants.MAX_LINE);
		commandTextArea.setWrapStyleWord(true);
		commandTextArea.setLineWrap(true);
		commandTextArea.setFont(new Font("Comic Sans MS", Font.PLAIN, 15));
		commandTextArea.setEditable(false);
		commandTextArea.setBackground(SystemColor.menu);
		scrollPane.setViewportView(commandTextArea);

	}

	/**
	 * this method getTextAreaContent <br>
	 * 
	 * @author Canberk.Erkmen
	 * @return <br>
	 */
	public String getTextAreaContent() {
		return commandTextArea.getText();
	}

	/**
	 * this method setTextAreaContent <br>
	 * 
	 * @author Canberk.Erkmen
	 * @param setContent
	 * @return <br>
	 */
	public boolean setTextAreaContent(String setContent) {
		try {
			commandTextArea.setText(setContent);
		} catch (Exception e) {
			LOGGER.error("" + e);
			return false;
		}
		return true;
	}

	/**
	 * this method appendTextAreaContent <br>
	 * 
	 * @author Canberk.Erkmen
	 * @param appendContent
	 * @return <br>
	 */
	public boolean appendTextAreaContent(String appendContent) {
		try {
			commandTextArea.append("\n" + DateTimeUtils.getTEDAMdbFormatSystemDateAsString() + " :  " + appendContent);
			commandTextArea.setCaretPosition(commandTextArea.getDocument().getLength());
		} catch (Exception e) {
			LOGGER.error("" + e);
			return false;
		}
		return true;
	}

	/**
	 * this method getTextArea <br>
	 * 
	 * @author Canberk.Erkmen
	 * @return <br>
	 */
	public JTextArea getTextArea() {
		return commandTextArea;
	}

	/**
	 * this method setTextArea <br>
	 * 
	 * @author Canberk.Erkmen
	 * @param jTextArea
	 * @return <br>
	 */
	public boolean setTextArea(TextAreaFIFO jTextArea) {
		try {
			commandTextArea = jTextArea;
		} catch (Exception e) {
			LOGGER.error("" + e);
			return false;
		}
		return true;
	}

	public class TextAreaFIFO extends JTextArea implements DocumentListener {
		/** long serialVersionUID */
		private static final long serialVersionUID = 1L;
		private int maxLines;

		public TextAreaFIFO(int lines) {
			maxLines = lines;
			getDocument().addDocumentListener(this);
		}

		public void insertUpdate(DocumentEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					removeLines();
				}
			});
		}

		@Override
		public void changedUpdate(DocumentEvent arg0) {
			LOGGER.debug("changedUpdate overwrite");
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			LOGGER.debug("changedUpdate overwrite");
		}

		public void removeLines() {
			Element root = getDocument().getDefaultRootElement();
			while (root.getElementCount() > maxLines) {
				Element firstLine = root.getElement(0);
				try {
					getDocument().remove(0, firstLine.getEndOffset());
				} catch (BadLocationException ble) {
					System.out.println(ble);
				}
			}
		}

	}
}
