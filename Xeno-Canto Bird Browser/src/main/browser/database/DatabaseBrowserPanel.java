package main.browser.database;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import main.browser.Recording;
import main.browser.Species;
import main.sonogram.Sonogram;

public class DatabaseBrowserPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final String SPECIES_CARD = "Species";
	private static final String RECORDINGS_CARD = "Recordings";

	private final DatabaseCard databaseCard;

	private final CardLayout layout;
	private final SpeciesBrowser speciesBrowser;
	private final RecordingsBrowser recordingsBrowser;
	private final DatabaseBrowserActionListener databaseBrowserActionListener;
	private JLabel speciesLabel;
	
	private List<Recording> recordingList;
	private Species currentSpecies = null;
	private Recording currentRecording = null;

	JTable recordingTable;

	public DatabaseBrowserPanel(DatabaseCard dbc) {
		super();
		databaseCard = dbc;
		layout = new CardLayout();
		setLayout(layout);
		databaseBrowserActionListener = new DatabaseBrowserActionListener(this);
		speciesLabel = new JLabel();
		speciesBrowser = new SpeciesBrowser();
		recordingsBrowser = new RecordingsBrowser();
		add(speciesBrowser, SPECIES_CARD);
		add(recordingsBrowser, RECORDINGS_CARD);
	}

	private void setRecordingList() {
		if (currentSpecies == null)
			recordingList = new ArrayList<Recording>();
		else
			recordingList = Recording.retrieve(currentSpecies.getDbKey());
	}

	private class DatabaseBrowserActionListener implements ActionListener {
		private final JComponent parent;
		
		DatabaseBrowserActionListener(JComponent p) {
			super();
			parent = p;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String ac = e.getActionCommand();
			if (ac == "recordings") {
				speciesLabel.setText(currentSpecies.toString());
				setRecordingList();
//				System.out.println("DatabaseBrowserActionListener.actionPerformed(): "+recordingList.size()+" records");
				layout.show(parent, RECORDINGS_CARD);
			} else if (ac == "species") {
				currentRecording = null;
				databaseCard.recordingRemarksPanel.remarksTextArea.setText("");
				databaseCard.databaseOpPanel.disableButtons();
				layout.show(parent, SPECIES_CARD);
			}
			recordingsBrowser.repaint();
		}
	}
	
	private class SpeciesBrowser extends JPanel {
		private final int[] COLUMN_WIDTH = { 100, 100, 100, 200 };
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Set<Species> speciesList;
		private final SpeciesTableListener speciesTableListener;
		private final JButton recordingsButton;
		
		SpeciesBrowser() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			speciesList = Species.retrieve();
			JTable speciesTable = new JTable(new SpeciesTableModel());
			for (int i = 0; i < speciesTable.getColumnCount(); i++)
				speciesTable.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTH[i]);
			speciesTableListener = new SpeciesTableListener();
			speciesTable.getSelectionModel().addListSelectionListener(speciesTableListener);
			JScrollPane scrollPane = new JScrollPane(speciesTable);
			add(scrollPane);
			recordingsButton = new JButton("Recordings");
			recordingsButton.setActionCommand("recordings");
			recordingsButton.addActionListener(databaseBrowserActionListener);
			recordingsButton.setEnabled(false);
			add(recordingsButton);
		}

		private class SpeciesTableListener implements ListSelectionListener {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (lsm.getValueIsAdjusting())
					return;
				int selectedRow = lsm.getMinSelectionIndex();
				if (selectedRow == -1) {
					recordingsButton.setEnabled(false);
					currentSpecies = null;
				} else {
					recordingsButton.setEnabled(true);
					currentSpecies = (Species) speciesList.toArray()[selectedRow];
					speciesLabel.setText(currentSpecies.toString());
					setRecordingList();
//					System.out.println("Species ID: "+ currentSpecies.getDbKey());
				}
			}
		}

		private static final int FAMILY_COL = 0,
				GENUS_COL = 1,
				SPECIES_COL = 2,
				COMMON_NAME_COL = 3;
		
		private class SpeciesTableModel extends AbstractTableModel {
			private static final long serialVersionUID = 1L;
			private String[] columnName = { "Family",
				"Genus",
				"Species",
				"Common Name"
			};

			@Override
			public int getRowCount() { return speciesList.size(); }

			@Override
			public int getColumnCount() { return columnName.length; }

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				Species species = (Species) speciesList.toArray()[rowIndex];
				if (columnIndex == FAMILY_COL)
					return species.getFamily();
				else if (columnIndex == GENUS_COL)
					return species.getGenus();
				else if (columnIndex == SPECIES_COL)
					return species.getSpecies();
				else if (columnIndex == COMMON_NAME_COL)
					return species.getCommonName();
				return null;
			}

			@Override
			public String getColumnName(int column) { return columnName[column]; }
		}
	}
	
	private class RecordingsBrowser extends JPanel {
		private final int[] COLUMN_WIDTH = { 90, 100, 50, 40, 160, 100, 80, 160, 200, 60, 200};
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JButton speciesButton;
		private final RecordingTableListener recordingTableListener;
		private final RecordingPanel recordingPanel;
		private JTable recordingTable;
		
		RecordingsBrowser() {
			setRecordingList();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			recordingTableListener = new RecordingTableListener();
			recordingTable = new JTable(new RecordingTableModel());
			speciesLabel = new JLabel();
			add(speciesLabel);
			recordingPanel = new RecordingPanel();
			add(recordingPanel);
			speciesButton = new JButton("Species");
			speciesButton.setActionCommand("species");
			speciesButton.addActionListener(databaseBrowserActionListener);
			speciesButton.setEnabled(true);
			add(speciesButton);
			
		}

		private class RecordingTableListener implements ListSelectionListener {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (lsm.getValueIsAdjusting())
					return;
				int selectedRow = lsm.getMinSelectionIndex();
				if (selectedRow == -1) {
					currentRecording = null;
					databaseCard.recordingRemarksPanel.remarksTextArea.setText("");
					databaseCard.databaseOpPanel.disableButtons();
					
				} else {
					currentRecording = recordingList.get(selectedRow);
					databaseCard.recordingRemarksPanel.remarksTextArea.setText(currentRecording.getRemarks());
					databaseCard.databaseOpPanel.enableButtons();
				}
			}
		}
		
		private class CustomCellRenderer extends DefaultTableCellRenderer {
			/* (non-Javadoc)
			 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
			*/
			public Component getTableCellRendererComponent(JTable table, Object value,
			    boolean isSelected, boolean hasFocus, int row, int column) {

				Component rendererComp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
			     row, column);
			   //Set foreground color
	//		   rendererComp.setForeground(Color.red);
			   //Set background color
			   if (row > -1) {
				   HasSonogramsKey key = new HasSonogramsKey(recordingList.get(row).getId(),
					   databaseCard.onsetPreference.getOpId(),
					   databaseCard.sonogramPreference.getSpId());
				   if (databaseCard.hasSonogramsSet.contains(key)) {
					   Color bgc = new Color(215, 235, 225);
					   rendererComp .setBackground(bgc);
				   }
				   else {
					   Color bgc = new Color(215, 215, 215);
					   rendererComp.setBackground(bgc);
				   }
			   }
			   return rendererComp ;
			  }
		 }
		
		 private class RecordingPanel extends JPanel {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			
			final CustomCellRenderer renderer;
			private class RecordingTable extends JTable {
				   public RecordingTable(RecordingTableModel recordingTableModel) { super(recordingTableModel); }

				@Override
				   public TableCellRenderer getCellRenderer(int row, int column) {
				    // TODO Auto-generated method stub
				    return renderer;
				   }
			}

			RecordingPanel() {
				super();
				renderer = new CustomCellRenderer();        

				setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
//				recordingTable = new JTable(new RecordingTableModel());
				recordingTable = new RecordingTable(new RecordingTableModel());
				recordingTable.getSelectionModel().addListSelectionListener(recordingTableListener);
				for (int i = 0; i < recordingTable.getColumnCount(); i++)
					recordingTable.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTH[i]);
				JScrollPane scrollPane = new JScrollPane(recordingTable);
				scrollPane.setPreferredSize(new Dimension(1260, 300));
				add(scrollPane);
			}
		}
		
		private class RecordingTableModel extends AbstractTableModel {
			private static final int RECORDING_ID_COL = 0,
					SUBSPECIES_COL = 1,
					LENGTH_COL = 2,
					FORMAT_COL = 3,
					RECORDIST_COL = 4,
					DATE_COL = 5,
					TIME_COL = 6,
					COUNTRY_COL = 7,
					LOCATION_COL = 8,
					ELEVATION_COL = 9,
					TYPE_COL = 10;
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private String[] columnName = { "ID",
					"Subspecies",
					"Length",
					"Format",
					"Recordist",
					"Date",
					"Time",
					"Country",
					"Location",
					"Elevation",
					"Type"
			};
			
			@Override
			public int getRowCount() { return recordingList.size(); }

			@Override
			public int getColumnCount() { return columnName.length; }

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				Recording recording = recordingList.get(rowIndex);
				if (columnIndex == RECORDING_ID_COL)
					return recording.getId();
				else if (columnIndex == SUBSPECIES_COL)
					return recording.getSubspecies();
				else if (columnIndex == LENGTH_COL)
					return recording.getLength();
				else if (columnIndex == FORMAT_COL)
					return recording.getFormat();
				else if (columnIndex == RECORDIST_COL)
					return recording.getRecordist();
				else if (columnIndex == DATE_COL)
					return recording.getDate();
				else if (columnIndex == TIME_COL)
					return recording.getTime();
				else if (columnIndex == COUNTRY_COL)
					return recording.getCountry();
				else if (columnIndex == LOCATION_COL)
					return recording.getLocation();
				else if (columnIndex == ELEVATION_COL)
					return recording.getElevation();
				else if (columnIndex == TYPE_COL)
					return recording.getType();
				return null;
			}

			@Override
			public String getColumnName(int column) { return columnName[column]; }
		}		

	}
	
	
	public void removeRecording() {
		
	}

	public Recording getCurrentRecording() {
		return currentRecording;
	}

	public void setCurrentRecording(Recording currentRecording) {
		this.currentRecording = currentRecording;
	}
}
