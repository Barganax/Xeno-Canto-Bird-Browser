package main.browser.database;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class PreferenceDisplayPanel extends JPanel {
	private final DatabaseCard databaseCard;
	private final BoxLayout layout;

	final OnsetPreferenceDisplayPanel onsetPreferenceDisplayPanel;
	final SonogramPreferenceDisplayPanel sonogramPreferenceDisplayPanel;
	
	PreferenceDisplayPanel(DatabaseCard dbc) {
		databaseCard = dbc;
		layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		onsetPreferenceDisplayPanel = new OnsetPreferenceDisplayPanel(databaseCard);
		sonogramPreferenceDisplayPanel = new SonogramPreferenceDisplayPanel(databaseCard);
		add(onsetPreferenceDisplayPanel);
		Dimension minSize = new Dimension(5, 100);
		Dimension prefSize = new Dimension(5, 100);
		Dimension maxSize = new Dimension(Short.MAX_VALUE, 100);
		add(new Box.Filler(minSize, prefSize, maxSize));

		add(sonogramPreferenceDisplayPanel);
		setPreferredSize(new Dimension(DatabaseCard.PREFERENCE_REVIEW_WIDTH, DatabaseCard.PREFERENCE_REVIEW_HEIGHT));
		
	}

}
