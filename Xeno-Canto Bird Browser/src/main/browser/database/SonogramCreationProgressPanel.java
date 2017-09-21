package main.browser.database;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class SonogramCreationProgressPanel extends JPanel {
	/**
	 * This holds the SonogramDataCreationProgressPanel objects
	 */
	private static final long serialVersionUID = 1L;
	private final DatabaseCard databaseCard;
	public SonogramCreationProgressPanel(DatabaseCard dbc) {
		databaseCard = dbc;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
}
