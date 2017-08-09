package main.browser.database;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import main.browser.Browser;
import main.onset.OnsetPreference;


public class OnsetPreferenceDisplayPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int DATABASE_ONSET_PREFERENCE_SELECT_HEIGHT = 100;
	private OnsetPreferenceROListener onsetPreferenceROListener = null;
	private JComponent onsetPreferenceSelect = null;
	private JLabel onsetPreferenceTagLabel = null;
	private JLabel onsetPreferenceGainLabel = null;
	private JLabel onsetPreferenceSilenceThresholdLabel = null;
	private JLabel onsetPreferencePeakThresholdLabel = null;
	private JLabel onsetPreferenceMinInteronsetIntervalLabel = null;
	private final DatabaseCard databaseCard;
	
	JTable onsetPreferenceTableRO;
	
	public OnsetPreferenceDisplayPanel(DatabaseCard dbc) {
		super();
		databaseCard = dbc;
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		setOnsetPreferenceValues();
		onsetPreferenceROListener = new OnsetPreferenceROListener();
		onsetPreferenceSelect = onsetPreferenceTableRO();
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(onsetPreferenceSelect)
				.addComponent(onsetPreferenceTagLabel)
				.addComponent(onsetPreferenceGainLabel)
				.addComponent(onsetPreferenceSilenceThresholdLabel)
				.addComponent(onsetPreferencePeakThresholdLabel)
				.addComponent(onsetPreferenceMinInteronsetIntervalLabel));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(onsetPreferenceSelect,
						DATABASE_ONSET_PREFERENCE_SELECT_HEIGHT,
						DATABASE_ONSET_PREFERENCE_SELECT_HEIGHT,
						DATABASE_ONSET_PREFERENCE_SELECT_HEIGHT
						)
				.addComponent(onsetPreferenceTagLabel)
				.addComponent(onsetPreferenceGainLabel)
				.addComponent(onsetPreferenceSilenceThresholdLabel)
				.addComponent(onsetPreferencePeakThresholdLabel)
				.addComponent(onsetPreferenceMinInteronsetIntervalLabel));

	}

	private class OnsetPreferenceROListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
	        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
	        if (!lsm.isSelectionEmpty()) {
	            int selectedRow = lsm.getMinSelectionIndex();
//	        	System.out.println("list select: "+selectedRow);
	            databaseCard.onsetPreference = OnsetPreference.retrieve(databaseCard.onsetPreferenceList.get(selectedRow).getOpId());
	            loadOnsetPreferenceValues();
	        }			
		}
	}
	
	private void setOnsetPreferenceValues() {
		onsetPreferenceTagLabel = new JLabel("Tag: "+databaseCard.onsetPreference.getTag());
		onsetPreferenceGainLabel = new JLabel("Gain: "+databaseCard.onsetPreference.getGain());
		onsetPreferenceSilenceThresholdLabel = new JLabel("Silence Threshold: "+databaseCard.onsetPreference.getSilenceThreshold()+" dB");
		onsetPreferencePeakThresholdLabel = new JLabel("Peak Threshold: "+databaseCard.onsetPreference.getPeakThreshold());
		onsetPreferenceMinInteronsetIntervalLabel
			= new JLabel("Minimum Inter-Onset Interval: "+databaseCard.onsetPreference.getMinInteronsetInterval()+" msec");
	}

	private void loadOnsetPreferenceValues() {
		onsetPreferenceTagLabel.setText("Tag: "+databaseCard.onsetPreference.getTag());
		onsetPreferenceGainLabel.setText("Gain: "+databaseCard.onsetPreference.getGain());
		onsetPreferenceSilenceThresholdLabel.setText("Silence Threshold: "+databaseCard.onsetPreference.getSilenceThreshold()+" dB");
		onsetPreferencePeakThresholdLabel.setText("Peak Threshold: "+databaseCard.onsetPreference.getPeakThreshold());
		onsetPreferenceMinInteronsetIntervalLabel.setText("Minimum Inter-Onset Interval: "+databaseCard.onsetPreference.getMinInteronsetInterval()+" msec");
	}

	private JComponent onsetPreferenceTableRO() {
		JPanel osp = new JPanel();
		osp.setLayout(new BoxLayout(osp, BoxLayout.Y_AXIS));
		onsetPreferenceTableRO = new JTable(new OnsetPreferenceROTableModel());
	    onsetPreferenceTableRO.setPreferredScrollableViewportSize(new Dimension(200, 700));
	    onsetPreferenceTableRO.setFillsViewportHeight(true);
	    onsetPreferenceTableRO.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    ListSelectionModel lsm = onsetPreferenceTableRO.getSelectionModel();
	    lsm.addListSelectionListener(onsetPreferenceROListener);
		onsetPreferenceTableRO.getColumnModel().getColumn(0).setPreferredWidth(5);
		onsetPreferenceTableRO.getColumnModel().getColumn(1).setPreferredWidth(150);
	    JScrollPane optsp =  new JScrollPane(onsetPreferenceTableRO);
	    osp.add(Browser.onsetSettingsLabel);
	    osp.add(optsp);
	    return osp;
	}

	private static final int OP_ID_COL = 0;
	private static final int OP_TAG_COL = 1;

	private class OnsetPreferenceROTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnName = { "ID", "Tag" };
		public OnsetPreferenceROTableModel() {
			super();
			databaseCard.onsetPreferenceList = OnsetPreference.retrieve();
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }

		@Override
		public int getRowCount() { return databaseCard.onsetPreferenceList.size(); }

		@Override
		public int getColumnCount() { return columnName.length; }

		@Override
		public String getColumnName(int column) { return columnName[column]; }

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			OnsetPreference op = databaseCard.onsetPreferenceList.get(rowIndex);
			if (columnIndex == OP_ID_COL)
				return new Long(op.getOpId());
			else if (columnIndex == OP_TAG_COL)
				return op.getTag();
			return null;
		}	
	}
		


}
