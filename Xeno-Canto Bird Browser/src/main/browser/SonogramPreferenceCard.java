package main.browser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import main.sonogram.SonogramPreference;
import main.sonogram.SonogramPreference.EnumWindowFunction;

public class SonogramPreferenceCard extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Browser browser;
	private final SonogramPreferenceListener sonogramPreferenceListener;
	private final JButton closeButton;	
	private final JPanel navBar;
	private final JLabel tagLabel;
	private final JTextField tagTextField;
	private final JLabel bufferSizeLabel;
	private final JTextField bufferSizeTextField;
	private final JLabel windowFunctionLabel;
	private final JComboBox<EnumWindowFunction> windowFunctionComboBox;
	private final JLabel loCutoffLabel;
	private final JTextField loCutoffTextField;
	private final JLabel hiCutoffLabel;
	private final JTextField hiCutoffTextField;
	private final JLabel componentsLabel;
	private final JTextField componentsTextField;
	private final JButton addSonogramPreferenceRecButton;
	private final JButton removeSonogramPreferenceRecButton;
	private final JPanel editor;
	private final JPanel spacerPanel;
	private final JPanel infoPanel;
	
	private JTable table = null;

	List<SonogramPreference> sonogramPreferenceList;
	
	SonogramPreferenceCard(Browser b) {
		browser = b;
		setLayout(new BorderLayout());
		sonogramPreferenceListener = new SonogramPreferenceListener();
		tagLabel = new JLabel("Tag");
		windowFunctionLabel = new JLabel("Window Function");
		bufferSizeLabel = new JLabel("Buffer Size");
		loCutoffLabel = new JLabel("Low Frequency Cutoff (Hz)");
		hiCutoffLabel = new JLabel("Hi Frequency Cutoff (Hz)");
		componentsLabel = new JLabel("Components");		
		tagTextField = new JTextField();
		tagTextField.setActionCommand("sono tag change");
		tagTextField.addActionListener(sonogramPreferenceListener);
		windowFunctionComboBox = new JComboBox<EnumWindowFunction>();
		EnumWindowFunction[] wf = EnumWindowFunction.values();
		for (int i=0; i<wf.length; i++)
			windowFunctionComboBox.addItem(wf[i]);
		windowFunctionComboBox.setActionCommand("sono window function change");
		windowFunctionComboBox.addActionListener(sonogramPreferenceListener);
		bufferSizeTextField = new JTextField();
		bufferSizeTextField.setActionCommand("sono buffer size change");
		bufferSizeTextField.addActionListener(sonogramPreferenceListener);
		loCutoffTextField = new JTextField();
		loCutoffTextField.setActionCommand("sono lo cutoff change");
		loCutoffTextField.addActionListener(sonogramPreferenceListener);
		hiCutoffTextField = new JTextField();
		hiCutoffTextField.setActionCommand("sono hi cutoff change");
		hiCutoffTextField.addActionListener(sonogramPreferenceListener);
		componentsTextField = new JTextField();
		componentsTextField.setActionCommand("sono components change");
		componentsTextField.addActionListener(sonogramPreferenceListener);
		closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(sonogramPreferenceListener);
		addSonogramPreferenceRecButton = new JButton("Add Record");
		addSonogramPreferenceRecButton.setActionCommand("add sonogram settings rec");
		addSonogramPreferenceRecButton.addActionListener(sonogramPreferenceListener);
		removeSonogramPreferenceRecButton = new JButton("Remove");
		removeSonogramPreferenceRecButton.setActionCommand("remove");
		removeSonogramPreferenceRecButton.addActionListener(sonogramPreferenceListener);
		removeSonogramPreferenceRecButton.setEnabled(false);

		sonogramPreferenceList = SonogramPreference.retrieve();
		navBar = new SonogramPreferenceSelect();
		add(navBar, BorderLayout.NORTH);
		editor = new Editor();
		spacerPanel = new JPanel();
		spacerPanel.setPreferredSize(new Dimension(150, 20));
		add(spacerPanel, BorderLayout.WEST);
		add(editor, BorderLayout.CENTER);
		infoPanel = new ActionBar();
		add(infoPanel, BorderLayout.SOUTH);
	}

	private class SonogramPreferenceListener implements ActionListener, ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) return;
	        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
	        if (lsm.isSelectionEmpty()) {
	        	removeSonogramPreferenceRecButton.setEnabled(false);
	        } else {
	            int selectedRow = lsm.getMinSelectionIndex();
	            long spid = sonogramPreferenceList.get(selectedRow).getSpId();
	            browser.databaseCard.sonogramPreference = SonogramPreference.retrieve(spid);
	            removeSonogramPreferenceRecButton.setEnabled(true);
	            loadEditorValues();
	            editor.invalidate();
	            revalidate();
	        }
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String ac = e.getActionCommand();
			if (ac == "add sonogram settings rec") {
				storeSonogramPreferenceEditorValues();
				browser.databaseCard.sonogramPreference.create();
				table.invalidate();
				revalidate();
			}
			else if (ac == "remove") {
				browser.databaseCard.sonogramPreference.remove();
				table.invalidate();
				revalidate();
			}
			else if (ac == "close")
				browser.showContent();
			else if (ac == "sono tag change")
				browser.databaseCard.sonogramPreference.setTag(tagTextField.getText());
			else if (ac == "sono window function change")
				browser.databaseCard.sonogramPreference.setEnumWindowFunction((EnumWindowFunction) windowFunctionComboBox.getSelectedItem());
			else if (ac == "sono buffer size change")
				browser.databaseCard.sonogramPreference.setBufferSize(Integer.parseInt(bufferSizeTextField.getText()));
			else if (ac == "sono lo cutoff change")
				browser.databaseCard.sonogramPreference.setLoCutoff(Integer.parseInt(loCutoffTextField.getText()));
			else if (ac == "sono hi cutoff change")
				browser.databaseCard.sonogramPreference.setHiCutoff(Integer.parseInt(hiCutoffTextField.getText()));
			else if (ac == "sono components change")
				browser.databaseCard.sonogramPreference.setComponents(Integer.parseInt(componentsTextField.getText()));
			else
				System.out.println("Unsupported action: "+ac);
		}
	}
	
	private class SonogramPreferenceSelect extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		SonogramPreferenceSelect() {
			table = new JTable(new SonoParamTableModel());
			table.setPreferredScrollableViewportSize(new Dimension(300, 100));
			table.setFillsViewportHeight(true);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ListSelectionModel lsm = table.getSelectionModel();
			lsm.addListSelectionListener(sonogramPreferenceListener);
			add(new JScrollPane(table), BorderLayout.CENTER);
		}
	}
	
	private class Editor extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		Editor() {
			GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			setSonogramComponents();
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(tagLabel)
							.addComponent(tagTextField, 150, 150, 150)
							.addComponent(bufferSizeLabel)
							.addComponent(bufferSizeTextField, 60, 60, 60))
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(windowFunctionLabel)
							.addComponent(windowFunctionComboBox, 200, 200, 200)
							.addComponent(loCutoffLabel)
							.addComponent(loCutoffTextField, 60, 60, 60)
							.addComponent(hiCutoffLabel)
							.addComponent(hiCutoffTextField, 60, 60, 60)
							.addComponent(componentsLabel)
							.addComponent(componentsTextField, 60, 60, 60)));
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
							.addComponent(tagLabel)
							.addComponent(windowFunctionLabel))
					.addGroup(layout.createParallelGroup()
							.addComponent(tagTextField, 20, 20, 20)
							.addComponent(windowFunctionComboBox, 20, 20, 20))
					.addGroup(layout.createParallelGroup()
							.addComponent(bufferSizeLabel)
							.addComponent(loCutoffLabel))
					.addGroup(layout.createParallelGroup()
							.addComponent(bufferSizeTextField, 20, 20, 20)
							.addComponent(loCutoffTextField, 20, 20, 20))
					.addComponent(hiCutoffLabel)
					.addComponent(hiCutoffTextField, 20, 20, 20)
					.addComponent(componentsLabel)
					.addComponent(componentsTextField, 20, 20, 20));
		}
		
		private void setSonogramComponents() {
		}
	}
	
	private class ActionBar extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		ActionBar() {
			add(closeButton);
			add(addSonogramPreferenceRecButton);
			add(removeSonogramPreferenceRecButton);
		}
	}

	private void loadEditorValues() {
		tagTextField.setText(browser.databaseCard.sonogramPreference.getTag());
		windowFunctionComboBox.setSelectedItem(browser.databaseCard.sonogramPreference.getEnumWindowFunction());
		bufferSizeTextField.setText(""+browser.databaseCard.sonogramPreference.getBufferSize());
		loCutoffTextField.setText(""+browser.databaseCard.sonogramPreference.getLoCutoff());
		hiCutoffTextField.setText(""+browser.databaseCard.sonogramPreference.getHiCutoff());
		componentsTextField.setText(""+browser.databaseCard.sonogramPreference.getComponents());
	}
	
	private void storeSonogramPreferenceEditorValues() {
		browser.databaseCard.sonogramPreference.setTag(tagTextField.getText());
		browser.databaseCard.sonogramPreference.setEnumWindowFunction((EnumWindowFunction) windowFunctionComboBox.getSelectedItem());
		try {
			browser.databaseCard.sonogramPreference.setBufferSize(Integer.parseInt(bufferSizeTextField.getText()));
			browser.databaseCard.sonogramPreference.setLoCutoff(Integer.parseInt(loCutoffTextField.getText()));
			browser.databaseCard.sonogramPreference.setHiCutoff(Integer.parseInt(hiCutoffTextField.getText()));
			browser.databaseCard.sonogramPreference.setComponents(Integer.parseInt(componentsTextField.getText()));
		} catch (NumberFormatException e) { }
	}
	
	private class SonoParamTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnName = {
				"ID",
				"Tag"
		};
		
		@Override
		public String getColumnName(int column) { return columnName[column]; }

		@Override
		public int getRowCount() { return sonogramPreferenceList.size(); }

		@Override
		public int getColumnCount() { return columnName.length; }

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex >= 0 && rowIndex < sonogramPreferenceList.size()) {
				SonogramPreference sp = sonogramPreferenceList.get(rowIndex);
				if (columnIndex == 0)
					return sp.getSpId();
				else if (columnIndex == 1)
					return sp.getTag();
			}
			return null;
		}
	}
}
