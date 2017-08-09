package main.browser.database;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import main.browser.Browser;
import main.sonogram.Sonogram;
import main.sonogram.Sonogram.EnumSonogramQuality;

public class SonogramReviewPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final DatabaseCard databaseCard;
	private final JList<Sonogram> sonogramReviewList;
	private final SonogramReviewListener sonogramReviewListener;
	private final ButtonBar buttonBar;
	private final JButton doneButton;
	private Sonogram previousSonogram;

	public SonogramReviewPanel(DatabaseCard dbc) {
		super();
		databaseCard = dbc;
		sonogramReviewListener = new SonogramReviewListener();
		sonogramReviewList = new JList<Sonogram>();
		sonogramReviewList.addListSelectionListener(sonogramReviewListener);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(Browser.reviewSonogramsLabel);
        JScrollPane listScrollPane = new JScrollPane(sonogramReviewList);
        add(listScrollPane);
        add(buttonBar = new ButtonBar());
		doneButton = new JButton("Done");
		doneButton.setActionCommand("done");
		doneButton.addActionListener(sonogramReviewListener);;
		add(doneButton);
	}
	
	public void setupReviewList() {
		Sonogram[] tempArray = new Sonogram[0];
		sonogramReviewList.setListData(databaseCard.sonogramPreferenceAndReviewPanel.sonogramSet.toArray(tempArray));
        sonogramReviewList.addListSelectionListener(sonogramReviewListener);
        sonogramReviewList.setVisibleRowCount(15);
        sonogramReviewList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sonogramReviewList.setSelectedIndex(0);
	}

	private class ButtonBar extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final JPanel playClearButtonPanel;
		final JButton playButton;
		final JButton clearButton;
		final JRadioButton cleanButton;
		final JRadioButton noisyButton;
		final JRadioButton otherBirdButton;
		final JRadioButton noisyOtherBirdButton;
		final ButtonGroup sonogramQualityButtonGroup;
		final JPanel sonogramQualityButtonBar;
		
		ButtonBar() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			playClearButtonPanel = new JPanel();
			playClearButtonPanel.setLayout(new BoxLayout(playClearButtonPanel, BoxLayout.Y_AXIS));
			playButton = new JButton("Play");
			playButton.setActionCommand("play");
			playButton.addActionListener(sonogramReviewListener);
			playClearButtonPanel.add(playButton);
			clearButton = new JButton("Clear");
			clearButton.setActionCommand("clear");
			clearButton.addActionListener(sonogramReviewListener);
			playClearButtonPanel.add(clearButton);
			add(playClearButtonPanel);
			cleanButton = new JRadioButton("Clean");
			cleanButton.setActionCommand("clean");
			cleanButton.addActionListener(sonogramReviewListener);
			noisyButton = new JRadioButton("Noisy");
			noisyButton.setActionCommand("noisy");
			noisyButton.addActionListener(sonogramReviewListener);
			otherBirdButton = new JRadioButton("Other Bird");
			otherBirdButton.setActionCommand("other bird");
			otherBirdButton.addActionListener(sonogramReviewListener);
			noisyOtherBirdButton = new JRadioButton("Noisy & Other Bird");
			noisyOtherBirdButton.setActionCommand("noisy & other bird");
			noisyOtherBirdButton.addActionListener(sonogramReviewListener);
			sonogramQualityButtonGroup = new ButtonGroup();
			sonogramQualityButtonGroup.add(cleanButton);
			sonogramQualityButtonGroup.add(noisyButton);
			sonogramQualityButtonGroup.add(otherBirdButton);
			sonogramQualityButtonGroup.add(noisyOtherBirdButton);
			sonogramQualityButtonBar = new JPanel();
			sonogramQualityButtonBar.setLayout(new BoxLayout(sonogramQualityButtonBar, BoxLayout.Y_AXIS));
			sonogramQualityButtonBar.add(cleanButton);
			sonogramQualityButtonBar.add(otherBirdButton);
			sonogramQualityButtonBar.add(noisyButton);
			sonogramQualityButtonBar.add(noisyOtherBirdButton);
			add(sonogramQualityButtonBar);
		}
		
		void loadQuality(Sonogram sonogram) {
			sonogramQualityButtonGroup.clearSelection();;
			EnumSonogramQuality sonogramQuality = sonogram.getQuality();
			if (sonogramQuality == EnumSonogramQuality.CLEAN)
				cleanButton.setSelected(true);
			else if (sonogramQuality == EnumSonogramQuality.OTHER_BIRD)
				otherBirdButton.setSelected(true);
			else if (sonogramQuality == EnumSonogramQuality.NOISY)
				noisyButton.setSelected(true);
			else if (sonogramQuality == EnumSonogramQuality.NOISY_OTHER_BIRD)
				noisyOtherBirdButton.setSelected(true);
			invalidate();
		}
	}
	
	private class SonogramReviewListener implements ListSelectionListener, ActionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()
					|| databaseCard.sonogramPreferenceAndReviewPanel.sonogramSet == null) return;
			if (sonogramReviewList.getSelectedIndex() == -1) return;
			Sonogram s = sonogramReviewList.getSelectedValue();
			sonogramReviewList.ensureIndexIsVisible(sonogramReviewList.getSelectedIndex());
			if (previousSonogram != null)
				databaseCard.sonogramPanel.markSonogram(previousSonogram);
			databaseCard.sonogramPanel.markSonogram(s);
			previousSonogram = s;
			buttonBar.loadQuality(s);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Sonogram sonogram = sonogramReviewList.getSelectedValue();
			String ac = e.getActionCommand();
			if (ac == "done") {
				if (previousSonogram != null) {
					databaseCard.sonogramPanel.markSonogram(previousSonogram);
					previousSonogram = null;
				}
				databaseCard.sonogramPreferenceAndReviewPanel.sonogramReviewDone();
				return;
			}
			if (ac == "clear") {
				buttonBar.sonogramQualityButtonGroup.clearSelection();
				sonogramReviewList.requestFocusInWindow();
				sonogram.setQuality(null);
			}
			else if (ac == "play") {
				sonogram.play();
				sonogramReviewList.requestFocusInWindow();
				return;
			}
			else if (ac == "clean")
				sonogram.setQuality(EnumSonogramQuality.CLEAN);
			else if (ac == "other bird")
				sonogram.setQuality(EnumSonogramQuality.OTHER_BIRD);
			else if (ac == "noisy")
				sonogram.setQuality(EnumSonogramQuality.NOISY);
			else if (ac == "noisy & other bird")
				sonogram.setQuality(EnumSonogramQuality.NOISY_OTHER_BIRD);
			int selectedIndex = sonogramReviewList.getSelectedIndex();
			if (selectedIndex < databaseCard.sonogramPreferenceAndReviewPanel.sonogramSet.size() - 1) {
		        sonogramReviewList.setSelectedIndex(selectedIndex + 1);
				sonogram = sonogramReviewList.getSelectedValue();
				buttonBar.loadQuality(sonogram);
			}
			sonogramReviewList.requestFocusInWindow();
		}
	}
}
