package main.browser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import main.onset.OnsetPreference;

public class OnsetPreferenceCard extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Browser browser;
	private final OnsetPreferenceListener onsetPreferenceListener;
	private final JPanel navBar;
	private final JPanel spacerPanel;
	private final JPanel editor;
	private final JPanel actionBar;
	private JLabel onsetTagLabel = null;
	private JTextField onsetTagTextField = null;
	private JLabel gainLabel = null;
	private JTextField gainTextField = null;
	private JLabel silenceThresholdLabel = null;
	private JTextField silenceThresholdTextField = null;
	private JLabel peakThresholdLabel = null;
	private JTextField peakThresholdTextField = null;
	private JLabel minInteronsetIntervalLabel = null;
	private JTextField minInteronsetIntervalTextField = null;
	private JButton addOnsetPreferenceRecButton = null;
	private JButton removeOnsetPreferenceRecButton = null;
	private JTable onsetPreferenceTable;
	private JButton closeButton = null;
	List<OnsetPreference> opList = null;


	OnsetPreferenceCard(Browser b) {
		browser = b;
		setLayout(new BorderLayout());
		opList = OnsetPreference.retrieve();
		onsetPreferenceListener = new OnsetPreferenceListener();
		navBar = (JPanel)codParamTable();
		add(navBar, BorderLayout.NORTH);
		spacerPanel = new JPanel();
		spacerPanel.setPreferredSize(new Dimension(150, 20));
		add(spacerPanel, BorderLayout.WEST);
		editor = (JPanel)onsetPreferenceEditor();
		add (editor, BorderLayout.CENTER);
		actionBar = (JPanel)codParamActionBar(); 
		add(actionBar, BorderLayout.SOUTH);
		loadOnsetEditorValues();

	}
	
	private JComponent codParamTable() {
		JPanel codpt = new JPanel();
		if (opList.size() == 0) {
			JButton defaultCODParamRecButton = new JButton("Add Default Settings");
			defaultCODParamRecButton.setActionCommand("new onset settings");
			defaultCODParamRecButton.addActionListener(onsetPreferenceListener);
			codpt.add(defaultCODParamRecButton);
			return codpt;
		}
		onsetPreferenceTable = new JTable(new CODParamTableModel());
        onsetPreferenceTable.setPreferredScrollableViewportSize(new Dimension(200, 100));
        onsetPreferenceTable.setFillsViewportHeight(true);
        onsetPreferenceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel lsm = onsetPreferenceTable.getSelectionModel();
        lsm.addListSelectionListener(onsetPreferenceListener);
        codpt.add(new JScrollPane(onsetPreferenceTable), BorderLayout.CENTER);
		return codpt;
	}

	private void loadOnsetEditorValues() {
		onsetTagTextField.setText(browser.databaseCard.onsetPreference.getTag());
		try {
			gainTextField.setText(Double.toString(browser.databaseCard.onsetPreference.getGain()));
			silenceThresholdTextField.setText(Double.toString(browser.databaseCard.onsetPreference.getSilenceThreshold()));
			peakThresholdTextField.setText(Double.toString(browser.databaseCard.onsetPreference.getPeakThreshold()));
			minInteronsetIntervalTextField.setText(Integer.toString(browser.databaseCard.onsetPreference.getMinInteronsetInterval()));
		} catch (NumberFormatException e) {}
	}
	
	private void storeOnsetEditorValues() {
		browser.databaseCard.onsetPreference.setTag(onsetTagTextField.getText());
		try {
			browser.databaseCard.onsetPreference.setGain(Double.parseDouble(gainTextField.getText()));
			browser.databaseCard.onsetPreference.setSilenceThreshold(Double.parseDouble(silenceThresholdTextField.getText()));
			browser.databaseCard.onsetPreference.setPeakThreshold(Double.parseDouble(peakThresholdTextField.getText()));
			browser.databaseCard.onsetPreference.setMinInteronsetInterval(Integer.parseInt(minInteronsetIntervalTextField.getText()));
		} catch (NumberFormatException e) {}
	}

	private class CODParamTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		String[] columnName = {
				"ID",
				"Tag",
				"Gain",
				"Silence Threshold",
				"Peak Threshold",
				"Minimum Inter-onset Interval"
		};
		
		@Override
		public String getColumnName(int column) { return columnName[column]; }

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }

		@Override
		public int getRowCount() { return opList.size(); }

		@Override
		public int getColumnCount() { return columnName.length; }

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex < getRowCount())
				if (columnIndex == 0)
					return opList.get(rowIndex).getOpId();
				else if (columnIndex == 1)
					return opList.get(rowIndex).getTag();
				else if (columnIndex == 2)
					return opList.get(rowIndex).getGain();
				else if (columnIndex == 3)
					return opList.get(rowIndex).getSilenceThreshold();
				else if (columnIndex == 4)
					return opList.get(rowIndex).getPeakThreshold();
				else if (columnIndex == 5)
					return opList.get(rowIndex).getMinInteronsetInterval();
			return null;
		}
	}
	
	private JComponent onsetPreferenceEditor() {
		JPanel ose = new JPanel();
		GroupLayout layout = new GroupLayout(ose);
		ose.setLayout(layout);
		layout.setAutoCreateGaps(true);
//		layout.setAutoCreateContainerGaps(true);
		
		setOnsetPreferenceComponents();
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(onsetTagLabel)
						.addComponent(onsetTagTextField, 100, 100, 150))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(gainLabel)
						.addComponent(gainTextField, 40, 40, 40))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(silenceThresholdLabel)
						.addComponent(silenceThresholdTextField, 40, 40, 40))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(peakThresholdLabel)
						.addComponent(peakThresholdTextField, 40, 40, 40))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(minInteronsetIntervalLabel)
						.addComponent(minInteronsetIntervalTextField, 60, 60, 60)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(onsetTagLabel)
						.addComponent(onsetTagTextField, 20, 20, 20))
				.addGroup(layout.createSequentialGroup()
						.addComponent(gainLabel)
						.addComponent(gainTextField, 20, 20, 20))
				.addGroup(layout.createSequentialGroup()
						.addComponent(silenceThresholdLabel)
						.addComponent(silenceThresholdTextField, 20, 20, 20))
				.addGroup(layout.createSequentialGroup()
						.addComponent(peakThresholdLabel)
						.addComponent(peakThresholdTextField, 20, 20, 20))
				.addGroup(layout.createSequentialGroup()
						.addComponent(minInteronsetIntervalLabel)
						.addComponent(minInteronsetIntervalTextField, 20, 20, 20)));

		return ose;
	}

	private void setOnsetPreferenceComponents() {
		onsetTagLabel = new JLabel("Tag");
		onsetTagTextField = new JTextField();
		onsetTagTextField.setActionCommand("onset tag change");
		onsetTagTextField.addActionListener(onsetPreferenceListener);
		gainLabel = new JLabel("Gain");
		gainTextField = new JTextField();
		gainTextField.setActionCommand("onset gain change");
		gainTextField.addActionListener(onsetPreferenceListener);
		silenceThresholdLabel = new JLabel("Silence Threshold (dB)");
		silenceThresholdTextField = new JTextField();
		silenceThresholdTextField.setActionCommand("onset silence threshold change");
		silenceThresholdTextField.addActionListener(onsetPreferenceListener);
		peakThresholdLabel = new JLabel("Peak Threshold");
		peakThresholdTextField = new JTextField();
		peakThresholdTextField.setActionCommand("onset peak threshold change");
		peakThresholdTextField.addActionListener(onsetPreferenceListener);
		minInteronsetIntervalLabel = new JLabel("Minimum Inter-onset Interval (msec)");
		minInteronsetIntervalTextField = new JTextField();
		minInteronsetIntervalTextField.setActionCommand("onset min interonset interval change");
		minInteronsetIntervalTextField.addActionListener(onsetPreferenceListener);
	}
	

	private class OnsetPreferenceListener implements ActionListener, ListSelectionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String ac = e.getActionCommand();
			if (ac == "onset tag change")
				browser.databaseCard.onsetPreference.setTag(onsetTagTextField.getText());
			else if (ac == "onset gain change")
				browser.databaseCard.onsetPreference.setGain(Double.parseDouble(gainTextField.getText()));
			else if (ac == "onset silence threshold change")
				browser.databaseCard.onsetPreference.setSilenceThreshold(Double.parseDouble(silenceThresholdTextField.getText()));
			else if (ac == "onset peak threshold change")
				browser.databaseCard.onsetPreference.setPeakThreshold(Double.parseDouble(peakThresholdTextField.getText()));
			else if (ac == "onset min interonset interval change")
				browser.databaseCard.onsetPreference.setMinInteronsetInterval(Integer.parseInt(minInteronsetIntervalTextField.getText()));
			else if (ac == "add onset settings rec") {
				storeOnsetEditorValues();
				browser.databaseCard.onsetPreference.create();
				revalidate();
			}
			else if (ac == "close") {
				browser.showContent();
				revalidate();
			}
			else if (ac == "remove") {
				ListSelectionModel lsm = onsetPreferenceTable.getSelectionModel();
				if (!lsm.isSelectionEmpty()) {
					int selectedRow = lsm.getMinSelectionIndex();
					long id = opList.get(selectedRow).getOpId();
					OnsetPreference.remove(id);
					loadOnsetEditorValues();
					revalidate();
				}
			}
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) return;
	        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
	        if (lsm.isSelectionEmpty()) {
	        	removeOnsetPreferenceRecButton.setEnabled(false);
	        } else {
	            int selectedRow = lsm.getMinSelectionIndex();
	            browser.databaseCard.onsetPreference = OnsetPreference.retrieve(opList.get(selectedRow).getOpId());
	            onsetPreferenceTable.invalidate();
	            loadOnsetEditorValues();
	            removeOnsetPreferenceRecButton.setEnabled(true);
	            revalidate();
	        }
		}
	}

	private JComponent codParamActionBar() {
		JPanel codpab = new JPanel();
		
		closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(onsetPreferenceListener);
		codpab.add(closeButton);
		addOnsetPreferenceRecButton = new JButton("Add Record");
		addOnsetPreferenceRecButton.setActionCommand("add onset settings rec");
		addOnsetPreferenceRecButton.addActionListener(onsetPreferenceListener);
		codpab.add(addOnsetPreferenceRecButton);
		removeOnsetPreferenceRecButton = new JButton("Remove");
		removeOnsetPreferenceRecButton.setActionCommand("remove");
		removeOnsetPreferenceRecButton.addActionListener(onsetPreferenceListener);
		removeOnsetPreferenceRecButton.setEnabled(false);
		codpab.add(removeOnsetPreferenceRecButton);
		
		return codpab;
	}
	

}
