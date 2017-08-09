package main.browser.database;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class RecordingRemarksPanel extends JPanel {
	final JTextArea remarksTextArea;
	
	public RecordingRemarksPanel() {
		remarksTextArea = new JTextArea();
		remarksTextArea.setLineWrap(true);
		remarksTextArea.setPreferredSize(new Dimension(DatabaseCard.PREFERENCE_REVIEW_WIDTH, DatabaseCard.PREFERENCE_REVIEW_HEIGHT));
		add(remarksTextArea);
	}
}
