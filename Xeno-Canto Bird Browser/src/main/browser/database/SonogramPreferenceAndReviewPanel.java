package main.browser.database;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import main.ConnectionFactory;
import main.browser.Browser;
import main.onset.Onset;
import main.sonogram.Sonogram;
import main.sonogram.SonogramData;

public class SonogramPreferenceAndReviewPanel extends JPanel {
	/**
	 * Card Layout:  PREFERENCE_DISPLAY_CARD shows onset and sonogram preferences.
	 * SONOGRAM_REVIEW_CARD displays and marks sonograms for review.
	 * They can be listened to and marked "Clean", "Noisy", "Other Bird" and "Noisy/Other Bird
	 * Sonograms with non-null quality are written to the database when "Done" is pressed.
	 */
	private static final long serialVersionUID = 1L;
	public static final String PREFERENCE_DISPLAY_CARD = "Preference Panel for database view";
	public static final String SONOGRAM_REVIEW_CARD = "Sonogram Review Panel for database view";
	
	final PreferenceDisplayPanel preferenceDisplayPanel;
	Set<Sonogram> sonogramSet;
	
	private final SonogramReviewPanel sonogramReviewPanel;
	private final DatabaseCard databaseCard;
	private final ExecutorService pool;
	
	volatile int databaseConnections;
	
	public SonogramPreferenceAndReviewPanel(DatabaseCard dbc) {
		super();
		databaseCard = dbc;
		setLayout(new CardLayout());
		preferenceDisplayPanel = new PreferenceDisplayPanel(databaseCard);
		add(preferenceDisplayPanel, PREFERENCE_DISPLAY_CARD);
		add(sonogramReviewPanel = new SonogramReviewPanel(databaseCard), SONOGRAM_REVIEW_CARD);
		setPreferredSize(new Dimension(DatabaseCard.PREFERENCE_REVIEW_WIDTH, DatabaseCard.PREFERENCE_REVIEW_HEIGHT));
		pool = Executors.newCachedThreadPool();
	}
	
	public void sonogramReviewDone() {
		HasSonogramsKey key = new HasSonogramsKey(databaseCard.currentRecordingId,
				databaseCard.onsetPreference.getOpId(),
				databaseCard.sonogramPreference.getSpId());
		if (createsSonograms())
			if (!databaseCard.hasSonogramsSet.contains(key)
				|| replaceSonogramsDialog()) {
				databaseConnections = 0;
				pool.execute(new CreateSonogramsWorker(key));
			}
        CardLayout cl = (CardLayout)(getLayout());
        cl.show(this, PREFERENCE_DISPLAY_CARD);
        databaseCard.databaseBrowserPanel.recordingsBrowser.enable();
        databaseCard.enableDatabaseControls();
	}

	private boolean createsSonograms() {
		if (sonogramSet == null)
			return false;
		Iterator<Sonogram> sonogramIterator = sonogramSet.iterator();
		while (sonogramIterator.hasNext())
			if (sonogramIterator.next().getQuality() != null)
				return true;
		return false;
	}
	
	private class CreateSonogramsWorker extends SwingWorker<Void, Void> {
		private final HasSonogramsKey key;
		
		CreateSonogramsWorker(HasSonogramsKey k) { key = k; }
		
		@Override
		protected Void doInBackground() throws Exception {
			createSonograms();
			return null;
		}

		@Override
		protected void done() {
			databaseCard.hasSonogramsSet.add(key);
			super.done();
		}
	}

	private void createSonograms() {
		Connection conn = null;
		Iterator<Sonogram> sonogramIterator = sonogramSet.iterator();
		while (sonogramIterator.hasNext()) {
			Sonogram sonogram = sonogramIterator.next();
			if (sonogram.getQuality() != null) {
				while (databaseConnections == Browser.MAX_DATABASE_CONNECTIONS)
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						System.out.println("SonogramPreferenceAndReviewPanel.createSonograms(): Sleep interrupted");
						e1.printStackTrace();
						return;
					}
				try {
					conn = ConnectionFactory.getConnection();
				} catch (SQLException e) {
					System.out.println("SonogramPreferenceAndReviewPanel.createSonograms(): Can't connect to db");
					e.printStackTrace();
					return;
				}
				databaseConnections++;
				sonogram.create(conn);
				createSonogramData(sonogram, conn);
			}
		}
	}

	
	private void createSonogramData(Sonogram sonogram, Connection conn) {
		SonogramDataCreationProgressPanel sdcpp = new SonogramDataCreationProgressPanel(sonogram);
		databaseCard.sonogramCreationProgressPanel.add(sdcpp);
		databaseCard.sonogramCreationProgressPanel.revalidate();
		CreateSonogramDataWorker createSonogramDataWorker = new CreateSonogramDataWorker(sonogram, conn, sdcpp);
		pool.execute(createSonogramDataWorker);
	}

	private class CreateSonogramDataWorker extends SwingWorker<Void, Void> {
		private final Sonogram sonogram;
		private final Connection conn;
		private final SonogramDataCreationProgressPanel progressPanel;
		
		CreateSonogramDataWorker(Sonogram s, Connection c, SonogramDataCreationProgressPanel sdcpp) {
			sonogram = s;
			conn = c;
			progressPanel = sdcpp;
			}
		
		@Override
		protected Void doInBackground() throws Exception {
			Iterator<SonogramData> sonogramDataIterator = sonogram.sonogramDataSet.iterator();
			while (sonogramDataIterator.hasNext()) {
				SonogramData sonogramData = sonogramDataIterator.next();
				if (sonogramData.sonogramDataId == -1)
					sonogramData.create(sonogram, conn);
				else
					sonogramData.createIntersect(sonogram, conn);
				int p = progressPanel.progressBar.getValue();
				progressPanel.progressBar.setValue(p+1);
				progressPanel.repaint();
			}
			
			return null;
		}
		
		@Override
		protected void done() {
			databaseCard.sonogramCreationProgressPanel.remove(progressPanel);
			databaseCard.sonogramCreationProgressPanel.repaint();
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			databaseConnections--;
			
			super.done();
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
