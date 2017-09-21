package main.browser.database;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import main.sonogram.Sonogram;

public class SonogramDataCreationProgressPanel extends JPanel {
	/**
	 * Progress monitor panel for sonograms.  Progress is measured by the number of SonogramData objects created
	 */
	private static final long serialVersionUID = 1L;
	final JLabel label;
	final JProgressBar progressBar;

	public SonogramDataCreationProgressPanel(Sonogram sonogram) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		label = new JLabel(sonogram.toString());
		add(label);
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(sonogram.getSonogramPreference().dataCount);
		progressBar.setValue(0);
		add(progressBar);
	}
}
