package main.browser.database;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import main.sonogram.SonogramFrequencyScale;
import main.sonogram.SonogramPanel;
import main.sonogram.SonogramTimeScale;
import main.sonogram.Spacer;

public class BrowserSonogramPanel extends JPanel {
	private final DatabaseCard databaseCard;
	
	SonogramTimeScale timeScale;
	SonogramFrequencyScale frequencyScale;
	JScrollPane sonogramScrollPane;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BrowserSonogramPanel(DatabaseCard dbc) {
		super();
		databaseCard = dbc;
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		JComponent sonogramScrollPane = sonogramScrollPane();
		JComponent spacer = new Spacer();
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(spacer, Spacer.IMAGE_WIDTH, Spacer.IMAGE_WIDTH, Spacer.IMAGE_WIDTH)
						.addComponent(frequencyScale, SonogramFrequencyScale.IMAGE_WIDTH, SonogramFrequencyScale.IMAGE_WIDTH, SonogramFrequencyScale.IMAGE_WIDTH))
				.addComponent(sonogramScrollPane, DatabaseCard.SONOGRAM_VIEWPORT_WIDTH,
						DatabaseCard.SONOGRAM_VIEWPORT_WIDTH,
						DatabaseCard.SONOGRAM_VIEWPORT_WIDTH));
		layout.setVerticalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(spacer, Spacer.IMAGE_HEIGHT, Spacer.IMAGE_HEIGHT, Spacer.IMAGE_HEIGHT)
						.addComponent(frequencyScale, SonogramPanel.IMAGE_HEIGHT, SonogramPanel.IMAGE_HEIGHT, SonogramPanel.IMAGE_HEIGHT))
				.addComponent(sonogramScrollPane));

	}
	
	private JComponent sonogramScrollPane() {
		JPanel sp = new JPanel();
		GroupLayout layout = new GroupLayout(sp);
		sp.setLayout(layout);
		timeScale = new SonogramTimeScale();
		frequencyScale = new SonogramFrequencyScale();
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(timeScale, SonogramPanel.IMAGE_WIDTH, SonogramPanel.IMAGE_WIDTH, SonogramPanel.IMAGE_WIDTH)
				.addComponent(databaseCard.sonogramPanel, SonogramPanel.IMAGE_WIDTH, SonogramPanel.IMAGE_WIDTH, SonogramPanel.IMAGE_WIDTH));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(timeScale, SonogramTimeScale.IMAGE_HEIGHT, SonogramTimeScale.IMAGE_HEIGHT, SonogramTimeScale.IMAGE_HEIGHT)
				.addComponent(databaseCard.sonogramPanel, SonogramPanel.IMAGE_HEIGHT, SonogramPanel.IMAGE_HEIGHT, SonogramPanel.IMAGE_HEIGHT));
		sonogramScrollPane = new JScrollPane(sp);
		databaseCard.sonogramPanel.setScrollPane(sonogramScrollPane);
		return sonogramScrollPane;
	}
	

}
