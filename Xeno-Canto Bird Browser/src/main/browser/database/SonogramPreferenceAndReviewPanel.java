package main.browser.database;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import main.ConnectionFactory;
import main.onset.Onset;
import main.sonogram.Sonogram;

public class SonogramPreferenceAndReviewPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String PREFERENCE_DISPLAY_CARD = "Preference Panel for database view";
	public static final String SONOGRAM_REVIEW_CARD = "Sonogram Review Panel for database view";
	
	final PreferenceDisplayPanel preferenceDisplayPanel;
	
	private final SonogramReviewPanel sonogramReviewPanel;
	private final DatabaseCard databaseCard;
	Set<Sonogram> sonogramSet;
	
	public SonogramPreferenceAndReviewPanel(DatabaseCard dbc) {
		super();
		databaseCard = dbc;
		setLayout(new CardLayout());
		preferenceDisplayPanel = new PreferenceDisplayPanel(databaseCard);
		add(preferenceDisplayPanel, PREFERENCE_DISPLAY_CARD);
		add(sonogramReviewPanel = new SonogramReviewPanel(databaseCard), SONOGRAM_REVIEW_CARD);
		setPreferredSize(new Dimension(DatabaseCard.PREFERENCE_REVIEW_WIDTH, DatabaseCard.PREFERENCE_REVIEW_HEIGHT));
	}
	
	public void sonogramReviewDone() {
		HasSonogramsKey key = new HasSonogramsKey(databaseCard.currentRecordingId,
				databaseCard.onsetPreference.getOpId(),
				databaseCard.sonogramPreference.getSpId());
		if (createsSonograms())
			if (!databaseCard.hasSonogramsSet.contains(key)
				|| replaceSonogramsDialog()) {
			createSonograms();
			databaseCard.hasSonogramsSet.add(key);
		}
        CardLayout cl = (CardLayout)(getLayout());
        cl.show(this, PREFERENCE_DISPLAY_CARD);
        databaseCard.enableDatabaseControls();
	}

	private boolean createsSonograms() {
		if (this.sonogramSet == null)
			return false;
		Iterator<Sonogram> sonogramIterator = this.sonogramSet.iterator();
		while (sonogramIterator.hasNext())
			if (sonogramIterator.next().getQuality() != null)
				return true;
		return false;
	}
	
	private void createSonograms() {
		Iterator<Sonogram> sonogramIterator = sonogramSet.iterator();
		Connection conn;
		try {
			conn = ConnectionFactory.getConnection();
		while (sonogramIterator.hasNext()) {
			Sonogram sonogram = sonogramIterator.next();
			if (sonogram.getQuality() != null)
				sonogram.create(conn);
		}
		conn.close();
		} catch (SQLException e) {
			System.out.println("SonogramPreferenceAndReviewPanel.createSonograms()");
			e.printStackTrace();
		}
	}
	
	private boolean replaceSonogramsDialog() {
		String replaceString = "Replace existing sonograms?";
		int dialogResult = JOptionPane.showConfirmDialog (null,
				replaceString,
				replaceString,
				JOptionPane.YES_NO_OPTION);
		return dialogResult == JOptionPane.YES_OPTION;
	}

	public void reviewSonograms() {
		sonogramSet = databaseCard.sonogramProcessor.getSonogramSet();
		if (sonogramSet.isEmpty()) return;
		databaseCard.disableDatabaseControls();
		if (!databaseCard.existingOnsets)
			createOnsets();
		
/*
		Iterator<Sonogram> sonogramSetIterator = sonogramList.iterator();
		while (sonogramSetIterator.hasNext()) {
			Sonogram sonogram = sonogramSetIterator.next();
			sonogram.createWavFile(RESYNTH_DIRECTORY
					+ this.currentRecordingId
					+ "-"
					+ sonogram.getOnset().getOnsetPreferenceId()
					+ "-"
					+ sonogram.getSonogramPreference().getSpId()
					+ "-"
					+ sonogram.getOnset().getOnsetTime()
					+ "-l"
					+ sonogram.getLen()
					+ WAV_EXTENSION);
		}
	*/

		sonogramReviewPanel.setupReviewList();
        CardLayout cl = (CardLayout)(getLayout());
        cl.show(this, SONOGRAM_REVIEW_CARD);
        revalidate();
	}
	
	private void createOnsets() {
		Iterator<Onset> onsetIterator = databaseCard.onsetList.iterator();
		while (onsetIterator.hasNext())
			onsetIterator.next().create();
	}
}
