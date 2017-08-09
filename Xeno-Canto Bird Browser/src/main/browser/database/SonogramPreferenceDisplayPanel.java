package main.browser.database;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import main.browser.Browser;
import main.sonogram.SonogramPreference;


public class SonogramPreferenceDisplayPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int DATABASE_SONOGRAM_PREFERENCE_SELECT_HEIGHT = 100;
	private final SonogramPreferenceSelect sonogramPreferenceSelect;
	private final SonogramPreferenceSelectListener sonogramPreferenceSelectListener;
	private final DatabaseCard databaseCard;
	private final JLabel sonogramPreferenceTagLabel;
	private final JLabel sonogramPreferenceBufferSizeLabel;
	private final JLabel sonogramPreferenceOverlapLabel;
	private final JLabel sonogramPreferenceWindowFunctionLabel;
	private final JLabel sonogramPreferenceLoCutoffLabel;
	private final JLabel sonogramPreferenceHiCutoffLabel;
	private final JLabel sonogramPreferenceComponentsLabel;
	
	public SonogramPreferenceDisplayPanel(DatabaseCard dbc) {
		super();
		databaseCard = dbc;
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		sonogramPreferenceSelectListener = new SonogramPreferenceSelectListener();
		sonogramPreferenceSelect = new SonogramPreferenceSelect();
		sonogramPreferenceTagLabel = new JLabel("Tag: "+databaseCard.sonogramPreference.getTag());
		sonogramPreferenceBufferSizeLabel = new JLabel("Buffer Size: "+databaseCard.sonogramPreference.getBufferSize());
		sonogramPreferenceOverlapLabel = new JLabel("Buffer Overlap: "+databaseCard.sonogramPreference.getOverlap());
		sonogramPreferenceWindowFunctionLabel = new JLabel("Window Function: "+databaseCard.sonogramPreference.getEnumWindowFunction());
		sonogramPreferenceLoCutoffLabel = new JLabel("Low Cutoff: "+databaseCard.sonogramPreference.getLoCutoff()+" Hz");
		sonogramPreferenceHiCutoffLabel = new JLabel("High Cutoff: "+databaseCard.sonogramPreference.getHiCutoff()+" Hz");
		sonogramPreferenceComponentsLabel = new JLabel("Components: "+databaseCard.sonogramPreference.getComponents());
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(sonogramPreferenceSelect)
				.addComponent(sonogramPreferenceTagLabel)
				.addComponent(sonogramPreferenceBufferSizeLabel)
				.addComponent(sonogramPreferenceOverlapLabel)
				.addComponent(sonogramPreferenceWindowFunctionLabel)
				.addComponent(sonogramPreferenceLoCutoffLabel)
				.addComponent(sonogramPreferenceHiCutoffLabel)
				.addComponent(sonogramPreferenceComponentsLabel));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(sonogramPreferenceSelect,
					DATABASE_SONOGRAM_PREFERENCE_SELECT_HEIGHT,
					DATABASE_SONOGRAM_PREFERENCE_SELECT_HEIGHT,
					DATABASE_SONOGRAM_PREFERENCE_SELECT_HEIGHT)
				.addComponent(sonogramPreferenceTagLabel)
				.addComponent(sonogramPreferenceBufferSizeLabel)
				.addComponent(sonogramPreferenceOverlapLabel)
				.addComponent(sonogramPreferenceWindowFunctionLabel)
				.addComponent(sonogramPreferenceLoCutoffLabel)
				.addComponent(sonogramPreferenceHiCutoffLabel)
				.addComponent(sonogramPreferenceComponentsLabel));
	}
	
	private void setSonogramPreferenceValues() {
		sonogramPreferenceTagLabel.setText("Tag: "+databaseCard.sonogramPreference.getTag());
		sonogramPreferenceBufferSizeLabel.setText("Buffer Size: "+databaseCard.sonogramPreference.getBufferSize());
		sonogramPreferenceOverlapLabel.setText("Buffer Overlap: "+databaseCard.sonogramPreference.getOverlap());
		sonogramPreferenceWindowFunctionLabel.setText("Window Function: "+databaseCard.sonogramPreference.getEnumWindowFunction());
		sonogramPreferenceLoCutoffLabel.setText("Low Cutoff: "+databaseCard.sonogramPreference.getLoCutoff()+" Hz");
		sonogramPreferenceHiCutoffLabel.setText("High Cutoff: "+databaseCard.sonogramPreference.getHiCutoff()+" Hz");
		sonogramPreferenceComponentsLabel.setText("Components: "+databaseCard.sonogramPreference.getComponents());
	}
	
	private class SonogramPreferenceSelect extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JTable sonogramPreferenceTable;
		
		SonogramPreferenceSelect() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			sonogramPreferenceTable = new JTable(new SonogramPreferenceTableModel());
			sonogramPreferenceTable.setPreferredScrollableViewportSize(new Dimension(200, 700));
			sonogramPreferenceTable.setFillsViewportHeight(true);
			sonogramPreferenceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ListSelectionModel lsm = sonogramPreferenceTable.getSelectionModel();
			lsm.addListSelectionListener(sonogramPreferenceSelectListener);
			sonogramPreferenceTable.getColumnModel().getColumn(0).setPreferredWidth(5);
			sonogramPreferenceTable.getColumnModel().getColumn(1).setPreferredWidth(150);
			JScrollPane dsptsp = new JScrollPane(sonogramPreferenceTable);
			add(Browser.sonogramSettingsLabel);
			add(dsptsp);
		}
	}
	
	private class SonogramPreferenceTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnName = { "ID", "Tag" };
				
		@Override
		public String getColumnName(int column) { return columnName[column]; }

		@Override
		public int getRowCount() { return databaseCard.sonogramPreferenceList.size(); }

		@Override
		public int getColumnCount() { return columnName.length; }

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex < this.getRowCount()) {
				SonogramPreference sp = databaseCard.sonogramPreferenceList.get(rowIndex);
				if (columnIndex == 0)
					return sp.getSpId();
				else if (columnIndex == 1)
					return sp.getTag();
			}
			return null;
		}
	}


	private class SonogramPreferenceSelectListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
		    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		    if (!lsm.isSelectionEmpty()) {
		        int selectedRow = lsm.getMinSelectionIndex();
//		        	System.out.println("list select: "+selectedRow);
		        databaseCard.sonogramPreference = databaseCard.sonogramPreferenceList.get(selectedRow);
		        setSonogramPreferenceValues();
		        revalidate();
		    }
		}
	}
}
