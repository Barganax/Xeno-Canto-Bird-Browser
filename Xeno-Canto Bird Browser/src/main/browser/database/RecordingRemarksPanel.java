package main.browser.database;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class RecordingRemarksPanel extends JPanel {
	final JTextArea remarksTextArea;
	
	public RecordingRemarksPanel() {
		remarksTextArea = new JTextArea();
		remarksTextArea.setLineWrap(true);
		remarksTextArea.setPreferredSize(new Dimension(DatabaseCard.RECORDING_REMARKS_WIDTH, DatabaseCard.RECORDING_REMARKS_HEIGHT));
		add(remarksTextArea);
	}
}
